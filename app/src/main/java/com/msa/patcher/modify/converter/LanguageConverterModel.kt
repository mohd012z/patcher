package com.msa.patcher.modify.converter

data class LanguageRequest(
    val text: String,
    val sourceLanguage: String = "Auto",
    val targetLanguage: String = "Malay",
    val preserveFormat: Boolean = true
)

data class PreparedTranslation(
    val providerText: String,
    val guarded: GuardedText?,
    val sourceLanguage: String,
    val targetLanguage: String
)

object LanguageConverterModel {
    fun prepare(request: LanguageRequest): PreparedTranslation {
        require(request.text.length <= DataConverter.MAX_INPUT_CHARS) { "Translation input is too large." }
        val guarded = if (request.preserveFormat) TranslationGuard.protect(request.text) else null
        return PreparedTranslation(
            providerText = guarded?.text ?: request.text,
            guarded = guarded,
            sourceLanguage = request.sourceLanguage,
            targetLanguage = request.targetLanguage
        )
    }

    fun finish(prepared: PreparedTranslation, providerOutput: String): String =
        prepared.guarded?.let { TranslationGuard.restore(providerOutput, it) } ?: providerOutput
}
