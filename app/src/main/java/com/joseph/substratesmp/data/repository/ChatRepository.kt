package com.joseph.substratesmp.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.ui.components.SoundHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository(private val context: Context) {
  private val TAG = "SubstrateChat"

  private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messagesFlow: StateFlow<List<ChatMessage>> = _messagesFlow.asStateFlow()

  private var activeChannelId: String = "general-chat"
  private var firestoreListener: ListenerRegistration? = null
  private var currentGamertag: String = ""
  private var isInitialLoadDone = false

  private val firestore: FirebaseFirestore by lazy {
    if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context)
    FirebaseFirestore.getInstance()
  }

  init {
    selectChannel("general-chat")
  }

  fun updateLocalGamertag(gamertag: String) {
    currentGamertag = gamertag
    _messagesFlow.value = _messagesFlow.value.map { msg ->
      msg.copy(isLocalUser = gamertag.isNotBlank() && msg.senderName.equals(gamertag, ignoreCase = true))
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
            val liveMessages = snapshot.documents.mapNotNull { doc ->
              val sender = doc.getString("senderName") ?: "Player"
              val isLocal = currentGamertag.isNotBlank() && sender.equals(currentGamertag, ignoreCase = true)
              val readByList = (doc.get("readBy") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

              if (!isLocal && currentGamertag.isNotBlank() && !readByList.contains(currentGamertag)) {
                doc.reference.update("readBy", FieldValue.arrayUnion(currentGamertag))
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
                SoundHelper.playMessageSound(context)
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

    // Subcollection chunking: If payload > 600KB (e.g. your 1.18MB audio file), chunk it to bypass Firestore 1MB doc limit
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
}
