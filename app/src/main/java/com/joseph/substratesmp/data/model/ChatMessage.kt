package com.joseph.substratesmp.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
  val id: String = "",
  val channelId: String = "general-chat",
  val senderName: String = "",
  val senderRole: String = "MEMBER",
  val content: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val isLocalUser: Boolean = false,
  val coordinates: String? = null,
  val imageUrl: String? = null,
  val audioUrl: String? = null,
  val audioDurationSeconds: Int = 0,
  val fileUrl: String? = null,
  val fileName: String? = null,
  val isSticker: Boolean = false,
  val replyToId: String? = null,
  val replyToSender: String? = null,
  val replyToContent: String? = null,
  val readBy: List<String> = emptyList() // Tracks who has seen this message
) {
  val isAdmin: Boolean
    get() = senderRole == "ADMIN" || senderName.equals("Siang5680", ignoreCase = true)

  val formattedTime: String
    get() {
      val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
      return sdf.format(Date(timestamp))
    }
}
