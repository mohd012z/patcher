package com.msa.patcher.modify.help

import org.junit.Assert.*
import org.junit.Test

class WorkspaceSuggestionsTest {
    @Test fun suggestionsAreDeterministicValuesOnly() {
        val ctx = SuggestionContext("Demo.apk", "1.2.3", 12, "Demo", "res/values/strings.xml")
        assertTrue(WorkspaceSuggestions.forField("search", ctx).any { it.value == "strings.xml" })
        assertEquals("1.2.4", WorkspaceSuggestions.forField("versionName", ctx).first().value)
        assertEquals("13", WorkspaceSuggestions.forField("versionCode", ctx).first().value)
        assertTrue(WorkspaceSuggestions.forField("outputName", ctx).single().value.endsWith("_modified_unsigned.apk"))
    }
}
