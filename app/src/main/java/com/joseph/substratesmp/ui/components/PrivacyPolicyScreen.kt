package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen(isDarkMode: Boolean, onNavigateBack: () -> Unit) {
  val bgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF0F2F5)
  val textColor = if (isDarkMode) Color.White else Color.Black
  val subTextColor = if (isDarkMode) Color.LightGray else Color.DarkGray

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
      .statusBarsPadding()
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text("Privacy Policy", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)
    ) {
      Text("Introduction", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        "Substrate SMP values your privacy. This document outlines how we handle your data, the third-party services we rely on, and the security measures in place to protect your information.",
        fontSize = 14.sp, color = subTextColor, lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      Text("Firebase", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        "We use Google Firebase for authentication and real-time database synchronization. Your Gamertag, hashed password, profile information, and chat messages are securely stored in Firestore. Firebase ensures encrypted transmission and secure data storage.",
        fontSize = 14.sp, color = subTextColor, lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      Text("ImgBB", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        "When you upload an image in a chat channel, the image is compressed and temporarily processed on your device before being uploaded to ImgBB. ImgBB acts as our cloud Content Delivery Network to securely host the image and provide a direct link for other users to view it. These uploads are anonymous and disconnected from your personal identity.",
        fontSize = 14.sp, color = subTextColor, lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      Text("Agora.io", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        "Real-time voice and video calls are powered by the Agora RTC engine. When you join a voice or video channel, audio and video streams are transmitted peer-to-peer through Agora's low-latency network. We do not record or store any voice or video calls.",
        fontSize = 14.sp, color = subTextColor, lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      Text("MyMemory Translation API", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        "To support our diverse community, we utilize the MyMemory Translation API. When you tap Translate Message, the text of the selected message is sent to the MyMemory service to be translated into English, Chinese, or Malay. No personal metadata is included in this translation request.",
        fontSize = 14.sp, color = subTextColor, lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      Text("Local Storage", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        "We use Android SharedPreferences to store your session data, app settings (such as dark mode preferences and notification toggles), and blocked users locally on your device.",
        fontSize = 14.sp, color = subTextColor, lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
