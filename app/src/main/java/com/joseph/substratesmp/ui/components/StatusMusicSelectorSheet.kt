package com.joseph.substratesmp.ui.components

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
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

object StarredMusicManager {
  private const val PREFS_NAME = "substrate_starred_music"
  private const val KEY_TRACKS = "starred_tracks_json"

  fun getStarredTracks(context: Context): List<MusicTrack> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val jsonStr = prefs.getString(KEY_TRACKS, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonStr)
      val list = mutableListOf<MusicTrack>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          MusicTrack(
            id = obj.getString("id"),
            title = obj.getString("title"),
            artist = obj.getString("artist"),
            duration = obj.getString("duration"),
            artworkUrl = obj.getString("artworkUrl"),
            previewUrl = obj.getString("previewUrl")
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun toggleStar(context: Context, track: MusicTrack): Boolean {
    val current = getStarredTracks(context).toMutableList()
    val exists = current.any { it.id == track.id }
    val isNowStarred = if (exists) {
      current.removeAll { it.id == track.id }
      false
    } else {
      current.add(0, track)
      true
    }
    val array = JSONArray()
    for (t in current) {
      val obj = JSONObject().apply {
        put("id", t.id)
        put("title", t.title)
        put("artist", t.artist)
        put("duration", t.duration)
        put("artworkUrl", t.artworkUrl)
        put("previewUrl", t.previewUrl)
      }
      array.put(obj)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_TRACKS, array.toString())
      .apply()
    return isNowStarred
  }

  fun isStarred(context: Context, trackId: String): Boolean {
    return getStarredTracks(context).any { it.id == trackId }
  }
}

val VerifiedRealTracks = listOf(
  MusicTrack("c418_sweden", "Sweden (Minecraft)", "C418", "3:35", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/1b/ad/83/1bad831d-b875-1e35-ce4e-12ce17b2b005/mzaf_11802931494954497645.plus.aac.p.m4a"),
  MusicTrack("c418_subwoofer", "Subwoofer Lullaby", "C418", "3:28", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/ff/8d/62/ff8d6263-d305-6548-e8a3-2c1a1796c9ca/mzaf_6454790098418047913.plus.aac.p.m4a"),
  MusicTrack("c418_wet_hands", "Wet Hands", "C418", "1:30", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/a5/ea/bd/a5eabd84-c5b7-7eb5-17a4-bb0ee24430e7/mzaf_17208493060647898863.plus.aac.p.m4a"),
  MusicTrack("jvke_golden_hour", "golden hour", "JVKE", "3:29", "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/5c/4b/f9/5c4bf925-bdf9-03a1-2fc5-5db4f3e0c0df/196925184203_Cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview112/v4/19/b6/2a/19b62a74-d4ba-7a26-34ca-879930f3fec1/mzaf_3197626943890833202.plus.aac.p.m4a"),
  MusicTrack("post_sunflower", "Sunflower", "Post Malone & Swae Lee", "2:38", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/36/83/82/368382ba-b3be-bfb6-ff95-5ad200d720b5/18UMGIM70072.rgb.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/80/cb/0e/80cb0ea0-31fe-9c02-7476-eb8d32d0cb53/mzaf_7867086812852269550.plus.aac.p.m4a")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusMusicSelectorSheet(
  isDarkMode: Boolean,
  onDismiss: () -> Unit,
  onSelectTrack: (track: MusicTrack, startOffsetMs: Int) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Suggested") }
  var isSearching by remember { mutableStateOf(false) }
  var searchResults by remember { mutableStateOf(VerifiedRealTracks) }

  var starredTracks by remember { mutableStateOf(StarredMusicManager.getStarredTracks(context)) }

  var playingTrackId by remember { mutableStateOf<String?>(null) }
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

  var trimmingTrack by remember { mutableStateOf<MusicTrack?>(null) }

  // Theme Colors
  val sheetBg = if (isDarkMode) Color(0xFF16181D) else Color(0xFFF7F8FA)
  val searchBg = if (isDarkMode) Color(0xFF232730) else Color.White
  val searchBorder = if (isDarkMode) Color(0xFF323845) else Color(0xFFE2E4E8)
  val textColor = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF111B21)
  val subTextColor = if (isDarkMode) Color(0xFFA0AAB5) else Color(0xFF707784)
  val dragHandleColor = if (isDarkMode) Color(0xFF3D4554) else Color(0xFFD1D5DB)
  val chipBg = if (isDarkMode) Color(0xFF232730) else Color.White
  val chipBorder = if (isDarkMode) Color(0xFF323845) else Color(0xFFE2E4E8)
  val blueAccent = Color(0xFF007AFF)

  DisposableEffect(Unit) {
    onDispose {
      mediaPlayer?.release()
      mediaPlayer = null
    }
  }

  fun playPreview(track: MusicTrack, startMs: Int = 0) {
    if (playingTrackId == track.id && startMs == 0) {
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
          if (startMs > 0) it.seekTo(startMs)
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

  fun fetchItunes(query: String) {
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
          if (list.isNotEmpty()) {
            searchResults = list
          }
        }
      } catch (_: Exception) {} finally {
        isSearching = false
      }
    }
  }

  // Load real trending songs on startup
  LaunchedEffect(Unit) {
    fetchItunes("top hits")
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = sheetBg,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(vertical = 10.dp)
          .size(width = 38.dp, height = 4.dp)
          .clip(CircleShape)
          .background(dragHandleColor)
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
      // Search Bar
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = searchBg,
        border = BorderStroke(1.dp, searchBorder),
        modifier = Modifier.fillMaxWidth().height(48.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Search, contentDescription = null, tint = subTextColor, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          BasicTextField(
            value = searchQuery,
            onValueChange = {
              searchQuery = it
              if (it.isBlank()) {
                if (selectedCategory == "Starred") searchResults = starredTracks
                else fetchItunes("top hits")
              } else {
                fetchItunes(it)
              }
            },
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontSize = 15.sp),
            singleLine = true,
            decorationBox = { inner ->
              if (searchQuery.isEmpty()) Text("Search songs or artists", style = MaterialTheme.typography.bodyMedium, color = subTextColor, fontSize = 15.sp)
              inner()
            }
          )
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = ""; fetchItunes("top hits") }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Clear", tint = subTextColor, modifier = Modifier.size(16.dp))
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
            color = if (isSelected) (if (isDarkMode) Color(0xFF004D40) else Color(0xFFD8FDD2)) else chipBg,
            border = BorderStroke(1.dp, if (isSelected) Color(0xFF00A884) else chipBorder),
            modifier = Modifier.clickable {
              selectedCategory = chip
              searchQuery = ""
              if (chip == "Suggested") fetchItunes("popular songs")
              else if (chip == "Mood") fetchItunes("chill lofi minecraft")
              else if (chip == "Genre") fetchItunes("gaming electronic ambient")
              else if (chip == "Starred") {
                starredTracks = StarredMusicManager.getStarredTracks(context)
                searchResults = starredTracks
              }
            }
          ) {
            Text(
              text = if (chip == "Starred") "★ Starred (${starredTracks.size})" else chip,
              color = if (isSelected) (if (isDarkMode) Color(0xFF7BFF9F) else Color(0xFF008069)) else subTextColor,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Track List
      if (searchResults.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxWidth().weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.MusicOff, contentDescription = null, tint = subTextColor, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (selectedCategory == "Starred") "No starred songs yet" else "No songs found",
              fontWeight = FontWeight.Bold,
              color = textColor,
              fontSize = 16.sp
            )
            Text(
              text = if (selectedCategory == "Starred") "Tap the star icon on any song to save it here!" else "Try searching for a different track title.",
              color = subTextColor,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      } else {
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
          items(searchResults, key = { it.id }) { track ->
            val isStarred = starredTracks.any { it.id == track.id }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { playPreview(track) }
                .padding(vertical = 8.dp, horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                AsyncImage(
                  model = track.artworkUrl,
                  contentDescription = null,
                  modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                  contentScale = ContentScale.Crop
                )
                Surface(
                  shape = CircleShape,
                  color = Color.Black.copy(alpha = 0.55f),
                  modifier = Modifier.size(28.dp).clickable { playPreview(track) }
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
                Text(
                  text = track.title,
                  fontWeight = FontWeight.SemiBold,
                  color = textColor,
                  fontSize = 14.5.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${track.artist} • ${track.duration}",
                  color = subTextColor,
                  fontSize = 12.sp,
                  maxLines = 1
                )
              }

              // Star Icon (Persistently saved)
              IconButton(onClick = {
                StarredMusicManager.toggleStar(context, track)
                starredTracks = StarredMusicManager.getStarredTracks(context)
                if (selectedCategory == "Starred") searchResults = starredTracks
              }) {
                Icon(
                  imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = null,
                  tint = if (isStarred) Color(0xFFFFB300) else subTextColor
                )
              }

              // Arrow Button opens section selector
              Surface(
                shape = CircleShape,
                color = if (isDarkMode) Color(0xFF282C34) else Color(0xFFE5E7EB),
                modifier = Modifier.size(36.dp).clickable {
                  mediaPlayer?.stop()
                  trimmingTrack = track
                }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Select Section",
                    tint = blueAccent,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Section Selector Dialog (Choose starting second of 30s preview)
  trimmingTrack?.let { track ->
    var startOffsetSec by remember { mutableFloatStateOf(0f) }
    var isTestingSection by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = {
      mediaPlayer?.stop()
      trimmingTrack = null
    }) {
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (isDarkMode) Color(0xFF1E2024) else Color.White,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Select Song Section",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = textColor
          )
          Text(
            text = "Drag the slider to choose where the 30s clip begins:",
            fontSize = 13.sp,
            color = subTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
              model = track.artworkUrl,
              contentDescription = null,
              modifier = Modifier.size(46.dp).clip(RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(track.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor, maxLines = 1)
              Text(track.artist, color = subTextColor, fontSize = 12.sp, maxLines = 1)
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = "Starts at: ${startOffsetSec.toInt()}s",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = blueAccent
          )

          Slider(
            value = startOffsetSec,
            onValueChange = {
              startOffsetSec = it
              if (isTestingSection) {
                playPreview(track, (it * 1000).toInt())
              }
            },
            valueRange = 0f..20f,
            steps = 19,
            colors = SliderDefaults.colors(thumbColor = blueAccent, activeTrackColor = blueAccent)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("0s", fontSize = 11.5.sp, color = subTextColor)
            Text("10s", fontSize = 11.5.sp, color = subTextColor)
            Text("20s", fontSize = 11.5.sp, color = subTextColor)
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Test section audio preview
          OutlinedButton(
            onClick = {
              isTestingSection = !isTestingSection
              if (isTestingSection) {
                playPreview(track, (startOffsetSec * 1000).toInt())
              } else {
                mediaPlayer?.stop()
              }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = if (isTestingSection) Icons.Default.Stop else Icons.Default.PlayArrow,
              contentDescription = null,
              tint = blueAccent,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isTestingSection) "Stop Preview" else "Listen from ${startOffsetSec.toInt()}s", color = blueAccent)
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              mediaPlayer?.stop()
              onSelectTrack(track, (startOffsetSec * 1000).toInt())
              trimmingTrack = null
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
          ) {
            Text("Confirm & Attach Clip", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}
