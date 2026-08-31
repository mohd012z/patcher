package com.msa.patcher
import org.junit.Assert.assertEquals
import org.junit.Test
class AppIdentityTest { @Test fun versionIsV82() { assertEquals("8.2", BuildConfig.VERSION_NAME) } }
