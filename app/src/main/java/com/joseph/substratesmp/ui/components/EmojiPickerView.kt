package com.joseph.substratesmp.ui.components

import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
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

object FavoriteEmojiStickerManager {
  private const val PREFS_NAME = "substrate_fav_emoji_stickers"
  private const val KEY_FAV_EMOJIS = "fav_emojis"
  private const val KEY_FAV_STICKER_IDS = "fav_sticker_ids"

  fun getFavoriteEmojis(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (!prefs.contains(KEY_FAV_EMOJIS)) {
      val initialDefaults = setOf("🔥", "❤️", "⛏️", "💎", "👍", "😂", "✨")
      prefs.edit().putStringSet(KEY_FAV_EMOJIS, initialDefaults).apply()
      return initialDefaults.toList()
    }
    return prefs.getStringSet(KEY_FAV_EMOJIS, emptySet())?.toList() ?: emptyList()
  }

  fun toggleFavoriteEmoji(context: Context, emoji: String): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = prefs.getStringSet(KEY_FAV_EMOJIS, emptySet())?.toMutableSet() ?: mutableSetOf()
    val isFav = if (current.contains(emoji)) {
      current.remove(emoji)
      false
    } else {
      current.add(emoji)
      true
    }
    prefs.edit().putStringSet(KEY_FAV_EMOJIS, current).apply()
    return isFav
  }

  fun isEmojiFavorite(context: Context, emoji: String): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = prefs.getStringSet(KEY_FAV_EMOJIS, emptySet()) ?: emptySet()
    return set.contains(emoji)
  }

  fun getFavoriteStickerIds(context: Context): Set<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getStringSet(KEY_FAV_STICKER_IDS, emptySet()) ?: emptySet()
  }

  fun toggleFavoriteSticker(context: Context, stickerId: String): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = prefs.getStringSet(KEY_FAV_STICKER_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
    val isFav = if (current.contains(stickerId)) {
      current.remove(stickerId)
      false
    } else {
      current.add(stickerId)
      true
    }
    prefs.edit().putStringSet(KEY_FAV_STICKER_IDS, current).apply()
    return isFav
  }

  fun isStickerFavorite(context: Context, stickerId: String): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val set = prefs.getStringSet(KEY_FAV_STICKER_IDS, emptySet()) ?: emptySet()
    return set.contains(stickerId)
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmojiPickerView(
  stickers: List<ServerSticker> = emptyList(),
  isAdmin: Boolean = false,
  isDarkMode: Boolean = false,
  onOpenCreateSticker: () -> Unit = {},
  onEmojiSelected: (String) -> Unit,
  onStickerSelected: (ServerSticker) -> Unit = {},
  onDeleteSticker: (String) -> Unit = {},
  onBackspace: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedCategory by remember { mutableIntStateOf(0) }
  var favSubTab by remember { mutableIntStateOf(0) } // 0 = Emojis, 1 = Stickers

  var favEmojis by remember { mutableStateOf(FavoriteEmojiStickerManager.getFavoriteEmojis(context)) }
  var favStickerIds by remember { mutableStateOf(FavoriteEmojiStickerManager.getFavoriteStickerIds(context)) }

  var emojiToManage by remember { mutableStateOf<String?>(null) }
  var stickerToManage by remember { mutableStateOf<ServerSticker?>(null) }

  // Theme-aware colors
  val panelBg = if (isDarkMode) Color(0xFF1E2024) else Color(0xFFF7F8FA)
  val headerBg = if (isDarkMode) Color(0xFF16181D) else Color.White
  val dividerColor = if (isDarkMode) Color(0xFF2C2F36) else WhatsAppDivider
  val textColor = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF111B21)
  val subTextColor = if (isDarkMode) Color(0xFFA0AAB5) else WhatsAppTextSecondary
  val selectedTabBg = if (isDarkMode) Color(0xFF00A884) else WhatsAppNavSelectedPill
  val selectedTabText = if (isDarkMode) Color.White else WhatsAppGreenDark
  val chipBg = if (isDarkMode) Color(0xFF282C34) else Color(0xFFE9EDEF)

  val imageLoader = remember {
    ImageLoader.Builder(context).components {
      if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
    }.build()
  }

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
    modifier = modifier.fillMaxWidth().height(280.dp),
    color = panelBg
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

      // Category Navigation Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(headerBg)
          .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Favourites Tab (★)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (selectedCategory == -1) selectedTabBg else Color.Transparent)
              .clickable { selectedCategory = -1 }
              .padding(horizontal = 7.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "★",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = if (selectedCategory == -1) selectedTabText else (if (isDarkMode) Color(0xFFFFB300) else Color(0xFFF59E0B))
            )
          }

          // Category icons
          categories.forEachIndexed { index, _ ->
            val icon = when (index) { 0 -> "🇲🇾"; 1 -> "😀"; 2 -> "🙏"; 3 -> "🥀"; else -> "⛏️" }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedCategory == index) selectedTabBg else Color.Transparent)
                .clickable { selectedCategory = index }
                .padding(horizontal = 7.dp, vertical = 4.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(text = icon, fontSize = 16.5.sp)
            }
          }

          // Server Stickers Tab
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (selectedCategory == 99) selectedTabBg else Color.Transparent)
              .clickable { selectedCategory = 99 }
              .padding(horizontal = 7.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "💟", fontSize = 16.5.sp)
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (selectedCategory == 99 && isAdmin) {
            IconButton(onClick = onOpenCreateSticker, modifier = Modifier.size(34.dp)) {
              Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Add Sticker",
                tint = if (isDarkMode) Color(0xFF00A884) else WhatsAppGreenDark,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          if (selectedCategory != 99) {
            IconButton(onClick = onBackspace, modifier = Modifier.size(34.dp)) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = subTextColor,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

      when (selectedCategory) {
        // FAVOURITES TAB
        -1 -> {
          val favoriteStickers = stickers.filter { favStickerIds.contains(it.id) }

          Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            // Sub-filter tabs for Favourites (Emojis vs Stickers)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (favSubTab == 0) selectedTabBg else chipBg,
                modifier = Modifier.clickable { favSubTab = 0 }
              ) {
                Text(
                  text = "Emojis (${favEmojis.size})",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (favSubTab == 0) selectedTabText else subTextColor,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
              }

              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (favSubTab == 1) selectedTabBg else chipBg,
                modifier = Modifier.clickable { favSubTab = 1 }
              ) {
                Text(
                  text = "Stickers (${favoriteStickers.size})",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (favSubTab == 1) selectedTabText else subTextColor,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
              }
            }

            if (favSubTab == 0) {
              if (favEmojis.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.StarOutline, contentDescription = null, tint = subTextColor, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("No Favourite Emojis", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                    Text("Long-press any emoji to add it to your favourites!", color = subTextColor, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                  }
                }
              } else {
                LazyVerticalGrid(
                  columns = GridCells.Fixed(7),
                  modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                  items(favEmojis) { emoji ->
                    Box(
                      modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                          onClick = { onEmojiSelected(emoji) },
                          onLongClick = { emojiToManage = emoji }
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(text = emoji, fontSize = 23.sp)
                    }
                  }
                }
              }
            } else {
              if (favoriteStickers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.StarOutline, contentDescription = null, tint = subTextColor, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("No Favourite Stickers", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                    Text("Long-press any sticker to add it to your favourites!", color = subTextColor, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                  }
                }
              } else {
                LazyVerticalGrid(
                  columns = GridCells.Fixed(4),
                  modifier = Modifier.fillMaxSize().padding(8.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  items(favoriteStickers, key = { it.id }) { sticker ->
                    val imageBytes = remember(sticker.imageData) {
                      try { Base64.decode(sticker.imageData.substringAfter("base64,"), Base64.NO_WRAP) } catch (_: Exception) { null }
                    }

                    if (imageBytes != null) {
                      Box(
                        modifier = Modifier
                          .size(76.dp)
                          .clip(RoundedCornerShape(8.dp))
                          .combinedClickable(
                            onClick = { onStickerSelected(sticker) },
                            onLongClick = { stickerToManage = sticker }
                          )
                          .padding(4.dp),
                        contentAlignment = Alignment.Center
                      ) {
                        AsyncImage(
                          model = imageBytes,
                          imageLoader = imageLoader,
                          contentDescription = sticker.name,
                          modifier = Modifier.size(68.dp),
                          contentScale = ContentScale.Fit
                        )
                        Icon(
                          imageVector = Icons.Default.Star,
                          contentDescription = null,
                          tint = Color(0xFFFFB300),
                          modifier = Modifier.size(13.dp).align(Alignment.TopEnd)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // SERVER STICKERS TAB
        99 -> {
          if (stickers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No Server Stickers Yet", fontWeight = FontWeight.Bold, color = subTextColor, fontSize = 14.sp)
                if (isAdmin) {
                  Text(
                    text = "Admins can tap + above to add stickers",
                    color = if (isDarkMode) Color(0xFF00A884) else WhatsAppGreenDark,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onOpenCreateSticker() }
                  )
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
              items(stickers, key = { it.id }) { sticker ->
                val isFav = favStickerIds.contains(sticker.id)
                val imageBytes = remember(sticker.imageData) {
                  try { Base64.decode(sticker.imageData.substringAfter("base64,"), Base64.NO_WRAP) } catch (_: Exception) { null }
                }

                if (imageBytes != null) {
                  Box(
                    modifier = Modifier
                      .size(76.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .combinedClickable(
                        onClick = { onStickerSelected(sticker) },
                        onLongClick = { stickerToManage = sticker }
                      )
                      .padding(4.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    AsyncImage(
                      model = imageBytes,
                      imageLoader = imageLoader,
                      contentDescription = sticker.name,
                      modifier = Modifier.size(68.dp),
                      contentScale = ContentScale.Fit
                    )
                    if (isFav) {
                      Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Starred",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(13.dp).align(Alignment.TopEnd)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // REGULAR EMOJI CATEGORIES
        else -> {
          val currentEmojis = categories[selectedCategory].second
          LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 6.dp, vertical = 4.dp)
          ) {
            items(currentEmojis) { emoji ->
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .combinedClickable(
                    onClick = { onEmojiSelected(emoji) },
                    onLongClick = { emojiToManage = emoji }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(text = emoji, fontSize = 23.sp)
              }
            }
          }
        }
      }
    }
  }

  // Emoji Management Dialog (Add/Remove from Favourites)
  emojiToManage?.let { emoji ->
    val isFav = FavoriteEmojiStickerManager.isEmojiFavorite(context, emoji)
    AlertDialog(
      onDismissRequest = { emojiToManage = null },
      containerColor = if (isDarkMode) Color(0xFF252830) else Color.White,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = emoji, fontSize = 28.sp)
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (isFav) "Favourite Emoji" else "Add to Favourites",
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 17.sp
          )
        }
      },
      text = {
        Text(
          text = if (isFav) "This emoji is currently in your favourites. Remove it?" else "Add $emoji to your favourites tab for one-tap access?",
          color = subTextColor,
          fontSize = 14.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            FavoriteEmojiStickerManager.toggleFavoriteEmoji(context, emoji)
            favEmojis = FavoriteEmojiStickerManager.getFavoriteEmojis(context)
            emojiToManage = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isFav) Color(0xFFEA0038) else (if (isDarkMode) Color(0xFF00A884) else WhatsAppGreenDark)
          )
        ) {
          Text(if (isFav) "Remove ★" else "Add ★", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { emojiToManage = null }) {
          Text("Cancel", color = subTextColor)
        }
      }
    )
  }

  // Sticker Management Dialog (Favourite / Unfavourite / Delete)
  stickerToManage?.let { sticker ->
    val isFav = FavoriteEmojiStickerManager.isStickerFavorite(context, sticker.id)
    AlertDialog(
      onDismissRequest = { stickerToManage = null },
      containerColor = if (isDarkMode) Color(0xFF252830) else Color.White,
      title = {
        Text(
          text = sticker.name.ifBlank { "Server Sticker" },
          fontWeight = FontWeight.Bold,
          color = textColor,
          fontSize = 17.sp
        )
      },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          val imageBytes = remember(sticker.imageData) {
            try { Base64.decode(sticker.imageData.substringAfter("base64,"), Base64.NO_WRAP) } catch (_: Exception) { null }
          }
          if (imageBytes != null) {
            AsyncImage(
              model = imageBytes,
              imageLoader = imageLoader,
              contentDescription = sticker.name,
              modifier = Modifier.size(72.dp),
              contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(10.dp))
          }
          Text(
            text = if (isFav) "Remove this sticker from your favourites?" else "Add this sticker to your favourites tab?",
            color = subTextColor,
            fontSize = 14.sp
          )
        }
      },
      confirmButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (isAdmin) {
            Button(
              onClick = {
                onDeleteSticker(sticker.id)
                stickerToManage = null
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA0038))
            ) {
              Text("Delete", color = Color.White)
            }
          }
          Button(
            onClick = {
              FavoriteEmojiStickerManager.toggleFavoriteSticker(context, sticker.id)
              favStickerIds = FavoriteEmojiStickerManager.getFavoriteStickerIds(context)
              stickerToManage = null
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isFav) Color(0xFFEA0038) else (if (isDarkMode) Color(0xFF00A884) else WhatsAppGreenDark)
            )
          ) {
            Text(if (isFav) "Remove ★" else "Favourite ★", color = Color.White)
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { stickerToManage = null }) {
          Text("Cancel", color = subTextColor)
        }
      }
    )
  }
}
