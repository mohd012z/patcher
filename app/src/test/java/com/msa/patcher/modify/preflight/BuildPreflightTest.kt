package com.msa.patcher.modify.preflight

import org.junit.Assert.*
import org.junit.Test

class BuildPreflightTest {
    @Test fun blocksMissingWorkspace() {
        val r = BuildPreflight.check(false, 0, 0)
        assertFalse(r.ready)
        assertTrue(r.errors.isNotEmpty())
    }

    @Test fun allowsValidWorkspaceWithWarning() {
        val r = BuildPreflight.check(true, 10, 1)
        assertTrue(r.ready)
        assertTrue(r.warnings.any { it.contains("unsigned", true) })
    }
}
