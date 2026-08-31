package com.msa.patcher.report
import com.msa.patcher.analyze.*
import org.junit.Assert.assertTrue
import org.junit.Test
class ReportSummaryTest {
 @Test fun reportsCoverageAndWarnings(){
   val r=ScanResult(ScanMode.DEEP,10,1,1,setOf("arm64-v8a"),emptySet(),emptySet(),emptyList(),10,500,setOf(AnalyzerNames.DEX,AnalyzerNames.SIGNING),setOf(AnalyzerNames.SIGNING),listOf("limited signing"))
   val s=ReportSummary.format(r); assertTrue(s.contains("limited signing")); assertTrue(s.contains("Analyzers run"))
 }
}
