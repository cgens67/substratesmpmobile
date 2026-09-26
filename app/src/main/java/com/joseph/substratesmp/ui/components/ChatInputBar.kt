package com.joseph.substratesmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.ui.theme.ChannelPillShape
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle

@Composable
fun ChatInputBar(
  channelName: String,
  onSendMessage: (content: String, coordinates: String?) -> Unit,
  modifier: Modifier = Modifier
) {
  var text by remember { mutableStateOf("") }
  var showCoordinateInput by remember { mutableStateOf(false) }
  var coordText by remember { mutableStateOf("") }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .testTag("chat_input_bar"),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    tonalElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      // Coordinate pill toggle row
      AnimatedVisibility(
        visible = showCoordinateInput,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          OutlinedTextField(
            value = coordText,
            onValueChange = { coordText = it },
            placeholder = {
              Text("Coordinates (e.g. X: -120, Y: 64, Z: 540)", style = CoordinateTextStyle)
            },
            textStyle = CoordinateTextStyle,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("coordinates_text_field"),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
          )
          IconButton(
            onClick = {
              showCoordinateInput = false
              coordText = ""
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Cancel coordinates",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Bedrock Coordinate quick stamp toggle
        IconButton(
          onClick = { showCoordinateInput = !showCoordinateInput },
          modifier = Modifier
            .size(40.dp)
            .testTag("toggle_coords_button"),
          colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (showCoordinateInput) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
          )
        ) {
          Icon(
            imageVector = Icons.Default.Place,
            contentDescription = "Attach Coordinates",
            tint = if (showCoordinateInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Main text input field
        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          modifier = Modifier
            .weight(1f)
            .testTag("chat_text_input"),
          placeholder = {
            Text(
              text = "Message #$channelName",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
          },
          shape = MaterialTheme.shapes.medium,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
          ),
          keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Send
          ),
          keyboardActions = KeyboardActions(
            onSend = {
              if (text.isNotBlank() || coordText.isNotBlank()) {
                val coords = if (coordText.isNotBlank()) coordText.trim() else null
                onSendMessage(text.trim(), coords)
                text = ""
                coordText = ""
                showCoordinateInput = false
              }
            }
          ),
          maxLines = 4
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Send button
        val canSend = text.isNotBlank() || coordText.isNotBlank()
        IconButton(
          onClick = {
            if (canSend) {
              val coords = if (coordText.isNotBlank()) coordText.trim() else null
              onSendMessage(text.trim(), coords)
              text = ""
              coordText = ""
              showCoordinateInput = false
            }
          },
          modifier = Modifier
            .size(42.dp)
            .testTag("chat_send_button"),
          colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
            contentColor = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
          ),
          enabled = canSend
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send message",
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
