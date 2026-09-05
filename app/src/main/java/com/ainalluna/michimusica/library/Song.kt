package com.ainalluna.michimusica.library

import android.net.Uri

data class Song(
    val id: String,
    val filename: String,
    val title: String,
    val artist: String = "",
    val album: String = "",
    val durationMs: Long = 0L,
    val uri: Uri,
)
