package com.joseph.substratesmp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Substrate SMP Brand & Expressive Minecraft/Discord Tonal Palette
val CyanPrimary = Color(0xFF00E5FF)
val CyanOnPrimary = Color(0xFF00363D)
val CyanPrimaryContainer = Color(0xFF004D56)
val CyanOnPrimaryContainer = Color(0xFF80F3FF)

val EmeraldSecondary = Color(0xFF00E676)
val EmeraldOnSecondary = Color(0xFF003918)
val EmeraldSecondaryContainer = Color(0xFF005225)
val EmeraldOnSecondaryContainer = Color(0xFF7BFF9F)

val PortalPurpleTertiary = Color(0xFFB388FF)
val PortalPurpleOnTertiary = Color(0xFF2C0066)
val PortalPurpleTertiaryContainer = Color(0xFF4900A3)
val PortalPurpleOnTertiaryContainer = Color(0xFFDCC2FF)

// Obsidian / Bedrock Dark Surface Elevation
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

// Accent / Gaming Role & Voice Status Tokens
val RoleAdminGold = Color(0xFFFFC107)
val RoleAdminGoldContainer = Color(0xFF332700)
val RoleModCyan = Color(0xFF00E5FF)
val RoleMemberGreen = Color(0xFF4ADE80)
val RoleBotViolet = Color(0xFFA78BFA)
val StatusOnline = Color(0xFF22C55E)
val StatusOnlineGreen = Color(0xFF25D366)
val StatusIdle = Color(0xFFFBBF24)
val StatusDnd = Color(0xFFEF4444)
val StatusVoiceActive = Color(0xFF10B981)
val StatusCallActive = Color(0xFF00A884)
val StatusMuted = Color(0xFFF43F5E)
val StatusCallEndRed = Color(0xFFEA0038)
val BedrockGlass = Color(0x331B2333)

// WhatsApp / Google Material Signature Colors
val WhatsAppTeal = Color(0xFF008069)
val WhatsAppTealDark = Color(0xFF0B141B)
val WhatsAppTealHeader = Color(0xFF1F2C34)
val WhatsAppGreenPrimary = Color(0xFF25D366)
val WhatsAppChatOutgoing = Color(0xFF005C4B)
val WhatsAppChatIncoming = Color(0xFF1F2C34)
val WhatsAppBackgroundDark = Color(0xFF0B141B)
val WhatsAppSurface = Color(0xFF121B22)
val WhatsAppSurfaceHigh = Color(0xFF233138)
val WhatsAppCheckmarkBlue = Color(0xFF53BDEB)
val WhatsAppTextPrimary = Color(0xFFE9EDEF)
val WhatsAppTextSecondary = Color(0xFF8696A0)
val WhatsAppDivider = Color(0xFF202C33)

// Light Palette
val LightPrimary = Color(0xFF006874)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFF8FF0FF)
val LightOnPrimaryContainer = Color(0xFF001F24)

val LightSecondary = Color(0xFF006C32)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFF7FFEA3)
val LightOnSecondaryContainer = Color(0xFF00210A)

val LightTertiary = Color(0xFF673AB7)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFE9DDFF)
val LightOnTertiaryContainer = Color(0xFF23005C)

val LightBackground = Color(0xFFF7F9FC)
val LightOnBackground = Color(0xFF171C22)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF171C22)
val LightSurfaceVariant = Color(0xFFDEE3EC)
val LightOnSurfaceVariant = Color(0xFF424750)
val LightOutline = Color(0xFF727781)

@Immutable
data class SubstrateCustomColors(
  val adminGold: Color = RoleAdminGold,
  val modCyan: Color = RoleModCyan,
  val memberGreen: Color = RoleMemberGreen,
  val botViolet: Color = RoleBotViolet,
  val statusOnline: Color = StatusOnlineGreen,
  val statusIdle: Color = StatusIdle,
  val statusDnd: Color = StatusDnd,
  val statusVoiceActive: Color = StatusCallActive,
  val statusMuted: Color = StatusCallEndRed,
  val voiceVisualizerGlow: Color = CyanPrimary,
  val surfaceGlass: Color = BedrockGlass,
  val checkmarkBlue: Color = WhatsAppCheckmarkBlue,
  val chatOutgoing: Color = WhatsAppChatOutgoing,
  val chatIncoming: Color = WhatsAppChatIncoming,
  val headerTeal: Color = WhatsAppTealHeader
)
