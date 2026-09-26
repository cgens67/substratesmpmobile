package com.joseph.substratesmp.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material Design 3 Expressive Shapes
val SubstrateShapes = Shapes(
  extraSmall = RoundedCornerShape(6.dp),
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(32.dp),
)

// Specialized Expressive Shapes for Discord/Minecraft Bedrock UI
val MessageBubbleStartShape = RoundedCornerShape(
  topStart = 4.dp,
  topEnd = 18.dp,
  bottomEnd = 18.dp,
  bottomStart = 18.dp
)

val MessageBubbleOtherShape = RoundedCornerShape(
  topStart = 18.dp,
  topEnd = 18.dp,
  bottomEnd = 18.dp,
  bottomStart = 4.dp
)

val ChannelPillShape = RoundedCornerShape(14.dp)
val VoiceOverlayCardShape = RoundedCornerShape(28.dp)
val ServerBadgeShape = RoundedCornerShape(18.dp)
val ActiveSpeakerShape = RoundedCornerShape(22.dp)
