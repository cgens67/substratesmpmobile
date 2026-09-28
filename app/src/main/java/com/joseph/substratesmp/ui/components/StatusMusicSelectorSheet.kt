package com.joseph.substratesmp.ui.components

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class MusicTrack(
  val id: String,
  val title: String,
  val artist: String,
  val duration: String,
  val artworkUrl: String,
  val previewUrl: String
)

val DefaultSuggestedTracks = listOf(
  MusicTrack("1", "I Won't Cry", "Remi Wolf", "2:45", "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/37/d9/38/37d938b8-b131-e1f9-906f-77119f3900cb/24UMGIM10352.rgb.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/a4/bc/39/a4bc3922-3860-6c37-14fa-7bf66ecdc083/mzaf_10334887309995873977.plus.aac.p.m4a"),
  MusicTrack("2", "Little Sweet Treat", "Cute Kawaii Annie", "2:26", "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/4a/c3/84/4ac38421-4f1d-ff14-419a-9e19e075079a/artwork.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview122/v4/ee/12/bd/ee12bd32-353d-24bf-875f-2ffb6c2a4773/mzaf_15783350419339023610.plus.aac.p.m4a"),
  MusicTrack("3", "Funny", "Gold-Tiger", "1:25", "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/9c/61/42/9c614272-356a-113f-cb96-d8f99e46a96e/artwork.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/38/c4/e8/38c4e857-e6f9-0eb3-fa00-f6556e80b435/mzaf_16403063595992987342.plus.aac.p.m4a"),
  MusicTrack("4", "Sweden (Minecraft)", "C418", "3:35", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/1b/ad/83/1bad831d-b875-1e35-ce4e-12ce17b2b005/mzaf_11802931494954497645.plus.aac.p.m4a"),
  MusicTrack("5", "Subwoofer Lullaby", "C418", "3:28", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/ff/8d/62/ff8d6263-d305-6548-e8a3-2c1a1796c9ca/mzaf_6454790098418047913.plus.aac.p.m4a"),
  MusicTrack("6", "Risk It All", "NIFANA", "3:22", "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/91/36/45/9136456f-9818-f682-14eb-f0ee64c1cf7d/artwork.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/fa/10/7a/fa107afc-d9c9-272e-3367-27b9a544b6bc/mzaf_647717462744747738.plus.aac.p.m4a"),
  MusicTrack("7", "Golden Hour", "JVKE", "3:29", "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/5c/4b/f9/5c4bf925-bdf9-03a1-2fc5-5db4f3e0c0df/196925184203_Cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview112/v4/19/b6/2a/19b62a74-d4ba-7a26-34ca-879930f3fec1/mzaf_3197626943890833202.plus.aac.p.m4a")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusMusicSelectorSheet(
  onDismiss: () -> Unit,
  onSelectTrack: (MusicTrack) -> Unit
) {
  val scope = rememberCoroutineScope()
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Suggested") }
  var isSearching by remember { mutableStateOf(false) }
  var searchResults by remember { mutableStateOf(DefaultSuggestedTracks) }
  var starredTrackIds by remember { mutableStateOf(setOf("1", "4")) }

  var playingTrackId by remember { mutableStateOf<String?>(null) }
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

  DisposableEffect(Unit) {
    onDispose {
      mediaPlayer?.release()
      mediaPlayer = null
    }
  }

  fun playPreview(track: MusicTrack) {
    if (playingTrackId == track.id) {
      mediaPlayer?.stop()
      mediaPlayer?.release()
      mediaPlayer = null
      playingTrackId = null
      return
    }

    try {
      mediaPlayer?.stop()
      mediaPlayer?.release()
      mediaPlayer = MediaPlayer().apply {
        setDataSource(track.previewUrl)
        prepareAsync()
        setOnPreparedListener {
          it.start()
          playingTrackId = track.id
        }
        setOnCompletionListener {
          playingTrackId = null
        }
      }
    } catch (_: Exception) {
      playingTrackId = null
    }
  }

  fun searchItunes(query: String) {
    if (query.isBlank()) {
      searchResults = DefaultSuggestedTracks
      return
    }
    scope.launch {
      isSearching = true
      try {
        val client = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).build()
        val encoded = withContext(Dispatchers.IO) { URLEncoder.encode(query, "UTF-8") }
        val request = Request.Builder().url("https://itunes.apple.com/search?term=$encoded&entity=song&limit=25").build()
        val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
        val body = response.body?.string()
        if (body != null) {
          val json = JSONObject(body)
          val array = json.optJSONArray("results")
          val list = mutableListOf<MusicTrack>()
          if (array != null) {
            for (i in 0 until array.length()) {
              val item = array.getJSONObject(i)
              val preview = item.optString("previewUrl")
              if (preview.isNotBlank()) {
                val ms = item.optLong("trackTimeMillis", 180000L)
                val durFormatted = "%d:%02d".format((ms / 1000) / 60, (ms / 1000) % 60)
                list.add(
                  MusicTrack(
                    id = item.optLong("trackId", i.toLong()).toString(),
                    title = item.optString("trackName", "Track"),
                    artist = item.optString("artistName", "Artist"),
                    duration = durFormatted,
                    artworkUrl = item.optString("artworkUrl100"),
                    previewUrl = preview
                  )
                )
              }
            }
          }
          searchResults = list
        }
      } catch (_: Exception) {} finally {
        isSearching = false
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = Color(0xFF0F141A),
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(vertical = 10.dp)
          .size(width = 38.dp, height = 4.dp)
          .clip(CircleShape)
          .background(Color(0xFF384353))
      )
    },
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.90f)
        .padding(horizontal = 16.dp)
    ) {
      // Search Bar from screenshot
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1B232D),
        modifier = Modifier.fillMaxWidth().height(48.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          BasicTextField(
            value = searchQuery,
            onValueChange = {
              searchQuery = it
              searchItunes(it)
            },
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 15.sp),
            singleLine = true,
            decorationBox = { inner ->
              if (searchQuery.isEmpty()) Text("Search songs", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E9BAE), fontSize = 15.sp)
              inner()
            }
          )
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = ""; searchResults = DefaultSuggestedTracks }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF8E9BAE), modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Category filter chips
      val chips = listOf("Suggested", "Mood", "Genre", "Starred")
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(chips) { chip ->
          val isSelected = selectedCategory == chip
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) Color(0xFF004D40) else Color(0xFF1B232D),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00A884)) else null,
            modifier = Modifier.clickable {
              selectedCategory = chip
              if (chip == "Suggested") searchItunes("trending hits")
              else if (chip == "Mood") searchItunes("chill lofi minecraft")
              else if (chip == "Genre") searchItunes("ambient electronic")
              else if (chip == "Starred") searchResults = DefaultSuggestedTracks.filter { starredTrackIds.contains(it.id) }
            }
          ) {
            Text(
              text = chip,
              color = if (isSelected) Color(0xFF7BFF9F) else Color(0xFFA0ACC2),
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Hero Featured Track (like Remi Wolf card in screenshot)
      val heroTrack = searchResults.firstOrNull()
      if (heroTrack != null && searchQuery.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color(0xFF4A252A),
          modifier = Modifier.fillMaxWidth().height(82.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
              AsyncImage(
                model = heroTrack.artworkUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
              )
              Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp).clickable { playPreview(heroTrack) }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = if (playingTrackId == heroTrack.id) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(heroTrack.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
              Text(heroTrack.artist, color = Color.White.copy(alpha = 0.8f), fontSize = 12.5.sp, maxLines = 1)
            }

            IconButton(onClick = {
              starredTrackIds = if (starredTrackIds.contains(heroTrack.id)) starredTrackIds - heroTrack.id else starredTrackIds + heroTrack.id
            }) {
              Icon(
                imageVector = if (starredTrackIds.contains(heroTrack.id)) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (starredTrackIds.contains(heroTrack.id)) Color(0xFFFFB300) else Color.White.copy(alpha = 0.8f)
              )
            }

            Surface(
              shape = CircleShape,
              color = Color.White.copy(alpha = 0.2f),
              modifier = Modifier.size(36.dp).clickable {
                mediaPlayer?.stop()
                onSelectTrack(heroTrack)
                onDismiss()
              }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Select", tint = Color.White, modifier = Modifier.size(18.dp))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // Track List
      LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
        items(searchResults.drop(if (searchQuery.isEmpty()) 1 else 0), key = { it.id }) { track ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { playPreview(track) }
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
              AsyncImage(
                model = track.artworkUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
              )
              Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.size(26.dp).clickable { playPreview(track) }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = if (playingTrackId == track.id) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(track.title, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
              Text("${track.artist} • ${track.duration}", color = Color(0xFF8E9BAE), fontSize = 12.sp, maxLines = 1)
            }

            IconButton(onClick = {
              starredTrackIds = if (starredTrackIds.contains(track.id)) starredTrackIds - track.id else starredTrackIds + track.id
            }) {
              Icon(
                imageVector = if (starredTrackIds.contains(track.id)) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (starredTrackIds.contains(track.id)) Color(0xFFFFB300) else Color(0xFF8E9BAE)
              )
            }

            Surface(
              shape = CircleShape,
              color = Color(0xFF1B232D),
              modifier = Modifier.size(36.dp).clickable {
                mediaPlayer?.stop()
                onSelectTrack(track)
                onDismiss()
              }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Select", tint = Color.White, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}
