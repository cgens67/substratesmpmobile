package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.ui.theme.WhatsAppTextPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary

data class SheetOption(
  val id: String,
  val title: String,
  val subtitle: String,
  val isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownModalSheet(
  options: List<SheetOption>,
  isDarkMode: Boolean,
  onDismiss: () -> Unit,
  onOptionSelected: (SheetOption) -> Unit
) {
  val surfaceColor = if (isDarkMode) Color(0xFF303030) else Color(0xFFFBFBFB)
  val textColor = if (isDarkMode) Color.White else WhatsAppTextPrimary
  val subTextColor = if (isDarkMode) Color.LightGray else WhatsAppTextSecondary
  val selectedBgColor = if (isDarkMode) Color(0xFF424242) else Color(0xFFEDEDED)
  val checkmarkColor = if (isDarkMode) Color.White else Color.Black

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor = surfaceColor
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      options.forEach { option ->
        val isSelected = option.isSelected
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = if (isSelected) selectedBgColor else Color.Transparent,
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onOptionSelected(option)
              onDismiss()
            }
            .padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = checkmarkColor,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(16.dp))
            } else {
              Spacer(modifier = Modifier.width(36.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                fontSize = 17.sp
              )
              Text(
                text = option.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subTextColor,
                fontSize = 13.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
