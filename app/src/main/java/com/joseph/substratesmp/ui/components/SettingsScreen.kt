package com.joseph.substratesmp.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.repository.AuthUserState
import com.joseph.substratesmp.ui.AppSettings
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

data class SettingItemData(
  val id: String,
  val icon: ImageVector,
  val iconBgColor: Color,
  val title: String,
  val subtitle: String
)

@Composable
fun SettingsScreen(
  userState: AuthUserState,
  appSettings: AppSettings,
  isDarkMode: Boolean,
  onUpdateSetting: (String, Any) -> Unit,
  onNavigateBack: () -> Unit,
  onNavigateProfile: () -> Unit
) {
  val animState = remember { MutableTransitionState(false) }.apply { targetState = true }

  var isSearching by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var activeDialog by remember { mutableStateOf<String?>(null) }

  // Dark Mode Colors
  val bgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF0F2F5)
  val surfaceColor = if (isDarkMode) Color(0xFF303030) else Color.White
  val textColor = if (isDarkMode) Color.White else Color.Black
  val subTextColor = if (isDarkMode) Color.LightGray else Color.Gray

  val settingsList = listOf(
    SettingItemData("account", Icons.Default.Person, Color(0xFF1DA1F2), "Account", "Number, Username, Bio"),
    SettingItemData("appearance", Icons.Default.ChatBubble, Color(0xFFF7A23B), "Appearance", "Wallpaper, Dark Mode, Animations"),
    SettingItemData("privacy", Icons.Default.Policy, Color(0xFF27D05B), "Privacy Policy", "Firebase, ImgBB, Agora.io"),
    SettingItemData("language", Icons.Default.Language, Color(0xFFB15DFF), "Language", appSettings.language)
  )

  val filteredSettings = settingsList.filter { 
    it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true) 
  }

  Column(
    modifier = Modifier.fillMaxSize().background(bgColor).statusBarsPadding()
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
      }
      if (isSearching) {
        TextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search settings...", color = subTextColor) },
          singleLine = true,
          modifier = Modifier.weight(1f),
          colors = TextFieldDefaults.colors(
            focusedTextColor = textColor, unfocusedTextColor = textColor,
            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
          )
        )
        IconButton(onClick = { isSearching = false; searchQuery = "" }) {
          Icon(Icons.Default.Close, contentDescription = "Close Search", tint = textColor)
        }
      } else {
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { isSearching = true }) {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = textColor)
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
        Text(userState.gamertag, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = textColor)
        Text("Active User • @${userState.gamertag}", fontSize = 13.sp, color = subTextColor)
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    AnimatedVisibility(
      visibleState = animState,
      enter = slideInVertically(initialOffsetY = { 400 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
    ) {
      LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
          Surface(shape = RoundedCornerShape(24.dp), color = surfaceColor, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
              filteredSettings.forEach { setting ->
                SettingsListItem(
                  icon = setting.icon, iconBgColor = setting.iconBgColor, title = setting.title, subtitle = setting.subtitle,
                  textColor = textColor, subTextColor = subTextColor,
                  onClick = {
                    if (setting.id == "account") onNavigateProfile() else activeDialog = setting.id
                  }
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(120.dp))
        }
      }
    }
  }

  when (activeDialog) {
    "appearance" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = surfaceColor, titleContentColor = textColor, textContentColor = textColor,
        title = { Text("Appearance Settings", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Dark Mode")
              Switch(checked = appSettings.isNightMode, onCheckedChange = { onUpdateSetting("night_mode", it) }, colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Smooth Animations")
              Switch(checked = appSettings.smoothAnimations, onCheckedChange = { onUpdateSetting("animations", it) }, colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark))
            }
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Done", color = WhatsAppGreenDark) } }
      )
    }
    "language" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = surfaceColor, titleContentColor = textColor, textContentColor = textColor,
        title = { Text("Select Language", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            listOf("English", "Chinese", "Malay").forEach { lang ->
              Row(
                modifier = Modifier.fillMaxWidth().clickable { onUpdateSetting("language", lang); activeDialog = null }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(lang, fontSize = 16.sp, color = if (appSettings.language == lang) WhatsAppGreenDark else textColor, fontWeight = if (appSettings.language == lang) FontWeight.Bold else FontWeight.Normal)
              }
            }
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Cancel", color = subTextColor) } }
      )
    }
    "privacy" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null }, containerColor = surfaceColor, titleContentColor = textColor, textContentColor = textColor,
        title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
        text = { 
          Column {
            Text("Substrate SMP uses the following trusted services to provide seamless functionality securely:", fontSize = 14.sp, color = textColor)
            Spacer(modifier = Modifier.height(12.dp))
            Text("🔥 Firebase", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
            Text("Powers real-time chat, authentication, and encrypted profile data syncing.", fontSize = 13.sp, color = subTextColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text("🖼️ ImgBB", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
            Text("Used as a secure cloud CDN to store and process image uploads anonymously.", fontSize = 13.sp, color = subTextColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text("📞 Agora.io", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
            Text("Our low-latency RTC engine for peer-to-peer real-time voice and video calls with secure encryption.", fontSize = 13.sp, color = subTextColor)
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("Close", color = WhatsAppGreenDark) } }
      )
    }
  }
}

@Composable
fun SettingsListItem(icon: ImageVector, iconBgColor: Color, title: String, subtitle: String, textColor: Color, subTextColor: Color, onClick: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBgColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      Text(title, fontSize = 16.sp, fontWeight = FontWeight.Normal, color = textColor)
      Text(subtitle, fontSize = 13.sp, color = subTextColor)
    }
  }
}
