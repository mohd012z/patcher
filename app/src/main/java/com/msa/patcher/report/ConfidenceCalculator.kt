package com.msa.patcher.report

data class ConfidenceScores(val analysisCoverage: Int, val behaviourConfidence: Int)
object ConfidenceCalculator {
    fun calculate(scannedSurfaces: Int, totalSurfaces: Int, confirmed: Int, strong: Int, medium: Int, weak: Int): ConfidenceScores {
        val coverage = if (totalSurfaces <= 0) 0 else ((scannedSurfaces * 100.0) / totalSurfaces).toInt().coerceIn(0, 100)
        val denom = confirmed + strong + medium + weak
        val behaviour = if (denom <= 0) 0 else (((confirmed * 100) + (strong * 80) + (medium * 55) + (weak * 20)) / denom).coerceIn(0, 100)
        return ConfidenceScores(coverage, behaviour)
    }
}
