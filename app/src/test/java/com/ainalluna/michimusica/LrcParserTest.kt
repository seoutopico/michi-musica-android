package com.ainalluna.michimusica

import com.ainalluna.michimusica.lyrics.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Test

class LrcParserTest {
    @Test
    fun parsesSortsAndSelectsTheActiveLine() {
        val lines = LrcParser.parse("[00:12.50]Segunda\n[00:01.2]Primera\n[ar:Michi]")
        assertEquals(listOf("Primera", "Segunda"), lines.map { it.text })
        assertEquals(1_200L, lines.first().timeMs)
        assertEquals(0, LrcParser.activeIndex(lines, 12_499L))
        assertEquals(1, LrcParser.activeIndex(lines, 12_500L))
    }

    @Test
    fun supportsSeveralTimestampsForOneLine() {
        val lines = LrcParser.parse("[00:01.00][00:03.000]Otra vez")
        assertEquals(listOf(1_000L, 3_000L), lines.map { it.timeMs })
    }
}
