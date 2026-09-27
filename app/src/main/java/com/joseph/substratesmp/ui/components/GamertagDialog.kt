package com.joseph.substratesmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

  AlertDialog(
    onDismissRequest = {
      if (canDismiss) onDismiss()
    },
    title = {
      Text(
        text = if (isLoginMode) "Log In to Substrate SMP" else "Create Bedrock Account",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (isLoginMode) {
            "Log in with your registered Minecraft Bedrock gamertag and password to access channels & recover your profile."
          } else {
            "Create a new Bedrock account with a password so you can easily log back in after app reinstalls."
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = gamertagInput,
          onValueChange = {
            gamertagInput = it
            errorMessage = null
          },
          label = { Text("Minecraft Gamertag") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          singleLine = true,
          enabled = !isLoading,
          modifier = Modifier.fillMaxWidth().testTag("gamertag_input_field"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = passwordInput,
          onValueChange = {
            passwordInput = it
            errorMessage = null
          },
          label = { Text("Account Password") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Toggle password"
              )
            }
          },
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { }),
          singleLine = true,
          enabled = !isLoading,
          modifier = Modifier.fillMaxWidth().testTag("password_input_field"),
          shape = RoundedCornerShape(12.dp)
        )

        AnimatedVisibility(visible = isSiangAdmin) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = RoleAdminGold.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Admin Privileges Assigned (Siang5680)", color = RoleAdminGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        AnimatedVisibility(visible = errorMessage != null) {
          errorMessage?.let { msg ->
            Text(
              text = msg,
              color = MaterialTheme.colorScheme.error,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 8.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = if (isLoginMode) "Don't have an account? " else "Already have an account? ",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (isLoginMode) "Register" else "Log In",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = WhatsAppGreenDark,
            modifier = Modifier.clickable {
              isLoginMode = !isLoginMode
              errorMessage = null
            }
          )
        }
      }
    },
    confirmButton = {
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
        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
      ) {
        if (isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
          Spacer(modifier = Modifier.width(8.dp))
        }
        Text(if (isLoginMode) "Log In" else "Create Account")
      }
    },
    dismissButton = {
      if (canDismiss) {
        TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") }
      }
    }
  )
}
