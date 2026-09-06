package com.ainalluna.michimusica.podcasts

import java.io.ByteArrayInputStream
import java.net.URI
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

/** Channels use YouTube's public Atom feed; no account or API key is stored. */
internal object YouTubePodcasts {
    private val channelId = Regex("UC[A-Za-z0-9_-]{22}")
    private val videoId = Regex("[A-Za-z0-9_-]{11}")
    private const val ATOM = "http://www.w3.org/2005/Atom"
    private const val YT = "http://www.youtube.com/xml/schemas/2015"
    private const val MEDIA = "http://search.yahoo.com/mrss/"

    fun isYouTube(raw: String): Boolean = runCatching {
        URI(raw.trim()).host?.lowercase() in setOf("youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be")
    }.getOrDefault(false)

    fun channelUrl(id: String): String {
        require(channelId.matches(id)) { "El identificador del canal de YouTube no es válido." }
        return "https://www.youtube.com/channel/$id"
    }

    fun normalize(raw: String): String {
        val uri = URI(publicFeedUrl(raw))
        require(isYouTube(raw) && uri.host != "youtu.be") { "Pega el enlace del canal de YouTube; para un vídeo suelto utiliza Buscar." }
        val parts = uri.path.trim('/').split('/')
        if (uri.path == "/feeds/videos.xml") {
            val id = uri.query.orEmpty().split('&').singleOrNull { it.startsWith("channel_id=") }?.substringAfter('=')
            return channelUrl(id.orEmpty())
        }
        val baseSize = if (parts.first().startsWith('@')) 1 else 2
        require(parts.size == baseSize || (parts.size == baseSize + 1 && parts.last() in setOf("videos", "podcasts", "streams", "shorts", "featured"))) {
            "Pega el enlace del canal de YouTube; para un vídeo suelto utiliza Buscar."
        }
        if (parts.first() == "channel") return channelUrl(parts.getOrElse(1) { "" })
        require((parts.first().startsWith('@') && parts.first().length > 1) ||
            (parts.first() in setOf("c", "user") && parts.getOrElse(1) { "" }.isNotBlank())) {
            "Pega un enlace de canal de YouTube, por ejemplo youtube.com/@nombre."
        }
        return "https://www.youtube.com/" + parts.take(baseSize).joinToString("/")
    }

    fun resolve(raw: String, fetch: (String) -> ByteArray): String {
        val normalized = normalize(raw)
        if (URI(normalized).path.startsWith("/channel/")) return normalized
        val html = fetch(normalized).toString(Charsets.UTF_8)
        val id = Regex("\"externalId\"\\s*:\\s*\"(UC[A-Za-z0-9_-]{22})\"").find(html)?.groupValues?.get(1)
            ?: error("No puedo identificar ese canal. Prueba su enlace /channel/ o vuelve a intentarlo más tarde.")
        return channelUrl(id)
    }

    fun feed(raw: String): PodcastShow {
        val url = resolve(raw) { PodcastNetwork.bytes(it, PodcastFeed.MAX_BYTES) }
        val id = URI(url).path.substringAfterLast('/')
        return parse(PodcastNetwork.bytes("https://www.youtube.com/feeds/videos.xml?channel_id=$id", PodcastFeed.MAX_BYTES), url)
    }

    fun parse(bytes: ByteArray, source: String): PodcastShow {
        require(bytes.size <= PodcastFeed.MAX_BYTES) { "El canal devuelve demasiados datos." }
        require(!Regex("<!\\s*(DOCTYPE|ENTITY)", RegexOption.IGNORE_CASE)
            .containsMatchIn(bytes.toString(Charsets.UTF_8).replace("\u0000", ""))) { "Declaraciones XML no admitidas." }
        val url = normalize(source)
        require(URI(url).path.startsWith("/channel/"))
        val expectedChannel = URI(url).path.substringAfterLast('/')
        val episodes = mutableListOf<PodcastEpisode>()
        val stack = mutableListOf<Pair<String, String>>()
        val texts = mutableListOf<StringBuilder>()
        var fields: MutableMap<String, String>? = null
        var title = ""; var author = ""; var feedChannel = ""; var excluded = 0; var count = 0
        val handler = object : DefaultHandler() {
            override fun resolveEntity(publicId: String?, systemId: String?): InputSource = error("Entidades externas no admitidas.")
            override fun startElement(uri: String, local: String, qName: String, attrs: Attributes) {
                require(stack.size < 40) { "El canal devuelve XML demasiado profundo." }
                if (stack.isEmpty()) require(uri == ATOM && local == "feed") { "No encuentro publicaciones de YouTube." }
                if (uri == ATOM && local == "entry" && stack.size == 1) {
                    require(++count <= 1000); fields = mutableMapOf()
                }
                stack.add(uri to local); texts.add(StringBuilder())
                if (uri == MEDIA && local == "thumbnail") fields?.set("image",
                    runCatching { publicHttps(attrs.getValue("url").orEmpty()) }.getOrDefault(""))
            }
            override fun characters(ch: CharArray, start: Int, length: Int) {
                texts.lastOrNull()?.let { if (it.length < 32000) it.append(ch, start, minOf(length, 32000 - it.length)) }
            }
            override fun endElement(uri: String, local: String, qName: String) {
                stack.removeAt(stack.lastIndex)
                val value = texts.removeAt(texts.lastIndex).toString().trim()
                val parent = stack.lastOrNull()
                val item = fields
                if (item != null) {
                    if (parent == (ATOM to "entry") && ((uri == ATOM && local in setOf("title", "published")) ||
                            (uri == YT && local in setOf("videoId", "channelId")))) item[local] = value
                    if (uri == MEDIA && local == "description") item[local] = value
                    if (uri == ATOM && local == "entry" && stack.size == 1) {
                        val t = plainPodcastText(item["title"].orEmpty()).take(500)
                        val d = plainPodcastText(item["description"].orEmpty()).take(4000)
                        val id = item["videoId"].orEmpty()
                        if (restrictedPodcast(t, d)) excluded++
                        else if (t.isNotBlank() && videoId.matches(id) && item["channelId"] == expectedChannel) {
                            episodes.add(PodcastEpisode(podcastId("youtube:$id"), t, d, "https://www.youtube.com/watch?v=$id",
                                "audio/mpeg", podcastDate(item["published"].orEmpty()), 0, item["image"].orEmpty()))
                        }
                        fields = null
                    }
                } else {
                    if (uri == ATOM && local == "title" && stack.size == 1) title = plainPodcastText(value).take(500)
                    if (uri == ATOM && local == "name" && parent == (ATOM to "author")) author = plainPodcastText(value).take(500)
                    if (uri == YT && local == "channelId" && stack.size == 1) feedChannel = value
                }
            }
        }
        SAXParserFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }.newSAXParser().parse(ByteArrayInputStream(bytes), handler)
        require(title.isNotBlank() && feedChannel in setOf(expectedChannel, expectedChannel.removePrefix("UC"))) { "La respuesta no corresponde al canal solicitado." }
        val sorted = episodes.distinctBy { it.id }.sortedByDescending { it.published }
        return PodcastShow(url, title, author, sorted.firstOrNull()?.image.orEmpty(), sorted, excluded = excluded)
    }
}

internal fun podcastSource(raw: String): PodcastShow =
    if (YouTubePodcasts.isYouTube(raw)) YouTubePodcasts.feed(raw) else PodcastNetwork.feed(raw)

internal fun podcastsByLatest(shows: List<PodcastShow>, now: Long): List<PodcastShow> = shows.sortedWith(
    compareByDescending<PodcastShow> { show -> show.episodes.filter { it.published in 1..now }.maxOfOrNull { it.published } ?: 0L }
        .thenBy { it.title.lowercase() }.thenBy { it.url }
)
