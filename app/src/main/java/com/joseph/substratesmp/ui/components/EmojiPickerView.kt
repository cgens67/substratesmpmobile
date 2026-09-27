package com.joseph.substratesmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseph.substratesmp.ui.theme.WhatsAppDivider
import com.joseph.substratesmp.ui.theme.WhatsAppNavSelectedPill
import com.joseph.substratesmp.ui.theme.WhatsAppTextSecondary

@Composable
fun EmojiPickerView(
  onEmojiSelected: (String) -> Unit,
  onBackspace: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableIntStateOf(0) }

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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                .padding(horizontal = 10.dp, vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(text = icon, fontSize = 18.sp)
            }
          }
        }

        IconButton(
          onClick = onBackspace,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            tint = WhatsAppTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      HorizontalDivider(color = WhatsAppDivider, thickness = 0.5.dp)

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
