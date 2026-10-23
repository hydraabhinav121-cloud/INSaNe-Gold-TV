package com.insane.goldtv

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private data class CloudVideoItem(
    val id: String,
    val title: String,
    val filename: String,
    val sizeBytes: Long,
    val playUrl: String
)

@Composable
fun CloudLibraryScreen(context: Context) {
    var serviceUrl by remember { mutableStateOf("") }
    var accessToken by remember { mutableStateOf("") }
    var videos by remember { mutableStateOf(emptyList<CloudVideoItem>()) }
    var message by remember { mutableStateOf("Deploy stream.py first, then enter your service URL and private token.") }
    var loading by remember { mutableStateOf(false) }
    var selectedVideo by remember { mutableStateOf<CloudVideoItem?>(null) }
    val scope = rememberCoroutineScope()
    val gold = Color(0xFFFFD36A)
    val panel = Color(0xFF17191F)

    suspend fun loadLibrary() {
        val base = serviceUrl.trim().trimEnd('/')
        val token = accessToken.trim()
        if (!base.startsWith("https://")) {
            message = "Enter your deployed HTTPS service URL, for example https://your-service.onrender.com"
            return
        }
        if (token.length < 32) {
            message = "Enter the private cloud token you set as STREAM_API_TOKEN."
            return
        }
        loading = true
        message = "Connecting to your private cloud..."
        try {
            val result = withContext(Dispatchers.IO) {
                val connection = (URL("$base/api/videos").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 20000
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/json")
                }
                try {
                    val code = connection.responseCode
                    val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                    val text = stream.bufferedReader().use { it.readText() }
                    if (code !in 200..299) {
                        val detail = runCatching { JSONObject(text).optString("detail") }.getOrDefault("")
                        throw IllegalStateException(if (detail.isNotBlank()) detail else "Cloud request failed ($code).")
                    }
                    val array = JSONObject(text).optJSONArray("videos")
                        ?: throw IllegalStateException("Cloud service returned an invalid video list.")
                    buildList {
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            add(
                                CloudVideoItem(
                                    id = item.optString("id"),
                                    title = item.optString("title", "Untitled video"),
                                    filename = item.optString("filename"),
                                    sizeBytes = item.optLong("sizeBytes"),
                                    playUrl = item.optString("playUrl")
                                )
                            )
                        }
                    }
                } finally {
                    connection.disconnect()
                }
            }
            videos = result
            message = if (result.isEmpty()) "Connected successfully. Upload a video from your phone to see it here."
                else "Connected · \${result.size} video(s) in your cloud."
        } catch (error: Exception) {
            message = error.message ?: "Could not connect. Check the URL, token, and internet connection."
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("MY CLOUD", color = gold, fontSize = 22.sp)
        Text(
            "Upload from your phone's browser, then stream the same cloud library here. Videos play over the internet; they are not saved as a second full copy on this TV.",
            color = Color.LightGray,
            fontSize = 13.sp
        )
        OutlinedTextField(
            value = serviceUrl,
            onValueChange = { serviceUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cloud service URL") },
            placeholder = { Text("https://your-service.onrender.com") },
            singleLine = true
        )
        OutlinedTextField(
            value = accessToken,
            onValueChange = { accessToken = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Private cloud token") },
            placeholder = { Text("STREAM_API_TOKEN") },
            singleLine = true
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { scope.launch { loadLibrary() } }, enabled = !loading) {
                Text("Connect / Refresh")
            }
            if (loading) {
                Spacer(Modifier.width(12.dp))
                CircularProgressIndicator(color = gold, modifier = Modifier.width(24.dp).height(24.dp), strokeWidth = 2.dp)
            }
        }
        Text(message, color = gold, fontSize = 13.sp)
        if (selectedVideo != null) {
            val playing = selectedVideo!!
            Text("NOW PLAYING · \${playing.title}", color = Color.White, fontSize = 18.sp)
            val player = remember(playing.playUrl) {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(playing.playUrl))
                    prepare()
                    playWhenReady = true
                }
            }
            DisposableEffect(player) {
                onDispose { player.release() }
            }
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        this.player = player
                        useController = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            (viewContext.resources.displayMetrics.density * 260).toInt()
                        )
                    }
                },
                update = { it.player = player },
                modifier = Modifier.fillMaxWidth().height(260.dp).background(Color.Black, RoundedCornerShape(12.dp))
            )
            Button(onClick = { selectedVideo = null }) { Text("Close player") }
        }
        if (videos.isEmpty() && !loading) {
            Text("Your uploaded cloud videos will appear below after you connect.", color = Color.LightGray, fontSize = 13.sp)
        }
        videos.forEach { video ->
            Column(
                modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(12.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(video.title, color = Color.White, fontSize = 17.sp)
                Text("\${video.filename} · \${formatCloudSize(video.sizeBytes)}", color = Color.LightGray, fontSize = 12.sp)
                Button(
                    onClick = {
                        if (video.playUrl.isBlank()) {
                            message = "No playback URL was returned. Refresh the library and try again."
                        } else {
                            selectedVideo = video
                        }
                    }
                ) { Text("▶ Play on this device") }
            }
        }
        Text(
            "Security: use only your own token and keep it private. Signed playback links expire after a few hours; reconnect to refresh them.",
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

private fun formatCloudSize(size: Long): String {
    if (size <= 0) return "Unknown size"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = size.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return "%.1f %s".format(value, units[unit])
}
