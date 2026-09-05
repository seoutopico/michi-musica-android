package com.ainalluna.michimusica.lyrics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ainalluna.michimusica.R
import com.ainalluna.michimusica.formatTime
import com.ainalluna.michimusica.library.Song
import com.ainalluna.michimusica.ui.HomeCat
import com.ainalluna.michimusica.ui.HomeIcon

@Composable
fun LyricsHome(song: Song, state: LyricsUiState, positionMs: Long, onBack: () -> Unit, onEdit: () -> Unit,
               onTitle: (String) -> Unit, onArtist: (String) -> Unit, onSearch: () -> Unit,
               onPreview: (LyricsMatch) -> Unit, onSave: () -> Unit, onRemove: () -> Unit,
               onSeek: (Long) -> Unit, onChooseFolder: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton({ focus.clearFocus(); onBack() }) { HomeIcon(R.drawable.ic_lyrics_arrow_back, "Volver") }
            Text(if (state.candidate != null) "Vista previa" else if (state.editing) "Buscar letra" else "Letra",
                Modifier.weight(1f).padding(horizontal = 8.dp).semantics { heading() }, fontFamily = FontFamily.Serif,
                fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
            Box {
                if (state.stored != null && !state.editing) {
                    IconButton({ menu = true }, enabled = state.busy == null) { HomeIcon(R.drawable.ic_lyrics_more_vert, "Opciones de letra") }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text("Buscar otra letra") }, onClick = { menu = false; onEdit() })
                        DropdownMenuItem(text = { Text("Quitar letra guardada", color = colors.error) }, onClick = { menu = false; confirmRemove = true })
                    }
                } else Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { HomeCat(Modifier.size(24.dp)) }
            }
        }
        Text(song.title, Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp), fontSize = 16.sp,
            fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(song.artist.ifBlank { song.album }, Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp),
            fontSize = 13.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (state.loading) Column(Modifier.padding(24.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Abriendo tu letra…", Modifier.padding(top = 12.dp), color = colors.onSurfaceVariant)
        }
        else if (state.editing) LyricsSearch(state, onTitle, onArtist, onSearch, onPreview, onSave, onChooseFolder, Modifier.weight(1f))
        else {
            if (state.error != null) LyricsError(state, onChooseFolder, Modifier.padding(horizontal = 24.dp))
            if (state.busy != null) Text(state.busy, Modifier.padding(horizontal = 24.dp).semantics { liveRegion = LiveRegionMode.Polite }, color = colors.onSurfaceVariant)
            val stored = state.stored
            if (stored != null && stored.content.isNotBlank()) LyricsReader(stored, positionMs, onSeek, Modifier.weight(1f))
            else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)) {
                item {
                    Text("Cada canción\ntiene algo que decir.", fontFamily = FontFamily.Serif, fontSize = 28.sp, lineHeight = 35.sp)
                    Text("Todavía no hay una letra guardada para esta canción.", Modifier.padding(top = 16.dp), fontSize = 15.sp, lineHeight = 23.sp, color = colors.onSurfaceVariant)
                    Button(onEdit, Modifier.fillMaxWidth().padding(top = 24.dp).heightIn(min = 48.dp), enabled = state.busy == null, shape = RoundedCornerShape(14.dp)) { Text("Buscar letra") }
                    Text("Busca en LRCLIB y guarda la versión que corresponda. Después podrás leerla sin conexión.",
                        Modifier.padding(top = 16.dp), fontSize = 13.sp, lineHeight = 20.sp, color = colors.onSurfaceVariant)
                }
            }
        }
    }
    if (confirmRemove) AlertDialog(onDismissRequest = { confirmRemove = false }, title = { Text("¿Quitar esta letra?") },
        text = { Text("Se quitará la letra guardada de esta canción. El audio se conserva.") },
        confirmButton = { TextButton({ confirmRemove = false; onRemove() }) { Text("Quitar letra", color = colors.error) } },
        dismissButton = { TextButton({ confirmRemove = false }) { Text("Cancelar") } })
}

@Composable
fun LyricsReader(stored: StoredLyrics, positionMs: Long, onSeek: (Long) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val timed = remember(stored) { if (stored.synced) LrcParser.parse(stored.content) else emptyList() }
    val lines = remember(stored, timed) { if (timed.isNotEmpty()) timed.map { it.text } else stored.content.lines() }
    val active = if (timed.isEmpty()) -1 else LrcParser.activeIndex(timed, positionMs)
    val listState = rememberLazyListState()
    var following by remember(stored) { mutableStateOf(true) }
    val dragging by listState.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(dragging) { if (dragging) following = false }
    LaunchedEffect(active, following, stored) {
        if (following && active >= 0) listState.animateScrollToItem((active - 1).coerceAtLeast(0))
    }
    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (timed.isNotEmpty()) "Sincronizada" else "Letra sin tiempos", Modifier.weight(1f), fontSize = 13.sp, color = colors.onSurfaceVariant)
            if (timed.isNotEmpty() && !following) TextButton({ following = true }) { Text("Seguir canción") }
        }
        if (timed.isNotEmpty()) Text("Toca un verso para ir a ese momento.", Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp), fontSize = 12.sp, color = colors.onSurfaceVariant)
        LazyColumn(Modifier.fillMaxWidth().weight(1f), state = listState, contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(lines) { index, line ->
                val clickable = if (timed.isNotEmpty()) Modifier.clickable(role = Role.Button, onClickLabel = "Ir a ${formatTime(timed[index].timeMs)}") { onSeek(timed[index].timeMs) } else Modifier
                Text(line, Modifier.fillMaxWidth().then(clickable).heightIn(min = if (timed.isNotEmpty()) 48.dp else 0.dp)
                    .padding(vertical = 4.dp).semantics { if (timed.isNotEmpty()) selected = index == active },
                    fontFamily = FontFamily.Serif, fontSize = if (timed.isNotEmpty()) 27.sp else 23.sp,
                    lineHeight = if (timed.isNotEmpty()) 36.sp else 33.sp,
                    fontWeight = if (index == active) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == active) colors.primary else colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LyricsSearch(state: LyricsUiState, onTitle: (String) -> Unit, onArtist: (String) -> Unit,
                         onSearch: () -> Unit, onPreview: (LyricsMatch) -> Unit, onSave: () -> Unit,
                         onChooseFolder: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current
    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val candidate = state.candidate
        if (candidate == null) {
            item {
                Text("Título y artista para esta búsqueda", fontSize = 14.sp, color = colors.onSurfaceVariant)
                TextField(state.title, onTitle, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Título") }, singleLine = true,
                    shape = RoundedCornerShape(14.dp), colors = lyricFieldColors(), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                TextField(state.artist, onArtist, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Artista") }, singleLine = true,
                    shape = RoundedCornerShape(14.dp), colors = lyricFieldColors(), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus(); onSearch() }))
                Button({ focus.clearFocus(); onSearch() }, Modifier.fillMaxWidth().padding(top = 16.dp).heightIn(min = 48.dp),
                    enabled = state.busy == null && state.title.isNotBlank(), shape = RoundedCornerShape(14.dp)) { Text("Buscar en LRCLIB") }
            }
        } else item {
            Text(candidate.trackName, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Text("${candidate.artistName} · ${if (candidate.synced) "Sincronizada" else "Sin tiempos"}", Modifier.padding(top = 6.dp), fontSize = 14.sp, color = colors.onSurfaceVariant)
            Button(onSave, Modifier.fillMaxWidth().padding(top = 16.dp).heightIn(min = 48.dp), enabled = state.busy == null, shape = RoundedCornerShape(14.dp)) { Text("Guardar esta letra") }
        }
        if (state.busy != null) item { LinearProgressIndicator(Modifier.fillMaxWidth()); Text(state.busy, Modifier.padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite }, color = colors.onSurfaceVariant) }
        if (state.error != null) item { LyricsError(state, onChooseFolder) }
        if (candidate != null) {
            val lines = if (candidate.synced) LrcParser.parse(candidate.content).map { it.text } else candidate.content.lines()
            itemsIndexed(lines) { _, line -> Text(line, Modifier.padding(vertical = 4.dp), fontFamily = FontFamily.Serif, fontSize = 22.sp, lineHeight = 31.sp, color = colors.onSurfaceVariant) }
        } else {
            if (state.results.isNotEmpty()) item { Text("Coincidencias", Modifier.padding(top = 12.dp).semantics { heading() }, fontSize = 20.sp, fontWeight = FontWeight.Medium) }
            if (state.searched && state.results.isEmpty()) item {
                Text("No hay coincidencias", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Text("Prueba con el título sin indicaciones como “Official Video” y revisa el artista.", Modifier.padding(top = 8.dp), fontSize = 14.sp, color = colors.onSurfaceVariant)
            }
            itemsIndexed(state.results) { _, match ->
                Column(Modifier.fillMaxWidth().clickable(enabled = state.busy == null, role = Role.Button, onClickLabel = "Ver letra de ${match.trackName}") { onPreview(match) }.padding(vertical = 12.dp)) {
                    Text(match.trackName, fontSize = 17.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(match.artistName, Modifier.padding(top = 5.dp), fontSize = 14.sp, color = colors.onSurfaceVariant)
                    if (match.albumName.isNotBlank()) Text(match.albumName, Modifier.padding(top = 3.dp), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = colors.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${if (match.synced) "Sincronizada" else "Sin tiempos"}${if (match.durationSeconds > 0) " · ${formatTime((match.durationSeconds * 1000).toLong())}" else ""}", fontSize = 13.sp, color = colors.onSurfaceVariant)
                        Text("Ver letra", fontSize = 14.sp, color = colors.primary)
                    }
                }
                HorizontalDivider(color = colors.outline.copy(alpha = .3f))
            }
        }
    }
}

@Composable
private fun lyricFieldColors() = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)

@Composable
private fun LyricsError(state: LyricsUiState, onChooseFolder: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Text(state.error.orEmpty(), fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
        if (state.needsFolder) TextButton(onChooseFolder) { Text("Elegir carpeta") }
    }
}
