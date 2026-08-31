package com.msa.patcher.evidence

object EvidenceScrollState {
    fun label(
        scrollY: Int,
        contentHeight: Int,
        viewportHeight: Int,
        evidenceCount: Int
    ): String {
        if (evidenceCount <= 0) return "Scroll 0% • Evidence 0/0"

        val maxScroll = (contentHeight - viewportHeight).coerceAtLeast(0)
        if (maxScroll == 0) return "Scroll 0% • Evidence 1/$evidenceCount"

        val clamped = scrollY.coerceIn(0, maxScroll)
        val percent = ((clamped.toDouble() / maxScroll.toDouble()) * 100.0)
            .toInt()
            .coerceIn(0, 100)

        val evidenceIndex = if (percent >= 100) {
            evidenceCount
        } else {
            1 + ((percent / 100.0) * (evidenceCount - 1)).toInt()
        }.coerceIn(1, evidenceCount)

        return "Scroll $percent% • Evidence $evidenceIndex/$evidenceCount"
    }
}
