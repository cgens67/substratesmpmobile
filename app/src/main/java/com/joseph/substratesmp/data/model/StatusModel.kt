package com.joseph.substratesmp.data.model

data class StatusUpdate(
  val id: String = "",
  val authorGamertag: String = "",
  val content: String = "", // Instagram Note thought text (e.g. "Mining Ancient Cities ⛏️")
  val timestamp: Long = System.currentTimeMillis(),
  val isAdmin: Boolean = false,
  val activityTag: String = "Note",
  val backgroundTheme: String = "EMERALD",
  val coordinates: String? = null,
  val reactionCounts: Map<String, Int> = emptyMap(),
  val musicTrackName: String? = null,
  val musicArtistName: String? = null,
  val musicPreviewUrl: String? = null, // 30-second audio stream URL
  val musicArtworkUrl: String? = null
)
