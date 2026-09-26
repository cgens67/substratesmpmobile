package com.joseph.substratesmp.data.model

enum class ChannelType {
  TEXT,
  VOICE
}

data class Channel(
  val id: String,
  val name: String,
  val type: ChannelType,
  val category: String,
  val description: String = "",
  val unreadCount: Int = 0,
  val activeUsersCount: Int = 0
)

val SeedChannels = listOf(
  Channel(
    id = "announcements",
    name = "announcements",
    type = ChannelType.TEXT,
    category = "TEXT CHANNELS",
    description = "Official announcements, realm server IP updates, and patch notes",
    unreadCount = 1
  ),
  Channel(
    id = "general-chat",
    name = "general-chat",
    type = ChannelType.TEXT,
    category = "TEXT CHANNELS",
    description = "General banter, base coordinates, mega-builds, and trades",
    unreadCount = 0
  ),
  Channel(
    id = "voice-general-1",
    name = "General Voice 1",
    type = ChannelType.VOICE,
    category = "VOICE CHANNELS",
    description = "Public voice room for builders and chill hangouts",
    activeUsersCount = 3
  ),
  Channel(
    id = "voice-mining",
    name = "Mining Expedition",
    type = ChannelType.VOICE,
    category = "VOICE CHANNELS",
    description = "Deepslate mining, diamond hunting, and Ancient City raids",
    activeUsersCount = 2
  )
)
