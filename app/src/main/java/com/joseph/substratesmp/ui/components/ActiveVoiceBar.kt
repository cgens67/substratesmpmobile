package com.joseph.substratesmp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
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
import com.joseph.substratesmp.ui.theme.ActiveSpeakerShape
import com.joseph.substratesmp.ui.theme.SubstrateTheme
import com.joseph.substratesmp.ui.theme.VoiceOverlayCardShape

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
    initialValue = 0.4f,
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
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .testTag("active_voice_bar"),
    shape = VoiceOverlayCardShape,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    border = CardDefaults.outlinedCardBorder().copy(
      width = 1.dp,
      brush = androidx.compose.ui.graphics.SolidColor(
        SubstrateTheme.customColors.statusVoiceActive.copy(alpha = 0.5f)
      )
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      // 1. Voice Channel Header & Agora Status
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
              .background(
                SubstrateTheme.customColors.statusVoiceActive.copy(alpha = pulseAlpha)
              )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "🔊 ${voiceRoom.channelName}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (voiceRoom.isConnecting) "Connecting to Agora RTC..." else "Agora.io RTC • Low-Latency Voice Engine",
              style = MaterialTheme.typography.labelSmall,
              color = SubstrateTheme.customColors.statusVoiceActive,
              fontSize = 10.sp
            )
          }
        }

        // Disconnect Call Button
        FilledIconButton(
          onClick = onDisconnect,
          modifier = Modifier
            .size(36.dp)
            .testTag("voice_disconnect_button"),
          colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = SubstrateTheme.customColors.statusMuted,
            contentColor = Color.White
          )
        ) {
          Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "Disconnect from voice",
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Active Speakers Row with Dynamic Voice Visualizers
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        items(voiceRoom.participants) { participant ->
          SpeakerAvatarPill(participant = participant)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Agora Audio & Video Controls (Mute, Speakerphone, Deafen, Camera)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Mute / Unmute Button (muteLocalAudioStream)
        val isMuted = voiceRoom.isMuted
        val micBg by animateColorAsState(
          targetValue = if (isMuted) SubstrateTheme.customColors.statusMuted.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
          label = "mic_bg"
        )
        val micTint by animateColorAsState(
          targetValue = if (isMuted) SubstrateTheme.customColors.statusMuted else MaterialTheme.colorScheme.onSurface,
          label = "mic_tint"
        )

        Surface(
          shape = MaterialTheme.shapes.small,
          color = micBg,
          modifier = Modifier.testTag("voice_mute_toggle")
        ) {
          IconButton(
            onClick = onToggleMute,
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
              contentDescription = if (isMuted) "Unmute" else "Mute",
              tint = micTint,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Speakerphone Toggle (setEnableSpeakerphone)
        val isSpeaker = voiceRoom.isSpeakerOn
        val speakerBg by animateColorAsState(
          targetValue = if (isSpeaker) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
          label = "speaker_bg"
        )
        val speakerTint by animateColorAsState(
          targetValue = if (isSpeaker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          label = "speaker_tint"
        )

        Surface(
          shape = MaterialTheme.shapes.small,
          color = speakerBg,
          modifier = Modifier.testTag("voice_speaker_toggle")
        ) {
          IconButton(
            onClick = onToggleSpeaker,
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              imageVector = if (isSpeaker) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
              contentDescription = if (isSpeaker) "Speakerphone On" else "Earpiece Mode",
              tint = speakerTint,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Deafen / Undeafen Button (muteAllRemoteAudioStreams)
        val isDeaf = voiceRoom.isDeafened
        val deafBg by animateColorAsState(
          targetValue = if (isDeaf) SubstrateTheme.customColors.statusMuted.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
          label = "deaf_bg"
        )
        val deafTint by animateColorAsState(
          targetValue = if (isDeaf) SubstrateTheme.customColors.statusMuted else MaterialTheme.colorScheme.onSurface,
          label = "deaf_tint"
        )

        Surface(
          shape = MaterialTheme.shapes.small,
          color = deafBg,
          modifier = Modifier.testTag("voice_deafen_toggle")
        ) {
          IconButton(
            onClick = onToggleDeafen,
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              imageVector = if (isDeaf) Icons.Default.VolumeOff else Icons.Default.Headphones,
              contentDescription = if (isDeaf) "Undeafen" else "Deafen",
              tint = deafTint,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Camera Toggle Button
        val isCam = voiceRoom.isCameraOn
        val camBg by animateColorAsState(
          targetValue = if (isCam) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
          label = "cam_bg"
        )

        Surface(
          shape = MaterialTheme.shapes.small,
          color = camBg,
          modifier = Modifier.testTag("voice_camera_toggle")
        ) {
          IconButton(
            onClick = onToggleCamera,
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              imageVector = if (isCam) Icons.Default.Videocam else Icons.Default.VideocamOff,
              contentDescription = if (isCam) "Turn Camera Off" else "Turn Camera On",
              tint = if (isCam) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun SpeakerAvatarPill(participant: VoiceParticipant) {
  val isSpeaking = participant.isSpeaking
  val borderColor by animateColorAsState(
    targetValue = if (isSpeaking) SubstrateTheme.customColors.statusVoiceActive else Color.Transparent,
    label = "border_color"
  )

  Surface(
    shape = ActiveSpeakerShape,
    color = MaterialTheme.colorScheme.surfaceContainer,
    border = androidx.compose.foundation.BorderStroke(
      width = if (isSpeaking) 2.dp else 1.dp,
      color = if (isSpeaking) borderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(26.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = participant.name.take(1).uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      Column {
        Text(
          text = participant.name,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = if (isSpeaking) FontWeight.Bold else FontWeight.Normal,
          maxLines = 1
        )

        // Dynamic Waveform Audio Level Bars
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          modifier = Modifier.height(10.dp)
        ) {
          val baseLevel = if (participant.isMuted) 0f else participant.audioLevel
          for (i in 0..3) {
            val barHeight = when (i) {
              0 -> (4 + baseLevel * 6).dp
              1 -> (6 + baseLevel * 8).dp
              2 -> (3 + baseLevel * 7).dp
              else -> (5 + baseLevel * 5).dp
            }
            Box(
              modifier = Modifier
                .width(2.dp)
                .height(barHeight)
                .clip(CircleShape)
                .background(
                  if (participant.isMuted) SubstrateTheme.customColors.statusMuted
                  else if (isSpeaking) SubstrateTheme.customColors.statusVoiceActive
                  else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            )
          }
        }
      }
    }
  }
}
