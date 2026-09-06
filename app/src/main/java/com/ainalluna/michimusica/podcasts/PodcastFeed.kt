package com.ainalluna.michimusica.podcasts

import java.io.ByteArrayInputStream
import java.net.URI
import java.security.MessageDigest
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Locale
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

data class PodcastEpisode(
    val id: String, val title: String, val description: String, val audio: String,
    val mime: String, val published: Long, val duration: Long, val image: String = "",
    val isNew: Boolean = false,
)

data class PodcastShow(
    val url: String, val title: String, val author: String, val image: String,
    val episodes: List<PodcastEpisode>, val checked: Long = 0, val error: String = "",
    val seen: Set<String> = emptySet(), val excluded: Int = 0,
)

internal const val PODCAST_NEWS_WINDOW_MS = 3 * 24 * 60 * 60 * 1000L

/** News is based on publication age, never the date Michi discovered an entry. */
internal fun PodcastEpisode.isRecent(now: Long): Boolean =
    published > 0 && published <= now && now - published <= PODCAST_NEWS_WINDOW_MS

internal fun PodcastEpisode.isUnseenRecent(now: Long): Boolean = isNew && isRecent(now)

internal class PodcastPreviewException : IllegalStateException("El RSS solo ofrece un adelanto. El audio completo no está disponible para descargar desde este enlace público.")

internal fun requireCompletePodcast(expectedDuration: Long, actualDuration: Long) {
    require(actualDuration > 0) { "El archivo recibido no contiene un audio reproducible." }
    // Allow small encoder/advertisement differences, but reject materially truncated episodes.
    if (expectedDuration > 0 && expectedDuration - actualDuration > maxOf(30000L, expectedDuration / 10)) throw PodcastPreviewException()
}

internal fun podcastId(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

internal fun publicHttps(raw: String): String {
    require(raw.length <= 8192) { "El enlace es demasiado largo." }
    val uri = runCatching { URI(raw.trim()) }.getOrElse { error("El enlace no es válido.") }
    require(uri.scheme.equals("https", true) && !uri.host.isNullOrBlank() && uri.userInfo == null && uri.port in listOf(-1, 443)) {
        "Usa un enlace público HTTPS, sin usuario ni contraseña."
    }
    val host = uri.host.lowercase(Locale.ROOT)
    require(host != "localhost" && !host.endsWith(".localhost") && !host.endsWith(".local") && !host.contains(':') &&
        !Regex("[0-9.]+").matches(host)) { "Usa la dirección pública del podcast." }
    return URI("https://${uri.rawAuthority.lowercase(Locale.ROOT)}${uri.rawPath.ifBlank { "/" }}${uri.rawQuery?.let { "?$it" }.orEmpty()}").toASCIIString()
}

internal fun publicFeedUrl(raw: String): String {
    val result = publicHttps(raw)
    val uri = URI(result)
    val query = uri.query.orEmpty().lowercase(Locale.ROOT)
    require(!Regex("(^|&)(token|auth|key|password|email|signature|sig|access_token)=").containsMatchIn(query) &&
        !uri.path.contains("/private", true)) { "Esta sección admite solo RSS públicos, sin suscripciones de pago." }
    return result
}

internal fun plainPodcastText(raw: String): String {
    val withoutTags = raw.replace(Regex("<[^>]*>"), " ")
    val entities = mapOf("amp" to "&", "quot" to "\"", "apos" to "'", "lt" to "<", "gt" to ">", "nbsp" to " ",
        "aacute" to "á", "eacute" to "é", "iacute" to "í", "oacute" to "ó", "uacute" to "ú", "ntilde" to "ñ", "uuml" to "ü")
    return Regex("&(#x[0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);").replace(withoutTags) { match ->
        val key = match.groupValues[1]
        val number = if (key.startsWith("#x")) key.drop(2).toIntOrNull(16) else if (key.startsWith('#')) key.drop(1).toIntOrNull() else null
        if (number != null && Character.isValidCodePoint(number) && number !in 0xD800..0xDFFF) String(Character.toChars(number))
        else entities[key] ?: match.value
    }.replace(Regex("\\s+"), " ").trim()
}

internal fun restrictedPodcast(title: String, description: String, episodeType: String = ""): Boolean {
    val text = Normalizer.normalize(plainPodcastText("$title $description"), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
    return episodeType.equals("trailer", true) || listOf(
        "exclusivo para mecenas", "exclusivo para fans", "solo para mecenas", "solo para suscriptores",
        "episodios exclusivos como", "episodio exclusivo", "contenido de pago", "suscriptores de pago",
        "paid subscribers", "paid episode", "subscriber-only", "subscribers only", "premium episode",
        "premium content", "members only", "members-only", "patreon exclusive", "preview of a paid",
        "preview from an exclusive", "adelanto para", "avance exclusivo",
    ).any(text::contains) || Regex("(^|[\\s(:\\[])((free )?preview|adelanto|trailer|avance)([\\s):\\]]|$)")
        .containsMatchIn(Normalizer.normalize(title, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT))
}

internal fun podcastDuration(raw: String): Long {
    val parts = raw.trim().split(':').map { it.toLongOrNull() ?: return 0 }
    if (parts.size !in 1..3 || parts.any { it < 0 || it > 604800 }) return 0
    return parts.fold(0L) { total, part -> total * 60 + part }.coerceAtMost(604800) * 1000
}

internal fun podcastDate(raw: String): Long {
    for (pattern in listOf("EEE, dd MMM yyyy HH:mm:ss Z", "dd MMM yyyy HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")) {
        runCatching { SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(raw.trim())?.time }
            .getOrNull()?.let { return it }
    }
    return 0
}

/** RSS 2.0 audio only; declarations/entities, deep XML and oversized feeds fail closed. */
object PodcastFeed {
    const val MAX_BYTES = 6 * 1024 * 1024
    fun parse(bytes: ByteArray, source: String): PodcastShow {
        require(bytes.size <= MAX_BYTES) { "El RSS es demasiado grande." }
        val scan = bytes.toString(Charsets.UTF_8).replace("\u0000", "")
        require(!Regex("<!\\s*(DOCTYPE|ENTITY)", RegexOption.IGNORE_CASE).containsMatchIn(scan)) { "Ese RSS contiene declaraciones XML no admitidas." }
        val feedUrl = publicFeedUrl(source)
        val episodes = mutableListOf<PodcastEpisode>()
        val stack = mutableListOf<String>()
        val texts = mutableListOf<StringBuilder>()
        var title = ""; var author = ""; var image = ""; var excluded = 0; var items = 0
        var fields: MutableMap<String, String>? = null
        var channel = false
        var channelBlocked = false
        fun resolve(raw: String): String = if (raw.isBlank()) "" else runCatching { publicHttps(URI(feedUrl).resolve(raw).toString()) }.getOrDefault("")
        val handler = object : DefaultHandler() {
            override fun resolveEntity(publicId: String?, systemId: String?): InputSource = throw IllegalArgumentException("Entidades externas no admitidas.")
            override fun startElement(uri: String, local: String, qName: String, attrs: Attributes) {
                require(stack.size < 40) { "El RSS tiene demasiados niveles." }
                val name = local.ifBlank { qName.substringAfter(':') }
                stack.add(name); texts.add(StringBuilder())
                if (name == "channel" && stack.size == 2 && stack.first() == "rss") channel = true
                if (name == "item" && stack.getOrNull(stack.lastIndex - 1) == "channel") {
                    require(++items <= 5000) { "El RSS contiene demasiados episodios." }; fields = mutableMapOf()
                }
                if (name == "enclosure" && fields != null && fields!!["audio"].isNullOrBlank()) {
                    val mime = attrs.getValue("type").orEmpty().substringBefore(';').lowercase(Locale.ROOT)
                    if (mime.startsWith("audio/")) { fields!!["audio"] = resolve(attrs.getValue("url").orEmpty()); fields!!["mime"] = mime }
                }
                if (name == "image" && attrs.getValue("href") != null) {
                    if (fields != null) fields!!["image"] = resolve(attrs.getValue("href")) else image = resolve(attrs.getValue("href"))
                }
                if (name == "isAccessibleForFree" && attrs.getValue("value").equals("false", true)) {
                    if (fields != null) fields!!["paid"] = "true" else channelBlocked = true
                }
            }
            override fun characters(ch: CharArray, start: Int, length: Int) {
                if (texts.isNotEmpty() && texts.last().length < 32000) texts.last().append(ch, start, minOf(length, 32000 - texts.last().length))
            }
            override fun endElement(uri: String, local: String, qName: String) {
                val name = stack.removeAt(stack.lastIndex)
                val value = texts.removeAt(texts.lastIndex).toString().trim()
                val parent = stack.lastOrNull()
                val item = fields
                if (item != null) {
                    if (parent == "item") when (name) {
                        "title", "guid", "pubDate", "duration", "episodeType", "description" -> item[name] = value
                        "encoded", "summary" -> if (item["description"].isNullOrBlank()) item["description"] = value
                        "isAccessibleForFree" -> if (value.equals("false", true)) item["paid"] = "true"
                    }
                    if (name == "item") {
                        val t = plainPodcastText(item["title"].orEmpty()).take(500)
                        val d = plainPodcastText(item["description"].orEmpty()).take(4000)
                        if (item["paid"] == "true" || restrictedPodcast(t, d, item["episodeType"].orEmpty())) excluded++
                        else if (!item["audio"].isNullOrBlank() && t.isNotBlank() && runCatching { podcastExtension(item["mime"].orEmpty()) }.isSuccess) episodes.add(PodcastEpisode(
                            podcastId(item["guid"].orEmpty().ifBlank { item["audio"]!!.substringBefore('?') }), t, d,
                            item["audio"]!!, item["mime"].orEmpty(), podcastDate(item["pubDate"].orEmpty()),
                            podcastDuration(item["duration"].orEmpty()), item["image"].orEmpty(),
                        ))
                        fields = null
                    }
                } else if (parent == "channel") when (name) {
                    "title" -> title = plainPodcastText(value).take(500)
                    "author" -> author = plainPodcastText(value).take(500)
                    "isAccessibleForFree" -> if (value.equals("false", true)) channelBlocked = true
                }
                if (name == "url" && parent == "image" && fields == null && image.isBlank()) image = resolve(value)
            }
        }
        val factory = SAXParserFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }
        factory.newSAXParser().parse(ByteArrayInputStream(bytes), handler)
        require(channel && title.isNotBlank()) { "No encuentro un RSS de podcast. Pega su enlace RSS público." }
        require(!channelBlocked) { "Ese podcast está marcado como contenido de pago." }
        return PodcastShow(feedUrl, title, author, image, episodes.distinctBy { it.id }.sortedByDescending { it.published }.take(1000), excluded = excluded)
    }
}

internal fun mergePodcast(previous: PodcastShow?, fetched: PodcastShow, now: Long): PodcastShow {
    val known = previous?.seen.orEmpty() + previous?.episodes.orEmpty().map { it.id }
    val old = previous?.episodes.orEmpty().associateBy { it.id }
    return fetched.copy(checked = now, seen = (known + fetched.episodes.map { it.id }).takeLastSet(20000), episodes = fetched.episodes.map {
        it.copy(isNew = it.isRecent(now) && (old[it.id]?.isNew ?: (previous != null && it.id !in known)))
    })
}

private fun <T> Set<T>.takeLastSet(count: Int): Set<T> = toList().takeLast(count).toSet()
