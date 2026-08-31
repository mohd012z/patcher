package com.msa.patcher.analyze
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class AnalysisStateMapperTest {
 @Test fun mapsSourceAwareEvidence(){
   val r=ScanResult(ScanMode.DEEP,100,1,0,emptySet(),emptySet(),emptySet(),listOf(ScanFinding("DEX & Code","DEX header metadata","classes=5","CONFIRMED","classes.dex",EvidenceType.DEX)),50,1000,setOf(AnalyzerNames.DEX,AnalyzerNames.NETWORK,AnalyzerNames.NATIVE,AnalyzerNames.RESOURCES,AnalyzerNames.MANIFEST,AnalyzerNames.SIGNING,AnalyzerNames.HEURISTICS),setOf(AnalyzerNames.SIGNING),emptyList())
   val rows=AnalysisStateMapper.map(r)
   val dex=rows.first{it.title=="DEX & Code"}
   assertEquals("FOUND",dex.state)
   assertTrue(dex.detail.contains("classes.dex"))
   assertEquals("LIMITED",rows.first{it.title=="Signing & Integrity"}.state.takeIf{ r.findings.none{f->f.category=="Signing & Integrity"} } ?: "LIMITED")
 }
}
