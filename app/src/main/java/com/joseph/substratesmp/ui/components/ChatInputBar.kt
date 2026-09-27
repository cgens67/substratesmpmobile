package com.joseph.substratesmp.ui.components

import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle
import com.joseph.substratesmp.ui.theme.StatusCallEndRed
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppGreenTeal
import com.joseph.substratesmp.ui.theme.WhatsAppTextPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun ChatInputBar(
  channelName: String,
  text: String,
  onTextChanged: (String) -> Unit,
  onSendMessage: (content: String, coordinates: String?, imageUrl: String?, audioUrl: String?, audioDuration: Int, replyTo: ChatMessage?) -> Unit,
  replyingTo: ChatMessage? = null,
  onCancelReply: () -> Unit = {},
  isEmojiPickerVisible: Boolean,
  onToggleEmojiPicker: () -> Unit,
  onTextFieldFocused: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showAttachmentMenu by remember { mutableStateOf(false) }
  var showCoordinateInput by remember { mutableStateOf(false) }
  var coordText by remember { mutableStateOf("") }

  // Voice recording state
  var isRecording by remember { mutableStateOf(false) }
  var recordingSeconds by remember { mutableIntStateOf(0) }
  var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
  var recordedAudioFile by remember { mutableStateOf<File?>(null) }

  // Gallery Picker
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          val bytes = stream.readBytes()
          val base64Img = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
          onSendMessage("", null, base64Img, null, 0, replyingTo)
        }
      } catch (_: Exception) {}
    }
  }

  // Camera Capture
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      val baos = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
      val base64Img = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
      onSendMessage("", null, base64Img, null, 0, replyingTo)
    }
  }

  LaunchedEffect(isRecording) {
    if (isRecording) {
      recordingSeconds = 0
      while (isRecording) {
        delay(1000L)
        recordingSeconds++
      }
    }
  }

  Surface(
    modifier = modifier.fillMaxWidth().testTag("chat_input_bar"),
    color = Color.Transparent
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)) {
      // Quoted Reply Banner
      if (replyingTo != null) {
        Surface(
          shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
          color = Color(0xFFF0F2F5),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.width(3.dp).height(32.dp).background(WhatsAppGreenDark, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text("Replying to ${replyingTo.senderName}", fontWeight = FontWeight.Bold, color = WhatsAppGreenDark, fontSize = 12.sp)
              Text(replyingTo.content.ifBlank { if (replyingTo.imageUrl != null) "📷 Photo" else "🎤 Voice note" }, color = WhatsAppTextSecondary, fontSize = 11.5.sp, maxLines = 1)
            }
            IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Cancel", tint = WhatsAppTextSecondary, modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      // Coordinates input bar
      AnimatedVisibility(visible = showCoordinateInput, enter = fadeIn(), exit = fadeOut()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Place, contentDescription = null, tint = WhatsAppGreenDark, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          OutlinedTextField(
            value = coordText,
            onValueChange = { coordText = it },
            placeholder = { Text("Coordinates (e.g. X: -120, Y: 64, Z: 540)", style = CoordinateTextStyle, color = WhatsAppTextSecondary) },
            textStyle = CoordinateTextStyle,
            modifier = Modifier.weight(1f).height(46.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
          IconButton(onClick = { showCoordinateInput = false; coordText = "" }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = WhatsAppTextSecondary, modifier = Modifier.size(18.dp))
          }
        }
      }

      // Attachment Tray (Gallery, Camera, Coordinates)
      AnimatedVisibility(visible = showAttachmentMenu) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.White,
          shadowElevation = 3.dp,
          modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
          Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
              showAttachmentMenu = false
              galleryLauncher.launch("image/*")
            }) {
              Surface(shape = CircleShape, color = Color(0xFFE91E63), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Image, contentDescription = "Gallery", tint = Color.White)
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Gallery", fontSize = 12.sp, color = WhatsAppTextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
              showAttachmentMenu = false
              cameraLauncher.launch(null)
            }) {
              Surface(shape = CircleShape, color = Color(0xFF00A884), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White)
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Camera", fontSize = 12.sp, color = WhatsAppTextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
              showAttachmentMenu = false
              showCoordinateInput = true
            }) {
              Surface(shape = CircleShape, color = Color(0xFF2196F3), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Place, contentDescription = "Coordinates", tint = Color.White)
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Location", fontSize = 12.sp, color = WhatsAppTextPrimary)
            }
          }
        }
      }

      // WhatsApp Chat Input Capsule & FAB
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(26.dp),
          color = Color.White,
          shadowElevation = 1.dp
        ) {
          if (isRecording) {
            // Live voice note recording UI
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StatusCallEndRed))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recording ${recordingSeconds}s", fontWeight = FontWeight.Bold, color = StatusCallEndRed, fontSize = 14.sp)
              }
              Text(
                "Cancel",
                color = WhatsAppTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                  try { recorder?.stop(); recorder?.release() } catch (_: Exception) {}
                  recorder = null
                  isRecording = false
                  recordedAudioFile?.delete()
                }
              )
            }
          } else {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(onClick = onToggleEmojiPicker, modifier = Modifier.size(34.dp)) {
                Icon(
                  imageVector = if (isEmojiPickerVisible) Icons.Default.Keyboard else Icons.Default.SentimentSatisfied,
                  contentDescription = "Emoji",
                  tint = if (isEmojiPickerVisible) WhatsAppGreenDark else WhatsAppTextSecondary,
                  modifier = Modifier.size(24.dp)
                )
              }

              OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier
                  .weight(1f)
                  .onFocusChanged { if (it.isFocused) onTextFieldFocused() }
                  .testTag("chat_text_input"),
                placeholder = { Text("Message", color = WhatsAppTextSecondary, fontSize = 15.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color.Transparent,
                  unfocusedContainerColor = Color.Transparent,
                  focusedBorderColor = Color.Transparent,
                  unfocusedBorderColor = Color.Transparent,
                  focusedTextColor = WhatsAppTextPrimary,
                  unfocusedTextColor = WhatsAppTextPrimary
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                  onSend = {
                    if (text.isNotBlank() || coordText.isNotBlank()) {
                      val coords = if (coordText.isNotBlank()) coordText.trim() else null
                      onSendMessage(text.trim(), coords, null, null, 0, replyingTo)
                      coordText = ""
                      showCoordinateInput = false
                    }
                  }
                ),
                maxLines = 4
              )

              IconButton(onClick = { showAttachmentMenu = !showAttachmentMenu }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = WhatsAppTextSecondary, modifier = Modifier.size(22.dp))
              }

              IconButton(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = WhatsAppTextSecondary, modifier = Modifier.size(22.dp))
              }
            }
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        val canSend = text.isNotBlank() || coordText.isNotBlank()

        FloatingActionButton(
          onClick = {
            if (isRecording) {
              // Finish recording and send voice message
              try {
                recorder?.stop()
                recorder?.release()
                recorder = null
                isRecording = false
                val file = recordedAudioFile
                if (file != null && file.exists()) {
                  val bytes = file.readBytes()
                  val base64Aud = "data:audio/mp4;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                  onSendMessage("", null, null, base64Aud, recordingSeconds, replyingTo)
                  file.delete()
                }
              } catch (_: Exception) {}
            } else if (canSend) {
              val coords = if (coordText.isNotBlank()) coordText.trim() else null
              onSendMessage(text.trim(), coords, null, null, 0, replyingTo)
              coordText = ""
              showCoordinateInput = false
            } else {
              // Start voice recording
              try {
                val tempAudio = File(context.cacheDir, "rec_${System.currentTimeMillis()}.m4a")
                recordedAudioFile = tempAudio
                recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                  MediaRecorder(context)
                } else {
                  @Suppress("DEPRECATION")
                  MediaRecorder()
                }.apply {
                  setAudioSource(MediaRecorder.AudioSource.MIC)
                  setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                  setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                  setOutputFile(tempAudio.absolutePath)
                  prepare()
                  start()
                }
                isRecording = true
              } catch (_: Exception) {
                isRecording = false
              }
            }
          },
          modifier = Modifier.size(48.dp).testTag("chat_send_button"),
          shape = CircleShape,
          containerColor = if (isRecording) StatusCallEndRed else WhatsAppGreenTeal,
          contentColor = Color.White,
          elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
          Icon(
            imageVector = if (isRecording) Icons.Default.Stop else if (canSend) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
            contentDescription = "Send",
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }
  }
}
