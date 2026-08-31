package com.msa.patcher.evidence
import org.junit.Assert.assertEquals
import org.junit.Test
class EvidenceCorrelatorTest {
 @Test fun rejectsFridayAsFrida() { assertEquals(EvidenceLevel.REJECTED, EvidenceCorrelator.correlate(listOf(RawEvidence("Frida","string","FRIDAY"))).single().level) }
 @Test fun rejectsKeysexposed() { assertEquals(EvidenceLevel.REJECTED, EvidenceCorrelator.correlate(listOf(RawEvidence("Xposed","string","keysexposed"))).single().level) }
 @Test fun rejectsSweepAnglePangle() { assertEquals(EvidenceLevel.REJECTED, EvidenceCorrelator.correlate(listOf(RawEvidence("Pangle","string","sweepAngle"))).single().level) }
}
