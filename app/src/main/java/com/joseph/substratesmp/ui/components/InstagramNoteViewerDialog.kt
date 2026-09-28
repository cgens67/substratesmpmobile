package com.joseph.substratesmp.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.joseph.substratesmp.data.model.StatusUpdate
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import kotlinx.coroutines.delay

@Composable
fun InstagramNoteViewerDialog(
  note: StatusUpdate,
  isOwnNote: Boolean,
  isDarkMode: Boolean,
  onDismiss: () -> Unit,
  onDeleteNote: (String) -> Unit,
  onReplyNote: (recipientTag: String, messageText: String) -> Unit,
  onEditNote: () -> Unit = {}
) {
  var replyText by remember { mutableStateOf("") }
  var isPlaying by remember { mutableStateOf(false) }
  var currentPosMs by remember { mutableIntStateOf(0) }
  val maxDurationMs = 30_000
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

  val cardBg = if (isDarkMode) Color(0xFF1E2024) else Color.White
  val bubbleBg = if (isDarkMode) Color(0xFF2C2F36) else Color(0xFFF2F4F7)
  val textColor = if (isDarkMode) Color.White else Color(0xFF111B21)
  val subTextColor = if (isDarkMode) Color(0xFFA0AAB5) else Color(0xFF8E9297)
  val blueAccent = Color(0xFF007AFF)

  // Rotating disc animation for music
  val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )

  // Play audio starting from the exact section selected by the author
  LaunchedEffect(note.musicPreviewUrl) {
    if (!note.musicPreviewUrl.isNullOrBlank()) {
      try {
        mediaPlayer = MediaPlayer().apply {
          setDataSource(note.musicPreviewUrl)
          isLooping = true
          prepareAsync()
          setOnPreparedListener {
            if (note.musicStartTimeMs > 0) {
              it.seekTo(note.musicStartTimeMs)
            }
            it.start()
            isPlaying = true
          }
        }
      } catch (_: Exception) {}
    }
  }

  LaunchedEffect(isPlaying) {
    while (isPlaying) {
      mediaPlayer?.let { currentPosMs = it.currentPosition.coerceIn(0, maxDurationMs) }
      delay(100L)
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      mediaPlayer?.stop()
      mediaPlayer?.release()
      mediaPlayer = null
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = cardBg,
      modifier = Modifier
        .fillMaxWidth(0.90f)
        .padding(vertical = 20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = subTextColor)
          }

          if (isOwnNote) {
            IconButton(
              onClick = {
                onDeleteNote(note.id)
                onDismiss()
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = Color(0xFFEA0038))
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Thought Bubble
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = bubbleBg,
          modifier = Modifier.fillMaxWidth(0.92f)
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            if (note.content.isNotBlank()) {
              Text(
                text = note.content,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
              )
            } else {
              Text(
                text = "🎵 Shared a soundtrack",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = blueAccent
              )
            }
          }
        }

        // Thought bubble pointer dots
        Box(
          modifier = Modifier
            .padding(top = 4.dp)
            .size(10.dp)
            .clip(CircleShape)
            .background(bubbleBg)
        )
        Box(
          modifier = Modifier
            .padding(top = 2.dp)
            .size(6.dp)
            .clip(CircleShape)
            .background(bubbleBg)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Avatar
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(if (note.isAdmin) RoleAdminGold else if (isDarkMode) Color(0xFF383838) else Color(0xFFE5E7EB))
            .border(2.dp, if (note.isAdmin) RoleAdminGold else blueAccent, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = note.authorGamertag.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = note.authorGamertag,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
          )
          if (note.isAdmin) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(14.dp))
          }
        }

        // 30-Second Music Preview Card
        if (!note.musicTrackName.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(14.dp))

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkMode) Color(0xFF141618) else Color(0xFFF2F4F7),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .then(if (isPlaying) Modifier.rotate(rotationAngle) else Modifier),
                  contentAlignment = Alignment.Center
                ) {
                  AsyncImage(
                    model = note.musicArtworkUrl ?: "",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = note.musicTrackName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1
                  )
                  Text(
                    text = "${note.musicArtistName ?: "Artist"} • Playing from ${note.musicStartTimeMs / 1000}s",
                    fontSize = 12.sp,
                    color = subTextColor,
                    maxLines = 1
                  )
                }

                IconButton(
                  onClick = {
                    if (isPlaying) {
                      mediaPlayer?.pause()
                      isPlaying = false
                    } else {
                      if (note.musicStartTimeMs > 0 && currentPosMs < note.musicStartTimeMs) {
                        mediaPlayer?.seekTo(note.musicStartTimeMs)
                      }
                      mediaPlayer?.start()
                      isPlaying = true
                    }
                  },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(
                    imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = blueAccent,
                    modifier = Modifier.size(32.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              LinearProgressIndicator(
                progress = { (currentPosMs.toFloat() / maxDurationMs.toFloat()).coerceIn(0f, 1f) },
                color = blueAccent,
                trackColor = subTextColor.copy(alpha = 0.2f),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp))
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Reply or Manage buttons
        if (!isOwnNote) {
          Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDarkMode) Color(0xFF2C2F36) else Color(0xFFF2F4F7),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              BasicTextField(
                value = replyText,
                onValueChange = { replyText = it },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontSize = 14.sp),
                singleLine = true,
                decorationBox = { innerTextField ->
                  if (replyText.isEmpty()) {
                    Text("Reply to ${note.authorGamertag}...", color = subTextColor, fontSize = 14.sp)
                  }
                  innerTextField()
                }
              )

              if (replyText.isNotBlank()) {
                IconButton(
                  onClick = {
                    onReplyNote(note.authorGamertag, replyText.trim())
                    onDismiss()
                  },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = blueAccent, modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        } else {
          Button(
            onClick = {
              onDismiss()
              onEditNote()
            },
            colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
          ) {
            Text("Share a new note", fontWeight = FontWeight.SemiBold, color = Color.White)
          }
        }
      }
    }
  }
}
