package com.ainalluna.michimusica.lyrics

data class TimedLyricLine(val timeMs: Long, val text: String)

object LrcParser {
    private val timestamp = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")

    fun parse(content: String): List<TimedLyricLine> = content.lineSequence()
        .flatMap { line ->
            val text = timestamp.replace(line, "").trim()
            if (text.isBlank()) emptySequence() else timestamp.findAll(line).map { match ->
                val minutes = match.groupValues[1].toLong()
                val seconds = match.groupValues[2].toLong()
                val fraction = match.groupValues[3]
                val milliseconds = when (fraction.length) {
                    1 -> fraction.toLongOrNull()?.times(100L) ?: 0L
                    2 -> fraction.toLongOrNull()?.times(10L) ?: 0L
                    else -> fraction.take(3).padEnd(3, '0').toLongOrNull() ?: 0L
                }
                TimedLyricLine((minutes * 60L + seconds) * 1_000L + milliseconds, text)
            }
        }
        .sortedBy(TimedLyricLine::timeMs)
        .toList()

    fun activeIndex(lines: List<TimedLyricLine>, positionMs: Long): Int =
        lines.indexOfLast { it.timeMs <= positionMs }
}
