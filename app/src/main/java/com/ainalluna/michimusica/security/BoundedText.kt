package com.ainalluna.michimusica.security

import java.io.Reader

/** Enforce the limit while reading, before a provider or network response exhausts memory. */
internal fun Reader.readBoundedText(maxChars: Int): String {
    require(maxChars > 0)
    val result = StringBuilder(minOf(maxChars, 4096))
    val buffer = CharArray(minOf(maxChars, 4096))
    while (true) {
        val count = read(buffer, 0, minOf(buffer.size, maxChars - result.length + 1))
        if (count < 0) return result.toString()
        require(result.length + count <= maxChars) { "El texto supera el tamaño permitido." }
        result.append(buffer, 0, count)
    }
}
