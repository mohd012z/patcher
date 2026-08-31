package com.msa.patcher.analyze

object ResourceAnalyzer {
    fun analyze(entries: List<String>, textBySource: Map<String, String>): List<ScanFinding> {
        val out = mutableListOf<ScanFinding>()
        val assetCount = entries.count { it.startsWith("assets/") && !it.endsWith('/') }
        val resCount = entries.count { it.startsWith("res/") && !it.endsWith('/') }
        if (assetCount > 0 || resCount > 0) {
            out += ScanFinding("Resources & Build", "Resource inventory", "res files=$resCount, asset files=$assetCount", "CONFIRMED", "APK archive", EvidenceType.RESOURCE)
        }
        val markers = listOf(
            Triple("Flutter", listOf("flutter_assets/", "libflutter.so"), "STRONG"),
            Triple("Unity / IL2CPP", listOf("libil2cpp.so", "assets/bin/Data/"), "STRONG"),
            Triple("React Native", listOf("assets/index.android.bundle", "libreactnative"), "STRONG")
        )
        val joinedEntries = entries.joinToString("\n")
        markers.forEach { (name, needles, confidence) ->
            val hits = needles.filter { joinedEntries.contains(it, true) }
            if (hits.isNotEmpty()) out += ScanFinding("Resources & Build", "$name framework markers", hits.joinToString(), confidence, "APK archive", EvidenceType.RESOURCE)
        }
        val configSources = textBySource.keys.filter { src ->
            val n = src.lowercase()
            n.endsWith(".json") || n.endsWith(".xml") || n.endsWith(".properties") || n.endsWith(".txt") || n.endsWith(".yaml") || n.endsWith(".yml")
        }
        if (configSources.isNotEmpty()) {
            out += ScanFinding("Resources & Build", "Readable configuration files", "${configSources.size} sampled config/text resources.", "CONFIRMED", configSources.take(8).joinToString(), EvidenceType.RESOURCE)
        }
        return out
    }
}
