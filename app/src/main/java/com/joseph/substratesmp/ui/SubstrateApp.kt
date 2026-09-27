package com.joseph.substratesmp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.joseph.substratesmp.ui.components.GamertagDialog
import com.joseph.substratesmp.ui.components.ServerInfoSheet
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.SubstrateTheme
import com.joseph.substratesmp.ui.theme.WhatsAppBackgroundDark
import com.joseph.substratesmp.ui.theme.WhatsAppChatIncoming
import com.joseph.substratesmp.ui.theme.WhatsAppChatOutgoing
import com.joseph.substratesmp.ui.theme.WhatsAppGreenPrimary
import com.joseph.substratesmp.ui.theme.WhatsAppTealHeader
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstrateApp(
  viewModel: SubstrateViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val channels by viewModel.channels.collectAsStateWithLifecycle()
  val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()
  val userState by viewModel.userState.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val activeVoiceRoom by viewModel.activeVoiceRoom.collectAsStateWithLifecycle()
  val agoraSettings by viewModel.agoraSettings.collectAsStateWithLifecycle()

  val showGamertagDialog by viewModel.showGamertagDialog.collectAsStateWithLifecycle()
  val showAgoraDialog by viewModel.showAgoraDialog.collectAsStateWithLifecycle()
  val showServerInfoSheet by viewModel.showServerInfoSheet.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Chats, 1: Calls, 2: Realm Status
  var menuExpanded by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  // Camera & Audio Permission Launcher
  var pendingVoiceChannel by remember { mutableStateOf<Channel?>(null) }
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
    val cameraGranted = permissions[Manifest.permission.CAMERA] == true
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

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("substrate_main_scaffold"),
    containerColor = WhatsAppBackgroundDark,
    topBar = {
      Column(modifier = Modifier.background(WhatsAppTealHeader)) {
        // Top App Header
        TopAppBar(
          title = {
            Column {
              Text(
                text = "Substrate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = if (userState.isAdmin) "${userState.gamertag} (ADMIN)" else userState.gamertag,
                style = MaterialTheme.typography.labelSmall,
                color = if (userState.isAdmin) RoleAdminGold else WhatsAppGreenPrimary,
                fontSize = 11.sp
              )
            }
          },
          actions = {
            IconButton(onClick = { viewModel.setServerInfoSheetVisible(true) }) {
              Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = "Server IP",
                tint = Color.White
              )
            }

            Box {
              IconButton(onClick = { menuExpanded = true }) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "Options",
                  tint = Color.White
                )
              }

              DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = WhatsAppChatIncoming
              ) {
                DropdownMenuItem(
                  text = { Text("Gamertag: ${userState.gamertag.ifBlank { "Not set" }}", color = Color.White) },
                  onClick = {
                    menuExpanded = false
                    viewModel.setGamertagDialogVisible(true)
                  }
                )
                DropdownMenuItem(
                  text = { Text("Agora Call Settings", color = Color.White) },
                  onClick = {
                    menuExpanded = false
                    viewModel.setAgoraDialogVisible(true)
                  }
                )
                DropdownMenuItem(
                  text = { Text("Bedrock Realm Details", color = Color.White) },
                  onClick = {
                    menuExpanded = false
                    viewModel.setServerInfoSheetVisible(true)
                  }
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = WhatsAppTealHeader
          )
        )

        // Top Navigation Tabs: CHATS | CALLS | REALM
        PrimaryTabRow(
          selectedTabIndex = selectedTab,
          containerColor = WhatsAppTealHeader,
          contentColor = WhatsAppGreenPrimary
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("CHATS", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) WhatsAppGreenPrimary else WhatsAppTextSecondary) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("CALLS", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) WhatsAppGreenPrimary else WhatsAppTextSecondary) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = { Text("REALM", fontWeight = FontWeight.Bold, color = if (selectedTab == 2) WhatsAppGreenPrimary else WhatsAppTextSecondary) }
          )
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(WhatsAppBackgroundDark)
    ) {
      when (selectedTab) {
        // Tab 0: WhatsApp-Style Conversation Stream
        0 -> {
          // Channel selector chip row
          val textChannels = channels.filter { it.type == ChannelType.TEXT }
          LazyRow(
            modifier = Modifier
              .fillMaxWidth()
              .background(WhatsAppTealHeader.copy(alpha = 0.6f))
              .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            items(textChannels, key = { it.id }) { channel ->
              val isSelected = channel.id == activeChannel.id
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) WhatsAppGreenPrimary else WhatsAppChatIncoming,
                modifier = Modifier.clickable { viewModel.selectChannel(channel) }
              ) {
                Text(
                  text = "#${channel.name}",
                  color = if (isSelected) Color.Black else Color.White,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }

          // Message Stream
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
                Text(
                  text = "Welcome to #${activeChannel.name}",
                  color = WhatsAppTextSecondary,
                  style = MaterialTheme.typography.bodyMedium
                )
              }
            } else {
              LazyColumn(
                state = listState,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(vertical = 6.dp)
              ) {
                items(
                  items = messages,
                  key = { it.id }
                ) { message ->
                  ChatMessageItem(message = message)
                }
              }
            }
          }

          // In-call Floating Card
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

          // Pinned Bottom WhatsApp Input Bar
          ChatInputBar(
            channelName = activeChannel.name,
            onSendMessage = { content, coords ->
              viewModel.sendMessage(content, coords)
            }
          )
        }

        // Tab 1: Calls Tab (Agora Live Channels with Real Gamertags)
        1 -> {
          val voiceChannels = channels.filter { it.type == ChannelType.VOICE }
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(12.dp)
          ) {
            item {
              Text(
                text = "Voice & Video Channels",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            }

            items(voiceChannels) { channel ->
              val isCurrent = activeVoiceRoom?.channelId == channel.id
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppChatIncoming),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) WhatsAppGreenPrimary else WhatsAppTealHeader),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = if (isCurrent) Color.Black else WhatsAppGreenPrimary,
                        modifier = Modifier.size(22.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                      Text(
                        text = channel.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = WhatsAppTextSecondary,
                        maxLines = 1
                      )
                    }
                  }

                  IconButton(
                    onClick = {
                      if (isCurrent) {
                        viewModel.voiceManager.disconnect()
                      } else {
                        requestCallPermissionsAndJoin(channel)
                      }
                    },
                    modifier = Modifier
                      .size(42.dp)
                      .clip(CircleShape)
                      .background(if (isCurrent) SubstrateTheme.customColors.statusMuted else WhatsAppGreenPrimary)
                  ) {
                    Icon(
                      imageVector = if (isCurrent) Icons.Default.CallEnd else Icons.Default.PhoneInTalk,
                      contentDescription = if (isCurrent) "Hang Up" else "Join Call",
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // Tab 2: Bedrock Realm Server Info
        2 -> {
          Box(modifier = Modifier.fillMaxSize()) {
            ServerInfoSheet(onDismiss = {})
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

  if (showServerInfoSheet && selectedTab != 2) {
    ServerInfoSheet(
      onDismiss = { viewModel.setServerInfoSheetVisible(false) }
    )
  }
}
