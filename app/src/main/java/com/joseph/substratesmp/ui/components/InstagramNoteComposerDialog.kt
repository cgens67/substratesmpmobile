package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@Composable
fun InstagramNoteComposerDialog(
  currentGamertag: String,
  isDarkMode: Boolean,
  onDismiss: () -> Unit,
  onShareNote: (
    thoughtText: String,
    musicName: String?,
    musicArtist: String?,
    musicPreview: String?,
    musicArtwork: String?
  ) -> Unit
) {
  var thoughtText by remember { mutableStateOf("") }
  var selectedTrack by remember { mutableStateOf<MusicTrack?>(null) }
  var showMusicPicker by remember { mutableStateOf(false) }

  val maxChars = 60
  val cardBg = if (isDarkMode) Color(0xFF1E2024) else Color.White
  val bubbleBg = if (isDarkMode) Color(0xFF2C2F36) else Color(0xFFF2F4F7)
  val textColor = if (isDarkMode) Color.White else Color(0xFF111B21)
  val subTextColor = if (isDarkMode) Color(0xFFA0AAB5) else Color(0xFF8E9297)
  val blueAccent = Color(0xFF007AFF)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = cardBg,
      modifier = Modifier
        .fillMaxWidth(0.90f)
        .padding(vertical = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = subTextColor)
          }

          Text(
            text = "New note",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = textColor
          )

          TextButton(
            onClick = {
              if (thoughtText.isNotBlank() || selectedTrack != null) {
                onShareNote(
                  thoughtText.trim(),
                  selectedTrack?.title,
                  selectedTrack?.artist,
                  selectedTrack?.previewUrl,
                  selectedTrack?.artworkUrl
                )
                onDismiss()
              }
            },
            enabled = thoughtText.isNotBlank() || selectedTrack != null
          ) {
            Text(
              text = "Share",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = if (thoughtText.isNotBlank() || selectedTrack != null) blueAccent else subTextColor
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cartoon Thought Bubble
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = bubbleBg,
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(horizontal = 8.dp)
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            BasicTextField(
              value = thoughtText,
              onValueChange = {
                if (it.length <= maxChars) thoughtText = it
              },
              modifier = Modifier.fillMaxWidth(),
              textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = textColor,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
              ),
              decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center) {
                  if (thoughtText.isEmpty()) {
                    Text(
                      text = "Share a thought...",
                      color = subTextColor,
                      fontSize = 14.5.sp,
                      textAlign = TextAlign.Center
                    )
                  }
                  inner()
                }
              }
            )

            if (selectedTrack != null) {
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isDarkMode) Color(0xFF1B1D22) else Color.White)
                  .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                AsyncImage(
                  model = selectedTrack!!.artworkUrl,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)),
                  contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${selectedTrack!!.title} • 30s",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = blueAccent,
                  maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { selectedTrack = null }, modifier = Modifier.size(18.dp)) {
                  Icon(Icons.Default.Close, contentDescription = null, tint = subTextColor, modifier = Modifier.size(12.dp))
                }
              }
            }
          }
        }

        // Thought Bubble pointer dots
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
            .size(72.dp)
            .clip(CircleShape)
            .background(if (isDarkMode) Color(0xFF383C44) else Color(0xFFE5E7EB))
            .border(2.dp, if (isDarkMode) Color(0xFF4A505C) else Color(0xFFD1D5DB), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (currentGamertag.isNotBlank()) currentGamertag.take(1).uppercase() else "?",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
          )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = currentGamertag,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Medium,
          color = subTextColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Add Music Action Button (30s Audio Clip)
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = if (isDarkMode) Color(0xFF2C2F36) else Color(0xFFF2F4F7),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { showMusicPicker = true }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(blueAccent.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.MusicNote, contentDescription = null, tint = blueAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (selectedTrack != null) selectedTrack!!.title else "Attach 30s song",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                color = textColor,
                maxLines = 1
              )
              Text(
                text = if (selectedTrack != null) selectedTrack!!.artist else "Pick soundtrack or hits",
                fontSize = 12.sp,
                color = subTextColor,
                maxLines = 1
              )
            }
          }
        }
      }
    }
  }

  if (showMusicPicker) {
    StatusMusicSelectorSheet(
      onDismiss = { showMusicPicker = false },
      onSelectTrack = { track ->
        selectedTrack = track
        showMusicPicker = false
      }
    )
  }
}
