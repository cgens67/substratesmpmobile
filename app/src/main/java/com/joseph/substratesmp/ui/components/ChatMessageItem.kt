package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.RoleAdminGoldContainer
import com.joseph.substratesmp.ui.theme.WhatsAppCheckmarkBlue
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppIncomingBubble
import com.joseph.substratesmp.ui.theme.WhatsAppOutgoingBubble
import com.joseph.substratesmp.ui.theme.WhatsAppTextPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary

@Composable
fun ChatMessageItem(
  message: ChatMessage,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  val isLocal = message.isLocalUser
  val isSenderAdmin = message.isAdmin

  // WhatsApp rounded message bubble shape
  val bubbleShape = if (isLocal) {
    RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomEnd = 14.dp, bottomStart = 14.dp)
  } else {
    RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp)
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 2.dp)
      .testTag("chat_message_${message.id}"),
    horizontalArrangement = if (isLocal) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Top
  ) {
    Card(
      shape = bubbleShape,
      colors = CardDefaults.cardColors(
        containerColor = if (isLocal) WhatsAppOutgoingBubble else WhatsAppIncomingBubble
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier.widthIn(min = 90.dp, max = 310.dp)
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
      ) {
        // Sender header for incoming group messages
        if (!isLocal) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
          ) {
            Text(
              text = message.senderName,
              style = MaterialTheme.typography.labelMedium,
              color = if (isSenderAdmin) RoleAdminGold else WhatsAppGreenDark,
              fontWeight = FontWeight.Bold,
              fontSize = 12.5.sp
            )
            if (isSenderAdmin) {
              Spacer(modifier = Modifier.width(5.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = RoleAdminGoldContainer
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = RoleAdminGold,
                    modifier = Modifier.size(9.dp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(
                    text = "ADMIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = RoleAdminGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 8.sp
                  )
                }
              }
            }
          }
        }

        // Message text
        Text(
          text = message.content,
          style = MaterialTheme.typography.bodyMedium,
          color = WhatsAppTextPrimary,
          lineHeight = 18.sp,
          fontSize = 14.5.sp
        )

        // Minecraft Coordinates attachment pill
        if (!message.coordinates.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isLocal) Color.White.copy(alpha = 0.6f) else Color(0xFFF0F2F5),
            modifier = Modifier.clickable {
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
                tint = WhatsAppGreenDark,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = message.coordinates,
                style = CoordinateTextStyle,
                color = WhatsAppGreenDark,
                fontSize = 11.5.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy coordinates",
                tint = WhatsAppTextSecondary,
                modifier = Modifier.size(12.dp)
              )
            }
          }
        }

        // Timestamp & WhatsApp Double Checkmarks
        Row(
          modifier = Modifier
            .align(Alignment.End)
            .padding(top = 1.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = message.formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = WhatsAppTextSecondary,
            fontSize = 10.sp
          )
          if (isLocal) {
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
              imageVector = Icons.Default.DoneAll,
              contentDescription = "Sent",
              tint = WhatsAppCheckmarkBlue,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }
  }
}
