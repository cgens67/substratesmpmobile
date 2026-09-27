package com.joseph.substratesmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.repository.AuthUserState
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
  userState: AuthUserState,
  onNavigateSettings: () -> Unit,
  onNavigateHome: () -> Unit,
  onUpdateProfile: (String, String) -> Unit
) {
  val animState = remember { MutableTransitionState(false) }.apply { targetState = true }
  var showEditDialog by remember { mutableStateOf(false) }

  Scaffold(
    containerColor = Color(0xFFF0F2F5),
    bottomBar = {
      Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().height(60.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          BottomNavItem(icon = Icons.Default.ChatBubbleOutline, label = "Chats", isSelected = false, onClick = onNavigateHome)
          BottomNavItem(icon = Icons.Default.PersonOutline, label = "Contacts", isSelected = false)
          BottomNavItem(icon = Icons.Default.Settings, label = "Settings", isSelected = false, onClick = onNavigateSettings)
          BottomNavItem(
            icon = null,
            label = "Profile",
            isSelected = true,
            customIcon = {
              Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF1C2228)),
                contentAlignment = Alignment.Center
              ) {
                Text(userState.gamertag.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          )
        }
      }
    },
    floatingActionButton = {
      AnimatedVisibility(
        visibleState = animState,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessLow))
      ) {
        FloatingActionButton(
          onClick = { },
          shape = RoundedCornerShape(24.dp),
          containerColor = Color(0xFF00A3FF),
          contentColor = Color.White
        ) {
          Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add a post", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .statusBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.QrCode, contentDescription = "QR", tint = Color.Black)
        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Black)
      }

      Box(
        modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF1C2228)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = userState.gamertag.ifBlank { "User" },
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = userState.gamertag.ifBlank { "Minecraft Player" },
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black
      )
      Text(
        text = "online",
        fontSize = 14.sp,
        color = Color.Gray
      )

      Spacer(modifier = Modifier.height(20.dp))

      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        ProfileActionButton(icon = Icons.Default.CameraAlt, label = "Set Photo") {}
        ProfileActionButton(icon = Icons.Default.Edit, label = "Edit Info") { showEditDialog = true }
        ProfileActionButton(icon = Icons.Default.Settings, label = "Settings") { onNavigateSettings() }
      }

      Spacer(modifier = Modifier.height(20.dp))

      AnimatedVisibility(
        visibleState = animState,
        enter = slideInVertically(initialOffsetY = { 300 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
      ) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(0.dp),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            ProfileInfoRow(value = "+60 14 6605 680", label = "Mobile")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoRow(value = userState.bio.ifBlank { "No bio added." }, label = "Bio")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoRow(value = "@${userState.gamertag}", label = "Username")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoRow(value = userState.birthday.ifBlank { "Not set" }, label = "Birthday")
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      var selectedTab by remember { mutableIntStateOf(0) }
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFE5E7EB).copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp)
      ) {
        Row(modifier = Modifier.padding(4.dp)) {
          TabButton(text = "Posts", isSelected = selectedTab == 0, onClick = { selectedTab = 0 })
          TabButton(text = "Archived Posts", isSelected = selectedTab == 1, onClick = { selectedTab = 1 })
        }
      }
    }
  }

  if (showEditDialog) {
    var editBio by remember { mutableStateOf(userState.bio) }
    var editBirthday by remember { mutableStateOf(userState.birthday) }

    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      title = { Text("Edit Profile Info") },
      text = {
        Column {
          OutlinedTextField(
            value = editBio,
            onValueChange = { editBio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = editBirthday,
            onValueChange = { editBirthday = it },
            label = { Text("Birthday (e.g. Mar 04)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(onClick = {
          onUpdateProfile(editBio, editBirthday)
          showEditDialog = false
        }) { Text("Save") }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun ProfileActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale = if (isPressed) 0.95f else 1f

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    shadowElevation = 2.dp,
    modifier = Modifier
      .width(100.dp)
      .height(64.dp)
      .scale(scale)
      .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
  }
}

@Composable
fun ProfileInfoRow(value: String, label: String) {
  Column {
    Text(text = value, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Normal)
    Text(text = label, fontSize = 13.sp, color = Color.Gray)
  }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = if (isSelected) Color.White else Color.Transparent,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Text(
      text = text,
      color = if (isSelected) Color(0xFF00A3FF) else Color.Gray,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
  }
}

@Composable
fun BottomNavItem(icon: ImageVector?, customIcon: @Composable (() -> Unit)? = null, label: String, isSelected: Boolean, onClick: () -> Unit = {}) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
  ) {
    if (isSelected) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFD6F0FF),
        modifier = Modifier.padding(bottom = 4.dp)
      ) {
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
          if (customIcon != null) customIcon() else Icon(imageVector = icon!!, contentDescription = null, tint = Color(0xFF003859))
        }
      }
    } else {
      if (customIcon != null) customIcon() else Icon(imageVector = icon!!, contentDescription = null, tint = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
    }
    Text(text = label, fontSize = 10.sp, color = if (isSelected) Color(0xFF003859) else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
  }
}
