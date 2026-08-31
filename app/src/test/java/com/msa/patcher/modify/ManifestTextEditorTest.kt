package com.msa.patcher.modify

import org.junit.Assert.*
import org.junit.Test

class ManifestTextEditorTest {
    @Test fun updatesDirectPlaintextMetadata() {
        val xml = """<manifest xmlns:android="http://schemas.android.com/apk/res/android" android:versionCode="1" android:versionName="1.0"><application android:label="Demo"></application></manifest>"""
        val result = ManifestTextEditor.update(xml, "2.0", 2, "New Demo")
        assertTrue(result.changed)
        assertTrue(result.text.contains("""android:versionName="2.0""""))
        assertTrue(result.text.contains("""android:versionCode="2""""))
        assertTrue(result.text.contains("""android:label="New Demo""""))
    }

    @Test fun refusesResourceReferenceLabelRewrite() {
        val xml = """<manifest><application android:label="@string/app_name"></application></manifest>"""
        val result = ManifestTextEditor.update(xml, appLabel = "New")
        assertFalse(result.changed)
        assertTrue(result.text.contains("@string/app_name"))
    }

    @Test fun binaryLikeManifestIsNotPlaintext() {
        assertFalse(ManifestTextEditor.isPlaintext(byteArrayOf(0, 3, 8, 0, 1)))
    }
}
