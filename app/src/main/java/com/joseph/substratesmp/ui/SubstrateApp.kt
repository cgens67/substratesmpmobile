package com.joseph.substratesmp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.data.model.StatusUpdate
import com.joseph.substratesmp.ui.components.ActiveVoiceBar
import com.joseph.substratesmp.ui.components.AdminControlSheet
import com.joseph.substratesmp.ui.components.AgoraSettingsDialog
import com.joseph.substratesmp.ui.components.ChatInputBar
import com.joseph.substratesmp.ui.components.ChatMessageItem
import com.joseph.substratesmp.ui.components.CustomDropdownModalSheet
import com.joseph.substratesmp.ui.components.EmojiPickerView
import com.joseph.substratesmp.ui.components.GamertagDialog
import com.joseph.substratesmp.ui.components.ServerInfoSheet
import com.joseph.substratesmp.ui.components.SheetOption
import com.joseph.substratesmp.ui.components.StatusCreatorDialog
import com.joseph.substratesmp.ui.components.StatusViewerScreen
import com.joseph.substratesmp.ui.components.VideoCallScreen
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.RoleAdminGoldContainer
import com.joseph.substratesmp.ui.theme.WhatsAppChatBackground
import com.joseph.substratesmp.ui.theme.WhatsAppChipUnselected
import com.joseph.substratesmp.ui.theme.WhatsAppDivider
import com.joseph.substratesmp.ui.theme.WhatsAppGreen
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppGreenTeal
import com.joseph.substratesmp.ui.theme.WhatsAppHeaderGreen
import com.joseph.substratesmp.ui.theme.WhatsAppNavSelectedPill
import com.joseph.substratesmp.ui.theme.WhatsAppSearchBackground
import com.joseph.substratesmp.ui.theme.WhatsAppTextPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstrateApp(
  viewModel: SubstrateViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current

  val channels by viewModel.channels.collectAsStateWithLifecycle()
  val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()
  val userState by viewModel.userState.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val statuses by viewModel.statuses.collectAsStateWithLifecycle()
  val members by viewModel.members.collectAsStateWithLifecycle()
  val activeVoiceRoom by viewModel.activeVoiceRoom.collectAsStateWithLifecycle()
  val agoraSettings by viewModel.agoraSettings.collectAsStateWithLifecycle()

  val showGamertagDialog by viewModel.showGamertagDialog.collectAsStateWithLifecycle()
  val showAgoraDialog by viewModel.showAgoraDialog.collectAsStateWithLifecycle()
  val showServerInfoSheet by viewModel.showServerInfoSheet.collectAsStateWithLifecycle()
  val showAdminConsole by viewModel.showAdminConsole.collectAsStateWithLifecycle()

  var currentScreen by remember { mutableStateOf("home") } // "home", "chat_screen", or "video_call_screen"
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Chats, 1: Updates, 2: Calls
  var activeFilterChip by remember { mutableStateOf("All") }
  var searchQuery by remember { mutableStateOf("") }
  var showMenuDropdownSheet by remember { mutableStateOf(false) }

  // Status creation and viewing
  var showCreateStatusDialog by remember { mutableStateOf(false) }
  var viewedStatus by remember { mutableStateOf<StatusUpdate?>(null) }

  // Chat message & emoji state
  var chatInputText by remember { mutableStateOf("") }
  var showEmojiPicker by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  BackHandler(enabled = currentScreen == "chat_screen" || currentScreen == "video_call_screen") {
    if (showEmojiPicker) {
      showEmojiPicker = false
    } else if (currentScreen == "video_call_screen") {
      currentScreen = if (activeVoiceRoom?.isConnected == true) "chat_screen" else "home"
    } else {
      currentScreen = "home"
    }
  }

  // Permission Launcher for Agora Calls
  var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
      pendingAction?.invoke()
    }
    pendingAction = null
  }

  fun runWithPermissions(action: () -> Unit) {
    val audioGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    if (audioGranted && cameraGranted) {
      action()
    } else {
      pendingAction = action
      permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))
    }
  }

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Box(modifier = modifier.fillMaxSize().background(Color.White)) {
    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = {
        if (targetState == "chat_screen" || targetState == "video_call_screen") {
          (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it } + fadeIn())
            .togetherWith(slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { -it / 3 } + fadeOut())
        } else {
          (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { -it / 3 } + fadeIn())
            .togetherWith(slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it } + fadeOut())
        }
      },
      label = "screen_transition"
    ) { screen ->
      when (screen) {
        "video_call_screen" -> {
          activeVoiceRoom?.let { room ->
            VideoCallScreen(
              voiceRoom = room,
              voiceManager = viewModel.voiceManager,
              onDisconnect = {
                viewModel.voiceManager.disconnect()
                currentScreen = "home"
              }
            )
          } ?: run {
            currentScreen = "home"
          }
        }

        "chat_screen" -> {
          Scaffold(
            topBar = {
              TopAppBar(
                title = {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.setServerInfoSheetVisible(true) }
                  ) {
                    Box(
                      modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFE9EDEF)),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(activeChannel.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = WhatsAppGreenDark)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(activeChannel.name, fontWeight = FontWeight.Bold, color = WhatsAppTextPrimary, fontSize = 17.sp)
                        if (activeChannel.isRestrictedToAdmin) {
                          Spacer(modifier = Modifier.width(4.dp))
                          Icon(Icons.Default.Lock, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(13.dp))
                        }
                      }
                      Text("mc.substratesmp.net", color = WhatsAppTextSecondary, fontSize = 11.5.sp)
                    }
                  }
                },
                navigationIcon = {
                  IconButton(onClick = {
                    showEmojiPicker = false
                    currentScreen = "home"
                  }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhatsAppTextPrimary)
                  }
                },
                actions = {
                  IconButton(
                    onClick = {
                      val vc = channels.find { it.type == ChannelType.VOICE } ?: channels.last()
                      runWithPermissions {
                        viewModel.startVideoCall(vc)
                        currentScreen = "video_call_screen"
                      }
                    }
                  ) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = WhatsAppTextPrimary)
                  }

                  IconButton(
                    onClick = {
                      val vc = channels.find { it.type == ChannelType.VOICE } ?: channels.last()
                      runWithPermissions { viewModel.selectChannel(vc) }
                    }
                  ) {
                    Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = WhatsAppTextPrimary)
                  }

                  IconButton(onClick = { showMenuDropdownSheet = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = WhatsAppTextPrimary)
                  }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
              )
            },
            contentWindowInsets = WindowInsets.statusBars
          ) { chatPadding ->
            Box(
              modifier = Modifier.fillMaxSize().padding(chatPadding).background(WhatsAppChatBackground)
            ) {
              Column(
                modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding()
              ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                  if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.9f)) {
                        Text(
                          "Messages in #${activeChannel.name} are synchronized in real-time.",
                          color = WhatsAppTextSecondary,
                          style = MaterialTheme.typography.labelSmall,
                          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                      }
                    }
                  } else {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
                      item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                          Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.95f)) {
                            Text("Today", color = WhatsAppTextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                          }
                        }
                      }

                      items(messages, key = { it.id }) { message ->
                        ChatMessageItem(message = message, modifier = Modifier.animateItem())
                      }
                    }
                  }
                }

                AnimatedVisibility(
                  visible = activeVoiceRoom != null,
                  enter = slideInVertically { it },
                  exit = slideOutVertically { it }
                ) {
                  activeVoiceRoom?.let { room ->
                    ActiveVoiceBar(
                      voiceRoom = room,
                      onToggleMute = { viewModel.voiceManager.toggleMute() },
                      onToggleSpeaker = { viewModel.voiceManager.toggleSpeaker() },
                      onToggleDeafen = { viewModel.voiceManager.toggleDeafen() },
                      onToggleCamera = {
                        viewModel.voiceManager.toggleCamera()
                        if (viewModel.voiceManager.voiceRoomState.value?.isCameraOn == true) {
                          currentScreen = "video_call_screen"
                        }
                      },
                      onDisconnect = { viewModel.voiceManager.disconnect() }
                    )
                  }
                }

                if (activeChannel.isRestrictedToAdmin && !userState.isAdmin) {
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF7F8FA),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                  ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Lock, contentDescription = null, tint = RoleAdminGold)
                      Spacer(modifier = Modifier.width(10.dp))
                      Text("Only server Admins can send messages in this channel.", style = MaterialTheme.typography.bodySmall, color = WhatsAppTextSecondary)
                    }
                  }
                } else {
                  ChatInputBar(
                    channelName = activeChannel.name,
                    text = chatInputText,
                    onTextChanged = { chatInputText = it },
                    onSendMessage = { content, coords ->
                      viewModel.sendMessage(content, coords)
                      chatInputText = ""
                    },
                    isEmojiPickerVisible = showEmojiPicker,
                    onToggleEmojiPicker = {
                      showEmojiPicker = !showEmojiPicker
                      if (showEmojiPicker) keyboardController?.hide()
                    },
                    onTextFieldFocused = {
                      if (showEmojiPicker) showEmojiPicker = false
                    }
                  )

                  AnimatedVisibility(
                    visible = showEmojiPicker,
                    enter = expandVertically(animationSpec = tween(220)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(220)) + fadeOut()
                  ) {
                    EmojiPickerView(
                      onEmojiSelected = { emoji -> chatInputText += emoji },
                      onBackspace = { if (chatInputText.isNotEmpty()) chatInputText = chatInputText.dropLast(1) }
                    )
                  }
                }
              }
            }
          }
        }

        else -> {
          Scaffold(
            topBar = {
              Column(modifier = Modifier.background(Color.White)) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("WhatsApp", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = WhatsAppHeaderGreen, fontSize = 25.sp)

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (userState.isAdmin) {
                      IconButton(onClick = { viewModel.setAdminConsoleVisible(true) }) {
                        Icon(Icons.Default.Shield, contentDescription = "Admin Console", tint = RoleAdminGold)
                      }
                    }
                    IconButton(onClick = { showMenuDropdownSheet = true }) {
                      Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = WhatsAppTextPrimary)
                    }
                  }
                }

                if (selectedTab == 0) {
                  Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = WhatsAppSearchBackground,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).height(44.dp)
                  ) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Search, contentDescription = "Search", tint = WhatsAppTextSecondary, modifier = Modifier.size(20.dp))
                      Spacer(modifier = Modifier.width(10.dp))
                      BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = WhatsAppTextPrimary, fontSize = 15.sp),
                        singleLine = true,
                        decorationBox = { inner ->
                          if (searchQuery.isEmpty()) Text("Search", style = MaterialTheme.typography.bodyMedium, color = WhatsAppTextSecondary, fontSize = 15.sp)
                          inner()
                        }
                      )
                      if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                          Icon(Icons.Default.Close, contentDescription = "Clear", tint = WhatsAppTextSecondary, modifier = Modifier.size(16.dp))
                        }
                      }
                    }
                  }

                  val filterChips = listOf("All", "Unread", "Favourites", "Groups")
                  LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterChips) { chip ->
                      val isSelected = activeFilterChip == chip
                      Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) WhatsAppNavSelectedPill else WhatsAppChipUnselected,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9EDEF)),
                        modifier = Modifier.clickable { activeFilterChip = chip }
                      ) {
                        Text(chip, color = if (isSelected) WhatsAppGreenDark else WhatsAppTextSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontSize = 13.sp)
                      }
                    }
                  }
                }
              }
            },
            bottomBar = {
              NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                  selected = selectedTab == 0,
                  onClick = { selectedTab = 0 },
                  icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                  label = { Text("Chats", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                  colors = NavigationBarItemDefaults.colors(selectedIconColor = WhatsAppGreenDark, indicatorColor = WhatsAppNavSelectedPill)
                )
                NavigationBarItem(
                  selected = selectedTab == 1,
                  onClick = { selectedTab = 1 },
                  icon = { Icon(Icons.Default.DonutLarge, contentDescription = "Updates") },
                  label = { Text("Updates", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                  colors = NavigationBarItemDefaults.colors(selectedIconColor = WhatsAppGreenDark, indicatorColor = WhatsAppNavSelectedPill)
                )
                NavigationBarItem(
                  selected = selectedTab == 2,
                  onClick = { selectedTab = 2 },
                  icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                  label = { Text("Calls", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                  colors = NavigationBarItemDefaults.colors(selectedIconColor = WhatsAppGreenDark, indicatorColor = WhatsAppNavSelectedPill)
                )
              }
            },
            floatingActionButton = {
              FloatingActionButton(
                onClick = {
                  if (selectedTab == 1) {
                    showCreateStatusDialog = true
                  } else if (selectedTab == 2) {
                    val vc = channels.find { it.type == ChannelType.VOICE } ?: channels.last()
                    runWithPermissions { viewModel.selectChannel(vc) }
                  } else {
                    viewModel.setServerInfoSheetVisible(true)
                  }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = WhatsAppGreenTeal,
                contentColor = Color.White
              ) {
                Icon(
                  imageVector = if (selectedTab == 1) Icons.Default.Add else if (selectedTab == 2) Icons.Default.Call else Icons.Default.Chat,
                  contentDescription = "Action",
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color.White)) {
              AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                  if (targetState > initialState) {
                    (slideInHorizontally(animationSpec = tween(240, easing = FastOutSlowInEasing)) { it } + fadeIn())
                      .togetherWith(slideOutHorizontally(animationSpec = tween(240, easing = FastOutSlowInEasing)) { -it } + fadeOut())
                  } else {
                    (slideInHorizontally(animationSpec = tween(240, easing = FastOutSlowInEasing)) { -it } + fadeIn())
                      .togetherWith(slideOutHorizontally(animationSpec = tween(240, easing = FastOutSlowInEasing)) { it } + fadeOut())
                  }
                },
                label = "tab_transition"
              ) { tabIndex ->
                when (tabIndex) {
                  0 -> {
                    val filtered = channels.filter { it.type == ChannelType.TEXT }.filter { ch ->
                      if (searchQuery.isBlank()) true else ch.name.contains(searchQuery, true) || ch.description.contains(searchQuery, true)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
                      item {
                        Row(
                          modifier = Modifier.fillMaxWidth().clickable { viewModel.setGamertagDialogVisible(true) }.padding(horizontal = 16.dp, vertical = 10.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(if (userState.isAdmin) RoleAdminGold else Color(0xFF1F2C34)),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(if (userState.gamertag.isNotBlank()) userState.gamertag.take(1).uppercase() else "Y", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                          }
                          Spacer(modifier = Modifier.width(14.dp))
                          Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                              Text(if (userState.gamertag.isNotBlank()) "@${userState.gamertag} (You)" else "Log In / Register", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                              if (userState.isAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = RoleAdminGoldContainer) {
                                  Text("ADMIN", color = RoleAdminGold, fontWeight = FontWeight.ExtraBold, fontSize = 8.5.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                              }
                            }
                            Text("Bedrock SMP Account • Tap to switch or log in", color = WhatsAppTextSecondary, fontSize = 13.sp)
                          }
                        }
                        HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 82.dp))
                      }

                      items(filtered) { channel ->
                        Row(
                          modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.selectChannel(channel)
                            currentScreen = "chat_screen"
                          }.padding(horizontal = 16.dp, vertical = 12.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(if (channel.id == "announcements") Color(0xFFEA0038).copy(alpha = 0.12f) else WhatsAppNavSelectedPill),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(channel.name.take(1).uppercase(), color = if (channel.id == "announcements") Color(0xFFEA0038) else WhatsAppGreenDark, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                          }
                          Spacer(modifier = Modifier.width(14.dp))
                          Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                              Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(channel.name, fontWeight = FontWeight.SemiBold, color = WhatsAppTextPrimary, fontSize = 16.5.sp)
                                if (channel.isRestrictedToAdmin) {
                                  Spacer(modifier = Modifier.width(4.dp))
                                  Icon(Icons.Default.Lock, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(13.dp))
                                }
                              }
                              Text("11:37 am", color = WhatsAppTextSecondary, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                              Text(channel.description, color = WhatsAppTextSecondary, fontSize = 13.5.sp, maxLines = 1, modifier = Modifier.weight(1f))
                            }
                          }
                        }
                        HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 82.dp))
                      }
                    }
                  }

                  1 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
                      item {
                        Text("Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(14.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                          item {
                            Card(
                              shape = RoundedCornerShape(16.dp),
                              colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
                              modifier = Modifier.size(width = 110.dp, height = 160.dp).clickable { showCreateStatusDialog = true }
                            ) {
                              Box(modifier = Modifier.fillMaxSize()) {
                                Box(
                                  modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).size(50.dp).clip(CircleShape).background(Color(0xFF1F2C34)),
                                  contentAlignment = Alignment.Center
                                ) {
                                  Text(userState.gamertag.take(1).ifBlank { "Y" }.uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                  Box(
                                    modifier = Modifier.align(Alignment.BottomEnd).size(18.dp).clip(CircleShape).background(WhatsAppGreen),
                                    contentAlignment = Alignment.Center
                                  ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                  }
                                }
                                Text("Add status", color = WhatsAppTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
                              }
                            }
                          }

                          items(statuses) { status ->
                            val cardBg = when (status.backgroundTheme) {
                              "CRIMSON" -> Color(0xFFFF4500)
                              "END_VOID" -> Color(0xFF6A0DAD)
                              "DIAMOND" -> Color(0xFF00E5FF)
                              "GOLDEN" -> Color(0xFFFFD700)
                              "OBSIDIAN" -> Color(0xFF273142)
                              else -> WhatsAppNavSelectedPill
                            }

                            Card(
                              shape = RoundedCornerShape(16.dp),
                              colors = CardDefaults.cardColors(containerColor = cardBg),
                              modifier = Modifier.size(width = 110.dp, height = 160.dp).clickable { viewedStatus = status }
                            ) {
                              Box(modifier = Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                  Text(status.activityTag.take(2), fontSize = 28.sp)
                                  Spacer(modifier = Modifier.height(6.dp))
                                  Text(status.authorGamertag, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (status.backgroundTheme == "EMERALD") WhatsAppGreenDark else Color.White, maxLines = 1)
                                  Text(status.content, fontSize = 10.sp, color = if (status.backgroundTheme == "EMERALD") WhatsAppTextSecondary else Color.White.copy(alpha = 0.85f), maxLines = 2)
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }

                  2 -> {
                    val voiceChannels = channels.filter { it.type == ChannelType.VOICE }
                    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
                      item {
                        Text("Active Realm Voice & Video Rooms", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Low-latency Agora RTC engine with camera previews & boosted audio", color = WhatsAppTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                      }

                      items(voiceChannels) { channel ->
                        val isCurrent = activeVoiceRoom?.channelId == channel.id
                        val participantsInRoom = if (isCurrent) activeVoiceRoom?.participants ?: emptyList() else emptyList()

                        Card(
                          shape = RoundedCornerShape(16.dp),
                          colors = CardDefaults.cardColors(containerColor = if (isCurrent) WhatsAppNavSelectedPill.copy(alpha = 0.5f) else Color(0xFFF7F8FA)),
                          modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                          Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween,
                              verticalAlignment = Alignment.CenterVertically
                            ) {
                              Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(WhatsAppNavSelectedPill), contentAlignment = Alignment.Center) {
                                  Text("🔊", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                  Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                  Text(channel.description, color = WhatsAppTextSecondary, fontSize = 12.sp)
                                }
                              }

                              Row {
                                IconButton(onClick = {
                                  runWithPermissions {
                                    viewModel.startVideoCall(channel)
                                    currentScreen = "video_call_screen"
                                  }
                                }) {
                                  Icon(Icons.Default.Videocam, contentDescription = "Video", tint = WhatsAppGreenDark)
                                }
                                IconButton(onClick = {
                                  if (isCurrent) {
                                    viewModel.voiceManager.disconnect()
                                  } else {
                                    runWithPermissions { viewModel.selectChannel(channel) }
                                  }
                                }) {
                                  Icon(if (isCurrent) Icons.Default.CallEnd else Icons.Default.Call, contentDescription = "Voice", tint = if (isCurrent) Color(0xFFEA0038) else WhatsAppGreenDark)
                                }
                              }
                            }

                            if (participantsInRoom.isNotEmpty()) {
                              Spacer(modifier = Modifier.height(8.dp))
                              Text("In this call right now (${participantsInRoom.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WhatsAppGreenDark)
                              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                items(participantsInRoom) { p ->
                                  Surface(shape = RoundedCornerShape(8.dp), color = Color.White) {
                                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                      Text(if (p.isSpeaking) "🟢" else "⚪", fontSize = 8.sp)
                                      Spacer(modifier = Modifier.width(4.dp))
                                      Text(p.name, fontSize = 11.sp, fontWeight = if (p.isAdmin) FontWeight.Bold else FontWeight.Normal, color = if (p.isAdmin) RoleAdminGold else Color.Black)
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Gemini-Style Bottom Sheet Dropdown Menu
  if (showMenuDropdownSheet) {
    CustomDropdownModalSheet(
      options = listOf(
        SheetOption("profile", "Account Profile", "Gamertag: ${userState.gamertag.ifBlank { "Not set" }}", isSelected = false),
        SheetOption("server", "Bedrock Server IP", "mc.substratesmp.net:19132", isSelected = true),
        SheetOption("agora", "Voice Engine Settings", "App ID: ${agoraSettings.appId.take(8)}...", isSelected = false),
        if (userState.isAdmin) SheetOption("admin", "Admin Control Console", "Manage channels and player roles", isSelected = false) else null
      ).filterNotNull(),
      onDismiss = { showMenuDropdownSheet = false },
      onOptionSelected = { option ->
        when (option.id) {
          "profile" -> viewModel.setGamertagDialogVisible(true)
          "server" -> viewModel.setServerInfoSheetVisible(true)
          "agora" -> viewModel.setAgoraDialogVisible(true)
          "admin" -> viewModel.setAdminConsoleVisible(true)
        }
      }
    )
  }

  // Admin Control Sheet
  if (showAdminConsole && userState.isAdmin) {
    AdminControlSheet(
      channels = channels,
      members = members,
      onDismiss = { viewModel.setAdminConsoleVisible(false) },
      onCreateChannel = { name, type, desc, onlyAdmin -> viewModel.createChannel(name, type, desc, onlyAdmin) },
      onDeleteChannel = { id -> viewModel.deleteChannel(id) },
      onToggleChannelPermission = { ch -> viewModel.toggleChannelPermission(ch) },
      onUpdateMemberRole = { id, role -> viewModel.updateMemberRole(id, role) },
      onUpdateMemberGamertag = { id, name -> viewModel.updateMemberGamertag(id, name) },
      onRemoveMember = { id -> viewModel.removeMember(id) }
    )
  }

  // Add Real Status Dialog
  if (showCreateStatusDialog) {
    StatusCreatorDialog(
      onDismiss = { showCreateStatusDialog = false },
      onPostStatus = { text, theme, activity, coords ->
        viewModel.postStatus(text, theme, activity, coords)
        showCreateStatusDialog = false
      }
    )
  }

  // WhatsApp-Style Full-Screen Status Viewer
  viewedStatus?.let { status ->
    StatusViewerScreen(
      status = status,
      isOwnStatus = status.authorGamertag == userState.gamertag,
      onDismiss = { viewedStatus = null },
      onDelete = {
        viewModel.deleteStatus(status.id)
        viewedStatus = null
      },
      onReact = { emoji ->
        viewModel.reactToStatus(status.id, emoji)
      }
    )
  }

  // Gamertag Login & Registration Dialog
  if (showGamertagDialog || userState.needsGamertagSetup) {
    GamertagDialog(
      currentGamertag = userState.gamertag,
      onDismiss = { viewModel.setGamertagDialogVisible(false) },
      onLogin = { gamertag, pass, onComplete ->
        viewModel.loginAccount(gamertag, pass, onComplete)
      },
      onRegister = { gamertag, pass, onComplete ->
        viewModel.registerAccount(gamertag, pass, onComplete)
      }
    )
  }

  if (showAgoraDialog) {
    AgoraSettingsDialog(
      currentSettings = agoraSettings,
      onDismiss = { viewModel.setAgoraDialogVisible(false) },
      onSave = { appId, appCert, token ->
        viewModel.voiceManager.updateSettings(appId, appCert, token)
        viewModel.setAgoraDialogVisible(false)
      }
    )
  }

  if (showServerInfoSheet) {
    ServerInfoSheet(onDismiss = { viewModel.setServerInfoSheetVisible(false) })
  }
}
