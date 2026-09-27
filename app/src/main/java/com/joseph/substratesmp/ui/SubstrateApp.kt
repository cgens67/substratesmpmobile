package com.joseph.substratesmp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.joseph.substratesmp.data.model.*
import com.joseph.substratesmp.ui.components.*
import com.joseph.substratesmp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatChatListTime(timestamp: Long): String {
  if (timestamp <= 0L) return ""
  val now = System.currentTimeMillis()
  val diff = now - timestamp
  val calendar = Calendar.getInstance()
  val msgCal = Calendar.getInstance().apply { timeInMillis = timestamp }

  val isToday = calendar.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)

  val isYesterday = calendar.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1

  return when {
    isToday -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    isYesterday -> "Yesterday"
    diff < 7 * 24 * 3600 * 1000L -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
    else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstrateApp(
  viewModel: SubstrateViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val keyboardController = LocalSoftwareKeyboardController.current

  val channels by viewModel.channels.collectAsStateWithLifecycle()
  val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()
  val userState by viewModel.userState.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val statuses by viewModel.statuses.collectAsStateWithLifecycle()
  val stickers by viewModel.stickers.collectAsStateWithLifecycle()
  val members by viewModel.members.collectAsStateWithLifecycle()
  val typingUsers by viewModel.typingUsers.collectAsStateWithLifecycle()
  val mutedChannels by viewModel.mutedChannels.collectAsStateWithLifecycle()
  val blockedUsers by viewModel.blockedUsers.collectAsStateWithLifecycle()

  val activeVoiceRoom by viewModel.activeVoiceRoom.collectAsStateWithLifecycle()
  val agoraSettings by viewModel.agoraSettings.collectAsStateWithLifecycle()

  val showGamertagDialog by viewModel.showGamertagDialog.collectAsStateWithLifecycle()
  val showAgoraDialog by viewModel.showAgoraDialog.collectAsStateWithLifecycle()
  val showServerInfoSheet by viewModel.showServerInfoSheet.collectAsStateWithLifecycle()
  val showAdminConsole by viewModel.showAdminConsole.collectAsStateWithLifecycle()
  val showSelectContactDialog by viewModel.showSelectContactDialog.collectAsStateWithLifecycle()

  var currentScreen by rememberSaveable { mutableStateOf("home") }
  var selectedTab by rememberSaveable { mutableIntStateOf(0) }
  var activeFilterChip by rememberSaveable { mutableStateOf("All") }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var chatInputText by rememberSaveable { mutableStateOf("") }

  var showMenuDropdownSheet by remember { mutableStateOf(false) }
  var showCreateStatusDialog by remember { mutableStateOf(false) }
  
  var statusToDisplay by remember { mutableStateOf<StatusUpdate?>(null) }
  var isStatusViewerVisible by remember { mutableStateOf(false) }

  fun openStatus(status: StatusUpdate) {
    statusToDisplay = status
    isStatusViewerVisible = true
  }

  fun closeStatus() {
    isStatusViewerVisible = false
  }

  var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
  var showEmojiPicker by remember { mutableStateOf(false) }
  var viewedImageUrl by remember { mutableStateOf<String?>(null) }

  val listState = rememberLazyListState()

  val isScrolledToBottom by remember {
    derivedStateOf {
      val layoutInfo = listState.layoutInfo
      val totalItems = layoutInfo.totalItemsCount
      if (totalItems == 0) true
      else {
        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        lastVisible >= totalItems - 2
      }
    }
  }

  LaunchedEffect(chatInputText) {
    if (chatInputText.isNotBlank()) {
      viewModel.setTyping(true)
      delay(3000L)
      viewModel.setTyping(false)
    } else {
      viewModel.setTyping(false)
    }
  }

  val stickerPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      val base64 = processAndCompressImage(uri, context)
      if (base64 != null) {
        viewModel.addServerSticker("Sticker_${System.currentTimeMillis() % 1000}", base64)
      }
    }
  }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) {}

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= 33) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  BackHandler(enabled = currentScreen == "chat_screen" || currentScreen == "video_call_screen" || viewedImageUrl != null || isStatusViewerVisible) {
    if (isStatusViewerVisible) {
      closeStatus()
    } else if (viewedImageUrl != null) {
      viewedImageUrl = null
    } else if (showEmojiPicker) {
      showEmojiPicker = false
    } else if (currentScreen == "video_call_screen") {
      currentScreen = if (activeVoiceRoom?.isConnected == true) "chat_screen" else "home"
    } else {
      currentScreen = "home"
    }
  }

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
    if (messages.isNotEmpty() && isScrolledToBottom) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val isCurrentChannelMuted = mutedChannels.contains(activeChannel.id)
  val isRecipientBlocked = activeChannel.isDm && activeChannel.dmRecipientGamertag != null && blockedUsers.contains(activeChannel.dmRecipientGamertag!!.lowercase().trim())

  Box(modifier = modifier.fillMaxSize().background(Color.White)) {
    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = {
        if (targetState == "chat_screen" || targetState == "video_call_screen") {
          (slideInHorizontally(animationSpec = spring(stiffness = 400f)) { it } + fadeIn())
            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = 400f)) { -it / 3 } + fadeOut())
        } else {
          (slideInHorizontally(animationSpec = spring(stiffness = 400f)) { -it / 3 } + fadeIn())
            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = 400f)) { it } + fadeOut())
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
          } ?: run { currentScreen = "home" }
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
                        if (isCurrentChannelMuted) {
                          Spacer(modifier = Modifier.width(4.dp))
                          Icon(Icons.Default.VolumeOff, contentDescription = "Muted", tint = WhatsAppTextSecondary, modifier = Modifier.size(14.dp))
                        }
                      }
                      if (typingUsers.isNotEmpty()) {
                        Text(
                          text = "${typingUsers.first()} is typing...",
                          color = WhatsAppGreenDark,
                          fontWeight = FontWeight.Medium,
                          fontSize = 11.5.sp
                        )
                      } else {
                        Text(
                          text = if (activeChannel.isDm) "Direct Message" else "mc.substratesmp.net",
                          color = WhatsAppTextSecondary,
                          fontSize = 11.5.sp
                        )
                      }
                    }
                  }
                },
                navigationIcon = {
                  IconButton(onClick = {
                    showEmojiPicker = false
                    replyingToMessage = null
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
            Box(modifier = Modifier.fillMaxSize().padding(chatPadding).background(WhatsAppChatBackground)) {
              Column(modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding()) {
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
                        ChatMessageItem(
                          message = message,
                          isReadByAll = message.readBy.size >= (members.size - 1),
                          canDelete = userState.isAdmin || message.isLocalUser,
                          onDeleteMessage = { msg -> viewModel.deleteMessage(msg.channelId, msg.id) },
                          onReply = { msg -> replyingToMessage = msg },
                          onImageClick = { url -> viewedImageUrl = url },
                          modifier = Modifier.animateItem()
                        )
                      }

                      if (typingUsers.isNotEmpty()) {
                        item {
                          TypingBubble(typerName = typingUsers.first(), modifier = Modifier.animateItem())
                        }
                      }
                    }
                  }

                  androidx.compose.animation.AnimatedVisibility(
                    visible = !isScrolledToBottom,
                    enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                    exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
                    modifier = Modifier
                      .align(Alignment.BottomEnd)
                      .padding(bottom = 12.dp, end = 16.dp)
                  ) {
                    Surface(
                      shape = CircleShape,
                      color = Color.White,
                      shadowElevation = 4.dp,
                      modifier = Modifier
                        .size(42.dp)
                        .clickable {
                          scope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                          }
                        }
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = Icons.Default.KeyboardArrowDown,
                          contentDescription = "Scroll to bottom",
                          tint = WhatsAppGreenDark,
                          modifier = Modifier.size(24.dp)
                        )
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

                if (isRecipientBlocked) {
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF7F8FA),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                  ) {
                    Row(
                      modifier = Modifier.padding(14.dp).clickable {
                        activeChannel.dmRecipientGamertag?.let { viewModel.toggleBlockUser(it) }
                      },
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFEA0038))
                      Spacer(modifier = Modifier.width(10.dp))
                      Text("You blocked this player. Tap to unblock.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFEA0038), fontWeight = FontWeight.Bold)
                    }
                  }
                } else if (activeChannel.isRestrictedToAdmin && !userState.isAdmin) {
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
                    onSendMessage = { content, coords, img, aud, dur, fileUrl, fileName, isSticker, reply ->
                      viewModel.sendMessage(
                        content = content,
                        coordinates = coords,
                        imageUrl = img,
                        audioUrl = aud,
                        audioDurationSeconds = dur,
                        fileUrl = fileUrl,
                        fileName = fileName,
                        isSticker = isSticker,
                        replyTo = reply
                      )
                      chatInputText = ""
                      replyingToMessage = null
                    },
                    replyingTo = replyingToMessage,
                    onCancelReply = { replyingToMessage = null },
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
                      stickers = stickers,
                      isAdmin = userState.isAdmin,
                      onOpenCreateSticker = { stickerPickerLauncher.launch("image/*") },
                      onEmojiSelected = { emoji -> chatInputText += emoji },
                      onStickerSelected = { st ->
                        viewModel.sendMessage("", null, st.imageData, null, 0, null, null, true, replyingToMessage)
                        replyingToMessage = null
                        showEmojiPicker = false
                      },
                      onDeleteSticker = { id -> viewModel.deleteServerSticker(id) },
                      onBackspace = { chatInputText = dropLastGrapheme(chatInputText) }
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
              Column(modifier = Modifier.background(Color.White).statusBarsPadding()) {
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
                  if (selectedTab == 0) {
                    viewModel.setSelectContactDialogVisible(true)
                  } else if (selectedTab == 1) {
                    showCreateStatusDialog = true
                  } else if (selectedTab == 2) {
                    val vc = channels.find { it.type == ChannelType.VOICE } ?: channels.last()
                    runWithPermissions { viewModel.selectChannel(vc) }
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
                    (slideInHorizontally(animationSpec = spring(stiffness = 400f)) { it } + fadeIn())
                      .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = 400f)) { -it } + fadeOut())
                  } else {
                    (slideInHorizontally(animationSpec = spring(stiffness = 400f)) { -it } + fadeIn())
                      .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = 400f)) { it } + fadeOut())
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
                            Text("Bedrock Role: ${userState.role} • Tap to manage account", color = WhatsAppTextSecondary, fontSize = 13.sp)
                          }
                        }
                        HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 82.dp))
                      }

                      // Dynamic Live Preview: Shows latest message content, sender, time, and checkmarks
                      items(filtered, key = { it.id }) { channel ->
                        val isMuted = mutedChannels.contains(channel.id)
                        val isLocalSender = channel.lastMessageSender != null && channel.lastMessageSender.equals(userState.gamertag, ignoreCase = true)
                        val formattedTime = formatChatListTime(channel.lastMessageTimestamp)
                        val displayPreview = when {
                          channel.lastMessage.isNullOrBlank() -> channel.description
                          channel.isDm -> channel.lastMessage
                          isLocalSender -> "You: ${channel.lastMessage}"
                          channel.lastMessageSender != null -> "${channel.lastMessageSender}: ${channel.lastMessage}"
                          else -> channel.lastMessage
                        }

                        Row(
                          modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.selectChannel(channel)
                            currentScreen = "chat_screen"
                          }.padding(horizontal = 16.dp, vertical = 12.dp)
                          .animateItem(),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(if (channel.id == "announcements") Color(0xFFEA0038).copy(alpha = 0.12f) else if (channel.isDm) RoleAdminGold.copy(alpha = 0.15f) else WhatsAppNavSelectedPill),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(channel.name.take(1).uppercase(), color = if (channel.id == "announcements") Color(0xFFEA0038) else if (channel.isDm) RoleAdminGold else WhatsAppGreenDark, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                          }
                          Spacer(modifier = Modifier.width(14.dp))
                          Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                  text = channel.name,
                                  fontWeight = FontWeight.SemiBold,
                                  color = WhatsAppTextPrimary,
                                  fontSize = 16.5.sp,
                                  maxLines = 1,
                                  overflow = TextOverflow.Ellipsis
                                )
                                if (channel.isDm) {
                                  Spacer(modifier = Modifier.width(4.dp))
                                  Text("• PM", color = WhatsAppGreenDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                if (channel.isRestrictedToAdmin) {
                                  Spacer(modifier = Modifier.width(4.dp))
                                  Icon(Icons.Default.Lock, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(13.dp))
                                }
                              }
                              Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isMuted) {
                                  Icon(Icons.Default.VolumeOff, contentDescription = "Muted", tint = WhatsAppTextSecondary, modifier = Modifier.size(14.dp))
                                  Spacer(modifier = Modifier.width(4.dp))
                                }
                                if (formattedTime.isNotBlank()) {
                                  Text(formattedTime, color = WhatsAppTextSecondary, fontSize = 12.sp)
                                }
                              }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                              if (isLocalSender && channel.lastMessage != null) {
                                Icon(Icons.Default.DoneAll, contentDescription = "Sent", tint = WhatsAppCheckmarkBlue, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                              }
                              Text(
                                text = displayPreview,
                                color = WhatsAppTextSecondary,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                              )
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
                                    modifier = Modifier.align(Alignment.BottomEnd).size(18.dp).clip(CircleShape).background(WhatsAppGreen)
                                  ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                  }
                                }
                                Text("Add status", color = WhatsAppTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
                              }
                            }
                          }

                          items(statuses, key = { it.id }) { status ->
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
                              modifier = Modifier.size(width = 110.dp, height = 160.dp).clickable { openStatus(status) }.animateItem()
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

                      items(voiceChannels, key = { it.id }) { channel ->
                        val isCurrent = activeVoiceRoom?.channelId == channel.id
                        val participantsInRoom = if (isCurrent) activeVoiceRoom?.participants ?: emptyList() else emptyList()

                        Card(
                          shape = RoundedCornerShape(16.dp),
                          colors = CardDefaults.cardColors(containerColor = if (isCurrent) WhatsAppNavSelectedPill.copy(alpha = 0.5f) else Color(0xFFF7F8FA)),
                          modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).animateItem()
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

    AnimatedVisibility(
      visible = viewedImageUrl != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black)
          .clickable { viewedImageUrl = null }
      ) {
        IconButton(
          onClick = { viewedImageUrl = null },
          modifier = Modifier.align(Alignment.TopStart).statusBarsPadding()
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        viewedImageUrl?.let { url ->
          if (url.startsWith("http")) {
            AsyncImage(
              model = url,
              contentDescription = "Expanded Image",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Fit
            )
          } else {
            val raw = url.substringAfter("base64,")
            val bytes = Base64.decode(raw, Base64.NO_WRAP)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            if (bitmap != null) {
              Image(
                bitmap = bitmap,
                contentDescription = "Expanded Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
              )
            }
          }
        }
      }
    }
  }

  // Smooth WhatsApp-style Animated Entry/Exit for the Status Viewer Screen
  AnimatedVisibility(
    visible = isStatusViewerVisible && statusToDisplay != null,
    enter = scaleIn(initialScale = 0.82f, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(250)),
    exit = scaleOut(targetScale = 0.82f, animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium)) + fadeOut(tween(200))
  ) {
    statusToDisplay?.let { status ->
      StatusViewerScreen(
        status = status,
        isOwnStatus = status.authorGamertag == userState.gamertag,
        onDismiss = { closeStatus() },
        onDelete = {
          viewModel.deleteStatus(status.id)
          closeStatus()
        },
        onReact = { emoji ->
          viewModel.reactToStatus(status.id, emoji)
        }
      )
    }
  }

  if (showSelectContactDialog) {
    SelectContactDialog(
      members = members,
      currentGamertag = userState.gamertag,
      onDismiss = { viewModel.setSelectContactDialogVisible(false) },
      onSelectMember = { target ->
        viewModel.startPrivateChat(target.gamertag)
        currentScreen = "chat_screen"
      }
    )
  }

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

  if (showCreateStatusDialog) {
    StatusCreatorDialog(
      onDismiss = { showCreateStatusDialog = false },
      onPostStatus = { text, theme, activity, coords ->
        viewModel.postStatus(text, theme, activity, coords)
        showCreateStatusDialog = false
      }
    )
  }

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
