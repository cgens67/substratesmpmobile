package com.joseph.substratesmp.voice

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.SurfaceView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.joseph.substratesmp.SubstrateApplication
import com.joseph.substratesmp.data.model.ActiveVoiceRoom
import com.joseph.substratesmp.data.model.VoiceParticipant
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
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
  private var currentChannelId: String = ""
  private var voiceMembersListener: ListenerRegistration? = null

  private val uidToUserMap = mutableMapOf<Int, Pair<String, Boolean>>()

  private fun getSafeContext(): Context? {
    val ctx = context?.applicationContext ?: context
    if (ctx != null) return ctx
    return SubstrateApplication.instance
  }

  private fun getPrefs(): SharedPreferences? {
    return getSafeContext()?.getSharedPreferences("substrate_agora_prefs", Context.MODE_PRIVATE)
  }

  private val firestore: FirebaseFirestore by lazy {
    val safeCtx = getSafeContext()
    if (safeCtx != null && FirebaseApp.getApps(safeCtx).isEmpty()) {
      FirebaseApp.initializeApp(safeCtx)
    }
    FirebaseFirestore.getInstance()
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
      Log.i(TAG, "Live Agora onJoinChannelSuccess: channel=$channel, uid=$uid")
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
          statusMessage = "Connected • Boosted Audio",
          participants = listOf(localUser) + remoteExisting
        )
      }
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
      Log.i(TAG, "Live Agora remote user joined: uid=$uid")
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        val resolved = resolveGamertag(uid)
        val newParticipant = VoiceParticipant(
          id = "remote_$uid",
          name = resolved.first,
          isSpeaking = false,
          isMuted = false,
          isLocal = false,
          isAdmin = resolved.second,
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
      scope.launch {
        val current = _voiceRoomState.value ?: return@launch
        _voiceRoomState.value = current.copy(
          participants = current.participants.filterNot { it.id == "remote_$uid" }
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
          if (speakerMatch != null && speakerMatch.volume > 4) {
            val normalizedLevel = (speakerMatch.volume / 255.0f).coerceIn(0.15f, 1.0f)
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
      scope.launch {
        _voiceRoomState.value = null
      }
    }
  }

  init {
    initAgoraEngine()
  }

  fun initAgoraEngine() {
    if (rtcEngine != null) return
    val validContext = getSafeContext() ?: return

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

      // Audio volume boost: 200% recording and playback amplification
      rtcEngine?.adjustRecordingSignalVolume(200)
      rtcEngine?.adjustPlaybackSignalVolume(200)
      rtcEngine?.setEnableSpeakerphone(true)

      Log.i(TAG, "Agora RtcEngine initialized with amplified audio volume")
    } catch (e: Throwable) {
      Log.w(TAG, "Agora RtcEngine initialization fallback: ${e.message}")
    }
  }

  fun setupLocalVideoCanvas(surfaceView: SurfaceView) {
    try {
      rtcEngine?.enableVideo()
      rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
      rtcEngine?.startPreview()
    } catch (e: Throwable) {
      Log.e(TAG, "Error setting up local video: ${e.message}")
    }
  }

  fun setupRemoteVideoCanvas(surfaceView: SurfaceView, uid: Int) {
    try {
      rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    } catch (e: Throwable) {
      Log.e(TAG, "Error setting up remote video for uid $uid: ${e.message}")
    }
  }

  fun switchCamera() {
    try {
      rtcEngine?.switchCamera()
    } catch (e: Throwable) {
      Log.e(TAG, "switchCamera error: ${e.message}")
    }
  }

  fun joinVoiceChannel(channelId: String, channelName: String, localGamertag: String, isVideo: Boolean = false) {
    currentGamertag = localGamertag
    currentChannelId = channelId

    if (rtcEngine == null) {
      initAgoraEngine()
    }

    try {
      rtcEngine?.leaveChannel()
    } catch (_: Throwable) {}

    val isAdmin = localGamertag.equals("Siang5680", ignoreCase = true)
    val numericUid = (localGamertag.hashCode().toLong() and 0x7FFFFFFFL).toInt().let { if (it <= 0) 10001 else it }
    activeNumericUid = numericUid

    uidToUserMap[numericUid] = Pair(localGamertag, isAdmin)
    syncVoicePresenceToFirestore(channelId, numericUid, localGamertag, isAdmin)
    startVoiceMembersListener(channelId)

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
      isCameraOn = isVideo,
      participants = listOf(pendingLocalUser),
      appId = _settings.value.appId,
      statusMessage = if (isVideo) "Connecting video call..." else "Connecting voice call..."
    )

    val sanitizedChannel = channelId.replace("-", "_")
    val privilegeTs = ((System.currentTimeMillis() / 1000) + 24 * 3600).toInt()

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
        Log.e(TAG, "Error generating Agora token: ${e.message}", e)
        ""
      }
    }

    try {
      rtcEngine?.setEnableSpeakerphone(true)
      rtcEngine?.adjustRecordingSignalVolume(200)
      rtcEngine?.adjustPlaybackSignalVolume(200)

      if (isVideo) {
        rtcEngine?.enableVideo()
        rtcEngine?.muteLocalVideoStream(false)
        rtcEngine?.startPreview()
      } else {
        rtcEngine?.disableVideo()
      }

      rtcEngine?.muteLocalAudioStream(false)
      rtcEngine?.joinChannel(token, sanitizedChannel, "", numericUid)
    } catch (e: Throwable) {
      Log.e(TAG, "Agora join error: ${e.message}", e)
    }
  }

  private fun syncVoicePresenceToFirestore(channelId: String, uid: Int, gamertag: String, isAdmin: Boolean) {
    try {
      firestore.collection("channels")
        .document(channelId)
        .collection("voiceMembers")
        .document(uid.toString())
        .set(
          hashMapOf(
            "uid" to uid,
            "gamertag" to gamertag,
            "isAdmin" to isAdmin,
            "joinedAt" to System.currentTimeMillis()
          )
        )
    } catch (_: Exception) {}
  }

  private fun startVoiceMembersListener(channelId: String) {
    voiceMembersListener?.remove()
    voiceMembersListener = firestore.collection("channels")
      .document(channelId)
      .collection("voiceMembers")
      .addSnapshotListener { snapshot, _ ->
        if (snapshot != null) {
          for (doc in snapshot.documents) {
            val uid = doc.getLong("uid")?.toInt() ?: continue
            val gamertag = doc.getString("gamertag") ?: continue
            val isAdmin = doc.getBoolean("isAdmin") ?: gamertag.equals("Siang5680", ignoreCase = true)
            uidToUserMap[uid] = Pair(gamertag, isAdmin)
          }

          val current = _voiceRoomState.value ?: return@addSnapshotListener
          val updated = current.participants.map { participant ->
            if (participant.isLocal) {
              participant
            } else {
              val rawUid = participant.id.removePrefix("remote_").toIntOrNull()
              if (rawUid != null && uidToUserMap.containsKey(rawUid)) {
                val (name, admin) = uidToUserMap[rawUid]!!
                participant.copy(name = name, isAdmin = admin)
              } else {
                participant
              }
            }
          }
          _voiceRoomState.value = current.copy(participants = updated)
        }
      }
  }

  private fun resolveGamertag(uid: Int): Pair<String, Boolean> {
    return uidToUserMap[uid] ?: Pair("Player_$uid", false)
  }

  fun toggleMute() {
    val current = _voiceRoomState.value ?: return
    val newMute = !current.isMuted
    try {
      rtcEngine?.muteLocalAudioStream(newMute)
    } catch (_: Throwable) {}
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
    } catch (_: Throwable) {}
    _voiceRoomState.value = current.copy(isSpeakerOn = newSpeaker)
  }

  fun toggleDeafen() {
    val current = _voiceRoomState.value ?: return
    val newDeafen = !current.isDeafened
    val newMute = if (newDeafen) true else current.isMuted
    try {
      rtcEngine?.muteAllRemoteAudioStreams(newDeafen)
      rtcEngine?.muteLocalAudioStream(newMute)
    } catch (_: Throwable) {}
    _voiceRoomState.value = current.copy(isDeafened = newDeafen, isMuted = newMute)
  }

  fun toggleCamera() {
    val current = _voiceRoomState.value ?: return
    val newCam = !current.isCameraOn
    try {
      if (newCam) {
        rtcEngine?.enableVideo()
        rtcEngine?.startPreview()
        rtcEngine?.muteLocalVideoStream(false)
      } else {
        rtcEngine?.muteLocalVideoStream(true)
      }
    } catch (_: Throwable) {}
    _voiceRoomState.value = current.copy(isCameraOn = newCam)
  }

  fun disconnect() {
    try {
      if (currentChannelId.isNotBlank() && activeNumericUid != 0) {
        firestore.collection("channels")
          .document(currentChannelId)
          .collection("voiceMembers")
          .document(activeNumericUid.toString())
          .delete()
      }
    } catch (_: Exception) {}

    voiceMembersListener?.remove()
    voiceMembersListener = null

    try {
      rtcEngine?.leaveChannel()
    } catch (_: Throwable) {}
    _voiceRoomState.value = null
  }

  fun updateSettings(appId: String, appCert: String, token: String) {
    getPrefs()?.edit()
      ?.putString("agora_app_id", appId)
      ?.putString("agora_app_certificate", appCert)
      ?.putString("agora_token", token)
      ?.apply()

    _settings.value = AgoraSettings(appId, appCert, token)
    disconnect()
    try {
      RtcEngine.destroy()
    } catch (_: Throwable) {}
    rtcEngine = null
    initAgoraEngine()
  }

  fun destroy() {
    disconnect()
    try {
      RtcEngine.destroy()
    } catch (_: Throwable) {}
    rtcEngine = null
  }
}
