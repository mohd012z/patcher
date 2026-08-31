package com.msa.patcher.analyze

object SigningAnalyzer {
    private val signatureRegex = Regex("^META-INF/.+\\.(RSA|DSA|EC|SF)$", RegexOption.IGNORE_CASE)

    fun analyze(entries: List<String>): List<ScanFinding> {
        val out = mutableListOf<ScanFinding>()
        val artifacts = entries.filter { signatureRegex.matches(it) || it.equals("META-INF/MANIFEST.MF", true) }
        if (artifacts.isNotEmpty()) {
            out += ScanFinding("Signing & Integrity", "JAR signing artifacts", artifacts.take(20).joinToString("\n"), "CONFIRMED", "META-INF/", EvidenceType.SIGNING)
        } else {
            out += ScanFinding("Signing & Integrity", "No JAR v1 signing artifacts observed", "APK may use APK Signature Scheme v2/v3/v4, which is outside this archive-entry inventory.", "WEAK", "APK", EvidenceType.SIGNING)
        }
        return out
    }
}
