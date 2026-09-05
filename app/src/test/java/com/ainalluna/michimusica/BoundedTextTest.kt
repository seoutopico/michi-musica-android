package com.ainalluna.michimusica

import com.ainalluna.michimusica.security.readBoundedText
import java.io.Reader
import org.junit.Assert.*
import org.junit.Test

class BoundedTextTest {
    @Test fun acceptsExactlyTheLimitAndEmptyInput() {
        assertEquals("abc", "abc".reader().readBoundedText(3))
        assertEquals("", "".reader().readBoundedText(3))
    }
    @Test fun stopsAnUnboundedProviderAfterOnlyOneExtraCharacter() {
        var consumed = 0
        val endless = object : Reader() {
            override fun read(target: CharArray, offset: Int, length: Int): Int {
                target.fill('a', offset, offset + length); consumed += length; return length
            }
            override fun close() = Unit
        }
        assertThrows(IllegalArgumentException::class.java) { endless.readBoundedText(5000) }
        assertEquals(5001, consumed)
    }
}
