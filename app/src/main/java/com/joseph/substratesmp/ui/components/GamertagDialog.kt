package com.joseph.substratesmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.joseph.substratesmp.ui.theme.RoleAdminGold

@Composable
fun GamertagDialog(
  currentGamertag: String,
  onDismiss: () -> Unit,
  onLogin: (gamertag: String, pass: String, onComplete: (Result<String>) -> Unit) -> Unit,
  onRegister: (gamertag: String, pass: String, onComplete: (Result<String>) -> Unit) -> Unit
) {
  var isLoginMode by remember { mutableStateOf(currentGamertag.isBlank()) }
  var gamertagInput by remember { mutableStateOf(currentGamertag) }
  var passwordInput by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  val canDismiss = currentGamertag.isNotBlank() && !isLoading
  val isSiangAdmin = gamertagInput.trim().equals("Siang5680", ignoreCase = true)

  // Authentic iOS colors
  val iosCardBg = Color(0xFF1C1C1E)
  val iosGroupedFieldBg = Color(0xFF2C2C2E)
  val iosSeparator = Color(0x38545458)
  val iosSegmentBg = Color(0xFF2C2C2E)
  val iosSegmentSelected = Color(0xFF636366)
  val iosBlue = Color(0xFF007AFF)

  Dialog(
    onDismissRequest = { if (canDismiss) onDismiss() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = iosCardBg,
      modifier = Modifier
        .fillMaxWidth(0.88f)
        .padding(vertical = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = if (isLoginMode) "Sign In" else "Create Account",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 20.sp
        )
        Text(
          text = "Substrate SMP Bedrock Realm",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF8E8E93),
          fontSize = 13.sp,
          modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Native iOS Segmented Control
        Surface(
          shape = RoundedCornerShape(9.dp),
          color = iosSegmentBg,
          modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(2.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(7.dp))
                .background(if (isLoginMode) iosSegmentSelected else Color.Transparent)
                .clickable {
                  isLoginMode = true
                  errorMessage = null
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Log In",
                fontWeight = if (isLoginMode) FontWeight.SemiBold else FontWeight.Normal,
                color = Color.White,
                fontSize = 13.sp
              )
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(7.dp))
                .background(if (!isLoginMode) iosSegmentSelected else Color.Transparent)
                .clickable {
                  isLoginMode = false
                  errorMessage = null
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Register",
                fontWeight = if (!isLoginMode) FontWeight.SemiBold else FontWeight.Normal,
                color = Color.White,
                fontSize = 13.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // iOS Grouped Inset Text Fields with Middle Divider
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = iosGroupedFieldBg,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Gamertag", color = Color(0xFF8E8E93), fontSize = 15.sp, modifier = Modifier.width(82.dp))
              BasicTextField(
                value = gamertagInput,
                onValueChange = {
                  gamertagInput = it
                  errorMessage = null
                },
                singleLine = true,
                enabled = !isLoading,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 15.sp),
                modifier = Modifier.weight(1f).testTag("gamertag_input_field"),
                decorationBox = { inner ->
                  if (gamertagInput.isEmpty()) Text("Minecraft Name", color = Color(0xFF636366), fontSize = 15.sp)
                  inner()
                }
              )
            }

            HorizontalDivider(color = iosSeparator, thickness = 0.5.dp, modifier = Modifier.padding(start = 14.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Password", color = Color(0xFF8E8E93), fontSize = 15.sp, modifier = Modifier.width(82.dp))
              BasicTextField(
                value = passwordInput,
                onValueChange = {
                  passwordInput = it
                  errorMessage = null
                },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {}),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 15.sp),
                modifier = Modifier.weight(1f).testTag("password_input_field"),
                decorationBox = { inner ->
                  if (passwordInput.isEmpty()) Text("Required", color = Color(0xFF636366), fontSize = 15.sp)
                  inner()
                }
              )
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }, modifier = Modifier.size(28.dp)) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = null,
                  tint = Color(0xFF8E8E93),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        AnimatedVisibility(visible = isSiangAdmin) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 10.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Admin Account (Siang5680)", color = RoleAdminGold, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
          }
        }

        AnimatedVisibility(visible = errorMessage != null) {
          errorMessage?.let { msg ->
            Text(
              text = msg,
              color = Color(0xFFFF453A),
              fontSize = 12.5.sp,
              modifier = Modifier.padding(top = 10.dp, start = 4.dp).fillMaxWidth()
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // iOS Blue Action Button
        Button(
          onClick = {
            if (gamertagInput.trim().isNotBlank() && passwordInput.isNotBlank()) {
              isLoading = true
              errorMessage = null
              val action = if (isLoginMode) onLogin else onRegister
              action(gamertagInput.trim(), passwordInput) { res ->
                isLoading = false
                res.onFailure { exc ->
                  errorMessage = exc.message ?: "Authentication failed."
                }
              }
            }
          },
          enabled = gamertagInput.trim().length >= 3 && passwordInput.length >= 6 && !isLoading,
          colors = ButtonDefaults.buttonColors(containerColor = iosBlue),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
          }
          Text(
            text = if (isLoginMode) "Log In" else "Create Account",
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            fontSize = 16.sp
          )
        }

        if (canDismiss) {
          Spacer(modifier = Modifier.height(6.dp))
          TextButton(onClick = onDismiss, enabled = !isLoading) {
            Text("Cancel", color = iosBlue, fontSize = 15.sp)
          }
        }
      }
    }
  }
}
