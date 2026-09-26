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
import java.util.UUID

class ChatRepository(private val context: Context) {
  private val TAG = "SubstrateChat"

  // In-memory store per channel
  private val channelMessagesMap = mutableMapOf<String, MutableList<ChatMessage>>()
  private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messagesFlow: StateFlow<List<ChatMessage>> = _messagesFlow.asStateFlow()

  private var activeChannelId: String = "general-chat"
  private var firestoreListener: ListenerRegistration? = null

  init {
    seedInitialMessages()
    selectChannel("general-chat")
  }

  private fun seedInitialMessages() {
    val now = System.currentTimeMillis()

    val announcements = mutableListOf(
      ChatMessage(
        id = "ann-1",
        channelId = "announcements",
        senderName = "ServerAdmin_Alex",
        senderRole = "ADMIN",
        content = "Welcome to Substrate SMP Bedrock Realm! Server IP: mc.substratesmp.net:19132. The Nether Ice Highway is officially open to all districts!",
        timestamp = now - 3600000 * 5,
        coordinates = "Spawn: X: 0, Y: 68, Z: 0"
      ),
      ChatMessage(
        id = "ann-2",
        channelId = "announcements",
        senderName = "ModEnder",
        senderRole = "MOD",
        content = "Agora RTC Engine voice channels are now linked with proximity channels. Join 'General Voice 1' or 'Mining Expedition' anytime!",
        timestamp = now - 3600000 * 2
      )
    )

    val generalChat = mutableListOf(
      ChatMessage(
        id = "gen-1",
        channelId = "general-chat",
        senderName = "RedstoneKing",
        senderRole = "BUILDER",
        content = "Anyone need iron or shulker boxes at spawn trade hall? Trading for ancient debris!",
        timestamp = now - 1800000,
        coordinates = "Trade Hall: X: -140, Y: 72, Z: 88"
      ),
      ChatMessage(
        id = "gen-2",
        channelId = "general-chat",
        senderName = "CreeperWhisperer",
        senderRole = "MEMBER",
        content = "Heading down to Y: -58 for diamond strip mining. Starting voice room 'Mining Expedition' in 2 mins.",
        timestamp = now - 900000,
        coordinates = "Mine: X: -850, Y: -58, Z: 1220"
      ),
      ChatMessage(
        id = "gen-3",
        channelId = "general-chat",
        senderName = "ModEnder",
        senderRole = "MOD",
        content = "Remember to light up the caverns properly so mobs don't spill into the nether portal.",
        timestamp = now - 300000
      )
    )

    channelMessagesMap["announcements"] = announcements
    channelMessagesMap["general-chat"] = generalChat
  }

  fun selectChannel(channelId: String) {
    activeChannelId = channelId
    firestoreListener?.remove()
    firestoreListener = null

    val currentList = channelMessagesMap.getOrPut(channelId) { mutableListOf() }
    _messagesFlow.value = currentList.toList()

    // Attach Firestore real-time listener to substrate-smp
    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
      }

      if (FirebaseApp.getApps(context).isNotEmpty()) {
        val firestore = FirebaseFirestore.getInstance()
        Log.i(TAG, "Attaching Firestore realtime snapshot listener to channels/$channelId/messages")

        firestoreListener = firestore.collection("channels")
          .document(channelId)
          .collection("messages")
          .orderBy("timestamp", Query.Direction.ASCENDING)
          .addSnapshotListener { snapshot, error ->
            if (error != null) {
              Log.w(TAG, "Firestore snapshot listener error for channel $channelId: ${error.message}")
              return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
              val remoteMessages = snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val chId = doc.getString("channelId") ?: channelId
                val sender = doc.getString("senderName") ?: "Player"
                val role = doc.getString("senderRole") ?: "MEMBER"
                val content = doc.getString("content") ?: ""
                val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                val coords = doc.getString("coordinates")
                val isLocal = doc.getString("senderName") == "You"
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
              // Merge seed messages with remote Firestore messages
              val combined = (currentList.filter { it.id.startsWith("ann-") || it.id.startsWith("gen-") } + remoteMessages)
                .distinctBy { it.id }
                .sortedBy { it.timestamp }
              channelMessagesMap[channelId] = combined.toMutableList()
              _messagesFlow.value = combined
              Log.d(TAG, "Loaded ${remoteMessages.size} messages from Firestore for #$channelId")
            }
          }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firestore connection fallback: ${e.message}")
    }
  }

  fun sendMessage(
    channelId: String,
    senderName: String,
    senderRole: String,
    content: String,
    coordinates: String? = null
  ) {
    val newMsg = ChatMessage(
      id = UUID.randomUUID().toString(),
      channelId = channelId,
      senderName = senderName,
      senderRole = senderRole,
      content = content,
      timestamp = System.currentTimeMillis(),
      isLocalUser = true,
      coordinates = coordinates
    )

    val currentList = channelMessagesMap.getOrPut(channelId) { mutableListOf() }
    currentList.add(newMsg)
    if (activeChannelId == channelId) {
      _messagesFlow.value = currentList.toList()
    }

    // Write to live Firestore database for project substrate-smp
    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
      }

      if (FirebaseApp.getApps(context).isNotEmpty()) {
        val firestore = FirebaseFirestore.getInstance()
        val docData = hashMapOf(
          "channelId" to channelId,
          "senderName" to senderName,
          "senderRole" to senderRole,
          "content" to content,
          "timestamp" to newMsg.timestamp,
          "coordinates" to coordinates
        )
        firestore.collection("channels")
          .document(channelId)
          .collection("messages")
          .document(newMsg.id)
          .set(docData)
          .addOnSuccessListener {
            Log.d(TAG, "Message ${newMsg.id} synced to Firestore channel $channelId")
          }
          .addOnFailureListener { e ->
            Log.w(TAG, "Firestore sync failed: ${e.message}")
          }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Local message buffered; Firestore write error: ${e.message}")
    }
  }
}
