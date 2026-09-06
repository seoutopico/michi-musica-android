package com.ainalluna.michimusica

import com.ainalluna.michimusica.podcasts.*
import org.junit.Assert.*
import org.junit.Test

class YouTubePodcastsTest {
    private val channel = "UCVmlEoDSeImXV5aPz8EvP0A"
    private val url = "https://www.youtube.com/channel/$channel"
    private val now = podcastDate("2026-09-06T18:00:00+00:00")
    private fun entry(id: String = "diJ6j8Z1LzQ", title: String = "Una charla", date: String = "2026-09-05T17:00:06+00:00", channel: String = this.channel) = """
        <entry><yt:videoId>$id</yt:videoId><yt:channelId>$channel</yt:channelId><title>$title</title>
        <published>$date</published><updated>2026-09-06T17:00:00+00:00</updated>
        <media:group><media:title>Duplicated title</media:title><media:thumbnail url="https://i1.ytimg.com/vi/$id/hqdefault.jpg"/>
        <media:description>Una conversación &amp; preguntas</media:description></media:group></entry>
    """
    private fun feed(entries: String) = """<feed xmlns="http://www.w3.org/2005/Atom" xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns:media="http://search.yahoo.com/mrss/">
        <yt:channelId>${channel.removePrefix("UC")}</yt:channelId><title>Charlas</title><author><name>Autor</name></author>$entries</feed>"""
    private fun parse(xml: String) = YouTubePodcasts.parse(xml.toByteArray(), url)

    @Test fun resolvesHandleAndTabsToSameChannelWithoutFollowingVideoLinks() {
        for (suffix in listOf("", "/videos", "/podcasts", "/streams", "/shorts", "/featured")) {
            val resolved = YouTubePodcasts.resolve("https://www.youtube.com/@COASTTOCOASTAMOFFICIAL$suffix?si=share") {
                assertEquals("https://www.youtube.com/@COASTTOCOASTAMOFFICIAL", it)
                """{"externalId":"$channel"}""".toByteArray()
            }
            assertEquals(url, resolved)
        }
        assertEquals(url, YouTubePodcasts.resolve("$url/videos") { error("Should not fetch HTML") })
        assertEquals(url, YouTubePodcasts.normalize("https://www.youtube.com/feeds/videos.xml?channel_id=$channel"))
    }

    @Test fun rejectsVideoPlaylistSpoofAndPrivateUrls() {
        for (raw in listOf("https://youtu.be/diJ6j8Z1LzQ", "https://www.youtube.com/watch?v=diJ6j8Z1LzQ",
            "https://www.youtube.com/playlist?list=PL123", "https://youtube.com.evil.org/@test",
            "https://name:password@www.youtube.com/@test", "http://www.youtube.com/@test",
            "https://www.youtube.com/@test?token=secret", "https://www.youtube.com/channel/invalid")) {
            assertTrue(raw, runCatching { YouTubePodcasts.normalize(raw) }.isFailure)
        }
    }

    @Test fun atomUsesPublicationNotUpdateAndBuildsTrustedAudioSource() {
        val show = parse(feed(entry()))
        assertEquals(url, show.url); assertEquals("Charlas", show.title); assertEquals("Autor", show.author)
        val episode = show.episodes.single()
        assertEquals("Una charla", episode.title)
        assertEquals("Una conversación & preguntas", episode.description)
        assertEquals(podcastDate("2026-09-05T17:00:06Z"), episode.published)
        assertEquals("https://www.youtube.com/watch?v=diJ6j8Z1LzQ", episode.audio)
        assertTrue(episode.isRecent(now)); assertEquals(0L, episode.duration)
    }

    @Test fun filtersPaymentPreviewsWrongChannelsAndInvalidIds() {
        val show = parse(feed(entry() + entry(title = "Members only: charla") + entry(title = "Preview: charla") +
            entry(channel = "UCDifferent") + entry(id = "https://evil.org")))
        assertEquals(1, show.episodes.size); assertEquals(2, show.excluded)
    }

    @Test fun secureXmlRejectsEntitiesAndWrongRoot() {
        for (xml in listOf("<!DOCTYPE feed [<!ENTITY e SYSTEM 'file:///etc/passwd'>]>" + feed(entry()), "<html>oops</html>")) {
            assertTrue(runCatching { parse(xml) }.isFailure)
            assertTrue(runCatching { YouTubePodcasts.parse(xml.toByteArray(Charsets.UTF_16), url) }.isFailure)
        }
    }

    @Test fun oldFutureAndUndatedEntriesAreNeverNews() {
        val show = parse(feed(entry(date = "2026-09-01T00:00:00Z") + entry("aaaaaaaaaaa", date = "2030-01-01T00:00:00Z") + entry("bbbbbbbbbbb", date = "")))
        assertEquals(3, show.episodes.size)
        assertTrue(mergePodcast(show.copy(episodes = emptyList()), show, now).episodes.none { it.isNew })
    }

    @Test fun followingSortsByLatestPublishedEpisodeAcrossProvidersAndUpdates() {
        val youtube = parse(feed(entry())).copy(title = "Z YouTube")
        val rss = youtube.copy(url = "https://example.org/feed.xml", title = "A RSS", episodes = listOf(youtube.episodes.single().copy(published = now)))
        val empty = youtube.copy(url = "https://example.org/empty", episodes = emptyList())
        val future = youtube.copy(url = "https://example.org/future", episodes = listOf(youtube.episodes.single().copy(published = now + 1000)))
        assertEquals(listOf(rss, youtube, empty, future), podcastsByLatest(listOf(future, empty, youtube, rss), now))
        val updated = youtube.copy(episodes = youtube.episodes + youtube.episodes.single().copy(published = now + 500))
        assertEquals(updated, podcastsByLatest(listOf(rss, updated), now + 1000).first())
    }
}
