package com.msa.patcher.evidence
import com.msa.patcher.analyze.*
import org.junit.Assert.assertTrue
import org.junit.Test
class EvidenceFormatterTest {
 @Test fun includesSourceAndType(){ val s=EvidenceFormatter.format(listOf(ScanFinding("DEX & Code","x","d","CONFIRMED","classes.dex",EvidenceType.DEX))); assertTrue(s.contains("classes.dex")); assertTrue(s.contains("DEX")) }
}
