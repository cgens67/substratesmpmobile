package com.joseph.substratesmp.data.model

data class StatusUpdate(
  val id: String = "",
  val authorGamertag: String = "",
  val content: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val isAdmin: Boolean = false,
  val emoji: String = "⛏️"
)
