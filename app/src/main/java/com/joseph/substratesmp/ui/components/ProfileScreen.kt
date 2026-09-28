package com.joseph.substratesmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.R
import com.joseph.substratesmp.data.repository.AuthUserState
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  userState: AuthUserState,
  isDarkMode: Boolean,
  onNavigateSettings: () -> Unit,
  onNavigateHome: () -> Unit,
  onUpdateProfile: (String, String) -> Unit,
  onLogin: (String, String, (Result<String>) -> Unit) -> Unit,
  onRegister: (String, String, (Result<String>) -> Unit) -> Unit
) {
  val animState = remember { MutableTransitionState(false) }.apply { targetState = true }
  var showEditDialog by remember { mutableStateOf(false) }

  val isLoggedIn = userState.gamertag.isNotBlank()

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
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isLoggedIn) {
        Icon(Icons.Default.MoreVert, contentDescription = null, tint = textColor)
      }
    }

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
      text = if (isLoggedIn) userState.gamertag else stringResource(R.string.profile_guest),
      fontSize = 22.sp,
      fontWeight = FontWeight.Medium,
      color = textColor
    )
    Text(
      text = if (isLoggedIn) stringResource(R.string.profile_online) else stringResource(R.string.profile_offline),
      fontSize = 14.sp,
      color = subTextColor
    )

    Spacer(modifier = Modifier.height(20.dp))

    if (isLoggedIn) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center
      ) {
        ProfileActionButton(icon = Icons.Default.Edit, label = stringResource(R.string.profile_edit_info), surfaceColor = surfaceColor, textColor = textColor) { showEditDialog = true }
        Spacer(modifier = Modifier.width(16.dp))
        ProfileActionButton(icon = Icons.Default.Settings, label = stringResource(R.string.nav_settings), surfaceColor = surfaceColor, textColor = textColor) { onNavigateSettings() }
      }

      Spacer(modifier = Modifier.height(20.dp))

      AnimatedVisibility(
        visibleState = animState,
        enter = slideInVertically(initialOffsetY = { 300 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
      ) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = surfaceColor),
          elevation = CardDefaults.cardElevation(0.dp),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            ProfileInfoRow(value = userState.bio.ifBlank { stringResource(R.string.profile_no_bio) }, label = stringResource(R.string.profile_bio_label), textColor = textColor, subTextColor = subTextColor)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoRow(value = "@${userState.gamertag}", label = stringResource(R.string.profile_username_label), textColor = textColor, subTextColor = subTextColor)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfoRow(value = userState.birthday.ifBlank { stringResource(R.string.profile_not_set) }, label = stringResource(R.string.profile_birthday_label), textColor = textColor, subTextColor = subTextColor)
          }
        }
      }
    } else {
      var isLoginMode by remember { mutableStateOf(true) }
      var gamertagInput by remember { mutableStateOf("") }
      var passwordInput by remember { mutableStateOf("") }
      var isPasswordVisible by remember { mutableStateOf(false) }
      var isLoading by remember { mutableStateOf(false) }
      var errorMessage by remember { mutableStateOf<String?>(null) }
      
      val inputBgColor = if (isDarkMode) Color(0xFF424242) else Color(0xFFF9FAFB)

      AnimatedVisibility(
        visibleState = animState,
        enter = slideInVertically(initialOffsetY = { 300 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
      ) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = surfaceColor),
          elevation = CardDefaults.cardElevation(0.dp),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = if (isLoginMode) stringResource(R.string.profile_welcome_back) else stringResource(R.string.profile_create_account),
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = textColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
              value = gamertagInput,
              onValueChange = { gamertagInput = it; errorMessage = null },
              label = { Text(stringResource(R.string.profile_gamertag_label)) },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = inputBgColor, focusedContainerColor = inputBgColor,
                focusedTextColor = textColor, unfocusedTextColor = textColor
              )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
              value = passwordInput,
              onValueChange = { passwordInput = it; errorMessage = null },
              label = { Text(stringResource(R.string.profile_password_label)) },
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
              colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = inputBgColor, focusedContainerColor = inputBgColor,
                focusedTextColor = textColor, unfocusedTextColor = textColor
              )
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
              else Text(if (isLoginMode) stringResource(R.string.profile_login) else stringResource(R.string.profile_register), fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = if (isLoginMode) stringResource(R.string.profile_prompt_register) else stringResource(R.string.profile_prompt_login),
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
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      containerColor = surfaceColor,
      titleContentColor = textColor,
      textContentColor = textColor,
      title = { Text(stringResource(R.string.profile_edit_info), fontWeight = FontWeight.Bold) },
      text = {
        Column {
          OutlinedTextField(
            value = editBio,
            onValueChange = { editBio = it },
            label = { Text(stringResource(R.string.profile_bio_label)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
          )
          Spacer(modifier = Modifier.height(12.dp))
          
          OutlinedTextField(
            value = editBirthday.ifBlank { stringResource(R.string.profile_select_date) },
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.profile_birthday_label)) },
            trailingIcon = {
              IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = WhatsAppGreenDark)
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showDatePicker = true },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = textColor,
              unfocusedTextColor = textColor,
              disabledTextColor = textColor
            )
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
        ) { Text(stringResource(R.string.action_save), color = Color.White) }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) { Text(stringResource(R.string.action_cancel), color = subTextColor) }
      }
    )

    if (showDatePicker) {
      DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
          TextButton(onClick = {
            datePickerState.selectedDateMillis?.let { millis ->
              val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = millis
              }
              val sdf = SimpleDateFormat("MMM dd", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("UTC")
              }
              editBirthday = sdf.format(cal.time)
            }
            showDatePicker = false
          }) {
            Text(stringResource(R.string.action_ok), color = WhatsAppGreenDark, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showDatePicker = false }) {
            Text(stringResource(R.string.action_cancel), color = subTextColor)
          }
        },
        colors = DatePickerDefaults.colors(containerColor = surfaceColor)
      ) {
        DatePicker(
          state = datePickerState,
          colors = DatePickerDefaults.colors(
            containerColor = surfaceColor,
            titleContentColor = textColor,
            headlineContentColor = textColor,
            weekdayContentColor = subTextColor,
            yearContentColor = textColor,
            currentYearContentColor = WhatsAppGreenDark,
            selectedYearContentColor = Color.White,
            selectedYearContainerColor = WhatsAppGreenDark,
            dayContentColor = textColor,
            selectedDayContentColor = Color.White,
            selectedDayContainerColor = WhatsAppGreenDark,
            todayContentColor = WhatsAppGreenDark,
            todayDateBorderColor = WhatsAppGreenDark
          )
        )
      }
    }
  }
}

@Composable
fun ProfileActionButton(icon: ImageVector, label: String, surfaceColor: Color, textColor: Color, onClick: () -> Unit) {
  val pillShape = RoundedCornerShape(16.dp)

  Surface(
    shape = pillShape,
    color = surfaceColor,
    shadowElevation = 2.dp,
    onClick = onClick,
    modifier = Modifier
      .width(110.dp)
      .height(70.dp)
      .clip(pillShape)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
  }
}

@Composable
fun ProfileInfoRow(value: String, label: String, textColor: Color, subTextColor: Color) {
  Column {
    Text(text = value, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Normal)
    Text(text = label, fontSize = 13.sp, color = subTextColor)
  }
}
