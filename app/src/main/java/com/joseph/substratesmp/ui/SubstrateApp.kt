package com.joseph.substratesmp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.ui.components.ActiveVoiceBar
import com.joseph.substratesmp.ui.components.AgoraSettingsDialog
import com.joseph.substratesmp.ui.components.ChannelDrawerContent
import com.joseph.substratesmp.ui.components.ChatInputBar
import com.joseph.substratesmp.ui.components.ChatMessageItem
import com.joseph.substratesmp.ui.components.GamertagDialog
import com.joseph.substratesmp.ui.components.ServerInfoSheet
import com.joseph.substratesmp.ui.theme.SubstrateTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstrateApp(
  viewModel: SubstrateViewModel,
  modifier: Modifier = Modifier
) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val channels by viewModel.channels.collectAsStateWithLifecycle()
  val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()
  val userState by viewModel.userState.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val activeVoiceRoom by viewModel.activeVoiceRoom.collectAsStateWithLifecycle()
  val agoraSettings by viewModel.agoraSettings.collectAsStateWithLifecycle()

  val showGamertagDialog by viewModel.showGamertagDialog.collectAsStateWithLifecycle()
  val showAgoraDialog by viewModel.showAgoraDialog.collectAsStateWithLifecycle()
  val showServerInfoSheet by viewModel.showServerInfoSheet.collectAsStateWithLifecycle()

  var menuExpanded by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  // Auto-scroll to bottom whenever messages update
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
      ) {
        ChannelDrawerContent(
          channels = channels,
          activeChannel = activeChannel,
          userState = userState,
          onSelectChannel = { channel ->
            viewModel.selectChannel(channel)
            scope.launch { drawerState.close() }
          },
          onOpenGamertagDialog = { viewModel.setGamertagDialogVisible(true) },
          onOpenAgoraSettings = { viewModel.setAgoraDialogVisible(true) },
          onOpenServerInfo = { viewModel.setServerInfoSheetVisible(true) }
        )
      }
    }
  ) {
    Scaffold(
      modifier = modifier
        .fillMaxSize()
        .testTag("substrate_main_scaffold"),
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        TopAppBar(
          title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (activeChannel.id == "announcements") Icons.Default.Campaign else Icons.Default.Tag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = activeChannel.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = activeChannel.description,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  fontSize = 11.sp
                )
              }
            }
          },
          navigationIcon = {
            IconButton(
              onClick = { scope.launch { drawerState.open() } },
              modifier = Modifier.testTag("drawer_menu_button")
            ) {
              Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open Channels",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          },
          actions = {
            // Realm Server Info shortcut
            IconButton(
              onClick = { viewModel.setServerInfoSheetVisible(true) },
              modifier = Modifier.testTag("app_bar_server_info")
            ) {
              Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = "Server Status",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // More Options Dropdown
            Box {
              IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("app_bar_more_menu")
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "More Options",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
              ) {
                DropdownMenuItem(
                  text = { Text("Gamertag: ${userState.gamertag}") },
                  onClick = {
                    menuExpanded = false
                    viewModel.setGamertagDialogVisible(true)
                  }
                )
                DropdownMenuItem(
                  text = { Text("Agora Voice Settings") },
                  onClick = {
                    menuExpanded = false
                    viewModel.setAgoraDialogVisible(true)
                  }
                )
                DropdownMenuItem(
                  text = { Text("Bedrock Server Details") },
                  onClick = {
                    menuExpanded = false
                    viewModel.setServerInfoSheetVisible(true)
                  }
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
          )
        )
      }
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .background(MaterialTheme.colorScheme.background)
      ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // 1. Scrollable Real-Time Message Stream
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
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Default.Tag,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                  modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "Welcome to #${activeChannel.name}!",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "This is the start of the #${activeChannel.name} channel.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          } else {
            LazyColumn(
              state = listState,
              modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
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

        // 2. Active Voice Bar (Docked card when connected to voice channel)
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

        // 3. Pinned Bottom Text Input Bar
        ChatInputBar(
          channelName = activeChannel.name,
          onSendMessage = { content, coords ->
            viewModel.sendMessage(content, coords)
          }
        )
      }
    }
  }

  // Dialogs
  if (showGamertagDialog) {
    GamertagDialog(
      currentGamertag = userState.gamertag,
      currentRole = userState.role,
      onDismiss = { viewModel.setGamertagDialogVisible(false) },
      onSave = { gamertag, role ->
        viewModel.updateGamertag(gamertag, role)
      }
    )
  }

  if (showAgoraDialog) {
    AgoraSettingsDialog(
      currentSettings = agoraSettings,
      onDismiss = { viewModel.setAgoraDialogVisible(false) },
      onSave = { appId, token ->
        viewModel.voiceManager.updateSettings(appId, token)
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
