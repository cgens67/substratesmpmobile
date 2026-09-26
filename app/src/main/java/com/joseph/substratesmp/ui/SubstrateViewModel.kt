package com.joseph.substratesmp.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.data.model.DefaultChannels
import com.joseph.substratesmp.data.repository.AuthRepository
import com.joseph.substratesmp.data.repository.ChatRepository
import com.joseph.substratesmp.voice.AgoraVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubstrateViewModel(application: Application) : AndroidViewModel(application) {
  private val TAG = "SubstrateVM"

  val authRepository = AuthRepository(application)
  val chatRepository = ChatRepository(application)
  val voiceManager = AgoraVoiceManager(application)

  val userState = authRepository.userState
  val messages = chatRepository.messagesFlow
  val activeVoiceRoom = voiceManager.voiceRoomState
  val agoraSettings = voiceManager.settings

  private val _channels = MutableStateFlow(DefaultChannels)
  val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

  private val _activeChannel = MutableStateFlow(DefaultChannels[1]) // Default to #general-chat
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
      chatRepository.updateLocalGamertag(authRepository.userState.value.gamertag)
      syncLiveChannels()
    }
  }

  private fun syncLiveChannels() {
    try {
      val firestore = FirebaseFirestore.getInstance()
      firestore.collection("channels")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Live channels listener warning: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            val remoteChannels = snapshot.documents.mapNotNull { doc ->
              val id = doc.id
              val name = doc.getString("name") ?: id
              val typeStr = doc.getString("type") ?: "TEXT"
              val category = doc.getString("category") ?: "CHANNELS"
              val description = doc.getString("description") ?: ""
              val type = try {
                ChannelType.valueOf(typeStr)
              } catch (_: Exception) {
                ChannelType.TEXT
              }
              Channel(
                id = id,
                name = name,
                type = type,
                category = category,
                description = description,
                unreadCount = 0,
                activeUsersCount = 0
              )
            }
            if (remoteChannels.isNotEmpty()) {
              _channels.value = remoteChannels
              // Keep active channel reference in sync if it exists in remote
              remoteChannels.find { it.id == _activeChannel.value.id }?.let {
                _activeChannel.value = it
              }
            }
          } else {
            // Seed Firestore with the default realm channels so remote peers see them
            DefaultChannels.forEach { ch ->
              firestore.collection("channels").document(ch.id).set(
                hashMapOf(
                  "name" to ch.name,
                  "type" to ch.type.name,
                  "category" to ch.category,
                  "description" to ch.description
                )
              )
            }
          }
        }
    } catch (e: Exception) {
      Log.w(TAG, "Channel sync fallback: ${e.message}")
    }
  }

  fun selectChannel(channel: Channel) {
    if (channel.type == ChannelType.TEXT) {
      _activeChannel.value = channel
      chatRepository.selectChannel(channel.id)
    } else if (channel.type == ChannelType.VOICE) {
      // Connect to live Agora RTC voice channel
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
    chatRepository.updateLocalGamertag(newGamertag)
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
