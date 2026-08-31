package com.msa.patcher
import org.junit.Assert.assertEquals
import org.junit.Test
class AppIdentityTest { @Test fun versionIsV83() { assertEquals("8.3", BuildConfig.VERSION_NAME) } }
