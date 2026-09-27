package com.joseph.substratesmp.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
  val id: String = "",
  val channelId: String = "general-chat",
  val senderName: String = "",
  val senderRole: String = "MEMBER", // "ADMIN", "MEMBER"
  val content: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val isLocalUser: Boolean = false,
  val coordinates: String? = null // e.g. "X: -412, Y: -58, Z: 890"
) {
  val isAdmin: Boolean
    get() = senderRole == "ADMIN" || senderName.equals("Siang5680", ignoreCase = true)

  val formattedTime: String
    get() {
      val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
      return sdf.format(Date(timestamp))
    }
}
