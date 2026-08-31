package com.msa.patcher.analyze
import org.junit.Assert.assertTrue
import org.junit.Test
class NativeAnalyzerTest {
 @Test fun extractsAbiAndJni(){
   val rows=NativeAnalyzer.analyze(listOf("lib/arm64-v8a/libx.so"), mapOf("lib/arm64-v8a/libx.so" to "JNI_OnLoad dlopen"))
   assertTrue(rows.any{it.title.contains("arm64-v8a")})
   assertTrue(rows.any{it.title=="JNI exports"})
 }
}
