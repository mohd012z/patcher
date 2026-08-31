package com.msa.patcher.modify.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceUiSettingsStoreTest {
    private class MemoryBackend : SettingsBackend {
        private val values = mutableMapOf<String, Any>()

        override fun getString(key: String, defaultValue: String): String =
            values[key] as? String ?: defaultValue

        override fun getInt(key: String, defaultValue: Int): Int =
            values[key] as? Int ?: defaultValue

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
            values[key] as? Boolean ?: defaultValue

        override fun putString(key: String, value: String) { values[key] = value }
        override fun putInt(key: String, value: Int) { values[key] = value }
        override fun putBoolean(key: String, value: Boolean) { values[key] = value }
    }

    @Test
    fun defaultsFavorCompactFullViewExperience() {
        val loaded = WorkspaceUiSettingsStore(MemoryBackend()).load()
        assertEquals(ButtonSize.SMALL, loaded.buttonSize)
        assertEquals(ButtonSpacing.TIGHT, loaded.buttonSpacing)
        assertEquals(ChromeSize.COMPACT, loaded.toolbarSize)
        assertEquals(WorkspaceViewModeSetting.FOCUS, loaded.defaultViewMode)
        assertTrue(loaded.autoHideUi)
        assertTrue(loaded.rememberZoomPerView)
    }

    @Test
    fun saveAndLoadRoundTripsAndNormalizesRanges() {
        val backend = MemoryBackend()
        val store = WorkspaceUiSettingsStore(backend)

        store.save(
            WorkspaceUiSettings(
                uiScalePercent = 999,
                fontSp = 99,
                editorFontSp = 1,
                buttonSize = ButtonSize.MEDIUM,
                lineNumbers = true,
                wordWrap = false
            )
        )

        val loaded = store.load()
        assertEquals(130, loaded.uiScalePercent)
        assertEquals(24, loaded.fontSp)
        assertEquals(9, loaded.editorFontSp)
        assertEquals(ButtonSize.MEDIUM, loaded.buttonSize)
        assertTrue(loaded.lineNumbers)
        assertFalse(loaded.wordWrap)
    }
}
