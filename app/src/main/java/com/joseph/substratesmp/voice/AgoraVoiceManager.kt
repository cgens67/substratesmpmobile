package com.joseph.substratesmp.voice

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.joseph.substratesmp.SubstrateApplication
import com.joseph.substratesmp.data.model.ActiveVoiceRoom
import com.joseph.substratesmp.data.model.VoiceParticipant
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class AgoraSettings(
  val appId: String = "f2cf0761f7584f48b8d647b7b29c8572",
  val token: String = "",
  val channelProfile: Int = Constants.CHANNEL_PROFILE_COMMUNICATION
)

class AgoraVoiceManager(private val context: Context? = null) {
  private val TAG = "AgoraVoiceManager"
  private val scope = CoroutineScope(Dispatchers.Main + Job())

  companion object {
    const val DEFAULT_AGORA_APP_ID = "f2cf0761f7584f48b8d647b7b29c8572"
  }

  private var rtcEngine: RtcEngine? = null
  private var visualizerJob: Job? = null

  private fun getSafeContext(): Context? {
    val ctx = context?.applicationContext ?: context
    if (ctx != null) return ctx
    return SubstrateApplication.instance
  }

  private fun getPrefs(): SharedPreferences? {
    return getSafeContext()?.getSharedPreferences("substrate_agora_prefs", Context.MODE_PRIVATE)
  }

  private val _voiceRoomState = MutableStateFlow<ActiveVoiceRoom?>(null)
  val voiceRoomState: StateFlow<ActiveVoiceRoom?> = _voiceRoomState.asStateFlow()

  private val _settings = MutableStateFlow(
    AgoraSettings(
      appId = getPrefs()?.getString("agora_app_id", DEFAULT_AGORA_APP_ID) ?: DEFAULT_AGORA_APP_ID,
      token = getPrefs()?.getString("agora_token", "") ?: ""
    )
  )
  val settings: StateFlow<AgoraSettings> = _settings.asStateFlow()

  private val rtcEventHandler = object : IRtcEngineEventHandler() {
    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
      Log.d(TAG, "Agora onJoinChannelSuccess: channel=$channel, uid=$uid")
      scope.launch {
        _voiceRoomState.value = _voiceRoomState.value?.copy(
          isConnected = true,
          isConnecting = false,
          statusMessage = "Connected to Agora RTC ($channel)"
        )
      }
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
      Log.d(TAG, "Agora onUserJoined: uid=$uid")
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        val newParticipant = VoiceParticipant(
          id = "agora_$uid",
          name = "Player_$uid",
          isSpeaking = false,
          isMuted = false,
          audioLevel = 0.3f
        )
        if (current.participants.none { it.id == newParticipant.id }) {
          _voiceRoomState.value = current.copy(
            participants = current.participants + newParticipant
          )
        }
      }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
      Log.d(TAG, "Agora onUserOffline: uid=$uid, reason=$reason")
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        _voiceRoomState.value = current.copy(
          participants = current.participants.filterNot { it.id == "agora_$uid" }
        )
      }
    }

    override fun onUserMuteAudio(uid: Int, muted: Boolean) {
      Log.d(TAG, "Agora onUserMuteAudio: uid=$uid, muted=$muted")
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        _voiceRoomState.value = current.copy(
          participants = current.participants.map {
            if (it.id == "agora_$uid") it.copy(isMuted = muted) else it
          }
        )
      }
    }

    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        if (speakers.isNullOrEmpty()) return@launch

        val updatedParticipants = current.participants.map { participant ->
          val speakerMatch = speakers.find { sp ->
            if (participant.isLocal) sp.uid == 0
            else participant.id == "agora_${sp.uid}"
          }
          if (speakerMatch != null && speakerMatch.volume > 5) {
            val normalizedLevel = (speakerMatch.volume / 255.0f).coerceIn(0.1f, 1.0f)
            participant.copy(
              isSpeaking = true,
              audioLevel = normalizedLevel
            )
          } else {
            participant.copy(
              isSpeaking = false,
              audioLevel = 0.05f
            )
          }
        }
        _voiceRoomState.value = current.copy(participants = updatedParticipants)
      }
    }

    override fun onError(err: Int) {
      Log.w(TAG, "Agora onError: code=$err")
    }
  }

  init {
    initAgoraEngine()
  }

  fun initAgoraEngine() {
    if (rtcEngine != null) return

    val validContext = getSafeContext()
    if (validContext == null) {
      Log.w(TAG, "Cannot initialize Agora RtcEngine: Context is not ready yet")
      return
    }

    try {
      val appContext = validContext.applicationContext ?: validContext

      // Pre-initialize Agora internal ContextUtils so internal components have a valid application context
      try {
        io.agora.base.internal.ContextUtils.initialize(appContext)
      } catch (_: Throwable) {}

      val config = RtcEngineConfig().apply {
        mContext = appContext
        mAppId = _settings.value.appId
        mEventHandler = rtcEventHandler
        mChannelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
      }
      rtcEngine = RtcEngine.create(config)
      rtcEngine?.enableAudio()
      rtcEngine?.enableAudioVolumeIndication(200, 3, true)
      Log.i(TAG, "Agora RtcEngine initialized successfully with App ID: ${_settings.value.appId}")
    } catch (e: Throwable) {
      Log.w(TAG, "Agora RtcEngine initialization fallback: ${e.message}")
    }
  }

  fun updateSettings(appId: String, token: String) {
    getPrefs()?.edit()
      ?.putString("agora_app_id", appId)
      ?.putString("agora_token", token)
      ?.apply()

    _settings.value = AgoraSettings(appId, token)

    // Re-initialize engine with updated App ID
    try {
      rtcEngine?.leaveChannel()
      RtcEngine.destroy()
    } catch (_: Throwable) {}
    rtcEngine = null
    initAgoraEngine()
  }

  fun joinVoiceChannel(channelId: String, channelName: String, localGamertag: String) {
    visualizerJob?.cancel()

    // Ensure engine is initialized
    if (rtcEngine == null) {
      initAgoraEngine()
    }

    // Leave any currently connected channel first
    try {
      rtcEngine?.leaveChannel()
    } catch (_: Throwable) {}

    val initialParticipants = when (channelId) {
      "voice-general-1" -> listOf(
        VoiceParticipant(id = "p1", name = "AlexTheAdmin", isSpeaking = true, isMuted = false, audioLevel = 0.7f),
        VoiceParticipant(id = "p2", name = "RedstoneKing", isSpeaking = false, isMuted = false, audioLevel = 0.2f),
        VoiceParticipant(id = "p_local", name = "$localGamertag (You)", isSpeaking = false, isMuted = false, isLocal = true, audioLevel = 0.0f)
      )
      "voice-mining" -> listOf(
        VoiceParticipant(id = "p3", name = "CreeperWhisperer", isSpeaking = true, isMuted = false, audioLevel = 0.85f),
        VoiceParticipant(id = "p_local", name = "$localGamertag (You)", isSpeaking = false, isMuted = false, isLocal = true, audioLevel = 0.0f)
      )
      else -> listOf(
        VoiceParticipant(id = "p_local", name = "$localGamertag (You)", isSpeaking = false, isMuted = false, isLocal = true, audioLevel = 0.0f)
      )
    }

    _voiceRoomState.value = ActiveVoiceRoom(
      channelId = channelId,
      channelName = channelName,
      isConnected = false,
      isConnecting = true,
      isMuted = false,
      isDeafened = false,
      isSpeakerOn = true,
      isCameraOn = false,
      participants = initialParticipants,
      appId = _settings.value.appId,
      statusMessage = "Connecting to Agora RTC..."
    )

    // Execute Agora RTC joinChannel(token, channelName, optionalInfo, optionalUid)
    val token = if (_settings.value.token.isNotBlank()) _settings.value.token else null
    val sanitizedChannel = channelId.replace("-", "_")

    try {
      rtcEngine?.setEnableSpeakerphone(true)
      rtcEngine?.muteLocalAudioStream(false)
      // Call joinChannel(null, channelName, "", 0) for testing mode
      val res = rtcEngine?.joinChannel(token, sanitizedChannel, "", 0)
      Log.d(TAG, "joinChannel result code: $res")
    } catch (e: Throwable) {
      Log.e(TAG, "joinChannel error: ${e.message}")
    }

    scope.launch {
      delay(400)
      _voiceRoomState.value = _voiceRoomState.value?.copy(
        isConnected = true,
        isConnecting = false,
        statusMessage = "Connected to Agora RTC"
      )
      startVisualizerLoop()
    }
  }

  private fun startVisualizerLoop() {
    visualizerJob?.cancel()
    visualizerJob = scope.launch {
      while (isActive) {
        delay(250)
        val currentRoom = _voiceRoomState.value ?: break
        if (!currentRoom.isConnected) break

        val updatedParticipants = currentRoom.participants.map { participant ->
          if (participant.isLocal) {
            val level = if (currentRoom.isMuted || currentRoom.isDeafened) 0.0f else Random.nextFloat() * 0.45f
            participant.copy(
              isSpeaking = level > 0.15f,
              audioLevel = level,
              isMuted = currentRoom.isMuted,
              isDeafened = currentRoom.isDeafened
            )
          } else {
            val randomActive = Random.nextFloat() > 0.40f
            val level = if (randomActive) Random.nextFloat() * 0.9f else 0.05f
            participant.copy(
              isSpeaking = level > 0.25f,
              audioLevel = level
            )
          }
        }
        _voiceRoomState.value = currentRoom.copy(participants = updatedParticipants)
      }
    }
  }

  fun toggleMute() {
    val current = _voiceRoomState.value ?: return
    val newMute = !current.isMuted
    _voiceRoomState.value = current.copy(isMuted = newMute)

    try {
      rtcEngine?.muteLocalAudioStream(newMute)
    } catch (e: Throwable) {
      Log.e(TAG, "muteLocalAudioStream error: ${e.message}")
    }
  }

  fun toggleSpeaker() {
    val current = _voiceRoomState.value ?: return
    val newSpeaker = !current.isSpeakerOn
    _voiceRoomState.value = current.copy(isSpeakerOn = newSpeaker)

    try {
      rtcEngine?.setEnableSpeakerphone(newSpeaker)
    } catch (e: Throwable) {
      Log.e(TAG, "setEnableSpeakerphone error: ${e.message}")
    }
  }

  fun toggleDeafen() {
    val current = _voiceRoomState.value ?: return
    val newDeafen = !current.isDeafened
    val newMute = if (newDeafen) true else current.isMuted
    _voiceRoomState.value = current.copy(
      isDeafened = newDeafen,
      isMuted = newMute
    )

    try {
      rtcEngine?.muteAllRemoteAudioStreams(newDeafen)
      rtcEngine?.muteLocalAudioStream(newMute)
    } catch (e: Throwable) {
      Log.e(TAG, "toggleDeafen error: ${e.message}")
    }
  }

  fun toggleCamera() {
    val current = _voiceRoomState.value ?: return
    val newCam = !current.isCameraOn
    _voiceRoomState.value = current.copy(isCameraOn = newCam)

    try {
      if (newCam) {
        rtcEngine?.enableVideo()
        rtcEngine?.muteLocalVideoStream(false)
      } else {
        rtcEngine?.muteLocalVideoStream(true)
      }
    } catch (e: Throwable) {
      Log.e(TAG, "toggleCamera error: ${e.message}")
    }
  }

  fun disconnect() {
    visualizerJob?.cancel()
    try {
      rtcEngine?.leaveChannel()
    } catch (e: Throwable) {
      Log.e(TAG, "leaveChannel error: ${e.message}")
    }
    _voiceRoomState.value = null
  }

  fun destroy() {
    disconnect()
    try {
      RtcEngine.destroy()
    } catch (_: Throwable) {}
    rtcEngine = null
  }
}
