package com.joseph.substratesmp.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.repository.AuthUserState
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

data class SettingItemData(
  val id: String,
  val icon: ImageVector,
  val bgColor: Color,
  val title: String,
  val subtitle: String
)

@Composable
fun SettingsScreen(
  userState: AuthUserState,
  onNavigateBack: () -> Unit,
  onNavigateProfile: () -> Unit
) {
  val context = LocalContext.current
  val prefs = context.getSharedPreferences("substrate_settings_prefs", Context.MODE_PRIVATE)
  val animState = remember { MutableTransitionState(false) }.apply { targetState = true }

  var isSearching by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var activeDialog by remember { mutableStateOf<String?>(null) }

  // Persistent States
  var isNightMode by remember { mutableStateOf(prefs.getBoolean("night_mode", false)) }
  var isAnimations by remember { mutableStateOf(prefs.getBoolean("animations", true)) }
  var isNotifications by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
  var isAutoDownload by remember { mutableStateOf(prefs.getBoolean("auto_download", true)) }
  var isPowerSaving by remember { mutableStateOf(prefs.getBoolean("power_saving", false)) }
  var selectedLang by remember { mutableStateOf(prefs.getString("language", "English") ?: "English") }

  fun savePref(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }
  fun savePref(key: String, value: String) { prefs.edit().putString(key, value).apply() }

  val settingsList = listOf(
    SettingItemData("account", Icons.Default.Person, Color(0xFF1DA1F2), "Account", "Number, Username, Bio"),
    SettingItemData("chat", Icons.Default.ChatBubble, Color(0xFFF7A23B), "Chat Settings", "Wallpaper, Night Mode, Animations"),
    SettingItemData("privacy", Icons.Default.VpnKey, Color(0xFF27D05B), "Privacy & Security", "Last Seen, Devices, Passkeys"),
    SettingItemData("notifications", Icons.Default.Notifications, Color(0xFFF93D3E), "Notifications", "Sounds, Calls, Badges"),
    SettingItemData("data", Icons.Default.PieChart, Color(0xFF1DA1F2), "Data and Storage", "Media download settings"),
    SettingItemData("folders", Icons.Default.Folder, Color(0xFF1DA1F2), "Chat Folders", "Sort chats into folders"),
    SettingItemData("devices", Icons.Default.Devices, Color(0xFF00C6CC), "Devices", "Manage connected devices"),
    SettingItemData("power", Icons.Default.BatteryChargingFull, Color(0xFFF7A23B), "Power Saving", "Reduce power usage on low charge"),
    SettingItemData("language", Icons.Default.Language, Color(0xFFB15DFF), "Language", selectedLang)
  )

  val filteredSettings = settingsList.filter { 
    it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true) 
  }

  Column(
    modifier = Modifier.fillMaxSize().background(Color(0xFFF0F2F5)).statusBarsPadding()
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
      }
      if (isSearching) {
        TextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search settings...") },
          singleLine = true,
          modifier = Modifier.weight(1f),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
          )
        )
        IconButton(onClick = { isSearching = false; searchQuery = "" }) {
          Icon(Icons.Default.Close, contentDescription = "Close Search", tint = Color.Black)
        }
      } else {
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { isSearching = true }) {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black)
        }
      }
    }

    if (!isSearching) {
      Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier.size(90.dp).clip(CircleShape).background(Color(0xFF1C2228)),
          contentAlignment = Alignment.Center
        ) {
          Text(userState.gamertag.take(1).uppercase(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(userState.gamertag, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        Text("Active User • @${userState.gamertag}", fontSize = 13.sp, color = Color.Gray)
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    AnimatedVisibility(
      visibleState = animState,
      enter = slideInVertically(initialOffsetY = { 400 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
    ) {
      LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
          Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
              filteredSettings.forEach { setting ->
                SettingsListItem(
                  icon = setting.icon, bgColor = setting.bgColor, title = setting.title, subtitle = setting.subtitle,
                  onClick = {
                    if (setting.id == "account") onNavigateProfile() else activeDialog = setting.id
                  }
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(120.dp)) // padding for floating bar
        }
      }
    }
  }

  // Functional Dialogs
  when (activeDialog) {
    "chat" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = Color.White, titleContentColor = Color.Black, textContentColor = Color.Black,
        title = { Text("Chat Settings", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Night Mode")
              Switch(checked = isNightMode, onCheckedChange = { isNightMode = it; savePref("night_mode", it) }, colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Smooth Animations")
              Switch(checked = isAnimations, onCheckedChange = { isAnimations = it; savePref("animations", it) }, colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark))
            }
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Done", color = WhatsAppGreenDark) } }
      )
    }
    "notifications" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = Color.White, titleContentColor = Color.Black, textContentColor = Color.Black,
        title = { Text("Notifications", fontWeight = FontWeight.Bold) },
        text = {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Notifications")
            Switch(checked = isNotifications, onCheckedChange = { isNotifications = it; savePref("notifications", it) }, colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark))
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Done", color = WhatsAppGreenDark) } }
      )
    }
    "data" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = Color.White, titleContentColor = Color.Black, textContentColor = Color.Black,
        title = { Text("Data & Storage", fontWeight = FontWeight.Bold) },
        text = {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Auto-Download Media")
            Switch(checked = isAutoDownload, onCheckedChange = { isAutoDownload = it; savePref("auto_download", it) }, colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark))
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Done", color = WhatsAppGreenDark) } }
      )
    }
    "power" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = Color.White, titleContentColor = Color.Black, textContentColor = Color.Black,
        title = { Text("Power Saving", fontWeight = FontWeight.Bold) },
        text = {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Low Power Mode")
            Switch(checked = isPowerSaving, onCheckedChange = { isPowerSaving = it; savePref("power_saving", it) }, colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark))
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Done", color = WhatsAppGreenDark) } }
      )
    }
    "language" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = Color.White, titleContentColor = Color.Black, textContentColor = Color.Black,
        title = { Text("Select Language", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            listOf("English", "Chinese", "Malay").forEach { lang ->
              Row(
                modifier = Modifier.fillMaxWidth().clickable { selectedLang = lang; savePref("language", lang); activeDialog = null }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(lang, fontSize = 16.sp, color = if (selectedLang == lang) WhatsAppGreenDark else Color.Black, fontWeight = if (selectedLang == lang) FontWeight.Bold else FontWeight.Normal)
              }
            }
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Cancel", color = Color.Gray) } }
      )
    }
    "privacy" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = Color.White, titleContentColor = Color.Black, textContentColor = Color.Black,
        title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
        text = { Text("Manage passkeys, blocked users, and device sessions securely.") },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Close", color = WhatsAppGreenDark) } }
      )
    }
    "devices" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = Color.White, titleContentColor = Color.Black, textContentColor = Color.Black,
        title = { Text("Linked Devices", fontWeight = FontWeight.Bold) },
        text = { Text("Current Device: Android 14 Smartphone\nLocation: Localhost") },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Close", color = WhatsAppGreenDark) } }
      )
    }
    "folders" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = Color.White, titleContentColor = Color.Black, textContentColor = Color.Black,
        title = { Text("Chat Folders", fontWeight = FontWeight.Bold) },
        text = { Text("You can add specific chats to your Favourites for quick access from the home screen filter.") },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Got it", color = WhatsAppGreenDark) } }
      )
    }
  }
}

@Composable
fun SettingsListItem(icon: ImageVector, bgColor: Color, title: String, subtitle: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
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
