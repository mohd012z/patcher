package com.msa.patcher.analyze

data class AnalysisCategory(
    val number: Int,
    val title: String,
    val state: String,
    val detail: String
)

object AnalysisStateMapper {
    private val quickLimited = setOf(
        "Resources & Build", "Root & Virtualization", "Hook & Runtime", "Memory & Process"
    )

    fun map(result: ScanResult): List<AnalysisCategory> = AnalysisPlan.ALL.mapIndexed { index, title ->
        val matches = result.findings.filter { it.category == title }
        val state = when {
            matches.isNotEmpty() -> "FOUND"
            title == "Overview" || title == "DEX & Code" || title == "Report & Evidence" -> "READY"
            title == "Signing & Integrity" || title == "Memory & Process" || title == "Protection" -> "LIMITED"
            result.mode == ScanMode.QUICK && title in quickLimited -> "LIMITED"
            else -> "CLEAN"
        }
        AnalysisCategory(index + 1, title, state, detailFor(title, state, matches, result))
    }

    private fun detailFor(title: String, state: String, matches: List<ScanFinding>, result: ScanResult): String {
        if (matches.isNotEmpty()) return matches.joinToString("\n\n") { "[${it.confidence}] ${it.title}\n${it.detail}" }
        return when (title) {
            "Overview" -> "${result.mode.name} scan • ${result.entriesScanned} entries • ${result.findings.size} findings • ${result.durationMs} ms"
            "DEX & Code" -> "DEX files detected: ${result.dexCount}. This build inventories DEX files and printable static strings; it does not decompile code."
            "Signing & Integrity" -> "LIMITED: signature/certificate verification is not implemented in this static scanner yet."
            "Memory & Process" -> "LIMITED: runtime memory/process behaviour requires runtime evidence and is not inferred from static strings."
            "Protection" -> "LIMITED: protection/obfuscation classification is not implemented as a dedicated engine yet."
            "Report & Evidence" -> "Analysis rows are derived from the current scan result. Open Evidence and Report for the complete static findings."
            else -> if (state == "LIMITED") "LIMITED in ${result.mode.name} mode. Run Deep Scan for broader static coverage." else "No static indicators were found for this category in the current scan."
        }
    }
}
