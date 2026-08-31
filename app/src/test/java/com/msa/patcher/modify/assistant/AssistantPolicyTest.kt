package com.msa.patcher.modify.assistant

import org.junit.Assert.*
import org.junit.Test

class AssistantPolicyTest {
    @Test fun allowsOrdinaryHelpAndRestrictsBypassAutomation() {
        assertEquals(AssistantDecision.ALLOW, AssistantPolicy.decide("convert 255 to hex"))
        assertEquals(AssistantDecision.ALLOW, AssistantPolicy.decide("explain return-void"))
        assertEquals(AssistantDecision.READ_ONLY_EXPLANATION, AssistantPolicy.decide("bypass license check"))
    }

    @Test fun stripsSecretLookingContextLines() {
        val clean = AssistantPolicy.sanitizeContext("Path: a\nkeystore password=abc\nText: ok")
        assertFalse(clean.contains("password", true))
        assertTrue(clean.contains("Text: ok"))
    }
}
