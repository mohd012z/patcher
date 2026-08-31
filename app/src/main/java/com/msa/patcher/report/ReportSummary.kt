package com.msa.patcher.report

import com.msa.patcher.analyze.ScanResult

object ReportSummary {
    fun coveragePercent(result: ScanResult): Int {
        val total = result.analyzersRun.size.coerceAtLeast(1)
        val full = (total - result.analyzersLimited.size).coerceAtLeast(0)
        val base = (full * 100 / total)
        return if (result.mode.name == "DEEP") base.coerceAtLeast(55) else base.coerceAtMost(75)
    }

    fun format(result: ScanResult): String {
        val byConfidence = result.findings.groupingBy { it.confidence }.eachCount().toSortedMap()
        val network = result.findings.count { it.evidenceType.name == "NETWORK" }
        val native = result.findings.count { it.evidenceType.name == "NATIVE" }
        val manifest = result.findings.count { it.evidenceType.name == "MANIFEST" }
        return buildString {
            append("Mode: ${result.mode}\n")
            append("Entries inventoried: ${result.entriesScanned}\n")
            append("Static bytes sampled: ${result.sampledBytes}\n")
            append("DEX: ${result.dexCount}\nNative libraries: ${result.nativeCount}\n")
            append("ABI: ${if (result.abis.isEmpty()) "—" else result.abis.joinToString()}\n")
            append("Analyzers run: ${result.analyzersRun.sorted().joinToString()}\n")
            append("Limited analyzers: ${result.analyzersLimited.sorted().ifEmpty { listOf("None") }.joinToString()}\n")
            append("Evidence records: ${result.findings.size}\n")
            append("Confidence totals: ${byConfidence.entries.joinToString { "${it.key}=${it.value}" }}\n")
            append("Manifest evidence: $manifest • Network endpoints: $network • Native evidence: $native\n")
            if (result.warnings.isNotEmpty()) append("\nWarnings:\n${result.warnings.joinToString("\n") { "• $it" }}\n")
            append("\nStatic-analysis boundary: this report does not prove runtime behaviour and does not execute or modify the selected APK.")
        }
    }
}
