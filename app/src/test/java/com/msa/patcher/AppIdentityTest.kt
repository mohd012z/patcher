package com.msa.patcher
import org.junit.Assert.assertEquals
import org.junit.Test
class AppIdentityTest { @Test fun versionIsV84() { assertEquals("8.4", BuildConfig.VERSION_NAME) } }
