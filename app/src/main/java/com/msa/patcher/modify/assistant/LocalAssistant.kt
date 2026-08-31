package com.msa.patcher.modify.assistant

import com.msa.patcher.modify.code.SmaliQuickCode
import com.msa.patcher.modify.converter.DataConverter

object LocalAssistant {
    fun answer(question: String, context: AssistantContext = AssistantContext()): String {
        val decision = AssistantPolicy.decide(question)
        if (decision == AssistantDecision.READ_ONLY_EXPLANATION) {
            return "I can explain the selected code or protection evidence, but this workspace does not automate licensing, signature/integrity, paid-feature, or protection bypass changes."
        }
        val q = question.trim()
        if (q.startsWith("convert ", true)) {
            val value = q.substringAfter(' ')
            return runCatching {
                val r = DataConverter.convert(value)
                listOfNotNull(
                    r.decimal?.let { "Decimal: $it" },
                    r.hexadecimal?.let { "Hex: $it" },
                    r.binary?.let { "Binary: $it" },
                    r.octal?.let { "Octal: $it" },
                    r.text?.let { "Text: $it" },
                    r.base64?.let { "Base64: $it" },
                    r.hexBytes?.let { "Hex bytes: $it" }
                ).joinToString("\n")
            }.getOrElse { "Conversion error: ${it.message}" }
        }
        if (q.startsWith("explain smali", true)) {
            val line = q.substringAfter(':', context.selectedText.orEmpty()).trim()
            return SmaliQuickCode.explain(line)
        }
        val bounded = AssistantPolicy.sanitizeContext(context.bounded())
        return buildString {
            append("Local assistant can help with Search, Converter, Help, build errors, and ordinary Smali syntax.\n")
            if (bounded.isNotBlank()) append("Current context:\n").append(bounded)
        }
    }
}
