package com.ainalluna.michimusica.library

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.Normalizer

data class MarkdownPlaylistResult(
    val title: String,
    val entries: List<String>,
    val songs: List<Song>,
    val missing: List<String>,
    val duplicates: List<String>,
)

internal data class MarkdownPlaylistFilenameResult(
    val title: String,
    val entries: List<String>,
    val filenames: List<String>,
    val missing: List<String>,
    val duplicates: List<String>,
)

object MarkdownPlaylist {
    private val listItem = Regex("""^\s*(?:[-+*]|\d+[.)])\s+(.+?)\s*$""")
    private val heading = Regex("""^\s*#{1,6}\s+(.+?)\s*#*\s*$""")
    private val markdownLink = Regex("""^\[[^]]*]\((.+?)\)(?:\s.*)?$""")
    private val inlineCode = Regex("""^`([^`]+)`(?:\s.*)?$""")

    fun resolve(markdown: String, library: List<Song>): MarkdownPlaylistResult {
        val resolved = resolveFilenames(markdown, library.map(Song::filename))
        val songsByFilename = library.associateBy { comparable(it.filename) }
        return MarkdownPlaylistResult(
            title = resolved.title,
            entries = resolved.entries,
            songs = resolved.filenames.mapNotNull { songsByFilename[comparable(it)] },
            missing = resolved.missing,
            duplicates = resolved.duplicates,
        )
    }

    internal fun resolveFilenames(markdown: String, filenames: List<String>): MarkdownPlaylistFilenameResult {
        val lines = markdown.removePrefix("\uFEFF").lineSequence().toList()
        val title = lines.firstNotNullOfOrNull { heading.matchEntire(it)?.groupValues?.get(1)?.trim() }.orEmpty()
        val entries = lines.mapNotNull { line ->
            listItem.matchEntire(line)?.groupValues?.get(1)?.let(::decodeEntry)
        }.filter(String::isNotBlank)

        val byFilename = filenames.associateBy(::comparable)
        val byStem = filenames.groupBy { comparable(it.substringBeforeLast('.', it)) }
        val selected = mutableListOf<String>()
        val missing = mutableListOf<String>()
        val duplicates = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        entries.forEach { reference ->
            val exact = byFilename[comparable(reference)]
            val stemMatches = if (reference.substringAfterLast('.', "").isBlank()) byStem[comparable(reference)].orEmpty() else emptyList()
            val song = exact ?: stemMatches.singleOrNull()
            if (song == null) {
                missing += reference
            } else if (!seen.add(comparable(song))) {
                duplicates += reference
            } else {
                selected += song
            }
        }
        return MarkdownPlaylistFilenameResult(title, entries, selected, missing, duplicates)
    }

    private fun decodeEntry(raw: String): String {
        val withoutComment = raw.replace(Regex("""\s+<!--.*?-->\s*$"""), "").trim()
        val value = markdownLink.matchEntire(withoutComment)?.groupValues?.get(1)
            ?: inlineCode.matchEntire(withoutComment)?.groupValues?.get(1)
            ?: withoutComment.replace(Regex("""^\[[ xX]]\s*"""), "")
        val withoutTitle = value.replace(Regex("""\s+[\"'][^\"']*[\"']$"""), "")
        val decoded = runCatching { URLDecoder.decode(withoutTitle.trim('<', '>'), StandardCharsets.UTF_8.name()) }
            .getOrDefault(withoutTitle)
            .replace('\\', '/')
            .removePrefix("file://")
        return decoded.substringAfterLast('/')
    }

    private fun comparable(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFC).lowercase()
}
