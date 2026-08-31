package com.msa.patcher.modify.converter

data class GuardedText(val text: String, val tokens: Map<String, String>)

object TranslationGuard {
    private val protectedPattern = Regex(
        """("(?:[^"\\]|\\.)*"\s*:|%(?:\d+\$)?[a-zA-Z]|\\n|\\t|@(?:string|drawable|mipmap|color|layout|id|xml|raw|style)/[A-Za-z0-9_.-]+|<[^>]+>)"""
    )

    fun protect(input: String): GuardedText {
        var index = 0
        val tokens = linkedMapOf<String, String>()
        val guarded = protectedPattern.replace(input) { match ->
            val key = "⟦MSA_${index++}⟧"
            tokens[key] = match.value
            key
        }
        return GuardedText(guarded, tokens)
    }

    fun restore(translated: String, guarded: GuardedText): String {
        var out = translated
        guarded.tokens.forEach { (key, value) -> out = out.replace(key, value) }
        return out
    }

    fun protectedTokensPreserved(output: String, original: String): Boolean {
        val expected = protectedPattern.findAll(original).map { it.value }.toList()
        return expected.all { output.contains(it) }
    }
}
