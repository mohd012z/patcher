package com.msa.patcher.modify.converter

import org.junit.Assert.*
import org.junit.Test

class TranslationGuardTest {
    @Test fun preservesAndroidAndMarkupTokens() {
        val original = "<string name=\"hello\">Hello %1\$s\\n@string/app_name</string>"
        val prepared = LanguageConverterModel.prepare(LanguageRequest(original, targetLanguage = "Malay", preserveFormat = true))
        val fakeTranslated = prepared.providerText.replace("Hello", "Hai")
        val output = LanguageConverterModel.finish(prepared, fakeTranslated)
        assertTrue(output.contains("%1\$s"))
        assertTrue(output.contains("\\n"))
        assertTrue(output.contains("@string/app_name"))
        assertTrue(output.contains("<string name=\"hello\">"))
        assertTrue(output.contains("Hai"))
    }

    @Test fun preservesJsonKeys() {
        val original = "{\"title\":\"Hello %s\",\"count\":1}"
        val prepared = LanguageConverterModel.prepare(LanguageRequest(original, preserveFormat = true))
        val output = LanguageConverterModel.finish(prepared, prepared.providerText.replace("Hello", "Hai"))
        assertTrue(output.contains("\"title\":"))
        assertTrue(output.contains("\"count\":"))
        assertTrue(output.contains("%s"))
    }
}
