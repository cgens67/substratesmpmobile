package com.joseph.substratesmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.repository.AuthUserState

@Composable
fun SettingsScreen(
  userState: AuthUserState,
  onNavigateBack: () -> Unit
) {
  val animState = remember { MutableTransitionState(false) }.apply { targetState = true }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF0F2F5))
      .statusBarsPadding()
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
      }
      Spacer(modifier = Modifier.weight(1f))
      IconButton(onClick = { }) {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black)
      }
      IconButton(onClick = { }) {
        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Black)
      }
    }

    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(contentAlignment = Alignment.BottomEnd) {
        Box(
          modifier = Modifier.size(90.dp).clip(CircleShape).background(Color(0xFF1C2228)),
          contentAlignment = Alignment.Center
        ) {
          Text(userState.gamertag.take(1).uppercase(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Box(
          modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF00A3FF)).border(2.dp, Color(0xFFF0F2F5), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(userState.gamertag, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color.Black)
      Text("+60 14 6605 680 • @${userState.gamertag}", fontSize = 13.sp, color = Color.Gray)
    }

    Spacer(modifier = Modifier.height(20.dp))

    AnimatedVisibility(
      visibleState = animState,
      enter = slideInVertically(initialOffsetY = { 400 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
    ) {
      LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
          Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
              SettingsListItem(icon = Icons.Default.Person, bgColor = Color(0xFF1DA1F2), title = "Account", subtitle = "Number, Username, Bio")
              SettingsListItem(icon = Icons.Default.ChatBubble, bgColor = Color(0xFFF7A23B), title = "Chat Settings", subtitle = "Wallpaper, Night Mode, Animations")
              SettingsListItem(icon = Icons.Default.VpnKey, bgColor = Color(0xFF27D05B), title = "Privacy & Security", subtitle = "Last Seen, Devices, Passkeys")
              SettingsListItem(icon = Icons.Default.Notifications, bgColor = Color(0xFFF93D3E), title = "Notifications", subtitle = "Sounds, Calls, Badges")
              SettingsListItem(icon = Icons.Default.PieChart, bgColor = Color(0xFF1DA1F2), title = "Data and Storage", subtitle = "Media download settings")
              SettingsListItem(icon = Icons.Default.Folder, bgColor = Color(0xFF1DA1F2), title = "Chat Folders", subtitle = "Sort chats into folders")
              SettingsListItem(icon = Icons.Default.Devices, bgColor = Color(0xFF00C6CC), title = "Devices", subtitle = "Manage connected devices")
              SettingsListItem(icon = Icons.Default.BatteryChargingFull, bgColor = Color(0xFFF7A23B), title = "Power Saving", subtitle = "Reduce power usage on low charge")
              SettingsListItem(icon = Icons.Default.Language, bgColor = Color(0xFFB15DFF), title = "Language", subtitle = "English")
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        item {
          Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF8B54FF)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(16.dp))
              Text("Substrate Premium", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
          }
          Spacer(modifier = Modifier.height(30.dp))
        }
      }
    }
  }
}

@Composable
fun SettingsListItem(icon: ImageVector, bgColor: Color, title: String, subtitle: String) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 20.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier.size(36.dp).clip(CircleShape).background(bgColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      Text(title, fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.Black)
      Text(subtitle, fontSize = 13.sp, color = Color.Gray)
    }
  }
}
