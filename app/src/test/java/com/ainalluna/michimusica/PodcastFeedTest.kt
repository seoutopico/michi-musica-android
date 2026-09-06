package com.ainalluna.michimusica

import com.ainalluna.michimusica.podcasts.*
import org.junit.Assert.*
import org.junit.Test

class PodcastFeedTest {
    private val source = "https://example.org/podcast.xml"
    private fun rss(items: String, extra: String = "") = """<rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:podcast="https://podcastindex.org/namespace/1.0"><channel><title>Charlas &amp; café</title><itunes:author>Aina</itunes:author><itunes:image href="/cover.jpg"/>$extra$items</channel></rss>"""
    private fun item(id: String, title: String = "Episodio $id", extra: String = "", mime: String = "audio/mpeg", url: String = "/audio/$id.mp3") =
        """<item><title>$title</title><guid>$id</guid><enclosure url="$url" type="$mime"/><pubDate>Sat, 05 Sep 2026 14:37:42 +0200</pubDate><itunes:duration>01:14:02</itunes:duration>$extra</item>"""
    private fun parse(xml: String) = PodcastFeed.parse(xml.toByteArray(), source)

    @Test fun readsPublicRssDatesRelativeUrlsAndDurations() {
        val s = parse(rss(item("one", extra = "<description><![CDATA[Una <b>charla</b> &ntilde;]]></description>")))
        assertEquals("Charlas & café", s.title); assertEquals("Aina", s.author)
        assertEquals("https://example.org/cover.jpg", s.image)
        val e = s.episodes.single()
        assertEquals("https://example.org/audio/one.mp3", e.audio)
        assertEquals(4442000L, e.duration); assertTrue(e.published > 0)
        assertEquals("Una charla ñ", e.description)
    }

    @Test fun excludesIvooxPatronEpisodesButKeepsPublicBoilerplate() {
        val s = parse(rss(item("free", extra = "<description>Escucha el episodio completo en la app de iVoox</description>") +
            item("paid", "La Tercera Hora - Episodio exclusivo para mecenas") +
            item("paid2", extra = "<description>Agradece a este podcast tantas horas de entretenimiento y disfruta de episodios exclusivos como éste.</description>")))
        assertEquals(listOf("Episodio free"), s.episodes.map { it.title }); assertEquals(2, s.excluded)
    }

    @Test fun filtersPreviewsTrailersAndExplicitPaymentMetadata() {
        val s = parse(rss(item("preview", "Preview: charla") + item("trailer", extra = "<itunes:episodeType>trailer</itunes:episodeType>") +
            item("paid", extra = "<podcast:isAccessibleForFree>false</podcast:isAccessibleForFree>") + item("ok")))
        assertEquals(1, s.episodes.size); assertEquals(3, s.excluded)
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsPaidChannel() { parse(rss(item("one"), "<podcast:isAccessibleForFree>false</podcast:isAccessibleForFree>")) }
    @Test fun excludesInsecureAndNonAudioEnclosures() {
        val s = parse(rss(item("http", url = "http://example.org/a.mp3") + item("local", url = "https://localhost/a.mp3") +
            item("video", mime = "video/mp4") + item("empty", url = "") + item("ok")))
        assertEquals(1, s.episodes.size)
    }
    @Test fun duplicateGuidsProduceOneEpisode() { assertEquals(1, parse(rss(item("a") + item("a"))).episodes.size) }
    @Test fun missingGuidKeepsIdentityAcrossExpiringAudioQueries() {
        val a = parse(rss(item("a", url = "https://example.org/a.mp3?expires=1").replace("<guid>a</guid>", "")))
        val b = parse(rss(item("a", url = "https://example.org/a.mp3?expires=2").replace("<guid>a</guid>", "")))
        assertEquals(a.episodes.single().id, b.episodes.single().id)
    }
    @Test fun firstFollowDoesNotMarkTheWholeArchiveAsNew() {
        val merged = mergePodcast(null, parse(rss(item("a") + item("b"))), 10)
        assertFalse(merged.episodes.any { it.isNew }); assertEquals(2, merged.seen.size)
    }
    @Test fun refreshIdentifiesNewEpisodesAndPreservesReadState() {
        val now = podcastDate("Sun, 06 Sep 2026 10:00:00 +0200")
        val first = mergePodcast(null, parse(rss(item("a"))), now)
        val second = mergePodcast(first, parse(rss(item("a") + item("b"))), now + 1)
        assertEquals(listOf("Episodio b"), second.episodes.filter { it.isNew }.map { it.title })
        val seen = second.copy(episodes = second.episodes.map { it.copy(isNew = false) })
        val third = mergePodcast(seen, parse(rss(item("a") + item("b"))), now + 2)
        assertFalse(third.episodes.any { it.isNew })
    }
    @Test fun anOlderEpisodeReturningToFeedIsNotNewAgain() {
        val now = podcastDate("Sun, 06 Sep 2026 10:00:00 +0200")
        val first = mergePodcast(null, parse(rss(item("a"))), now)
        val gone = mergePodcast(first, parse(rss(item("b"))), now + 1)
        assertTrue(gone.episodes.single().isNew)
        assertFalse(mergePodcast(gone, parse(rss(item("a"))), now + 2).episodes.single().isNew)
    }
    @Test fun previouslyFreeEpisodeBecomingPaidDisappears() {
        val first = mergePodcast(null, parse(rss(item("a"))), 1)
        assertTrue(mergePodcast(first, parse(rss(item("a", "Episodio exclusivo para fans"))), 2).episodes.isEmpty())
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsExternalEntities() {
        parse("<!DOCTYPE rss [<!ENTITY secret SYSTEM 'file:///etc/passwd'>]>" + rss(item("a", "&secret;")))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsUtf16Doctype() {
        PodcastFeed.parse(("<?xml version='1.0' encoding='UTF-16'?>" + "<!DOCTYPE rss [<!ENTITY x 'hello'>]>" + rss(item("a"))).toByteArray(Charsets.UTF_16), source)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsHtmlInsteadOfRss() { parse("<html><title>Podcast</title></html>") }
    @Test(expected = IllegalArgumentException::class) fun rejectsOversizedXml() { PodcastFeed.parse(ByteArray(PodcastFeed.MAX_BYTES + 1), source) }
    @Test fun httpsValidationPreservesEscapedPathsAndQueries() {
        assertEquals("https://example.org/a%20b?name=c%20d", publicHttps("https://EXAMPLE.org/a%20b?name=c%20d#part"))
    }
    @Test fun privateAndLocalFeedsAreRejected() {
        listOf("http://example.org/feed", "https://user:pass@example.org/feed", "https://example.org/private/feed", "https://example.org/feed?token=secret",
            "https://localhost/feed", "https://192.168.1.2/feed", "https://example.local/feed", "file:///feed").forEach {
            assertTrue(it, runCatching { publicFeedUrl(it) }.isFailure)
        }
    }
    @Test fun invalidDatesAndDurationsDoNotInventValues() {
        assertEquals(0L, podcastDate("tomorrow")); assertEquals(0L, podcastDuration("no duration"))
        assertEquals(90000L, podcastDuration("90")); assertEquals(0L, podcastDuration("-1:30"))
    }
    @Test fun filenamesNeverOverwriteAndDoNotEscapeFolder() {
        assertEquals("A_B (1).mp3", podcastFilename("A/B", "audio/mpeg") { it == "A_B.mp3" })
        assertEquals("Episodio.m4a", podcastFilename("...", "audio/mp4") { false })
    }
    @Test(expected = PodcastPreviewException::class) fun rejectsUnmarkedIvooxPreviewBasedOnActualAudioDuration() {
        requireCompletePodcast(540000L, 126354L)
    }
    @Test fun durationCheckAllowsSmallEncoderAndAdDifferences() {
        requireCompletePodcast(540000L, 530000L)
        requireCompletePodcast(0L, 126354L)
        requireCompletePodcast(540000L, 580000L)
    }

    @Test fun newsIncludesExactly72HoursButNotOlderFutureOrUndatedEpisodes() {
        val now = 2_000_000_000_000L
        val e = parse(rss(item("a"))).episodes.single()
        assertTrue(e.copy(published = now).isRecent(now))
        assertTrue(e.copy(published = now - PODCAST_NEWS_WINDOW_MS).isRecent(now))
        assertFalse(e.copy(published = now - PODCAST_NEWS_WINDOW_MS - 1).isRecent(now))
        assertFalse(e.copy(published = now + 1).isRecent(now))
        assertFalse(e.copy(published = 0).isRecent(now))
    }

    @Test fun discoveringOldArchiveDoesNotCreateNewsAndExistingBadgesExpire() {
        val fetched = parse(rss(item("a")))
        val published = fetched.episodes.single().published
        val previous = fetched.copy(episodes = emptyList())
        val fresh = mergePodcast(previous, fetched, published + 1)
        assertTrue(fresh.episodes.single().isNew)
        assertFalse(fresh.episodes.single().isUnseenRecent(published + PODCAST_NEWS_WINDOW_MS + 1))
        val aged = mergePodcast(fresh, fetched, published + PODCAST_NEWS_WINDOW_MS + 1)
        assertFalse(aged.episodes.single().isNew)
        assertEquals(1, aged.episodes.size) // Full program catalog is retained.
        assertFalse(mergePodcast(previous, fetched, published + PODCAST_NEWS_WINDOW_MS + 1).episodes.single().isNew)
    }
}
