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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SentimentSatisfied
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppGreenTeal
import com.joseph.substratesmp.ui.theme.WhatsAppTextPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary

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
    color = Color.Transparent
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
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
            tint = WhatsAppGreenDark,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          OutlinedTextField(
            value = coordText,
            onValueChange = { coordText = it },
            placeholder = {
              Text("Coordinates (e.g. X: -120, Y: 64, Z: 540)", style = CoordinateTextStyle, color = WhatsAppTextSecondary)
            },
            textStyle = CoordinateTextStyle,
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("coordinates_text_field"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = WhatsAppGreenDark,
              unfocusedBorderColor = Color(0xFFE9EDEF)
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
              contentDescription = "Cancel",
              tint = WhatsAppTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // WhatsApp White Rounded Capsule
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(26.dp),
          color = Color.White,
          shadowElevation = 1.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.SentimentSatisfied,
              contentDescription = "Emoji",
              tint = WhatsAppTextSecondary,
              modifier = Modifier.size(24.dp).padding(start = 2.dp)
            )

            OutlinedTextField(
              value = text,
              onValueChange = { text = it },
              modifier = Modifier
                .weight(1f)
                .testTag("chat_text_input"),
              placeholder = {
                Text(
                  text = "Message",
                  style = MaterialTheme.typography.bodyMedium,
                  color = WhatsAppTextSecondary,
                  fontSize = 15.sp
                )
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = WhatsAppTextPrimary,
                unfocusedTextColor = WhatsAppTextPrimary
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

            // Paperclip for coordinates
            IconButton(
              onClick = { showCoordinateInput = !showCoordinateInput },
              modifier = Modifier.size(34.dp).testTag("toggle_coords_button")
            ) {
              Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "Attach Coordinates",
                tint = if (showCoordinateInput) WhatsAppGreenDark else WhatsAppTextSecondary,
                modifier = Modifier.size(22.dp)
              )
            }

            // Camera icon
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Camera",
              tint = WhatsAppTextSecondary,
              modifier = Modifier.size(22.dp).padding(end = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // WhatsApp Circular Action FAB (Send or Mic)
        val canSend = text.isNotBlank() || coordText.isNotBlank()
        FloatingActionButton(
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
            .size(48.dp)
            .testTag("chat_send_button"),
          shape = CircleShape,
          containerColor = WhatsAppGreenTeal,
          contentColor = Color.White,
          elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
          Icon(
            imageVector = if (canSend) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
            contentDescription = if (canSend) "Send" else "Voice Note",
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }
  }
}
