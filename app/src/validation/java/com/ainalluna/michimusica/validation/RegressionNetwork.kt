package com.ainalluna.michimusica.validation

import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.cert.Certificate
import javax.net.ssl.HttpsURLConnection

/** Controlled HTTPS responses in this process only; no hooks exist in production sources. */
internal object RegressionNetwork {
    const val FEED = "https://example.com/michi-regression.xml"
    @Volatile var slow = false
    @Volatile var broken = false
    @Volatile var newEpisode = false
    private var installed = false
    val audio: ByteArray = ByteBuffer.allocate(88244).order(ByteOrder.LITTLE_ENDIAN).apply {
        put("RIFF".toByteArray()); putInt(88236); put("WAVEfmt ".toByteArray()); putInt(16)
        putShort(1); putShort(1); putInt(44100); putInt(88200); putShort(2); putShort(16)
        put("data".toByteArray()); putInt(88200)
        repeat(44100) { putShort((kotlin.math.sin(it * 2 * Math.PI * 440 / 44100) * 4000).toInt().toShort()) }
    }.array()
    private fun rss() = """<rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd"><channel><title>Validación RSS</title>${(1..if (newEpisode) 9 else 8).joinToString("") {
        "<item><guid>episode-$it</guid><title>Audio de validación $it</title><pubDate>Sun, 06 Sep 2026 08:00:00 GMT</pubDate><itunes:duration>1</itunes:duration><enclosure url=\"https://example.com/audio-$it.wav\" type=\"audio/wav\" length=\"88244\"/></item>"
    }}</channel></rss>""".toByteArray()
    fun install() {
        if (installed) return
        URL.setURLStreamHandlerFactory { protocol -> if (protocol != "https") null else object : URLStreamHandler() {
            override fun openConnection(u: URL): URLConnection {
                check(u.host == "example.com") { "Unexpected network request in isolated regression" }
                return object : HttpsURLConnection(u) {
                    val isAudio = u.path.endsWith(".wav")
                    val bytes = if (isAudio) audio else rss()
                    override fun connect() {}
                    override fun disconnect() {}
                    override fun usingProxy() = false
                    override fun getCipherSuite() = "VALIDATION_ONLY"
                    override fun getLocalCertificates(): Array<Certificate>? = null
                    override fun getServerCertificates(): Array<Certificate> = emptyArray()
                    override fun getResponseCode() = 200
                    override fun getContentType() = if (isAudio) "audio/wav" else "application/rss+xml"
                    override fun getContentLengthLong() = bytes.size.toLong()
                    override fun getInputStream(): InputStream = object : InputStream() {
                        var position = 0
                        override fun read(): Int = if (position >= bytes.size) -1 else bytes[position++].toInt() and 255
                        override fun read(b: ByteArray, off: Int, len: Int): Int {
                            if (isAudio && broken && position > 0) throw java.net.SocketException("Simulated connection loss")
                            if (isAudio && slow) Thread.sleep(30)
                            if (position >= bytes.size) return -1
                            val n = minOf(len, bytes.size - position, if (isAudio) 512 else 16384)
                            bytes.copyInto(b, off, position, position + n); position += n; return n
                        }
                    }
                }
            }
        } }
        installed = true
    }
}
