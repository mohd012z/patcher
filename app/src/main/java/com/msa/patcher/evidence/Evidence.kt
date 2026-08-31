package com.msa.patcher.evidence

enum class EvidenceLevel { CONFIRMED, STRONG, MEDIUM, WEAK, REJECTED }
data class RawEvidence(val subject: String, val source: String, val detail: String)
data class Evidence(val level: EvidenceLevel, val subject: String, val source: String, val detail: String)
