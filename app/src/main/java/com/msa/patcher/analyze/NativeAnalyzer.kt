package com.msa.patcher.analyze

object NativeAnalyzer {
    private val libPattern = Regex("^lib/([^/]+)/([^/]+\\.so)$", RegexOption.IGNORE_CASE)

    fun analyze(entries: List<String>, textBySource: Map<String, String>): List<ScanFinding> {
        val libs = entries.mapNotNull { path -> libPattern.matchEntire(path)?.let { it.groupValues[1] to (it.groupValues[2] to path) } }
        if (libs.isEmpty()) return emptyList()
        val out = mutableListOf<ScanFinding>()
        val byAbi = libs.groupBy { it.first }
        byAbi.forEach { (abi, items) ->
            out += ScanFinding("Native & JNI", "Native ABI: $abi", "${items.size} native libraries: ${items.joinToString { it.second.first }}", "CONFIRMED", "lib/$abi/", EvidenceType.NATIVE)
        }
        val markerRules = mapOf(
            "JNI exports" to listOf("JNI_OnLoad", "Java_"),
            "Native dynamic linking" to listOf("dlopen", "dlsym"),
            "Runtime instrumentation strings" to listOf("frida", "gum-js-loop", "xposed")
        )
        textBySource.filterKeys { it.endsWith(".so", true) }.forEach { (source, text) ->
            markerRules.forEach { (title, markers) ->
                val hits = markers.filter { text.contains(it, true) }
                if (hits.isNotEmpty()) out += ScanFinding("Native & JNI", title, "Static symbols/strings: ${hits.joinToString()}", if (title == "JNI exports") "STRONG" else "MEDIUM", source, EvidenceType.NATIVE)
            }
        }
        return out.distinctBy { listOf(it.title, it.detail, it.source) }
    }
}
