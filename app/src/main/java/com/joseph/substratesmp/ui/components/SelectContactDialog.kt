package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.WhatsAppDivider
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppNavSelectedPill
import com.joseph.substratesmp.ui.theme.WhatsAppTextPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectContactDialog(
  members: List<AdminMember>,
  currentGamertag: String,
  isDarkMode: Boolean,
  onDismiss: () -> Unit,
  onSelectMember: (AdminMember) -> Unit
) {
  val availableMembers = members.filter { !it.gamertag.equals(currentGamertag, ignoreCase = true) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  val surfaceColor = if (isDarkMode) Color(0xFF303030) else Color.White
  val textColor = if (isDarkMode) Color.White else WhatsAppTextPrimary
  val subTextColor = if (isDarkMode) Color.LightGray else WhatsAppTextSecondary
  val dividerColor = if (isDarkMode) Color.Gray.copy(alpha = 0.3f) else WhatsAppDivider

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    containerColor = surfaceColor
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Select Contact", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
          Text("${availableMembers.size} Bedrock players", style = MaterialTheme.typography.bodySmall, color = subTextColor)
        }
        IconButton(onClick = { scope.launch { sheetState.hide(); onDismiss() } }) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = subTextColor)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

      if (availableMembers.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No other players found to message.", color = subTextColor, fontSize = 14.sp)
        }
      } else {
        LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
          items(availableMembers) { member ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  scope.launch {
                    sheetState.hide()
                    onSelectMember(member)
                    onDismiss()
                  }
                }
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(if (member.isAdmin) RoleAdminGold else (if (isDarkMode) Color(0xFF005C4B) else WhatsAppNavSelectedPill)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = member.gamertag.take(1).uppercase(),
                  color = if (member.isAdmin) Color.White else (if (isDarkMode) Color.White else WhatsAppGreenDark),
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(member.gamertag, fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                  if (member.isAdmin) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(13.dp))
                  }
                }
                Text("Role: ${member.role} • Tap to message privately", color = subTextColor, fontSize = 12.sp)
              }
            }
            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
          }
        }
      }
    }
  }
}
