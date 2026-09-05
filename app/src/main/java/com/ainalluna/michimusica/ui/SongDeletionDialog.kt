package com.ainalluna.michimusica.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ainalluna.michimusica.library.Song

@Composable
internal fun SongDeletionDialog(song: Song, busy: Boolean, error: String?, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("¿Borrar esta canción?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium)
                Text("Se eliminará el archivo «${song.filename}» de tu carpeta y dejará de estar disponible en tus listas. No se puede deshacer desde Michi.")
                if (busy) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Borrando…", Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                if (error != null) Text(error, Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onConfirm, enabled = !busy, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text(if (error == null) "Borrar canción" else "Reintentar")
            }
        },
        dismissButton = { TextButton(onDismiss, enabled = !busy) { Text("Cancelar") } },
    )
}
