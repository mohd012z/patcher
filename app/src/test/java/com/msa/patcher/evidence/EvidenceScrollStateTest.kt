package com.msa.patcher.evidence

import org.junit.Assert.assertEquals
import org.junit.Test

class EvidenceScrollStateTest {
    @Test
    fun reportsTopMiddleAndBottomPositions() {
        assertEquals(
            "Scroll 0% • Evidence 1/10",
            EvidenceScrollState.label(0, 1200, 200, 10)
        )
        assertEquals(
            "Scroll 50% • Evidence 5/10",
            EvidenceScrollState.label(500, 1200, 200, 10)
        )
        assertEquals(
            "Scroll 100% • Evidence 10/10",
            EvidenceScrollState.label(1000, 1200, 200, 10)
        )
    }

    @Test
    fun clampsOutOfRangeScrollAndHandlesNoEvidence() {
        assertEquals(
            "Scroll 0% • Evidence 1/4",
            EvidenceScrollState.label(-50, 1000, 250, 4)
        )
        assertEquals(
            "Scroll 100% • Evidence 4/4",
            EvidenceScrollState.label(5000, 1000, 250, 4)
        )
        assertEquals(
            "Scroll 0% • Evidence 0/0",
            EvidenceScrollState.label(0, 100, 200, 0)
        )
    }
}
