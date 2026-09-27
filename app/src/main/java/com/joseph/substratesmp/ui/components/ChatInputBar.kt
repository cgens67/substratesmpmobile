package com.joseph.substratesmp.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

object ImgBbUploader {
  const val IMGBB_API_KEY = "0925de674792b45903b6bbcf57fbd976"

  private val client = OkHttpClient.Builder()
    .connectTimeout(25, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .writeTimeout(25, TimeUnit.SECONDS)
    .build()

  suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
    if (IMGBB_API_KEY.isBlank()) {
      return@withContext Result.failure(Exception("No ImgBB API key"))
    }
    try {
      val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
      val ext = when {
        mimeType.contains("gif") -> "gif"
        mimeType.contains("webp") -> "webp"
        mimeType.contains("png") -> "png"
        else -> "jpg"
      }

      val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
        ?: return@withContext Result.failure(Exception("Cannot open image stream"))

      val buffer = ByteArrayOutputStream()
      inputStream.use { input ->
        val temp = ByteArray(8192)
        var read: Int
        while (input.read(temp).also { read = it } != -1) {
          buffer.write(temp, 0, read)
        }
      }
      val imageBytes = buffer.toByteArray()

      val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
          "image",
          "upload_${System.currentTimeMillis()}.$ext",
          imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
        )
        .build()

      val request = Request.Builder()
        .url("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        return@withContext Result.failure(Exception("ImgBB HTTP error: ${response.code}"))
      }

      val json = JSONObject(responseString)
      if (json.getBoolean("success")) {
        val data = json.getJSONObject("data")
        val directUrl = data.getString("url")
        Result.success(directUrl)
      } else {
        Result.failure(Exception("ImgBB error: $responseString"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun uploadBitmap(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
    if (IMGBB_API_KEY.isBlank()) {
      return@withContext Result.failure(Exception("No ImgBB API key"))
    }
    try {
      val baos = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.JPEG, 92, baos)
      val imageBytes = baos.toByteArray()

      val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
          "image",
          "camera_${System.currentTimeMillis()}.jpg",
          imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        .build()

      val request = Request.Builder()
        .url("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string() ?: ""

      val json = JSONObject(responseString)
      if (json.getBoolean("success")) {
        val directUrl = json.getJSONObject("data").getString("url")
        Result.success(directUrl)
      } else {
        Result.failure(Exception("ImgBB error: $responseString"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}

fun processAndCompressImage(uri: Uri, context: Context): String? {
  return try {
    val mimeType = context.contentResolver.getType(uri) ?: ""
    if (mimeType.contains("gif") || mimeType.contains("webp")) {
      val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
      if (bytes.size > 2000 * 1024) {
        Toast.makeText(context, "GIF/WebP too large (Max 2MB)", Toast.LENGTH_SHORT).show()
        return null
      }
      return "data:$mimeType;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeStream(inputStream, null, options)
    inputStream.close()

    var sampleSize = 1
    val maxDim = 800
    while (options.outWidth / sampleSize > maxDim || options.outHeight / sampleSize > maxDim) {
      sampleSize *= 2
    }

    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val secondStream = context.contentResolver.openInputStream(uri) ?: return null
    val bitmap = BitmapFactory.decodeStream(secondStream, null, decodeOptions)
    secondStream.close()
    if (bitmap == null) return null

    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
    val bytes = baos.toByteArray()
    "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
  } catch (_: Exception) { null }
}

fun compressBitmapDirect(bitmap: Bitmap): String {
  val baos = ByteArrayOutputStream()
  val maxDim = 800
  val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
    val width = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
    val height = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
    Bitmap.createScaledBitmap(bitmap, width, height, true)
  } else bitmap
  scaled.compress(Bitmap.CompressFormat.JPEG, 75, baos)
  return "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
}

fun getFileName(context: Context, uri: Uri): String {
  var name = "attachment.file"
  try {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0) name = cursor.getString(idx)
      }
    }
  } catch (_: Exception) {}
  return name
}

fun getAudioDurationSeconds(context: Context, uri: Uri): Int {
  return try {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, uri)
    val timeStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    retriever.release()
    val ms = timeStr?.toLongOrNull() ?: 0L
    (ms / 1000).toInt()
  } catch (_: Exception) {
    0
  }
}

@Composable
fun ChatInputBar(
  channelName: String,
  text: String,
  onTextChanged: (String) -> Unit,
  onSendMessage: (
    content: String,
    coordinates: String?,
    imageUrl: String?,
    audioUrl: String?,
    audioDuration: Int,
    fileUrl: String?,
    fileName: String?,
    isSticker: Boolean,
    replyTo: ChatMessage?
  ) -> Unit,
  replyingTo: ChatMessage? = null,
  onCancelReply: () -> Unit = {},
  isEmojiPickerVisible: Boolean,
  onToggleEmojiPicker: () -> Unit,
  onTextFieldFocused: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var showAttachmentMenu by remember { mutableStateOf(false) }
  var showCoordinateInput by remember { mutableStateOf(false) }
  var coordText by remember { mutableStateOf("") }
  var isUploadingImage by remember { mutableStateOf(false) }

  var isRecording by remember { mutableStateOf(false) }
  var recordingSeconds by remember { mutableIntStateOf(0) }
  var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
  var recordedAudioFile by remember { mutableStateOf<File?>(null) }

  val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    if (uri != null) {
      scope.launch {
        isUploadingImage = true
        val cloudResult = ImgBbUploader.uploadImage(context, uri)
        isUploadingImage = false

        cloudResult.onSuccess { fullResUrl ->
          onSendMessage("", null, fullResUrl, null, 0, null, null, false, replyingTo)
        }.onFailure {
          val base64Img = processAndCompressImage(uri, context)
          if (base64Img != null) {
            onSendMessage("", null, base64Img, null, 0, null, null, false, replyingTo)
          }
        }
      }
    }
  }

  val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
    if (bitmap != null) {
      scope.launch {
        isUploadingImage = true
        val cloudResult = ImgBbUploader.uploadBitmap(bitmap)
        isUploadingImage = false

        cloudResult.onSuccess { fullResUrl ->
          onSendMessage("", null, fullResUrl, null, 0, null, null, false, replyingTo)
        }.onFailure {
          val base64Img = compressBitmapDirect(bitmap)
          onSendMessage("", null, base64Img, null, 0, null, null, false, replyingTo)
        }
      }
    }
  }

  val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    if (uri != null) {
      scope.launch {
        try {
          isUploadingImage = true
          val fileName = getFileName(context, uri)
          context.contentResolver.openInputStream(uri)?.use { stream ->
            val bytes = stream.readBytes()
            if (bytes.size > 6 * 1024 * 1024) {
              Toast.makeText(context, "File exceeds max 6MB limit", Toast.LENGTH_SHORT).show()
              return@launch
            }
            val base64File = "data:application/octet-stream;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
            onSendMessage("", null, null, null, 0, base64File, fileName, false, replyingTo)
          }
        } catch (e: Exception) {
          Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
          isUploadingImage = false
        }
      }
    }
  }

  // Audio picker extracting true duration
  val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    if (uri != null) {
      scope.launch {
        try {
          isUploadingImage = true
          val fileName = getFileName(context, uri)
          val durSec = getAudioDurationSeconds(context, uri)
          Toast.makeText(context, "Processing $fileName...", Toast.LENGTH_SHORT).show()

          context.contentResolver.openInputStream(uri)?.use { stream ->
            val bytes = stream.readBytes()
            if (bytes.size > 6 * 1024 * 1024) {
              Toast.makeText(context, "Audio exceeds max 6MB limit", Toast.LENGTH_SHORT).show()
              return@launch
            }
            val base64Audio = "data:audio/mp4;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
            onSendMessage("", null, null, base64Audio, durSec, null, fileName, false, replyingTo)
          }
        } catch (e: Exception) {
          Toast.makeText(context, "Failed to read audio: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
          isUploadingImage = false
        }
      }
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

  Surface(modifier = modifier.fillMaxWidth().testTag("chat_input_bar"), color = Color.Transparent) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)) {
      AnimatedVisibility(
        visible = replyingTo != null,
        enter = slideInVertically(animationSpec = tween(220)) { it } + expandVertically(animationSpec = tween(220)) + fadeIn(),
        exit = slideOutVertically(animationSpec = tween(200)) { it } + shrinkVertically(animationSpec = tween(200)) + fadeOut()
      ) {
        replyingTo?.let { replyTarget ->
          Surface(
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
            color = Color(0xFFF0F2F5),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
          ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.width(3.dp).height(32.dp).background(WhatsAppGreenDark, RoundedCornerShape(2.dp)))
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text("Replying to ${replyTarget.senderName}", fontWeight = FontWeight.Bold, color = WhatsAppGreenDark, fontSize = 12.sp)
                Text(
                  replyTarget.content.ifBlank { if (replyTarget.isSticker) "💟 Sticker" else if (replyTarget.imageUrl != null) "📷 Photo" else if (replyTarget.fileUrl != null) "📄 Document" else "🎤 Voice note" },
                  color = WhatsAppTextSecondary,
                  fontSize = 11.5.sp,
                  maxLines = 1
                )
              }
              IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = WhatsAppTextSecondary, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

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

      AnimatedVisibility(visible = showAttachmentMenu) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.White,
          shadowElevation = 3.dp,
          modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
          Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showAttachmentMenu = false; fileLauncher.launch("*/*") }) {
              Surface(shape = CircleShape, color = Color(0xFF7E57C2), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = "Document", tint = Color.White) }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Document", fontSize = 12.sp, color = WhatsAppTextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showAttachmentMenu = false; audioLauncher.launch("audio/*") }) {
              Surface(shape = CircleShape, color = Color(0xFFE65100), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AudioFile, contentDescription = "Audio", tint = Color.White) }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Audio", fontSize = 12.sp, color = WhatsAppTextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showAttachmentMenu = false; galleryLauncher.launch("image/*") }) {
              Surface(shape = CircleShape, color = Color(0xFFE91E63), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Image, contentDescription = "Gallery", tint = Color.White) }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Gallery", fontSize = 12.sp, color = WhatsAppTextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showAttachmentMenu = false; cameraLauncher.launch(null) }) {
              Surface(shape = CircleShape, color = Color(0xFF00A884), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White) }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Camera", fontSize = 12.sp, color = WhatsAppTextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showAttachmentMenu = false; showCoordinateInput = true }) {
              Surface(shape = CircleShape, color = Color(0xFF2196F3), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Place, contentDescription = "Location", tint = Color.White) }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Location", fontSize = 12.sp, color = WhatsAppTextPrimary)
            }
          }
        }
      }

      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(26.dp), color = Color.White, shadowElevation = 1.dp) {
          if (isRecording) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StatusCallEndRed))
                Spacer(modifier = Modifier.width(8.dp))
                Text("HD Recording ${recordingSeconds}s", fontWeight = FontWeight.Bold, color = StatusCallEndRed, fontSize = 14.sp)
              }
              Text("Cancel", color = WhatsAppTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                try { recorder?.stop(); recorder?.release() } catch (_: Exception) {}
                recorder = null; isRecording = false; recordedAudioFile?.delete()
              })
            }
          } else {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = onToggleEmojiPicker, modifier = Modifier.size(34.dp)) {
                Icon(if (isEmojiPickerVisible) Icons.Default.Keyboard else Icons.Default.SentimentSatisfied, contentDescription = "Emoji", tint = if (isEmojiPickerVisible) WhatsAppGreenDark else WhatsAppTextSecondary, modifier = Modifier.size(24.dp))
              }
              OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) onTextFieldFocused() }.testTag("chat_text_input"),
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
                keyboardActions = KeyboardActions(onSend = {
                  if (text.isNotBlank() || coordText.isNotBlank()) {
                    val coords = if (coordText.isNotBlank()) coordText.trim() else null
                    onSendMessage(text.trim(), coords, null, null, 0, null, null, false, replyingTo)
                    coordText = ""; showCoordinateInput = false
                  }
                }),
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
            if (isUploadingImage) return@FloatingActionButton

            if (isRecording) {
              try {
                recorder?.stop(); recorder?.release(); recorder = null; isRecording = false
                val file = recordedAudioFile
                if (file != null && file.exists()) {
                  val bytes = file.readBytes()
                  val base64Aud = "data:audio/mp4;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                  onSendMessage("", null, null, base64Aud, recordingSeconds, null, null, false, replyingTo)
                  file.delete()
                }
              } catch (_: Exception) {}
            } else if (canSend) {
              val coords = if (coordText.isNotBlank()) coordText.trim() else null
              onSendMessage(text.trim(), coords, null, null, 0, null, null, false, replyingTo)
              coordText = ""; showCoordinateInput = false
            } else {
              try {
                val tempAudio = File(context.cacheDir, "rec_${System.currentTimeMillis()}.m4a")
                recordedAudioFile = tempAudio
                recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
                recorder?.apply {
                  setAudioSource(MediaRecorder.AudioSource.MIC)
                  setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                  setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                  setAudioSamplingRate(48000)
                  setAudioEncodingBitRate(128000)
                  setOutputFile(tempAudio.absolutePath)
                  prepare()
                  start()
                }
                isRecording = true
              } catch (_: Exception) { isRecording = false }
            }
          },
          modifier = Modifier.size(48.dp).testTag("chat_send_button"),
          shape = CircleShape,
          containerColor = if (isRecording) StatusCallEndRed else WhatsAppGreenTeal,
          contentColor = Color.White,
          elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
          if (isUploadingImage) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
          } else {
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
}
