package com.joseph.substratesmp.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import kotlin.math.abs

fun getMemberStatusString(gamertag: String, isAdmin: Boolean, currentGamertag: String): Pair<String, Boolean> {
  if (gamertag.equals(currentGamertag, ignoreCase = true) || isAdmin) {
    return Pair("online", true)
  }
  val hash = abs(gamertag.hashCode())
  return when (hash % 5) {
    0 -> Pair("online", true)
    1 -> Pair("last seen at 6:17 AM", false)
    2 -> Pair("last seen at 6:08 AM", false)
    3 -> Pair("last seen at 5:49 AM", false)
    else -> Pair("last seen yesterday at 11:44 PM", false)
  }
}

fun getMemberAvatarColor(gamertag: String): Color {
  val colors = listOf(
    Color(0xFF2ECC71), Color(0xFFE67E22), Color(0xFF3498DB),
    Color(0xFF9B59B6), Color(0xFFE74C3C), Color(0xFF1ABC9C), Color(0xFFF39C12)
  )
  return colors[abs(gamertag.hashCode()) % colors.size]
}

@Composable
fun ChannelActionCard(
  icon: ImageVector,
  label: String,
  surfaceColor: Color,
  textColor: Color,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    shadowElevation = 2.dp,
    modifier = Modifier
      .width(130.dp)
      .height(68.dp)
      .clickable(onClick = onClick)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(4.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
  }
}

@Composable
fun EmptyMediaPlaceholder(message: String, subTextColor: Color) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 40.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(message, color = subTextColor, fontSize = 14.sp)
  }
}

@Composable
fun ChannelInfoScreen(
  channel: Channel,
  members: List<AdminMember>,
  messages: List<ChatMessage>,
  currentGamertag: String,
  isAdmin: Boolean,
  isMuted: Boolean,
  isDarkMode: Boolean,
  onToggleMute: () -> Unit,
  onNavigateBack: () -> Unit,
  onSelectMember: (AdminMember) -> Unit,
  onAddMembers: () -> Unit,
  onImageClick: (String) -> Unit,
  onUpdateChannel: (channelId: String, newName: String, newDesc: String) -> Unit,
  onTogglePermission: (Channel) -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val uriHandler = LocalUriHandler.current

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Members", "Media", "Files", "Links", "Music", "GIFs")

  var showEditDialog by remember { mutableStateOf(false) }
  var showMoreMenu by remember { mutableStateOf(false) }

  val bgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF0F2F5)
  val surfaceColor = if (isDarkMode) Color(0xFF303030) else Color.White
  val textColor = if (isDarkMode) Color.White else Color.Black
  val subTextColor = if (isDarkMode) Color.LightGray else Color.Gray

  val initials = remember(channel.name) {
    val parts = channel.name.split("-", "_", " ").filter { it.isNotBlank() }
    if (parts.size >= 2) "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
    else channel.name.take(2).uppercase()
  }

  val mediaMessages = remember(messages) { messages.filter { !it.imageUrl.isNullOrBlank() && !it.isSticker } }
  val fileMessages = remember(messages) { messages.filter { !it.fileUrl.isNullOrBlank() } }
  val linkMessages = remember(messages) { messages.filter { it.content.contains("http://") || it.content.contains("https://") } }
  val musicMessages = remember(messages) { messages.filter { !it.audioUrl.isNullOrBlank() } }
  val gifMessages = remember(messages) { messages.filter { it.isSticker || it.content.contains(".gif", ignoreCase = true) } }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
      .statusBarsPadding()
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
      }
      Row {
        // Working Edit (pencil) button
        IconButton(onClick = { showEditDialog = true }) {
          Icon(Icons.Default.Edit, contentDescription = "Edit", tint = textColor)
        }
        // Working 3-dots button
        Box {
          IconButton(onClick = { showMoreMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = textColor)
          }
          DropdownMenu(
            expanded = showMoreMenu,
            onDismissRequest = { showMoreMenu = false },
            modifier = Modifier.background(surfaceColor)
          ) {
            DropdownMenuItem(
              text = { Text(if (isMuted) "Unmute notifications" else "Mute notifications", color = textColor) },
              leadingIcon = { Icon(if (isMuted) Icons.Default.VolumeUp else Icons.Default.VolumeOff, contentDescription = null, tint = textColor) },
              onClick = {
                onToggleMute()
                showMoreMenu = false
              }
            )
            DropdownMenuItem(
              text = { Text("Copy Server IP", color = textColor) },
              leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = textColor) },
              onClick = {
                clipboardManager.setText(AnnotatedString("mc.substratesmp.net:19132"))
                Toast.makeText(context, "Server IP copied to clipboard", Toast.LENGTH_SHORT).show()
                showMoreMenu = false
              }
            )
            DropdownMenuItem(
              text = { Text("Copy Channel Name", color = textColor) },
              leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = textColor) },
              onClick = {
                clipboardManager.setText(AnnotatedString("#${channel.name}"))
                Toast.makeText(context, "Channel name copied", Toast.LENGTH_SHORT).show()
                showMoreMenu = false
              }
            )
            if (isAdmin) {
              DropdownMenuItem(
                text = { Text(if (channel.isRestrictedToAdmin) "Unlock for all members" else "Lock for Admins only", color = textColor) },
                leadingIcon = { Icon(if (channel.isRestrictedToAdmin) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null, tint = RoleAdminGold) },
                onClick = {
                  onTogglePermission(channel)
                  Toast.makeText(context, "Channel permissions updated", Toast.LENGTH_SHORT).show()
                  showMoreMenu = false
                }
              )
            }
          }
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(105.dp)
              .clip(CircleShape)
              .background(Color(0xFF2ECC71)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = initials,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 38.sp
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = channel.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "${members.size} members",
            fontSize = 14.sp,
            color = subTextColor
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Removed Leave button; Message and Mute are neatly centered
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            ChannelActionCard(
              icon = Icons.Default.ChatBubbleOutline,
              label = "Message",
              surfaceColor = surfaceColor,
              textColor = textColor,
              onClick = onNavigateBack
            )
            ChannelActionCard(
              icon = if (isMuted) Icons.Default.NotificationsOff else Icons.Default.NotificationsNone,
              label = if (isMuted) "Unmute" else "Mute",
              surfaceColor = surfaceColor,
              textColor = textColor,
              onClick = onToggleMute
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onAddMembers() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Add Members",
                tint = textColor,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = "Add Members",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(tabs.size) { index ->
              val isSelected = selectedTab == index
              val pillBg = if (isSelected) {
                if (isDarkMode) Color(0xFF005C4B) else Color(0xFFD6F0FF)
              } else {
                Color.Transparent
              }
              val pillText = if (isSelected) {
                if (isDarkMode) Color(0xFFD8FDD2) else Color(0xFF008069)
              } else {
                subTextColor
              }

              Surface(
                shape = RoundedCornerShape(20.dp),
                color = pillBg,
                modifier = Modifier.clickable { selectedTab = index }
              ) {
                Text(
                  text = tabs[index],
                  fontSize = 14.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = pillText,
                  modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
        }
      }

      when (selectedTab) {
        0 -> { // Members Tab
          items(members, key = { it.id }) { member ->
            val status = getMemberStatusString(member.gamertag, member.isAdmin, currentGamertag)
            val avatarBg = getMemberAvatarColor(member.gamertag)

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectMember(member) }
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(avatarBg),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = member.gamertag.take(2).uppercase(),
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = member.gamertag,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = textColor
                )
                Text(
                  text = status.first,
                  fontSize = 13.sp,
                  color = if (status.second) Color(0xFF00A884) else subTextColor
                )
              }
            }
            HorizontalDivider(
              color = if (isDarkMode) Color.Gray.copy(alpha = 0.2f) else Color(0xFFE9EDEF),
              thickness = 0.5.dp,
              modifier = Modifier.padding(start = 62.dp)
            )
          }
        }

        1 -> { // Media Tab
          if (mediaMessages.isEmpty()) {
            item { EmptyMediaPlaceholder("No media shared yet", subTextColor) }
          } else {
            item {
              LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(max = 600.dp)
              ) {
                items(mediaMessages) { msg ->
                  AsyncImage(
                    model = msg.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                      .aspectRatio(1f)
                      .clip(RoundedCornerShape(6.dp))
                      .clickable { msg.imageUrl?.let { onImageClick(it) } },
                    contentScale = ContentScale.Crop
                  )
                }
              }
            }
          }
        }

        2 -> { // Files Tab
          if (fileMessages.isEmpty()) {
            item { EmptyMediaPlaceholder("No files shared yet", subTextColor) }
          } else {
            items(fileMessages) { msg ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color(0xFF7E57C2),
                  modifier = Modifier.size(44.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      Icons.AutoMirrored.Filled.InsertDriveFile,
                      contentDescription = null,
                      tint = Color.White
                    )
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = msg.fileName ?: "Document",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${msg.formattedTime} • by ${msg.senderName}",
                    fontSize = 12.sp,
                    color = subTextColor
                  )
                }
              }
            }
          }
        }

        3 -> { // Links Tab
          if (linkMessages.isEmpty()) {
            item { EmptyMediaPlaceholder("No links shared yet", subTextColor) }
          } else {
            items(linkMessages) { msg ->
              val urlRegex = "(https?://\\S+)".toRegex()
              val match = urlRegex.find(msg.content)?.value ?: msg.content

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    try { uriHandler.openUri(match) } catch (_: Exception) {}
                  }
                  .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color(0xFF2196F3),
                  modifier = Modifier.size(44.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = Color.White)
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = match,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF0288D1),
                    textDecoration = TextDecoration.Underline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${msg.formattedTime} • by ${msg.senderName}",
                    fontSize = 12.sp,
                    color = subTextColor
                  )
                }
              }
            }
          }
        }

        4 -> { // Music / Audio Tab
          if (musicMessages.isEmpty()) {
            item { EmptyMediaPlaceholder("No audio shared yet", subTextColor) }
          } else {
            items(musicMessages) { msg ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color(0xFFE65100),
                  modifier = Modifier.size(44.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = msg.fileName ?: "Voice recording (${msg.audioDurationSeconds}s)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${msg.formattedTime} • by ${msg.senderName}",
                    fontSize = 12.sp,
                    color = subTextColor
                  )
                }
              }
            }
          }
        }

        5 -> { // GIFs / Stickers Tab
          if (gifMessages.isEmpty()) {
            item { EmptyMediaPlaceholder("No stickers or GIFs shared yet", subTextColor) }
          } else {
            item {
              LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(max = 600.dp)
              ) {
                items(gifMessages) { msg ->
                  AsyncImage(
                    model = msg.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                      .aspectRatio(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { msg.imageUrl?.let { onImageClick(it) } },
                    contentScale = ContentScale.Fit
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Working Edit Channel Dialog
  if (showEditDialog) {
    var editName by remember { mutableStateOf(channel.name) }
    var editDesc by remember { mutableStateOf(channel.description) }

    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      containerColor = surfaceColor,
      titleContentColor = textColor,
      textContentColor = textColor,
      title = { Text("Edit Channel Info", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Channel Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = textColor,
              unfocusedTextColor = textColor
            )
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = editDesc,
            onValueChange = { editDesc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = textColor,
              unfocusedTextColor = textColor
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editName.isNotBlank()) {
              onUpdateChannel(channel.id, editName, editDesc)
              showEditDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
        ) {
          Text("Save", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) {
          Text("Cancel", color = subTextColor)
        }
      }
    )
  }
}
