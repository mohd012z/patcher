package com.msa.patcher

import org.junit.Assert.assertEquals
import org.junit.Test

class AppIdentityTest {
    @Test
    fun versionIsV81() {
        assertEquals("8.1", BuildConfig.VERSION_NAME)
    }
}