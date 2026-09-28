package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.model.StatusUpdate
import com.joseph.substratesmp.ui.theme.RoleAdminGold
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatStatusTime(timestamp: Long): String {
  val diff = System.currentTimeMillis() - timestamp
  return when {
    diff < 60_000L -> "Just now"
    diff < 3600_000L -> "${diff / 60_000L}m ago"
    diff < 86400_000L -> "${diff / 3600_000L}h ago"
    else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
  }
}

@Composable
fun StatusScreen(
  statuses: List<StatusUpdate>,
  currentGamertag: String,
  isDarkMode: Boolean,
  onOpenStatus: (StatusUpdate) -> Unit,
  onPostStatusClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bgColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFF4F5F8)
  val surfaceColor = if (isDarkMode) Color(0xFF303030) else Color.White
  val textColor = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF111B21)
  val subTextColor = if (isDarkMode) Color(0xFFA0A0A5) else Color(0xFF667781)
  val dividerColor = if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFE2E4E8)

  val myStatus = statuses.find { it.authorGamertag.equals(currentGamertag, ignoreCase = true) }
  val otherStatuses = statuses.filter { !it.authorGamertag.equals(currentGamertag, ignoreCase = true) }

  Scaffold(
    topBar = {
      Column(modifier = Modifier.background(bgColor).statusBarsPadding()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Status",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 26.sp
          )

          IconButton(onClick = onPostStatusClick) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Status", tint = WhatsAppGreenDark)
          }
        }
      }
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onPostStatusClick,
        containerColor = WhatsAppGreenDark,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 76.dp)
      ) {
        Icon(Icons.Default.Edit, contentDescription = "Post Status")
      }
    },
    containerColor = bgColor,
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
    ) {
      // My Status Card
      item {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = surfaceColor,
          border = BorderStroke(1.dp, dividerColor),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
              if (myStatus != null) onOpenStatus(myStatus) else onPostStatusClick()
            }
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
              Box(
                modifier = Modifier
                  .size(52.dp)
                  .clip(CircleShape)
                  .background(if (isDarkMode) Color(0xFF383838) else Color(0xFFE9EDEF))
                  .then(
                    if (myStatus != null) Modifier.border(2.5.dp, WhatsAppGreenDark, CircleShape)
                    else Modifier
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (currentGamertag.isNotBlank()) currentGamertag.take(1).uppercase() else "+",
                  color = if (myStatus != null) WhatsAppGreenDark else textColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp
                )
              }

              Box(
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .size(18.dp)
                  .clip(CircleShape)
                  .background(WhatsAppGreenDark)
                  .border(1.5.dp, surfaceColor, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (myStatus != null) Icons.Default.Check else Icons.Default.Add,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(12.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "My Status",
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                color = textColor
              )
              Text(
                text = if (myStatus != null) {
                  if (!myStatus.musicTrackName.isNullOrBlank()) "🎵 ${myStatus.musicTrackName} • ${formatStatusTime(myStatus.timestamp)}"
                  else "Tap to view update • ${formatStatusTime(myStatus.timestamp)}"
                } else "Tap to add realm adventure update",
                fontSize = 13.sp,
                color = subTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            IconButton(onClick = onPostStatusClick) {
              Icon(Icons.Default.CameraAlt, contentDescription = null, tint = WhatsAppGreenDark)
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "RECENT UPDATES (${otherStatuses.size})",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = subTextColor,
          letterSpacing = 0.5.sp,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
      }

      if (otherStatuses.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, dividerColor),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
          ) {
            Column(
              modifier = Modifier.padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = subTextColor, modifier = Modifier.size(42.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text("No Recent Status Updates", fontWeight = FontWeight.Bold, color = textColor, fontSize = 15.sp)
              Text("When friends share updates or soundtracks, they'll appear here.", color = subTextColor, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            }
          }
        }
      } else {
        items(otherStatuses, key = { it.id }) { status ->
          val themeColor = when (status.backgroundTheme) {
            "CRIMSON" -> Color(0xFFFF4500)
            "END_VOID" -> Color(0xFF9C27B0)
            "DIAMOND" -> Color(0xFF00E5FF)
            "GOLDEN" -> Color(0xFFFFD700)
            "OBSIDIAN" -> Color(0xFF607D8B)
            else -> WhatsAppGreenDark
          }

          Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, dividerColor),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 5.dp)
              .clickable { onOpenStatus(status) }
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(50.dp)
                  .clip(CircleShape)
                  .border(2.5.dp, themeColor, CircleShape)
                  .padding(3.dp)
                  .clip(CircleShape)
                  .background(themeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = status.authorGamertag.take(1).uppercase(),
                  color = if (isDarkMode) Color.White else themeColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = status.authorGamertag,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                  )
                  if (status.isAdmin) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Shield, contentDescription = null, tint = RoleAdminGold, modifier = Modifier.size(13.dp))
                  }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "${status.activityTag} • ${formatStatusTime(status.timestamp)}",
                    fontSize = 12.5.sp,
                    color = subTextColor
                  )
                }
                if (!status.musicTrackName.isNullOrBlank()) {
                  Text(
                    text = "🎵 ${status.musicTrackName} - ${status.musicArtistName ?: ""}",
                    fontSize = 12.5.sp,
                    color = Color(0xFF00A884),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                  )
                } else if (status.content.isNotBlank()) {
                  Text(
                    text = status.content,
                    fontSize = 13.5.sp,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                  )
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(110.dp))
      }
    }
  }
}
