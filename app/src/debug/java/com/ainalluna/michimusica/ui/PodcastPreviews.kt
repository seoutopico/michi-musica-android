package com.ainalluna.michimusica.ui

import androidx.core.net.toUri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ainalluna.michimusica.library.AudioSection
import com.ainalluna.michimusica.podcasts.*

/** Production components, invented titles and no network/files. Native validation is separate. */
@Composable
internal fun PodcastPreviewContent(skin: MichiSkin = MichiSkin.MIDNIGHT, news: Boolean = false, empty: Boolean = false) {
    val nav = remember { PodcastNavigation().apply { tab = if (news) PodcastTab.NEWS else PodcastTab.FOLLOWING } }
    val episodes = listOf(
        PodcastEpisode("one", "Cómo cambia nuestra forma de escuchar", "Una conversación sobre música y memoria.", "https://example.org/a.mp3", "audio/mpeg", 1788609600000, 2460000, isNew = true),
        PodcastEpisode("two", "Un paseo por los sonidos de la noche", "", "https://example.org/b.mp3", "audio/mpeg", 1788523200000, 1860000),
    )
    val state = PodcastState(shows = if (empty) emptyList() else listOf(
        PodcastShow("https://example.org/feed", "Conversaciones de café", "Michi", "", episodes),
        PodcastShow("https://example.net/feed", "Ciencia para días tranquilos", "Michi", "", episodes.map { it.copy(isNew = false) }),
    ))
    MichiTheme(skin) {
        Scaffold(bottomBar = { HomeNavigation(0) {} }) { padding ->
            Box(Modifier.padding(padding)) {
                LibraryHome(emptyList(), null, false, true, false, null, null, 0, {}, {}, {}, {}, {}, {},
                    section = AudioSection.PODCASTS,
                    podcastHeader = { PodcastHeader(nav, state, false, true, "", {}, {}) },
                    podcastContent = { podcastItems(nav, state, emptyMap(), "content://preview/folder".toUri(), { _, _ -> }, {}, {}) })
            }
        }
    }
}

@Preview(name = "Podcasts · Siguiendo", widthDp = 412, heightDp = 915, showSystemUi = true)
@Composable private fun PodcastFollowingPreview() = PodcastPreviewContent()
@Preview(name = "Podcasts · Novedades", widthDp = 412, heightDp = 915, showSystemUi = true)
@Composable private fun PodcastNewsPreview() = PodcastPreviewContent(news = true)
@Preview(name = "Podcasts · Rosa · texto grande", widthDp = 360, heightDp = 800, fontScale = 1.3f, showSystemUi = true)
@Composable private fun PodcastRosePreview() = PodcastPreviewContent(MichiSkin.ROSE, news = true)
@Preview(name = "Podcasts · Sin programas", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable private fun PodcastEmptyPreview() = PodcastPreviewContent(empty = true)
