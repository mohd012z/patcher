package com.msa.patcher.modify

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkspaceSectionTest {
    @Test fun orderedSectionsAreStable() {
        assertEquals(
            listOf("Files", "Manifest", "Search", "Converter", "Code Tools", "Diff", "Build"),
            WorkspaceSection.values().map { it.title }
        )
    }
}
