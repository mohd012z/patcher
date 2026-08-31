package com.msa.patcher.analyze

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalysisStateMapperTest {
    @Test fun mapsFindingsToCategoryStatesAndDetails() {
        val result = ScanResult(ScanMode.DEEP,120,2,3,setOf("arm64-v8a"),setOf("Flutter"),setOf("OkHttp"),listOf(ScanFinding("Native & JNI","Native libraries","3 native libraries found","STRONG"),ScanFinding("SDK & Network","OkHttp","Static OkHttp indicator","MEDIUM")),250)
        val rows = AnalysisStateMapper.map(result)
        assertEquals(AnalysisPlan.ALL.size, rows.size)
        assertEquals("FOUND", rows.first { it.title == "Native & JNI" }.state)
        assertTrue(rows.first { it.title == "Native & JNI" }.detail.contains("3 native libraries"))
        assertEquals("FOUND", rows.first { it.title == "SDK & Network" }.state)
        assertEquals("READY", rows.first { it.title == "Overview" }.state)
        assertEquals("CLEAN", rows.first { it.title == "Hook & Runtime" }.state)
    }
    @Test fun quickScanMarksDeepOnlyCategoriesLimited() {
        val result = ScanResult(ScanMode.QUICK,10,1,0,emptySet(),emptySet(),emptySet(),emptyList(),20)
        val rows = AnalysisStateMapper.map(result)
        assertEquals("LIMITED", rows.first { it.title == "Memory & Process" }.state)
        assertEquals("LIMITED", rows.first { it.title == "Hook & Runtime" }.state)
        assertEquals("READY", rows.first { it.title == "Report & Evidence" }.state)
    }
}
