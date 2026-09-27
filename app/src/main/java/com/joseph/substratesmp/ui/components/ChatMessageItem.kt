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
import com.joseph.substratesmp.ui.theme.SubstrateTheme
import com.joseph.substratesmp.ui.theme.WhatsAppChatIncoming
import com.joseph.substratesmp.ui.theme.WhatsAppChatOutgoing
import com.joseph.substratesmp.ui.theme.WhatsAppCheckmarkBlue
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary

@Composable
fun ChatMessageItem(
  message: ChatMessage,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  val isLocal = message.isLocalUser
  val isSenderAdmin = message.isAdmin

  val bubbleShape = if (isLocal) {
    RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
  } else {
    RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 3.dp)
      .testTag("chat_message_${message.id}"),
    horizontalArrangement = if (isLocal) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Top
  ) {
    Card(
      shape = bubbleShape,
      colors = CardDefaults.cardColors(
        containerColor = if (isLocal) WhatsAppChatOutgoing else WhatsAppChatIncoming
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier.widthIn(min = 80.dp, max = 310.dp)
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        // Sender Header for incoming messages
        if (!isLocal) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
          ) {
            Text(
              text = message.senderName,
              style = MaterialTheme.typography.labelMedium,
              color = if (isSenderAdmin) RoleAdminGold else SubstrateTheme.customColors.statusOnline,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
            if (isSenderAdmin) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = RoleAdminGold.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoleAdminGold.copy(alpha = 0.6f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = RoleAdminGold,
                    modifier = Modifier.size(10.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "ADMIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = RoleAdminGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp
                  )
                }
              }
            }
          }
        }

        // Message Content
        Text(
          text = message.content,
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White,
          lineHeight = 18.sp,
          fontSize = 14.sp
        )

        // Coordinates attachment card (Minecraft Bedrock location stamp)
        if (!message.coordinates.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.25f),
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
                tint = SubstrateTheme.customColors.statusOnline,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = message.coordinates,
                style = CoordinateTextStyle,
                color = SubstrateTheme.customColors.statusOnline,
                fontSize = 11.sp
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

        // WhatsApp timestamp & double checkmarks footer
        Row(
          modifier = Modifier
            .align(Alignment.End)
            .padding(top = 2.dp),
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
