package com.msa.patcher
import org.junit.Assert.assertEquals
import org.junit.Test
class AppIdentityTest {
 @Test fun versionIsV8() { assertEquals("8.0", BuildConfig.VERSION_NAME) }
}
