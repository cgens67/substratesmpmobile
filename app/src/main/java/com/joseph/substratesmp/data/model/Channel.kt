package com.joseph.substratesmp.data.model

enum class ChannelType {
  TEXT,
  VOICE
}

data class Channel(
  val id: String = "",
  val name: String = "",
  val type: ChannelType = ChannelType.TEXT,
  val category: String = "TEXT CHANNELS",
  val description: String = "",
  val unreadCount: Int = 0,
  val activeUsersCount: Int = 0,
  val allowedRolesToSend: List<String> = listOf("ALL"),
  val isDm: Boolean = false,
  val dmRecipientGamertag: String? = null
) {
  val isRestrictedToAdmin: Boolean
    get() = !isDm && (id == "announcements" || (allowedRolesToSend.contains("ADMIN") && !allowedRolesToSend.contains("ALL")))
}

val DefaultChannels = listOf(
  Channel(
    id = "announcements",
    name = "announcements",
    type = ChannelType.TEXT,
    category = "TEXT CHANNELS",
    description = "Official announcements & realm IP updates",
    allowedRolesToSend = listOf("ADMIN")
  ),
  Channel(
    id = "general-chat",
    name = "general-chat",
    type = ChannelType.TEXT,
    category = "TEXT CHANNELS",
    description = "General realm banter and base coords",
    allowedRolesToSend = listOf("ALL")
  ),
  Channel(
    id = "voice-general-1",
    name = "General Voice 1",
    type = ChannelType.VOICE,
    category = "VOICE CHANNELS",
    description = "Public room for hangout & building"
  ),
  Channel(
    id = "voice-mining",
    name = "Mining Expedition",
    type = ChannelType.VOICE,
    category = "VOICE CHANNELS",
    description = "Ancient City raids & deepslate mining"
  )
)
