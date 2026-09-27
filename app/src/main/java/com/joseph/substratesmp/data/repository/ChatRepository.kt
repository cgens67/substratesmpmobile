package com.joseph.substratesmp.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.joseph.substratesmp.data.model.ChatMessage
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

  private val firestore: FirebaseFirestore by lazy {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
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

    try {
      Log.i(TAG, "Attaching live Firestore listener for channels/$channelId/messages")
      firestoreListener = firestore.collection("channels")
        .document(channelId)
        .collection("messages")
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.e(TAG, "Firestore messages listener error for #$channelId: ${error.message}", error)
            return@addSnapshotListener
          }

          if (snapshot != null) {
            val liveMessages = snapshot.documents.mapNotNull { doc ->
              val id = doc.id
              val chId = doc.getString("channelId") ?: channelId
              val sender = doc.getString("senderName") ?: "Player"
              val rawRole = doc.getString("senderRole") ?: "MEMBER"
              // Ensure Siang5680 always displays ADMIN
              val role = if (sender.equals("Siang5680", ignoreCase = true)) "ADMIN" else rawRole
              val content = doc.getString("content") ?: ""
              val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
              val coords = doc.getString("coordinates")
              val isLocal = currentGamertag.isNotBlank() && sender.equals(currentGamertag, ignoreCase = true)

              ChatMessage(
                id = id,
                channelId = chId,
                senderName = sender,
                senderRole = role,
                content = content,
                timestamp = ts,
                isLocalUser = isLocal,
                coordinates = coords
              )
            }
            _messagesFlow.value = liveMessages
            Log.d(TAG, "Live Firestore synced ${liveMessages.size} messages for #$channelId")
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Error connecting to live Firestore: ${e.message}", e)
    }
  }

  fun sendMessage(
    channelId: String,
    senderName: String,
    senderRole: String,
    content: String,
    coordinates: String? = null
  ) {
    if (content.isBlank() && coordinates == null) return
    if (senderName.isBlank()) return

    val effectiveRole = if (senderName.equals("Siang5680", ignoreCase = true)) "ADMIN" else senderRole
    val docRef = firestore.collection("channels")
      .document(channelId)
      .collection("messages")
      .document()

    val docData = hashMapOf(
      "channelId" to channelId,
      "senderName" to senderName,
      "senderRole" to effectiveRole,
      "content" to content,
      "timestamp" to System.currentTimeMillis(),
      "coordinates" to coordinates
    )

    docRef.set(docData)
      .addOnSuccessListener {
        Log.d(TAG, "Live message ${docRef.id} written to Firestore successfully in #$channelId")
      }
      .addOnFailureListener { error ->
        Log.e(TAG, "Failed writing message to Firestore in #$channelId: ${error.message}", error)
      }
  }
}
