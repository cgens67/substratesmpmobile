package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

@Composable
fun StatusCreatorDialog(
  onDismiss: () -> Unit,
  onPostStatus: (text: String, theme: String, activity: String, coords: String?) -> Unit
) {
  var contentText by remember { mutableStateOf("") }
  var coordsText by remember { mutableStateOf("") }
  var selectedTheme by remember { mutableStateOf("EMERALD") }
  var selectedActivity by remember { mutableStateOf("⛏️ Mining") }

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
          placeholder = { Text("Found a deep dark ancient city!") },
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
            onPostStatus(contentText.trim(), selectedTheme, selectedActivity, coords)
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
}
