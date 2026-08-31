package com.msa.patcher.analyze

enum class ScanMode { QUICK, DEEP }

data class ScanFinding(
    val category: String,
    val title: String,
    val detail: String,
    val confidence: String
)

data class ScanResult(
    val mode: ScanMode,
    val entriesScanned: Int,
    val dexCount: Int,
    val nativeCount: Int,
    val abis: Set<String>,
    val frameworkHints: Set<String>,
    val keywordHints: Set<String>,
    val findings: List<ScanFinding>,
    val durationMs: Long
) {
    val coverageLabel: String
        get() = if (mode == ScanMode.DEEP) "Deep static coverage" else "Quick static coverage"
}
