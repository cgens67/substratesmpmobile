package com.joseph.substratesmp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.R
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.data.repository.AuthUserState
import com.joseph.substratesmp.ui.theme.ChannelPillShape
import com.joseph.substratesmp.ui.theme.CoordinateTextStyle
import com.joseph.substratesmp.ui.theme.ServerBadgeShape
import com.joseph.substratesmp.ui.theme.SubstrateTheme

@Composable
fun ChannelDrawerContent(
  channels: List<Channel>,
  activeChannel: Channel,
  userState: AuthUserState,
  onSelectChannel: (Channel) -> Unit,
  onOpenGamertagDialog: () -> Unit,
  onOpenAgoraSettings: () -> Unit,
  onOpenServerInfo: () -> Unit,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current

  Surface(
    modifier = modifier
      .fillMaxHeight()
      .width(310.dp)
      .testTag("channel_drawer_surface"),
    color = MaterialTheme.colorScheme.surfaceContainerLowest
  ) {
    Column(modifier = Modifier.fillMaxHeight()) {
      // 1. Server Header Banner & Info
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.substrate_banner),
          contentDescription = "Substrate SMP Server Banner",
          modifier = Modifier.fillMaxWidth().height(130.dp),
          contentScale = ContentScale.Crop
        )

        // Gradient overlay for readability
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Transparent,
                  MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f)
                )
              )
            )
        )

        Row(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(ServerBadgeShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "SMP",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Substrate SMP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(SubstrateTheme.customColors.statusOnline)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "Bedrock v1.21 • 14 Online",
                  style = MaterialTheme.typography.labelSmall,
                  color = SubstrateTheme.customColors.statusOnline
                )
              }
            }
          }

          IconButton(
            onClick = onOpenServerInfo,
            modifier = Modifier.size(32.dp).testTag("server_info_button")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Server info",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // 2. Channel Lists (Text & Voice)
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        // Category: TEXT CHANNELS
        item {
          CategoryHeader(title = "TEXT CHANNELS")
        }

        val textChannels = channels.filter { it.type == ChannelType.TEXT }
        items(textChannels.size) { index ->
          val channel = textChannels[index]
          val isSelected = channel.id == activeChannel.id

          ChannelItemRow(
            name = channel.name,
            icon = if (channel.id == "announcements") Icons.Default.Campaign else Icons.Default.Tag,
            isSelected = isSelected,
            badgeCount = channel.unreadCount,
            onClick = { onSelectChannel(channel) }
          )
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
          CategoryHeader(title = "VOICE CHANNELS")
        }

        val voiceChannels = channels.filter { it.type == ChannelType.VOICE }
        items(voiceChannels.size) { index ->
          val channel = voiceChannels[index]
          val isSelected = channel.id == activeChannel.id

          VoiceChannelItemRow(
            name = channel.name,
            activeCount = channel.activeUsersCount,
            onClick = { onSelectChannel(channel) }
          )
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
          CategoryHeader(title = "BEDROCK REALM SERVER")
          Surface(
            shape = ChannelPillShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                clipboardManager.setText(AnnotatedString("mc.substratesmp.net:19132"))
              }
              .padding(vertical = 2.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "mc.substratesmp.net",
                  style = CoordinateTextStyle,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Port: 19132 (Tap to copy)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy IP",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // 3. User Gamertag Status Footer (Discord-style)
      Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("user_profile_bar")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .weight(1f)
              .clickable { onOpenGamertagDialog() }
          ) {
            // Avatar badge
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, SubstrateTheme.customColors.statusOnline, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = userState.gamertag.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
              Text(
                text = userState.gamertag,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = MaterialTheme.shapes.extraSmall,
                  color = when (userState.role) {
                    "ADMIN" -> SubstrateTheme.customColors.adminGold.copy(alpha = 0.2f)
                    "MOD" -> SubstrateTheme.customColors.modCyan.copy(alpha = 0.2f)
                    else -> SubstrateTheme.customColors.memberGreen.copy(alpha = 0.2f)
                  }
                ) {
                  Text(
                    text = userState.role,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (userState.role) {
                      "ADMIN" -> SubstrateTheme.customColors.adminGold
                      "MOD" -> SubstrateTheme.customColors.modCyan
                      else -> SubstrateTheme.customColors.memberGreen
                    },
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                  )
                }
              }
            }
          }

          Row {
            IconButton(
              onClick = onOpenGamertagDialog,
              modifier = Modifier.size(34.dp).testTag("edit_gamertag_button")
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change Gamertag",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }

            IconButton(
              onClick = onOpenAgoraSettings,
              modifier = Modifier.size(34.dp).testTag("agora_settings_button")
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Agora Voice Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CategoryHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.8.sp,
    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
  )
}

@Composable
fun ChannelItemRow(
  name: String,
  icon: ImageVector,
  isSelected: Boolean,
  badgeCount: Int = 0,
  onClick: () -> Unit
) {
  val backgroundColor by animateColorAsState(
    targetValue = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
    label = "channel_bg"
  )
  val contentColor by animateColorAsState(
    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    label = "channel_fg"
  )

  Surface(
    shape = ChannelPillShape,
    color = backgroundColor,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp)
      .clickable { onClick() }
      .testTag("channel_item_$name")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = contentColor,
          modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = name,
          style = MaterialTheme.typography.bodyMedium,
          color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
      }

      if (badgeCount > 0) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "$badgeCount",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun VoiceChannelItemRow(
  name: String,
  activeCount: Int,
  onClick: () -> Unit
) {
  Surface(
    shape = ChannelPillShape,
    color = Color.Transparent,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp)
      .clickable { onClick() }
      .testTag("voice_channel_$name")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.VolumeUp,
          contentDescription = null,
          tint = SubstrateTheme.customColors.statusVoiceActive,
          modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = name,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Medium
        )
      }

      if (activeCount > 0) {
        Surface(
          shape = MaterialTheme.shapes.extraSmall,
          color = SubstrateTheme.customColors.statusVoiceActive.copy(alpha = 0.15f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(SubstrateTheme.customColors.statusVoiceActive)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$activeCount",
              style = MaterialTheme.typography.labelSmall,
              color = SubstrateTheme.customColors.statusVoiceActive,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          }
        }
      }
    }
  }
}
