package com.msa.patcher.modify.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandHubControllerTest {
    @Test
    fun commandHubContainsCoreActionsWithoutDuplicates() {
        val actions = CommandHubController().visibleActions()

        assertTrue(actions.contains(CommandHubAction.SEARCH))
        assertTrue(actions.contains(CommandHubAction.CONVERTER))
        assertTrue(actions.contains(CommandHubAction.AI))
        assertTrue(actions.contains(CommandHubAction.BUILD))
        assertTrue(actions.contains(CommandHubAction.MANIFEST))
        assertEquals(actions.size, actions.distinct().size)
    }

    @Test
    fun stagedActionsAreClearlyMarked() {
        val controller = CommandHubController()
        assertEquals("Crypto Lab • staged", controller.titleFor(CommandHubAction.CRYPTO))
        assertEquals("Search", controller.titleFor(CommandHubAction.SEARCH))
    }
}
