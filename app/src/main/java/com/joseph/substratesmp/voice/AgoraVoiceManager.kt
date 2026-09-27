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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AgoraSettings(
  val appId: String = AgoraVoiceManager.DEFAULT_AGORA_APP_ID,
  val appCertificate: String = AgoraVoiceManager.DEFAULT_AGORA_APP_CERTIFICATE,
  val token: String = "",
  val channelProfile: Int = Constants.CHANNEL_PROFILE_COMMUNICATION
)

class AgoraVoiceManager(private val context: Context? = null) {
  private val TAG = "AgoraVoiceManager"
  private val scope = CoroutineScope(Dispatchers.Main + Job())

  companion object {
    const val DEFAULT_AGORA_APP_ID = "f2cf0761f7584f48b8d647b7b29c8572"
    const val DEFAULT_AGORA_APP_CERTIFICATE = "1f85f7a31c4e4c4fb02551414a89b2da"
  }

  private var rtcEngine: RtcEngine? = null
  private var currentGamertag: String = ""
  private var activeNumericUid: Int = 0

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
      appCertificate = getPrefs()?.getString("agora_app_certificate", DEFAULT_AGORA_APP_CERTIFICATE) ?: DEFAULT_AGORA_APP_CERTIFICATE,
      token = getPrefs()?.getString("agora_token", "") ?: ""
    )
  )
  val settings: StateFlow<AgoraSettings> = _settings.asStateFlow()

  private val rtcEventHandler = object : IRtcEngineEventHandler() {
    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
      Log.i(TAG, "Live Agora onJoinChannelSuccess: channel=$channel, uid=$uid, elapsed=${elapsed}ms")
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        val isAdmin = currentGamertag.equals("Siang5680", ignoreCase = true)
        val localUser = VoiceParticipant(
          id = "local_$uid",
          name = if (isAdmin) "$currentGamertag (Admin)" else "$currentGamertag (You)",
          isSpeaking = false,
          isMuted = current.isMuted,
          isLocal = true,
          isAdmin = isAdmin,
          audioLevel = 0.0f
        )
        val remoteExisting = current.participants.filter { !it.isLocal }
        _voiceRoomState.value = current.copy(
          isConnected = true,
          isConnecting = false,
          statusMessage = "Connected to live Agora channel: $channel",
          participants = listOf(localUser) + remoteExisting
        )
      }
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
      Log.i(TAG, "Live Agora remote user joined: uid=$uid, elapsed=${elapsed}ms")
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        val newParticipant = VoiceParticipant(
          id = "remote_$uid",
          name = "Player_$uid",
          isSpeaking = false,
          isMuted = false,
          isLocal = false,
          isAdmin = false,
          audioLevel = 0.0f
        )
        if (current.participants.none { it.id == newParticipant.id }) {
          _voiceRoomState.value = current.copy(
            participants = current.participants + newParticipant
          )
        }
      }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
      Log.i(TAG, "Live Agora remote user offline: uid=$uid, reason=$reason")
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        _voiceRoomState.value = current.copy(
          participants = current.participants.filterNot { it.id == "remote_$uid" }
        )
      }
    }

    override fun onUserMuteAudio(uid: Int, muted: Boolean) {
      Log.i(TAG, "Live Agora remote user audio mute changed: uid=$uid, muted=$muted")
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        _voiceRoomState.value = current.copy(
          participants = current.participants.map {
            if (it.id == "remote_$uid") it.copy(isMuted = muted) else it
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
            if (participant.isLocal) sp.uid == 0 || sp.uid == activeNumericUid
            else participant.id == "remote_${sp.uid}"
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
              audioLevel = 0.0f
            )
          }
        }
        _voiceRoomState.value = current.copy(participants = updatedParticipants)
      }
    }

    override fun onLeaveChannel(stats: RtcStats?) {
      Log.i(TAG, "Live Agora onLeaveChannel: totalDuration=${stats?.totalDuration ?: 0}")
      scope.launch {
        _voiceRoomState.value = null
      }
    }

    override fun onError(err: Int) {
      Log.e(TAG, "Live Agora onError: code=$err")
      scope.launch {
        _voiceRoomState.value = _voiceRoomState.value?.copy(
          statusMessage = "Agora error: code $err"
        )
      }
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

  fun updateSettings(appId: String, appCertificate: String, token: String) {
    getPrefs()?.edit()
      ?.putString("agora_app_id", appId)
      ?.putString("agora_app_certificate", appCertificate)
      ?.putString("agora_token", token)
      ?.apply()

    _settings.value = AgoraSettings(appId, appCertificate, token)

    try {
      rtcEngine?.leaveChannel()
      RtcEngine.destroy()
    } catch (_: Throwable) {}
    rtcEngine = null
    initAgoraEngine()
  }

  fun joinVoiceChannel(channelId: String, channelName: String, localGamertag: String) {
    currentGamertag = localGamertag

    if (rtcEngine == null) {
      initAgoraEngine()
    }

    try {
      rtcEngine?.leaveChannel()
    } catch (_: Throwable) {}

    val isAdmin = localGamertag.equals("Siang5680", ignoreCase = true)
    // Derive a stable positive 32-bit integer UID from localGamertag
    val numericUid = (localGamertag.hashCode().toLong() and 0x7FFFFFFFL).toInt().let { if (it <= 0) 10001 else it }
    activeNumericUid = numericUid

    val pendingLocalUser = VoiceParticipant(
      id = "local_$numericUid",
      name = if (isAdmin) "$localGamertag (Admin)" else "$localGamertag (You)",
      isSpeaking = false,
      isMuted = false,
      isLocal = true,
      isAdmin = isAdmin,
      audioLevel = 0.0f
    )

    _voiceRoomState.value = ActiveVoiceRoom(
      channelId = channelId,
      channelName = channelName,
      isConnected = false,
      isConnecting = true,
      isMuted = false,
      isDeafened = false,
      isSpeakerOn = true,
      isCameraOn = false,
      participants = listOf(pendingLocalUser),
      appId = _settings.value.appId,
      statusMessage = "Connecting to Agora RTC channel: $channelId..."
    )

    val sanitizedChannel = channelId.replace("-", "_")
    val expirationSeconds = 24 * 3600
    val privilegeTs = ((System.currentTimeMillis() / 1000) + expirationSeconds).toInt()

    // 1. Generate client-side token locally using RtcTokenBuilder if no custom token was provided
    val token = if (_settings.value.token.isNotBlank()) {
      _settings.value.token
    } else {
      try {
        RtcTokenBuilder().buildTokenWithUid(
          appId = _settings.value.appId,
          appCertificate = _settings.value.appCertificate,
          channelName = sanitizedChannel,
          uid = numericUid,
          role = RtcTokenBuilder.Role.Role_Publisher,
          privilegeTs = privilegeTs
        )
      } catch (e: Throwable) {
        Log.e(TAG, "Error generating Agora token client-side: ${e.message}", e)
        ""
      }
    }

    // 2. Pass generated token, sanitized channel, and numeric UID to joinChannel
    try {
      rtcEngine?.setEnableSpeakerphone(true)
      rtcEngine?.muteLocalAudioStream(false)
      val joinCode = rtcEngine?.joinChannel(token, sanitizedChannel, "", numericUid)
      Log.i(TAG, "Live Agora joinChannel($sanitizedChannel, uid=$numericUid) returned code: $joinCode")
    } catch (e: Throwable) {
      Log.e(TAG, "Live Agora joinChannel error: ${e.message}", e)
      _voiceRoomState.value = _voiceRoomState.value?.copy(
        isConnecting = false,
        statusMessage = "Connection failed: ${e.message}"
      )
    }
  }

  fun toggleMute() {
    val current = _voiceRoomState.value ?: return
    val newMute = !current.isMuted

    try {
      rtcEngine?.muteLocalAudioStream(newMute)
    } catch (e: Throwable) {
      Log.e(TAG, "muteLocalAudioStream error: ${e.message}")
    }

    _voiceRoomState.value = current.copy(
      isMuted = newMute,
      participants = current.participants.map {
        if (it.isLocal) it.copy(isMuted = newMute) else it
      }
    )
  }

  fun toggleSpeaker() {
    val current = _voiceRoomState.value ?: return
    val newSpeaker = !current.isSpeakerOn

    try {
      rtcEngine?.setEnableSpeakerphone(newSpeaker)
    } catch (e: Throwable) {
      Log.e(TAG, "setEnableSpeakerphone error: ${e.message}")
    }

    _voiceRoomState.value = current.copy(isSpeakerOn = newSpeaker)
  }

  fun toggleDeafen() {
    val current = _voiceRoomState.value ?: return
    val newDeafen = !current.isDeafened
    val newMute = if (newDeafen) true else current.isMuted

    try {
      rtcEngine?.muteAllRemoteAudioStreams(newDeafen)
      rtcEngine?.muteLocalAudioStream(newMute)
    } catch (e: Throwable) {
      Log.e(TAG, "toggleDeafen error: ${e.message}")
    }

    _voiceRoomState.value = current.copy(
      isDeafened = newDeafen,
      isMuted = newMute,
      participants = current.participants.map {
        if (it.isLocal) it.copy(isMuted = newMute, isDeafened = newDeafen)
        else it
      }
    )
  }

  fun toggleCamera() {
    val current = _voiceRoomState.value ?: return
    val newCam = !current.isCameraOn

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

    _voiceRoomState.value = current.copy(isCameraOn = newCam)
  }

  fun disconnect() {
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
