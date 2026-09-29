package com.joseph.substratesmp.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.R
import com.joseph.substratesmp.data.repository.AuthUserState
import com.joseph.substratesmp.ui.AppSettings
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

data class SettingItemData(
  val id: String,
  val icon: ImageVector,
  val iconBgColor: Color,
  val titleRes: Int = 0,
  val subtitleRes: Int = 0,
  val titleText: String? = null,
  val subtitleText: String? = null,
  val dynamicSubtitle: String? = null
)

@Composable
fun SettingsScreen(
  userState: AuthUserState,
  appSettings: AppSettings,
  isDarkMode: Boolean,
  onUpdateSetting: (String, Any) -> Unit,
  onNavigateBack: () -> Unit,
  onNavigateProfile: () -> Unit,
  onNavigatePrivacy: () -> Unit
) {
  var showChangelog by rememberSaveable { mutableStateOf(false) }

  AnimatedContent(
    targetState = showChangelog,
    transitionSpec = {
      if (targetState) {
        (slideInHorizontally(animationSpec = spring(stiffness = 400f)) { it } + fadeIn())
          .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = 400f)) { -it / 3 } + fadeOut())
      } else {
        (slideInHorizontally(animationSpec = spring(stiffness = 400f)) { -it / 3 } + fadeIn())
          .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = 400f)) { it } + fadeOut())
      }
    },
    label = "settingsToChangelogTransition"
  ) { displayingChangelog ->
    if (displayingChangelog) {
      BackHandler { showChangelog = false }
      ChangelogScreen(
        onDismiss = { showChangelog = false }
      )
    } else {
      MainSettingsContent(
        userState = userState,
        appSettings = appSettings,
        isDarkMode = isDarkMode,
        onUpdateSetting = onUpdateSetting,
        onNavigateBack = onNavigateBack,
        onNavigateProfile = onNavigateProfile,
        onNavigatePrivacy = onNavigatePrivacy,
        onOpenChangelog = { showChangelog = true }
      )
    }
  }
}

@Composable
private fun MainSettingsContent(
  userState: AuthUserState,
  appSettings: AppSettings,
  isDarkMode: Boolean,
  onUpdateSetting: (String, Any) -> Unit,
  onNavigateBack: () -> Unit,
  onNavigateProfile: () -> Unit,
  onNavigatePrivacy: () -> Unit,
  onOpenChangelog: () -> Unit
) {
  val animState = remember { MutableTransitionState(false) }.apply { targetState = true }

  var isSearching by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var activeDialog by remember { mutableStateOf<String?>(null) }

  val bgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF0F2F5)
  val surfaceColor = if (isDarkMode) Color(0xFF303030) else Color.White
  val textColor = if (isDarkMode) Color.White else Color.Black
  val subTextColor = if (isDarkMode) Color.LightGray else Color.Gray

  val settingsList = listOf(
    SettingItemData(
      id = "account",
      icon = Icons.Default.Person,
      iconBgColor = Color(0xFF1DA1F2),
      titleRes = R.string.settings_account,
      subtitleRes = R.string.settings_account_sub
    ),
    SettingItemData(
      id = "appearance",
      icon = Icons.Default.ChatBubble,
      iconBgColor = Color(0xFFF7A23B),
      titleRes = R.string.settings_appearance,
      subtitleRes = R.string.settings_appearance_sub
    ),
    SettingItemData(
      id = "privacy",
      icon = Icons.Default.Policy,
      iconBgColor = Color(0xFF27D05B),
      titleRes = R.string.settings_privacy_policy,
      subtitleRes = R.string.settings_privacy_policy_sub
    ),
    SettingItemData(
      id = "changelog",
      icon = Icons.Default.History,
      iconBgColor = Color(0xFF007AFF),
      titleText = "Changelog",
      subtitleText = "Releases, beta updates & commit history"
    ),
    SettingItemData(
      id = "language",
      icon = Icons.Default.Language,
      iconBgColor = Color(0xFFB15DFF),
      titleRes = R.string.settings_language,
      subtitleRes = R.string.settings_language,
      dynamicSubtitle = appSettings.language
    )
  )

  Column(
    modifier = Modifier.fillMaxSize().background(bgColor).statusBarsPadding()
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = textColor)
      }
      if (isSearching) {
        TextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text(stringResource(R.string.settings_search_hint), color = subTextColor) },
          singleLine = true,
          modifier = Modifier.weight(1f),
          colors = TextFieldDefaults.colors(
            focusedTextColor = textColor, unfocusedTextColor = textColor,
            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
          )
        )
        IconButton(onClick = { isSearching = false; searchQuery = "" }) {
          Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear), tint = textColor)
        }
      } else {
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { isSearching = true }) {
          Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search), tint = textColor)
        }
      }
    }

    if (!isSearching) {
      Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier.size(90.dp).clip(CircleShape).background(Color(0xFF1C2228)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (userState.gamertag.isNotBlank()) userState.gamertag.take(1).uppercase() else "?",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = if (userState.gamertag.isNotBlank()) userState.gamertag else stringResource(R.string.profile_guest),
          fontSize = 22.sp,
          fontWeight = FontWeight.Medium,
          color = textColor
        )
        Text(
          text = if (userState.gamertag.isNotBlank()) "@${userState.gamertag}" else "",
          fontSize = 13.sp,
          color = subTextColor
        )
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
              settingsList.forEach { setting ->
                val title = if (setting.titleRes != 0) stringResource(setting.titleRes) else (setting.titleText ?: "")
                val subtitle = setting.dynamicSubtitle 
                    ?: if (setting.subtitleRes != 0) stringResource(setting.subtitleRes) else (setting.subtitleText ?: "")

                if (searchQuery.isBlank() || title.contains(searchQuery, true) || subtitle.contains(searchQuery, true)) {
                  SettingsListItem(
                    icon = setting.icon,
                    iconBgColor = setting.iconBgColor,
                    title = title,
                    subtitle = subtitle,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    onClick = {
                      when (setting.id) {
                        "account" -> onNavigateProfile()
                        "privacy" -> onNavigatePrivacy()
                        "changelog" -> onOpenChangelog()
                        else -> activeDialog = setting.id
                      }
                    }
                  )
                }
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
        onDismissRequest = { activeDialog = null },
        containerColor = surfaceColor,
        titleContentColor = textColor,
        textContentColor = textColor,
        title = { Text(stringResource(R.string.settings_appearance_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(stringResource(R.string.settings_dark_mode), color = textColor)
              Switch(checked = appSettings.isNightMode, onCheckedChange = { onUpdateSetting("night_mode", it) }, colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(stringResource(R.string.settings_smooth_animations), color = textColor)
              Switch(checked = appSettings.smoothAnimations, onCheckedChange = { onUpdateSetting("animations", it) }, colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark))
            }
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text(stringResource(R.string.action_done), color = WhatsAppGreenDark) } }
      )
    }
    "language" -> {
      AlertDialog(
        onDismissRequest = { activeDialog = null },
        containerColor = surfaceColor,
        titleContentColor = textColor,
        textContentColor = textColor,
        title = { Text(stringResource(R.string.settings_language_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_auto_translate_title), fontWeight = FontWeight.Bold, color = textColor, fontSize = 15.sp)
                Text(stringResource(R.string.settings_auto_translate_sub), fontSize = 12.sp, color = subTextColor)
              }
              Switch(
                checked = appSettings.autoTranslate,
                onCheckedChange = { onUpdateSetting("auto_translate", it) },
                colors = SwitchDefaults.colors(checkedTrackColor = WhatsAppGreenDark)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = subTextColor.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            Text(stringResource(R.string.settings_select_language), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = subTextColor)

            listOf(
              "English" to stringResource(R.string.lang_english),
              "Chinese" to stringResource(R.string.lang_chinese),
              "Malay" to stringResource(R.string.lang_malay)
            ).forEach { (langCode, langDisplay) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable {
                    onUpdateSetting("language", langCode)
                    activeDialog = null
                  }
                  .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = langDisplay,
                  fontSize = 16.sp,
                  color = if (appSettings.language.equals(langCode, true)) WhatsAppGreenDark else textColor,
                  fontWeight = if (appSettings.language.equals(langCode, true)) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }
        },
        confirmButton = { TextButton(onClick = { activeDialog = null }) { Text(stringResource(R.string.action_done), color = WhatsAppGreenDark) } }
      )
    }
  }
}

@Composable
fun SettingsListItem(icon: ImageVector, iconBgColor: Color, title: String, subtitle: String, textColor: Color, subTextColor: Color, onClick: () -> Unit) {
  val pillShape = RoundedCornerShape(12.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(pillShape)
      .clickable(onClick = onClick)
      .padding(horizontal = 20.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(iconBgColor),
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
