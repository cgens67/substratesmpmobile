package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.joseph.substratesmp.voice.AgoraSettings

@Composable
fun AgoraSettingsDialog(
  currentSettings: AgoraSettings,
  onDismiss: () -> Unit,
  onSave: (appId: String, token: String) -> Unit
) {
  var appIdInput by remember { mutableStateOf(currentSettings.appId) }
  var tokenInput by remember { mutableStateOf(currentSettings.token) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Agora.io RTC Settings",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Agora RTC Engine real-time voice & audio settings for Substrate SMP Bedrock channels:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = appIdInput,
          onValueChange = { appIdInput = it },
          label = { Text("Agora App ID") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("agora_app_id_input"),
          shape = MaterialTheme.shapes.small
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = tokenInput,
          onValueChange = { tokenInput = it },
          label = { Text("Agora RTC Token (Optional / Testing)") },
          placeholder = { Text("Leave blank for test mode (token=null)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.small
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(appIdInput.trim(), tokenInput.trim())
        },
        modifier = Modifier.testTag("save_agora_settings_button")
      ) {
        Text("Save Config")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
