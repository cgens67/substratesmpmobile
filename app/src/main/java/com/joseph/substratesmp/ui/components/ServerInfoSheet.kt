package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle
import com.joseph.substratesmp.ui.theme.StatusOnline
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerInfoSheet(
  onDismiss: () -> Unit,
  isDarkMode: Boolean
) {
  val sheetState = rememberModalBottomSheetState()
  val clipboardManager = LocalClipboardManager.current

  val surfaceColor = if (isDarkMode) Color(0xFF303030) else Color.White
  val textColor = if (isDarkMode) Color.White else Color(0xFF111B21)
  val subTextColor = if (isDarkMode) Color.LightGray else Color(0xFF667781)
  val cardBgColor = if (isDarkMode) Color(0xFF424242) else Color(0xFFF7F8FA)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = surfaceColor
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .testTag("server_info_sheet")
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(StatusOnline)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Substrate SMP (Bedrock Realm)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = textColor
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Surface(
        shape = MaterialTheme.shapes.medium,
        color = cardBgColor,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          ServerDetailRow(
            icon = Icons.Default.Dns,
            label = "Server IP / Domain",
            value = "mc.substratesmp.net",
            labelColor = subTextColor,
            valueColor = textColor
          )
          Spacer(modifier = Modifier.height(8.dp))
          ServerDetailRow(
            icon = Icons.Default.Sensors,
            label = "Bedrock Port",
            value = "19132",
            labelColor = subTextColor,
            valueColor = textColor
          )
          Spacer(modifier = Modifier.height(8.dp))
          ServerDetailRow(
            icon = Icons.Default.Shield,
            label = "Bedrock Version",
            value = "v1.21.x",
            labelColor = subTextColor,
            valueColor = textColor
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = {
          clipboardManager.setText(AnnotatedString("mc.substratesmp.net:19132"))
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("copy_server_ip_button")
      ) {
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Copy Server IP:Port", color = Color.White)
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun ServerDetailRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String,
  labelColor: Color,
  valueColor: Color
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = WhatsAppGreenDark,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = labelColor
      )
    }
    Text(
      text = value,
      style = CoordinateTextStyle,
      color = valueColor,
      fontWeight = FontWeight.SemiBold
    )
  }
}
