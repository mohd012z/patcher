package com.msa.patcher.analyze
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class NetworkAnalyzerTest {
    @Test fun deduplicatesAndKeepsSource() {
        val rows = NetworkAnalyzer.analyze(mapOf("a.dex" to "https://Example.com/api https://example.com/api", "b.xml" to "https://other.test/v1"))
        assertEquals(2, rows.size)
        assertTrue(rows.any { it.source == "a.dex" && it.detail.contains("Example.com") })
    }
}
