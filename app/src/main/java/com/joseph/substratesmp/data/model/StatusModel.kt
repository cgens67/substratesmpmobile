package com.joseph.substratesmp.data.model

data class StatusUpdate(
  val id: String = "",
  val authorGamertag: String = "",
  val content: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val isAdmin: Boolean = false,
  val activityTag: String = "Note",
  val backgroundTheme: String = "EMERALD",
  val coordinates: String? = null,
  val reactionCounts: Map<String, Int> = emptyMap(),
  val musicTrackName: String? = null,
  val musicArtistName: String? = null,
  val musicPreviewUrl: String? = null,
  val musicArtworkUrl: String? = null,
  val musicStartTimeMs: Int = 0 // Custom section start offset selected by user
)
