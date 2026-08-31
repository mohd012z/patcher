package com.msa.patcher.analyze

data class AnalysisCategory(val number: Int, val title: String, val state: String, val detail: String)

object AnalysisStateMapper {
    private val categoryAnalyzers = mapOf(
        "APK Structure" to setOf(AnalyzerNames.MANIFEST),
        "DEX & Code" to setOf(AnalyzerNames.DEX),
        "Native & JNI" to setOf(AnalyzerNames.NATIVE),
        "Resources & Build" to setOf(AnalyzerNames.RESOURCES),
        "SDK & Network" to setOf(AnalyzerNames.NETWORK),
        "Signing & Integrity" to setOf(AnalyzerNames.SIGNING),
        "Protection" to setOf(AnalyzerNames.HEURISTICS)
    )
    private val runtimeOnly = setOf("Memory & Process")

    fun map(result: ScanResult): List<AnalysisCategory> = AnalysisPlan.ALL.mapIndexed { index, title ->
        val matches = result.findings.filter { it.category == title }
        val required = categoryAnalyzers[title].orEmpty()
        val limited = required.any { it in result.analyzersLimited }
        val state = when {
            matches.isNotEmpty() && title !in setOf("Overview", "Report & Evidence") -> "FOUND"
            title == "Overview" || title == "Report & Evidence" -> "READY"
            title in runtimeOnly -> "LIMITED"
            limited -> "LIMITED"
            required.isNotEmpty() && required.all { it in result.analyzersRun } -> "CLEAN"
            title == "Hook & Runtime" || title == "Root & Virtualization" -> if (AnalyzerNames.HEURISTICS in result.analyzersRun) "CLEAN" else "LIMITED"
            else -> "LIMITED"
        }
        AnalysisCategory(index + 1, title, state, detailFor(title, state, matches, result))
    }

    private fun detailFor(title: String, state: String, matches: List<ScanFinding>, result: ScanResult): String {
        if (matches.isNotEmpty()) return buildString {
            append("Evidence: ${matches.size}\n\n")
            append(matches.joinToString("\n\n") { "[${it.confidence}] ${it.title}\nSource: ${it.source}\nType: ${it.evidenceType}\n${it.detail}" })
        }
        return when (title) {
            "Overview" -> "${result.mode.name} scan • ${result.entriesScanned} entries • ${result.findings.size} evidence records • ${result.sampledBytes / 1024} KiB sampled • ${result.durationMs} ms"
            "Report & Evidence" -> "Analyzers run: ${result.analyzersRun.sorted().joinToString()}\nLimited: ${result.analyzersLimited.sorted().ifEmpty { listOf("None") }.joinToString()}\nWarnings: ${result.warnings.size}"
            "Memory & Process" -> "LIMITED: runtime memory/process behaviour cannot be established by offline static analysis."
            else -> if (state == "LIMITED") "Coverage is limited for this category. ${result.warnings.joinToString(" ").take(800)}" else "Analyzer completed and no matching static evidence was found."
        }
    }
}
