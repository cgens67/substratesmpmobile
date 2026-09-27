package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

@Composable
fun UserProfileScreen(
  gamertag: String,
  role: String,
  isAdmin: Boolean,
  isDarkMode: Boolean,
  onNavigateBack: () -> Unit,
  onMessageUser: () -> Unit
) {
  val bgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF0F2F5)
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

    Spacer(modifier = Modifier.height(20.dp))

    Box(
      modifier = Modifier.size(120.dp).clip(CircleShape).background(if (isAdmin) RoleAdminGold else Color(0xFF1C2228)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = gamertag.take(1).uppercase(),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = gamertag,
        fontSize = 26.sp,
        fontWeight = FontWeight.Medium,
        color = textColor
      )
      if (isAdmin) {
        Spacer(modifier = Modifier.width(6.dp))
        Icon(Icons.Default.Shield, contentDescription = "Admin", tint = RoleAdminGold, modifier = Modifier.size(20.dp))
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "Server Role: $role",
      fontSize = 15.sp,
      color = subTextColor
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
      onClick = onMessageUser,
      colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
    ) {
      Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Message", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
  }
}
