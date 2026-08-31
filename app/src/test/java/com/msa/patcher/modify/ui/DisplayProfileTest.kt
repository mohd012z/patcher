package com.msa.patcher.modify.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayProfileTest {
    @Test
    fun smallPortraitUsesCompactDensity() {
        val p = DisplayProfile.from(widthDp = 360, heightDp = 740, density = 3f)
        assertEquals(UiDensity.COMPACT, p.defaultDensity)
        assertEquals(LayoutMode.PORTRAIT, p.layoutMode)
    }

    @Test
    fun landscapePhoneUsesLandscapeMode() {
        val p = DisplayProfile.from(widthDp = 740, heightDp = 360, density = 3f)
        assertEquals(LayoutMode.LANDSCAPE, p.layoutMode)
    }

    @Test
    fun tabletUsesNormalDensity() {
        val p = DisplayProfile.from(widthDp = 900, heightDp = 1280, density = 2f)
        assertEquals(UiDensity.NORMAL, p.defaultDensity)
        assertEquals(LayoutMode.TABLET, p.layoutMode)
    }
}
