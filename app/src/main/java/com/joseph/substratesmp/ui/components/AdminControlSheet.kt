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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.WhatsAppDivider
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppNavSelectedPill
import com.joseph.substratesmp.ui.theme.WhatsAppTextPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary

data class AdminMember(
  val id: String,
  val gamertag: String,
  val role: String,
  val isAdmin: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlSheet(
  channels: List<Channel>,
  members: List<AdminMember>,
  onDismiss: () -> Unit,
  onCreateChannel: (name: String, type: ChannelType, desc: String, onlyAdmin: Boolean) -> Unit,
  onDeleteChannel: (channelId: String) -> Unit,
  onToggleChannelPermission: (channel: Channel) -> Unit,
  onUpdateMemberRole: (userId: String, newRole: String) -> Unit,
  onUpdateMemberGamertag: (userId: String, newName: String) -> Unit,
  onRemoveMember: (userId: String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Channels, 1: Members

  // Channel Creation State
  var showNewChannelDialog by remember { mutableStateOf(false) }
  var newChannelName by remember { mutableStateOf("") }
  var newChannelDesc by remember { mutableStateOf("") }
  var newChannelType by remember { mutableStateOf(ChannelType.TEXT) }
  var newChannelAdminOnly by remember { mutableStateOf(false) }

  // Member Edit State
  var memberToEdit by remember { mutableStateOf<AdminMember?>(null) }
  var editGamertagInput by remember { mutableStateOf("") }
  var editRoleInput by remember { mutableStateOf("MEMBER") }

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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Admin Control Console", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      PrimaryTabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.White,
        contentColor = WhatsAppGreenDark
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Channels (${channels.size})", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Members (${members.size})", fontWeight = FontWeight.Bold) }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (selectedTab == 0) {
        // CHANNELS TAB
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Server Channels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Button(
            onClick = { showNewChannelDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Create Channel")
          }
        }

        if (showNewChannelDialog) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF7F8FA),
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text("New Channel", fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = newChannelName,
                onValueChange = { newChannelName = it },
                label = { Text("Channel Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = newChannelDesc,
                onValueChange = { newChannelDesc = it },
                label = { Text("Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                  selected = newChannelType == ChannelType.TEXT,
                  onClick = { newChannelType = ChannelType.TEXT },
                  label = { Text("Text Channel") }
                )
                FilterChip(
                  selected = newChannelType == ChannelType.VOICE,
                  onClick = { newChannelType = ChannelType.VOICE },
                  label = { Text("Voice Channel") }
                )
              }
              if (newChannelType == ChannelType.TEXT) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.clickable { newChannelAdminOnly = !newChannelAdminOnly }.padding(vertical = 6.dp)
                ) {
                  Icon(if (newChannelAdminOnly) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = null, tint = RoleAdminGold)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Restrict sending to Admins only (Announcements)", style = MaterialTheme.typography.bodySmall)
                }
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                  onClick = {
                    if (newChannelName.isNotBlank()) {
                      onCreateChannel(newChannelName.trim(), newChannelType, newChannelDesc.trim(), newChannelAdminOnly)
                      newChannelName = ""
                      newChannelDesc = ""
                      showNewChannelDialog = false
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
                ) {
                  Text("Add Channel")
                }
              }
            }
          }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
          items(channels) { channel ->
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(channel.name, fontWeight = FontWeight.Bold, color = WhatsAppTextPrimary)
                  if (channel.isRestrictedToAdmin) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = RoleAdminGold.copy(alpha = 0.2f)) {
                      Text("ADMINS ONLY", color = RoleAdminGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                  }
                }
                Text(channel.description, color = WhatsAppTextSecondary, fontSize = 12.sp, maxLines = 1)
              }

              Row {
                if (channel.type == ChannelType.TEXT) {
                  IconButton(onClick = { onToggleChannelPermission(channel) }) {
                    Icon(
                      imageVector = if (channel.isRestrictedToAdmin) Icons.Default.Lock else Icons.Default.LockOpen,
                      contentDescription = "Toggle Permission",
                      tint = if (channel.isRestrictedToAdmin) RoleAdminGold else WhatsAppTextSecondary
                    )
                  }
                }
                IconButton(onClick = { onDeleteChannel(channel.id) }) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEA0038))
                }
              }
            }
            HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp)
          }
        }
      } else {
        // MEMBERS TAB
        Text("Registered Members", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        if (memberToEdit != null) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF7F8FA),
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text("Edit Member: ${memberToEdit?.gamertag}", fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = editGamertagInput,
                onValueChange = { editGamertagInput = it },
                label = { Text("Gamertag") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text("Assign Role:", style = MaterialTheme.typography.labelMedium)
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("MEMBER", "BUILDER", "MOD", "ADMIN").forEach { role ->
                  FilterChip(
                    selected = editRoleInput == role,
                    onClick = { editRoleInput = role },
                    label = { Text(role) }
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                  onClick = {
                    val m = memberToEdit ?: return@Button
                    if (editGamertagInput.isNotBlank()) {
                      onUpdateMemberGamertag(m.id, editGamertagInput.trim())
                    }
                    onUpdateMemberRole(m.id, editRoleInput)
                    memberToEdit = null
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
                ) {
                  Text("Save")
                }
              }
            }
          }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
          items(members) { member ->
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier.size(36.dp).clip(CircleShape).background(if (member.isAdmin) RoleAdminGold else WhatsAppNavSelectedPill),
                  contentAlignment = Alignment.Center
                ) {
                  Text(member.gamertag.take(1).uppercase(), fontWeight = FontWeight.Bold, color = if (member.isAdmin) Color.White else WhatsAppGreenDark)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(member.gamertag, fontWeight = FontWeight.Bold, color = WhatsAppTextPrimary)
                  Text("Role: ${member.role}", color = WhatsAppTextSecondary, fontSize = 12.sp)
                }
              }

              Row {
                IconButton(
                  onClick = {
                    memberToEdit = member
                    editGamertagInput = member.gamertag
                    editRoleInput = member.role
                  }
                ) {
                  Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WhatsAppTextSecondary)
                }
                if (!member.gamertag.equals("Siang5680", ignoreCase = true)) {
                  IconButton(onClick = { onRemoveMember(member.id) }) {
                    Icon(Icons.Default.PersonRemove, contentDescription = "Remove", tint = Color(0xFFEA0038))
                  }
                }
              }
            }
            HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp)
          }
        }
      }
    }
  }
}
