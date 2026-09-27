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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
  isMuted: Boolean,
  isFavourite: Boolean,
  isBlocked: Boolean,
  isDarkMode: Boolean,
  onNavigateBack: () -> Unit,
  onMessageUser: () -> Unit,
  onToggleMute: () -> Unit,
  onToggleFavourite: () -> Unit,
  onToggleBlock: () -> Unit
) {
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
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
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
            Icon(Icons.Default.Shield, contentDescription = "Admin", tint = RoleAdminGold, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("ADMIN", color = RoleAdminGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "Bedrock Role: $role",
      fontSize = 14.sp,
      color = subTextColor
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Interactive Actions Row
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      UserActionPill(
        icon = Icons.Default.Chat,
        label = "Message",
        tint = WhatsAppGreenDark,
        surfaceColor = surfaceColor,
        textColor = textColor,
        onClick = onMessageUser
      )
      UserActionPill(
        icon = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
        label = if (isMuted) "Unmute" else "Mute",
        tint = if (isMuted) Color(0xFFEA0038) else WhatsAppGreenDark,
        surfaceColor = surfaceColor,
        textColor = textColor,
        onClick = onToggleMute
      )
      UserActionPill(
        icon = if (isFavourite) Icons.Default.Star else Icons.Default.StarBorder,
        label = if (isFavourite) "Favorited" else "Favorite",
        tint = if (isFavourite) Color(0xFFFFB300) else textColor,
        surfaceColor = surfaceColor,
        textColor = textColor,
        onClick = onToggleFavourite
      )
      UserActionPill(
        icon = Icons.Default.Block,
        label = if (isBlocked) "Unblock" else "Block",
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
        ProfileInfoRow(value = bio.ifBlank { "No bio added." }, label = "Bio", textColor = textColor, subTextColor = subTextColor)
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfoRow(value = "@$gamertag", label = "Username", textColor = textColor, subTextColor = subTextColor)
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfoRow(value = birthday.ifBlank { "Not set" }, label = "Birthday", textColor = textColor, subTextColor = subTextColor)
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
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    shadowElevation = 2.dp,
    modifier = Modifier
      .width(76.dp)
      .height(64.dp)
      .clickable(onClick = onClick)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(4.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor, maxLines = 1)
    }
  }
}
