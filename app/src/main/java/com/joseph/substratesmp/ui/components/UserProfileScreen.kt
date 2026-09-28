package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.R
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.RoleAdminGoldContainer
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

@Composable
fun UserProfileScreen(
  gamertag: String,
  role: String,
  isAdmin: Boolean,
  bio: String,
  birthday: String,
  lastCoordinates: String = "",
  lastCoordinatesTimestamp: Long = 0L,
  isMuted: Boolean,
  isFavourite: Boolean,
  isBlocked: Boolean,
  isDarkMode: Boolean,
  onNavigateBack: () -> Unit,
  onMessageUser: () -> Unit,
  onRequestLocation: () -> Unit = {},
  onToggleMute: () -> Unit,
  onToggleFavourite: () -> Unit,
  onToggleBlock: () -> Unit
) {
  val clipboardManager = LocalClipboardManager.current

  val bgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF0F2F5)
  val surfaceColor = if (isDarkMode) Color(0xFF303030) else Color.White
  val textColor = if (isDarkMode) Color.White else Color.Black
  val subTextColor = if (isDarkMode) Color.LightGray else Color.Gray

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
      .statusBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = textColor)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Box(
      modifier = Modifier.size(100.dp).clip(CircleShape).background(if (isAdmin) RoleAdminGold else Color(0xFF1C2228)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = gamertag.take(1).uppercase(),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = gamertag,
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        color = textColor
      )
      if (isAdmin) {
        Spacer(modifier = Modifier.width(6.dp))
        Surface(shape = RoundedCornerShape(4.dp), color = RoleAdminGoldContainer) {
          Row(modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(stringResource(R.string.admin_badge), color = RoleAdminGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = stringResource(R.string.profile_role_prefix, role),
      fontSize = 14.sp,
      color = subTextColor
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Interactive Action Pills
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      UserActionPill(
        icon = Icons.Default.Chat,
        label = stringResource(R.string.action_message),
        tint = WhatsAppGreenDark,
        surfaceColor = surfaceColor,
        textColor = textColor,
        onClick = onMessageUser
      )
      UserActionPill(
        icon = Icons.Default.Place,
        label = stringResource(R.string.action_request_location),
        tint = Color(0xFF0288D1),
        surfaceColor = surfaceColor,
        textColor = textColor,
        onClick = onRequestLocation
      )
      UserActionPill(
        icon = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
        label = if (isMuted) stringResource(R.string.user_action_unmute) else stringResource(R.string.user_action_mute),
        tint = if (isMuted) Color(0xFFEA0038) else WhatsAppGreenDark,
        surfaceColor = surfaceColor,
        textColor = textColor,
        onClick = onToggleMute
      )
      UserActionPill(
        icon = if (isFavourite) Icons.Default.Star else Icons.Default.StarBorder,
        label = if (isFavourite) stringResource(R.string.user_action_favorited) else stringResource(R.string.user_action_favorite),
        tint = if (isFavourite) Color(0xFFFFB300) else textColor,
        surfaceColor = surfaceColor,
        textColor = textColor,
        onClick = onToggleFavourite
      )
      UserActionPill(
        icon = Icons.Default.Block,
        label = if (isBlocked) stringResource(R.string.user_action_unblock) else stringResource(R.string.user_action_block),
        tint = if (isBlocked) Color(0xFFEA0038) else textColor,
        surfaceColor = surfaceColor,
        textColor = textColor,
        onClick = onToggleBlock
      )
    }

    Spacer(modifier = Modifier.height(22.dp))

    // Profile Details Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = surfaceColor),
      elevation = CardDefaults.cardElevation(0.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
    ) {
      Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
        ProfileInfoRow(
          value = bio.ifBlank { stringResource(R.string.profile_no_bio) },
          label = stringResource(R.string.profile_bio_label),
          textColor = textColor,
          subTextColor = subTextColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfoRow(
          value = "@$gamertag",
          label = stringResource(R.string.profile_username_label),
          textColor = textColor,
          subTextColor = subTextColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfoRow(
          value = birthday.ifBlank { stringResource(R.string.profile_not_set) },
          label = stringResource(R.string.profile_birthday_label),
          textColor = textColor,
          subTextColor = subTextColor
        )

        // Minecraft Coordinates Card
        Spacer(modifier = Modifier.height(16.dp))
        Column {
          Text(
            text = stringResource(R.string.attach_location),
            fontSize = 13.sp,
            color = subTextColor
          )
          Spacer(modifier = Modifier.height(4.dp))
          if (lastCoordinates.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.Black.copy(alpha = 0.08f),
              modifier = Modifier.clickable {
                clipboardManager.setText(AnnotatedString(lastCoordinates))
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = WhatsAppGreenDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = lastCoordinates,
                  style = CoordinateTextStyle,
                  color = WhatsAppGreenDark,
                  fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = subTextColor, modifier = Modifier.size(14.dp))
              }
            }
          } else {
            Text(
              text = stringResource(R.string.location_no_coords),
              fontSize = 14.sp,
              color = subTextColor
            )
          }
        }
      }
    }
  }
}

@Composable
fun UserActionPill(
  icon: ImageVector,
  label: String,
  tint: Color,
  surfaceColor: Color,
  textColor: Color,
  onClick: () -> Unit
) {
  val pillShape = RoundedCornerShape(16.dp)
  Surface(
    shape = pillShape,
    color = surfaceColor,
    shadowElevation = 2.dp,
    onClick = onClick,
    modifier = Modifier
      .width(66.dp)
      .height(64.dp)
      .clip(pillShape)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(2.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = textColor, maxLines = 1)
    }
  }
}
