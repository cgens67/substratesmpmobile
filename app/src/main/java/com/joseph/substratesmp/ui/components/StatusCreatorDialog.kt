package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

@Composable
fun StatusCreatorDialog(
  isDarkMode: Boolean = false,
  onDismiss: () -> Unit,
  onPostStatus: (
    text: String,
    theme: String,
    activity: String,
    coords: String?,
    musicName: String?,
    musicArtist: String?,
    musicPreview: String?,
    musicArtwork: String?,
    musicStartMs: Int
  ) -> Unit
) {
  var contentText by remember { mutableStateOf("") }
  var coordsText by remember { mutableStateOf("") }
  var selectedTheme by remember { mutableStateOf("EMERALD") }
  var selectedActivity by remember { mutableStateOf("⛏️ Mining") }
  var selectedMusic by remember { mutableStateOf<MusicTrack?>(null) }
  var selectedStartOffsetMs by remember { mutableIntStateOf(0) }
  var showMusicPicker by remember { mutableStateOf(false) }

  val themes = listOf(
    "EMERALD" to Color(0xFF00A884),
    "CRIMSON" to Color(0xFFFF4500),
    "END_VOID" to Color(0xFF6A0DAD),
    "DIAMOND" to Color(0xFF00E5FF),
    "GOLDEN" to Color(0xFFFFD700),
    "OBSIDIAN" to Color(0xFF273142)
  )

  val activities = listOf(
    "⛏️ Mining",
    "⚔️ Raid",
    "🏰 Base Building",
    "🧭 Exploring",
    "💎 Found Diamonds"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    titleContentColor = Color.Black,
    textContentColor = Color.Black,
    title = { Text("Create Realm Status", fontWeight = FontWeight.Bold) },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = contentText,
          onValueChange = { contentText = it },
          label = { Text("What are you building or doing?") },
          placeholder = { Text("Found an ancient city deepslate base!") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          maxLines = 4
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = coordsText,
          onValueChange = { coordsText = it },
          label = { Text("Coordinates (Optional)") },
          leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
          placeholder = { Text("X: -420, Y: 12, Z: 890") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedMusic != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF2F4F7),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              AsyncImage(
                model = selectedMusic!!.artworkUrl,
                contentDescription = null,
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(selectedMusic!!.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                Text("${selectedMusic!!.artist} • Starts at ${selectedStartOffsetMs / 1000}s", color = Color.Gray, fontSize = 11.5.sp, maxLines = 1)
              }
              IconButton(onClick = { selectedMusic = null; selectedStartOffsetMs = 0 }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(18.dp))
              }
            }
          }
        } else {
          OutlinedButton(
            onClick = { showMusicPicker = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = WhatsAppGreenDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Soundtrack / Song", color = WhatsAppGreenDark, fontWeight = FontWeight.SemiBold)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Theme Background:", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          themes.forEach { (name, color) ->
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { selectedTheme = name }
                .then(
                  if (selectedTheme == name) Modifier.border(2.5.dp, Color.Black, CircleShape)
                  else Modifier
                )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Activity Tag:", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(activities) { act ->
            FilterChip(
              selected = selectedActivity == act,
              onClick = { selectedActivity = act },
              label = { Text(act, fontSize = 12.sp) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (contentText.isNotBlank()) {
            val coords = if (coordsText.isNotBlank()) coordsText.trim() else null
            onPostStatus(
              contentText.trim(),
              selectedTheme,
              selectedActivity,
              coords,
              selectedMusic?.title,
              selectedMusic?.artist,
              selectedMusic?.previewUrl,
              selectedMusic?.artworkUrl,
              selectedStartOffsetMs
            )
            onDismiss()
          }
        },
        enabled = contentText.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
      ) {
        Text("Post Status")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Black) }
    }
  )

  if (showMusicPicker) {
    StatusMusicSelectorSheet(
      isDarkMode = isDarkMode,
      onDismiss = { showMusicPicker = false },
      onSelectTrack = { track, startOffsetMs ->
        selectedMusic = track
        selectedStartOffsetMs = startOffsetMs
        showMusicPicker = false
      }
    )
  }
}

// Convenient overload supporting legacy 4-parameter calls
@Composable
fun StatusCreatorDialog(
  onDismiss: () -> Unit,
  onPostStatus: (text: String, theme: String, activity: String, coords: String?) -> Unit
) {
  StatusCreatorDialog(
    isDarkMode = false,
    onDismiss = onDismiss,
    onPostStatus = { text, theme, activity, coords, _, _, _, _, _ ->
      onPostStatus(text, theme, activity, coords)
    }
  )
}
