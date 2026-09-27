package com.joseph.substratesmp.data.model

data class ServerSticker(
  val id: String = "",
  val name: String = "",
  val imageData: String = "", // Base64 data
  val uploadedBy: String = "",
  val timestamp: Long = System.currentTimeMillis()
)
