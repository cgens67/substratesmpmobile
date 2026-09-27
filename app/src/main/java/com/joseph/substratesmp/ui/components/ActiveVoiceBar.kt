package com.joseph.substratesmp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.joseph.substratesmp.ui.theme.SubstrateTheme
import com.joseph.substratesmp.ui.theme.WhatsAppChatOutgoing
import com.joseph.substratesmp.ui.theme.WhatsAppSurfaceHigh

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
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag("active_voice_bar"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = WhatsAppSurfaceHigh
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
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
              .background(SubstrateTheme.customColors.statusOnline.copy(alpha = pulseAlpha))
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
              text = if (voiceRoom.isConnecting) "Calling..." else "${voiceRoom.participants.size} participant(s) in call",
              style = MaterialTheme.typography.labelSmall,
              color = SubstrateTheme.customColors.statusOnline,
              fontSize = 11.sp
            )
          }
        }

        FilledIconButton(
          onClick = onDisconnect,
          modifier = Modifier
            .size(40.dp)
            .testTag("voice_disconnect_button"),
          colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = StatusCallEndRed,
            contentColor = Color.White
          )
        ) {
          Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        items(voiceRoom.participants) { participant ->
          SpeakerAvatarPill(participant = participant)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

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
            .background(if (isMuted) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
            .testTag("voice_mute_toggle")
        ) {
          Icon(
            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = if (isMuted) "Unmute" else "Mute",
            tint = if (isMuted) StatusCallEndRed else Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        val isSpeaker = voiceRoom.isSpeakerOn
        IconButton(
          onClick = onToggleSpeaker,
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isSpeaker) WhatsAppChatOutgoing else MaterialTheme.colorScheme.surfaceVariant)
            .testTag("voice_speaker_toggle")
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
            .background(if (isCam) WhatsAppChatOutgoing else MaterialTheme.colorScheme.surfaceVariant)
            .testTag("voice_camera_toggle")
        ) {
          Icon(
            imageVector = if (isCam) Icons.Default.Videocam else Icons.Default.VideocamOff,
            contentDescription = "Video",
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
            .background(if (isDeaf) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
            .testTag("voice_deafen_toggle")
        ) {
          Icon(
            imageVector = if (isDeaf) Icons.Default.VolumeOff else Icons.Default.Headphones,
            contentDescription = "Deafen",
            tint = if (isDeaf) StatusCallEndRed else Color.White,
            modifier = Modifier.size(20.dp)
          )
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
      isSpeaking -> SubstrateTheme.customColors.statusOnline
      isAdmin -> RoleAdminGold
      else -> Color.Transparent
    },
    label = "ring_border"
  )

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = androidx.compose.foundation.BorderStroke(
      width = if (isSpeaking) 2.dp else if (isAdmin) 1.dp else 0.dp,
      color = ringBorderColor
    )
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(
            if (isAdmin) RoleAdminGold.copy(alpha = 0.25f)
            else WhatsAppChatOutgoing
          ),
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
            style = MaterialTheme.typography.labelMedium,
            color = if (isAdmin) RoleAdminGold else Color.White,
            fontWeight = if (isSpeaking || isAdmin) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
          )

          if (isAdmin) {
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = RoleAdminGold.copy(alpha = 0.2f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = null,
                  tint = RoleAdminGold,
                  modifier = Modifier.size(9.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = "ADMIN",
                  style = MaterialTheme.typography.labelSmall,
                  color = RoleAdminGold,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 8.sp
                )
              }
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          modifier = Modifier.height(8.dp)
        ) {
          val baseLevel = if (participant.isMuted) 0f else participant.audioLevel
          for (i in 0..3) {
            val barHeight = when (i) {
              0 -> (3 + baseLevel * 5).dp
              1 -> (5 + baseLevel * 7).dp
              2 -> (3 + baseLevel * 6).dp
              else -> (4 + baseLevel * 4).dp
            }
            Box(
              modifier = Modifier
                .width(2.dp)
                .height(barHeight)
                .clip(CircleShape)
                .background(
                  if (participant.isMuted) StatusCallEndRed
                  else if (isSpeaking) SubstrateTheme.customColors.statusOnline
                  else Color.Gray.copy(alpha = 0.4f)
                )
            )
          }
        }
      }
    }
  }
}
