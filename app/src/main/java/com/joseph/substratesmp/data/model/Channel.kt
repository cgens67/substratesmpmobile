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
  val activeUsersCount: Int = 0
)

val DefaultChannels = listOf(
  Channel(
    id = "announcements",
    name = "announcements",
    type = ChannelType.TEXT,
    category = "TEXT CHANNELS",
    description = "Official announcements, realm server IP updates, and patch notes"
  ),
  Channel(
    id = "general-chat",
    name = "general-chat",
    type = ChannelType.TEXT,
    category = "TEXT CHANNELS",
    description = "General banter, base coordinates, mega-builds, and trades"
  ),
  Channel(
    id = "voice-general-1",
    name = "General Voice 1",
    type = ChannelType.VOICE,
    category = "VOICE CHANNELS",
    description = "Public voice room for builders and chill hangouts"
  ),
  Channel(
    id = "voice-mining",
    name = "Mining Expedition",
    type = ChannelType.VOICE,
    category = "VOICE CHANNELS",
    description = "Deepslate mining, diamond hunting, and Ancient City raids"
  )
)
