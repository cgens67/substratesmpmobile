package com.joseph.substratesmp.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
  val id: String = "",
  val channelId: String = "general-chat",
  val senderName: String = "Steve",
  val senderRole: String = "MEMBER", // "ADMIN", "MOD", "BUILDER", "MEMBER"
  val content: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val isLocalUser: Boolean = false,
  val coordinates: String? = null // e.g. "X: -412, Y: -58, Z: 890"
) {
  val formattedTime: String
    get() {
      val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
      return sdf.format(Date(timestamp))
    }
}
