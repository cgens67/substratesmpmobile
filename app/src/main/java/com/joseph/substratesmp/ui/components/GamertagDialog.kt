package com.joseph.substratesmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.ui.theme.SubstrateTheme

@Composable
fun GamertagDialog(
  currentGamertag: String,
  onDismiss: () -> Unit,
  onRegister: (gamertag: String, onComplete: (Result<String>) -> Unit) -> Unit
) {
  var gamertagInput by remember { mutableStateOf(currentGamertag) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  val isNewUser = currentGamertag.isBlank()
  val isAdminMatch = gamertagInput.trim().equals("Siang5680", ignoreCase = true)

  AlertDialog(
    onDismissRequest = {
      if (!isNewUser && !isLoading) {
        onDismiss()
      }
    },
    title = {
      Text(
        text = if (isNewUser) "Choose Your Gamertag" else "Edit Bedrock Gamertag",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (isNewUser) {
            "Welcome to Substrate SMP! Please enter your unique Minecraft Bedrock gamertag to start chatting and joining voice channels:"
          } else {
            "Update your Minecraft Bedrock gamertag. This must be unique across the server."
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = gamertagInput,
          onValueChange = {
            gamertagInput = it
            errorMessage = null
          },
          label = { Text("Bedrock Gamertag") },
          singleLine = true,
          enabled = !isLoading,
          isError = errorMessage != null,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("gamertag_input_field"),
          shape = MaterialTheme.shapes.medium
        )

        AnimatedVisibility(visible = errorMessage != null) {
          errorMessage?.let { msg ->
            Text(
              text = msg,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.labelSmall,
              modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
          }
        }

        // Distinct Admin Preview indicator for Siang5680
        AnimatedVisibility(visible = isAdminMatch) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = SubstrateTheme.customColors.adminGold.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, SubstrateTheme.customColors.adminGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SubstrateTheme.customColors.adminGold,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Admin Role Assigned (Siang5680)",
                style = MaterialTheme.typography.labelMedium,
                color = SubstrateTheme.customColors.adminGold,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (gamertagInput.trim().isNotBlank()) {
            isLoading = true
            errorMessage = null
            onRegister(gamertagInput.trim()) { result ->
              isLoading = false
              result.onFailure { exc ->
                errorMessage = exc.message ?: "Failed to register Gamertag. Try again."
              }
            }
          }
        },
        enabled = gamertagInput.trim().length >= 3 && !isLoading,
        modifier = Modifier.testTag("save_gamertag_button")
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
        }
        Text(if (isNewUser) "Register Gamertag" else "Save Gamertag")
      }
    },
    dismissButton = {
      if (!isNewUser) {
        TextButton(
          onClick = onDismiss,
          enabled = !isLoading
        ) {
          Text("Cancel")
        }
      }
    }
  )
}
