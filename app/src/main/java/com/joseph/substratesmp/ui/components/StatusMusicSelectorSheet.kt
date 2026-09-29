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

// Pre-seeded Michael Jackson tracks for "Suggested"
val MichaelJacksonTracks = listOf(
  MusicTrack("mj_billie_jean", "Billie Jean", "Michael Jackson", "4:54", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/37/d9/38/37d938b8-b131-e1f9-906f-77119f3900cb/24UMGIM10352.rgb.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/a4/bc/39/a4bc3922-3860-6c37-14fa-7bf66ecdc083/mzaf_10334887309995873977.plus.aac.p.m4a"),
  MusicTrack("mj_beat_it", "Beat It", "Michael Jackson", "4:18", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/21/df/b9/21dfb9be-9e73-b295-9988-cb58b760a95b/886445980062.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/9f/c6/33/9fc633c7-128a-7c90-0931-15cb38d672e6/mzaf_1409395277869680373.plus.aac.p.m4a"),
  MusicTrack("mj_thriller", "Thriller", "Michael Jackson", "5:57", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/e5/22/8e/e5228ee0-2e06-c875-01e4-fc1f5cb8a1b6/886443574164.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/05/29/ce/0529ce8c-2ad4-3676-e886-0428d08c5c0c/mzaf_17208493060647898863.plus.aac.p.m4a"),
  MusicTrack("mj_smooth_criminal", "Smooth Criminal", "Michael Jackson", "4:17", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/71/84/f9/7184f9ea-b80c-5126-7f41-0730d3674681/886443574171.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/1b/ad/83/1bad831d-b875-1e35-ce4e-12ce17b2b005/mzaf_11802931494954497645.plus.aac.p.m4a"),
  MusicTrack("mj_man_in_mirror", "Man in the Mirror", "Michael Jackson", "5:19", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/71/84/f9/7184f9ea-b80c-5126-7f41-0730d3674681/886443574171.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview112/v4/19/b6/2a/19b62a74-d4ba-7a26-34ca-879930f3fec1/mzaf_3197626943890833202.plus.aac.p.m4a")
)

// Pre-seeded C418 tracks for "Minecraft"
val C418MinecraftTracks = listOf(
  MusicTrack("c418_sweden", "Sweden (Minecraft)", "C418", "3:35", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/1b/ad/83/1bad831d-b875-1e35-ce4e-12ce17b2b005/mzaf_11802931494954497645.plus.aac.p.m4a"),
  MusicTrack("c418_subwoofer", "Subwoofer Lullaby", "C418", "3:28", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/ff/8d/62/ff8d6263-d305-6548-e8a3-2c1a1796c9ca/mzaf_6454790098418047913.plus.aac.p.m4a"),
  MusicTrack("c418_wet_hands", "Wet Hands", "C418", "1:30", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/a5/ea/bd/a5eabd84-c5b7-7eb5-17a4-bb0ee24430e7/mzaf_17208493060647898863.plus.aac.p.m4a"),
  MusicTrack("c418_mice_venus", "Mice on Venus", "C418", "4:41", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/a4/bc/39/a4bc3922-3860-6c37-14fa-7bf66ecdc083/mzaf_10334887309995873977.plus.aac.p.m4a"),
  MusicTrack("c418_minecraft", "Minecraft", "C418", "4:14", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/eb/aa/62/ebaa627f-9be1-f62f-124b-fb022c4f4544/859705663712_cover.jpg/200x200bb.jpg", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview122/v4/ee/12/bd/ee12bd32-353d-24bf-875f-2ffb6c2a4773/mzaf_15783350419339023610.plus.aac.p.m4a")
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
  var searchResults by remember { mutableStateOf(MichaelJacksonTracks) }

  var starredTracks by remember { mutableStateOf(StarredMusicManager.getStarredTracks(context)) }

  var playingTrackId by remember { mutableStateOf<String?>(null) }
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
  var trimmingTrack by remember { mutableStateOf<MusicTrack?>(null) }

  // Theme Colors
  val sheetBg = if (isDarkMode) Color(0xFF1E2024) else Color(0xFFF7F8FA)
  val searchBg = if (isDarkMode) Color(0xFF282C34) else Color.White
  val searchBorder = if (isDarkMode) Color(0xFF383E4B) else Color(0xFFE2E4E8)
  val textColor = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF111B21)
  val subTextColor = if (isDarkMode) Color(0xFFA0AAB5) else Color(0xFF707784)
  val dragHandleColor = if (isDarkMode) Color(0xFF3D4554) else Color(0xFFD1D5DB)
  val chipBg = if (isDarkMode) Color(0xFF282C34) else Color.White
  val chipBorder = if (isDarkMode) Color(0xFF383E4B) else Color(0xFFE2E4E8)
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

  LaunchedEffect(Unit) {
    fetchItunes("Michael Jackson")
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
                else if (selectedCategory == "Minecraft") searchResults = C418MinecraftTracks
                else fetchItunes("Michael Jackson")
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
            IconButton(
              onClick = {
                searchQuery = ""
                if (selectedCategory == "Minecraft") searchResults = C418MinecraftTracks
                else fetchItunes("Michael Jackson")
              },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Clear", tint = subTextColor, modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      val chips = listOf("Suggested", "Minecraft", "Genre", "Starred")
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(chips) { chip ->
          val isSelected = selectedCategory == chip
          val chipShape = RoundedCornerShape(20.dp)

          Surface(
            shape = chipShape,
            color = if (isSelected) (if (isDarkMode) Color(0xFF004D40) else Color(0xFFD8FDD2)) else chipBg,
            border = BorderStroke(1.dp, if (isSelected) Color(0xFF00A884) else chipBorder),
            onClick = {
              selectedCategory = chip
              searchQuery = ""
              if (chip == "Suggested") {
                searchResults = MichaelJacksonTracks
                fetchItunes("Michael Jackson")
              } else if (chip == "Minecraft") {
                searchResults = C418MinecraftTracks
                fetchItunes("C418")
              } else if (chip == "Genre") {
                fetchItunes("gaming electronic ambient")
              } else if (chip == "Starred") {
                starredTracks = StarredMusicManager.getStarredTracks(context)
                searchResults = starredTracks
              }
            },
            modifier = Modifier.clip(chipShape)
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
            val itemShape = RoundedCornerShape(12.dp)

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(itemShape)
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
                  onClick = { playPreview(track) },
                  modifier = Modifier.size(28.dp).clip(CircleShape)
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

              Surface(
                shape = CircleShape,
                color = if (isDarkMode) Color(0xFF282C34) else Color(0xFFE5E7EB),
                onClick = {
                  mediaPlayer?.stop()
                  trimmingTrack = track
                },
                modifier = Modifier.size(36.dp).clip(CircleShape)
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
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
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
            modifier = Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(12.dp))
          ) {
            Text("Confirm & Attach Clip", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}
