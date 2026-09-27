package com.joseph.substratesmp.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.ui.components.NotificationHelper
import com.joseph.substratesmp.ui.components.SoundHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository(private val context: Context) {
  private val TAG = "SubstrateChat"
  private val prefs: SharedPreferences = context.getSharedPreferences("substrate_chat_settings", Context.MODE_PRIVATE)

  private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messagesFlow: StateFlow<List<ChatMessage>> = _messagesFlow.asStateFlow()

  private val _mutedChannels = MutableStateFlow<Set<String>>(prefs.getStringSet("muted_channels", emptySet()) ?: emptySet())
  val mutedChannels: StateFlow<Set<String>> = _mutedChannels.asStateFlow()

  private val _blockedUsers = MutableStateFlow<Set<String>>(prefs.getStringSet("blocked_users", emptySet()) ?: emptySet())
  val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

  private var activeChannelId: String = "general-chat"
  private var firestoreListener: ListenerRegistration? = null
  private var currentGamertag: String = ""
  private var isInitialLoadDone = false

  private val firestore: FirebaseFirestore by lazy {
    if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context)
    FirebaseFirestore.getInstance()
  }

  init {
    NotificationHelper.init(context)
    selectChannel("general-chat")
  }

  fun isChannelMuted(channelId: String): Boolean = _mutedChannels.value.contains(channelId)

  fun toggleMuteChannel(channelId: String): Boolean {
    val current = _mutedChannels.value.toMutableSet()
    val isNowMuted = if (current.contains(channelId)) {
      current.remove(channelId)
      false
    } else {
      current.add(channelId)
      true
    }
    prefs.edit().putStringSet("muted_channels", current).apply()
    _mutedChannels.value = current
    return isNowMuted
  }

  fun isUserBlocked(gamertag: String): Boolean = _blockedUsers.value.contains(gamertag.lowercase().trim())

  fun toggleBlockUser(gamertag: String): Boolean {
    val clean = gamertag.lowercase().trim()
    val current = _blockedUsers.value.toMutableSet()
    val isNowBlocked = if (current.contains(clean)) {
      current.remove(clean)
      false
    } else {
      current.add(clean)
      true
    }
    prefs.edit().putStringSet("blocked_users", current).apply()
    _blockedUsers.value = current
    return isNowBlocked
  }

  fun updateLocalGamertag(gamertag: String) {
    currentGamertag = gamertag.trim()
    _messagesFlow.value = _messagesFlow.value.map { msg ->
      msg.copy(isLocalUser = currentGamertag.isNotBlank() && msg.senderName.equals(currentGamertag, ignoreCase = true))
    }
  }

  fun selectChannel(channelId: String) {
    activeChannelId = channelId
    firestoreListener?.remove()
    firestoreListener = null
    _messagesFlow.value = emptyList()
    isInitialLoadDone = false

    try {
      firestoreListener = firestore.collection("channels")
        .document(channelId)
        .collection("messages")
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, error ->
          if (error != null) return@addSnapshotListener

          if (snapshot != null) {
            val previousCount = _messagesFlow.value.size
            val cleanMyTag = currentGamertag.trim()

            val liveMessages = snapshot.documents.mapNotNull { doc ->
              val sender = doc.getString("senderName") ?: "Player"
              val isLocal = cleanMyTag.isNotBlank() && sender.equals(cleanMyTag, ignoreCase = true)
              val readByList = (doc.get("readBy") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

              // Case-insensitive check: marks message as read when incoming
              val hasAlreadyRead = readByList.any { it.equals(cleanMyTag, ignoreCase = true) }
              if (!isLocal && cleanMyTag.isNotBlank() && !hasAlreadyRead) {
                doc.reference.update("readBy", FieldValue.arrayUnion(cleanMyTag))
              }

              ChatMessage(
                id = doc.id,
                channelId = doc.getString("channelId") ?: channelId,
                senderName = sender,
                senderRole = if (sender.equals("Siang5680", ignoreCase = true)) "ADMIN" else doc.getString("senderRole") ?: "MEMBER",
                content = doc.getString("content") ?: "",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                isLocalUser = isLocal,
                coordinates = doc.getString("coordinates"),
                imageUrl = doc.getString("imageUrl"),
                audioUrl = doc.getString("audioUrl"),
                audioDurationSeconds = doc.getLong("audioDurationSeconds")?.toInt() ?: 0,
                fileUrl = doc.getString("fileUrl"),
                fileName = doc.getString("fileName"),
                isSticker = doc.getBoolean("isSticker") ?: false,
                replyToId = doc.getString("replyToId"),
                replyToSender = doc.getString("replyToSender"),
                replyToContent = doc.getString("replyToContent"),
                readBy = readByList
              )
            }

            if (isInitialLoadDone && liveMessages.size > previousCount) {
              val newest = liveMessages.lastOrNull()
              if (newest != null && !newest.isLocalUser) {
                val senderClean = newest.senderName.lowercase().trim()
                if (!isChannelMuted(channelId) && !isUserBlocked(senderClean)) {
                  SoundHelper.playMessageSound(context)

                  val notifTitle = if (channelId.startsWith("dm_")) newest.senderName else "#$channelId • ${newest.senderName}"
                  val notifContent = when {
                    newest.isSticker -> "💟 Sticker"
                    newest.imageUrl != null -> "📷 Photo"
                    newest.audioUrl != null -> "🎤 Voice message"
                    newest.fileUrl != null -> "📄 ${newest.fileName ?: "Document"}"
                    else -> newest.content
                  }
                  NotificationHelper.showMessageNotification(
                    context = context,
                    notificationId = channelId.hashCode(),
                    title = notifTitle,
                    content = notifContent
                  )
                }
              }
            }
            isInitialLoadDone = true
            _messagesFlow.value = liveMessages
          }
        }
    } catch (_: Exception) {}
  }

  fun sendMessage(
    channelId: String,
    senderName: String,
    senderRole: String,
    content: String,
    coordinates: String? = null,
    imageUrl: String? = null,
    audioUrl: String? = null,
    audioDurationSeconds: Int = 0,
    fileUrl: String? = null,
    fileName: String? = null,
    isSticker: Boolean = false,
    replyToId: String? = null,
    replyToSender: String? = null,
    replyToContent: String? = null
  ) {
    if (content.isBlank() && coordinates == null && imageUrl == null && audioUrl == null && fileUrl == null) return
    if (senderName.isBlank()) return

    val effectiveRole = if (senderName.equals("Siang5680", ignoreCase = true)) "ADMIN" else senderRole
    val docRef = firestore.collection("channels").document(channelId).collection("messages").document()

    val CHUNK_LIMIT = 500_000
    var finalAudioUrl = audioUrl
    var isAudioChunked = false
    var audioChunks = emptyList<String>()

    if (audioUrl != null && audioUrl.length > CHUNK_LIMIT) {
      isAudioChunked = true
      audioChunks = audioUrl.chunked(CHUNK_LIMIT)
      finalAudioUrl = "chunked:${audioChunks.size}"
    }

    var finalFileUrl = fileUrl
    var isFileChunked = false
    var fileChunks = emptyList<String>()

    if (fileUrl != null && fileUrl.length > CHUNK_LIMIT) {
      isFileChunked = true
      fileChunks = fileUrl.chunked(CHUNK_LIMIT)
      finalFileUrl = "chunked:${fileChunks.size}"
    }

    val docData = hashMapOf(
      "channelId" to channelId,
      "senderName" to senderName,
      "senderRole" to effectiveRole,
      "content" to content,
      "timestamp" to System.currentTimeMillis(),
      "coordinates" to coordinates,
      "imageUrl" to imageUrl,
      "audioUrl" to finalAudioUrl,
      "audioDurationSeconds" to audioDurationSeconds,
      "fileUrl" to finalFileUrl,
      "fileName" to fileName,
      "isSticker" to isSticker,
      "replyToId" to replyToId,
      "replyToSender" to replyToSender,
      "replyToContent" to replyToContent,
      "readBy" to emptyList<String>()
    )

    docRef.set(docData).addOnSuccessListener {
      if (isAudioChunked) {
        audioChunks.forEachIndexed { idx, chunk ->
          docRef.collection("audioChunks").document(String.format("%03d", idx)).set(mapOf("data" to chunk))
        }
      }
      if (isFileChunked) {
        fileChunks.forEachIndexed { idx, chunk ->
          docRef.collection("fileChunks").document(String.format("%03d", idx)).set(mapOf("data" to chunk))
        }
      }
    }
  }

  fun deleteMessage(channelId: String, messageId: String) {
    val docRef = firestore.collection("channels").document(channelId).collection("messages").document(messageId)
    docRef.collection("audioChunks").get().addOnSuccessListener { snapshot ->
      for (doc in snapshot.documents) doc.reference.delete()
    }
    docRef.collection("fileChunks").get().addOnSuccessListener { snapshot ->
      for (doc in snapshot.documents) doc.reference.delete()
    }
    docRef.delete()
  }
}
