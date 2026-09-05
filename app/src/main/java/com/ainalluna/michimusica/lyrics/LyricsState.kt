package com.ainalluna.michimusica.lyrics

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import com.ainalluna.michimusica.library.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class LyricsUiState(val title: String, val artist: String, val stored: StoredLyrics? = null,
                         val loading: Boolean = true, val busy: String? = null, val editing: Boolean = false,
                         val results: List<LyricsMatch> = emptyList(), val searched: Boolean = false,
                         val candidate: LyricsMatch? = null, val error: String? = null, val needsFolder: Boolean = false)

/** A controller is scoped to one song and folder. Previous requests cannot replace another song's lyrics. */
@Stable
class LyricsController(private val context: Context, private val folder: Uri, private val song: Song, private val scope: CoroutineScope) {
    var ui by mutableStateOf(LyricsUiState(song.title, song.artist))
        private set
    suspend fun load() {
        try { val value = LyricsStorage.load(context, folder, song.filename); ui = ui.copy(stored = value) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { ui = ui.copy(error = "No se pudo abrir la letra guardada. Revisa el acceso a tu carpeta.", needsFolder = true) }
        finally { ui = ui.copy(loading = false) }
    }
    fun edit() { ui = ui.copy(editing = true, error = null, candidate = null, needsFolder = false) }
    fun editTitle(value: String) { ui = ui.copy(title = value.take(160)) }
    fun editArtist(value: String) { ui = ui.copy(artist = value.take(160)) }
    fun back(): Boolean {
        if (ui.candidate != null) { ui = ui.copy(candidate = null); return true }
        if (ui.editing) { ui = ui.copy(editing = false, error = null, needsFolder = false); return true }
        return false
    }
    fun search() {
        if (ui.busy != null || ui.title.isBlank()) return
        val title = ui.title; val artist = ui.artist
        ui = ui.copy(busy = "Buscando en LRCLIB…", error = null, results = emptyList(), searched = false, needsFolder = false)
        scope.launch {
            try { val matches = LyricsRepository.search(title, artist); ui = ui.copy(results = matches, searched = true) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { ui = ui.copy(error = "No se pudo buscar. Revisa la conexión y vuelve a intentarlo.") }
            finally { ui = ui.copy(busy = null) }
        }
    }
    fun preview(match: LyricsMatch) { if (ui.busy == null) ui = ui.copy(candidate = match, error = null) }
    fun save() {
        val match = ui.candidate ?: return
        if (ui.busy != null) return
        ui = ui.copy(busy = "Guardando letra…", error = null)
        scope.launch {
            try {
                LyricsStorage.save(context, folder, song.filename, match.content, match.synced)
                ui = ui.copy(stored = StoredLyrics(match.content, match.synced), candidate = null, editing = false, results = emptyList())
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { ui = ui.copy(error = "No se pudo guardar. Vuelve a elegir la carpeta para conceder acceso de escritura.", needsFolder = true) }
            finally { ui = ui.copy(busy = null) }
        }
    }
    fun remove() {
        if (ui.busy != null) return
        ui = ui.copy(busy = "Quitando letra…", error = null)
        scope.launch {
            try { LyricsStorage.remove(context, folder, song.filename); ui = ui.copy(stored = null) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { ui = ui.copy(error = "No se pudo quitar la letra. Revisa el acceso a tu carpeta.", needsFolder = true) }
            finally { ui = ui.copy(busy = null) }
        }
    }
}
