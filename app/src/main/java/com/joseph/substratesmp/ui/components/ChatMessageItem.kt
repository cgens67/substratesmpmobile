package com.joseph.substratesmp.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.RoleAdminGoldContainer
import com.joseph.substratesmp.ui.theme.WhatsAppCheckmarkBlue
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppIncomingBubble
import com.joseph.substratesmp.ui.theme.WhatsAppOutgoingBubble
import com.joseph.substratesmp.ui.theme.WhatsAppTextPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@Composable
fun ChatMessageItem(
  message: ChatMessage,
  onReply: (ChatMessage) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val scope = rememberCoroutineScope()
  val isLocal = message.isLocalUser
  val isSenderAdmin = message.isAdmin

  // Voice playback state
  var isPlayingAudio by remember { mutableStateOf(false) }
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

  DisposableEffect(Unit) {
    onDispose {
      mediaPlayer?.release()
      mediaPlayer = null
    }
  }

  // Swipe-to-Reply Drag State
  val offsetX = remember { Animatable(0f) }

  val bubbleShape = if (isLocal) {
    RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomEnd = 14.dp, bottomStart = 14.dp)
  } else {
    RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp)
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 2.dp)
  ) {
    // Reveal reply icon on drag
    if (offsetX.value > 15f) {
      Box(
        modifier = Modifier
          .align(Alignment.CenterStart)
          .padding(start = 12.dp)
          .size(32.dp)
          .clip(CircleShape)
          .background(Color(0xFFE9EDEF)),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Reply", tint = WhatsAppGreenDark, modifier = Modifier.size(18.dp))
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .pointerInput(message.id) {
          detectHorizontalDragGestures(
            onHorizontalDrag = { _, dragAmount ->
              val newOffset = (offsetX.value + dragAmount).coerceIn(0f, 90f)
              scope.launch { offsetX.snapTo(newOffset) }
            },
            onDragEnd = {
              if (offsetX.value > 45f) {
                onReply(message)
              }
              scope.launch { offsetX.animateTo(0f, spring()) }
            },
            onDragCancel = {
              scope.launch { offsetX.animateTo(0f, spring()) }
            }
          )
        },
      horizontalArrangement = if (isLocal) Arrangement.End else Arrangement.Start,
      verticalAlignment = Alignment.Top
    ) {
      Card(
        shape = bubbleShape,
        colors = CardDefaults.cardColors(
          containerColor = if (isLocal) WhatsAppOutgoingBubble else WhatsAppIncomingBubble
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.widthIn(min = 90.dp, max = 320.dp)
      ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
          // Sender Header for incoming messages
          if (!isLocal) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
              Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSenderAdmin) RoleAdminGold else WhatsAppGreenDark,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
              )
              if (isSenderAdmin) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(shape = RoundedCornerShape(4.dp), color = RoleAdminGoldContainer) {
                  Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(9.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("ADMIN", color = RoleAdminGold, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                  }
                }
              }
            }
          }

          // Quoted Reply preview inside message
          if (!message.replyToSender.isNullOrBlank()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color.Black.copy(alpha = 0.06f),
              modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
              Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(28.dp).background(WhatsAppGreenDark, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(message.replyToSender, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WhatsAppGreenDark)
                  Text(message.replyToContent ?: "", fontSize = 11.sp, color = WhatsAppTextSecondary, maxLines = 1)
                }
              }
            }
          }

          // Attached Photo View
          if (!message.imageUrl.isNullOrBlank()) {
            AsyncImage(
              model = message.imageUrl,
              contentDescription = "Attached Photo",
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(bottom = 4.dp),
              contentScale = ContentScale.Crop
            )
          }

          // Voice Note Player View
          if (!message.audioUrl.isNullOrBlank()) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.Black.copy(alpha = 0.05f),
              modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = {
                    if (isPlayingAudio) {
                      mediaPlayer?.pause()
                      isPlayingAudio = false
                    } else {
                      try {
                        if (mediaPlayer == null) {
                          val rawBytes = android.util.Base64.decode(
                            message.audioUrl.substringAfter("base64,"),
                            android.util.Base64.NO_WRAP
                          )
                          val tempFile = File(context.cacheDir, "audio_${message.id}.m4a")
                          FileOutputStream(tempFile).use { it.write(rawBytes) }
                          mediaPlayer = MediaPlayer().apply {
                            setDataSource(tempFile.absolutePath)
                            prepare()
                            setOnCompletionListener { isPlayingAudio = false }
                          }
                        }
                        mediaPlayer?.start()
                        isPlayingAudio = true
                      } catch (_: Exception) {
                        isPlayingAudio = false
                      }
                    }
                  },
                  modifier = Modifier.size(34.dp).clip(CircleShape).background(WhatsAppGreenDark)
                ) {
                  Icon(
                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text("Voice message", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WhatsAppTextPrimary)
                  Text("${message.audioDurationSeconds}s", fontSize = 10.sp, color = WhatsAppTextSecondary)
                }
              }
            }
          }

          // Message content
          if (message.content.isNotBlank()) {
            Text(
              text = message.content,
              style = MaterialTheme.typography.bodyMedium,
              color = WhatsAppTextPrimary,
              lineHeight = 18.sp,
              fontSize = 14.5.sp
            )
          }

          // Coordinates Attachment Tag
          if (!message.coordinates.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isLocal) Color.White.copy(alpha = 0.6f) else Color(0xFFF0F2F5),
              modifier = Modifier.clickable { clipboardManager.setText(AnnotatedString(message.coordinates)) }
            ) {
              Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = WhatsAppGreenDark, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(message.coordinates, style = CoordinateTextStyle, color = WhatsAppGreenDark, fontSize = 11.5.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = WhatsAppTextSecondary, modifier = Modifier.size(12.dp))
              }
            }
          }

          // Timestamp & Delivery Checkmarks
          Row(
            modifier = Modifier.align(Alignment.End).padding(top = 1.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(message.formattedTime, style = MaterialTheme.typography.labelSmall, color = WhatsAppTextSecondary, fontSize = 10.sp)
            if (isLocal) {
              Spacer(modifier = Modifier.width(3.dp))
              Icon(Icons.Default.DoneAll, contentDescription = "Sent", tint = WhatsAppCheckmarkBlue, modifier = Modifier.size(14.dp))
            }
          }
        }
      }
    }
  }
}
