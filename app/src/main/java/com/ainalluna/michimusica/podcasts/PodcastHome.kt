package com.ainalluna.michimusica.podcasts

import android.Manifest
import android.os.Build
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.ainalluna.michimusica.R
import com.ainalluna.michimusica.formatTime
import com.ainalluna.michimusica.searchable
import com.ainalluna.michimusica.ui.HomeIcon
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

enum class PodcastTab(val label: String) { FOLLOWING("Siguiendo"), NEWS("Novedades"), DOWNLOADED("Descargados") }

@Stable
class PodcastNavigation {
    var tab by mutableStateOf(PodcastTab.FOLLOWING)
    var showUrl by mutableStateOf<String?>(null)
    var query by mutableStateOf("")
    var add by mutableStateOf(false)
    var settings by mutableStateOf(false)
    var remove by mutableStateOf<PodcastShow?>(null)
    var detail by mutableStateOf<Pair<PodcastShow, PodcastEpisode>?>(null)
}

@Composable
fun rememberPodcastNavigation(): PodcastNavigation {
    val activity = LocalActivity.current
    var tab by rememberSaveable { mutableStateOf(if (activity?.intent?.getBooleanExtra("podcast_news", false) == true) PodcastTab.NEWS else PodcastTab.FOLLOWING) }
    var url by rememberSaveable { mutableStateOf<String?>(null) }
    val nav = remember { PodcastNavigation().apply { this.tab = tab; showUrl = url } }
    SideEffect { tab = nav.tab; url = nav.showUrl }
    return nav
}

@Composable
fun PodcastHeader(nav: PodcastNavigation, state: PodcastState, refreshing: Boolean, ready: Boolean, error: String,
                  onRefresh: () -> Unit, onDismissError: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.padding(horizontal = 8.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup()) {
            PodcastTab.entries.forEach { item ->
                Column(Modifier.selectable(nav.tab == item, role = Role.Tab, onClick = { nav.tab = item; nav.showUrl = null; nav.query = "" })
                    .heightIn(min = 48.dp).padding(end = 22.dp), verticalArrangement = Arrangement.Center) {
                    Text(item.label, color = if (nav.tab == item) colors.primary else colors.onSurfaceVariant,
                        fontSize = 15.sp, fontWeight = if (nav.tab == item) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton({ nav.add = true }, enabled = ready, modifier = Modifier.weight(1f)) {
                HomeIcon(R.drawable.ic_lists_add, null); Text("Añadir podcast", Modifier.padding(start = 6.dp))
            }
            TextButton(onRefresh, enabled = ready && !refreshing && state.shows.isNotEmpty()) { Text("Actualizar") }
            IconButton({ nav.settings = true }, enabled = ready) { HomeIcon(R.drawable.ic_lyrics_more_vert, "Opciones de podcasts") }
        }
        if (!ready || refreshing) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 6.dp))
            Text(if (!ready) "Leyendo tus podcasts…" else "Comprobando episodios…", color = colors.onSurfaceVariant,
                fontSize = 13.sp, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
        if (error.isNotBlank()) {
            Text(error, color = colors.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            TextButton(onDismissError) { Text("Entendido") }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun dateLabel(time: Long): String = if (time > 0) DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(time)) else "Fecha no indicada"

@Composable
fun rememberPodcastTime(state: PodcastState): Long {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return produceState(System.currentTimeMillis(), state, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                val now = System.currentTimeMillis()
                value = now
                val nextExpiry = state.shows.flatMap { it.episodes }.filter { it.isRecent(now) }
                    .minOfOrNull { PODCAST_NEWS_WINDOW_MS - (now - it.published) + 1 } ?: 60000L
                delay(nextExpiry.coerceIn(1, 60000))
            }
        }
    }.value
}

fun LazyListScope.podcastTransfers(state: PodcastState, progress: Map<String, Int>, folder: Uri,
                                  onRetry: (PodcastDownload) -> Unit, onCancel: (String) -> Unit) {
    val transfers = state.downloads.filter { it.folder == folder.toString() && it.status in listOf("queued", "downloading", "error") }
    items(transfers, key = { "transfer:${it.key}" }) { entry ->
        Column(Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
            Text(entry.title, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(entry.author, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            if (entry.status == "downloading") {
                val percent = progress[entry.key]
                if (percent != null && percent >= 0) LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                else LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
            }
            if (entry.error.isNotBlank()) Text(entry.error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (entry.status == "error") TextButton({ onRetry(entry) }) { Text("Reintentar") }
                else { Text(if (entry.status == "queued") "En cola" else "Descargando…", fontSize = 13.sp); TextButton({ onCancel(entry.key) }) { Text("Cancelar") } }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .25f))
        }
    }
}

fun LazyListScope.podcastItems(nav: PodcastNavigation, state: PodcastState, progress: Map<String, Int>, folder: Uri,
                              onDownload: (PodcastShow, PodcastEpisode) -> Unit, onCancel: (String) -> Unit, onSeen: (String?) -> Unit,
                              now: Long = System.currentTimeMillis()) {
    val selected = state.shows.firstOrNull { it.url == nav.showUrl }
    val following = nav.tab == PodcastTab.FOLLOWING && selected == null
    val news = nav.tab == PodcastTab.NEWS && selected == null
    val recentCount = state.shows.sumOf { show -> show.episodes.count { it.isRecent(now) } }
    item(key = "podcast-title") {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            if (selected != null) TextButton({ nav.showUrl = null; nav.query = "" }, contentPadding = PaddingValues(end = 12.dp)) {
                HomeIcon(R.drawable.ic_lyrics_arrow_back, null); Text("Tus programas", Modifier.padding(start = 8.dp))
            }
            Text(selected?.title ?: if (following) "Tus programas" else "Últimos episodios", fontSize = 22.sp,
                lineHeight = 29.sp, fontWeight = FontWeight.Medium, modifier = Modifier.semantics { heading() })
            Text(if (following) "${state.shows.size} ${if (state.shows.size == 1) "podcast" else "podcasts"} · Solo contenido gratuito"
                else if (news) "$recentCount ${if (recentCount == 1) "episodio" else "episodios"} · Últimos 3 días"
                else if (selected != null && YouTubePodcasts.isYouTube(selected.url)) "${selected.episodes.size} publicaciones recientes · YouTube"
                else "${selected?.episodes?.size ?: state.shows.sumOf { it.episodes.size }} episodios en el RSS público",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
            if (selected != null && selected.checked > 0) Text("Comprobado el ${dateLabel(selected.checked)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            if (selected != null && YouTubePodcasts.isYouTube(selected.url)) Text(
                "YouTube comparte las publicaciones recientes del canal. Se descarga el audio del vídeo público, que puede ser un extracto del programa original.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            if (selected != null && java.net.URI(selected.url).host.endsWith("ivoox.com")) Text(
                "Algunos programas solo ofrecen adelantos fuera de iVoox. Michi comprueba la duración y no guarda esos recortes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            if (selected?.error?.isNotBlank() == true) Text(selected.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            if (selected != null) TextButton({ nav.remove = selected }) { Text("Dejar de seguir") }
            if (!following && (selected?.episodes ?: state.shows.flatMap { it.episodes }).any { it.isUnseenRecent(now) })
                TextButton({ onSeen(selected?.url) }) { Text("Marcar novedades como vistas") }
            if (state.shows.isNotEmpty()) TextField(nav.query, { nav.query = it }, Modifier.fillMaxWidth().padding(top = 12.dp),
                placeholder = { Text(if (following) "Buscar programa" else "Buscar episodio") }, singleLine = true,
                leadingIcon = { HomeIcon(R.drawable.ic_home_search, null) }, shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                trailingIcon = if (nav.query.isNotEmpty()) { { IconButton({ nav.query = "" }) { HomeIcon(R.drawable.ic_home_close, "Borrar búsqueda") } } } else null)
        }
    }
    if (state.shows.isEmpty()) item(key = "podcast-empty") {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Sigue tus podcasts favoritos", style = MaterialTheme.typography.titleLarge)
            Text("Añade su RSS o canal de YouTube para consultar los episodios y descargar su audio.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton({ nav.add = true }) { Text("Añadir podcast") }
        }
    }
    val wanted = searchable(nav.query)
    if (following) {
        val shows = podcastsByLatest(state.shows, now).filter { searchable("${it.title} ${it.author}").contains(wanted) }
        if (shows.isEmpty() && state.shows.isNotEmpty()) item { EmptyFilter { nav.query = "" } }
        items(shows, key = { "show:${it.url}" }) { show ->
            Row(Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "Ver episodios de ${show.title}") {
                nav.showUrl = show.url; nav.query = ""
            }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PodcastArtwork(show.image, Modifier.size(72.dp))
                Column(Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
                    Text(show.title, fontSize = 16.sp, lineHeight = 22.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                    val new = show.episodes.count { it.isUnseenRecent(now) }
                    Text(if (new > 0) "$new ${if (new == 1) "episodio nuevo" else "episodios nuevos"}" else "${show.episodes.size} episodios",
                        color = if (new > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
                    if (show.error.isNotBlank()) Text("No se pudo actualizar", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    else show.episodes.filter { it.published in 1..now }.maxByOrNull { it.published }?.let { Text("Último · ${dateLabel(it.published)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
                }
                HomeIcon(R.drawable.ic_lists_chevron_right, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = .25f))
        }
    } else {
        val entries = (selected?.let { listOf(it) } ?: state.shows).flatMap { s -> s.episodes.map { s to it } }
            .filter { (_, e) -> !news || e.isRecent(now) }
            .filter { (s, e) -> searchable("${s.title} ${e.title}").contains(wanted) }.sortedByDescending { it.second.published }
        if (entries.isEmpty() && state.shows.isNotEmpty()) item {
            if (news && wanted.isBlank()) Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("No hay episodios publicados en los últimos 3 días.")
                Text("Puedes consultar los anteriores en Siguiendo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else EmptyFilter { nav.query = "" }
        }
        items(entries, key = { "episode:${it.first.url}:${it.second.id}" }) { (show, episode) ->
            val download = state.downloads.firstOrNull { it.showUrl == show.url && it.episodeId == episode.id && it.folder == folder.toString() }
            PodcastEpisodeRow(show, episode.copy(isNew = episode.isUnseenRecent(now)), download, download?.let { progress[it.key] },
                onDetails = { nav.detail = show to episode },
                onDownload = { onDownload(show, episode) }, onCancel = { download?.let { onCancel(it.key) } },
                onDownloaded = { nav.tab = PodcastTab.DOWNLOADED; nav.showUrl = null; nav.query = "" })
        }
    }
}

@Composable
private fun EmptyFilter(clear: () -> Unit) {
    Column(Modifier.padding(8.dp)) { Text("No hay coincidencias."); TextButton(clear) { Text("Borrar búsqueda") } }
}

@Composable
private fun PodcastEpisodeRow(show: PodcastShow, episode: PodcastEpisode, download: PodcastDownload?, progress: Int?,
                              onDetails: () -> Unit, onDownload: () -> Unit, onCancel: () -> Unit, onDownloaded: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "Detalles de ${episode.title}", onClick = onDetails), verticalAlignment = Alignment.CenterVertically) {
            PodcastArtwork(episode.image.ifBlank { show.image }, Modifier.size(56.dp))
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(show.title, fontSize = 12.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(episode.title, fontSize = 16.sp, lineHeight = 22.sp, maxLines = 3, overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
            }
        }
        Text(listOfNotNull(if (episode.isNew) "Nuevo" else null, dateLabel(episode.published), if (episode.duration > 0) formatTime(episode.duration) else null).joinToString(" · "),
            fontSize = 12.sp, color = if (episode.isNew) colors.primary else colors.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
        if (download?.status == "downloading") {
            if (progress != null && progress >= 0) LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
            else LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 10.dp))
        }
        if (download?.error?.isNotBlank() == true) Text(download.error, fontSize = 13.sp, color = colors.error, modifier = Modifier.padding(top = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            when (download?.status) {
                "done" -> TextButton(onDownloaded) { HomeIcon(R.drawable.ic_search_check, null); Text("Descargado", Modifier.padding(start = 8.dp)) }
                "unavailable" -> Text("Audio completo no disponible", color = colors.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
                "music" -> Text("Guardado en Música", color = colors.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
                "queued", "downloading" -> {
                    Text(if (download.status == "queued") "En cola" else if (progress != null && progress >= 0) "$progress %" else "Descargando…", fontSize = 13.sp, color = colors.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
                    TextButton(onCancel) { Text("Cancelar") }
                }
                else -> TextButton(onDownload) { HomeIcon(R.drawable.ic_search_download, null); Text(if (download?.status == "error") "Reintentar" else "Descargar", Modifier.padding(start = 8.dp)) }
            }
        }
        HorizontalDivider(color = colors.outline.copy(alpha = .25f))
    }
}

@Composable
fun PodcastDialogs(nav: PodcastNavigation, controller: PodcastController, state: PodcastState, folder: Uri) {
    BackHandler(nav.showUrl != null) { nav.showUrl = null; nav.query = "" }
    var url by rememberSaveable { mutableStateOf("") }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        controller.settings(state.automatic, granted)
    }
    if (nav.add) AlertDialog(onDismissRequest = { if (!controller.adding) { nav.add = false; controller.clearAddError() } },
        title = { Text("Añadir podcast") }, text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Pega el RSS público o el enlace del canal de YouTube. Seguirlo no descarga sus audios. Solo contenido gratuito; sin cuentas ni pagos.")
                OutlinedTextField(url, { url = it; controller.clearAddError() }, label = { Text("RSS o canal de YouTube") },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                    enabled = !controller.adding, maxLines = 4, isError = controller.addError.isNotBlank())
                if (controller.adding) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (controller.addError.isNotBlank()) Text(controller.addError, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        }, confirmButton = { TextButton({ controller.follow(url) { nav.add = false; url = ""; nav.tab = PodcastTab.FOLLOWING; nav.showUrl = null; nav.query = "" } }, enabled = url.isNotBlank() && !controller.adding) { Text(if (controller.adding) "Comprobando…" else "Seguir") } },
        dismissButton = { TextButton({ nav.add = false; controller.clearAddError() }, enabled = !controller.adding) { Text("Cancelar") } })
    nav.remove?.let { show -> AlertDialog(onDismissRequest = { nav.remove = null }, title = { Text("Dejar de seguir") },
        text = { Text("Dejarás de recibir novedades de ${show.title}. Los episodios descargados y sus posiciones se conservan.") },
        confirmButton = { TextButton({ controller.unfollow(show.url); nav.remove = null; nav.showUrl = null }) { Text("Dejar de seguir") } },
        dismissButton = { TextButton({ nav.remove = null }) { Text("Cancelar") } }) }
    nav.detail?.let { (show, episode) -> AlertDialog(onDismissRequest = { nav.detail = null }, title = { Text(episode.title) },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(show.title, color = MaterialTheme.colorScheme.primary)
            Text(dateLabel(episode.published), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(episode.description.ifBlank { "Este episodio no tiene descripción." })
        } }, confirmButton = { TextButton({ nav.detail = null }) { Text("Cerrar") } }) }
    if (nav.settings) AlertDialog(onDismissRequest = { nav.settings = false }, title = { Text("Ajustes de podcasts") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Comprobar novedades automáticamente", Modifier.weight(1f))
                Switch(state.automatic, { controller.settings(it, if (it) state.notifications else false) }, modifier = Modifier.semantics { contentDescription = "Comprobar novedades automáticamente" })
            }
            Text("Cada 6 horas aproximadamente, cuando Android permita la conexión. Puedes actualizar manualmente cuando quieras.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Avisar de episodios nuevos", Modifier.weight(1f))
                Switch(state.notifications && NotificationManagerCompat.from(controller.context).areNotificationsEnabled(), { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else controller.settings(state.automatic, enabled)
                }, enabled = state.automatic, modifier = Modifier.semantics { contentDescription = "Avisar de episodios nuevos" })
            }
            Text("Las descargas son siempre manuales. Los programas y el progreso se guardan en este dispositivo.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }, confirmButton = { TextButton({ nav.settings = false }) { Text("Listo") } })
}
