package com.joseph.substratesmp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.data.model.SeedChannels
import com.joseph.substratesmp.data.repository.AuthRepository
import com.joseph.substratesmp.data.repository.ChatRepository
import com.joseph.substratesmp.voice.AgoraVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubstrateViewModel(application: Application) : AndroidViewModel(application) {
  val authRepository = AuthRepository(application)
  val chatRepository = ChatRepository(application)
  val voiceManager = AgoraVoiceManager(application)

  val userState = authRepository.userState
  val messages = chatRepository.messagesFlow
  val activeVoiceRoom = voiceManager.voiceRoomState
  val agoraSettings = voiceManager.settings

  private val _channels = MutableStateFlow(SeedChannels)
  val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

  private val _activeChannel = MutableStateFlow(SeedChannels[1]) // Default to #general-chat
  val activeChannel: StateFlow<Channel> = _activeChannel.asStateFlow()

  private val _showGamertagDialog = MutableStateFlow(false)
  val showGamertagDialog: StateFlow<Boolean> = _showGamertagDialog.asStateFlow()

  private val _showAgoraDialog = MutableStateFlow(false)
  val showAgoraDialog: StateFlow<Boolean> = _showAgoraDialog.asStateFlow()

  private val _showServerInfoSheet = MutableStateFlow(false)
  val showServerInfoSheet: StateFlow<Boolean> = _showServerInfoSheet.asStateFlow()

  init {
    viewModelScope.launch {
      authRepository.initializeAuth()
    }
  }

  fun selectChannel(channel: Channel) {
    if (channel.type == ChannelType.TEXT) {
      _activeChannel.value = channel
      chatRepository.selectChannel(channel.id)
    } else if (channel.type == ChannelType.VOICE) {
      // Join voice channel via Agora RTC Engine
      val gamertag = userState.value.gamertag
      voiceManager.joinVoiceChannel(channel.id, channel.name, gamertag)
    }
  }

  fun sendMessage(content: String, coordinates: String? = null) {
    if (content.isBlank() && coordinates == null) return
    val user = userState.value
    chatRepository.sendMessage(
      channelId = _activeChannel.value.id,
      senderName = user.gamertag,
      senderRole = user.role,
      content = content.trim(),
      coordinates = coordinates
    )
  }

  fun updateGamertag(newGamertag: String, newRole: String) {
    authRepository.updateGamertag(newGamertag, newRole)
    _showGamertagDialog.value = false
  }

  fun setGamertagDialogVisible(visible: Boolean) {
    _showGamertagDialog.value = visible
  }

  fun setAgoraDialogVisible(visible: Boolean) {
    _showAgoraDialog.value = visible
  }

  fun setServerInfoSheetVisible(visible: Boolean) {
    _showServerInfoSheet.value = visible
  }

  override fun onCleared() {
    super.onCleared()
    voiceManager.destroy()
  }
}
