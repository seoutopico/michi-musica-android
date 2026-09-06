package com.ainalluna.michimusica.podcasts

import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.URI
import javax.net.ssl.HttpsURLConnection

/** No cookies, credentials, cleartext traffic or redirects into the local network. */
internal object PodcastNetwork {
    fun open(raw: String): HttpsURLConnection {
        var target = publicHttps(raw)
        repeat(6) {
            val uri = URI(target)
            require(InetAddress.getAllByName(uri.host).all {
                !it.isAnyLocalAddress && !it.isLoopbackAddress && !it.isLinkLocalAddress && !it.isSiteLocalAddress &&
                    !it.isMulticastAddress && !(it.address.size == 16 && (it.address[0].toInt() and 0xfe) == 0xfc)
            }) { "La dirección del podcast no es pública." }
            val connection = uri.toURL().openConnection() as HttpsURLConnection
            try {
                connection.instanceFollowRedirects = false
                connection.useCaches = false
                connection.connectTimeout = 15000; connection.readTimeout = 20000
                connection.setRequestProperty("User-Agent", "MichiMusica/1.12 (public podcast reader)")
                connection.setRequestProperty("Accept-Encoding", "identity")
                when (connection.responseCode) {
                    in 200..299 -> return connection
                    301, 302, 303, 307, 308 -> {
                        target = publicHttps(uri.resolve(connection.getHeaderField("Location") ?: error("Redirección incompleta.")).toString())
                        connection.disconnect()
                    }
                    401, 403 -> error("El servidor no permite acceso público a este contenido.")
                    404, 410 -> error("Ese contenido ya no está disponible.")
                    else -> error("El servidor no responde correctamente. Inténtalo más tarde.")
                }
            } catch (failure: Exception) { connection.disconnect(); throw failure }
        }
        error("El enlace tiene demasiadas redirecciones.")
    }

    fun bytes(raw: String, limit: Int): ByteArray {
        val connection = open(raw)
        try {
            require(connection.contentLengthLong <= limit) { "El contenido supera el tamaño admitido." }
            return connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(16384)
                val deadline = System.nanoTime() + 60_000_000_000L
                while (true) {
                    if (Thread.currentThread().isInterrupted) throw InterruptedException()
                    if (System.nanoTime() > deadline) throw java.net.SocketTimeoutException()
                    val count = input.read(buffer)
                    if (count < 0) break
                    require(output.size() + count <= limit) { "El contenido supera el tamaño admitido." }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
        } finally { connection.disconnect() }
    }

    fun feed(raw: String): PodcastShow {
        val url = publicFeedUrl(raw)
        val content = bytes(url, PodcastFeed.MAX_BYTES)
        val beginning = content.take(2048).toByteArray().toString(Charsets.UTF_8)
        require(!Regex("<(html|!doctype\\s+html)\\b", RegexOption.IGNORE_CASE).containsMatchIn(beginning)) {
            "Ese enlace es una página web. Copia la dirección de Fuente RSS del programa."
        }
        return PodcastFeed.parse(content, url)
    }
}

internal fun podcastError(failure: Throwable): String = when (failure) {
    is java.net.UnknownHostException, is java.net.ConnectException -> "No hay conexión con el podcast. Comprueba la red y reintenta."
    is java.net.SocketTimeoutException -> "El servidor tarda demasiado. Inténtalo de nuevo."
    is java.net.SocketException -> "Se ha interrumpido la conexión. Inténtalo de nuevo."
    is javax.net.ssl.SSLException -> "No se pudo establecer una conexión segura con el servidor."
    is SecurityException -> "Android ha retirado el acceso. Vuelve a elegir la carpeta."
    is IllegalArgumentException, is IllegalStateException -> failure.message ?: "No se pudo completar la operación."
    else -> "No se pudo leer o guardar el contenido. Comprueba la conexión y el espacio disponible, y reintenta."
}
