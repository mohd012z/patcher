package com.msa.patcher.modify.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaintextReplacePreviewTest {
    @Test
    fun literalReplaceCountsMatchesAndKeepsReplacementLiteral() {
        val result = PlaintextReplacePreview.build("a.b a.b", "a.b", "$1", ignoreCase = false)
        assertEquals(2, result.matchCount)
        assertEquals("$1 $1", result.resultText)
        assertTrue(result.changed)
    }

    @Test
    fun ignoreCaseReplacesAllLiteralMatches() {
        val result = PlaintextReplacePreview.build("hello HELLO world", "hello", "hi", ignoreCase = true)
        assertEquals(2, result.matchCount)
        assertEquals("hi hi world", result.resultText)
    }

    @Test
    fun noMatchProducesUnchangedPreview() {
        val result = PlaintextReplacePreview.build("abc", "z", "x")
        assertEquals(0, result.matchCount)
        assertFalse(result.changed)
        assertEquals("abc", result.resultText)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyFindIsRejected() {
        PlaintextReplacePreview.build("abc", "", "x")
    }
}
