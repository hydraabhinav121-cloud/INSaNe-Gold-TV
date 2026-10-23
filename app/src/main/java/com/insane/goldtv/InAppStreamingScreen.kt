package com.insane.goldtv

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private val StreamingGold = Color(0xFFFFD36A)
private val StreamingBackground = Color(0xFF090A0D)

@Composable
fun InAppStreamingScreen(
    title: String,
    description: String,
    url: String,
    onClose: () -> Unit
) {
    val directMedia = remember(url) {
        val path = url.substringBefore('?').lowercase()
        path.endsWith(".m3u8") || path.endsWith(".mp4") || path.endsWith(".mpd")
    }
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        val page = webView
        if (!directMedia && page != null && page.canGoBack()) {
            page.goBack()
        } else {
            onClose()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(StreamingBackground).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(onClick = {
                val page = webView
                if (!directMedia && page != null && page.canGoBack()) page.goBack() else onClose()
            }, modifier = Modifier.focusable()) { Text("‹ Back") }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = StreamingGold, fontSize = 19.sp, style = MaterialTheme.typography.titleMedium)
                Text(description, color = Color.LightGray, fontSize = 11.sp, maxLines = 2)
            }
            if (!directMedia) {
                Button(onClick = { webView?.reload() }, modifier = Modifier.focusable()) { Text("Refresh") }
            }
        }

        if (directMedia) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val player = remember(url) {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(url))
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
                        useController = true
                        this.player = player
                        isFocusable = true
                        isFocusableInTouchMode = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { it.player = player },
                modifier = Modifier.fillMaxSize().weight(1f).background(Color.Black, RoundedCornerShape(10.dp))
            )
        } else {
            AndroidView(
                factory = { context: Context ->
                    WebView(context).apply {
                        webView = this
                        setBackgroundColor(android.graphics.Color.BLACK)
                        isFocusable = true
                        isFocusableInTouchMode = true
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.javaScriptCanOpenWindowsAutomatically = false
                        settings.setSupportMultipleWindows(false)
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                val scheme = request.url.scheme?.lowercase()
                                // Keep normal web navigation inside this screen; never launch another app.
                                return scheme != "https" && scheme != "http"
                            }
                        }
                        loadUrl(url)
                        requestFocus()
                    }
                },
                update = { view ->
                    webView = view
                    if (view.url.isNullOrBlank()) view.loadUrl(url)
                },
                onRelease = { view ->
                    if (webView === view) webView = null
                    view.stopLoading()
                    view.webChromeClient = null
                    view.webViewClient = WebViewClient()
                    view.destroy()
                },
                modifier = Modifier.fillMaxSize().weight(1f).background(Color.Black, RoundedCornerShape(10.dp))
            )
        }
        Text(
            if (directMedia) "Use the TV remote to control playback." else "This page stays inside the app. Some providers may block embedded playback or require their own sign-in/subscription.",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
