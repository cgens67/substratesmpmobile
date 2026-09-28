package com.joseph.substratesmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark

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

  Dialog(
    onDismissRequest = { if (canDismiss) onDismiss() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = Color(0xFF1E2024),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3238)),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Bedrock SMP Crystal Icon
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
            .border(2.dp, Color(0xFF00E5FF), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Diamond,
            contentDescription = null,
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Substrate SMP",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 22.sp
        )
        Text(
          text = "Minecraft Bedrock Realm Network",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFA0AAB5),
          fontSize = 12.5.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Segmented Switch Pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFF141618),
          modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
          Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isLoginMode) WhatsAppGreenDark else Color.Transparent)
                .clickable {
                  isLoginMode = true
                  errorMessage = null
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Log In",
                fontWeight = FontWeight.Bold,
                color = if (isLoginMode) Color.White else Color(0xFFA0AAB5),
                fontSize = 14.sp
              )
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(if (!isLoginMode) WhatsAppGreenDark else Color.Transparent)
                .clickable {
                  isLoginMode = false
                  errorMessage = null
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Register",
                fontWeight = FontWeight.Bold,
                color = if (!isLoginMode) Color.White else Color(0xFFA0AAB5),
                fontSize = 14.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
          value = gamertagInput,
          onValueChange = {
            gamertagInput = it
            errorMessage = null
          },
          label = { Text("Minecraft Bedrock Gamertag", color = Color(0xFFA0AAB5)) },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00E5FF)) },
          singleLine = true,
          enabled = !isLoading,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF00E5FF),
            unfocusedBorderColor = Color(0xFF383C44),
            focusedContainerColor = Color(0xFF141618),
            unfocusedContainerColor = Color(0xFF141618)
          ),
          modifier = Modifier.fillMaxWidth().testTag("gamertag_input_field"),
          shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = passwordInput,
          onValueChange = {
            passwordInput = it
            errorMessage = null
          },
          label = { Text("Account Password", color = Color(0xFFA0AAB5)) },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E5FF)) },
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                tint = Color(0xFFA0AAB5)
              )
            }
          },
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = {}),
          singleLine = true,
          enabled = !isLoading,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF00E5FF),
            unfocusedBorderColor = Color(0xFF383C44),
            focusedContainerColor = Color(0xFF141618),
            unfocusedContainerColor = Color(0xFF141618)
          ),
          modifier = Modifier.fillMaxWidth().testTag("password_input_field"),
          shape = RoundedCornerShape(14.dp)
        )

        AnimatedVisibility(visible = isSiangAdmin) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = RoleAdminGold.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, RoleAdminGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Server Administrator Account Detected (Siang5680)", color = RoleAdminGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        AnimatedVisibility(visible = errorMessage != null) {
          errorMessage?.let { msg ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFEA0038).copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEA0038).copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
              Text(
                text = msg,
                color = Color(0xFFFF6B81),
                fontSize = 12.5.sp,
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(22.dp))

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
          colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
          }
          Text(
            text = if (isLoginMode) "Log In to Substrate SMP" else "Create Bedrock Account",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 15.sp
          )
        }

        if (canDismiss) {
          Spacer(modifier = Modifier.height(6.dp))
          TextButton(onClick = onDismiss, enabled = !isLoading) {
            Text("Cancel", color = Color(0xFFA0AAB5))
          }
        }
      }
    }
  }
}
