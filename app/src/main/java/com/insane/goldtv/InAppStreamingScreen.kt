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
    var pageProgress by remember { mutableStateOf(0) }
    var pageLoading by remember { mutableStateOf(!directMedia) }

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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, color = StreamingGold, fontSize = 19.sp, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Box(Modifier.background(Color(0xFF18392A), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(if (directMedia) "PLAYER" else if (pageLoading) "LOADING $pageProgress%" else "SOURCE OPEN", color = Color(0xFF9CF0B5), fontSize = 9.sp)
                    }
                }
                Text(description, color = Color.LightGray, fontSize = 11.sp, maxLines = 2)
            }
            if (!directMedia) {
                Button(onClick = { pageLoading = true; pageProgress = 0; webView?.reload() }, modifier = Modifier.focusable()) { Text("Refresh") }
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
                        settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        // YouTube search results get a simple D-pad focus model for TV remotes.
                        if (url.contains("youtube.com/results") || url.contains("youtube.com/shorts")) {
                            setOnKeyListener { _, keyCode, event ->
                                if (event?.action != android.view.KeyEvent.ACTION_DOWN) {
                                    false
                                } else {
                                    when (keyCode) {
                                        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                            evaluateJavascript("window.__goldTvMoveFocus && window.__goldTvMoveFocus(1)", null)
                                            true
                                        }
                                        android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                            evaluateJavascript("window.__goldTvMoveFocus && window.__goldTvMoveFocus(-1)", null)
                                            true
                                        }
                                        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                                        android.view.KeyEvent.KEYCODE_ENTER -> {
                                            evaluateJavascript("window.__goldTvActivate && window.__goldTvActivate()", null)
                                            true
                                        }
                                        else -> false
                                    }
                                }
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView, newProgress: Int) {
                                pageProgress = newProgress
                                pageLoading = newProgress < 100
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                                pageLoading = true
                                pageProgress = 0
                            }
                            override fun onPageFinished(view: WebView, url: String?) {
                                pageLoading = false
                                pageProgress = 100
                                if (url?.contains("youtube.com/results") == true || url?.contains("youtube.com/shorts") == true) {
                                    view.evaluateJavascript("""
                                        (function() {
                                          window.__goldTvIndex = -1;
                                          window.__goldTvLinks = function() {
                                            return Array.from(document.querySelectorAll('a[href*="/watch?v="], a[href*="/shorts/"]'))
                                              .filter(function(a) {
                                                var r = a.getBoundingClientRect();
                                                return r.width > 0 && r.height > 0 && a.href && !a.href.includes('googleads');
                                              });
                                          };
                                          window.__goldTvMoveFocus = function(direction) {
                                            var links = window.__goldTvLinks();
                                            if (!links.length) return;
                                            window.__goldTvIndex = Math.max(0, Math.min(links.length - 1, window.__goldTvIndex + direction));
                                            links.forEach(function(a) { a.style.outline = ''; a.style.outlineOffset = ''; });
                                            var target = links[window.__goldTvIndex];
                                            target.setAttribute('tabindex', '0');
                                            target.style.outline = '3px solid #FFD36A';
                                            target.style.outlineOffset = '3px';
                                            target.focus({preventScroll: true});
                                            target.scrollIntoView({block: 'center', behavior: 'auto'});
                                          };
                                          window.__goldTvActivate = function() {
                                            var links = window.__goldTvLinks();
                                            var target = links[window.__goldTvIndex];
                                            if (target) target.click();
                                          };
                                        })();
                                    """.trimIndent(), null)
                                }
                            }
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
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF17191F), RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("●", color = if (pageLoading && !directMedia) StreamingGold else Color(0xFF79D99A), fontSize = 10.sp)
            Text(
                if (directMedia) "Built-in playback. Press OK to show player controls."
                else if (pageLoading) "Loading the official source inside the app…"
                else "No ads are added by this app. Provider ads may still appear during live streams.",
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            Text("IN-APP", color = StreamingGold, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}
