package com.msa.patcher.analyze
import org.junit.Assert.assertTrue
import org.junit.Test
class SigningAnalyzerTest {
 @Test fun inventoriesV1Artifacts(){ val r=SigningAnalyzer.analyze(listOf("META-INF/CERT.RSA","META-INF/CERT.SF")); assertTrue(r.first().detail.contains("CERT.RSA")) }
}
