package com.ainalluna.michimusica.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ainalluna.michimusica.R
import com.ainalluna.michimusica.formatTime
import com.ainalluna.michimusica.library.AudioSection
import com.ainalluna.michimusica.library.Song
import com.ainalluna.michimusica.searchable

data class LibraryNotice(val text: String, val needsFolder: Boolean = false)

/** Approved opening screen: real data and callbacks; no playback instance is owned by the UI. */
@Composable
fun LibraryHome(
    songs: List<Song>, selectedSongId: String?, playing: Boolean, ready: Boolean,
    loading: Boolean, notice: LibraryNotice?, sourceName: String?, artworkRevision: Int,
    onSelect: (Song) -> Unit, onShuffle: () -> Unit, onSettings: () -> Unit,
    onChooseFolder: () -> Unit, onAllMusic: () -> Unit, onDismissNotice: () -> Unit,
    onDelete: ((Song) -> Unit)? = null,
    section: AudioSection = AudioSection.MUSIC, onSection: (AudioSection) -> Unit = {},
    onClassify: ((Song) -> Unit)? = null, episodePosition: (Song) -> Long = { 0L },
    podcastHeader: (@Composable () -> Unit)? = null,
    podcastContent: (LazyListScope.() -> Unit)? = null,
    podcastDownloads: (LazyListScope.() -> Unit)? = null,
) {
    var query by rememberSaveable(section) { mutableStateOf("") }
    val positions by produceState(emptyMap<String, Long>(), songs.toList(), section) {
        if (section == AudioSection.PODCASTS) while (true) {
            value = songs.associate { it.id to episodePosition(it) }
            kotlinx.coroutines.delay(5_000)
        }
    }
    val wanted = searchable(query)
    val filtered by remember(songs, wanted) { derivedStateOf { songs.filter { wanted.isBlank() || searchable("${it.title} ${it.artist} ${it.album} ${it.filename}").contains(wanted) } } }
    val colors = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)) {
        item(key = "brand") {
            Row(Modifier.fillMaxWidth().padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).clearAndSetSemantics { contentDescription = "Michi Música" }, verticalAlignment = Alignment.CenterVertically) {
                    HomeCat(Modifier.size(26.dp))
                    Text("michi", Modifier.padding(start = 10.dp), fontSize = 23.sp, letterSpacing = 1.sp, color = colors.primary)
                }
                IconButton(onSettings) { HomeIcon(R.drawable.ic_home_settings, "Ajustes", Modifier.size(27.dp)) }
            }
            Text("Biblioteca", Modifier.padding(start = 8.dp, top = 12.dp, bottom = 20.dp).semantics { heading() },
                fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold)
        }
        item(key = "sections") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp).selectableGroup()) {
                AudioSection.entries.forEach { item ->
                    Column(Modifier.weight(1f).selectable(section == item, role = Role.Tab, onClick = { focus.clearFocus(); onSection(item) })) {
                        Text(item.label, Modifier.padding(vertical = 14.dp).align(Alignment.CenterHorizontally),
                            color = if (section == item) colors.primary else colors.onSurfaceVariant, fontSize = 17.sp,
                            fontWeight = if (section == item) FontWeight.SemiBold else FontWeight.Normal)
                        HorizontalDivider(thickness = if (section == item) 2.dp else 1.dp,
                            color = if (section == item) colors.primary else colors.outline.copy(alpha = .4f))
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        if (section == AudioSection.PODCASTS && podcastHeader != null) item(key = "podcast-controls") { podcastHeader() }
        if (section == AudioSection.PODCASTS && podcastContent != null) podcastContent(this) else {
        if (section == AudioSection.PODCASTS) podcastDownloads?.invoke(this)
        item(key = "filter") {
            TextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                placeholder = { Text(if (section == AudioSection.PODCASTS) "Buscar en tus podcasts" else "Buscar en tu música", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { HomeIcon(R.drawable.ic_home_search, null) },
                trailingIcon = if (query.isNotEmpty()) { { IconButton({ query = "" }) { HomeIcon(R.drawable.ic_home_close, "Borrar búsqueda") } } } else null,
                singleLine = true, shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceContainerHigh, unfocusedContainerColor = colors.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                ))
        }
        item(key = "heading") {
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, top = 18.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (section == AudioSection.PODCASTS) "Descargados" else "Canciones", Modifier.semantics { heading() }, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Text(if (wanted.isBlank()) "${songs.size} ${if (section == AudioSection.PODCASTS) { if (songs.size == 1) "episodio" else "episodios" } else if (songs.size == 1) "canción" else "canciones"}" else "${filtered.size} de ${songs.size}",
                        Modifier.padding(top = 3.dp), color = colors.onSurfaceVariant, fontSize = 14.sp)
                }
                if (section == AudioSection.MUSIC) TextButton({ focus.clearFocus(); onShuffle() }, enabled = ready && songs.isNotEmpty() && !loading) {
                    HomeIcon(R.drawable.ic_home_shuffle, null, tint = if (ready) colors.primary else colors.onSurfaceVariant)
                    Text("Azar", Modifier.padding(start = 8.dp), fontSize = 16.sp)
                }
            }
            if (sourceName != null) Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(sourceName, Modifier.weight(1f), fontSize = 14.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                TextButton(onAllMusic) { Text("Ver toda") }
            }
        }
        if (notice != null) item(key = "notice") {
            Surface(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).semantics { liveRegion = LiveRegionMode.Polite },
                color = if (notice.needsFolder) colors.errorContainer else colors.surfaceContainerHigh, shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(notice.text, color = if (notice.needsFolder) colors.onErrorContainer else colors.onSurface)
                    TextButton(if (notice.needsFolder) onChooseFolder else onDismissNotice) { Text(if (notice.needsFolder) "Elegir carpeta" else "Entendido") }
                }
            }
        }
        if (loading) item(key = "loading") {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp))
            Text("Leyendo tu biblioteca…", Modifier.padding(8.dp).semantics { liveRegion = LiveRegionMode.Polite }, color = colors.onSurfaceVariant)
        }
        if (!loading && filtered.isEmpty() && notice?.needsFolder != true) item(key = "empty") {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (songs.isEmpty()) { if (section == AudioSection.PODCASTS) "Tus podcasts, aquí" else "Tu biblioteca está vacía" } else "No hay coincidencias", style = MaterialTheme.typography.titleLarge)
                Text(if (songs.isEmpty()) { if (section == AudioSection.PODCASTS) "Descarga un episodio desde Siguiendo o Novedades. También puedes guardar un audio desde Buscar eligiendo Podcasts." else "Elige una carpeta que contenga música." } else "Prueba con otro título, artista o álbum.", color = colors.onSurfaceVariant)
                if (section == AudioSection.MUSIC || songs.isNotEmpty()) TextButton(if (songs.isEmpty()) onChooseFolder else { { query = "" } }) { Text(if (songs.isEmpty()) "Elegir carpeta" else "Borrar búsqueda") }
            }
        }
        items(filtered, key = Song::id) { song ->
            HomeSongRow(song, song.id == selectedSongId, playing && song.id == selectedSongId, ready && !loading,
                artworkRevision, podcast = section == AudioSection.PODCASTS, position = positions[song.id] ?: 0L,
                onClassify = onClassify?.let { action -> { action(song) } }, onDelete = onDelete?.let { action -> { focus.clearFocus(); action(song) } }) { focus.clearFocus(); onSelect(song) }
        }
        }
    }
}

@Composable
private fun HomeSongRow(song: Song, selected: Boolean, playing: Boolean, enabled: Boolean, artworkRevision: Int, onDelete: (() -> Unit)?, podcast: Boolean, position: Long, onClassify: (() -> Unit)?, onSelect: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var menuOpen by remember(song.id) { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(12.dp))
        .background(if (selected) Brush.horizontalGradient(listOf(colors.primaryContainer.copy(alpha = .6f), colors.surfaceContainerHigh.copy(alpha = .5f))) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
        .clickable(enabled = enabled, role = Role.Button, onClickLabel = "Reproducir ${song.title}", onClick = onSelect)
        .semantics { this.selected = selected }) {
        if (selected) Box(Modifier.align(Alignment.CenterStart).width(2.dp).height(46.dp).background(colors.primary))
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SongArtwork(song, Modifier.size(72.dp), artworkRevision)
            Column(Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
                Text(song.title, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(song.artist.ifBlank { song.filename }, Modifier.weight(1f), fontSize = 14.sp, lineHeight = 20.sp,
                    color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (song.durationMs > 0) formatTime(song.durationMs) else "—", Modifier.padding(start = 8.dp), fontSize = 13.sp, color = colors.onSurfaceVariant)
                }
                if (podcast) Text(when {
                    song.durationMs > 0 && position >= song.durationMs -> "Escuchado"
                    position > 0 -> "Continuar · ${formatTime(position)}"
                    else -> "Sin empezar"
                }, color = colors.primary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
            if (playing) PlayingBars(Modifier.size(16.dp))
            if (onDelete != null || onClassify != null) Box {
                IconButton({ menuOpen = true }, enabled = enabled) {
                    HomeIcon(R.drawable.ic_lyrics_more_vert, "Opciones de ${song.title}", tint = colors.onSurfaceVariant)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (onClassify != null) DropdownMenuItem(text = { Text(if (podcast) "Marcar como música" else "Marcar como podcast") }, onClick = { menuOpen = false; onClassify() })
                    if (onDelete != null) DropdownMenuItem(text = { Text(if (podcast) "Borrar episodio" else "Borrar canción", color = colors.error) }, onClick = { menuOpen = false; onDelete() })
                }
            }
        }
    }
}

@Composable
fun HomeMiniPlayer(song: Song, playing: Boolean, position: Long, duration: Long, ready: Boolean, nextEnabled: Boolean,
                   artworkRevision: Int, onToggle: () -> Unit, onNext: () -> Unit, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp), color = colors.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, colors.outline.copy(alpha = .65f)), shadowElevation = 4.dp) {
        Column(Modifier.background(Brush.horizontalGradient(listOf(colors.primaryContainer.copy(alpha = .25f), Color.Transparent)))) {
            Row(Modifier.padding(start = 10.dp, end = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).heightIn(min = 56.dp).clickable(enabled = ready, role = Role.Button, onClickLabel = "Abrir Ahora suena", onClick = onOpen), verticalAlignment = Alignment.CenterVertically) {
                    SongArtwork(song, Modifier.size(48.dp), artworkRevision)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(song.title, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${if (playing) "Reproduciendo" else "En pausa"} · ${formatTime(position)}", Modifier.padding(top = 4.dp),
                            fontSize = 12.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                IconButton(onToggle, enabled = ready) { HomeIcon(if (playing) R.drawable.ic_home_pause else R.drawable.ic_home_play_arrow, if (playing) "Pausar" else "Continuar", Modifier.size(32.dp)) }
                IconButton(onNext, enabled = ready && nextEnabled) { HomeIcon(R.drawable.ic_home_skip_next, "Siguiente canción", Modifier.size(27.dp)) }
            }
            LinearProgressIndicator(progress = { if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f },
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 7.dp, bottom = 9.dp).height(2.dp),
                color = colors.primary, trackColor = colors.outline.copy(alpha = .3f), gapSize = 0.dp, drawStopIndicator = {})
        }
    }
}

@Composable
fun HomeNavigation(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().background(colors.background)) {
    Spacer(Modifier.height(8.dp))
    HorizontalDivider(color = colors.outline.copy(alpha = .55f))
    Row(Modifier.fillMaxWidth().padding(top = 4.dp).navigationBarsPadding().selectableGroup()) {
        listOf("Biblioteca" to R.drawable.ic_home_library_music, "Buscar" to R.drawable.ic_home_search, "Listas" to R.drawable.ic_home_playlist_play).forEachIndexed { index, item ->
            Column(Modifier.weight(1f).heightIn(min = 72.dp).selectable(selectedIndex == index, role = Role.Tab, onClick = { onSelect(index) }).padding(top = 10.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                HomeIcon(item.second, null, Modifier.size(26.dp), if (selectedIndex == index) colors.primary else colors.onSurfaceVariant)
                Text(item.first, fontSize = 12.sp, color = if (selectedIndex == index) colors.primary else colors.onSurfaceVariant)
            }
        }
    }
    }
}

@Composable
fun HomeCat(modifier: Modifier = Modifier) {
    val tint = MaterialTheme.colorScheme.primary
    Canvas(modifier.clearAndSetSemantics {}) {
        val w = size.width; val h = size.height
        val outline = Path().apply {
            moveTo(w * .12f, h * .56f); lineTo(w * .12f, h * .08f); quadraticTo(w * .14f, 0f, w * .22f, h * .1f)
            lineTo(w * .38f, h * .28f); lineTo(w * .62f, h * .28f); lineTo(w * .8f, h * .08f)
            quadraticTo(w * .89f, 0f, w * .88f, h * .16f); lineTo(w * .88f, h * .56f)
            lineTo(w * .97f, h * .68f); quadraticTo(w * .85f, h * .9f, w * .5f, h * .99f)
            quadraticTo(w * .15f, h * .9f, w * .03f, h * .68f); close()
        }
        drawPath(outline, tint, style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(tint, w * .045f, Offset(w * .31f, h * .65f))
        drawCircle(tint, w * .045f, Offset(w * .69f, h * .65f))
    }
}

@Composable
private fun PlayingBars(modifier: Modifier) {
    val tint = MaterialTheme.colorScheme.primary
    Canvas(modifier.semantics { contentDescription = "Reproduciendo" }) {
        listOf(.45f, .7f, 1f).forEachIndexed { index, height ->
            val x = size.width * (.15f + index * .35f)
            drawLine(tint, Offset(x, size.height), Offset(x, size.height * (1f - height)), size.width * .2f, StrokeCap.Round)
        }
    }
}

@Composable
fun HomeIcon(@DrawableRes resource: Int, description: String?, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(painterResource(resource), description, modifier.sizeIn(minWidth = 24.dp, minHeight = 24.dp), tint)
}
