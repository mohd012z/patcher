package com.msa.patcher.analyze

object ProtectionAnalyzer {
    fun analyze(entries: List<String>, textBySource: Map<String, String>): List<ScanFinding> {
        val allEntries = entries.joinToString("\n").lowercase()
        val allText = textBySource.values.joinToString("\n").lowercase()
        val rules = listOf(
            Triple("Common shell/packer markers", listOf("libjiagu", "ijiami", "bangcle", "secexe", "libshell", "com.stub"), "MEDIUM"),
            Triple("Obfuscation/minification indicators", listOf("proguard", "r8/", "mapping.txt"), "WEAK")
        )
        return rules.mapNotNull { (title, needles, confidence) ->
            val hits = needles.filter { allEntries.contains(it) || allText.contains(it) }
            if (hits.isEmpty()) null else ScanFinding("Protection", title, "Static markers: ${hits.joinToString()}", confidence, "APK static data", EvidenceType.HEURISTIC)
        }
    }
}
