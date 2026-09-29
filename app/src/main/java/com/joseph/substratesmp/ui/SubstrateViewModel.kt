package com.joseph.substratesmp.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.data.model.DefaultChannels
import com.joseph.substratesmp.data.model.ServerSticker
import com.joseph.substratesmp.data.model.StatusUpdate
import com.joseph.substratesmp.data.repository.AuthRepository
import com.joseph.substratesmp.data.repository.ChatRepository
import com.joseph.substratesmp.services.OneSignalHelper
import com.joseph.substratesmp.ui.components.AdminMember
import com.joseph.substratesmp.ui.components.CallRingtoneHelper
import com.joseph.substratesmp.ui.components.NotificationHelper
import com.joseph.substratesmp.ui.components.SoundHelper
import com.joseph.substratesmp.voice.AgoraVoiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class IncomingCallData(
  val callId: String,
  val caller: String,
  val isVideo: Boolean,
  val timestamp: Long
)

data class AppSettings(
  val isNightMode: Boolean = false,
  val smoothAnimations: Boolean = true,
  val autoDownloadMedia: Boolean = true,
  val powerSaving: Boolean = false,
  val notifications: Boolean = true,
  val language: String = "English",
  val autoTranslate: Boolean = false
)

class SubstrateViewModel(application: Application) : AndroidViewModel(application) {
  val authRepository = AuthRepository(application)
  val chatRepository = ChatRepository(application)
  val voiceManager = AgoraVoiceManager(application)

  val userState = authRepository.userState
  val messages = chatRepository.messagesFlow
  val mutedChannels = chatRepository.mutedChannels
  val blockedUsers = chatRepository.blockedUsers
  val favouriteChannels = chatRepository.favouriteChannels

  val activeVoiceRoom = voiceManager.voiceRoomState
  val agoraSettings = voiceManager.settings

  private val prefs = application.getSharedPreferences("substrate_settings_prefs", Context.MODE_PRIVATE)
  private val _appSettings = MutableStateFlow(
    AppSettings(
      isNightMode = prefs.getBoolean("night_mode", false),
      smoothAnimations = prefs.getBoolean("animations", true),
      autoDownloadMedia = prefs.getBoolean("auto_download", true),
      powerSaving = prefs.getBoolean("power_saving", false),
      notifications = prefs.getBoolean("notifications", true),
      language = prefs.getString("language", "English") ?: "English",
      autoTranslate = prefs.getBoolean("auto_translate", false)
    )
  )
  val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

  private val _channels = MutableStateFlow(DefaultChannels)
  val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

  private val _activeChannel = MutableStateFlow(DefaultChannels[1])
  val activeChannel: StateFlow<Channel> = _activeChannel.asStateFlow()

  private val _statuses = MutableStateFlow<List<StatusUpdate>>(emptyList())
  val statuses: StateFlow<List<StatusUpdate>> = _statuses.asStateFlow()

  private val _stickers = MutableStateFlow<List<ServerSticker>>(emptyList())
  val stickers: StateFlow<List<ServerSticker>> = _stickers.asStateFlow()

  private val _members = MutableStateFlow<List<AdminMember>>(emptyList())
  val members: StateFlow<List<AdminMember>> = _members.asStateFlow()

  private val _deletedGamertags = MutableStateFlow<Set<String>>(emptySet())
  val deletedGamertags: StateFlow<Set<String>> = _deletedGamertags.asStateFlow()

  private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
  val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()
  private var typingListener: ListenerRegistration? = null
  private val channelMsgListeners = mutableMapOf<String, ListenerRegistration>()
  private val channelLastKnownTs = mutableMapOf<String, Long>()

  private val _translatedMessages = MutableStateFlow<Map<String, String>>(emptyMap())
  val translatedMessages: StateFlow<Map<String, String>> = _translatedMessages.asStateFlow()

  private val _currentScreenState = MutableStateFlow("home")

  private val _showGamertagDialog = MutableStateFlow(false)
  val showGamertagDialog: StateFlow<Boolean> = _showGamertagDialog.asStateFlow()

  private val _showAgoraDialog = MutableStateFlow(false)
  val showAgoraDialog: StateFlow<Boolean> = _showAgoraDialog.asStateFlow()

  private val _showServerInfoSheet = MutableStateFlow(false)
  val showServerInfoSheet: StateFlow<Boolean> = _showServerInfoSheet.asStateFlow()

  private val _showAdminConsole = MutableStateFlow(false)
  val showAdminConsole: StateFlow<Boolean> = _showAdminConsole.asStateFlow()

  private val _showSelectContactDialog = MutableStateFlow(false)
  val showSelectContactDialog: StateFlow<Boolean> = _showSelectContactDialog.asStateFlow()

  private val _incomingCall = MutableStateFlow<IncomingCallData?>(null)
  val incomingCall: StateFlow<IncomingCallData?> = _incomingCall.asStateFlow()
  private var incomingCallListener: ListenerRegistration? = null

  private var serverCoordsListener: ListenerRegistration? = null

  private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

  init {
    viewModelScope.launch {
      authRepository.initializeAuth()
      val currentGamertag = authRepository.userState.value.gamertag
      chatRepository.updateLocalGamertag(currentGamertag)

      if (currentGamertag.isBlank()) {
        _showGamertagDialog.value = true
      }
      syncLiveChannels()
      syncLiveStatuses()
      syncLiveStickers()
      syncMembers()
      syncDeletedAccounts()
      listenToTyping(_activeChannel.value.id)
      listenToIncomingCalls()
      listenToServerLiveCoordinates()

      launch {
        messages.collect { msgList ->
          if (_appSettings.value.autoTranslate) {
            val targetCode = when (_appSettings.value.language.lowercase()) {
              "chinese" -> "zh"
              "malay" -> "ms"
              else -> "en"
            }
            val myTag = userState.value.gamertag.trim()
            msgList.forEach { msg ->
              if (msg.content.isNotBlank() && !msg.senderName.equals(myTag, ignoreCase = true) && !_translatedMessages.value.containsKey(msg.id)) {
                translateMessage(msg.id, msg.content, targetCode)
              }
            }
          }
        }
      }
    }
  }

  private fun listenToServerLiveCoordinates() {
    serverCoordsListener?.remove()
    serverCoordsListener = firestore.collection("server_coords").document("live")
      .addSnapshotListener { snapshot, error ->
        if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

        val playersMap = snapshot.get("players") as? Map<*, *> ?: return@addSnapshotListener
        val myTag = userState.value.gamertag.lowercase().trim()

        if (myTag.isNotBlank()) {
          val myData = playersMap[myTag] as? Map<*, *>
          if (myData != null) {
            val myCoords = myData["coords"]?.toString() ?: ""
            if (myCoords.isNotBlank() && myCoords != userState.value.lastCoordinates) {
              viewModelScope.launch { authRepository.updateCoordinates(myCoords) }
            }
          }
        }

        if (_members.value.isNotEmpty()) {
          _members.value = _members.value.map { member ->
            val memberKey = member.gamertag.lowercase().trim()
            val memberData = playersMap[memberKey] as? Map<*, *>
            if (memberData != null) {
              val coords = memberData["coords"]?.toString() ?: member.lastCoordinates
              val ts = (memberData["timestamp"] as? Number)?.toLong() ?: member.lastCoordinatesTimestamp
              member.copy(lastCoordinates = coords, lastCoordinatesTimestamp = ts)
            } else {
              member
            }
          }
        }
      }
  }

  fun sendLocationResponse(requestMessage: ChatMessage, onManualInputRequired: () -> Unit) {
    val myTag = userState.value.gamertag.trim()
    if (myTag.isBlank()) {
      _showGamertagDialog.value = true
      return
    }

    viewModelScope.launch {
      var coordsToSend = userState.value.lastCoordinates.trim()

      if (coordsToSend.isBlank()) {
        try {
          val liveDoc = firestore.collection("server_coords").document("live").get().await()
          val playersMap = liveDoc.get("players") as? Map<*, *>
          val myData = playersMap?.get(myTag.lowercase()) as? Map<*, *>
          coordsToSend = myData?.get("coords")?.toString()?.trim() ?: ""
        } catch (_: Exception) {}
      }

      if (coordsToSend.isBlank()) {
        onManualInputRequired()
        return@launch
      }

      firestore.collection("channels")
        .document(requestMessage.channelId)
        .collection("messages")
        .document(requestMessage.id)
        .update("coordinates", coordsToSend)

      sendMessage(
        content = "📍 Shared location: $coordsToSend",
        coordinates = coordsToSend,
        replyTo = requestMessage,
        channelId = requestMessage.channelId
      )

      authRepository.updateCoordinates(coordsToSend)
    }
  }

  fun submitManualCoordinates(requestMessage: ChatMessage, manualCoords: String) {
    val clean = manualCoords.trim()
    if (clean.isBlank()) return

    viewModelScope.launch {
      firestore.collection("channels")
        .document(requestMessage.channelId)
        .collection("messages")
        .document(requestMessage.id)
        .update("coordinates", clean)

      sendMessage(
        content = "📍 Shared location: $clean",
        coordinates = clean,
        replyTo = requestMessage,
        channelId = requestMessage.channelId
      )

      authRepository.updateCoordinates(clean)
    }
  }

  private fun syncDeletedAccounts() {
    firestore.collection("deleted_accounts").addSnapshotListener { snapshot, _ ->
      if (snapshot != null) {
        val set = snapshot.documents.mapNotNull { it.id.lowercase().trim() }.toSet()
        _deletedGamertags.value = set
      }
    }
  }

  fun setCurrentScreen(screen: String) {
    _currentScreenState.value = screen
  }

  fun updateSetting(key: String, value: Any) {
    when (value) {
      is Boolean -> prefs.edit().putBoolean(key, value).apply()
      is String -> prefs.edit().putString(key, value).apply()
    }
    _appSettings.value = AppSettings(
      isNightMode = prefs.getBoolean("night_mode", false),
      smoothAnimations = prefs.getBoolean("animations", true),
      autoDownloadMedia = prefs.getBoolean("auto_download", true),
      powerSaving = prefs.getBoolean("power_saving", false),
      notifications = prefs.getBoolean("notifications", true),
      language = prefs.getString("language", "English") ?: "English",
      autoTranslate = prefs.getBoolean("auto_translate", false)
    )

    if (key == "auto_translate" && value == true) triggerAutoTranslate()
  }

  private fun triggerAutoTranslate() {
    val targetCode = when (_appSettings.value.language.lowercase()) {
      "chinese" -> "zh"
      "malay" -> "ms"
      else -> "en"
    }
    val myTag = userState.value.gamertag.trim()
    messages.value.forEach { msg ->
      if (msg.content.isNotBlank() && !msg.senderName.equals(myTag, ignoreCase = true) && !_translatedMessages.value.containsKey(msg.id)) {
        translateMessage(msg.id, msg.content, targetCode)
      }
    }
  }

  fun updateProfile(bio: String, birthday: String) {
    viewModelScope.launch { authRepository.updateProfileInfo(bio, birthday) }
  }

  fun updateCoordinates(coords: String) {
    viewModelScope.launch { authRepository.updateCoordinates(coords) }
  }

  fun requestUserLocation(targetGamertag: String, channelId: String = _activeChannel.value.id) {
    val myTag = userState.value.gamertag.trim()
    if (myTag.isBlank()) {
      _showGamertagDialog.value = true
      return
    }
    if (targetGamertag.isBlank()) return

    sendMessage(
      content = "📍 Location Request",
      coordinates = null,
      isLocationRequest = true,
      locationTargetGamertag = targetGamertag,
      channelId = channelId
    )
  }

  fun startPrivateCall(recipientGamertag: String, callChannelId: String, callChannelName: String, isVideo: Boolean) {
    val myTag = userState.value.gamertag.trim()
    val user = userState.value
    if (myTag.isBlank() || recipientGamertag.isBlank()) {
      _showGamertagDialog.value = true
      return
    }
    val dmId = "dm_" + listOf(myTag.lowercase(), recipientGamertag.lowercase()).sorted().joinToString("_")

    firestore.collection("active_calls").document(callChannelId).set(
      hashMapOf(
        "callId" to callChannelId,
        "caller" to myTag,
        "recipient" to recipientGamertag,
        "isVideo" to isVideo,
        "status" to "ringing",
        "timestamp" to System.currentTimeMillis()
      )
    )

    sendMessage(
      content = "📞 Started a ${if (isVideo) "video" else "voice"} call",
      coordinates = null,
      channelId = dmId
    )

    // Ring recipient's phone via OneSignal even if their app is killed
    viewModelScope.launch {
      OneSignalHelper.sendToUser(
        recipientGamertag = recipientGamertag,
        title = "Incoming ${if (isVideo) "Video" else "Voice"} Call",
        message = "${user.gamertag} is calling you...",
        dataPayload = mapOf(
          "type" to "call",
          "callId" to callChannelId,
          "caller" to user.gamertag,
          "isVideo" to isVideo.toString()
        )
      )
    }

    voiceManager.joinVoiceChannel(callChannelId, callChannelName, myTag, isVideo = isVideo)
  }

  private fun listenToIncomingCalls() {
    val myTag = userState.value.gamertag.trim()
    if (myTag.isBlank()) return
    incomingCallListener?.remove()
    incomingCallListener = firestore.collection("active_calls")
      .whereEqualTo("recipient", myTag)
      .whereEqualTo("status", "ringing")
      .addSnapshotListener { snapshot, _ ->
        if (snapshot != null && !snapshot.isEmpty) {
          val now = System.currentTimeMillis()
          val validDoc = snapshot.documents.firstOrNull { doc ->
            val ts = doc.getLong("timestamp") ?: 0L
            (now - ts) < 45000L
          }
          if (validDoc != null) {
            val callId = validDoc.getString("callId") ?: validDoc.id
            val caller = validDoc.getString("caller") ?: "Player"
            val isVideo = validDoc.getBoolean("isVideo") ?: false
            val ts = validDoc.getLong("timestamp") ?: now

            // FIX: Only trigger the incoming call screen if the call started in the last 15 seconds.
            // This prevents old, missed calls from popping up when the user opens the app.
            val isRecentCall = (now - ts) < 15000L

            if (_incomingCall.value?.callId != callId && isRecentCall) {
              _incomingCall.value = IncomingCallData(callId, caller, isVideo, ts)
              CallRingtoneHelper.startRinging(getApplication())
              NotificationHelper.showIncomingCallNotification(
                context = getApplication(),
                callId = callId,
                caller = caller,
                isVideo = isVideo
              )
            }
          } else {
            stopIncomingCallAlerts()
          }
        } else {
          stopIncomingCallAlerts()
        }
      }
  }

  private fun stopIncomingCallAlerts() {
    _incomingCall.value?.let { call ->
      NotificationHelper.dismissCallNotification(getApplication(), call.callId.hashCode())
    }
    CallRingtoneHelper.stopRinging(getApplication())
    _incomingCall.value = null
  }

  fun answerIncomingCall() {
    val call = _incomingCall.value ?: return
    stopIncomingCallAlerts()
    val myTag = userState.value.gamertag
    firestore.collection("active_calls").document(call.callId).update("status", "accepted")
    voiceManager.joinVoiceChannel(call.callId, "Call with ${call.caller}", myTag, isVideo = call.isVideo)
  }

  fun declineIncomingCall() {
    val call = _incomingCall.value ?: return
    val callId = call.callId
    stopIncomingCallAlerts()
    firestore.collection("active_calls").document(callId).update("status", "declined")
  }

  fun inviteToCall(targetGamertag: String, callChannelId: String, callChannelName: String, isVideo: Boolean) {
    val myTag = userState.value.gamertag.trim()
    if (myTag.isBlank()) {
      _showGamertagDialog.value = true
      return
    }
    if (targetGamertag.isBlank()) return

    val dmId = "dm_" + listOf(myTag.lowercase(), targetGamertag.lowercase()).sorted().joinToString("_")
    sendMessage(
      content = "📞 Invited you to a private call ($callChannelName)",
      coordinates = null,
      channelId = dmId
    )
  }

  fun translateMessage(messageId: String, text: String, targetLangCode: String) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
        val url = "https://api.mymemory.translated.net/get?q=$encodedText&langpair=Autodetect|$targetLangCode"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        if (body != null) {
          val json = JSONObject(body)
          val translatedText = json.getJSONObject("responseData").getString("translatedText")
          val currentMap = _translatedMessages.value.toMutableMap()
          currentMap[messageId] = translatedText
          _translatedMessages.value = currentMap
        }
      } catch (_: Exception) {}
    }
  }

  fun toggleMuteChannel(channelId: String): Boolean = chatRepository.toggleMuteChannel(channelId)
  fun isChannelMuted(channelId: String): Boolean = chatRepository.isChannelMuted(channelId)

  fun toggleBlockUser(gamertag: String): Boolean = chatRepository.toggleBlockUser(gamertag)
  fun isUserBlocked(gamertag: String): Boolean = chatRepository.isUserBlocked(gamertag)

  fun toggleFavourite(channelId: String): Boolean = chatRepository.toggleFavourite(channelId)
  fun isFavourite(channelId: String): Boolean = chatRepository.isFavourite(channelId)

  private fun listenToTyping(channelId: String) {
    typingListener?.remove()
    typingListener = firestore.collection("channels")
      .document(channelId)
      .collection("typing")
      .addSnapshotListener { snapshot, _ ->
        if (snapshot != null) {
          val now = System.currentTimeMillis()
          val typers = snapshot.documents.mapNotNull { doc ->
            val tag = doc.getString("gamertag") ?: return@mapNotNull null
            val ts = doc.getLong("timestamp") ?: 0L
            if (tag != userState.value.gamertag && (now - ts) < 4000) tag else null
          }
          _typingUsers.value = typers
        }
      }
  }

  fun setTyping(isTyping: Boolean) {
    val myTag = userState.value.gamertag
    val channelId = _activeChannel.value.id
    if (myTag.isBlank() || channelId.isBlank()) return

    val docRef = firestore.collection("channels").document(channelId).collection("typing").document(myTag)
    if (isTyping) {
      docRef.set(mapOf("gamertag" to myTag, "timestamp" to System.currentTimeMillis()))
    } else {
      docRef.delete()
    }
  }

  fun startPrivateChat(recipientGamertag: String): Boolean {
    val myTag = userState.value.gamertag.trim()
    if (myTag.isBlank()) {
      _showGamertagDialog.value = true
      return false
    }
    if (recipientGamertag.isBlank() || myTag.equals(recipientGamertag, ignoreCase = true)) return false

    val dmId = "dm_" + listOf(myTag.lowercase(), recipientGamertag.lowercase().trim()).sorted().joinToString("_")
    val dmChannel = Channel(
      id = dmId,
      name = recipientGamertag,
      type = ChannelType.TEXT,
      category = "DIRECT MESSAGES",
      description = "Private chat with $recipientGamertag",
      allowedRolesToSend = listOf("ALL"),
      isDm = true,
      dmRecipientGamertag = recipientGamertag
    )

    firestore.collection("channels").document(dmId).set(
      mapOf(
        "name" to recipientGamertag,
        "type" to "TEXT",
        "category" to "DIRECT MESSAGES",
        "description" to "Private chat with $recipientGamertag",
        "allowedRolesToSend" to listOf("ALL"),
        "isDm" to true,
        "participants" to listOf(myTag.lowercase(), recipientGamertag.lowercase().trim())
      ),
      SetOptions.merge()
    )

    selectChannel(dmChannel)
    return true
  }

  private fun syncLiveChannels() {
    try {
      firestore.collection("channels")
        .addSnapshotListener { snapshot, error ->
          if (error != null) return@addSnapshotListener
          if (snapshot != null && !snapshot.isEmpty) {
            val myTag = userState.value.gamertag.lowercase().trim()
            val remote = snapshot.documents.mapNotNull { doc ->
              val id = doc.id
              val isDm = doc.getBoolean("isDm") ?: false
              val parts = (doc.get("participants") as? List<*>)?.mapNotNull { it?.toString()?.lowercase() }

              if (isDm) {
                if (myTag.isBlank() || parts == null || !parts.contains(myTag)) return@mapNotNull null
              }

              val rawName = doc.getString("name") ?: id
              val name = if (isDm && parts != null) parts.find { it != myTag } ?: rawName else rawName

              val typeStr = doc.getString("type") ?: "TEXT"
              val category = doc.getString("category") ?: "CHANNELS"
              val description = doc.getString("description") ?: ""
              val allowed = (doc.get("allowedRolesToSend") as? List<*>)?.mapNotNull { it?.toString() }
                ?: if (id == "announcements") listOf("ADMIN") else listOf("ALL")
              val type = try { ChannelType.valueOf(typeStr) } catch (_: Exception) { ChannelType.TEXT }

              Channel(
                id = id,
                name = name,
                type = type,
                category = category,
                description = description,
                allowedRolesToSend = allowed,
                isDm = isDm,
                dmRecipientGamertag = if (isDm) name else null,
                lastMessage = doc.getString("lastMessage"),
                lastMessageTimestamp = doc.getLong("lastMessageTimestamp") ?: 0L,
                lastMessageSender = doc.getString("lastMessageSender")
              )
            }

            if (remote.isNotEmpty()) {
              _channels.value = remote.sortedByDescending { it.lastMessageTimestamp }
              remote.find { it.id == _activeChannel.value.id }?.let { _activeChannel.value = it }
              remote.forEach { ch ->
                if (ch.type == ChannelType.TEXT && !channelMsgListeners.containsKey(ch.id)) {
                  attachChannelLatestMsgListener(ch.id)
                }
              }
            }
          }
        }
    } catch (_: Exception) {}
  }

  private fun attachChannelLatestMsgListener(channelId: String) {
    val listener = firestore.collection("channels").document(channelId).collection("messages")
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .limit(1)
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        val latest = snapshot?.documents?.firstOrNull() ?: return@addSnapshotListener
        val content = latest.getString("content") ?: ""
        val img = latest.getString("imageUrl")
        val aud = latest.getString("audioUrl")
        val fil = latest.getString("fileUrl")
        val sticker = latest.getBoolean("isSticker") ?: false
        val fn = latest.getString("fileName")
        val dur = latest.getLong("audioDurationSeconds")?.toInt() ?: 0
        val coordinates = latest.getString("coordinates")
        val ts = latest.getLong("timestamp") ?: 0L
        val sender = latest.getString("senderName") ?: ""
        val readByList = (latest.get("readBy") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

        val isRead = if (channelId.startsWith("dm_")) {
          readByList.isNotEmpty()
        } else {
          readByList.any { !it.equals(sender, ignoreCase = true) }
        }

        val preview = when {
          content.startsWith("📍 Location Request") -> "📍 Location Request"
          sticker -> "💟 Sticker"
          img != null -> "📷 Photo"
          aud != null -> if (dur > 0) "🎤 Voice message" else "🎵 ${fn ?: "Audio file"}"
          fil != null -> "📄 ${fn ?: "Document"}"
          coordinates != null && content.isBlank() -> "📍 $coordinates"
          else -> content
        }

        val prevTs = channelLastKnownTs[channelId]
        channelLastKnownTs[channelId] = ts

        val myTag = userState.value.gamertag.trim()
        val isFromMe = myTag.isNotBlank() && sender.equals(myTag, ignoreCase = true)
        val isViewingThisChat = _currentScreenState.value == "chat_screen" && _activeChannel.value.id == channelId

        // FIX: Ensure message is actually NEW (sent within the last 15 seconds). 
        // This prevents the app from spamming notifications for old messages when you open it.
        val isRecentlySent = (System.currentTimeMillis() - ts) < 15000L

        if (prevTs != null && ts > prevTs && !isFromMe && isRecentlySent) {
          if (!isViewingThisChat && !isChannelMuted(channelId) && !isUserBlocked(sender)) {
            SoundHelper.playMessageSound(getApplication())
            val notifTitle = if (channelId.startsWith("dm_")) sender else "#$channelId • $sender"
            NotificationHelper.showMessageNotification(
              context = getApplication(),
              notificationId = channelId.hashCode(),
              title = notifTitle,
              content = preview
            )
          }
        }

        _channels.value = _channels.value.map { ch ->
          if (ch.id == channelId) {
            ch.copy(
              lastMessage = preview,
              lastMessageTimestamp = ts,
              lastMessageSender = sender,
              lastMessageIsRead = isRead
            )
          } else ch
        }.sortedByDescending { it.lastMessageTimestamp }
      }
    channelMsgListeners[channelId] = listener
  }

  private fun syncLiveStatuses() {
    try {
      firestore.collection("statuses")
        .orderBy("timestamp")
        .addSnapshotListener { snapshot, _ ->
          if (snapshot != null) {
            val list = snapshot.documents.mapNotNull { doc ->
              val reactionsRaw = doc.get("reactionCounts") as? Map<*, *>
              val reactions = reactionsRaw?.mapKeys { it.key.toString() }?.mapValues { (it.value as? Long)?.toInt() ?: 0 } ?: emptyMap()
              StatusUpdate(
                id = doc.id,
                authorGamertag = doc.getString("authorGamertag") ?: "Player",
                content = doc.getString("content") ?: "",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                isAdmin = doc.getBoolean("isAdmin") ?: false,
                activityTag = doc.getString("activityTag") ?: "Note",
                backgroundTheme = doc.getString("backgroundTheme") ?: "EMERALD",
                coordinates = doc.getString("coordinates"),
                reactionCounts = reactions,
                musicTrackName = doc.getString("musicTrackName"),
                musicArtistName = doc.getString("musicArtistName"),
                musicPreviewUrl = doc.getString("musicPreviewUrl"),
                musicArtworkUrl = doc.getString("musicArtworkUrl"),
                musicStartTimeMs = doc.getLong("musicStartTimeMs")?.toInt() ?: 0
              )
            }
            _statuses.value = list
          }
        }
    } catch (_: Exception) {}
  }

  private fun syncLiveStickers() {
    try {
      firestore.collection("stickers")
        .orderBy("timestamp")
        .addSnapshotListener { snapshot, _ ->
          if (snapshot != null) {
            val list = snapshot.documents.mapNotNull { doc ->
              val data = doc.getString("imageData") ?: return@mapNotNull null
              ServerSticker(
                id = doc.id,
                name = doc.getString("name") ?: "Sticker",
                imageData = data,
                uploadedBy = doc.getString("uploadedBy") ?: "Admin",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
              )
            }
            _stickers.value = list
          }
        }
    } catch (_: Exception) {}
  }

  fun syncMembers() {
    try {
      firestore.collection("users")
        .addSnapshotListener { snapshot, _ ->
          if (snapshot != null) {
            val rawList = snapshot.documents.mapNotNull { doc ->
              if (doc.getBoolean("isDeleted") == true) return@mapNotNull null
              val gamertag = doc.getString("gamertag") ?: return@mapNotNull null
              if (gamertag == "Deleted Account") return@mapNotNull null
              val isAdmin = doc.getBoolean("isAdmin") ?: gamertag.equals("Siang5680", ignoreCase = true)
              val role = doc.getString("role") ?: if (isAdmin) "ADMIN" else "MEMBER"
              val bio = doc.getString("bio") ?: ""
              val birthday = doc.getString("birthday") ?: ""
              val lastCoords = doc.getString("lastCoordinates") ?: ""
              val lastCoordsTs = doc.getLong("lastCoordinatesTimestamp") ?: 0L
              AdminMember(
                id = doc.id,
                gamertag = gamertag,
                role = role,
                isAdmin = isAdmin,
                bio = bio,
                birthday = birthday,
                lastCoordinates = lastCoords,
                lastCoordinatesTimestamp = lastCoordsTs
              )
            }
            _members.value = rawList.distinctBy { it.gamertag.lowercase().trim() }
          }
        }
    } catch (_: Exception) {}
  }

  fun addServerSticker(name: String, base64Image: String) {
    if (base64Image.isBlank()) return
    val user = userState.value
    firestore.collection("stickers").add(
      hashMapOf(
        "name" to name.ifBlank { "Sticker" },
        "imageData" to base64Image,
        "uploadedBy" to user.gamertag,
        "timestamp" to System.currentTimeMillis()
      )
    )
  }

  fun deleteServerSticker(stickerId: String) {
    firestore.collection("stickers").document(stickerId).delete()
  }

  fun postStatus(
    content: String,
    theme: String = "EMERALD",
    activity: String = "Note",
    coords: String? = null,
    musicName: String? = null,
    musicArtist: String? = null,
    musicPreview: String? = null,
    musicArtwork: String? = null,
    musicStartMs: Int = 0
  ) {
    if (content.isBlank() && musicName.isNullOrBlank()) return
    val user = userState.value
    if (user.gamertag.isBlank()) {
      _showGamertagDialog.value = true
      return
    }
    if (!coords.isNullOrBlank()) {
      updateCoordinates(coords)
    }
    firestore.collection("statuses").add(
      hashMapOf(
        "authorGamertag" to user.gamertag,
        "content" to content.trim(),
        "timestamp" to System.currentTimeMillis(),
        "isAdmin" to user.isAdmin,
        "backgroundTheme" to theme,
        "activityTag" to activity,
        "coordinates" to coords,
        "reactionCounts" to emptyMap<String, Int>(),
        "musicTrackName" to musicName,
        "musicArtistName" to musicArtist,
        "musicPreviewUrl" to musicPreview,
        "musicArtworkUrl" to musicArtwork,
        "musicStartTimeMs" to musicStartMs
      )
    )
  }

  fun deleteStatus(statusId: String) {
    firestore.collection("statuses").document(statusId).delete()
  }

  fun editMessage(messageId: String, newContent: String) {
    chatRepository.editMessage(_activeChannel.value.id, messageId, newContent.trim())
  }

  fun deleteMessage(channelId: String, messageId: String) {
    chatRepository.deleteMessage(channelId, messageId)
  }

  fun reactToStatus(statusId: String, emoji: String) {
    firestore.collection("statuses").document(statusId).get().addOnSuccessListener { doc ->
      val reactionsRaw = doc.get("reactionCounts") as? Map<*, *>
      val counts = reactionsRaw?.mapKeys { it.key.toString() }?.mapValues { (it.value as? Long)?.toInt() ?: 0 }?.toMutableMap() ?: mutableMapOf()
      counts[emoji] = (counts[emoji] ?: 0) + 1
      firestore.collection("statuses").document(statusId).update("reactionCounts", counts)
    }
  }

  fun createChannel(name: String, type: ChannelType, desc: String, onlyAdmin: Boolean) {
    val cleanName = name.trim()
    val id = cleanName.lowercase().replace(" ", "-")
    val newChannel = Channel(
      id = id,
      name = cleanName,
      type = type,
      category = if (type == ChannelType.TEXT) "TEXT CHANNELS" else "VOICE CHANNELS",
      description = desc.trim(),
      allowedRolesToSend = if (onlyAdmin) listOf("ADMIN") else listOf("ALL")
    )
    firestore.collection("channels").document(id).set(
      hashMapOf(
        "name" to newChannel.name,
        "type" to newChannel.type.name,
        "category" to newChannel.category,
        "description" to newChannel.description,
        "allowedRolesToSend" to newChannel.allowedRolesToSend
      )
    )
    if (type == ChannelType.TEXT) {
      selectChannel(newChannel)
    }
  }

  fun updateChannelInfo(channelId: String, newName: String, newDescription: String) {
    val cleanName = newName.trim()
    if (cleanName.isBlank()) return
    firestore.collection("channels").document(channelId).set(
      mapOf(
        "name" to cleanName,
        "description" to newDescription.trim()
      ),
      SetOptions.merge()
    )
  }

  fun deleteChannel(channelId: String) {
    if (channelId == "general-chat" || channelId == "announcements") return
    firestore.collection("channels").document(channelId).delete()
    if (_activeChannel.value.id == channelId) {
      val fallback = _channels.value.firstOrNull { it.id == "general-chat" }
        ?: _channels.value.firstOrNull { it.type == ChannelType.TEXT }
        ?: DefaultChannels[1]
      selectChannel(fallback)
    }
  }

  fun toggleChannelPermission(channel: Channel) {
    val newPerm = if (channel.isRestrictedToAdmin) listOf("ALL") else listOf("ADMIN")
    firestore.collection("channels").document(channel.id).update("allowedRolesToSend", newPerm)
  }

  fun updateMemberRole(userId: String, newRole: String) {
    val isAdmin = newRole.equals("ADMIN", ignoreCase = true)
    val member = _members.value.find { it.id == userId } ?: return
    val cleanTag = member.gamertag.lowercase().trim()

    firestore.collection("users").document(userId).set(
      mapOf("role" to newRole, "isAdmin" to isAdmin),
      SetOptions.merge()
    )
    firestore.collection("gamertags").document(cleanTag).set(
      mapOf("role" to newRole, "isAdmin" to isAdmin),
      SetOptions.merge()
    )
    firestore.collection("users").whereEqualTo("gamertag", member.gamertag).get()
      .addOnSuccessListener { query ->
        for (doc in query.documents) {
          doc.reference.set(mapOf("role" to newRole, "isAdmin" to isAdmin), SetOptions.merge())
        }
      }
  }

  fun updateMemberGamertag(userId: String, newName: String) {
    val cleanName = newName.trim()
    if (cleanName.isBlank()) return
    val member = _members.value.find { it.id == userId }
    firestore.collection("users").document(userId).set(mapOf("gamertag" to cleanName), SetOptions.merge())
    if (member != null) {
      firestore.collection("gamertags").document(member.gamertag.lowercase().trim()).delete()
      firestore.collection("gamertags").document(cleanName.lowercase()).set(
        hashMapOf(
          "uid" to userId,
          "gamertag" to cleanName,
          "isAdmin" to member.isAdmin,
          "role" to member.role
        )
      )
    }
  }

  fun removeMember(userId: String) {
    val member = _members.value.find { it.id == userId }
    val tagClean = member?.gamertag?.lowercase()?.trim() ?: ""

    if (tagClean.isNotBlank()) {
      firestore.collection("deleted_accounts").document(tagClean).set(
        mapOf(
          "gamertag" to (member?.gamertag ?: ""),
          "deletedAt" to System.currentTimeMillis()
        )
      )
    }

    firestore.collection("users").document(userId).set(
      mapOf("isDeleted" to true, "role" to "DELETED", "gamertag" to "Deleted Account"),
      SetOptions.merge()
    ).addOnSuccessListener {
      firestore.collection("users").document(userId).delete()
      if (tagClean.isNotBlank()) {
        firestore.collection("gamertags").document(tagClean).delete()
      }
    }
  }

  fun selectChannel(channel: Channel) {
    if (channel.isDm && userState.value.gamertag.isBlank()) {
      _showGamertagDialog.value = true
      return
    }

    if (channel.type == ChannelType.TEXT) {
      _activeChannel.value = channel
      chatRepository.selectChannel(channel.id)
      listenToTyping(channel.id)
      triggerAutoTranslate()
    } else if (channel.type == ChannelType.VOICE) {
      val gamertag = userState.value.gamertag
      if (gamertag.isBlank()) {
        _showGamertagDialog.value = true
        return
      }
      voiceManager.joinVoiceChannel(channel.id, channel.name, gamertag, isVideo = false)
    }
  }

  fun startVideoCall(channel: Channel) {
    val gamertag = userState.value.gamertag
    if (gamertag.isBlank()) {
      _showGamertagDialog.value = true
      return
    }
    voiceManager.joinVoiceChannel(channel.id, channel.name, gamertag, isVideo = true)
  }

  fun sendMessage(
    content: String,
    coordinates: String? = null,
    imageUrl: String? = null,
    audioUrl: String? = null,
    audioDurationSeconds: Int = 0,
    fileUrl: String? = null,
    fileName: String? = null,
    isSticker: Boolean = false,
    replyTo: ChatMessage? = null,
    isLocationRequest: Boolean = false,
    locationTargetGamertag: String? = null,
    channelId: String = _activeChannel.value.id
  ) {
    val user = userState.value
    if (user.gamertag.isBlank()) {
      _showGamertagDialog.value = true
      return
    }
    if (_activeChannel.value.isRestrictedToAdmin && !user.isAdmin) return
    if (content.isBlank() && coordinates == null && imageUrl == null && audioUrl == null && fileUrl == null && !isLocationRequest) return

    setTyping(false)
    val role = if (user.isAdmin || user.gamertag.equals("Siang5680", ignoreCase = true)) "ADMIN" else user.role

    if (!coordinates.isNullOrBlank()) {
      updateCoordinates(coordinates)
    }

    chatRepository.sendMessage(
      channelId = channelId,
      senderName = user.gamertag,
      senderRole = role,
      content = content.trim(),
      coordinates = coordinates,
      imageUrl = imageUrl,
      audioUrl = audioUrl,
      audioDurationSeconds = audioDurationSeconds,
      fileUrl = fileUrl,
      fileName = fileName,
      isSticker = isSticker,
      replyToId = replyTo?.id,
      replyToSender = replyTo?.senderName,
      replyToContent = replyTo?.content,
      isLocationRequest = isLocationRequest,
      locationTargetGamertag = locationTargetGamertag
    )

    val preview = when {
      isLocationRequest -> "📍 Location Request"
      isSticker -> "💟 Sticker"
      imageUrl != null -> "📷 Photo"
      audioUrl != null -> if (audioDurationSeconds > 0) "🎤 Voice message" else "🎵 ${fileName ?: "Audio file"}"
      fileUrl != null -> "📄 ${fileName ?: "Document"}"
      coordinates != null && content.isBlank() -> "📍 $coordinates"
      else -> content.trim()
    }
    firestore.collection("channels").document(channelId).set(
      hashMapOf(
        "lastMessage" to preview,
        "lastMessageTimestamp" to System.currentTimeMillis(),
        "lastMessageSender" to user.gamertag
      ),
      SetOptions.merge()
    )

    // Trigger instant background push notification via OneSignal
    viewModelScope.launch {
      val isDm = channelId.startsWith("dm_")
      if (isDm) {
        val target = locationTargetGamertag 
          ?: channelId.removePrefix("dm_").split("_").firstOrNull { !it.equals(user.gamertag, ignoreCase = true) }
        if (!target.isNullOrBlank()) {
          OneSignalHelper.sendToUser(
            recipientGamertag = target,
            title = user.gamertag,
            message = preview
          )
        }
      } else {
        OneSignalHelper.broadcastToAll(
          title = "#$channelId • ${user.gamertag}",
          message = preview,
          senderGamertag = user.gamertag
        )
      }
    }
  }

  fun loginAccount(gamertag: String, pass: String, onResult: (Result<String>) -> Unit) {
    viewModelScope.launch {
      val result = authRepository.loginAccount(gamertag, pass)
      if (result.isSuccess) {
        val tag = result.getOrNull() ?: gamertag
        chatRepository.updateLocalGamertag(tag)
        _showGamertagDialog.value = false
        syncLiveChannels()
        listenToIncomingCalls()
        listenToServerLiveCoordinates()
      }
      onResult(result)
    }
  }

  fun registerAccount(gamertag: String, pass: String, onResult: (Result<String>) -> Unit) {
    viewModelScope.launch {
      val result = authRepository.registerAccount(gamertag, pass)
      if (result.isSuccess) {
        val tag = result.getOrNull() ?: gamertag
        chatRepository.updateLocalGamertag(tag)
        _showGamertagDialog.value = false
        syncLiveChannels()
        listenToIncomingCalls()
        listenToServerLiveCoordinates()
      }
      onResult(result)
    }
  }

  fun logout() {
    authRepository.logout()
    chatRepository.updateLocalGamertag("")
    incomingCallListener?.remove()
    incomingCallListener = null
    stopIncomingCallAlerts()
    serverCoordsListener?.remove()
    serverCoordsListener = null
    _channels.value = DefaultChannels
    _activeChannel.value = DefaultChannels[1]
    chatRepository.selectChannel(DefaultChannels[1].id)
  }

  fun setGamertagDialogVisible(v: Boolean) { _showGamertagDialog.value = v }
  fun setAgoraDialogVisible(v: Boolean) { _showAgoraDialog.value = v }
  fun setServerInfoSheetVisible(v: Boolean) { _showServerInfoSheet.value = v }
  fun setAdminConsoleVisible(v: Boolean) { _showAdminConsole.value = v }
  fun setSelectContactDialogVisible(v: Boolean) { _showSelectContactDialog.value = v }

  override fun onCleared() {
    super.onCleared()
    typingListener?.remove()
    incomingCallListener?.remove()
    serverCoordsListener?.remove()
    stopIncomingCallAlerts()
    channelMsgListeners.values.forEach { it.remove() }
    channelMsgListeners.clear()
    voiceManager.destroy()
  }
}
