package com.joseph.substratesmp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.ui.components.ActiveVoiceBar
import com.joseph.substratesmp.ui.components.AgoraSettingsDialog
import com.joseph.substratesmp.ui.components.ChatInputBar
import com.joseph.substratesmp.ui.components.ChatMessageItem
import com.joseph.substratesmp.ui.components.EmojiPickerView
import com.joseph.substratesmp.ui.components.GamertagDialog
import com.joseph.substratesmp.ui.components.ServerInfoSheet
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
  val activeVoiceRoom by viewModel.activeVoiceRoom.collectAsStateWithLifecycle()
  val agoraSettings by viewModel.agoraSettings.collectAsStateWithLifecycle()

  val showGamertagDialog by viewModel.showGamertagDialog.collectAsStateWithLifecycle()
  val showAgoraDialog by viewModel.showAgoraDialog.collectAsStateWithLifecycle()
  val showServerInfoSheet by viewModel.showServerInfoSheet.collectAsStateWithLifecycle()

  var currentScreen by remember { mutableStateOf("home") } // "home" or "chat_screen"
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Chats, 1: Updates, 2: Calls
  var activeFilterChip by remember { mutableStateOf("All") }
  var searchQuery by remember { mutableStateOf("") }
  var menuExpanded by remember { mutableStateOf(false) }

  // Chat message & emoji state
  var chatInputText by remember { mutableStateOf("") }
  var showEmojiPicker by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  // Hardware/gesture back press handler
  BackHandler(enabled = currentScreen == "chat_screen") {
    if (showEmojiPicker) {
      showEmojiPicker = false
    } else {
      currentScreen = "home"
    }
  }

  // Permission Launcher for Agora Calls
  var pendingVoiceChannel by remember { mutableStateOf<Channel?>(null) }
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
    if (audioGranted) {
      pendingVoiceChannel?.let { ch ->
        viewModel.selectChannel(ch)
      }
    }
    pendingVoiceChannel = null
  }

  fun requestCallPermissionsAndJoin(channel: Channel) {
    val audioCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
    val cameraCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)

    if (audioCheck == PackageManager.PERMISSION_GRANTED && cameraCheck == PackageManager.PERMISSION_GRANTED) {
      viewModel.selectChannel(channel)
    } else {
      pendingVoiceChannel = channel
      permissionLauncher.launch(
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
      )
    }
  }

  // Keyboard observer to ensure typing bar stays above software keyboard
  val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
  LaunchedEffect(imeBottomPadding, messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Box(modifier = modifier.fillMaxSize().background(Color.White)) {
    // Screen Navigation with Smooth Horizontal Slide Transition
    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = {
        if (targetState == "chat_screen") {
          (slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(animationSpec = tween(280)))
            .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { -it / 3 } + fadeOut(animationSpec = tween(280)))
        } else {
          (slideInHorizontally(animationSpec = tween(280)) { -it / 3 } + fadeIn(animationSpec = tween(280)))
            .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { it } + fadeOut(animationSpec = tween(280)))
        }
      },
      label = "screen_transition"
    ) { screen ->
      if (screen == "chat_screen") {
        // SCREEN: Conversation Screen
        Scaffold(
          topBar = {
            TopAppBar(
              title = {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.clickable { viewModel.setServerInfoSheetVisible(true) }
                ) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .clip(CircleShape)
                      .background(Color(0xFFE9EDEF)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = activeChannel.name.take(1).uppercase(),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = WhatsAppGreenDark
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = activeChannel.name,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = WhatsAppTextPrimary,
                      fontSize = 17.sp
                    )
                    Text(
                      text = "mc.substratesmp.net • 14 online",
                      style = MaterialTheme.typography.labelSmall,
                      color = WhatsAppTextSecondary,
                      fontSize = 11.5.sp
                    )
                  }
                }
              },
              navigationIcon = {
                IconButton(onClick = {
                  showEmojiPicker = false
                  currentScreen = "home"
                }) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WhatsAppTextPrimary
                  )
                }
              },
              actions = {
                IconButton(
                  onClick = {
                    val voiceChannel = channels.find { it.type == ChannelType.VOICE } ?: channels.last()
                    requestCallPermissionsAndJoin(voiceChannel)
                  }
                ) {
                  Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video Call",
                    tint = WhatsAppTextPrimary
                  )
                }

                IconButton(
                  onClick = {
                    val voiceChannel = channels.find { it.type == ChannelType.VOICE } ?: channels.last()
                    requestCallPermissionsAndJoin(voiceChannel)
                  }
                ) {
                  Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Voice Call",
                    tint = WhatsAppTextPrimary
                  )
                }

                Box {
                  IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                      imageVector = Icons.Default.MoreVert,
                      contentDescription = "Options",
                      tint = WhatsAppTextPrimary
                    )
                  }
                  DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = Color.White
                  ) {
                    DropdownMenuItem(
                      text = { Text("Gamertag: ${userState.gamertag.ifBlank { "Setup" }}") },
                      onClick = {
                        menuExpanded = false
                        viewModel.setGamertagDialogVisible(true)
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("Agora Audio Settings") },
                      onClick = {
                        menuExpanded = false
                        viewModel.setAgoraDialogVisible(true)
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("Bedrock Server Info") },
                      onClick = {
                        menuExpanded = false
                        viewModel.setServerInfoSheetVisible(true)
                      }
                    )
                  }
                }
              },
              colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
          },
          contentWindowInsets = WindowInsets.statusBars
        ) { chatPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(chatPadding)
              .background(WhatsAppChatBackground)
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val step = 80f
              for (x in 0..(size.width.toInt()) step step.toInt()) {
                for (y in 0..(size.height.toInt()) step step.toInt()) {
                  drawCircle(
                    color = Color.Black.copy(alpha = 0.015f),
                    radius = 2.5f,
                    center = Offset(x.toFloat(), y.toFloat())
                  )
                }
              }
            }

            Column(
              modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
            ) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth()
              ) {
                if (messages.isEmpty()) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = Color.White.copy(alpha = 0.9f),
                      shadowElevation = 1.dp
                    ) {
                      Text(
                        text = "Messages in #${activeChannel.name} are end-to-end encrypted.",
                        color = WhatsAppTextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                      )
                    }
                  }
                } else {
                  LazyColumn(
                    state = listState,
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(vertical = 4.dp)
                  ) {
                    item {
                      Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                      ) {
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = Color.White.copy(alpha = 0.95f),
                          shadowElevation = 1.dp
                        ) {
                          Text(
                            text = "Today",
                            color = WhatsAppTextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Medium
                          )
                        }
                      }
                    }

                    items(
                      items = messages,
                      key = { it.id }
                    ) { message ->
                      ChatMessageItem(message = message)
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
                    onToggleCamera = { viewModel.voiceManager.toggleCamera() },
                    onDisconnect = { viewModel.voiceManager.disconnect() }
                  )
                }
              }

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
                  if (showEmojiPicker) {
                    keyboardController?.hide()
                  }
                },
                onTextFieldFocused = {
                  if (showEmojiPicker) {
                    showEmojiPicker = false
                  }
                }
              )

              AnimatedVisibility(
                visible = showEmojiPicker,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(220)) + fadeOut()
              ) {
                EmojiPickerView(
                  onEmojiSelected = { emoji ->
                    chatInputText += emoji
                  },
                  onBackspace = {
                    if (chatInputText.isNotEmpty()) {
                      chatInputText = chatInputText.dropLast(1)
                    }
                  }
                )
              }
            }
          }
        }
      } else {
        // SCREEN: Main Tabs (Chats | Updates | Calls)
        Scaffold(
          topBar = {
            Column(modifier = Modifier.background(Color.White)) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "WhatsApp",
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                  color = WhatsAppHeaderGreen,
                  fontSize = 25.sp,
                  letterSpacing = (-0.4).sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                  IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                      imageVector = Icons.Default.MoreVert,
                      contentDescription = "Menu",
                      tint = WhatsAppTextPrimary
                    )
                  }

                  DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = Color.White
                  ) {
                    DropdownMenuItem(
                      text = { Text("Gamertag: ${userState.gamertag.ifBlank { "Setup" }}") },
                      onClick = {
                        menuExpanded = false
                        viewModel.setGamertagDialogVisible(true)
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("Agora Call Settings") },
                      onClick = {
                        menuExpanded = false
                        viewModel.setAgoraDialogVisible(true)
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("Bedrock Realm Details") },
                      onClick = {
                        menuExpanded = false
                        viewModel.setServerInfoSheetVisible(true)
                      }
                    )
                  }
                }
              }

              if (selectedTab == 0) {
                // Working WhatsApp Search Bar without Meta AI
                Surface(
                  shape = RoundedCornerShape(24.dp),
                  color = WhatsAppSearchBackground,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(44.dp)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Search,
                      contentDescription = "Search",
                      tint = WhatsAppTextSecondary,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                      value = searchQuery,
                      onValueChange = { searchQuery = it },
                      modifier = Modifier.weight(1f),
                      textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = WhatsAppTextPrimary,
                        fontSize = 15.sp
                      ),
                      singleLine = true,
                      decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                          Text(
                            text = "Search",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WhatsAppTextSecondary,
                            fontSize = 15.sp
                          )
                        }
                        innerTextField()
                      }
                    )
                    if (searchQuery.isNotEmpty()) {
                      IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(24.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = "Clear search",
                          tint = WhatsAppTextSecondary,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  }
                }

                // Interactive Filter Chips: All | Unread | Favourites | Groups
                val filterChips = listOf("All", "Unread", "Favourites", "Groups")
                LazyRow(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  items(filterChips) { chip ->
                    val isSelected = activeFilterChip == chip
                    Surface(
                      shape = RoundedCornerShape(18.dp),
                      color = if (isSelected) WhatsAppNavSelectedPill else WhatsAppChipUnselected,
                      border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9EDEF)),
                      modifier = Modifier.clickable { activeFilterChip = chip }
                    ) {
                      Text(
                        text = chip,
                        color = if (isSelected) WhatsAppGreenDark else WhatsAppTextSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 13.sp
                      )
                    }
                  }
                  item {
                    Box(
                      modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(WhatsAppChipUnselected)
                        .border(1.dp, Color(0xFFE9EDEF), CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Filter",
                        tint = WhatsAppTextSecondary,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }
              }
            }
          },
          bottomBar = {
            // WhatsApp Bottom Bar: Chats | Updates | Calls
            NavigationBar(
              containerColor = Color.White,
              tonalElevation = 8.dp
            ) {
              NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = {
                  Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chats"
                  )
                },
                label = { Text("Chats", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = WhatsAppGreenDark,
                  selectedTextColor = WhatsAppGreenDark,
                  indicatorColor = WhatsAppNavSelectedPill,
                  unselectedIconColor = WhatsAppTextSecondary,
                  unselectedTextColor = WhatsAppTextSecondary
                )
              )

              NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = {
                  Icon(
                    imageVector = Icons.Default.DonutLarge,
                    contentDescription = "Updates"
                  )
                },
                label = { Text("Updates", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = WhatsAppGreenDark,
                  selectedTextColor = WhatsAppGreenDark,
                  indicatorColor = WhatsAppNavSelectedPill,
                  unselectedIconColor = WhatsAppTextSecondary,
                  unselectedTextColor = WhatsAppTextSecondary
                )
              )

              NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = {
                  Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Calls"
                  )
                },
                label = { Text("Calls", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = WhatsAppGreenDark,
                  selectedTextColor = WhatsAppGreenDark,
                  indicatorColor = WhatsAppNavSelectedPill,
                  unselectedIconColor = WhatsAppTextSecondary,
                  unselectedTextColor = WhatsAppTextSecondary
                )
              )
            }
          },
          floatingActionButton = {
            FloatingActionButton(
              onClick = {
                if (selectedTab == 2) {
                  val voiceChannel = channels.find { it.type == ChannelType.VOICE } ?: channels.last()
                  requestCallPermissionsAndJoin(voiceChannel)
                } else {
                  viewModel.setServerInfoSheetVisible(true)
                }
              },
              shape = RoundedCornerShape(16.dp),
              containerColor = WhatsAppGreenTeal,
              contentColor = Color.White,
              elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
            ) {
              Icon(
                imageVector = if (selectedTab == 2) Icons.Default.Call else Icons.Default.Chat,
                contentDescription = "Action",
                modifier = Modifier.size(24.dp)
              )
            }
          }
        ) { innerPadding ->
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
              .background(Color.White)
          ) {
            // Smooth Tab Content Animation
            AnimatedContent(
              targetState = selectedTab,
              transitionSpec = {
                if (targetState > initialState) {
                  (slideInHorizontally(animationSpec = tween(220)) { it } + fadeIn())
                    .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { -it } + fadeOut())
                } else {
                  (slideInHorizontally(animationSpec = tween(220)) { -it } + fadeIn())
                    .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { it } + fadeOut())
                }
              },
              label = "tab_transition"
            ) { tabIndex ->
              when (tabIndex) {
                0 -> {
                  // TAB 0: CHATS LIST WITH LIVE SEARCH FILTER
                  val filteredTextChannels = channels
                    .filter { it.type == ChannelType.TEXT }
                    .filter { channel ->
                      when (activeFilterChip) {
                        "Unread" -> channel.unreadCount > 0
                        "Favourites" -> channel.id == "general-chat" || channel.id == "announcements"
                        else -> true
                      }
                    }
                    .filter { channel ->
                      if (searchQuery.isBlank()) true
                      else channel.name.contains(searchQuery, ignoreCase = true) ||
                           channel.description.contains(searchQuery, ignoreCase = true)
                    }

                  LazyColumn(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(Color.White)
                  ) {
                    item {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable { viewModel.setGamertagDialogVisible(true) }
                          .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Box(
                          modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (userState.isAdmin) RoleAdminGold else Color(0xFF1F2C34)),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = if (userState.gamertag.isNotBlank()) userState.gamertag.take(1).uppercase() else "Y",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                          )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                              text = if (userState.gamertag.isNotBlank()) "@${userState.gamertag} (You)" else "Choose Gamertag",
                              style = MaterialTheme.typography.titleMedium,
                              fontWeight = FontWeight.SemiBold,
                              color = WhatsAppTextPrimary,
                              fontSize = 16.sp
                            )
                            if (userState.isAdmin) {
                              Spacer(modifier = Modifier.width(6.dp))
                              Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RoleAdminGoldContainer
                              ) {
                                Text(
                                  text = "ADMIN",
                                  color = RoleAdminGold,
                                  style = MaterialTheme.typography.labelSmall,
                                  fontWeight = FontWeight.ExtraBold,
                                  fontSize = 8.5.sp,
                                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                              }
                            }
                          }
                          Text(
                            text = "Bedrock Player Presence • Tap to edit profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = WhatsAppTextSecondary,
                            fontSize = 13.sp
                          )
                        }
                      }
                      HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 82.dp))
                    }

                    if (filteredTextChannels.isEmpty() && searchQuery.isNotBlank()) {
                      item {
                        Box(
                          modifier = Modifier.fillMaxWidth().padding(32.dp),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = "No chats found matching \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WhatsAppTextSecondary
                          )
                        }
                      }
                    } else {
                      items(filteredTextChannels) { channel ->
                        Row(
                          modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                              viewModel.selectChannel(channel)
                              currentScreen = "chat_screen"
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Box(
                            modifier = Modifier
                              .size(52.dp)
                              .clip(CircleShape)
                              .background(if (channel.id == "announcements") Color(0xFFEA0038).copy(alpha = 0.12f) else WhatsAppNavSelectedPill),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(
                              text = channel.name.take(1).uppercase(),
                              style = MaterialTheme.typography.titleLarge,
                              color = if (channel.id == "announcements") Color(0xFFEA0038) else WhatsAppGreenDark,
                              fontWeight = FontWeight.Bold
                            )
                          }

                          Spacer(modifier = Modifier.width(14.dp))

                          Column(modifier = Modifier.weight(1f)) {
                            Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween,
                              verticalAlignment = Alignment.CenterVertically
                            ) {
                              Text(
                                text = channel.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = WhatsAppTextPrimary,
                                fontSize = 16.5.sp
                              )
                              Text(
                                text = "11:37 am",
                                style = MaterialTheme.typography.labelSmall,
                                color = WhatsAppTextSecondary,
                                fontSize = 12.sp
                              )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween,
                              verticalAlignment = Alignment.CenterVertically
                            ) {
                              Text(
                                text = channel.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = WhatsAppTextSecondary,
                                maxLines = 1,
                                fontSize = 13.5.sp,
                                modifier = Modifier.weight(1f)
                              )
                              Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = Color(0xFF8696A0),
                                modifier = Modifier.size(14.dp)
                              )
                            }
                          }
                        }
                        HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 82.dp))
                      }
                    }
                  }
                }

                1 -> {
                  // TAB 1: UPDATES & STATUS
                  LazyColumn(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(Color.White)
                      .padding(16.dp)
                  ) {
                    item {
                      Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppTextPrimary,
                        fontSize = 18.sp
                      )
                      Spacer(modifier = Modifier.height(12.dp))

                      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(
                          shape = RoundedCornerShape(16.dp),
                          colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
                          modifier = Modifier.size(width = 96.dp, height = 140.dp)
                        ) {
                          Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                              modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F2C34)),
                              contentAlignment = Alignment.Center
                            ) {
                              Text(text = "SMP", color = Color.White, fontWeight = FontWeight.Bold)
                              Box(
                                modifier = Modifier
                                  .align(Alignment.BottomEnd)
                                  .size(18.dp)
                                  .clip(CircleShape)
                                  .background(WhatsAppGreen),
                                contentAlignment = Alignment.Center
                              ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                              }
                            }
                            Text(
                              text = "Add status",
                              style = MaterialTheme.typography.labelSmall,
                              color = WhatsAppTextPrimary,
                              fontWeight = FontWeight.Medium,
                              modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                            )
                          }
                        }

                        Card(
                          shape = RoundedCornerShape(16.dp),
                          colors = CardDefaults.cardColors(containerColor = WhatsAppNavSelectedPill),
                          modifier = Modifier.size(width = 96.dp, height = 140.dp)
                        ) {
                          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                              Text(text = "⛏️", fontSize = 28.sp)
                              Spacer(modifier = Modifier.height(8.dp))
                              Text(
                                text = "Minecraft",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppGreenDark
                              )
                              Text(text = "Bedrock v1.21", style = MaterialTheme.typography.labelSmall, color = WhatsAppTextSecondary, fontSize = 9.sp)
                            }
                          }
                        }
                      }

                      Spacer(modifier = Modifier.height(24.dp))
                      Text(
                        text = "Channels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppTextPrimary,
                        fontSize = 18.sp
                      )
                      Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                      Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Box(
                          modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(WhatsAppNavSelectedPill),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(text = "MC", fontWeight = FontWeight.Bold, color = WhatsAppGreenDark)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                          Text(text = "Minecraft Substrate SMP", fontWeight = FontWeight.Bold, color = WhatsAppTextPrimary)
                          Text(text = "Head into the wild realm server at mc.substratesmp.net:19132", color = WhatsAppTextSecondary, fontSize = 12.sp, maxLines = 1)
                        }
                        Surface(
                          shape = CircleShape,
                          color = WhatsAppGreen,
                          modifier = Modifier.size(20.dp)
                        ) {
                          Box(contentAlignment = Alignment.Center) {
                            Text(text = "1", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                          }
                        }
                      }
                    }
                  }
                }

                2 -> {
                  // TAB 2: CALLS LIST
                  val voiceChannels = channels.filter { it.type == ChannelType.VOICE }
                  LazyColumn(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(Color.White)
                      .padding(horizontal = 16.dp, vertical = 10.dp)
                  ) {
                    item {
                      Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                      ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Surface(shape = CircleShape, color = Color(0xFFF0F2F5), modifier = Modifier.size(50.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                              Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = WhatsAppTextPrimary)
                            }
                          }
                          Spacer(modifier = Modifier.height(6.dp))
                          Text(text = "Call", style = MaterialTheme.typography.labelSmall, color = WhatsAppTextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Surface(shape = CircleShape, color = Color(0xFFF0F2F5), modifier = Modifier.size(50.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                              Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = WhatsAppTextPrimary)
                            }
                          }
                          Spacer(modifier = Modifier.height(6.dp))
                          Text(text = "Schedule", style = MaterialTheme.typography.labelSmall, color = WhatsAppTextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Surface(shape = CircleShape, color = Color(0xFFF0F2F5), modifier = Modifier.size(50.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                              Icon(imageVector = Icons.Default.Dialpad, contentDescription = null, tint = WhatsAppTextPrimary)
                            }
                          }
                          Spacer(modifier = Modifier.height(6.dp))
                          Text(text = "Keypad", style = MaterialTheme.typography.labelSmall, color = WhatsAppTextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Surface(shape = CircleShape, color = Color(0xFFF0F2F5), modifier = Modifier.size(50.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                              Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = WhatsAppTextPrimary)
                            }
                          }
                          Spacer(modifier = Modifier.height(6.dp))
                          Text(text = "Favourites", style = MaterialTheme.typography.labelSmall, color = WhatsAppTextPrimary)
                        }
                      }

                      Spacer(modifier = Modifier.height(16.dp))
                      Text(
                        text = "Recent",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppTextPrimary,
                        fontSize = 18.sp
                      )
                      Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(voiceChannels) { channel ->
                      val isCurrent = activeVoiceRoom?.channelId == channel.id
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable { requestCallPermissionsAndJoin(channel) }
                          .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Box(
                            modifier = Modifier
                              .size(50.dp)
                              .clip(CircleShape)
                              .background(if (isCurrent) WhatsAppNavSelectedPill else Color(0xFFF0F2F5)),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(
                              text = channel.name.take(1).uppercase(),
                              style = MaterialTheme.typography.titleMedium,
                              color = WhatsAppGreenDark,
                              fontWeight = FontWeight.Bold
                            )
                          }

                          Spacer(modifier = Modifier.width(14.dp))

                          Column {
                            Text(
                              text = channel.name,
                              style = MaterialTheme.typography.titleMedium,
                              fontWeight = FontWeight.SemiBold,
                              color = WhatsAppTextPrimary,
                              fontSize = 16.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                              Text(
                                text = if (isCurrent) "↗ Connected (In Call)" else "↗ Yesterday, 3:03 pm",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCurrent) WhatsAppGreenDark else WhatsAppTextSecondary,
                                fontSize = 13.sp
                              )
                            }
                          }
                        }

                        IconButton(
                          onClick = {
                            if (isCurrent) {
                              viewModel.voiceManager.disconnect()
                            } else {
                              requestCallPermissionsAndJoin(channel)
                            }
                          }
                        ) {
                          Icon(
                            imageVector = if (isCurrent) Icons.Default.CallEnd else Icons.Default.Call,
                            contentDescription = "Call",
                            tint = if (isCurrent) Color(0xFFEA0038) else WhatsAppGreenDark,
                            modifier = Modifier.size(24.dp)
                          )
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

  // Setup & Gamertag Registration Dialog
  if (showGamertagDialog || userState.needsGamertagSetup) {
    GamertagDialog(
      currentGamertag = userState.gamertag,
      onDismiss = { viewModel.setGamertagDialogVisible(false) },
      onRegister = { gamertag, onComplete ->
        viewModel.registerGamertag(gamertag, onComplete)
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
    ServerInfoSheet(
      onDismiss = { viewModel.setServerInfoSheetVisible(false) }
    )
  }
}
