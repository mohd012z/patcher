package com.msa.patcher.analyze
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class ManifestAnalyzerTest {
 @Test fun parsesPlainManifest(){
   val xml="""<manifest package="com.example" android:versionName="1.2"><uses-permission android:name="android.permission.INTERNET"/><application><activity android:name=".Main" android:exported="true"/></application></manifest>"""
   val result=ManifestAnalyzer.analyze(xml.toByteArray())
   assertFalse(result.limited)
   assertTrue(result.findings.any{it.title=="Package" && it.detail=="com.example"})
   assertTrue(result.findings.any{it.title=="Permissions"})
 }
 @Test fun invalidBinaryIsLimited(){ assertTrue(ManifestAnalyzer.analyze(byteArrayOf(3,0,8,0,1,0,0,0)).limited) }
}
