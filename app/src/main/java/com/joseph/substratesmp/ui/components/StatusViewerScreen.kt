package com.joseph.substratesmp.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.joseph.substratesmp.data.model.StatusUpdate
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle
import com.joseph.substratesmp.ui.theme.RoleAdminGold

@Composable
fun StatusViewerScreen(
  status: StatusUpdate,
  isOwnStatus: Boolean,
  onDismiss: () -> Unit,
  onDelete: (() -> Unit)?,
  onReact: (String) -> Unit
) {
  val clipboardManager = LocalClipboardManager.current
  val progress = remember { Animatable(0f) }

  var isMuted by remember { mutableStateOf(false) }
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

  // Background Music Playback
  LaunchedEffect(status.id, status.musicPreviewUrl) {
    if (!status.musicPreviewUrl.isNullOrBlank()) {
      try {
        mediaPlayer = MediaPlayer().apply {
          setDataSource(status.musicPreviewUrl)
          isLooping = true
          prepareAsync()
          setOnPreparedListener { it.start() }
        }
      } catch (_: Exception) {}
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      mediaPlayer?.stop()
      mediaPlayer?.release()
      mediaPlayer = null
    }
  }

  val gradientBrush = when (status.backgroundTheme) {
    "CRIMSON" -> Brush.verticalGradient(listOf(Color(0xFF8B0000), Color(0xFFFF4500)))
    "END_VOID" -> Brush.verticalGradient(listOf(Color(0xFF2E0854), Color(0xFF6A0DAD)))
    "DIAMOND" -> Brush.verticalGradient(listOf(Color(0xFF005C8A), Color(0xFF00E5FF)))
    "GOLDEN" -> Brush.verticalGradient(listOf(Color(0xFF7A5800), Color(0xFFFFD700)))
    "OBSIDIAN" -> Brush.verticalGradient(listOf(Color(0xFF0F131C), Color(0xFF273142)))
    else -> Brush.verticalGradient(listOf(Color(0xFF004085), Color(0xFF007AFF))) // Sapphire Blue
  }

  LaunchedEffect(status.id) {
    progress.snapTo(0f)
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 8500, easing = LinearEasing)
    )
    onDismiss()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(gradientBrush)
      .statusBarsPadding()
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = { offset ->
            if (offset.x < size.width * 0.35f) {
              onDismiss()
            } else {
              onDismiss()
            }
          }
        )
      }
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      LinearProgressIndicator(
        progress = { progress.value },
        modifier = Modifier
          .fillMaxWidth()
          .height(3.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = Color.White,
        trackColor = Color.White.copy(alpha = 0.3f),
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = status.authorGamertag.take(1).uppercase(),
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = status.authorGamertag,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              if (status.isAdmin) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(14.dp))
              }
            }
            Text(
              text = "Bedrock SMP • ${status.activityTag}",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 12.sp
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (!status.musicPreviewUrl.isNullOrBlank()) {
            IconButton(onClick = {
              isMuted = !isMuted
              if (isMuted) mediaPlayer?.setVolume(0f, 0f) else mediaPlayer?.setVolume(1f, 1f)
            }) {
              Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = "Mute",
                tint = Color.White
              )
            }
          }
          if (isOwnStatus && onDelete != null) {
            IconButton(onClick = onDelete) {
              Icon(Icons.Default.Delete, contentDescription = "Delete Status", tint = Color.White)
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }
      }

      // Music soundtrack banner sticker
      if (!status.musicTrackName.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.Black.copy(alpha = 0.45f),
          modifier = Modifier.padding(horizontal = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (!status.musicArtworkUrl.isNullOrBlank()) {
              AsyncImage(
                model = status.musicArtworkUrl,
                contentDescription = null,
                modifier = Modifier.size(24.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.width(8.dp))
            } else {
              Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
              text = "${status.musicTrackName} • ${status.musicArtistName ?: ""}",
              color = Color.White,
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        }
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = status.content,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
          )

          if (!status.coordinates.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.Black.copy(alpha = 0.35f),
              modifier = Modifier.clickable {
                clipboardManager.setText(AnnotatedString(status.coordinates))
              }
            ) {
              Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(status.coordinates, style = CoordinateTextStyle, color = Color.White, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
              }
            }
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          listOf("❤️", "🔥", "⛏️", "💎", "👏").forEach { emoji ->
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .clickable { onReact(emoji) }
                .padding(8.dp)
            ) {
              Text(text = emoji, fontSize = 24.sp)
            }
          }
        }
      }
    }
  }
}
