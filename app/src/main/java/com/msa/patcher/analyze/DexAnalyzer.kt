package com.msa.patcher.analyze

object DexAnalyzer {
    data class DexHeader(
        val strings: Long, val types: Long, val protos: Long,
        val fields: Long, val methods: Long, val classes: Long
    )

    fun analyze(
        dexBytesBySource: Map<String, ByteArray>,
        textBySource: Map<String, String>
    ): List<ScanFinding> {
        val out = mutableListOf<ScanFinding>()
        dexBytesBySource.forEach { (source, bytes) ->
            parseHeader(bytes)?.let { h ->
                out += ScanFinding(
                    "DEX & Code", "DEX header metadata",
                    "strings=${h.strings}, types=${h.types}, protos=${h.protos}, fields=${h.fields}, methods=${h.methods}, classes=${h.classes}",
                    "CONFIRMED", source, EvidenceType.DEX
                )
            }
        }
        if (dexBytesBySource.size > 1) {
            out += ScanFinding("DEX & Code", "Multidex application", "${dexBytesBySource.size} DEX files detected.", "CONFIRMED", "classes*.dex", EvidenceType.DEX)
        }

        val rules = listOf(
            Triple("Reflection / dynamic class loading", listOf("java/lang/reflect", "dalvik/system/DexClassLoader", "dalvik/system/PathClassLoader"), "MEDIUM"),
            Triple("WebView usage", listOf("android/webkit/WebView", "addJavascriptInterface", "loadUrl"), "MEDIUM"),
            Triple("Dynamic code loading", listOf("DexClassLoader", "InMemoryDexClassLoader", "System.loadLibrary"), "STRONG"),
            Triple("Cryptography APIs", listOf("javax/crypto", "java/security/MessageDigest", "Cipher.getInstance"), "MEDIUM")
        )
        textBySource.forEach { (source, text) ->
            rules.forEach { (title, needles, confidence) ->
                val hits = needles.filter { text.contains(it, ignoreCase = true) }
                if (hits.isNotEmpty()) {
                    out += ScanFinding("DEX & Code", title, "Indicators: ${hits.joinToString()}", confidence, source, EvidenceType.DEX)
                }
            }
        }
        return out.distinctBy { listOf(it.category, it.title, it.detail, it.source) }
    }

    fun parseHeader(bytes: ByteArray): DexHeader? {
        if (bytes.size < 112) return null
        if (!(bytes[0] == 'd'.code.toByte() && bytes[1] == 'e'.code.toByte() && bytes[2] == 'x'.code.toByte() && bytes[3] == '\n'.code.toByte())) return null
        return DexHeader(
            strings = u32(bytes, 56), types = u32(bytes, 64), protos = u32(bytes, 72),
            fields = u32(bytes, 80), methods = u32(bytes, 88), classes = u32(bytes, 96)
        )
    }

    private fun u32(bytes: ByteArray, offset: Int): Long {
        return (bytes[offset].toLong() and 0xff) or
            ((bytes[offset + 1].toLong() and 0xff) shl 8) or
            ((bytes[offset + 2].toLong() and 0xff) shl 16) or
            ((bytes[offset + 3].toLong() and 0xff) shl 24)
    }
}
