package com.msa.patcher.analyze
import com.msa.patcher.model.ApkSummary
import org.junit.Assert.assertEquals
import org.junit.Test
class ArchitectureClassifierTest {
 private val base = ApkSummary("x.apk",1,"00",1,0,emptySet(),0,0,true)
 @Test fun memoryProcess() { assertEquals(ArchitectureFamily.MEMORY_PROCESS, ArchitectureClassifier.classify(base, setOf("/proc/self/maps","luaj"))) }
 @Test fun apkEngineering() { assertEquals(ArchitectureFamily.APK_ENGINEERING, ArchitectureClassifier.classify(base, setOf("apktool","aapt2"))) }
 @Test fun hybrid() { assertEquals(ArchitectureFamily.HYBRID, ArchitectureClassifier.classify(base, setOf("apktool","/proc/self/maps"))) }
}
