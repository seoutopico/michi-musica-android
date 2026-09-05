package com.ainalluna.michimusica

import com.ainalluna.michimusica.security.readBoundedText

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.ainalluna.michimusica.library.MarkdownPlaylist
import com.ainalluna.michimusica.library.MusicFolderReader
import com.ainalluna.michimusica.library.Song
import com.ainalluna.michimusica.library.deleteSongFile
import com.ainalluna.michimusica.library.removedSongIndices
import com.ainalluna.michimusica.ui.SongDeletionDialog
import com.ainalluna.michimusica.lyrics.LyricsScreen
import com.ainalluna.michimusica.playback.PlaybackService
import com.ainalluna.michimusica.playback.canAdvanceTrack
import com.ainalluna.michimusica.playback.advanceTrack
import com.ainalluna.michimusica.ui.MichiSkin
import com.ainalluna.michimusica.ui.MichiTheme
import com.ainalluna.michimusica.ui.MichiArtwork
import com.ainalluna.michimusica.ui.MichiFace
import com.ainalluna.michimusica.ui.MichiIcon
import com.ainalluna.michimusica.ui.MichiIconType
import com.ainalluna.michimusica.ui.LibraryHome
import com.ainalluna.michimusica.ui.LibraryNotice
import com.ainalluna.michimusica.ui.HomeMiniPlayer
import com.ainalluna.michimusica.ui.HomeNavigation
import com.ainalluna.michimusica.ui.ListsHome
import com.ainalluna.michimusica.ui.PlaylistCollection
import com.ainalluna.michimusica.ui.PlayerHome
import com.ainalluna.michimusica.youtube.YouTubeScreen
import com.ainalluna.michimusica.youtube.rememberYouTubeSearchController
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import java.text.Normalizer

private const val PREFS = "michi_preferences"
private const val PREF_FOLDER = "music_folder_uri"
private const val PREF_PLAYLIST = "playlist_uri"
private const val PREF_PLAYLISTS = "playlist_uris"
private const val PREF_SKIN = "skin"
private const val PREF_LAST_SONG = "last_song_id"
private const val PREF_LAST_POSITION = "last_song_position"
private const val PREF_LAST_PLAYLIST = "last_playlist_uri"

private enum class Destination(val label: String, val icon: MichiIconType) {
    MUSIC("Biblioteca", MichiIconType.MUSIC),
    SEARCH("Buscar", MichiIconType.SEARCH),
    PLAYLISTS("Listas", MichiIconType.PLAYLIST),
}

private data class PlayerState(
    val index: Int = 0, val playing: Boolean = false, val shuffle: Boolean = false,
    val repeatOne: Boolean = false, val position: Long = 0, val duration: Long = 0,
    val engaged: Boolean = false, val buffering: Boolean = false, val failed: Boolean = false,
)

private data class SavedPlaylist(val uri: Uri, val name: String, val songCount: Int, val missingCount: Int, val artworkSongs: List<Song> = emptyList())
private data class ResumeListening(val song: Song, val positionMs: Long, val sourceUri: Uri?)
private data class PendingRestore(val songId: String, val positionMs: Long, val play: Boolean, val advance: Boolean, val sourceUri: Uri?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MichiApp() }
    }
}

@Composable
private fun MichiApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var skinName by rememberSaveable { mutableStateOf(preferences.getString(PREF_SKIN, "midnight") ?: "midnight") }
    val skin = if (skinName == "midnight") MichiSkin.MIDNIGHT else MichiSkin.ROSE
    SideEffect {
        val style = if (skin == MichiSkin.MIDNIGHT) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        (context as? ComponentActivity)?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
    val library = remember { mutableStateListOf<Song>() }
    val songs = remember { mutableStateListOf<Song>() }
    var folderUri by remember { mutableStateOf(preferences.getString(PREF_FOLDER, null)?.toUri()) }
    var playlistUri by remember { mutableStateOf(preferences.getString(PREF_PLAYLIST, null)?.toUri()) }
    var folderRevision by remember { mutableIntStateOf(0) }
    var playlistRevision by remember { mutableIntStateOf(0) }
    var playlistName by remember { mutableStateOf<String?>(null) }
    val savedPlaylists = remember { mutableStateListOf<SavedPlaylist>() }
    var listsLoading by remember { mutableStateOf(true) }
    var pendingResume by remember { mutableStateOf<PendingRestore?>(null) }
    var restoreRevision by remember { mutableIntStateOf(0) }
    var loadedSourceUri by remember { mutableStateOf<Uri?>(null) }
    var controllerQueueRevision by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var libraryLoaded by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<LibraryNotice?>(null) }
    var controller by remember { mutableStateOf<MediaController?>(null) }
    val deletionScope = rememberCoroutineScope()
    var songToDelete by remember { mutableStateOf<Song?>(null) }
    var deletingSong by remember { mutableStateOf(false) }
    var deletionError by remember { mutableStateOf<String?>(null) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            preferences.edit { putString(PREF_FOLDER, uri.toString()) }
            if (folderUri != uri) { playlistUri = null; playlistName = null; preferences.edit { remove(PREF_PLAYLIST) } }
            folderUri = uri; folderRevision++
        }.onFailure { notice = LibraryNotice("Android no pudo conservar el acceso. Vuelve a elegir la carpeta.", needsFolder = true) }
    }
    val playlistPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val stored = preferences.getStringSet(PREF_PLAYLISTS, emptySet()).orEmpty().toMutableSet().apply { add(uri.toString()) }
            playlistUri = uri; playlistRevision++
            preferences.edit { putString(PREF_PLAYLIST, uri.toString()); putStringSet(PREF_PLAYLISTS, stored) }
        }.onFailure { notice = LibraryNotice("No pude conservar el acceso a esa lista.") }
    }

    DisposableEffect(Unit) {
        val future: ListenableFuture<MediaController> = MediaController.Builder(
            context, SessionToken(context, ComponentName(context, PlaybackService::class.java)),
        ).buildAsync()
        future.addListener({ runCatching { future.get() }.onSuccess { controller = it } }, ContextCompat.getMainExecutor(context))
        onDispose { controller = null; MediaController.releaseFuture(future) }
    }

    DisposableEffect(controller) {
        val listener = object : Player.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) { controllerQueueRevision++ }
        }
        controller?.addListener(listener)
        onDispose { controller?.removeListener(listener) }
    }

    LaunchedEffect(folderUri, folderRevision) {
        val uri = folderUri ?: return@LaunchedEffect
        loading = true
        notice = null
        runCatching { withContext(Dispatchers.IO) { MusicFolderReader.read(context, uri) } }.onSuccess { found ->
            library.clear(); library.addAll(found)
            if (found.isEmpty()) {
                playlistUri = null; playlistName = null; pendingResume = null
                preferences.edit { remove(PREF_PLAYLIST) }
            }
            if (playlistUri == null) { songs.clear(); songs.addAll(found); loadedSourceUri = null }
            libraryLoaded = true
            val previousId = preferences.getString(PREF_LAST_SONG, null)
            if (previousId != null && found.none { it.id == previousId }) {
                preferences.edit { remove(PREF_LAST_SONG); remove(PREF_LAST_POSITION); remove(PREF_LAST_PLAYLIST) }
                if (found.isNotEmpty()) notice = LibraryNotice("La última canción ya no está en esta carpeta. Elige otra para escuchar.")
            }
        }.onFailure {
            if (it is CancellationException) throw it
            notice = LibraryNotice("Ya no puedo leer esa carpeta. Elígela de nuevo.", needsFolder = true)
        }
        loading = false
    }

    LaunchedEffect(library.toList(), playlistUri, playlistRevision) {
        val uri = playlistUri ?: return@LaunchedEffect
        if (library.isEmpty()) return@LaunchedEffect
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readBoundedText(1_000_000) } ?: error("No se pudo abrir")
            require(text.length <= 300_000); MarkdownPlaylist.resolve(text, library)
        }.onSuccess { result ->
            if (result.entries.isEmpty() || result.songs.isEmpty()) {
                songs.clear(); songs.addAll(library); playlistUri = null; playlistName = null; preferences.edit { remove(PREF_PLAYLIST) }
                loadedSourceUri = null
                pendingResume = pendingResume?.copy(sourceUri = null)
                notice = LibraryNotice("La lista no contiene canciones disponibles. Se muestra toda tu música.")
            } else {
                val restoring = pendingResume
                if (restoring != null && result.songs.none { it.id == restoring.songId }) {
                    songs.clear(); songs.addAll(library); playlistUri = null; playlistName = null; loadedSourceUri = null
                    preferences.edit { remove(PREF_PLAYLIST) }
                    pendingResume = restoring.copy(sourceUri = null)
                    notice = LibraryNotice("La canción ya no está en esa lista. Puedes retomarla desde Biblioteca.")
                } else {
                    songs.clear(); songs.addAll(result.songs); playlistName = result.title.ifBlank { "Playlist Markdown" }; loadedSourceUri = uri
                }
            }
        }.onFailure {
            songs.clear(); songs.addAll(library); playlistUri = null; playlistName = null; preferences.edit { remove(PREF_PLAYLIST) }
            loadedSourceUri = null
            pendingResume = pendingResume?.copy(sourceUri = null)
            notice = LibraryNotice("No pude abrir esa lista. Se muestra toda tu música.")
        }
    }

    LaunchedEffect(library.toList(), playlistRevision) {
        if (library.isEmpty()) { savedPlaylists.clear(); listsLoading = false; return@LaunchedEffect }
        listsLoading = true
        val snapshot = library.toList()
        val storedUris = preferences.getStringSet(PREF_PLAYLISTS, emptySet()).orEmpty()
            .toMutableSet().apply { playlistUri?.let { add(it.toString()) } }
        try {
        val summaries = withContext(Dispatchers.IO) { storedUris.mapNotNull { raw ->
            runCatching {
                val uri = raw.toUri()
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readBoundedText(1_000_000) } ?: return@runCatching null
                val result = MarkdownPlaylist.resolve(text.take(300_000), snapshot)
                if (result.songs.isEmpty()) return@runCatching null
                SavedPlaylist(
                    uri = uri,
                    name = result.title.ifBlank { DocumentFile.fromSingleUri(context, uri)?.name?.substringBeforeLast('.') ?: "Playlist Markdown" },
                    songCount = result.songs.size,
                    missingCount = result.missing.size,
                    artworkSongs = result.songs.take(4),
                )
            }.getOrNull()
        }.sortedBy { it.name.lowercase() } }
        savedPlaylists.clear(); savedPlaylists.addAll(summaries)
        } finally { listsLoading = false }
    }

    LaunchedEffect(controller, songs.toList(), loading, libraryLoaded, loadedSourceUri, playlistUri) {
        if (!canSynchronizeLibrary(libraryLoaded, loading, loadedSourceUri == playlistUri)) return@LaunchedEffect
        val player = controller ?: return@LaunchedEffect
        val ids = songs.map(Song::id)
        val current = List(player.mediaItemCount) { player.getMediaItemAt(it).mediaId }
        if (!playlistNeedsUpdate(current, ids)) return@LaunchedEffect
        val retained = ids.indexOf(player.currentMediaItem?.mediaId)
        val position = if (retained >= 0) player.currentPosition.coerceAtLeast(0) else 0
        val play = player.playWhenReady
        if (songs.isEmpty()) player.clearMediaItems() else {
            player.setMediaItems(songs.map(Song::asMediaItem), retained.coerceAtLeast(0), position); player.prepare(); player.playWhenReady = play
        }
    }


    LaunchedEffect(controller, songs.toList(), pendingResume, loadedSourceUri, controllerQueueRevision) {
        val request = pendingResume ?: return@LaunchedEffect
        val player = controller ?: return@LaunchedEffect
        if (!restoreQueueReady(request.sourceUri == loadedSourceUri, songs.map(Song::id), List(player.mediaItemCount) { player.getMediaItemAt(it).mediaId })) return@LaunchedEffect
        val index = songs.indexOfFirst { it.id == request.songId }
        if (index >= 0) {
            player.seekTo(index, request.positionMs.coerceAtLeast(0))
            if (request.advance) player.advanceTrack()
            player.playWhenReady = request.play
            restoreRevision++
            pendingResume = null
        }
    }

    val resume = remember(library.toList(), preferences.getString(PREF_LAST_SONG, null), preferences.getLong(PREF_LAST_POSITION, 0L)) {
        val songId = preferences.getString(PREF_LAST_SONG, null)
        val position = preferences.getLong(PREF_LAST_POSITION, 0L)
        val song = library.firstOrNull { it.id == songId }
        if (song != null) {
            ResumeListening(song, resumePosition(position, song.durationMs), preferences.getString(PREF_LAST_PLAYLIST, null)?.toUri())
        } else null
    }

    MichiTheme(skin) {
        MichiRoot(
            songs, library, savedPlaylists, controller, folderUri, playlistUri, skin, playlistName, resume,
            loading = loading, listsLoading = listsLoading, notice = notice, artworkRevision = folderRevision, restoreRevision = restoreRevision,
            onDismissNotice = { notice = null },
            onDeleteSong = { songToDelete = it; deletionError = null },
            onChooseFolder = { folderPicker.launch(folderUri) },
            onChoosePlaylist = { playlistPicker.launch(arrayOf("text/markdown", "text/plain")) },
            onAllMusic = {
                playlistUri = null; playlistName = null; preferences.edit { remove(PREF_PLAYLIST) }
                loadedSourceUri = null
                songs.clear(); songs.addAll(library)
            },
            onSelectPlaylist = { uri ->
                playlistUri = uri; playlistRevision++; preferences.edit { putString(PREF_PLAYLIST, uri.toString()) }
            },
            onResume = { saved, play, advance ->
                pendingResume = PendingRestore(saved.song.id, saved.positionMs, play, advance, saved.sourceUri)
                if (saved.sourceUri == null) {
                    playlistUri = null; playlistName = null; preferences.edit { remove(PREF_PLAYLIST) }
                    loadedSourceUri = null
                    songs.clear(); songs.addAll(library)
                } else {
                    playlistUri = saved.sourceUri; playlistRevision++; preferences.edit { putString(PREF_PLAYLIST, saved.sourceUri.toString()) }
                }
            },
            onRefresh = { folderRevision++ },
            onDownloaded = { folderRevision++ },
            onSkinChange = {
                skinName = if (it == MichiSkin.ROSE) "rose" else "midnight"; preferences.edit { putString(PREF_SKIN, skinName) }
            },
        )
        songToDelete?.let { target ->
            SongDeletionDialog(target, deletingSong, deletionError,
                onDismiss = { if (!deletingSong) { songToDelete = null; deletionError = null } },
                onConfirm = {
                    val source = folderUri
                    if (!deletingSong && source != null) {
                        deletingSong = true; deletionError = null
                        deletionScope.launch {
                            // Once confirmed, reconcile the queue/preferences even if the screen is disposed.
                            withContext(NonCancellable) {
                                runCatching {
                                    require(library.any { it.id == target.id && it.uri == target.uri }) { "La canción ya no está en esta biblioteca." }
                                    withContext(Dispatchers.IO) { deleteSongFile(context, source, target.uri) }
                                }.onSuccess {
                                    runCatching { controller?.let { player ->
                                        if (player.currentMediaItem?.mediaId == target.id) player.pause()
                                        removedSongIndices(List(player.mediaItemCount) { player.getMediaItemAt(it).mediaId }, target.id)
                                            .forEach { player.removeMediaItem(it) }
                                    } }.onFailure { notice = LibraryNotice("El archivo se ha borrado, pero no pude actualizar el reproductor. Vuelve a abrir Michi.") }
                                    if (pendingResume?.songId == target.id) pendingResume = null
                                    if (preferences.getString(PREF_LAST_SONG, null) == target.id) preferences.edit {
                                        remove(PREF_LAST_SONG); remove(PREF_LAST_POSITION); remove(PREF_LAST_PLAYLIST)
                                    }
                                    library.removeAll { it.id == target.id }; songs.removeAll { it.id == target.id }
                                    playlistRevision++
                                    songToDelete = null
                                }.onFailure {
                                    deletionError = if (it is SecurityException) "Android ha retirado el permiso. Vuelve a elegir la carpeta en Ajustes."
                                        else it.message ?: "No se pudo borrar la canción. Inténtalo de nuevo."
                                }
                                deletingSong = false
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun MichiRoot(
    songs: List<Song>, library: List<Song>, savedPlaylists: List<SavedPlaylist>, player: Player?, folderUri: Uri?, playlistUri: Uri?, skin: MichiSkin, playlistName: String?, resume: ResumeListening?,
    onChooseFolder: () -> Unit, onChoosePlaylist: () -> Unit, onAllMusic: () -> Unit, onRefresh: () -> Unit,
    onSelectPlaylist: (Uri) -> Unit, onResume: (ResumeListening, Boolean, Boolean) -> Unit,
    onDownloaded: (String) -> Unit, onSkinChange: (MichiSkin) -> Unit,
    loading: Boolean, listsLoading: Boolean, notice: LibraryNotice?, artworkRevision: Int, restoreRevision: Int, onDismissNotice: () -> Unit,
    onDeleteSong: (Song) -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf(Destination.MUSIC) }
    var nowPlaying by rememberSaveable { mutableStateOf(false) }
    var lyricsOpen by rememberSaveable { mutableStateOf(false) }
    var state by remember { mutableStateOf(PlayerState()) }
    val searchController = rememberYouTubeSearchController(folderUri, { player?.pause() }, onDownloaded)
    LaunchedEffect(destination, nowPlaying, state.playing) {
        if (destination != Destination.SEARCH || nowPlaying || state.playing) searchController.pausePreview()
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    DisposableEffect(player, restoreRevision) {
        if (player == null) return@DisposableEffect onDispose { }
        fun refresh() { state = state.copy(index = player.currentMediaItemIndex.coerceAtLeast(0), position = player.currentPosition.coerceAtLeast(0), playing = player.isPlaying, buffering = player.playbackState == Player.STATE_BUFFERING, failed = player.playerError != null, shuffle = player.shuffleModeEnabled, repeatOne = player.repeatMode == Player.REPEAT_MODE_ONE, duration = player.duration.takeIf { it > 0 } ?: 0, engaged = restoreRevision > 0 || hasActiveListeningSession(state.engaged, player.isPlaying, player.currentPosition)) }
        val listener = object : Player.Listener { override fun onEvents(player: Player, events: Player.Events) = refresh() }
        player.addListener(listener); refresh(); onDispose { player.removeListener(listener) }
    }
    LaunchedEffect(player) { while (true) { state = state.copy(position = player?.currentPosition?.coerceAtLeast(0) ?: 0, duration = player?.duration?.takeIf { it > 0 } ?: 0); delay(500) } }
    val song = if (state.engaged) library.firstOrNull { it.id == player?.currentMediaItem?.mediaId } else null
    val visibleSong = song ?: resume?.song
    val visiblePosition = if (song != null) state.position else resume?.positionMs ?: 0L
    BackHandler(nowPlaying || lyricsOpen) { if (lyricsOpen) lyricsOpen = false else nowPlaying = false }

    LaunchedEffect(song?.id, state.position / 5_000L, state.engaged, playlistUri) {
        if (song != null && state.engaged && player?.currentMediaItem?.mediaId == song.id) {
            preferences.edit {
                putString(PREF_LAST_SONG, song.id)
                putLong(PREF_LAST_POSITION, player.currentPosition.coerceAtLeast(0))
                if (playlistUri == null) remove(PREF_LAST_PLAYLIST) else putString(PREF_LAST_PLAYLIST, playlistUri.toString())
            }
        }
    }

    if (folderUri == null) { OnboardingScreen(onChooseFolder); return }
    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                if (visibleSong != null && (!nowPlaying || lyricsOpen) && notice?.needsFolder != true) HomeMiniPlayer(
                    visibleSong, song != null && state.playing, visiblePosition, if (song != null && state.duration > 0) state.duration else visibleSong.durationMs,
                    ready = player != null && !loading, nextEnabled = if (song != null) player?.canAdvanceTrack() == true else songs.indexOfFirst { it.id == visibleSong.id }.let { it >= 0 && songs.size > 1 && (state.shuffle || it < songs.lastIndex) },
                    artworkRevision = artworkRevision,
                    onToggle = { if (song == null && resume != null) onResume(resume, true, false) else if (player?.isPlaying == true) player.pause() else player?.play() },
                    onNext = { if (song == null && resume != null) onResume(resume, false, true) else player?.advanceTrack() },
                    onOpen = { if (song == null && resume != null) onResume(resume, false, false); lyricsOpen = false; nowPlaying = true },
                )
                MichiNavigationBar(destination) {
                    destination = it
                    nowPlaying = false
                    lyricsOpen = false
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                lyricsOpen && song != null -> LyricsScreen(song, folderUri, state.position, onBack = { lyricsOpen = false }, onSeek = { player?.seekTo(it) }, onChooseFolder = onChooseFolder)
                nowPlaying && visibleSong != null -> NowPlayingScreen(visibleSong, if (song != null) state else state.copy(position = visiblePosition, duration = visibleSong.durationMs), if (song != null) player else null, { nowPlaying = false }, { lyricsOpen = true }, sourceName = playlistName, artworkRevision = artworkRevision)
                else -> when (destination) {
                    Destination.MUSIC -> LibraryScreen(songs, state, player, skin, playlistName, resume, { state = state.copy(engaged = true) }, onChooseFolder, onRefresh, onSkinChange,
                        loading, notice, artworkRevision, onAllMusic, onDismissNotice, onDeleteSong)
                    Destination.SEARCH -> YouTubeScreen(searchController, onChooseFolder)
                    Destination.PLAYLISTS -> PlaylistsScreen(
                        library.size, savedPlaylists, playlistUri,
                        onAllMusic = { onAllMusic(); destination = Destination.MUSIC },
                        onChoosePlaylist = onChoosePlaylist,
                        onSelectPlaylist = { onSelectPlaylist(it); destination = Destination.MUSIC },
                        loading = loading || listsLoading, artworkRevision = artworkRevision,
                    )
                }
            }
        }
    }
}

@Composable
private fun MichiNavigationBar(destination: Destination, onSelect: (Destination) -> Unit) {
    HomeNavigation(destination.ordinal) { onSelect(Destination.entries[it]) }
}

@Composable
private fun EmptyLyricsScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Letra", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Reproduce una canción para buscar o leer su letra.", Modifier.padding(top = 10.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun OnboardingScreen(onChooseFolder: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CatMark(Modifier.fillMaxWidth().height(108.dp))
        Text("Hola, soy Michi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Tu música se queda en este dispositivo", Modifier.padding(top = 10.dp, bottom = 28.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onChooseFolder, Modifier.fillMaxWidth().height(54.dp)) { Text("Elegir carpeta") }
        Surface(Modifier.fillMaxWidth().padding(top = 16.dp), color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(18.dp)) {
            Text("▣  Tu música es privada. No se sube ni se comparte.", Modifier.padding(16.dp), textAlign = TextAlign.Center)
        }
        Text("MP3 · WAV · OGG · M4A · AAC · FLAC · OPUS", Modifier.padding(top = 20.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun LibraryScreen(
    songs: List<Song>, state: PlayerState, player: Player?, skin: MichiSkin, playlistName: String?,
    resume: ResumeListening?, onEngage: () -> Unit,
    onChooseFolder: () -> Unit, onRefresh: () -> Unit,
    onSkinChange: (MichiSkin) -> Unit,
    loading: Boolean = false, notice: LibraryNotice? = null, artworkRevision: Int = 0,
    onAllMusic: () -> Unit = {}, onDismissNotice: () -> Unit = {},
    onDeleteSong: ((Song) -> Unit)? = null,
) {
    var settings by remember { mutableStateOf(false) }
    LibraryHome(
        songs = songs,
        selectedSongId = if (state.engaged) player?.currentMediaItem?.mediaId else resume?.song?.id,
        playing = state.playing, ready = player != null && notice?.needsFolder != true,
        loading = loading, notice = notice, sourceName = playlistName, artworkRevision = artworkRevision,
        onSelect = { song ->
            val index = songs.indexOfFirst { it.id == song.id }
            if (index >= 0 && player != null) { onEngage(); player.seekToDefaultPosition(index); player.play() }
        },
        onShuffle = {
            if (songs.isNotEmpty() && player != null) {
                player.shuffleModeEnabled = true
                val index = player.currentTimeline.getFirstWindowIndex(true)
                if (index >= 0) { onEngage(); player.seekToDefaultPosition(index); player.play() }
            }
        },
        onSettings = { settings = true }, onChooseFolder = onChooseFolder,
        onAllMusic = onAllMusic, onDismissNotice = onDismissNotice,
        onDelete = onDeleteSong,
    )
    if (settings) AlertDialog({ settings = false }, title = { Text("Michi Música") }, text = { Column {
        Text("Apariencia", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeButton("Rosa", skin == MichiSkin.ROSE, Modifier.weight(1f)) { onSkinChange(MichiSkin.ROSE) }
            ThemeButton("Medianoche", skin == MichiSkin.MIDNIGHT, Modifier.weight(1f)) { onSkinChange(MichiSkin.MIDNIGHT) }
        }
        TextButton({ settings = false; onChooseFolder() }, Modifier.padding(top = 10.dp)) { MichiIcon(MichiIconType.FOLDER, "Carpeta", Modifier.size(20.dp)); Text("  Cambiar carpeta") }
        TextButton({ settings = false; onRefresh() }) { MichiIcon(MichiIconType.REFRESH, "Releer", Modifier.size(20.dp)); Text("  Releer música") }
    } }, confirmButton = { TextButton({ settings = false }) { Text("Listo") } })
}

@Composable
private fun PlaylistsScreen(
    libraryCount: Int, playlists: List<SavedPlaylist>, activeUri: Uri?,
    onAllMusic: () -> Unit, onChoosePlaylist: () -> Unit, onSelectPlaylist: (Uri) -> Unit,
    loading: Boolean = false, artworkRevision: Int = 0,
) {
    ListsHome(libraryCount, playlists.map { PlaylistCollection(it.uri.toString(), it.name, it.songCount, it.missingCount, it.artworkSongs) },
        activeUri?.toString(), loading, artworkRevision, onAllMusic, onChoosePlaylist, { onSelectPlaylist(it.toUri()) })
}

@Composable private fun ThemeButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (selected) Button(onClick, modifier) { Text(text) } else OutlinedButton(onClick, modifier) { Text(text) }
}

@Composable
private fun NowPlayingScreen(song: Song, state: PlayerState, player: Player?, onBack: () -> Unit, onLyrics: () -> Unit,
                             modifier: Modifier = Modifier, sourceName: String? = null, artworkRevision: Int = 0) {
    val ready = player != null && player.mediaItemCount > 0
    PlayerHome(song, state.playing, state.position, state.duration, state.shuffle, state.repeatOne,
        ready = ready, canPrevious = player?.isCommandAvailable(Player.COMMAND_SEEK_TO_PREVIOUS) == true,
        canNext = player?.canAdvanceTrack() == true, buffering = state.buffering, failed = state.failed,
        sourceName = sourceName, artworkRevision = artworkRevision, onClose = onBack,
        onToggle = { if (player?.isPlaying == true) player.pause() else { if (player?.playerError != null) player.prepare(); player?.play() } },
        onPrevious = { player?.seekToPreviousMediaItem() }, onNext = { player?.advanceTrack() },
        onSeek = { player?.seekTo(it) }, onShuffle = { player?.shuffleModeEnabled = !state.shuffle },
        onRepeat = { player?.repeatMode = if (state.repeatOne) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE },
        onLyrics = onLyrics, onRetry = { player?.prepare(); player?.play() }, modifier = modifier)
}

@Composable private fun CatMark(modifier: Modifier) {
    val fill = MaterialTheme.colorScheme.primaryContainer; val ink = MaterialTheme.colorScheme.onPrimaryContainer
    Canvas(modifier.semantics { contentDescription = "Michi" }) {
        val w = size.width.coerceAtMost(size.height * 1.8f); val left = (size.width - w) / 2; val top = size.height * .2f
        val a = Path().apply { moveTo(left + w*.08f, top+w*.1f); lineTo(left+w*.23f, 0f); lineTo(left+w*.36f, top+w*.12f); close() }
        val b = Path().apply { moveTo(left+w*.64f, top+w*.12f); lineTo(left+w*.77f, 0f); lineTo(left+w*.92f, top+w*.1f); close() }
        drawPath(a, fill); drawPath(b, fill); drawRoundRect(fill, Offset(left, top), Size(w, size.height-top), CornerRadius(size.height*.28f))
        drawCircle(ink, size.height*.045f, Offset(size.width/2-w*.14f, top+size.height*.3f)); drawCircle(ink, size.height*.045f, Offset(size.width/2+w*.14f, top+size.height*.3f))
        val n = Path().apply { moveTo(size.width/2-5, top+size.height*.48f); lineTo(size.width/2+5, top+size.height*.48f); lineTo(size.width/2, top+size.height*.56f); close() }; drawPath(n, ink)
    }
}

internal fun playlistNeedsUpdate(currentIds: List<String>, nextIds: List<String>) = currentIds != nextIds
internal fun hasActiveListeningSession(engaged: Boolean, playing: Boolean, positionMs: Long) = engaged || playing || positionMs > 0L
internal fun resumePosition(positionMs: Long, durationMs: Long): Long = when {
    positionMs < 0L -> 0L
    durationMs > 0L && positionMs >= durationMs -> 0L
    else -> positionMs
}
internal fun restoreQueueReady(sourceMatches: Boolean, requestedIds: List<String>, playerIds: List<String>): Boolean =
    sourceMatches && requestedIds.isNotEmpty() && requestedIds == playerIds
internal fun canSynchronizeLibrary(loaded: Boolean, loading: Boolean, sourceMatches: Boolean): Boolean = loaded && !loading && sourceMatches
private fun Song.asMediaItem() = MediaItem.Builder().setMediaId(id).setUri(uri).setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtist(artist).setAlbumTitle(album).build()).build()
internal fun formatTime(milliseconds: Long): String { val seconds = milliseconds.coerceAtLeast(0)/1000; return "%d:%02d".format(seconds/60, seconds%60) }
internal fun searchable(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase().trim()

private fun previewSongs() = listOf(
    Song("preview-luna", "Luna de septiembre.mp3", "Luna de septiembre", "Michi", "Noches tranquilas", 238_000, Uri.parse("content://preview/luna")),
    Song("preview-brisa", "Brisa de otoño.mp3", "Brisa de otoño", "Michi", "Noches tranquilas", 222_000, Uri.parse("content://preview/brisa")),
    Song("preview-mar", "Ecos del mar.mp3", "Ecos del mar", "Michi", "Noches tranquilas", 252_000, Uri.parse("content://preview/mar")),
)

@Preview(name = "01 · Primera apertura", device = "spec:width=412dp,height=915dp,dpi=420", showSystemUi = true)
@Composable
private fun OnboardingPreview() {
    MichiTheme(MichiSkin.ROSE) { OnboardingScreen {} }
}

@Preview(name = "03 · Ahora suena Rosa", device = "spec:width=412dp,height=915dp,dpi=420", showSystemUi = true)
@Composable
private fun NowPlayingRosePreview() {
    val song = previewSongs().first()
    MichiTheme(MichiSkin.ROSE) {
        Scaffold(bottomBar = { MichiNavigationBar(Destination.MUSIC) {} }) { padding ->
            NowPlayingScreen(song, PlayerState(playing = true, position = 84_000, duration = 238_000, engaged = true), null, {}, {}, Modifier.padding(padding))
        }
    }
}

@Preview(name = "04 · Listas", device = "spec:width=412dp,height=915dp,dpi=420", showSystemUi = true)
@Composable
private fun PlaylistsPreview() {
    MichiTheme(MichiSkin.ROSE) {
        Scaffold(bottomBar = { MichiNavigationBar(Destination.PLAYLISTS) {} }) { padding ->
            Box(Modifier.padding(padding)) {
                PlaylistsScreen(
                    112,
                    listOf(SavedPlaylist(Uri.parse("content://preview/night"), "Noches tranquilas", 24, 2)),
                    null,
                    {},
                    {},
                    {},
                )
            }
        }
    }
}

@Preview(name = "05 · Ahora suena Medianoche", device = "spec:width=412dp,height=915dp,dpi=420", showSystemUi = true)
@Composable
private fun NowPlayingMidnightPreview() {
    val song = previewSongs().last()
    MichiTheme(MichiSkin.MIDNIGHT) {
        Scaffold(bottomBar = { MichiNavigationBar(Destination.MUSIC) {} }) { padding ->
            NowPlayingScreen(song, PlayerState(playing = true, shuffle = true, position = 96_000, duration = 252_000, engaged = true), null, {}, {}, Modifier.padding(padding))
        }
    }
}
