package com.msa.patcher.evidence

object EvidenceCorrelator {
    fun correlate(raw: List<RawEvidence>): List<Evidence> = raw.map { item ->
        val s = "${item.subject} ${item.detail}".lowercase()
        val rejected = listOf("friday", "keysexposed", "navexposed", "sweepangle").any { s.contains(it) } ||
            (s.contains("hiddenapibypass") && item.subject.equals("LSPosed module", true))
        val exact = listOf("xposedbridge", "ixposedhookloadpackage", "lsplant", "dobbyhook", "/system/bin/su", "/proc/self/maps").any { s.contains(it) }
        val correlated = item.source.contains("native", true) || item.source.contains("class", true) || item.source.contains("asset", true)
        val level = when {
            rejected -> EvidenceLevel.REJECTED
            exact && correlated -> EvidenceLevel.CONFIRMED
            exact -> EvidenceLevel.STRONG
            correlated -> EvidenceLevel.MEDIUM
            else -> EvidenceLevel.WEAK
        }
        Evidence(level, item.subject, item.source, item.detail)
    }
}
