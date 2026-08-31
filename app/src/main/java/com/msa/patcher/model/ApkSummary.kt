package com.msa.patcher.model

data class ApkSummary(
    val fileName: String,
    val sizeBytes: Long,
    val sha256: String,
    val dexCount: Int,
    val nativeCount: Int,
    val abis: Set<String>,
    val embeddedDexCount: Int,
    val embeddedApkCount: Int,
    val signingPresent: Boolean
) {
    val sizeLabel: String get() = String.format("%.2f MB", sizeBytes / 1024.0 / 1024.0)
    val abiLabel: String get() = if (abis.isEmpty()) "—" else abis.sorted().joinToString(", ")
}
