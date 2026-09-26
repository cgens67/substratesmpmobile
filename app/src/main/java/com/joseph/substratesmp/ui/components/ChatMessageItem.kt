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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle
import com.joseph.substratesmp.ui.theme.MessageBubbleOtherShape
import com.joseph.substratesmp.ui.theme.MessageBubbleStartShape
import com.joseph.substratesmp.ui.theme.SubstrateTheme

@Composable
fun ChatMessageItem(
  message: ChatMessage,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  val isLocal = message.isLocalUser

  val roleColor = when (message.senderRole) {
    "ADMIN" -> SubstrateTheme.customColors.adminGold
    "MOD" -> SubstrateTheme.customColors.modCyan
    "BUILDER" -> SubstrateTheme.customColors.memberGreen
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 5.dp)
      .testTag("chat_message_${message.id}"),
    horizontalArrangement = if (isLocal) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Top
  ) {
    // Other user avatar (on left)
    if (!isLocal) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceContainerHigh)
          .border(1.5.dp, roleColor, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = message.senderName.take(1).uppercase(),
          style = MaterialTheme.typography.titleSmall,
          color = roleColor,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
    }

    // Message Card Bubble
    Column(
      horizontalAlignment = if (isLocal) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      // Header: Sender Name, Role Badge, and Time
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 3.dp, start = if (isLocal) 0.dp else 4.dp, end = if (isLocal) 4.dp else 0.dp)
      ) {
        if (!isLocal) {
          Text(
            text = message.senderName,
            style = MaterialTheme.typography.labelMedium,
            color = roleColor,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = roleColor.copy(alpha = 0.16f)
          ) {
            Text(
              text = message.senderRole,
              style = MaterialTheme.typography.labelSmall,
              color = roleColor,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
          text = message.formattedTime,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          fontSize = 10.sp
        )

        if (isLocal) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "You",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Bubble Body
      Card(
        shape = if (isLocal) MessageBubbleOtherShape else MessageBubbleStartShape,
        colors = CardDefaults.cardColors(
          containerColor = if (isLocal) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
          } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
          }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
          Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLocal) {
              MaterialTheme.colorScheme.onPrimaryContainer
            } else {
              MaterialTheme.colorScheme.onSurface
            },
            lineHeight = 19.sp
          )

          // Optional Minecraft Coordinates Tag
          if (!message.coordinates.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              shape = MaterialTheme.shapes.small,
              color = if (isLocal) {
                MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.7f)
              } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
              },
              modifier = Modifier
                .clickable {
                  clipboardManager.setText(AnnotatedString(message.coordinates))
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Place,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = message.coordinates,
                  style = CoordinateTextStyle,
                  color = MaterialTheme.colorScheme.primary,
                  fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy coordinates",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(11.dp)
                )
              }
            }
          }
        }
      }
    }

    // Local user avatar (on right)
    if (isLocal) {
      Spacer(modifier = Modifier.width(10.dp))
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary)
          .border(1.5.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = message.senderName.take(1).uppercase(),
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }
  }
}
