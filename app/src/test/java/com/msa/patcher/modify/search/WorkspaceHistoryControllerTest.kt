package com.msa.patcher.modify.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceHistoryControllerTest {
    @Test
    fun recentPathsAreMostRecentFirstAndBounded() {
        val history = WorkspaceHistoryController(maxRecent = 3)
        history.addRecent("a")
        history.addRecent("b")
        history.addRecent("a")
        history.addRecent("c")
        history.addRecent("d")
        assertEquals(listOf("d", "c", "a"), history.recent())
    }

    @Test
    fun favoritesToggleWithoutDuplicates() {
        val history = WorkspaceHistoryController()
        assertTrue(history.toggleFavorite("res/values/strings.xml"))
        assertTrue(history.isFavorite("res/values/strings.xml"))
        assertFalse(history.toggleFavorite("res/values/strings.xml"))
        assertFalse(history.isFavorite("res/values/strings.xml"))
        assertTrue(history.favorites().isEmpty())
    }

    @Test
    fun restoreNormalizesBlankAndDuplicatePaths() {
        val history = WorkspaceHistoryController(maxRecent = 3)
        history.restore(
            recents = listOf(" a ", "", "b", "a", "c", "d"),
            favorites = listOf(" x ", "x", "y")
        )
        assertEquals(listOf("a", "b", "c"), history.recent())
        assertEquals(listOf("x", "y"), history.favorites())
    }
}
