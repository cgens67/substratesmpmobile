package com.joseph.substratesmp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.delay

@Composable
fun TypingBubble(
  typerName: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp),
    color = Color.White,
    shadowElevation = 1.dp,
    modifier = modifier.padding(horizontal = 10.dp, vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = typerName,
        fontSize = 11.5.sp,
        color = WhatsAppGreenDark
      )
      Spacer(modifier = Modifier.width(6.dp))

      // 3 Bouncing Dots
      val dot1 = remember { Animatable(0f) }
      val dot2 = remember { Animatable(0f) }
      val dot3 = remember { Animatable(0f) }

      LaunchedEffect(Unit) {
        val spec = infiniteRepeatable<Float>(
          animation = tween(400, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse
        )
        dot1.animateTo(-4f, spec)
      }
      LaunchedEffect(Unit) {
        delay(130)
        val spec = infiniteRepeatable<Float>(
          animation = tween(400, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse
        )
        dot2.animateTo(-4f, spec)
      }
      LaunchedEffect(Unit) {
        delay(260)
        val spec = infiniteRepeatable<Float>(
          animation = tween(400, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse
        )
        dot3.animateTo(-4f, spec)
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(modifier = Modifier.size(5.dp).offset(y = dot1.value.dp).clip(CircleShape).background(WhatsAppGreenDark))
        Box(modifier = Modifier.size(5.dp).offset(y = dot2.value.dp).clip(CircleShape).background(WhatsAppGreenDark))
        Box(modifier = Modifier.size(5.dp).offset(y = dot3.value.dp).clip(CircleShape).background(WhatsAppGreenDark))
      }
    }
  }
}
