package com.joseph.substratesmp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.joseph.substratesmp.R
import com.joseph.substratesmp.data.model.*
import com.joseph.substratesmp.ui.components.*
import com.joseph.substratesmp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

suspend fun LazyListState.smoothScrollToBottom() {
  val total = layoutInfo.totalItemsCount
  if (total == 0) return
  val targetIndex = total - 1

  animateScrollToItem(targetIndex)

  val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
  if (lastVisible != null && lastVisible.index == targetIndex) {
    val viewportBottom = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
    val itemBottom = lastVisible.offset + lastVisible.size
    val diff = (itemBottom - viewportBottom).toFloat()
    if (diff > 0f) {
      animateScrollBy(
        value = diff + 30f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
      )
    }
  }
}

fun formatChatListTime(timestamp: Long, yesterdayText: String): String {
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
    isYesterday -> yesterdayText
    diff < 7 * 24 * 3600 * 1000L -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
    else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
  }
}

fun isSameDay(ts1: Long, ts2: Long): Boolean {
  val cal1 = Calendar.getInstance().apply { timeInMillis = ts1 }
  val cal2 = Calendar.getInstance().apply { timeInMillis = ts2 }
  return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
         cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun formatDatePill(timestamp: Long, todayText: String, yesterdayText: String): String {
  if (timestamp <= 0L) return todayText
  val now = System.currentTimeMillis()
  val calNow = Calendar.getInstance().apply { timeInMillis = now }
  val calMsg = Calendar.getInstance().apply { timeInMillis = timestamp }

  val isToday = calNow.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calMsg.get(Calendar.DAY_OF_YEAR)

  val isYesterday = calNow.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR) &&
                    calNow.get(Calendar.DAY_OF_YEAR) - calMsg.get(Calendar.DAY_OF_YEAR) == 1

  val isSameYear = calNow.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR)

  return when {
    isToday -> todayText
    isYesterday -> yesterdayText
    isSameYear -> SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date(timestamp))
    else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
  }
}

@Composable
fun FloatingBottomNavBar(
  currentScreen: String,
  onNavigate: (String) -> Unit,
  userInitial: String,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  val surfaceColor = if (isDarkMode) Color(0xFF2E2E2E) else Color.White
  val borderColor = if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFDDE0E5)
  val pillShape = RoundedCornerShape(32.dp)

  Surface(
    shape = pillShape,
    color = surfaceColor,
    border = BorderStroke(1.5.dp, borderColor),
    shadowElevation = if (isDarkMode) 8.dp else 14.dp,
    modifier = modifier.height(64.dp).clip(pillShape)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavBarPillItem(icon = Icons.Default.ChatBubble, label = stringResource(R.string.nav_chats), isSelected = currentScreen == "home", isDarkMode = isDarkMode) { onNavigate("home") }
      Spacer(modifier = Modifier.width(8.dp))
      NavBarPillItem(icon = Icons.Default.Person, label = stringResource(R.string.nav_contacts), isSelected = currentScreen == "contacts", isDarkMode = isDarkMode) { onNavigate("contacts") }
      Spacer(modifier = Modifier.width(8.dp))
      NavBarPillItem(icon = Icons.Default.Settings, label = stringResource(R.string.nav_settings), isSelected = currentScreen == "settings_screen", isDarkMode = isDarkMode) { onNavigate("settings_screen") }
      Spacer(modifier = Modifier.width(8.dp))
      NavBarPillItem(
        icon = null,
        label = stringResource(R.string.nav_profile),
        isSelected = currentScreen == "profile_screen",
        isDarkMode = isDarkMode,
        customIcon = {
          Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (isDarkMode) Color(0xFF404040) else Color(0xFF1C2228)), contentAlignment = Alignment.Center) {
            Text(userInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        },
        onClick = { onNavigate("profile_screen") }
      )
    }
  }
}

@Composable
fun NavBarPillItem(icon: androidx.compose.ui.graphics.vector.ImageVector?, label: String, isSelected: Boolean, isDarkMode: Boolean, customIcon: @Composable (() -> Unit)? = null, onClick: () -> Unit) {
  val bgColor = if (isSelected) (if (isDarkMode) Color(0xFF00A884) else Color(0xFFE1F5FE)) else Color.Transparent
  val contentColor = if (isSelected) (if (isDarkMode) Color.White else Color(0xFF0288D1)) else (if (isDarkMode) Color(0xFFA0A0A5) else Color.Gray)
  val itemShape = RoundedCornerShape(20.dp)

  Surface(
    shape = itemShape,
    color = bgColor,
    onClick = onClick,
    modifier = Modifier
      .clip(itemShape)
      .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (customIcon != null) customIcon() else Icon(imageVector = icon!!, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
      if (isSelected) {
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
      }
    }
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

  val gifImageLoader = remember(context) {
    ImageLoader.Builder(context)
      .components {
        if (Build.VERSION.SDK_INT >= 28) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
      }
      .build()
  }

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
  val favouriteChannels by viewModel.favouriteChannels.collectAsStateWithLifecycle()
  val translatedMessages by viewModel.translatedMessages.collectAsStateWithLifecycle()
  val deletedGamertags by viewModel.deletedGamertags.collectAsStateWithLifecycle()

  val activeVoiceRoom by viewModel.activeVoiceRoom.collectAsStateWithLifecycle()
  val incomingCall by viewModel.incomingCall.collectAsStateWithLifecycle()
  val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

  val showGamertagDialog by viewModel.showGamertagDialog.collectAsStateWithLifecycle()
  val showServerInfoSheet by viewModel.showServerInfoSheet.collectAsStateWithLifecycle()
  val showAdminConsole by viewModel.showAdminConsole.collectAsStateWithLifecycle()

  var currentScreen by rememberSaveable { mutableStateOf("home") }
  var activeFilterChip by rememberSaveable { mutableStateOf("All") }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var chatInputText by rememberSaveable { mutableStateOf("") }

  var showMenuDropdownSheet by remember { mutableStateOf(false) }
  var longPressedChannel by remember { mutableStateOf<Channel?>(null) }

  // Manual Coordinates Dialog fallback state
  var manualLocationTargetMessage by remember { mutableStateOf<ChatMessage?>(null) }
  var manualCoordsInput by remember { mutableStateOf("") }

  // Instagram Note State
  var activeNoteToView by remember { mutableStateOf<StatusUpdate?>(null) }
  var showNoteComposer by remember { mutableStateOf(false) }

  var viewedUser by remember { mutableStateOf<String?>(null) }
  var shouldForceScrollToBottom by remember { mutableStateOf(false) }
  var showInviteToCallDialog by remember { mutableStateOf(false) }

  LaunchedEffect(currentScreen) {
    viewModel.setCurrentScreen(currentScreen)
  }

  val isDarkMode = appSettings.isNightMode
  val menuDarkBg = Color(0xFF262626)
  val menuDarkSurface = Color(0xFF303030)
  val menuDarkSearch = Color(0xFF1E1E1E)
  val menuDarkBorder = Color(0xFF3D3D3D)
  val menuDarkText = Color(0xFFEDEDED)
  val menuDarkSubtext = Color(0xFFA0A0A5)

  val menuLightBg = Color(0xFFF4F5F8)
  val menuLightSurface = Color.White
  val menuLightSearch = Color.White
  val menuLightBorder = Color(0xFFE2E4E8)

  val bgColor = if (isDarkMode) menuDarkBg else menuLightBg
  val surfaceColor = if (isDarkMode) menuDarkSurface else menuLightSurface
  val textColor = if (isDarkMode) menuDarkText else WhatsAppTextPrimary
  val subTextColor = if (isDarkMode) menuDarkSubtext else WhatsAppTextSecondary

  val chatBgColor = if (isDarkMode) ChatDarkBackground else ChatLightBackground
  val datePillBg = if (isDarkMode) ChatDarkDatePill else ChatLightDatePill
  val datePillTextColor = if (isDarkMode) ChatDarkIncomingTime else ChatLightIncomingTime

  val todayText = stringResource(R.string.chat_today)
  val yesterdayText = stringResource(R.string.chat_yesterday)

  var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
  var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
  var showEmojiPicker by remember { mutableStateOf(false) }
  var viewedImageUrl by remember { mutableStateOf<String?>(null) }

  val listState = rememberLazyListState()

  val isScrolledToBottom by remember {
    derivedStateOf {
      val layoutInfo = listState.layoutInfo
      val totalItems = layoutInfo.totalItemsCount
      if (totalItems <= 1) true
      else {
        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        lastVisible >= totalItems - 2
      }
    }
  }

  // Guaranteed instant scroll to bottom on channel open and load
  var lastLoadedChannelId by remember { mutableStateOf("") }
  var hasScrolledToBottomForChannel by remember { mutableStateOf(false) }

  LaunchedEffect(activeChannel.id) {
    if (activeChannel.id != lastLoadedChannelId) {
      lastLoadedChannelId = activeChannel.id
      hasScrolledToBottomForChannel = false
    }
  }

  LaunchedEffect(messages.size, currentScreen, activeChannel.id) {
    if (currentScreen == "chat_screen" && messages.isNotEmpty()) {
      if (!hasScrolledToBottomForChannel) {
        delay(25L)
        listState.scrollToItem((messages.size * 2).coerceAtLeast(0))
        hasScrolledToBottomForChannel = true
      } else if (shouldForceScrollToBottom || isScrolledToBottom) {
        delay(35L)
        val target = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
        if (appSettings.smoothAnimations && shouldForceScrollToBottom) {
          listState.smoothScrollToBottom()
        } else {
          listState.scrollToItem(target)
        }
        shouldForceScrollToBottom = false
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

  BackHandler(enabled = currentScreen != "home" || viewedImageUrl != null || activeNoteToView != null) {
    if (activeNoteToView != null) {
      activeNoteToView = null
    } else if (viewedImageUrl != null) {
      viewedImageUrl = null
    } else if (showEmojiPicker) {
      showEmojiPicker = false
    } else if (currentScreen == "video_call_screen") {
      currentScreen = if (activeVoiceRoom?.isConnected == true) "chat_screen" else "home"
    } else if (currentScreen == "channel_info_screen") {
      currentScreen = "chat_screen"
    } else if (currentScreen == "privacy_policy_screen") {
      currentScreen = "settings_screen"
    } else if (currentScreen == "user_profile_screen") {
      currentScreen = "chat_screen"
    } else if (currentScreen == "profile_screen" || currentScreen == "settings_screen" || currentScreen == "contacts") {
      currentScreen = "home"
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

  val isCurrentChannelMuted = mutedChannels.contains(activeChannel.id)
  val isRecipientBlocked = activeChannel.isDm && activeChannel.dmRecipientGamertag != null && blockedUsers.contains(activeChannel.dmRecipientGamertag!!.lowercase().trim())

  Box(modifier = modifier.fillMaxSize().background(bgColor)) {
    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = {
        if (!appSettings.smoothAnimations) {
          fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
        } else if (targetState == "profile_screen" || targetState == "settings_screen" || targetState == "privacy_policy_screen" || targetState == "user_profile_screen" || targetState == "channel_info_screen") {
          (slideInVertically(animationSpec = spring(stiffness = 400f)) { it } + fadeIn())
            .togetherWith(slideOutVertically(animationSpec = spring(stiffness = 400f)) { -it / 3 } + fadeOut())
        } else if (targetState == "chat_screen" || targetState == "video_call_screen") {
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
        "channel_info_screen" -> {
          ChannelInfoScreen(
            channel = activeChannel,
            members = members,
            messages = messages,
            currentGamertag = userState.gamertag,
            isAdmin = userState.isAdmin,
            isMuted = isCurrentChannelMuted,
            isDarkMode = isDarkMode,
            onToggleMute = { viewModel.toggleMuteChannel(activeChannel.id) },
            onNavigateBack = { currentScreen = "chat_screen" },
            onSelectMember = { member ->
              viewedUser = member.gamertag
              currentScreen = "user_profile_screen"
            },
            onAddMembers = {
              if (userState.gamertag.isBlank()) viewModel.setGamertagDialogVisible(true)
              else currentScreen = "contacts"
            },
            onImageClick = { url -> viewedImageUrl = url },
            onUpdateChannel = { id, name, desc -> viewModel.updateChannelInfo(id, name, desc) },
            onTogglePermission = { ch -> viewModel.toggleChannelPermission(ch) }
          )
        }

        "profile_screen" -> {
          ProfileScreen(
            userState = userState,
            isDarkMode = isDarkMode,
            onNavigateSettings = { currentScreen = "settings_screen" },
            onNavigateHome = { currentScreen = "home" },
            onUpdateProfile = { bio, bday -> viewModel.updateProfile(bio, bday) },
            onLogin = { tag, pass, res -> viewModel.loginAccount(tag, pass, res) },
            onRegister = { tag, pass, res -> viewModel.registerAccount(tag, pass, res) },
            onLogout = {
              viewModel.logout()
              currentScreen = "home"
            }
          )
        }

        "settings_screen" -> {
          SettingsScreen(
            userState = userState,
            appSettings = appSettings,
            isDarkMode = isDarkMode,
            onUpdateSetting = { key, value -> viewModel.updateSetting(key, value) },
            onNavigateBack = { currentScreen = "home" },
            onNavigateProfile = { currentScreen = "profile_screen" },
            onNavigatePrivacy = { currentScreen = "privacy_policy_screen" }
          )
        }

        "privacy_policy_screen" -> {
          PrivacyPolicyScreen(
            isDarkMode = isDarkMode,
            onNavigateBack = { currentScreen = "settings_screen" }
          )
        }

        "user_profile_screen" -> {
          val targetGamertag = viewedUser ?: "Unknown"
          val targetMember = members.find { it.gamertag.equals(targetGamertag, ignoreCase = true) }
          val myTag = userState.gamertag.trim()
          val dmId = "dm_" + listOf(myTag.lowercase(), targetGamertag.lowercase()).sorted().joinToString("_")
          val isMuted = viewModel.isChannelMuted(dmId)
          val isFav = viewModel.isFavourite(dmId)
          val isBlocked = viewModel.isUserBlocked(targetGamertag)

          UserProfileScreen(
            gamertag = targetGamertag,
            role = targetMember?.role ?: "MEMBER",
            isAdmin = targetMember?.isAdmin ?: false,
            bio = targetMember?.bio ?: "",
            birthday = targetMember?.birthday ?: "",
            lastCoordinates = targetMember?.lastCoordinates ?: "",
            lastCoordinatesTimestamp = targetMember?.lastCoordinatesTimestamp ?: 0L,
            isViewerAdmin = userState.isAdmin,
            isMuted = isMuted,
            isFavourite = isFav,
            isBlocked = isBlocked,
            isDarkMode = isDarkMode,
            onNavigateBack = { currentScreen = "chat_screen" },
            onMessageUser = {
              if (userState.gamertag.isBlank()) {
                viewModel.setGamertagDialogVisible(true)
              } else {
                viewModel.startPrivateChat(targetGamertag)
                currentScreen = "chat_screen"
              }
            },
            onRequestLocation = {
              if (userState.gamertag.isBlank()) {
                viewModel.setGamertagDialogVisible(true)
              } else {
                viewModel.requestUserLocation(targetGamertag)
                currentScreen = "chat_screen"
              }
            },
            onToggleMute = { viewModel.toggleMuteChannel(dmId) },
            onToggleFavourite = { viewModel.toggleFavourite(dmId) },
            onToggleBlock = { viewModel.toggleBlockUser(targetGamertag) }
          )
        }

        "contacts" -> {
          if (userState.gamertag.isBlank()) {
            LaunchedEffect(Unit) {
              viewModel.setGamertagDialogVisible(true)
              currentScreen = "home"
            }
          } else {
            SelectContactDialog(
              members = members,
              currentGamertag = userState.gamertag,
              isDarkMode = isDarkMode,
              onDismiss = { currentScreen = "home" },
              onSelectMember = { target ->
                viewModel.startPrivateChat(target.gamertag)
                currentScreen = "chat_screen"
              }
            )
          }
        }

        "video_call_screen" -> {
          activeVoiceRoom?.let { room ->
            VideoCallScreen(
              voiceRoom = room,
              voiceManager = viewModel.voiceManager,
              onDisconnect = {
                viewModel.voiceManager.disconnect()
                currentScreen = "home"
              },
              onMinimize = {
                currentScreen = "chat_screen"
              },
              onAddPerson = {
                showInviteToCallDialog = true
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
                    modifier = Modifier.clickable {
                      if (activeChannel.isDm) {
                        viewedUser = activeChannel.dmRecipientGamertag ?: activeChannel.name
                        currentScreen = "user_profile_screen"
                      } else {
                        currentScreen = "channel_info_screen"
                      }
                    }
                  ) {
                    Box(
                      modifier = Modifier.size(38.dp).clip(CircleShape).background(if (isDarkMode) Color(0xFF424242) else Color(0xFFE9EDEF)),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(activeChannel.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else WhatsAppGreenDark)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          activeChannel.name,
                          fontWeight = FontWeight.Bold,
                          color = textColor,
                          fontSize = 17.sp,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                        if (activeChannel.isRestrictedToAdmin) {
                          Spacer(modifier = Modifier.width(4.dp))
                          Icon(Icons.Default.Lock, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(13.dp))
                        }
                        if (isCurrentChannelMuted) {
                          Spacer(modifier = Modifier.width(4.dp))
                          Icon(Icons.Default.VolumeOff, contentDescription = null, tint = subTextColor, modifier = Modifier.size(14.dp))
                        }
                      }
                      if (typingUsers.isNotEmpty()) {
                        Text(
                          text = stringResource(R.string.typing_indicator, typingUsers.first()),
                          color = WhatsAppGreenDark,
                          fontWeight = FontWeight.Medium,
                          fontSize = 11.5.sp,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      } else {
                        Text(
                          text = if (activeChannel.isDm) stringResource(R.string.direct_message) else stringResource(R.string.default_server_domain),
                          color = subTextColor,
                          fontSize = 11.5.sp,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }
                    }
                  }
                },
                navigationIcon = {
                  IconButton(onClick = {
                    showEmojiPicker = false
                    replyingToMessage = null
                    editingMessage = null
                    currentScreen = "home"
                  }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = textColor)
                  }
                },
                actions = {
                  IconButton(
                    onClick = {
                      runWithPermissions {
                        if (activeChannel.isDm) {
                          val callId = "call_" + activeChannel.id
                          viewModel.startPrivateCall(activeChannel.name, callId, "${activeChannel.name} (Video)", isVideo = true)
                          currentScreen = "video_call_screen"
                        } else {
                          val vc = channels.find { it.type == ChannelType.VOICE } ?: channels.last()
                          viewModel.startVideoCall(vc)
                          currentScreen = "video_call_screen"
                        }
                      }
                    }
                  ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = textColor)
                  }

                  IconButton(
                    onClick = {
                      runWithPermissions {
                        if (activeChannel.isDm) {
                          val callId = "call_" + activeChannel.id
                          viewModel.startPrivateCall(activeChannel.name, callId, "${activeChannel.name} (Voice)", isVideo = false)
                        } else {
                          val vc = channels.find { it.type == ChannelType.VOICE } ?: channels.last()
                          viewModel.selectChannel(vc)
                        }
                      }
                    }
                  ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = textColor)
                  }

                  IconButton(onClick = { showMenuDropdownSheet = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = textColor)
                  }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
              )
            },
            contentWindowInsets = WindowInsets.statusBars
          ) { chatPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(chatPadding).background(chatBgColor)) {
              Column(modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                  LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)) {
                    itemsIndexed(messages, key = { _, message -> message.id }) { index, message ->
                      val showDateHeader = index == 0 || !isSameDay(messages[index - 1].timestamp, message.timestamp)
                      if (showDateHeader) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                          Surface(shape = RoundedCornerShape(8.dp), color = datePillBg) {
                            Text(
                              text = formatDatePill(message.timestamp, todayText, yesterdayText),
                              color = datePillTextColor,
                              style = MaterialTheme.typography.labelSmall,
                              modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                          }
                        }
                      }

                      val isRead = if (activeChannel.isDm) {
                        val recipient = activeChannel.dmRecipientGamertag
                        if (!recipient.isNullOrBlank()) {
                          message.readBy.any { it.equals(recipient, ignoreCase = true) }
                        } else {
                          message.readBy.isNotEmpty()
                        }
                      } else {
                        val cleanSender = message.senderName.trim()
                        message.readBy.any { !it.equals(cleanSender, ignoreCase = true) }
                      }

                      val isDelivered = if (activeChannel.isDm) {
                        val recipient = activeChannel.dmRecipientGamertag
                        if (!recipient.isNullOrBlank()) {
                          message.deliveredTo.any { it.equals(recipient, ignoreCase = true) } || isRead
                        } else {
                          message.deliveredTo.isNotEmpty() || isRead
                        }
                      } else {
                        val cleanSender = message.senderName.trim()
                        message.deliveredTo.any { !it.equals(cleanSender, ignoreCase = true) } || isRead
                      }

                      val translation = translatedMessages[message.id]

                      ChatMessageItem(
                        message = message,
                        translatedText = translation,
                        currentGamertag = userState.gamertag,
                        isDarkMode = isDarkMode,
                        isRead = isRead,
                        isDelivered = isDelivered,
                        canDelete = userState.isAdmin || message.isLocalUser,
                        isSenderDeleted = deletedGamertags.contains(message.senderName.lowercase().trim()),
                        onUserClick = { name ->
                          if (!deletedGamertags.contains(name.lowercase().trim())) {
                            viewedUser = name
                            currentScreen = "user_profile_screen"
                          }
                        },
                        onDeleteMessage = { msg -> viewModel.deleteMessage(msg.channelId, msg.id) },
                        onReply = { msg -> replyingToMessage = msg },
                        onEdit = { msg ->
                          editingMessage = msg
                          chatInputText = msg.content
                          keyboardController?.show()
                        },
                        onTranslate = { msgId, lang -> viewModel.translateMessage(msgId, message.content, lang) },
                        onImageClick = { url -> viewedImageUrl = url },
                        onSendCurrentLocation = { reqMsg ->
                          shouldForceScrollToBottom = true
                          viewModel.sendLocationResponse(
                            requestMessage = reqMsg,
                            onManualInputRequired = {
                              manualLocationTargetMessage = reqMsg
                              manualCoordsInput = ""
                            }
                          )
                        },
                        modifier = Modifier.animateItem(
                          fadeInSpec = tween(200),
                          placementSpec = tween(250, easing = FastOutSlowInEasing),
                          fadeOutSpec = tween(150)
                        )
                      )
                    }

                    if (typingUsers.isNotEmpty()) {
                      item {
                        TypingBubble(typerName = typingUsers.first(), modifier = Modifier.animateItem())
                      }
                    }

                    item {
                      Spacer(modifier = Modifier.height(16.dp))
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
                      color = surfaceColor,
                      shadowElevation = 4.dp,
                      modifier = Modifier
                        .size(42.dp)
                        .clickable {
                          scope.launch {
                            listState.smoothScrollToBottom()
                          }
                        }
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = Icons.Default.KeyboardArrowDown,
                          contentDescription = null,
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
                    color = surfaceColor,
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
                      Text(stringResource(R.string.blocked_user_banner), style = MaterialTheme.typography.bodySmall, color = Color(0xFFEA0038), fontWeight = FontWeight.Bold)
                    }
                  }
                } else if (activeChannel.isRestrictedToAdmin && !userState.isAdmin) {
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = surfaceColor,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                  ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Lock, contentDescription = null, tint = RoleAdminGold)
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(stringResource(R.string.admin_only_banner), style = MaterialTheme.typography.bodySmall, color = subTextColor)
                    }
                  }
                } else {
                  ChatInputBar(
                    channelName = activeChannel.name,
                    text = chatInputText,
                    isDarkMode = isDarkMode,
                    isDm = activeChannel.isDm,
                    members = members,
                    onTextChanged = { chatInputText = it },
                    onSendMessage = { content, coords, img, aud, dur, fileUrl, fileName, isSticker, reply ->
                      if (editingMessage != null) {
                        viewModel.editMessage(editingMessage!!.id, content)
                        editingMessage = null
                        chatInputText = ""
                      } else {
                        shouldForceScrollToBottom = true
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
                      }
                    },
                    onRequestLocation = {
                      if (activeChannel.isDm) {
                        val recipient = activeChannel.dmRecipientGamertag ?: activeChannel.name
                        viewModel.requestUserLocation(recipient)
                      } else {
                        viewModel.sendMessage(content = "📍 Location Request", coordinates = null, isLocationRequest = true)
                      }
                    },
                    replyingTo = replyingToMessage,
                    onCancelReply = { replyingToMessage = null },
                    editingMessage = editingMessage,
                    onCancelEdit = {
                      editingMessage = null
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
                      stickers = stickers,
                      isAdmin = userState.isAdmin,
                      isDarkMode = isDarkMode,
                      onOpenCreateSticker = { stickerPickerLauncher.launch("image/*") },
                      onEmojiSelected = { emoji -> chatInputText += emoji },
                      onStickerSelected = { st ->
                        shouldForceScrollToBottom = true
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
              Column(modifier = Modifier.background(bgColor).statusBarsPadding()) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color(0xFF00A884) else WhatsAppHeaderGreen,
                    fontSize = 24.sp
                  )

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (userState.isAdmin) {
                      IconButton(onClick = { viewModel.setAdminConsoleVisible(true) }) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold)
                      }
                    }
                    IconButton(onClick = { showMenuDropdownSheet = true }) {
                      Icon(Icons.Default.MoreVert, contentDescription = null, tint = textColor)
                    }
                  }
                }

                Surface(
                  shape = RoundedCornerShape(24.dp),
                  color = if (isDarkMode) menuDarkSearch else menuLightSearch,
                  border = BorderStroke(1.dp, if (isDarkMode) menuDarkBorder else menuLightBorder),
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).height(44.dp)
                ) {
                  Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = subTextColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                      value = searchQuery,
                      onValueChange = { searchQuery = it },
                      modifier = Modifier.weight(1f),
                      textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontSize = 15.sp),
                      singleLine = true,
                      decorationBox = { inner ->
                        if (searchQuery.isEmpty()) Text(stringResource(R.string.action_search), style = MaterialTheme.typography.bodyMedium, color = subTextColor, fontSize = 15.sp)
                        inner()
                      }
                    )
                    if (searchQuery.isNotEmpty()) {
                      IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear), tint = subTextColor, modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }

                val filterChips = listOf(
                  stringResource(R.string.chip_all),
                  stringResource(R.string.chip_unread),
                  stringResource(R.string.chip_favourites),
                  stringResource(R.string.chip_groups)
                )
                LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  items(filterChips) { chip ->
                    val isSelected = activeFilterChip == chip
                    val chipShape = RoundedCornerShape(18.dp)
                    Surface(
                      shape = chipShape,
                      color = if (isSelected) {
                        if (isDarkMode) Color(0xFF00A884) else WhatsAppNavSelectedPill
                      } else {
                        if (isDarkMode) menuDarkSurface else surfaceColor
                      },
                      border = if (isSelected) null else BorderStroke(1.dp, if (isDarkMode) menuDarkBorder else menuLightBorder),
                      onClick = { activeFilterChip = chip },
                      modifier = Modifier.clip(chipShape)
                    ) {
                      Text(
                        chip,
                        color = if (isSelected) {
                          if (isDarkMode) Color.White else WhatsAppGreenDark
                        } else {
                          subTextColor
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 13.sp
                      )
                    }
                  }
                }
              }
            }
          ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(bgColor)) {
              InstagramNotesTray(
                notes = statuses,
                currentGamertag = userState.gamertag,
                isDarkMode = isDarkMode,
                onOpenNote = { activeNoteToView = it },
                onAddNote = {
                  if (userState.gamertag.isBlank()) {
                    viewModel.setGamertagDialogVisible(true)
                  } else {
                    showNoteComposer = true
                  }
                }
              )

              HorizontalDivider(
                color = if (isDarkMode) Color(0xFF333333) else menuLightBorder,
                thickness = 0.5.dp
              )

              val allLabel = stringResource(R.string.chip_all)
              val unreadLabel = stringResource(R.string.chip_unread)
              val favsLabel = stringResource(R.string.chip_favourites)
              val groupsLabel = stringResource(R.string.chip_groups)

              val filtered = channels.filter { it.type == ChannelType.TEXT }
                .filter { ch ->
                  when (activeFilterChip) {
                    unreadLabel -> {
                      val isLocalSender = ch.lastMessageSender != null && ch.lastMessageSender.equals(userState.gamertag, ignoreCase = true)
                      val hasUnreadIncoming = !ch.lastMessageIsRead && !isLocalSender && ch.lastMessage != null
                      ch.unreadCount > 0 || hasUnreadIncoming
                    }
                    favsLabel -> {
                      favouriteChannels.contains(ch.id) || ch.id == "announcements" || ch.id == "general-chat"
                    }
                    groupsLabel -> {
                      !ch.isDm
                    }
                    else -> true
                  }
                }
                .filter { ch ->
                  if (searchQuery.isBlank()) true
                  else ch.name.contains(searchQuery, true) || ch.description.contains(searchQuery, true) || (ch.lastMessage?.contains(searchQuery, true) == true)
                }

              LazyColumn(modifier = Modifier.fillMaxSize().background(bgColor)) {
                if (filtered.isEmpty()) {
                  item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                      Text(
                        text = when (activeFilterChip) {
                          unreadLabel -> stringResource(R.string.no_unread_chats)
                          favsLabel -> stringResource(R.string.no_favourite_chats)
                          groupsLabel -> stringResource(R.string.no_group_channels)
                          else -> stringResource(R.string.no_chats_found)
                        },
                        color = subTextColor,
                        fontSize = 14.sp
                      )
                    }
                  }
                } else {
                  items(filtered, key = { it.id }) { channel ->
                    val isMuted = mutedChannels.contains(channel.id)
                    val isLocalSender = channel.lastMessageSender != null && channel.lastMessageSender.equals(userState.gamertag, ignoreCase = true)
                    val formattedTime = formatChatListTime(channel.lastMessageTimestamp, yesterdayText)
                    val isLastMessageRead = channel.lastMessageIsRead

                    val displayPreview = when {
                      channel.lastMessage.isNullOrBlank() -> channel.description
                      channel.isDm -> channel.lastMessage
                      isLocalSender -> "You: ${channel.lastMessage}"
                      channel.lastMessageSender != null -> "${channel.lastMessageSender}: ${channel.lastMessage}"
                      else -> channel.lastMessage
                    }

                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkMode) menuDarkBg else Color.White)
                        .combinedClickable(
                          onClick = {
                            viewModel.selectChannel(channel)
                            currentScreen = "chat_screen"
                          },
                          onLongClick = { longPressedChannel = channel }
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .animateItem(),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Box(
                        modifier = Modifier
                          .size(52.dp)
                          .clip(CircleShape)
                          .background(
                            if (channel.id == "announcements") Color(0xFFEA0038).copy(alpha = 0.12f)
                            else if (channel.isDm) RoleAdminGold.copy(alpha = 0.15f)
                            else if (isDarkMode) Color(0xFF383838)
                            else WhatsAppNavSelectedPill
                          ),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          channel.name.take(1).uppercase(),
                          color = if (channel.id == "announcements") Color(0xFFEA0038)
                          else if (channel.isDm) RoleAdminGold
                          else if (isDarkMode) Color(0xFF00A884)
                          else WhatsAppGreenDark,
                          fontWeight = FontWeight.Bold,
                          fontSize = 20.sp
                        )
                      }
                      Spacer(modifier = Modifier.width(14.dp))
                      Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                              text = channel.name,
                              fontWeight = FontWeight.SemiBold,
                              color = textColor,
                              fontSize = 16.5.sp,
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis
                            )
                            if (channel.isDm) {
                              Spacer(modifier = Modifier.width(4.dp))
                              Text(stringResource(R.string.pm_tag), color = WhatsAppGreenDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            if (channel.isRestrictedToAdmin) {
                              Spacer(modifier = Modifier.width(4.dp))
                              Icon(Icons.Default.Lock, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(13.dp))
                            }
                          }
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isMuted) {
                              Icon(Icons.Default.VolumeOff, contentDescription = null, tint = subTextColor, modifier = Modifier.size(14.dp))
                              Spacer(modifier = Modifier.width(4.dp))
                            }
                            if (formattedTime.isNotBlank()) {
                              Text(formattedTime, color = if (isLastMessageRead) subTextColor else WhatsAppGreenDark, fontSize = 12.sp)
                            }
                          }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                          if (isLocalSender && channel.lastMessage != null) {
                            val tickIcon = when {
                              isLastMessageRead -> Icons.Default.DoneAll
                              channel.lastMessageIsDelivered -> Icons.Default.DoneAll
                              else -> Icons.Default.Check
                            }
                            val tickTint = if (isLastMessageRead) WhatsAppCheckmarkBlue else Color(0xFF8696A0)
                            Icon(
                              tickIcon,
                              contentDescription = null,
                              tint = tickTint,
                              modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                          }
                          Text(
                            text = displayPreview,
                            color = subTextColor,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                          )
                        }
                      }
                    }
                    HorizontalDivider(
                      color = if (isDarkMode) Color(0xFF333333) else menuLightBorder,
                      thickness = 0.5.dp,
                      modifier = Modifier.padding(start = 82.dp)
                    )
                  }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
              }
            }
          }
        }
      }
    }

    if (currentScreen in listOf("home", "profile_screen", "settings_screen", "contacts")) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        FloatingBottomNavBar(
          currentScreen = currentScreen,
          onNavigate = {
            if (it == "contacts" && userState.gamertag.isBlank()) {
              viewModel.setGamertagDialogVisible(true)
            } else {
              currentScreen = it
            }
          },
          userInitial = if (userState.gamertag.isNotBlank()) userState.gamertag.take(1).uppercase() else "?",
          isDarkMode = isDarkMode,
          modifier = Modifier.navigationBarsPadding().padding(bottom = 16.dp)
        )
      }
    }

    val fullScreenImageModel = remember(viewedImageUrl) {
      val url = viewedImageUrl ?: return@remember null
      if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("content://") || url.startsWith("file://")) {
        url
      } else if (url.contains("base64,")) {
        try {
          Base64.decode(url.substringAfter("base64,"), Base64.NO_WRAP)
        } catch (_: Exception) {
          url
        }
      } else {
        try {
          Base64.decode(url, Base64.NO_WRAP)
        } catch (_: Exception) {
          url
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
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = Color.White)
        }

        fullScreenImageModel?.let { model ->
          AsyncImage(
            model = model,
            imageLoader = gifImageLoader,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
          )
        }
      }
    }
  }

  incomingCall?.let { call ->
    AlertDialog(
      onDismissRequest = {},
      containerColor = if (isDarkMode) Color(0xFF1E2024) else Color.White,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = WhatsAppGreenDark)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Incoming ${if (call.isVideo) "Video" else "Voice"} Call")
        }
      },
      text = {
        Text("${call.caller} is calling you right now. Tap Accept to connect.", fontSize = 14.5.sp)
      },
      confirmButton = {
        Button(
          colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
          onClick = {
            viewModel.answerIncomingCall()
            if (call.isVideo) currentScreen = "video_call_screen"
          }
        ) {
          Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Accept")
        }
      },
      dismissButton = {
        Button(
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA0038)),
          onClick = { viewModel.declineIncomingCall() }
        ) {
          Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Decline")
        }
      }
    )
  }

  // Fallback Manual Coordinate Input Dialog
  manualLocationTargetMessage?.let { targetMsg ->
    AlertDialog(
      onDismissRequest = { manualLocationTargetMessage = null },
      containerColor = surfaceColor,
      titleContentColor = textColor,
      textContentColor = textColor,
      title = { Text("Share Coordinates", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            text = "No live coordinates were detected from the Minecraft server for ${userState.gamertag}. Please enter your coordinates manually:",
            fontSize = 13.5.sp,
            color = subTextColor
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = manualCoordsInput,
            onValueChange = { manualCoordsInput = it },
            placeholder = { Text("e.g. X: 120, Y: 64, Z: -350 (Overworld)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = textColor,
              unfocusedTextColor = textColor
            )
          )
        }
      },
      confirmButton = {
        Button(
          colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
          onClick = {
            if (manualCoordsInput.isNotBlank()) {
              viewModel.submitManualCoordinates(targetMsg, manualCoordsInput)
              manualLocationTargetMessage = null
            }
          }
        ) {
          Text("Send Location", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { manualLocationTargetMessage = null }) {
          Text("Cancel", color = subTextColor)
        }
      }
    )
  }

  activeNoteToView?.let { note ->
    InstagramNoteViewerDialog(
      note = note,
      isOwnNote = note.authorGamertag.equals(userState.gamertag, ignoreCase = true),
      isDarkMode = isDarkMode,
      onDismiss = { activeNoteToView = null },
      onDeleteNote = { id -> viewModel.deleteStatus(id) },
      onReplyNote = { recipientTag, msgText ->
        viewModel.startPrivateChat(recipientTag)
        currentScreen = "chat_screen"
        viewModel.sendMessage(
          content = "💭 Replied to note: \"$msgText\"",
          coordinates = null
        )
      },
      onEditNote = { showNoteComposer = true }
    )
  }

  if (showNoteComposer) {
    InstagramNoteComposerDialog(
      currentGamertag = userState.gamertag,
      isDarkMode = isDarkMode,
      onDismiss = { showNoteComposer = false },
      onShareNote = { thought, mName, mArtist, mPreview, mArtwork, mStartMs ->
        viewModel.postStatus(thought, "EMERALD", "Note", null, mName, mArtist, mPreview, mArtwork, mStartMs)
        showNoteComposer = false
      }
    )
  }

  if (showGamertagDialog) {
    GamertagDialog(
      currentGamertag = userState.gamertag,
      onDismiss = { viewModel.setGamertagDialogVisible(false) },
      onLogin = { tag, pass, res -> viewModel.loginAccount(tag, pass, res) },
      onRegister = { tag, pass, res -> viewModel.registerAccount(tag, pass, res) }
    )
  }

  if (longPressedChannel != null) {
    val ch = longPressedChannel!!
    val isMuted = viewModel.isChannelMuted(ch.id)
    val isFav = viewModel.isFavourite(ch.id)
    val isBlocked = ch.isDm && ch.dmRecipientGamertag != null && viewModel.isUserBlocked(ch.dmRecipientGamertag!!)

    CustomDropdownModalSheet(
      options = listOf(
        SheetOption("mute", if (isMuted) stringResource(R.string.menu_unmute_notifications) else stringResource(R.string.menu_mute_notifications), if (isMuted) stringResource(R.string.menu_unmute_sub) else stringResource(R.string.menu_mute_sub), isSelected = isMuted),
        SheetOption("fav", if (isFav) stringResource(R.string.menu_remove_favourites) else stringResource(R.string.menu_add_favourites), if (isFav) stringResource(R.string.menu_remove_fav_sub) else stringResource(R.string.menu_add_fav_sub), isSelected = isFav),
        if (ch.isDm && ch.dmRecipientGamertag != null) {
          SheetOption("block", if (isBlocked) stringResource(R.string.menu_unblock_user, ch.dmRecipientGamertag!!) else stringResource(R.string.menu_block_user, ch.dmRecipientGamertag!!), if (isBlocked) stringResource(R.string.menu_unblock_sub) else stringResource(R.string.menu_block_sub), isSelected = isBlocked)
        } else null
      ).filterNotNull(),
      isDarkMode = isDarkMode,
      onDismiss = { longPressedChannel = null },
      onOptionSelected = { opt ->
        when (opt.id) {
          "mute" -> viewModel.toggleMuteChannel(ch.id)
          "fav" -> viewModel.toggleFavourite(ch.id)
          "block" -> ch.dmRecipientGamertag?.let { viewModel.toggleBlockUser(it) }
        }
      }
    )
  }

  if (showMenuDropdownSheet) {
    val isDm = activeChannel.isDm
    val recipient = activeChannel.dmRecipientGamertag
    val isMuted = viewModel.isChannelMuted(activeChannel.id)
    val isFav = viewModel.isFavourite(activeChannel.id)
    val isBlocked = recipient != null && viewModel.isUserBlocked(recipient)

    CustomDropdownModalSheet(
      options = listOf(
        if (currentScreen == "chat_screen") SheetOption("mute", if (isMuted) stringResource(R.string.menu_unmute_notifications) else stringResource(R.string.menu_mute_notifications), if (isMuted) stringResource(R.string.menu_unmute_sub) else stringResource(R.string.menu_mute_sub), isSelected = isMuted) else null,
        if (currentScreen == "chat_screen") SheetOption("fav", if (isFav) stringResource(R.string.menu_remove_favourites) else stringResource(R.string.menu_add_favourites), if (isFav) stringResource(R.string.menu_remove_fav_sub) else stringResource(R.string.menu_add_fav_sub), isSelected = isFav) else null,
        if (currentScreen == "chat_screen" && isDm && recipient != null) SheetOption("block", if (isBlocked) stringResource(R.string.menu_unblock_user, recipient) else stringResource(R.string.menu_block_user, recipient), if (isBlocked) stringResource(R.string.menu_unblock_sub) else stringResource(R.string.menu_block_sub), isSelected = isBlocked) else null,
        SheetOption("post_note", "Leave a Note", "Share a thought & 30s music clip above your avatar", isSelected = false),
        SheetOption("server", stringResource(R.string.menu_server_ip), stringResource(R.string.default_server_ip), isSelected = false),
        if (userState.isAdmin) SheetOption("admin", stringResource(R.string.menu_admin_console), stringResource(R.string.menu_admin_console_sub), isSelected = false) else null
      ).filterNotNull(),
      isDarkMode = isDarkMode,
      onDismiss = { showMenuDropdownSheet = false },
      onOptionSelected = { option ->
        when (option.id) {
          "mute" -> viewModel.toggleMuteChannel(activeChannel.id)
          "fav" -> viewModel.toggleFavourite(activeChannel.id)
          "block" -> recipient?.let { viewModel.toggleBlockUser(it) }
          "post_note" -> {
            if (userState.gamertag.isBlank()) viewModel.setGamertagDialogVisible(true)
            else showNoteComposer = true
          }
          "server" -> viewModel.setServerInfoSheetVisible(true)
          "admin" -> viewModel.setAdminConsoleVisible(true)
        }
      }
    )
  }

  if (showAdminConsole && userState.isAdmin) {
    AdminControlSheet(
      channels = channels,
      members = members,
      isDarkMode = isDarkMode,
      onDismiss = { viewModel.setAdminConsoleVisible(false) },
      onCreateChannel = { name, type, desc, onlyAdmin -> viewModel.createChannel(name, type, desc, onlyAdmin) },
      onDeleteChannel = { id -> viewModel.deleteChannel(id) },
      onToggleChannelPermission = { ch -> viewModel.toggleChannelPermission(ch) },
      onUpdateMemberRole = { id, role -> viewModel.updateMemberRole(id, role) },
      onUpdateMemberGamertag = { id, name -> viewModel.updateMemberGamertag(id, name) },
      onRemoveMember = { id -> viewModel.removeMember(id) }
    )
  }

  if (showInviteToCallDialog) {
    SelectContactDialog(
      members = members,
      currentGamertag = userState.gamertag,
      isDarkMode = isDarkMode,
      onDismiss = { showInviteToCallDialog = false },
      onSelectMember = { member ->
        activeVoiceRoom?.let { room ->
          viewModel.inviteToCall(member.gamertag, room.channelId, room.channelName, room.isCameraOn)
        }
        showInviteToCallDialog = false
      }
    )
  }

  if (showServerInfoSheet) {
    ServerInfoSheet(onDismiss = { viewModel.setServerInfoSheetVisible(false) }, isDarkMode = isDarkMode)
  }
}
