package com.joseph.substratesmp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Unified Blue Signature Palette (Replaced WhatsApp Green)
val WhatsAppGreen = Color(0xFF0A84FF)
val WhatsAppGreenDark = Color(0xFF007AFF)
val WhatsAppGreenTeal = Color(0xFF0A84FF)
val WhatsAppHeaderGreen = Color(0xFF007AFF)
val WhatsAppBackground = Color(0xFFFFFFFF)
val WhatsAppSearchBackground = Color(0xFFF0F2F5)
val WhatsAppChatBackground = Color(0xFFF0F4F8)
val WhatsAppOutgoingBubble = Color(0xFFD6E9FF)
val WhatsAppIncomingBubble = Color(0xFFFFFFFF)
val WhatsAppChatOutgoing = Color(0xFF0052A3)
val WhatsAppChatIncoming = Color(0xFF1F2634)
val WhatsAppSurfaceHigh = Color(0xFF232B38)
val WhatsAppNavSelectedPill = Color(0xFFD6E9FF)
val WhatsAppChipUnselected = Color(0xFFFFFFFF)
val WhatsAppTextPrimary = Color(0xFF111B21)
val WhatsAppTextSecondary = Color(0xFF667781)
val WhatsAppCheckmarkBlue = Color(0xFF007AFF)
val WhatsAppCallRed = Color(0xFFEA0038)
val StatusCallEndRed = Color(0xFFEA0038)
val WhatsAppDivider = Color(0xFFE2E4E8)

// EXACT CHAT THEME COLORS
// Dark Mode:
val ChatDarkBackground = Color(0xFF161616)
val ChatDarkIncomingBubble = Color(0xFF262628)
val ChatDarkOutgoingBubble = Color(0xFF0866C6)
val ChatDarkDatePill = Color(0xFF2C2C2E)
val ChatDarkOutgoingTime = Color(0xFF4FA5FF)
val ChatDarkIncomingTime = Color(0xFF8E8E93)

// Light Mode:
val ChatLightBackground = Color(0xFFFFFFFF)
val ChatLightIncomingBubble = Color(0xFFF2F2F7)
val ChatLightOutgoingBubble = Color(0xFF0084FF)
val ChatLightDatePill = Color(0xFFF2F2F7)
val ChatLightOutgoingTime = Color(0xFFBCE0FD)
val ChatLightIncomingTime = Color(0xFF8E8E93)

// Minecraft Bedrock & Admin Role Tokens
val RoleAdminGold = Color(0xFFD48806)
val RoleAdminGoldContainer = Color(0xFFFFF1D6)
val RoleMemberGreen = Color(0xFF007AFF)
val RoleModCyan = Color(0xFF0A84FF)
val RoleBotViolet = Color(0xFF7C4DFF)
val StatusOnline = Color(0xFF0A84FF)
val StatusOnlineGreen = Color(0xFF0A84FF)
val StatusIdle = Color(0xFFFBBF24)
val StatusDnd = Color(0xFFEF4444)
val StatusVoiceActive = Color(0xFF0A84FF)
val StatusCallActive = Color(0xFF0A84FF)
val StatusMuted = Color(0xFFEA0038)
val BedrockGlass = Color(0x331B2333)

// Scheme Tokens
val CyanPrimary = Color(0xFF00E5FF)
val CyanOnPrimary = Color(0xFF00363D)
val CyanPrimaryContainer = Color(0xFF004D56)
val CyanOnPrimaryContainer = Color(0xFF80F3FF)
val EmeraldSecondary = Color(0xFF0A84FF)
val EmeraldOnSecondary = Color(0xFFFFFFFF)
val EmeraldSecondaryContainer = Color(0xFF004085)
val EmeraldOnSecondaryContainer = Color(0xFFD6E9FF)
val PortalPurpleTertiary = Color(0xFFB388FF)
val PortalPurpleOnTertiary = Color(0xFF2C0066)
val PortalPurpleTertiaryContainer = Color(0xFF4900A3)
val PortalPurpleOnTertiaryContainer = Color(0xFFDCC2FF)

val BedrockDark = Color(0xFF0B0E14)
val BedrockSurface = Color(0xFF111622)
val BedrockSurfaceDim = Color(0xFF0F131C)
val BedrockSurfaceBright = Color(0xFF273142)
val BedrockSurfaceContainerLowest = Color(0xFF090C12)
val BedrockSurfaceContainerLow = Color(0xFF141A27)
val BedrockSurfaceContainer = Color(0xFF1B2333)
val BedrockSurfaceContainerHigh = Color(0xFF242E43)
val BedrockSurfaceContainerHighest = Color(0xFF2E3B55)
val BedrockOnSurface = Color(0xFFE3E8F5)
val BedrockOnSurfaceVariant = Color(0xFFA0ACC2)
val BedrockOutline = Color(0xFF485672)
val BedrockOutlineVariant = Color(0xFF2D374B)

val LightPrimary = Color(0xFF007AFF)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD6E9FF)
val LightOnPrimaryContainer = Color(0xFF001D36)
val LightSecondary = Color(0xFF0A84FF)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE1F0FF)
val LightOnSecondaryContainer = Color(0xFF001D36)
val LightTertiary = Color(0xFF007AFF)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFE8F1FC)
val LightOnTertiaryContainer = Color(0xFF111B21)
val LightBackground = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF111B21)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF111B21)
val LightSurfaceVariant = Color(0xFFF0F2F5)
val LightOnSurfaceVariant = Color(0xFF667781)
val LightOutline = Color(0xFFE9EDEF)

@Immutable
data class SubstrateCustomColors(
  val adminGold: Color = RoleAdminGold,
  val modCyan: Color = RoleModCyan,
  val memberGreen: Color = RoleMemberGreen,
  val botViolet: Color = RoleBotViolet,
  val statusOnline: Color = StatusOnline,
  val statusIdle: Color = StatusIdle,
  val statusDnd: Color = StatusDnd,
  val statusVoiceActive: Color = StatusVoiceActive,
  val statusMuted: Color = StatusMuted,
  val voiceVisualizerGlow: Color = WhatsAppGreen,
  val surfaceGlass: Color = BedrockGlass,
  val checkmarkBlue: Color = WhatsAppCheckmarkBlue,
  val chatOutgoing: Color = WhatsAppOutgoingBubble,
  val chatIncoming: Color = WhatsAppIncomingBubble,
  val headerTeal: Color = WhatsAppGreenDark
)
