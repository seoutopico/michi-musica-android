package com.ainalluna.michimusica.youtube

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ainalluna.michimusica.R
import com.ainalluna.michimusica.ui.HomeIcon
import kotlinx.coroutines.delay

/** The bridge can only report playback state; it has no file, intent or credential access. */
private class VideoEvents(private val dispatch: (String, String) -> Unit) {
    @JavascriptInterface fun event(kind: String, value: String) {
        if (kind in setOf("ready", "state", "time", "error") && value.length <= 32) dispatch(kind, value)
    }
}

private data class FullscreenVideo(val view: View, val callback: WebChromeClient.CustomViewCallback)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeVideoPlayer(result: YouTubeResult, startSeconds: Double, autoplay: Boolean,
                       onPlaying: (Boolean) -> Unit, onPosition: (Double) -> Unit,
                       registerPause: (() -> Unit) -> Unit, onClose: () -> Unit) {
    key(result.id) {
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val playingCallback by rememberUpdatedState(onPlaying)
        val positionCallback by rememberUpdatedState(onPosition)
        var ready by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var attempt by remember { mutableIntStateOf(0) }
        var fullscreen by remember { mutableStateOf<FullscreenVideo?>(null) }
        var disposed by remember { mutableStateOf(false) }
        fun openYouTube() {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=${result.id}".toUri())) }
                .onFailure { error = "No hay una aplicación disponible para abrir YouTube." }
        }
        fun exitFullscreen() {
            val current = fullscreen
            fullscreen = null
            current?.callback?.onCustomViewHidden()
        }
        val webView = remember {
            WebView(context).apply {
                setBackgroundColor(AndroidColor.BLACK)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                addJavascriptInterface(VideoEvents { kind, value ->
                    post {
                        if (!disposed) when (kind) {
                            "ready" -> { ready = true; error = null }
                            "state" -> {
                                val playing = value.toIntOrNull() == 1
                                keepScreenOn = playing
                                playingCallback(playing)
                            }
                            "time" -> value.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }?.let(positionCallback)
                            "error" -> { ready = true; error = youtubeEmbedError(value.toIntOrNull() ?: 0); playingCallback(false) }
                        }
                    }
                }, "MichiVideo")
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        if (!request.isForMainFrame) return false
                        // Keep this WebView on our local wrapper; user links open the selected video externally.
                        if (request.hasGesture() && request.url.scheme == "https") openYouTube()
                        return true
                    }
                    override fun onReceivedError(view: WebView, request: WebResourceRequest, failure: WebResourceError) {
                        if (request.isForMainFrame) { ready = true; error = youtubeEmbedError(0) }
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                        if (fullscreen != null || disposed) callback.onCustomViewHidden()
                        else fullscreen = FullscreenVideo(view, callback)
                    }
                    override fun onHideCustomView() { fullscreen = null }
                }
            }
        }
        val pause = remember(webView) { {
            webView.evaluateJavascript("if(window.pauseMichiVideo)pauseMichiVideo();", null)
            webView.keepScreenOn = false
        } }
        LaunchedEffect(webView, attempt) {
            ready = false; error = null
            webView.loadDataWithBaseURL("https://${context.packageName}/",
                youtubeEmbedHtml(result.id, context.packageName, startSeconds, autoplay), "text/html", "utf-8", null)
            delay(20_000)
            if (!ready) { error = "El vídeo tarda en cargar. Comprueba la conexión y vuelve a intentarlo." }
        }
        DisposableEffect(webView, lifecycle) {
            registerPause(pause)
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> { pause(); playingCallback(false); exitFullscreen(); webView.onPause() }
                    Lifecycle.Event.ON_RESUME -> webView.onResume()
                    else -> Unit
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                disposed = true
                lifecycle.removeObserver(observer)
                registerPause({})
                exitFullscreen()
                webView.stopLoading()
                webView.removeJavascriptInterface("MichiVideo")
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.destroy()
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(result.title, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                IconButton(onClose) { HomeIcon(R.drawable.ic_home_close, "Cerrar vídeo") }
            }
            // YouTube requires at least a 200 × 200 viewport, even on narrow phones.
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                AndroidView(factory = { webView }, modifier = Modifier.fillMaxWidth().height((maxWidth * 9 / 16).coerceAtLeast(200.dp)))
            }
            if (!ready && error == null) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
            if (error != null) Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                Text(error.orEmpty(), Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                Row {
                    TextButton({ attempt++ }) { Text("Reintentar") }
                    TextButton({ pause(); openYouTube() }) { Text("Abrir en YouTube") }
                }
            } else Text("Pantalla completa: toca ⛶ en el vídeo", Modifier.padding(top = 8.dp, bottom = 8.dp),
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .3f))
        }
        fullscreen?.let { video -> FullscreenVideoDialog(video, ::exitFullscreen) }
    }
}

@Composable
private fun FullscreenVideoDialog(video: FullscreenVideo, onDismiss: () -> Unit) {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity) {
        val previous = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { if (previous != null) activity.requestedOrientation = previous }
    }
    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        val view = LocalView.current
        DisposableEffect(view) {
            val window = (view.parent as DialogWindowProvider).window
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val insets = WindowCompat.getInsetsController(window, view)
            insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insets.hide(WindowInsetsCompat.Type.systemBars())
            onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); insets.show(WindowInsetsCompat.Type.systemBars()) }
        }
        Column(Modifier.fillMaxSize().background(Color.Black).displayCutoutPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onDismiss) { Text("Salir de pantalla completa", color = Color.White) }
            }
            AndroidView(factory = { video.view }, modifier = Modifier.fillMaxWidth().weight(1f),
                onRelease = { (it.parent as? ViewGroup)?.removeView(it) })
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
