package com.msa.patcher.modify.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceViewControllerTest {
    private val controller = WorkspaceViewController()

    @Test
    fun focusKeepsEditorAndHidesNavigatorInventory() {
        val state = controller.stateFor(WorkspaceViewMode.FOCUS)
        assertTrue(state.showEditor)
        assertFalse(state.showNavigator)
        assertFalse(state.showSnapshot)
        assertFalse(state.showInventory)
    }

    @Test
    fun splitShowsNavigatorSnapshotAndEditorWithoutLongInventory() {
        val state = controller.stateFor(WorkspaceViewMode.SPLIT)
        assertTrue(state.showEditor)
        assertTrue(state.showNavigator)
        assertTrue(state.showSnapshot)
        assertFalse(state.showInventory)
    }

    @Test
    fun inspectShowsNavigatorEditorAndInventory() {
        val state = controller.stateFor(WorkspaceViewMode.INSPECT)
        assertTrue(state.showEditor)
        assertTrue(state.showNavigator)
        assertFalse(state.showSnapshot)
        assertTrue(state.showInventory)
    }
}
