package com.msa.patcher.analyze
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class DexAnalyzerTest {
    @Test fun parsesHeaderAndIndicators() {
        val b = ByteArray(112); "dex\n035\u0000".toByteArray().copyInto(b)
        put32(b,56,10); put32(b,64,20); put32(b,72,3); put32(b,80,4); put32(b,88,99); put32(b,96,7)
        val h = DexAnalyzer.parseHeader(b)!!
        assertEquals(99, h.methods)
        assertEquals(7, h.classes)
        val rows = DexAnalyzer.analyze(mapOf("classes.dex" to b), mapOf("classes.dex" to "dalvik/system/DexClassLoader android/webkit/WebView"))
        assertTrue(rows.any { it.title == "DEX header metadata" })
        assertTrue(rows.any { it.title == "WebView usage" })
    }
    private fun put32(b:ByteArray,o:Int,v:Int){ for(i in 0..3) b[o+i]=((v ushr (8*i)) and 255).toByte() }
}
