package com.joseph.substratesmp.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.data.model.ServerSticker
import com.joseph.substratesmp.ui.theme.WhatsAppDivider
import com.joseph.substratesmp.ui.theme.WhatsAppGreenDark
import com.joseph.substratesmp.ui.theme.WhatsAppNavSelectedPill
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary
import java.text.BreakIterator

fun dropLastGrapheme(str: String): String {
  if (str.isEmpty()) return ""
  val boundary = BreakIterator.getCharacterInstance()
  boundary.setText(str)
  val last = boundary.last()
  val previous = boundary.previous()
  return if (previous != BreakIterator.DONE) {
    str.substring(0, previous)
  } else {
    ""
  }
}

@Composable
fun EmojiPickerView(
  stickers: List<ServerSticker> = emptyList(),
  isAdmin: Boolean = false,
  onOpenCreateSticker: () -> Unit = {},
  onEmojiSelected: (String) -> Unit,
  onStickerSelected: (ServerSticker) -> Unit = {},
  onDeleteSticker: (String) -> Unit = {},
  onBackspace: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableIntStateOf(0) }
  var stickerToDelete by remember { mutableStateOf<ServerSticker?>(null) }

  val categories = listOf(
    "Trending" to listOf(
      "🫪", "🥀", "🙏", "🏳️‍🌈", "💅🏿", "🥵", "🥱", "🌝", "🤗", "💩",
      "🙄", "😒", "😮‍💨", "🤨", "😱", "😳", "🫩", "👄", "🫦", "🏳️‍⚧️",
      "🇦🇶", "🇲🇾", "✅", "❌", "🌈", "🏜️", "🖕"
    ),
    "Smileys" to listOf(
      "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
      "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
      "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
      "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
      "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "😎", "🤓", "🧐",
      "😮‍💨", "🥱", "🌝", "💀", "💩", "🤡", "👹", "👺", "👻", "👽"
    ),
    "Hands" to listOf(
      "🙏", "💅🏿", "🖕", "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌",
      "🤏", "✌️", "🤞", "🤟", "🤘", "🤙", "👈", "👉", "👆", "👇",
      "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲"
    ),
    "Flags & Nature" to listOf(
      "🇲🇾", "🇦🇶", "🏳️‍🌈", "🏳️‍⚧️", "🥀", "🫪", "🫩", "🌈", "🏜️", "✅",
      "❌", "👄", "🫦", "❤️", "🔥", "✨", "🌟", "💫", "💥", "💯"
    ),
    "Minecraft" to listOf(
      "⛏️", "💎", "⚔️", "🛡️", "🏹", "🪓", "🧪", "🍎", "🥩", "🍞",
      "🪵", "🧱", "📦", "🧭", "🗺️", "🪙", "👑", "🧟", "🐉", "🏕️",
      "🏠", "🎮", "🕹️", "🎲", "🏆", "🥇", "🥈", "🥉", "🎯", "⚡"
    )
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(260.dp),
    color = Color(0xFFF7F8FA)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp)

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White)
          .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
          categories.forEachIndexed { index, _ ->
            val icon = when (index) {
              0 -> "🇲🇾"
              1 -> "😀"
              2 -> "🙏"
              3 -> "🥀"
              else -> "⛏️"
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedCategory == index) WhatsAppNavSelectedPill else Color.Transparent)
                .clickable { selectedCategory = index }
                .padding(horizontal = 8.dp, vertical = 5.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(text = icon, fontSize = 17.sp)
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (selectedCategory == 99) WhatsAppNavSelectedPill else Color.Transparent)
              .clickable { selectedCategory = 99 }
              .padding(horizontal = 8.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "💟", fontSize = 17.sp)
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (selectedCategory == 99 && isAdmin) {
            IconButton(onClick = onOpenCreateSticker, modifier = Modifier.size(36.dp)) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Sticker", tint = WhatsAppGreenDark, modifier = Modifier.size(20.dp))
            }
          }

          if (selectedCategory != 99) {
            IconButton(onClick = onBackspace, modifier = Modifier.size(36.dp)) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = WhatsAppTextSecondary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp)

      if (selectedCategory == 99) {
        if (stickers.isEmpty()) {
          Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("No Server Stickers Yet", fontWeight = FontWeight.Bold, color = WhatsAppTextSecondary, fontSize = 14.sp)
              if (isAdmin) {
                Text("Admins can tap + above to add stickers", color = WhatsAppGreenDark, fontSize = 12.sp, modifier = Modifier.clickable { onOpenCreateSticker() })
              }
            }
          }
        } else {
          LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(stickers) { sticker ->
              val stickerBitmap = remember(sticker.imageData) {
                try {
                  val raw = sticker.imageData.substringAfter("base64,")
                  val bytes = Base64.decode(raw, Base64.NO_WRAP)
                  BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                } catch (_: Exception) {
                  null
                }
              }

              if (stickerBitmap != null) {
                Box(
                  modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .combinedClickable(
                      onClick = { onStickerSelected(sticker) },
                      onLongClick = { if (isAdmin) stickerToDelete = sticker }
                    )
                    .padding(4.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Image(
                    bitmap = stickerBitmap,
                    contentDescription = sticker.name,
                    modifier = Modifier.size(68.dp),
                    contentScale = ContentScale.Fit
                  )
                }
              }
            }
          }
        }
      } else {
        val currentEmojis = categories[selectedCategory].second
        LazyVerticalGrid(
          columns = GridCells.Fixed(7),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
          items(currentEmojis) { emoji ->
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable { onEmojiSelected(emoji) },
              contentAlignment = Alignment.Center
            ) {
              Text(text = emoji, fontSize = 23.sp)
            }
          }
        }
      }
    }
  }

  if (stickerToDelete != null) {
    AlertDialog(
      onDismissRequest = { stickerToDelete = null },
      title = { Text("Delete Sticker?") },
      text = { Text("Remove this sticker from the server? This cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteSticker(stickerToDelete!!.id)
            stickerToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA0038))
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { stickerToDelete = null }) { Text("Cancel") }
      }
    )
  }
}
