package com.msa.patcher.modify.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomToolbarControllerTest {

    @Test
    fun focusModeUsesCompactFocusLabel() {
        val controller = BottomToolbarController()
        assertEquals("Focus", controller.viewLabel(WorkspaceViewMode.FOCUS))
    }

    @Test
    fun splitModeUsesCompactSplitLabel() {
        val controller = BottomToolbarController()
        assertEquals("Split", controller.viewLabel(WorkspaceViewMode.SPLIT))
    }

    @Test
    fun inspectModeUsesCompactInspectLabel() {
        val controller = BottomToolbarController()
        assertEquals("Inspect", controller.viewLabel(WorkspaceViewMode.INSPECT))
    }

    @Test
    fun commandHubUsesCompactToolsLabel() {
        val controller = BottomToolbarController()
        assertEquals("\u26A1 Tools", controller.commandHubLabel())
    }
}
