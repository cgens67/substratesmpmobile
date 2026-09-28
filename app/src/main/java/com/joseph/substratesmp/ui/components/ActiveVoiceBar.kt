package com.joseph.substratesmp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.model.ActiveVoiceRoom
import com.joseph.substratesmp.data.model.VoiceParticipant
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.StatusCallEndRed
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

@Composable
fun ActiveVoiceBar(
  voiceRoom: ActiveVoiceRoom,
  onToggleMute: () -> Unit,
  onToggleSpeaker: () -> Unit,
  onToggleDeafen: () -> Unit,
  onToggleCamera: () -> Unit,
  onDisconnect: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isMinimized by rememberSaveable { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 4.dp)
      .testTag("active_voice_bar"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2C34)),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      // Header Bar (With Minimize/Collapse Toggle Button)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(Color(0xFF25D366).copy(alpha = pulseAlpha))
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = voiceRoom.channelName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = if (voiceRoom.isConnecting) "Connecting..." else "${voiceRoom.participants.size} participant(s) in call",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF25D366),
              fontSize = 11.sp
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Minimize / Expand Toggle Button
          IconButton(
            onClick = { isMinimized = !isMinimized },
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = if (isMinimized) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
              contentDescription = if (isMinimized) "Expand" else "Minimize",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          FilledIconButton(
            onClick = onDisconnect,
            modifier = Modifier.size(36.dp).testTag("voice_disconnect_button"),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = StatusCallEndRed)
          ) {
            Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(17.dp))
          }
        }
      }

      // Collapsible Participant List and Call Controls
      AnimatedVisibility(
        visible = !isMinimized,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            items(voiceRoom.participants) { participant ->
              SpeakerAvatarPill(participant = participant)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Call Controls Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val isMuted = voiceRoom.isMuted
            IconButton(
              onClick = onToggleMute,
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isMuted) StatusCallEndRed else Color(0xFF2A3942))
            ) {
              Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            val isSpeaker = voiceRoom.isSpeakerOn
            IconButton(
              onClick = onToggleSpeaker,
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isSpeaker) WhatsAppGreenDark else Color(0xFF2A3942))
            ) {
              Icon(
                imageVector = if (isSpeaker) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                contentDescription = "Speaker",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            val isCam = voiceRoom.isCameraOn
            IconButton(
              onClick = onToggleCamera,
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isCam) WhatsAppGreenDark else Color(0xFF2A3942))
            ) {
              Icon(
                imageVector = if (isCam) Icons.Default.Videocam else Icons.Default.VideocamOff,
                contentDescription = "Camera",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            val isDeaf = voiceRoom.isDeafened
            IconButton(
              onClick = onToggleDeafen,
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isDeaf) StatusCallEndRed else Color(0xFF2A3942))
            ) {
              Icon(
                imageVector = if (isDeaf) Icons.Default.VolumeOff else Icons.Default.Headphones,
                contentDescription = "Deafen",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun SpeakerAvatarPill(participant: VoiceParticipant) {
  val isSpeaking = participant.isSpeaking
  val isAdmin = participant.isAdmin || participant.name.contains("Siang5680", ignoreCase = true)

  val ringBorderColor by animateColorAsState(
    targetValue = when {
      isSpeaking -> Color(0xFF25D366)
      isAdmin -> RoleAdminGold
      else -> Color.Transparent
    },
    label = "ring_border"
  )

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xFF2A3942),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isSpeaking) 2.dp else if (isAdmin) 1.dp else 0.dp,
      color = ringBorderColor
    )
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(26.dp)
          .clip(CircleShape)
          .background(if (isAdmin) RoleAdminGold.copy(alpha = 0.3f) else WhatsAppGreenDark),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = participant.name.take(1).uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = if (isAdmin) RoleAdminGold else Color.White,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = participant.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isAdmin) RoleAdminGold else Color.White,
            fontWeight = if (isSpeaking || isAdmin) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
          )

          if (isAdmin) {
            Spacer(modifier = Modifier.width(3.dp))
            Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(10.dp))
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          modifier = Modifier.height(6.dp)
        ) {
          val baseLevel = if (participant.isMuted) 0f else participant.audioLevel
          for (i in 0..3) {
            val barHeight = when (i) {
              0 -> (3 + baseLevel * 4).dp
              1 -> (4 + baseLevel * 6).dp
              2 -> (3 + baseLevel * 5).dp
              else -> (4 + baseLevel * 3).dp
            }
            Box(
              modifier = Modifier
                .width(2.dp)
                .height(barHeight)
                .clip(CircleShape)
                .background(if (participant.isMuted) StatusCallEndRed else if (isSpeaking) Color(0xFF25D366) else Color.Gray.copy(alpha = 0.4f))
            )
          }
        }
      }
    }
  }
}
