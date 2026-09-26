package com.joseph.substratesmp.data.model

data class VoiceParticipant(
  val id: String,
  val name: String,
  val isSpeaking: Boolean = false,
  val isMuted: Boolean = false,
  val isDeafened: Boolean = false,
  val isLocal: Boolean = false,
  val audioLevel: Float = 0.5f // 0.0f to 1.0f for visualizer
)

data class ActiveVoiceRoom(
  val channelId: String,
  val channelName: String,
  val isConnected: Boolean = false,
  val isConnecting: Boolean = false,
  val isMuted: Boolean = false,
  val isDeafened: Boolean = false,
  val isSpeakerOn: Boolean = true,
  val isCameraOn: Boolean = false,
  val participants: List<VoiceParticipant> = emptyList(),
  val appId: String = "f2cf0761f7584f48b8d647b7b29c8572",
  val statusMessage: String = "Connected (Agora RTC Engine)"
)
