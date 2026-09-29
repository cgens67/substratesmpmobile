package com.joseph.substratesmp.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore

class CallActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val callId = intent.getStringExtra("callId") ?: return

    // Stop ringtone and cancel notification
    CallRingtoneHelper.stopRinging(context)
    NotificationHelper.dismissCallNotification(context, callId.hashCode())

    // Decline call in Firestore
    try {
      FirebaseFirestore.getInstance()
        .collection("active_calls")
        .document(callId)
        .update("status", "declined")
    } catch (_: Exception) {}
  }
}
