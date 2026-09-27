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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.repository.AuthUserState
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

@Composable
fun ProfileScreen(
  userState: AuthUserState,
  onNavigateSettings: () -> Unit,
  onNavigateHome: () -> Unit,
  onUpdateProfile: (String, String) -> Unit,
  onLogin: (String, String, (Result<String>) -> Unit) -> Unit,
  onRegister: (String, String, (Result<String>) -> Unit) -> Unit
) {
  val animState = remember { MutableTransitionState(false) }.apply { targetState = true }
  var showEditDialog by remember { mutableStateOf(false) }

  val isLoggedIn = userState.gamertag.isNotBlank()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF0F2F5))
      .statusBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isLoggedIn) {
        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Black)
      }
    }

    // Profile Picture (First Letter only)
    Box(
      modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF1C2228)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = if (isLoggedIn) userState.gamertag.take(1).uppercase() else "?",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = if (isLoggedIn) userState.gamertag else "Guest Profile",
      fontSize = 22.sp,
      fontWeight = FontWeight.Medium,
      color = Color.Black
    )
    Text(
      text = if (isLoggedIn) "online" else "offline",
      fontSize = 14.sp,
      color = Color.Gray
    )

    Spacer(modifier = Modifier.height(20.dp))

    if (isLoggedIn) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center
      ) {
        ProfileActionButton(icon = Icons.Default.Edit, label = "Edit Info") { showEditDialog = true }
        Spacer(modifier = Modifier.width(16.dp))
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
            ProfileInfoRow(value = userState.bio.ifBlank { "No bio added." }, label = "Bio")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoRow(value = "@${userState.gamertag}", label = "Username")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoRow(value = userState.birthday.ifBlank { "Not set" }, label = "Birthday")
          }
        }
      }
    } else {
      // Login / Register UI mapped seamlessly into Profile Screen
      var isLoginMode by remember { mutableStateOf(true) }
      var gamertagInput by remember { mutableStateOf("") }
      var passwordInput by remember { mutableStateOf("") }
      var isPasswordVisible by remember { mutableStateOf(false) }
      var isLoading by remember { mutableStateOf(false) }
      var errorMessage by remember { mutableStateOf<String?>(null) }

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
          Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = if (isLoginMode) "Welcome back!" else "Create your account",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
              value = gamertagInput,
              onValueChange = { gamertagInput = it; errorMessage = null },
              label = { Text("Gamertag") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF9FAFB), focusedContainerColor = Color(0xFFF9FAFB))
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
              value = passwordInput,
              onValueChange = { passwordInput = it; errorMessage = null },
              label = { Text("Password") },
              singleLine = true,
              visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                  Icon(if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                }
              },
              keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF9FAFB), focusedContainerColor = Color(0xFFF9FAFB))
            )

            errorMessage?.let {
              Spacer(modifier = Modifier.height(8.dp))
              Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = {
                if (gamertagInput.isNotBlank() && passwordInput.length >= 6) {
                  isLoading = true
                  val action = if (isLoginMode) onLogin else onRegister
                  action(gamertagInput, passwordInput) { res ->
                    isLoading = false
                    res.onFailure { exc -> errorMessage = exc.message }
                  }
                }
              },
              enabled = !isLoading,
              modifier = Modifier.fillMaxWidth().height(48.dp),
              colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
            ) {
              if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
              else Text(if (isLoginMode) "Log In" else "Register", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = if (isLoginMode) "Don't have an account? Register" else "Already have an account? Log In",
              color = Color(0xFF00A3FF),
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.clickable { isLoginMode = !isLoginMode; errorMessage = null }
            )
          }
        }
      }
    }
  }

  if (showEditDialog) {
    var editBio by remember { mutableStateOf(userState.bio) }
    var editBirthday by remember { mutableStateOf(userState.birthday) }

    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      containerColor = Color.White,
      titleContentColor = Color.Black,
      textContentColor = Color.Black,
      title = { Text("Edit Profile Info", fontWeight = FontWeight.Bold) },
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
        Button(
          colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
          onClick = {
            onUpdateProfile(editBio, editBirthday)
            showEditDialog = false
          }
        ) { Text("Save") }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = Color.Black) }
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
      .width(110.dp)
      .height(70.dp)
      .scale(scale)
      .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
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
