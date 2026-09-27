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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.WhatsAppDivider
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppNavSelectedPill
import com.joseph.substratesmp.ui.theme.WhatsAppTextPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectContactDialog(
  members: List<AdminMember>,
  currentGamertag: String,
  onDismiss: () -> Unit,
  onSelectMember: (AdminMember) -> Unit
) {
  val availableMembers = members.filter { !it.gamertag.equals(currentGamertag, ignoreCase = true) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    containerColor = Color.White
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
          Text("Select Contact", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WhatsAppTextPrimary)
          Text("${availableMembers.size} Bedrock players", style = MaterialTheme.typography.bodySmall, color = WhatsAppTextSecondary)
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = WhatsAppTextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp)

      if (availableMembers.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No other players found to message.", color = WhatsAppTextSecondary, fontSize = 14.sp)
        }
      } else {
        LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
          items(availableMembers) { member ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelectMember(member)
                  onDismiss()
                }
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(if (member.isAdmin) RoleAdminGold else WhatsAppNavSelectedPill),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = member.gamertag.take(1).uppercase(),
                  color = if (member.isAdmin) Color.White else WhatsAppGreenDark,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(member.gamertag, fontWeight = FontWeight.Bold, color = WhatsAppTextPrimary, fontSize = 16.sp)
                  if (member.isAdmin) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(13.dp))
                  }
                }
                Text("Role: ${member.role} • Tap to message privately", color = WhatsAppTextSecondary, fontSize = 12.sp)
              }
            }
            HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp)
          }
        }
      }
    }
  }
}
