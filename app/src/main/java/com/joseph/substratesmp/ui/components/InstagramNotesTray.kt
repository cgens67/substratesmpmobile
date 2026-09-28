package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.model.StatusUpdate
import com.joseph.substratesmp.ui.theme.RoleAdminGold

@Composable
fun InstagramNotesTray(
  notes: List<StatusUpdate>,
  currentGamertag: String,
  isDarkMode: Boolean,
  onOpenNote: (StatusUpdate) -> Unit,
  onAddNote: () -> Unit,
  modifier: Modifier = Modifier
) {
  val textColor = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF111B21)
  val subTextColor = if (isDarkMode) Color(0xFFA0A0A5) else Color(0xFF8E9297)
  val bubbleBg = if (isDarkMode) Color(0xFF2C2F36) else Color.White
  val blueAccent = Color(0xFF007AFF)

  val myNote = notes.find { it.authorGamertag.equals(currentGamertag, ignoreCase = true) }
  val friendNotes = notes.filter { !it.authorGamertag.equals(currentGamertag, ignoreCase = true) }

  LazyRow(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.Top
  ) {
    // "Your Note" (Instagram Style)
    item {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .width(78.dp)
          .clickable {
            if (myNote != null) onOpenNote(myNote) else onAddNote()
          }
      ) {
        Box(
          modifier = Modifier.height(98.dp),
          contentAlignment = Alignment.BottomCenter
        ) {
          // Floating Thought Bubble above head
          Box(
            modifier = Modifier
              .align(Alignment.TopCenter)
              .shadow(4.dp, RoundedCornerShape(14.dp))
              .clip(RoundedCornerShape(14.dp))
              .background(bubbleBg)
              .padding(horizontal = 8.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
          ) {
            if (myNote != null) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (!myNote.musicTrackName.isNullOrBlank()) {
                  Icon(Icons.Default.MusicNote, contentDescription = null, tint = blueAccent, modifier = Modifier.size(11.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                  text = myNote.content.ifBlank { myNote.musicTrackName ?: "Note" },
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Medium,
                  color = textColor,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            } else {
              Text(
                text = "Note...",
                fontSize = 10.5.sp,
                color = subTextColor,
                maxLines = 1
              )
            }
          }

          // Avatar with (+) badge
          Box(
            modifier = Modifier.size(62.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isDarkMode) Color(0xFF383838) else Color(0xFFE9EDEF))
                .border(2.dp, if (myNote != null) blueAccent else Color.Transparent, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (currentGamertag.isNotBlank()) currentGamertag.take(1).uppercase() else "+",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
              )
            }

            if (myNote == null) {
              Box(
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(blueAccent)
                  .border(2.dp, if (isDarkMode) Color(0xFF262626) else Color.White, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Your note",
          fontSize = 11.5.sp,
          color = subTextColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // Friend Notes
    items(friendNotes, key = { it.id }) { note ->
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .width(78.dp)
          .clickable { onOpenNote(note) }
      ) {
        Box(
          modifier = Modifier.height(98.dp),
          contentAlignment = Alignment.BottomCenter
        ) {
          // Floating Thought Bubble
          Box(
            modifier = Modifier
              .align(Alignment.TopCenter)
              .shadow(4.dp, RoundedCornerShape(14.dp))
              .clip(RoundedCornerShape(14.dp))
              .background(bubbleBg)
              .padding(horizontal = 8.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (!note.musicTrackName.isNullOrBlank()) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = blueAccent, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(3.dp))
              }
              Text(
                text = note.content.ifBlank { note.musicTrackName ?: "Note" },
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Avatar
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(if (note.isAdmin) RoleAdminGold.copy(alpha = 0.25f) else if (isDarkMode) Color(0xFF383838) else Color(0xFFE9EDEF))
              .border(2.dp, if (note.isAdmin) RoleAdminGold else blueAccent, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = note.authorGamertag.take(1).uppercase(),
              color = if (note.isAdmin) RoleAdminGold else textColor,
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = note.authorGamertag,
          fontSize = 11.5.sp,
          color = textColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
