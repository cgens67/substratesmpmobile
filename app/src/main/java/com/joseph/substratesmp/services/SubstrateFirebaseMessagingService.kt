package com.joseph.substratesmp.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.joseph.substratesmp.ui.components.NotificationHelper

class SubstrateFirebaseMessagingService : FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    // Upload the new token to Firestore so the Backend can send notifications to this specific device
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    try {
      FirebaseFirestore.getInstance().collection("users").document(uid)
        .update("fcmToken", token)
    } catch (e: Exception) {
      Log.e("FCM", "Failed to update token: ${e.message}")
    }
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    // Parse pure Data payload sent from your backend (if you have Cloud Functions running)
    val data = remoteMessage.data
    if (data.isNotEmpty()) {
      val type = data["type"]
      if (type == "chat") {
        val title = data["title"] ?: "New Message"
        val content = data["content"] ?: ""
        val channelId = data["channelId"] ?: "chat"
        
        // Prevent showing notifications for channels the user has manually muted locally
        val prefs = getSharedPreferences("substrate_chat_settings", MODE_PRIVATE)
        val mutedChannels = prefs.getStringSet("muted_channels", emptySet()) ?: emptySet()
        
        if (!mutedChannels.contains(channelId)) {
          NotificationHelper.showMessageNotification(
            context = applicationContext,
            notificationId = channelId.hashCode(),
            title = title,
            content = content
          )
        }
      } else if (type == "call") {
        val caller = data["caller"] ?: "Someone"
        val callId = data["callId"] ?: "call"
        val isVideo = data["isVideo"].toBoolean()
        
        NotificationHelper.showIncomingCallNotification(
          context = applicationContext,
          callId = callId,
          caller = caller,
          isVideo = isVideo
        )
      }
    }
  }
}
