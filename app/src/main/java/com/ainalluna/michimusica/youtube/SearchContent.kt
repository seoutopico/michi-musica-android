package com.ainalluna.michimusica.youtube

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.ainalluna.michimusica.ui.HomeCat
import com.ainalluna.michimusica.ui.HomeIcon

/** Real screen and previews share the same layout; all work is delegated to the controller. */
@Composable
fun SearchContent(state: SearchUiState, hasFolder: Boolean, onQuery: (String) -> Unit,
                  onSearch: () -> Unit, onPreview: (YouTubeResult) -> Unit,
                  onDownload: (YouTubeResult) -> Unit, onChooseFolder: () -> Unit,
                  videoPlayer: (@Composable () -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current
    val submit = { focus.clearFocus(); onSearch() }
    Column(Modifier.fillMaxSize()) {
    videoPlayer?.invoke()
    LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)) {
        item(key = "heading") {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).clearAndSetSemantics { contentDescription = "Michi Música" }, verticalAlignment = Alignment.CenterVertically) {
                    HomeCat(Modifier.size(26.dp))
                    Text("michi", Modifier.padding(start = 10.dp), fontSize = 23.sp, letterSpacing = 1.sp, color = colors.primary)
                }
                Text("YOUTUBE", color = colors.onSurfaceVariant, fontSize = 11.sp, letterSpacing = 1.5.sp)
            }
            Text("Buscar", Modifier.padding(top = 12.dp, bottom = 20.dp).semantics { heading() },
                fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold)
        }
        item(key = "input") {
            TextField(state.query, onQuery, Modifier.fillMaxWidth(),
                placeholder = { Text("Canción, artista o enlace", fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { HomeIcon(R.drawable.ic_home_search, null) },
                trailingIcon = if (state.query.isNotEmpty()) { { IconButton({ onQuery("") }) { HomeIcon(R.drawable.ic_home_close, "Borrar búsqueda") } } } else null,
                singleLine = true, shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { submit() }),
                colors = TextFieldDefaults.colors(focusedContainerColor = colors.surfaceContainerHigh, unfocusedContainerColor = colors.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
            Button(submit, Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 48.dp),
                enabled = !state.busy && state.query.isNotBlank(), shape = RoundedCornerShape(14.dp)) {
                Text(if (state.searching) "Buscando…" else "Buscar en YouTube", fontSize = 16.sp)
            }
        }
        if (!hasFolder) item(key = "folder") {
            Column(Modifier.padding(top = 16.dp)) {
                Text("Elige dónde guardar tu música", fontWeight = FontWeight.Medium)
                TextButton(onChooseFolder) { Text("Elegir carpeta") }
            }
        }
        when {
            state.searching -> item(key = "loading") {
                Column(Modifier.padding(top = 28.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                    Text("Buscando en YouTube…", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("Puede tardar unos segundos.", Modifier.padding(top = 6.dp), color = colors.onSurfaceVariant, fontSize = 14.sp)
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 18.dp))
                }
            }
            state.searchError != null -> item(key = "error") {
                Column(Modifier.padding(top = 28.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                    Text("No se pudo buscar", color = colors.error, fontWeight = FontWeight.Medium, fontSize = 20.sp)
                    Text(state.searchError, Modifier.padding(top = 8.dp), color = colors.onSurfaceVariant)
                    TextButton(submit, enabled = !state.busy && state.query.isNotBlank()) { Text("Volver a intentar") }
                }
            }
            state.submitted == null -> item(key = "welcome") {
                Column(Modifier.padding(top = 40.dp, bottom = 16.dp)) {
                    Text("Dale imagen\na tu música.", fontFamily = FontFamily.Serif, fontSize = 28.sp, lineHeight = 35.sp)
                    Text("Busca y reproduce vídeos aquí, también a pantalla completa. Si quieres conservar el audio, puedes guardar el MP3.",
                        Modifier.padding(top = 14.dp), fontSize = 15.sp, lineHeight = 23.sp, color = colors.onSurfaceVariant)
                    HorizontalDivider(Modifier.padding(top = 28.dp, bottom = 20.dp), color = colors.outline.copy(alpha = .35f))
                    Text("¿Ya tienes el enlace?", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Pégalo arriba para ir directamente al vídeo.", Modifier.padding(top = 6.dp), fontSize = 14.sp, lineHeight = 21.sp, color = colors.onSurfaceVariant)
                }
            }
            state.results.isEmpty() -> item(key = "empty") {
                Column(Modifier.padding(top = 32.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                    Text("Sin resultados", fontFamily = FontFamily.Serif, fontSize = 28.sp)
                    Text("Prueba con el título y el artista, o pega el enlace de un vídeo.", Modifier.padding(top = 12.dp), color = colors.onSurfaceVariant)
                }
            }
            else -> {
                item(key = "results-heading") {
                    Row(Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Resultados", Modifier.semantics { heading() }, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                            Text(state.submitted.orEmpty(), Modifier.padding(top = 4.dp), fontSize = 13.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("${state.results.size} ${if (state.results.size == 1) "vídeo" else "vídeos"}", fontSize = 13.sp, color = colors.onSurfaceVariant)
                    }
                }
                items(state.results, key = YouTubeResult::id) { result ->
                    SearchResultRow(result, state, hasFolder, { focus.clearFocus(); onPreview(result) }, { focus.clearFocus(); onDownload(result) })
                }
            }
        }
    }
    }
}

@Composable
private fun SearchResultRow(result: YouTubeResult, state: SearchUiState, hasFolder: Boolean, onPreview: () -> Unit, onDownload: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val active = state.previewId == result.id
    val downloading = state.downloadingId == result.id
    val saved = result.id in state.savedIds
    Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            SearchThumbnail(result, Modifier.width(96.dp).height(72.dp))
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(result.title, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(result.channel, Modifier.padding(top = 5.dp), fontSize = 13.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (active && state.previewPlaying) "Reproduciendo vídeo" else if (result.durationSeconds > 0) youtubeTime(result.durationSeconds) else "YouTube",
                    Modifier.padding(top = 3.dp), fontSize = 12.sp, color = if (active && state.previewPlaying) colors.primary else colors.onSurfaceVariant)
            }
        }
        FlowRow(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onPreview, enabled = !state.busy && !active, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp), modifier = Modifier.heightIn(min = 48.dp)) {
                HomeIcon(R.drawable.ic_home_play_arrow, null)
                Text(if (active) "Vídeo abierto" else "Ver vídeo", Modifier.padding(start = 6.dp), fontSize = 14.sp)
            }
            TextButton(onDownload, enabled = !state.busy && hasFolder && !saved,
                modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)) {
                HomeIcon(if (saved) R.drawable.ic_search_check else R.drawable.ic_search_download, null)
                Text(if (saved) "Guardado" else if (downloading) "Guardando…" else "Guardar MP3", Modifier.padding(start = 6.dp), fontSize = 14.sp)
            }
        }
        if (downloading) Column(Modifier.padding(bottom = 10.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
            Text(if (state.progress < 100) "Descargando · ${state.progress}%" else "Preparando el MP3…", fontSize = 13.sp, color = colors.primary)
            LinearProgressIndicator(progress = { state.progress / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
        if (saved) Text("Ya está en tu biblioteca", Modifier.padding(bottom = 10.dp).semantics { liveRegion = LiveRegionMode.Polite }, fontSize = 13.sp, color = colors.onSurfaceVariant)
        state.rowError?.takeIf { it.first == result.id }?.let {
            Text(it.second, Modifier.padding(bottom = 12.dp).semantics { liveRegion = LiveRegionMode.Polite }, fontSize = 14.sp, lineHeight = 20.sp, color = colors.error)
        }
        HorizontalDivider(color = colors.outline.copy(alpha = .25f))
    }
}

private fun youtubeTime(seconds: Long) = "%d:%02d".format(seconds / 60, seconds % 60)
