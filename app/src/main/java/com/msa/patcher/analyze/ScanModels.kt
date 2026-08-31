package com.msa.patcher.analyze

enum class ScanMode { QUICK, DEEP }
enum class EvidenceType { INVENTORY, MANIFEST, DEX, RESOURCE, NETWORK, NATIVE, SIGNING, HEURISTIC }

data class ScanFinding(
    val category: String,
    val title: String,
    val detail: String,
    val confidence: String,
    val source: String = "APK",
    val evidenceType: EvidenceType = EvidenceType.HEURISTIC
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
    val durationMs: Long,
    val sampledBytes: Int = 0,
    val analyzersRun: Set<String> = emptySet(),
    val analyzersLimited: Set<String> = emptySet(),
    val warnings: List<String> = emptyList()
) {
    val coverageLabel: String
        get() = if (mode == ScanMode.DEEP) "Deep static coverage" else "Quick static coverage"
}

object AnalyzerNames {
    const val MANIFEST = "Manifest"
    const val DEX = "DEX"
    const val RESOURCES = "Resources"
    const val NETWORK = "Network"
    const val NATIVE = "Native"
    const val SIGNING = "Signing"
    const val HEURISTICS = "Heuristics"
}
