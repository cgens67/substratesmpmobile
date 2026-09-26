package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GamertagDialog(
  currentGamertag: String,
  currentRole: String,
  onDismiss: () -> Unit,
  onSave: (gamertag: String, role: String) -> Unit
) {
  var gamertagInput by remember { mutableStateOf(currentGamertag) }
  var selectedRole by remember { mutableStateOf(currentRole) }

  val roles = listOf("BUILDER", "MEMBER", "MOD", "ADMIN")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Minecraft Bedrock Gamertag",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Enter your in-game Bedrock gamertag to link your messages and voice presence on Substrate SMP:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
          value = gamertagInput,
          onValueChange = { gamertagInput = it },
          label = { Text("Bedrock Gamertag") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("gamertag_input_field"),
          shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Role Badge:",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          roles.forEach { role ->
            FilterChip(
              selected = selectedRole == role,
              onClick = { selectedRole = role },
              label = { Text(role, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (gamertagInput.isNotBlank()) {
            onSave(gamertagInput.trim(), selectedRole)
          }
        },
        enabled = gamertagInput.isNotBlank(),
        modifier = Modifier.testTag("save_gamertag_button")
      ) {
        Text("Save & Sync")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
