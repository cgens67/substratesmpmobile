@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.joseph.substratesmp.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.joseph.substratesmp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@Composable
private fun stringResourceSafe(resName: String, fallback: String, vararg formatArgs: Any): String {
    val context = LocalContext.current
    val resId = remember(resName) { context.resources.getIdentifier(resName, "string", context.packageName) }
    return if (resId != 0) {
        if (formatArgs.isNotEmpty()) stringResource(resId, *formatArgs) else stringResource(resId)
    } else {
        if (formatArgs.isNotEmpty()) fallback.format(*formatArgs) else fallback
    }
}

private fun getStringSafe(context: Context, resName: String, fallback: String, vararg formatArgs: Any): String {
    val resId = context.resources.getIdentifier(resName, "string", context.packageName)
    return if (resId != 0) {
        if (formatArgs.isNotEmpty()) context.getString(resId, *formatArgs) else context.getString(resId)
    } else {
        if (formatArgs.isNotEmpty()) fallback.format(*formatArgs) else fallback
    }
}

@Composable
private fun rememberIconPainterSafe(resName: String, fallbackIcon: ImageVector): Painter {
    val context = LocalContext.current
    val resId = remember(resName) { context.resources.getIdentifier(resName, "drawable", context.packageName) }
    return if (resId != 0) {
        painterResource(id = resId)
    } else {
        rememberVectorPainter(fallbackIcon)
    }
}

data class ChangelogSection(val title: String, val items: List<String>)

data class ReleaseMetadata(
    val tagName: String,
    val name: String,
    val date: String,
    val changelogUrl: String?,
    val isPrerelease: Boolean,
    val rawDate: String,
    val body: String
)

data class CachedChangelogData(
    val sections: List<ChangelogSection>,
    val image: String?,
    val description: String?,
    val warning: String?
)

fun getTimeAgo(context: Context, dateString: String?): String {
    if (dateString.isNullOrBlank()) return getStringSafe(context, "unknown", "Unknown")
    return try {
        val zdt = ZonedDateTime.parse(dateString)
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val duration = Duration.between(zdt, now)
        val seconds = duration.seconds
        when {
            seconds < 0 -> getStringSafe(context, "just_now", "Just now")
            seconds < 60 -> getStringSafe(context, "seconds_ago", "%d seconds ago", seconds)
            seconds < 120 -> getStringSafe(context, "one_minute_ago", "1 minute ago")
            seconds < 3600 -> getStringSafe(context, "minutes_ago", "%d minutes ago", seconds / 60)
            seconds < 7200 -> getStringSafe(context, "one_hour_ago", "1 hour ago")
            seconds < 86400 -> getStringSafe(context, "hours_ago", "%d hours ago", seconds / 3600)
            seconds < 172800 -> getStringSafe(context, "one_day_ago", "1 day ago")
            seconds < 2592000 -> getStringSafe(context, "days_ago", "%d days ago", seconds / 86400)
            seconds < 5184000 -> getStringSafe(context, "one_month_ago", "1 month ago")
            seconds < 31536000 -> getStringSafe(context, "months_ago", "%d months ago", seconds / 2592000)
            seconds < 63072000 -> getStringSafe(context, "one_year_ago", "1 year ago")
            else -> getStringSafe(context, "years_ago", "%d years ago", seconds / 31536000)
        }
    } catch (_: Exception) {
        dateString ?: ""
    }
}

fun getBetaTarget(tagName: String): String? {
    val base = tagName.removePrefix("v")
    if (base.contains("-")) {
        return base.substringBefore("-")
    }
    return null
}

fun normalizeVersionString(version: String): String {
    return version.trim()
        .lowercase()
        .replace(" ", "")
        .removePrefix("v.")
        .removePrefix("v")
        .replace("..", ".")
}

fun matchesVersion(tagName: String, query: String): Boolean {
    val cleanQuery = query.trim().lowercase().replace(" ", "")
    if (cleanQuery.isBlank()) return false
    if (cleanQuery == "v" || cleanQuery == "v.") return false

    if (tagName.lowercase().replace(" ", "").contains(cleanQuery)) return true
    val normTag = normalizeVersionString(tagName)
    val normQuery = normalizeVersionString(cleanQuery)
    return normQuery.isNotEmpty() && normTag.contains(normQuery)
}

@Composable
fun EmptySearchResultsView(
    modifier: Modifier = Modifier,
    title: String = "No results found",
    subtitle: String = "Try another search term",
    isDarkMode: Boolean = false
) {
    val textColor = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF111B21)
    val subTextColor = if (isDarkMode) Color(0xFFA0A0A5) else Color(0xFF667781)

    var visible by remember(title, subtitle) { mutableStateOf(false) }
    LaunchedEffect(title, subtitle) {
        visible = true
    }

    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "emptyIconScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "emptyContentAlpha"
    )

    val contentOffsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 16.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "emptyContentOffsetY"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = contentAlpha
                translationY = contentOffsetY.toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(iconScale)
                .background(
                    color = if (isDarkMode) Color(0xFF383838) else Color(0xFFE9EDEF),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = rememberIconPainterSafe("search", Icons.Default.Search),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = subTextColor
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = subTextColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChangelogScreen(
    isDarkMode: Boolean = false,
    onDismiss: () -> Unit = {},
    versionTag: String = BuildConfig.VERSION_NAME
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val bgColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFF4F5F8)
    val surfaceColor = if (isDarkMode) Color(0xFF303030) else Color.White
    val textColor = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF111B21)
    val subTextColor = if (isDarkMode) Color(0xFFA0A0A5) else Color(0xFF667781)
    val accentGreen = if (isDarkMode) Color(0xFF00A884) else Color(0xFF008069)
    val dividerColor = if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFE2E4E8)

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = bgColor,
        topBar = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                            fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                },
                label = "changelogTopBar"
            ) { searching ->
                if (searching) {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = { isSearchActive = false },
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = { Text(text = "Search releases or changes...", color = subTextColor) },
                                leadingIcon = {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            isSearchActive = false
                                        }
                                    ) {
                                        Icon(
                                            painter = rememberIconPainterSafe("arrow_back", Icons.AutoMirrored.Filled.ArrowBack),
                                            contentDescription = "Back",
                                            tint = textColor
                                        )
                                    }
                                },
                                trailingIcon = if (searchQuery.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                painter = rememberIconPainterSafe("close", Icons.Default.Close),
                                                contentDescription = "Clear",
                                                tint = subTextColor
                                            )
                                        }
                                    }
                                } else null
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        colors = SearchBarDefaults.colors(
                            containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {}
                } else {
                    LargeTopAppBar(
                        title = {
                            Text(
                                text = "Changelog",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    painter = rememberIconPainterSafe("arrow_back", Icons.AutoMirrored.Filled.ArrowBack),
                                    contentDescription = "Back",
                                    tint = textColor
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    painter = rememberIconPainterSafe("search", Icons.Default.Search),
                                    contentDescription = "Search",
                                    tint = textColor
                                )
                            }
                            IconButton(onClick = { refreshTrigger++ }) {
                                Icon(
                                    painter = rememberIconPainterSafe("sync", Icons.Default.Sync),
                                    contentDescription = "Refresh",
                                    tint = textColor
                                )
                            }
                        },
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = bgColor,
                            scrolledContainerColor = surfaceColor,
                        ),
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Only 2 tabs: Releases and Beta Releases (Commits tab is completely hidden)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = bgColor,
                contentColor = accentGreen,
                divider = { HorizontalDivider(color = dividerColor, thickness = 0.5.dp) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Releases",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) accentGreen else subTextColor
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Beta Releases",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) accentGreen else subTextColor
                        )
                    }
                )
            }

            ReleasesContent(
                versionTag = versionTag,
                refreshTrigger = refreshTrigger,
                isBetaTab = selectedTab == 1,
                searchQuery = searchQuery,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
fun ReleasesContent(
    versionTag: String,
    refreshTrigger: Int,
    isBetaTab: Boolean,
    searchQuery: String = "",
    isDarkMode: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentVersionTag by remember { mutableStateOf("") }
    var availableReleases by remember { mutableStateOf<List<ReleaseMetadata>>(emptyList()) }
    var isFetchingOldReleases by remember { mutableStateOf(true) }
    var releasesFetchError by remember { mutableStateOf<String?>(null) }

    val allChangelogsData = remember { mutableStateMapOf<String, CachedChangelogData>() }
    val changelogFetchErrors = remember { mutableStateMapOf<String, String>() }
    val changelogLoadingStates = remember { mutableStateMapOf<String, Boolean>() }

    val currentIsLoading = isFetchingOldReleases || (changelogLoadingStates[currentVersionTag] == true)

    // Neutral dark mode styling (no glowing neon)
    val cardBg = if (isDarkMode) Color(0xFF303030) else Color.White
    val cardBorder = if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFE2E4E8)
    val textColor = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF111B21)
    val subTextColor = if (isDarkMode) Color(0xFFA0A0A5) else Color(0xFF667781)
    val accentGreen = if (isDarkMode) Color(0xFF00A884) else Color(0xFF008069)
    val linkColor = if (isDarkMode) Color(0xFF53BDEB) else Color(0xFF0288D1)
    val highlightColor = if (isDarkMode) Color(0xFF005C4B).copy(alpha = 0.45f) else Color(0xFFD8FDD2)
    val highlightTextColor = if (isDarkMode) Color(0xFFD8FDD2) else Color(0xFF005C4B)

    val httpClient = remember(context) {
        val cacheSize = 10L * 1024 * 1024
        val cache = Cache(File(context.cacheDir, "github_api_cache"), cacheSize)
        OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    fun ensureChangelogFetched(tag: String, release: ReleaseMetadata?, bypassCache: Boolean = false) {
        if (tag.isBlank()) return
        if (changelogLoadingStates[tag] == true) return
        if (!bypassCache && allChangelogsData.containsKey(tag)) return

        changelogLoadingStates[tag] = true
        changelogFetchErrors.remove(tag)

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val cachedData = if (!bypassCache) loadChangelogFromCache(context, tag) else null
                if (cachedData != null) {
                    withContext(Dispatchers.Main) {
                        allChangelogsData[tag] = cachedData
                        changelogLoadingStates[tag] = false
                    }
                } else {
                    val urlToFetch = release?.changelogUrl
                        ?: "https://github.com/cgens67/substratesmpmobile/releases/download/$tag/changelog.json"

                    val request = Request.Builder()
                        .url(urlToFetch)
                        .header("User-Agent", "Mozilla/5.0")
                        .build()

                    val response = httpClient.newCall(request).execute()

                    if (response.isSuccessful) {
                        val changelogJson = response.body?.string() ?: "{}"
                        try {
                            val changelogData = JSONObject(changelogJson)
                            val desc = changelogData.optString("description", "").takeIf { it.isNotBlank() }
                            val imageUrl = changelogData.optString("image", "").takeIf { it.isNotBlank() }
                            val warning = changelogData.optString("warning", "").takeIf { it.isNotBlank() }
                            val changelogArray = changelogData.optJSONArray("changelog")

                            val sections = mutableListOf<ChangelogSection>()
                            if (changelogArray != null) {
                                for (i in 0 until changelogArray.length()) {
                                    val sectionObj = changelogArray.optJSONObject(i)
                                    if (sectionObj != null) {
                                        val title = sectionObj.optString("title", "")
                                        val itemsArray = sectionObj.optJSONArray("items")
                                        val items = mutableListOf<String>()
                                        if (itemsArray != null) {
                                            for (j in 0 until itemsArray.length()) {
                                                items.add(itemsArray.getString(j))
                                            }
                                        }
                                        if (title.isNotBlank() || items.isNotEmpty()) {
                                            sections.add(ChangelogSection(title, items))
                                        }
                                    } else {
                                        val item = changelogArray.optString(i, "")
                                        if (item.isNotBlank()) {
                                            if (sections.isEmpty() || sections[0].title.isNotBlank()) {
                                                sections.add(0, ChangelogSection("", mutableListOf()))
                                            }
                                            (sections[0].items as MutableList<String>).add(item)
                                        }
                                    }
                                }
                            }

                            saveChangelogToCache(context, tag, sections, imageUrl, desc, warning)
                            val parsed = CachedChangelogData(sections, imageUrl, desc, warning)
                            withContext(Dispatchers.Main) {
                                allChangelogsData[tag] = parsed
                                changelogLoadingStates[tag] = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                changelogFetchErrors[tag] = "JSON Parse Error: ${e.message}"
                                changelogLoadingStates[tag] = false
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            changelogFetchErrors[tag] = if (response.code == 403) "GitHub API rate limit exceeded" else "HTTP ${response.code}: ${response.message}"
                            changelogLoadingStates[tag] = false
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    changelogFetchErrors[tag] = "Network Error: ${e.message}"
                    changelogLoadingStates[tag] = false
                }
            }
        }
    }

    fun fetchOldReleases(bypassCache: Boolean = false) {
        isFetchingOldReleases = true
        releasesFetchError = null
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val cachedJson = if (!bypassCache) loadReleasesFromCache(context) else null
                val json = if (cachedJson != null) {
                    cachedJson
                } else {
                    val request = Request.Builder()
                        .url("https://api.github.com/repos/cgens67/substratesmpmobile/releases?per_page=50")
                        .header("User-Agent", "Mozilla/5.0")
                        .header("Accept", "application/vnd.github.v3+json")
                        .build()

                    val response = httpClient.newCall(request).execute()

                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: "[]"
                        saveReleasesToCache(context, bodyString)
                        bodyString
                    } else {
                        withContext(Dispatchers.Main) {
                            isFetchingOldReleases = false
                            releasesFetchError = if (response.code == 403) "GitHub API rate limit exceeded" else "HTTP ${response.code}: ${response.message}"
                        }
                        return@launch
                    }
                }

                val array = JSONArray(json)
                val list = mutableListOf<ReleaseMetadata>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val tagName = obj.getString("tag_name")
                    val name = obj.optString("name", tagName)
                    val publishedAt = obj.getString("published_at")
                    val isPrerelease = obj.getBoolean("prerelease")
                    val bodyText = obj.optString("body", "")
                    val formattedDate = getTimeAgo(context, publishedAt)

                    val assets = obj.getJSONArray("assets")
                    var changelogUrl: String? = null
                    for (j in 0 until assets.length()) {
                        val asset = assets.getJSONObject(j)
                        if (asset.getString("name").equals("changelog.json", ignoreCase = true)) {
                            changelogUrl = asset.getString("browser_download_url")
                            break
                        }
                    }

                    list.add(ReleaseMetadata(tagName, name, formattedDate, changelogUrl, isPrerelease, publishedAt, bodyText))
                }
                withContext(Dispatchers.Main) {
                    availableReleases = list
                    isFetchingOldReleases = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isFetchingOldReleases = false
                    releasesFetchError = "Network Error: ${e.message}"
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchOldReleases(false)
    }

    LaunchedEffect(availableReleases) {
        availableReleases.forEach { release ->
            ensureChangelogFetched(release.tagName, release, false)
        }
    }

    val filteredReleases = availableReleases.filter { it.isPrerelease == isBetaTab }
    val allChangelogsValues = allChangelogsData.toMap()

    val searchFilteredReleases = remember(filteredReleases, searchQuery, allChangelogsValues) {
        if (searchQuery.isBlank()) {
            filteredReleases
        } else {
            filteredReleases.filter { release ->
                val changelogData = allChangelogsData[release.tagName]
                val changelogText = buildString {
                    changelogData?.sections?.forEach { section ->
                        append(section.title).append(" ")
                        section.items.forEach { append(it).append(" ") }
                    }
                    append(changelogData?.description.orEmpty())
                }

                matchesVersion(release.tagName, searchQuery) ||
                release.name.contains(searchQuery, ignoreCase = true) ||
                changelogText.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(isBetaTab, availableReleases) {
        if (filteredReleases.isNotEmpty()) {
            if (currentVersionTag.isBlank() || filteredReleases.none { it.tagName == currentVersionTag }) {
                val appVerMatchesTab = (versionTag.contains("-") == isBetaTab)
                currentVersionTag = if (appVerMatchesTab && filteredReleases.any { it.tagName == versionTag }) {
                    versionTag
                } else {
                    filteredReleases.first().tagName
                }
            }
        }
    }

    LaunchedEffect(searchFilteredReleases) {
        if (searchFilteredReleases.isNotEmpty() && searchFilteredReleases.none { it.tagName == currentVersionTag }) {
            currentVersionTag = searchFilteredReleases.first().tagName
        }
    }

    LaunchedEffect(currentVersionTag, refreshTrigger) {
        if (currentVersionTag.isNotBlank()) {
            val release = availableReleases.find { it.tagName == currentVersionTag }
            if (refreshTrigger > 0) {
                cleanupOldChangelogCache(context, currentVersionTag)
                ensureChangelogFetched(currentVersionTag, release, bypassCache = true)
            } else {
                ensureChangelogFetched(currentVersionTag, release, bypassCache = false)
            }
        }
    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            fetchOldReleases(bypassCache = true)
        }
    }

    val currentChangelog = allChangelogsData[currentVersionTag]
    val currentHasError = releasesFetchError != null || changelogFetchErrors[currentVersionTag] != null
    val currentDetailedError = releasesFetchError ?: changelogFetchErrors[currentVersionTag]

    val changelogSections = currentChangelog?.sections ?: emptyList()
    val updateImage = currentChangelog?.image
    val updateDescription = currentChangelog?.description
    val updateWarning = currentChangelog?.warning

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            if (currentIsLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = accentGreen,
                    trackColor = accentGreen.copy(alpha = 0.2f)
                )
            }

            if (searchFilteredReleases.isNotEmpty()) {
                // Version pills selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        searchFilteredReleases.forEach { release ->
                            val isSelected = currentVersionTag == release.tagName
                            val pillShape = RoundedCornerShape(16.dp)

                            Surface(
                                shape = pillShape,
                                color = if (isSelected) accentGreen else (if (isDarkMode) Color(0xFF383838) else Color(0xFFE9EDEF)),
                                border = if (isSelected) null else BorderStroke(1.dp, cardBorder),
                                onClick = {
                                    if (currentVersionTag != release.tagName) {
                                        currentVersionTag = release.tagName
                                    }
                                },
                                modifier = Modifier.clip(pillShape)
                            ) {
                                Text(
                                    text = release.tagName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else subTextColor,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    if (currentIsLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp,
                            color = accentGreen
                        )
                    }
                }

                val currentRelease = searchFilteredReleases.find { it.tagName == currentVersionTag }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentVersionTag,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        if (currentRelease != null) {
                            Text(
                                text = getTimeAgo(context, currentRelease.rawDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = subTextColor
                            )
                            if (currentRelease.isPrerelease) {
                                val target = getBetaTarget(currentRelease.tagName)
                                if (target != null) {
                                    Text(
                                        text = "Pre-release for $target",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accentGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (!isFetchingOldReleases && !currentIsLoading && !currentHasError) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptySearchResultsView(isDarkMode = isDarkMode)
                }
            }

            if (currentHasError && !currentIsLoading) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 340.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.Error, null, tint = Color(0xFFEA0038), modifier = Modifier.size(44.dp))
                        Spacer(Modifier.height(14.dp))
                        Text("Error loading changelog", color = textColor, fontWeight = FontWeight.Bold)

                        currentDetailedError?.let { detail ->
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = if (isDarkMode) Color(0xFF3D2626) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = detail,
                                    color = if (isDarkMode) Color(0xFFFFB4AB) else Color(0xFFC62828),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                fetchOldReleases(bypassCache = true)
                                if (currentVersionTag.isNotBlank()) {
                                    val rel = availableReleases.find { it.tagName == currentVersionTag }
                                    ensureChangelogFetched(currentVersionTag, rel, bypassCache = true)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentGreen)
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            } else if (searchFilteredReleases.isNotEmpty() && currentChangelog != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    updateImage?.let { imageUrl ->
                        Spacer(modifier = Modifier.height(8.dp))
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = cardBg),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.FillWidth
                            )
                            updateDescription?.let { desc ->
                                val annotatedDesc = buildAnnotatedString {
                                    append(desc)
                                    if (searchQuery.isNotBlank()) {
                                        val matches = Regex(Regex.escape(searchQuery), RegexOption.IGNORE_CASE).findAll(desc)
                                        matches.forEach { match ->
                                            addStyle(SpanStyle(background = highlightColor, color = highlightTextColor), match.range.first, match.range.last + 1)
                                        }
                                    }
                                }
                                Text(
                                    text = annotatedDesc,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } ?: updateDescription?.let { desc ->
                        Spacer(Modifier.height(8.dp))
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = cardBg),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val annotatedDesc = buildAnnotatedString {
                                append(desc)
                                if (searchQuery.isNotBlank()) {
                                    val matches = Regex(Regex.escape(searchQuery), RegexOption.IGNORE_CASE).findAll(desc)
                                    matches.forEach { match ->
                                        addStyle(SpanStyle(background = highlightColor, color = highlightTextColor), match.range.first, match.range.last + 1)
                                    }
                                }
                            }
                            Text(
                                text = annotatedDesc,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    if (changelogSections.isNotEmpty()) {
                        changelogSections.forEach { section ->
                            Spacer(Modifier.height(14.dp))
                            ElevatedCard(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = cardBg),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                                border = BorderStroke(1.dp, cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    if (section.title.isNotBlank()) {
                                        val annotatedTitle = buildAnnotatedString {
                                            append(section.title)
                                            if (searchQuery.isNotBlank()) {
                                                val matches = Regex(Regex.escape(searchQuery), RegexOption.IGNORE_CASE).findAll(section.title)
                                                matches.forEach { match ->
                                                    addStyle(SpanStyle(background = highlightColor, color = highlightTextColor), match.range.first, match.range.last + 1)
                                                }
                                            }
                                        }
                                        Text(
                                            text = annotatedTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Spacer(Modifier.height(10.dp))
                                    }

                                    section.items.forEach { item ->
                                        val urls = item.extractUrls()
                                        val itemTrimmed = item.trim()
                                        val annotatedText = buildAnnotatedString {
                                            append(itemTrimmed)
                                            urls.forEach { (range, url) ->
                                                addStringAnnotation("URL", url, range.first, range.last + 1)
                                                addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), range.first, range.last + 1)
                                            }
                                            if (searchQuery.isNotBlank()) {
                                                val matches = Regex(Regex.escape(searchQuery), RegexOption.IGNORE_CASE).findAll(itemTrimmed)
                                                matches.forEach { match ->
                                                    addStyle(SpanStyle(background = highlightColor, color = highlightTextColor), match.range.first, match.range.last + 1)
                                                }
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.padding(vertical = 5.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 8.dp)
                                                    .size(6.dp)
                                                    .background(accentGreen, CircleShape)
                                            )
                                            ClickableText(
                                                text = annotatedText,
                                                onClick = { offset ->
                                                    annotatedText.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
                                                        ContextCompat.startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(it.item)), null)
                                                    }
                                                },
                                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor, lineHeight = 20.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    updateWarning?.let { warning ->
                        Spacer(Modifier.height(20.dp))
                        Surface(
                            color = if (isDarkMode) Color(0xFF3D2626) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF5E3535) else Color(0xFFFFCDD2))
                        ) {
                            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = Color(0xFFEA0038), modifier = Modifier.size(20.dp))
                                val annotatedWarning = buildAnnotatedString {
                                    append(warning)
                                    if (searchQuery.isNotBlank()) {
                                        val matches = Regex(Regex.escape(searchQuery), RegexOption.IGNORE_CASE).findAll(warning)
                                        matches.forEach { match ->
                                            addStyle(SpanStyle(background = highlightColor, color = highlightTextColor), match.range.first, match.range.last + 1)
                                        }
                                    }
                                }
                                Text(annotatedWarning, style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) Color(0xFFFFB4AB) else Color(0xFFC62828))
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

private fun saveReleasesToCache(context: Context, json: String) {
    try {
        context.openFileOutput("releases_cache.json", Context.MODE_PRIVATE).use { it.write(json.toByteArray()) }
        context.getSharedPreferences("changelog_prefs", Context.MODE_PRIVATE).edit().putLong("releases_cache_time", System.currentTimeMillis()).apply()
    } catch (e: Exception) { Log.e("ChangelogCache", "Error saving releases cache", e) }
}

private fun loadReleasesFromCache(context: Context): String? {
    val prefs = context.getSharedPreferences("changelog_prefs", Context.MODE_PRIVATE)
    val time = prefs.getLong("releases_cache_time", 0)
    if (System.currentTimeMillis() - time > 3600_000) return null
    return try {
        val file = File(context.filesDir, "releases_cache.json")
        if (!file.exists()) return null
        context.openFileInput("releases_cache.json").use { it.bufferedReader().readText() }
    } catch (_: Exception) { null }
}

private fun cleanupOldChangelogCache(context: Context, currentVersionTag: String) {
    try {
        context.filesDir.listFiles { file -> file.name.startsWith("changelog_cache_") && file.name.endsWith(".json") }?.forEach { file ->
            if (file.name != "changelog_cache_$currentVersionTag.json") file.delete()
        }
    } catch (e: Exception) { Log.e("ChangelogCache", "Error cleaning up cache", e) }
}

private fun saveChangelogToCache(context: Context, versionTag: String, sections: List<ChangelogSection>, image: String?, description: String?, warning: String?) {
    try {
        val cacheData = JSONObject().apply {
            val sectionsArray = JSONArray()
            sections.forEach { section ->
                val sectionObj = JSONObject().apply {
                    put("title", section.title)
                    val itemsArray = JSONArray()
                    section.items.forEach { itemsArray.put(it) }
                    put("items", itemsArray)
                }
                sectionsArray.put(sectionObj)
            }
            put("sections", sectionsArray)
            put("image", image ?: "")
            put("description", description ?: "")
            put("warning", warning ?: "")
        }
        context.openFileOutput("changelog_cache_$versionTag.json", Context.MODE_PRIVATE).use { it.write(cacheData.toString().toByteArray()) }
    } catch (e: Exception) { Log.e("ChangelogCache", "Error saving cache", e) }
}

private fun loadChangelogFromCache(context: Context, versionTag: String): CachedChangelogData? {
    return try {
        val cacheFile = File(context.filesDir, "changelog_cache_$versionTag.json")
        if (!cacheFile.exists()) return null
        val cacheData = JSONObject(context.openFileInput("changelog_cache_$versionTag.json").use { it.bufferedReader().readText() })
        val sectionsArray = cacheData.optJSONArray("sections")
        val sections = mutableListOf<ChangelogSection>()
        if (sectionsArray != null) {
            for (i in 0 until sectionsArray.length()) {
                val sectionObj = sectionsArray.getJSONObject(i)
                val title = sectionObj.getString("title")
                val itemsArray = sectionObj.getJSONArray("items")
                val items = mutableListOf<String>()
                for (j in 0 until itemsArray.length()) {
                    items.add(itemsArray.getString(j))
                }
                sections.add(ChangelogSection(title, items))
            }
        }

        CachedChangelogData(
            sections = sections,
            image = cacheData.optString("image", null).takeIf { !it.isNullOrBlank() },
            description = cacheData.optString("description", null).takeIf { !it.isNullOrBlank() },
            warning = cacheData.optString("warning", null).takeIf { !it.isNullOrBlank() }
        )
    } catch (_: Exception) { null }
}

fun String.extractUrls(): List<Pair<IntRange, String>> {
    val urlRegex = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))".toRegex()
    return urlRegex.findAll(this).map { it.range to it.value }.toList()
}
