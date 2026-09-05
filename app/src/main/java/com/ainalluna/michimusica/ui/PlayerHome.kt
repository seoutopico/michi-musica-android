package com.ainalluna.michimusica.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ainalluna.michimusica.R
import com.ainalluna.michimusica.formatTime
import com.ainalluna.michimusica.library.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerHome(song: Song, playing: Boolean, position: Long, duration: Long,
               shuffle: Boolean, repeatOne: Boolean, ready: Boolean, canPrevious: Boolean, canNext: Boolean,
               buffering: Boolean, failed: Boolean, sourceName: String?, artworkRevision: Int,
               onClose: () -> Unit, onToggle: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit,
               onSeek: (Long) -> Unit, onShuffle: () -> Unit, onRepeat: () -> Unit,
               onLyrics: () -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    var scrubFraction by remember(song.id) { mutableStateOf<Float?>(null) }
    val shownPosition = scrubFraction?.let { playerSeekPosition(it, duration) }
        ?: if (duration > 0) position.coerceIn(0, duration) else position.coerceAtLeast(0)
    BoxWithConstraints(modifier.fillMaxSize()) {
        val artworkSize = minOf(maxWidth - 48.dp, (maxHeight - 440.dp).coerceIn(160.dp, 340.dp))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClose) { HomeIcon(R.drawable.ic_player_keyboard_arrow_down, "Minimizar reproductor", Modifier.size(30.dp)) }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ahora suena", Modifier.semantics { heading() }, fontFamily = FontFamily.Serif, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text(sourceName ?: "Tu biblioteca", Modifier.padding(top = 3.dp), fontSize = 12.sp,
                        color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { HomeCat(Modifier.size(24.dp)) }
            }
            Surface(Modifier.padding(top = 16.dp).size(artworkSize), shape = RoundedCornerShape(8.dp),
                color = colors.surfaceContainerHigh, shadowElevation = 8.dp) {
                SongArtwork(song, Modifier.fillMaxSize(), artworkRevision, maxDimension = 1024, largePlaceholder = true)
            }
            Column(Modifier.fillMaxWidth().padding(top = 22.dp)) {
                Text(song.title, fontFamily = FontFamily.Serif, fontSize = 27.sp, lineHeight = 33.sp,
                    fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(song.artist.ifBlank { song.album.ifBlank { song.filename } }, Modifier.padding(top = 6.dp),
                    fontSize = 16.sp, lineHeight = 22.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Slider(value = if (duration > 0) shownPosition.toFloat() / duration else 0f,
                onValueChange = { scrubFraction = it }, onValueChangeFinished = {
                    scrubFraction?.let { onSeek(playerSeekPosition(it, duration)) }; scrubFraction = null
                }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp).semantics {
                    contentDescription = "Posición de reproducción"
                    stateDescription = "${formatTime(shownPosition)} de ${formatTime(duration)}"
                }, enabled = ready && duration > 0,
                thumb = { Box(Modifier.size(14.dp).background(if (ready) colors.primary else colors.outline, CircleShape)) },
                track = { sliderState ->
                    Box(Modifier.fillMaxWidth().height(4.dp).background(colors.outline.copy(alpha = .4f), CircleShape)) {
                        Box(Modifier.fillMaxWidth(sliderState.value.coerceIn(0f, 1f)).height(4.dp).background(colors.primary, CircleShape))
                    }
                })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(shownPosition), fontSize = 12.sp, color = colors.onSurfaceVariant)
                Text(if (duration > 0) "−${formatTime((duration - shownPosition).coerceAtLeast(0))}" else "—", fontSize = 12.sp, color = colors.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onPrevious, Modifier.size(56.dp), enabled = ready && canPrevious) {
                    HomeIcon(R.drawable.ic_player_skip_previous, "Anterior", Modifier.size(38.dp))
                }
                Button(onToggle, Modifier.size(76.dp), enabled = ready, shape = CircleShape, contentPadding = PaddingValues(0.dp)) {
                    HomeIcon(if (playing) R.drawable.ic_home_pause else R.drawable.ic_home_play_arrow,
                        if (playing) "Pausar" else "Reproducir", Modifier.size(40.dp), colors.onPrimary)
                }
                IconButton(onNext, Modifier.size(56.dp), enabled = ready && canNext) {
                    HomeIcon(R.drawable.ic_home_skip_next, "Siguiente", Modifier.size(38.dp))
                }
            }
            if (failed) Column(Modifier.fillMaxWidth().padding(top = 12.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                Text("No se puede reproducir esta canción.", color = colors.error, fontSize = 14.sp)
                TextButton(onRetry, enabled = ready) { Text("Reintentar") }
            }
            else if (buffering || !ready) Row(Modifier.padding(top = 12.dp).semantics { liveRegion = LiveRegionMode.Polite }, verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("Preparando la canción…", Modifier.padding(start = 8.dp), fontSize = 13.sp, color = colors.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlayerMode("Azar", R.drawable.ic_home_shuffle, shuffle, ready, onShuffle, Modifier.weight(1f))
                PlayerMode(if (repeatOne) "Repetir una" else "Repetir", if (repeatOne) R.drawable.ic_player_repeat_one else R.drawable.ic_player_repeat,
                    repeatOne, ready, onRepeat, Modifier.weight(1f))
                Column(Modifier.weight(1f).heightIn(min = 64.dp).clickable(enabled = ready, role = Role.Button, onClick = onLyrics)
                    .padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    HomeIcon(R.drawable.ic_player_lyrics, null, tint = if (ready) colors.onSurfaceVariant else colors.onSurface.copy(alpha = .38f))
                    Text("Letra", Modifier.padding(top = 5.dp), fontSize = 12.sp, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PlayerMode(label: String, icon: Int, active: Boolean, enabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val tint = if (!enabled) colors.onSurface.copy(alpha = .38f) else if (active) colors.primary else colors.onSurfaceVariant
    Column(modifier.heightIn(min = 64.dp).toggleable(active, enabled = enabled, role = Role.Switch, onValueChange = { onToggle() })
        .padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        HomeIcon(icon, null, tint = tint)
        Text(label, Modifier.padding(top = 5.dp), color = tint, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

internal fun playerSeekPosition(fraction: Float, duration: Long): Long =
    if (!fraction.isFinite() || duration <= 0) 0L else (fraction.coerceIn(0f, 1f).toDouble() * duration).toLong().coerceIn(0, duration)
