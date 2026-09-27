package com.joseph.substratesmp.ui.components

import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@Composable
fun ChatMessageItem(
  message: ChatMessage,
  isReadByAll: Boolean = false,
  canDelete: Boolean = false,
  onDeleteMessage: (ChatMessage) -> Unit = {},
  onReply: (ChatMessage) -> Unit = {},
  onImageClick: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val scope = rememberCoroutineScope()
  val isLocal = message.isLocalUser
  val isSenderAdmin = message.isAdmin

  var isMessageVisible by remember { mutableStateOf(true) }
  var showOptionsDialog by remember { mutableStateOf(false) }

  var isPlayingAudio by remember { mutableStateOf(false) }
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

  DisposableEffect(Unit) {
    onDispose {
      mediaPlayer?.release()
      mediaPlayer = null
    }
  }

  // Coil ImageLoader with GIF and animated WebP support
  val imageLoader = remember {
    ImageLoader.Builder(context)
      .components {
        if (Build.VERSION.SDK_INT >= 28) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
      }
      .build()
  }

  // Parse direct Base64 ByteArray so Coil can animate it natively
  val imageBytes = remember(message.imageUrl) {
    try {
      if (!message.imageUrl.isNullOrBlank() && message.imageUrl.startsWith("data:")) {
        Base64.decode(message.imageUrl.substringAfter("base64,"), Base64.NO_WRAP)
      } else null
    } catch (_: Exception) { null }
  }

  // Detect GIF/WebP URL in text content
  val gifUrl = remember(message.content) {
    val regex = "(?i)https?://\\S+\\.(gif|webp)\\b".toRegex()
    regex.find(message.content)?.value
  }

  val offsetX = remember { Animatable(0f) }

  val bubbleShape = if (isLocal) {
    RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomEnd = 14.dp, bottomStart = 14.dp)
  } else {
    RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp)
  }

  AnimatedVisibility(
    visible = isMessageVisible,
    exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(200))
  ) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 2.dp)
        .animateContentSize()
    ) {
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
                if (offsetX.value > 45f) onReply(message)
                scope.launch { offsetX.animateTo(0f, spring(stiffness = 500f)) }
              },
              onDragCancel = {
                scope.launch { offsetX.animateTo(0f, spring()) }
              }
            )
          },
        horizontalArrangement = if (isLocal) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
      ) {
        if (message.isSticker && (imageBytes != null || !message.imageUrl.isNullOrBlank())) {
          // STICKER BUBBLE (Transparent, No Background)
          Column(
            horizontalAlignment = if (isLocal) Alignment.End else Alignment.Start,
            modifier = Modifier.combinedClickable(
              onClick = { onImageClick(message.imageUrl!!) },
              onLongClick = { showOptionsDialog = true }
            )
          ) {
            if (!isLocal) {
              Text(message.senderName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSenderAdmin) RoleAdminGold else WhatsAppGreenDark)
            }
            AsyncImage(
              model = imageBytes ?: message.imageUrl,
              imageLoader = imageLoader,
              contentDescription = "Sticker",
              modifier = Modifier.size(125.dp),
              contentScale = ContentScale.Fit
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(message.formattedTime, fontSize = 9.5.sp, color = WhatsAppTextSecondary)
              if (isLocal) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(Icons.Default.DoneAll, contentDescription = "Sent", tint = if (isReadByAll) WhatsAppCheckmarkBlue else Color.Gray, modifier = Modifier.size(12.dp))
              }
            }
          }
        } else {
          // REGULAR CHAT BUBBLE
          Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = if (isLocal) WhatsAppOutgoingBubble else WhatsAppIncomingBubble),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .widthIn(min = 90.dp, max = 320.dp)
              .combinedClickable(
                onClick = {},
                onLongClick = { showOptionsDialog = true }
              )
          ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
              if (!isLocal) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
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

              // Replying Preview
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

              // Image Attachment
              if (imageBytes != null || (!message.imageUrl.isNullOrBlank() && !message.imageUrl.startsWith("data:"))) {
                AsyncImage(
                  model = imageBytes ?: message.imageUrl,
                  imageLoader = imageLoader,
                  contentDescription = "Attached Photo",
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onImageClick(message.imageUrl!!) }
                    .padding(bottom = 4.dp),
                  contentScale = ContentScale.Crop
                )
              } else if (gifUrl != null) {
                AsyncImage(
                  model = gifUrl,
                  imageLoader = imageLoader,
                  contentDescription = "GIF",
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onImageClick(gifUrl) }
                    .padding(bottom = 4.dp),
                  contentScale = ContentScale.Crop
                )
              }

              // Document/File Attachment
              if (!message.fileUrl.isNullOrBlank()) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color.Black.copy(alpha = 0.05f),
                  modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).clickable {
                    try {
                      val bytes = Base64.decode(message.fileUrl.substringAfter("base64,"), Base64.NO_WRAP)
                      val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                      val file = File(downloadsDir, message.fileName ?: "document.file")
                      FileOutputStream(file).use { it.write(bytes) }
                      Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                      Toast.makeText(context, "Failed to download file", Toast.LENGTH_SHORT).show()
                    }
                  }
                ) {
                  Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(0xFF7E57C2), modifier = Modifier.size(34.dp)) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = "File", tint = Color.White, modifier = Modifier.size(18.dp))
                      }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(message.fileName ?: "Document", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WhatsAppTextPrimary, maxLines = 1)
                      Text("Tap to download", fontSize = 10.sp, color = WhatsAppTextSecondary)
                    }
                  }
                }
              }

              // Audio / Voice Message Attachment
              if (!message.audioUrl.isNullOrBlank()) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color.Black.copy(alpha = 0.05f),
                  modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                  Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                      onClick = {
                        if (isPlayingAudio) {
                          mediaPlayer?.pause(); isPlayingAudio = false
                        } else {
                          try {
                            if (mediaPlayer == null) {
                              val rawBytes = Base64.decode(message.audioUrl.substringAfter("base64,"), Base64.NO_WRAP)
                              val tempFile = File(context.cacheDir, "audio_${message.id}.m4a")
                              FileOutputStream(tempFile).use { it.write(rawBytes) }
                              mediaPlayer = MediaPlayer().apply {
                                setDataSource(tempFile.absolutePath)
                                prepare()
                                setOnCompletionListener { isPlayingAudio = false }
                              }
                            }
                            mediaPlayer?.start(); isPlayingAudio = true
                          } catch (_: Exception) { isPlayingAudio = false }
                        }
                      },
                      modifier = Modifier.size(34.dp).clip(CircleShape).background(WhatsAppGreenDark)
                    ) {
                      Icon(if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text("Voice message (HD)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WhatsAppTextPrimary)
                      Text("${message.audioDurationSeconds}s", fontSize = 10.sp, color = WhatsAppTextSecondary)
                    }
                  }
                }
              }

              if (message.content.isNotBlank()) {
                Text(
                  text = message.content,
                  style = MaterialTheme.typography.bodyMedium,
                  color = WhatsAppTextPrimary,
                  lineHeight = 18.sp,
                  fontSize = 14.5.sp
                )
              }

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

              Row(modifier = Modifier.align(Alignment.End).padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(message.formattedTime, style = MaterialTheme.typography.labelSmall, color = WhatsAppTextSecondary, fontSize = 10.sp)
                if (isLocal) {
                  Spacer(modifier = Modifier.width(3.dp))
                  Icon(Icons.Default.DoneAll, contentDescription = "Sent", tint = if (isReadByAll) WhatsAppCheckmarkBlue else Color.Gray, modifier = Modifier.size(14.dp))
                }
              }
            }
          }
        }
      }
    }
  }

  // Long-press Options Dialog (Copy, Reply, Delete)
  if (showOptionsDialog) {
    AlertDialog(
      onDismissRequest = { showOptionsDialog = false },
      title = { Text("Message Options", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          TextButton(onClick = { onReply(message); showOptionsDialog = false }) {
            Text("Reply", fontSize = 16.sp)
          }
          if (message.content.isNotBlank() || !message.coordinates.isNullOrBlank()) {
            TextButton(onClick = {
              val copyText = if (message.content.isNotBlank()) message.content else message.coordinates ?: ""
              clipboardManager.setText(AnnotatedString(copyText))
              showOptionsDialog = false
            }) {
              Text("Copy Text", fontSize = 16.sp)
            }
          }
          if (canDelete) {
            TextButton(onClick = {
              scope.launch {
                showOptionsDialog = false
                isMessageVisible = false
                delay(250L) // Wait for smooth shrink/fade animation before deleting document
                onDeleteMessage(message)
              }
            }) {
              Text("Delete", color = Color(0xFFEA0038), fontSize = 16.sp)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showOptionsDialog = false }) { Text("Cancel") }
      }
    )
  }
}
