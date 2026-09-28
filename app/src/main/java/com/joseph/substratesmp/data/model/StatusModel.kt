package com.joseph.substratesmp.data.model

data class StatusUpdate(
  val id: String = "",
  val authorGamertag: String = "",
  val content: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val isAdmin: Boolean = false,
  val activityTag: String = "Mining",
  val backgroundTheme: String = "EMERALD", // EMERALD, CRIMSON, END_VOID, DIAMOND, GOLDEN, OBSIDIAN
  val coordinates: String? = null,
  val reactionCounts: Map<String, Int> = emptyMap(),
  val musicTrackName: String? = null,
  val musicArtistName: String? = null,
  val musicPreviewUrl: String? = null,
  val musicArtworkUrl: String? = null
)
