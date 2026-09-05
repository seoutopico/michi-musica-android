package com.ainalluna.michimusica.library

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/** Delete only a supported audio file directly inside the currently authorized folder. */
internal fun deleteSongFile(context: Context, folderUri: Uri, songUri: Uri) {
    val root = DocumentFile.fromTreeUri(context, folderUri)
        ?: error("No puedo abrir la carpeta. Vuelve a elegirla en Ajustes.")
    val document = root.listFiles().firstOrNull { it.uri == songUri }
        ?: error("El archivo ya no está disponible. Relee la carpeta desde Ajustes.")
    require(document.isFile && MusicFolderReader.isSupportedAudio(document.name.orEmpty())) {
        "Solo se pueden borrar archivos de audio de esta carpeta."
    }
    check(document.delete()) { "Android no pudo borrar el archivo. Comprueba que la carpeta permite eliminar archivos." }
}

/** Descending indices keep every remaining entry and its order, including duplicate references. */
internal fun removedSongIndices(queueIds: List<String>, songId: String): List<Int> =
    queueIds.indices.reversed().filter { queueIds[it] == songId }
