package com.joseph.substratesmp.ui.components

import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.joseph.substratesmp.data.model.ActiveVoiceRoom
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.StatusCallEndRed
import com.joseph.substratesmp.voice.AgoraVoiceManager

@Composable
fun VideoCallScreen(
  voiceRoom: ActiveVoiceRoom,
  voiceManager: AgoraVoiceManager,
  onDisconnect: () -> Unit,
  modifier: Modifier = Modifier
) {
  val remoteParticipant = voiceRoom.participants.firstOrNull { !it.isLocal }
  val remoteUid = remoteParticipant?.id?.removePrefix("remote_")?.toIntOrNull()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0F131C))
  ) {
    if (remoteUid != null) {
      AndroidView(
        factory = { ctx ->
          SurfaceView(ctx).apply {
            setZOrderMediaOverlay(false)
            voiceManager.setupRemoteVideoCanvas(this, remoteUid)
          }
        },
        modifier = Modifier.fillMaxSize()
      )
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color(0xFF1F2C34)),
          contentAlignment = Alignment.Center
        ) {
          Text("🔊", fontSize = 48.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(voiceRoom.channelName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Waiting for remote video...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
      }
    }

    AnimatedVisibility(
      visible = voiceRoom.isCameraOn,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .statusBarsPadding()
        .padding(16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(width = 110.dp, height = 160.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
          .background(Color.Black)
      ) {
        AndroidView(
          factory = { ctx ->
            SurfaceView(ctx).apply {
              setZOrderMediaOverlay(true)
              voiceManager.setupLocalVideoCanvas(this)
            }
          },
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(28.dp),
      color = Color.Black.copy(alpha = 0.75f),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { voiceManager.switchCamera() },
          modifier = Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
        ) {
          Icon(Icons.Default.Cameraswitch, contentDescription = "Flip Camera", tint = Color.White)
        }

        IconButton(
          onClick = { voiceManager.toggleCamera() },
          modifier = Modifier.size(46.dp).clip(CircleShape).background(if (voiceRoom.isCameraOn) Color.White.copy(alpha = 0.2f) else StatusCallEndRed)
        ) {
          Icon(if (voiceRoom.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff, contentDescription = "Camera", tint = Color.White)
        }

        IconButton(
          onClick = { voiceManager.toggleMute() },
          modifier = Modifier.size(46.dp).clip(CircleShape).background(if (voiceRoom.isMuted) StatusCallEndRed else Color.White.copy(alpha = 0.2f))
        ) {
          Icon(if (voiceRoom.isMuted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Mic", tint = Color.White)
        }

        IconButton(
          onClick = { voiceManager.toggleSpeaker() },
          modifier = Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
        ) {
          Icon(if (voiceRoom.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown, contentDescription = "Speaker", tint = Color.White)
        }

        FilledIconButton(
          onClick = onDisconnect,
          modifier = Modifier.size(50.dp),
          colors = IconButtonDefaults.filledIconButtonColors(containerColor = StatusCallEndRed)
        ) {
          Icon(Icons.Default.CallEnd, contentDescription = "Hang Up", tint = Color.White)
        }
      }
    }
  }
}
