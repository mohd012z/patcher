package com.msa.patcher.evidence

import com.msa.patcher.analyze.ScanFinding

object EvidenceFormatter {
    fun format(findings: List<ScanFinding>): String {
        if (findings.isEmpty()) return "No static evidence records were produced."
        return findings.joinToString("\n\n") {
            "[${it.confidence}] ${it.category}\n${it.title}\nSource: ${it.source}\nType: ${it.evidenceType}\n${it.detail}"
        }
    }
}
