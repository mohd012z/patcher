package com.msa.patcher.analyze

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanHeuristicsTest {
    @Test
    fun inventoriesDexNativeAbiAndFlutter() {
        val inventory = ScanHeuristics.inventory(listOf(
            "classes.dex",
            "classes2.dex",
            "lib/arm64-v8a/libflutter.so",
            "assets/flutter_assets/NOTICES.Z"
        ))
        assertEquals(2, inventory.dexCount)
        assertEquals(1, inventory.nativeCount)
        assertTrue("arm64-v8a" in inventory.abis)
        assertTrue("Flutter" in inventory.frameworkHints)
    }

    @Test
    fun rejectsKnownSubstringFalsePositives() {
        val hints = ScanHeuristics.keywordHints("FRIDAY sweepAngle keysexposed navexposed")
        assertFalse("Frida" in hints)
        assertFalse("Xposed" in hints)
    }

    @Test
    fun detectsExplicitRuntimeAndNetworkIndicators() {
        val hints = ScanHeuristics.keywordHints("libfrida-gadget.so de.robv.android.xposed okhttp3 retrofit2")
        assertTrue("Frida" in hints)
        assertTrue("Xposed" in hints)
        assertTrue("OkHttp" in hints)
        assertTrue("Retrofit" in hints)
    }
}
