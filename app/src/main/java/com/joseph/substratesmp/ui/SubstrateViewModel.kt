package com.joseph.substratesmp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.data.model.DefaultChannels
import com.joseph.substratesmp.data.model.ServerSticker
import com.joseph.substratesmp.data.model.StatusUpdate
import com.joseph.substratesmp.data.repository.AuthRepository
import com.joseph.substratesmp.data.repository.ChatRepository
import com.joseph.substratesmp.ui.components.AdminMember
import com.joseph.substratesmp.voice.AgoraVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubstrateViewModel(application: Application) : AndroidViewModel(application) {
  val authRepository = AuthRepository(application)
  val chatRepository = ChatRepository(application)
  val voiceManager = AgoraVoiceManager(application)

  val userState = authRepository.userState
  val messages = chatRepository.messagesFlow
  val activeVoiceRoom = voiceManager.voiceRoomState
  val agoraSettings = voiceManager.settings

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

  // Real-time live typers in current channel
  private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
  val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()
  private var typingListener: ListenerRegistration? = null

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
      listenToTyping(_activeChannel.value.id)
    }
  }

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

    val docRef = firestore.collection("channels")
      .document(channelId)
      .collection("typing")
      .document(myTag)

    if (isTyping) {
      docRef.set(mapOf("gamertag" to myTag, "timestamp" to System.currentTimeMillis()))
    } else {
      docRef.delete()
    }
  }

  fun startPrivateChat(recipientGamertag: String) {
    val myTag = userState.value.gamertag
    if (myTag.isBlank() || recipientGamertag.isBlank() || myTag.equals(recipientGamertag, ignoreCase = true)) return

    val dmId = "dm_" + listOf(myTag.lowercase().trim(), recipientGamertag.lowercase().trim()).sorted().joinToString("_")
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
        "participants" to listOf(myTag.lowercase().trim(), recipientGamertag.lowercase().trim())
      ),
      SetOptions.merge()
    )

    selectChannel(dmChannel)
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

              // Filter out private DMs not addressed to me
              if (isDm && parts != null && myTag.isNotBlank() && !parts.contains(myTag)) {
                return@mapNotNull null
              }

              val name = doc.getString("name") ?: id
              val typeStr = doc.getString("type") ?: "TEXT"
              val category = doc.getString("category") ?: "CHANNELS"
              val description = doc.getString("description") ?: ""
              val allowed = (doc.get("allowedRolesToSend") as? List<*>)?.mapNotNull { it?.toString() }
                ?: if (id == "announcements") listOf("ADMIN") else listOf("ALL")
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
                allowedRolesToSend = allowed,
                isDm = isDm,
                dmRecipientGamertag = if (isDm) name else null
              )
            }
            if (remote.isNotEmpty()) {
              _channels.value = remote
              remote.find { it.id == _activeChannel.value.id }?.let {
                _activeChannel.value = it
              }
            }
          } else {
            DefaultChannels.forEach { ch ->
              firestore.collection("channels").document(ch.id).set(
                hashMapOf(
                  "name" to ch.name,
                  "type" to ch.type.name,
                  "category" to ch.category,
                  "description" to ch.description,
                  "allowedRolesToSend" to ch.allowedRolesToSend
                )
              )
            }
          }
        }
    } catch (_: Exception) {}
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
                activityTag = doc.getString("activityTag") ?: "Mining",
                backgroundTheme = doc.getString("backgroundTheme") ?: "EMERALD",
                coordinates = doc.getString("coordinates"),
                reactionCounts = reactions
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
            // Deduplicate members by lowercase gamertag to solve duplicate Siang5680 records
            val rawList = snapshot.documents.mapNotNull { doc ->
              val gamertag = doc.getString("gamertag") ?: return@mapNotNull null
              val isAdmin = doc.getBoolean("isAdmin") ?: gamertag.equals("Siang5680", ignoreCase = true)
              val role = doc.getString("role") ?: if (isAdmin) "ADMIN" else "MEMBER"
              AdminMember(id = doc.id, gamertag = gamertag, role = role, isAdmin = isAdmin)
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

  fun postStatus(content: String, theme: String, activity: String, coords: String?) {
    if (content.isBlank()) return
    val user = userState.value
    firestore.collection("statuses").add(
      hashMapOf(
        "authorGamertag" to user.gamertag,
        "content" to content.trim(),
        "timestamp" to System.currentTimeMillis(),
        "isAdmin" to user.isAdmin,
        "backgroundTheme" to theme,
        "activityTag" to activity,
        "coordinates" to coords,
        "reactionCounts" to emptyMap<String, Int>()
      )
    )
  }

  fun deleteStatus(statusId: String) {
    firestore.collection("statuses").document(statusId).delete()
  }

  fun deleteMessage(channelId: String, messageId: String) {
    firestore.collection("channels").document(channelId).collection("messages").document(messageId).delete()
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

  fun deleteChannel(channelId: String) {
    if (channelId == "general-chat") return
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

    // 1. Update user document with merge
    firestore.collection("users").document(userId).set(
      mapOf(
        "role" to newRole,
        "isAdmin" to isAdmin
      ),
      SetOptions.merge()
    )

    // 2. Update gamertags index
    firestore.collection("gamertags").document(cleanTag).set(
      mapOf(
        "role" to newRole,
        "isAdmin" to isAdmin
      ),
      SetOptions.merge()
    )

    // 3. Update any duplicate documents under the same gamertag
    firestore.collection("users").whereEqualTo("gamertag", member.gamertag).get()
      .addOnSuccessListener { query ->
        for (doc in query.documents) {
          doc.reference.set(
            mapOf("role" to newRole, "isAdmin" to isAdmin),
            SetOptions.merge()
          )
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
    firestore.collection("users").document(userId).delete()
    if (member != null) {
      firestore.collection("gamertags").document(member.gamertag.lowercase().trim()).delete()
      // Remove any duplicate records
      firestore.collection("users").whereEqualTo("gamertag", member.gamertag).get()
        .addOnSuccessListener { query ->
          for (doc in query.documents) {
            doc.reference.delete()
          }
        }
    }
  }

  fun selectChannel(channel: Channel) {
    if (channel.type == ChannelType.TEXT) {
      _activeChannel.value = channel
      chatRepository.selectChannel(channel.id)
      listenToTyping(channel.id)
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
    isSticker: Boolean = false,
    replyTo: ChatMessage? = null
  ) {
    val user = userState.value
    if (user.gamertag.isBlank()) {
      _showGamertagDialog.value = true
      return
    }
    if (_activeChannel.value.isRestrictedToAdmin && !user.isAdmin) {
      return
    }
    if (content.isBlank() && coordinates == null && imageUrl == null && audioUrl == null) return

    setTyping(false)
    val role = if (user.isAdmin || user.gamertag.equals("Siang5680", ignoreCase = true)) "ADMIN" else user.role
    chatRepository.sendMessage(
      channelId = _activeChannel.value.id,
      senderName = user.gamertag,
      senderRole = role,
      content = content.trim(),
      coordinates = coordinates,
      imageUrl = imageUrl,
      audioUrl = audioUrl,
      audioDurationSeconds = audioDurationSeconds,
      isSticker = isSticker,
      replyToId = replyTo?.id,
      replyToSender = replyTo?.senderName,
      replyToContent = replyTo?.content
    )
  }

  fun loginAccount(gamertag: String, pass: String, onResult: (Result<String>) -> Unit) {
    viewModelScope.launch {
      val result = authRepository.loginAccount(gamertag, pass)
      if (result.isSuccess) {
        val tag = result.getOrNull() ?: gamertag
        chatRepository.updateLocalGamertag(tag)
        _showGamertagDialog.value = false
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
      }
      onResult(result)
    }
  }

  fun setGamertagDialogVisible(v: Boolean) { _showGamertagDialog.value = v }
  fun setAgoraDialogVisible(v: Boolean) { _showAgoraDialog.value = v }
  fun setServerInfoSheetVisible(v: Boolean) { _showServerInfoSheet.value = v }
  fun setAdminConsoleVisible(v: Boolean) { _showAdminConsole.value = v }
  fun setSelectContactDialogVisible(v: Boolean) { _showSelectContactDialog.value = v }

  override fun onCleared() {
    super.onCleared()
    typingListener?.remove()
    voiceManager.destroy()
  }
}
