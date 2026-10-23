package com.insane.goldtv

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.insane.goldtv.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val ExplorerGold = Color(0xFFFFD36A)
private val ExplorerPanel = Color(0xFF17191F)

private data class DiscoveryTitle(
    val id: Int,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val rating: Double,
    val year: String,
    val isSeries: Boolean
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

private enum class DiscoveryMode(val label: String, val endpoint: String, val series: Boolean) {
    TRENDING("Trending", "/trending/movie/week", false),
    SERIES("TV Series", "/trending/tv/week", true),
    FAMILY("Family", "/discover/movie?include_adult=false&with_genres=10751", false),
    ANIMATION("Animation", "/discover/movie?include_adult=false&with_genres=16", false)
}

@Composable
fun MovieExplorerScreen() {
    val context = LocalContext.current
    val apiKey = BuildConfig.TMDB_API_KEY
    var mode by remember { mutableStateOf(DiscoveryMode.TRENDING) }
    var query by remember { mutableStateOf("") }
    var titles by remember { mutableStateOf<List<DiscoveryTitle>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mode, query, apiKey) {
        if (apiKey.isBlank()) {
            titles = emptyList()
            error = "TMDB_API_KEY is not configured yet."
            loading = false
        } else {
            loading = true
            error = null
            try {
                val requestPath = if (query.isNotBlank()) {
                    val encoded = URLEncoder.encode(query.trim(), "UTF-8")
                    "/search/${if (mode.series) "tv" else "movie"}?query=$encoded&include_adult=false"
                } else {
                    mode.endpoint
                }
                titles = withContext(Dispatchers.IO) { fetchDiscoveryTitles(apiKey, requestPath, mode.series) }
                if (titles.isEmpty()) error = "No titles found. Try another search or category."
            } catch (exception: Exception) {
                error = "Couldn't load the catalogue. Check internet access and the TMDB API key."
                titles = emptyList()
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF201B10), Color(0xFF090A0D)))
        ).padding(horizontal = 22.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("MOVIE EXPLORER", color = ExplorerGold, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("Discover films and series • India", color = Color.LightGray, fontSize = 12.sp)
            }
            Text("SHREE REEMPU DEVI TV", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(ExplorerPanel).border(1.dp, Color(0xFF6F5726), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⌕", color = ExplorerGold, fontSize = 23.sp)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                if (query.isBlank()) Text("Search movies or TV series…", color = Color.Gray, fontSize = 14.sp)
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth().focusable()
                )
            }
            if (query.isNotBlank()) Text(
                "CLEAR", color = ExplorerGold, fontSize = 11.sp,
                modifier = Modifier.clickable { query = "" }.focusable().padding(6.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DiscoveryMode.entries.forEach { option ->
                ExplorerChip(option.label, mode == option, Modifier.weight(1f)) { mode = option }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (query.isBlank()) when (mode) {
                DiscoveryMode.TRENDING -> "TRENDING MOVIES"
                DiscoveryMode.SERIES -> "POPULAR TV SERIES"
                DiscoveryMode.FAMILY -> "FAMILY MOVIES"
                DiscoveryMode.ANIMATION -> "ANIMATED MOVIES"
            } else "SEARCH RESULTS",
            color = ExplorerGold, fontSize = 12.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading movie catalogue…", color = ExplorerGold, fontSize = 17.sp)
            }
            error != null -> Box(
                Modifier.fillMaxSize().padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error ?: "", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (apiKey.isBlank()) "Add a TMDB_API_KEY secret in GitHub → Settings → Secrets and variables → Actions, then rebuild the app."
                        else "You can still use the existing Free Movies and official-source catalogue tabs.",
                        color = Color.LightGray, fontSize = 12.sp
                    )
                }
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(titles, key = { "${it.isSeries}-${it.id}" }) { title ->
                    DiscoveryCard(
                        title = title,
                        onWatchOptions = {
                            val kind = if (title.isSeries) "tv" else "movie"
                            openExternal(context, "https://www.themoviedb.org/$kind/${title.id}/watch")
                        },
                        onTrailer = {
                            val searchText = URLEncoder.encode("${title.name} official trailer", "UTF-8")
                            openExternal(context, "https://www.youtube.com/results?search_query=$searchText")
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Movie data and images provided by TMDB. This product uses the TMDB API but is not endorsed or certified by TMDB. Availability depends on each licensed provider.",
            color = Color(0xFFB5B5B5), fontSize = 9.sp, maxLines = 2
        )
    }
}

@Composable
private fun ExplorerChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier.clip(RoundedCornerShape(10.dp))
            .background(if (selected || focused) Color(0xFF3A2D12) else ExplorerPanel)
            .border(if (selected || focused) 2.dp else 1.dp, if (selected || focused) ExplorerGold else Color(0xFF393B42), RoundedCornerShape(10.dp))
            .onFocusChanged { focused = it.isFocused }.focusable().clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected || focused) ExplorerGold else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DiscoveryCard(title: DiscoveryTitle, onWatchOptions: () -> Unit, onTrailer: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(ExplorerPanel)
            .border(if (focused) 2.dp else 1.dp, if (focused) ExplorerGold else Color(0xFF34363D), RoundedCornerShape(12.dp))
            .onFocusChanged { focused = it.isFocused }
            .padding(7.dp)
    ) {
        if (title.posterUrl != null) {
            AsyncImage(
                model = title.posterUrl,
                contentDescription = "${title.name} poster",
                modifier = Modifier.fillMaxWidth().height(158.dp).clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(Modifier.fillMaxWidth().height(158.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF29251B)), contentAlignment = Alignment.Center) {
                Text("SHREE TV", color = ExplorerGold, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(title.name, color = if (focused) ExplorerGold else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, minLines = 2)
        Text(
            listOf(title.year, if (title.rating > 0) "★ ${String.format("%.1f", title.rating)}" else "").filter { it.isNotBlank() }.joinToString("  •  "),
            color = Color.LightGray, fontSize = 10.sp
        )
        Spacer(Modifier.height(5.dp))
        Text(title.overview.ifBlank { "Open details to learn more about this title." }, color = Color(0xFFCCCCCC), fontSize = 10.sp, maxLines = 3, minLines = 3)
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("WATCH OPTIONS", color = ExplorerGold, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF332811)).clickable { onWatchOptions() }.focusable().padding(7.dp))
            Text("TRAILER", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF30323A)).clickable { onTrailer() }.focusable().padding(7.dp))
        }
    }
}

private fun openExternal(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

private fun fetchDiscoveryTitles(apiKey: String, path: String, series: Boolean): List<DiscoveryTitle> {
    val separator = if (path.contains("?")) "&" else "?"
    val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
    val url = URL("https://api.themoviedb.org/3$path${separator}api_key=$encodedKey&language=en-US&region=IN")
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10000
        readTimeout = 10000
        setRequestProperty("accept", "application/json")
    }
    try {
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        if (status !in 200..299) throw IllegalStateException("TMDB returned HTTP $status")
        val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
        return (0 until results.length()).mapNotNull { index ->
            val item = results.optJSONObject(index) ?: return@mapNotNull null
            if (item.optBoolean("adult", false)) return@mapNotNull null
            val id = item.optInt("id", 0)
            if (id <= 0) return@mapNotNull null
            val name = item.optString(if (series) "name" else "title", "").ifBlank {
                item.optString(if (series) "original_name" else "original_title", "Untitled")
            }
            val date = item.optString(if (series) "first_air_date" else "release_date", "")
            DiscoveryTitle(
                id = id,
                name = name,
                overview = item.optString("overview", ""),
                posterPath = item.optString("poster_path", "").takeIf { it.isNotBlank() && it != "null" },
                rating = item.optDouble("vote_average", 0.0),
                year = date.take(4),
                isSeries = series
            )
        }
    } finally {
        connection.disconnect()
    }
}
