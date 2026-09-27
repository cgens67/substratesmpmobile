package com.joseph.substratesmp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// WhatsApp / Google Material You Signature Color Palette
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

// Admin & Role Badges
val RoleAdminGold = Color(0xFFFFC107)
val RoleAdminGoldContainer = Color(0xFF332700)
val StatusOnlineGreen = Color(0xFF25D366)
val StatusCallActive = Color(0xFF00A884)
val StatusCallEndRed = Color(0xFFEA0038)

// Text Colors
val WhatsAppTextPrimary = Color(0xFFE9EDEF)
val WhatsAppTextSecondary = Color(0xFF8696A0)
val WhatsAppDivider = Color(0xFF202C33)

@Immutable
data class SubstrateCustomColors(
  val adminGold: Color = RoleAdminGold,
  val statusOnline: Color = StatusOnlineGreen,
  val statusVoiceActive: Color = StatusCallActive,
  val statusMuted: Color = StatusCallEndRed,
  val checkmarkBlue: Color = WhatsAppCheckmarkBlue,
  val chatOutgoing: Color = WhatsAppChatOutgoing,
  val chatIncoming: Color = WhatsAppChatIncoming,
  val headerTeal: Color = WhatsAppTealHeader
)
