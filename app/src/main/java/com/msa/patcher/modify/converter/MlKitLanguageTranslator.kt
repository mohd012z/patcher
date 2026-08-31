package com.msa.patcher.modify.converter

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class MlKitLanguageTranslator {
    companion object {
        val supportedNames = listOf(
            "Auto", "English", "Malay", "Indonesian", "Chinese", "Japanese", "Korean",
            "Spanish", "French", "German", "Arabic", "Thai", "Vietnamese", "Italian",
            "Portuguese", "Russian", "Turkish", "Hindi"
        )

        private val tags = mapOf(
            "English" to "en", "Malay" to "ms", "Indonesian" to "id", "Chinese" to "zh",
            "Japanese" to "ja", "Korean" to "ko", "Spanish" to "es", "French" to "fr",
            "German" to "de", "Arabic" to "ar", "Thai" to "th", "Vietnamese" to "vi",
            "Italian" to "it", "Portuguese" to "pt", "Russian" to "ru", "Turkish" to "tr",
            "Hindi" to "hi"
        )
    }

    fun translate(
        request: LanguageRequest,
        onStatus: (String) -> Unit,
        onResult: (Result<String>) -> Unit
    ) {
        val prepared = runCatching { LanguageConverterModel.prepare(request) }
            .getOrElse { onResult(Result.failure(it)); return }
        resolveSource(prepared.sourceLanguage, prepared.providerText) { sourceResult ->
            val sourceTag = sourceResult.getOrElse { onResult(Result.failure(it)); return@resolveSource }
            val targetTag = tags[prepared.targetLanguage]
                ?: prepared.targetLanguage.takeIf { it.length in 2..3 }
                ?: run { onResult(Result.failure(IllegalArgumentException("Unsupported target language."))); return@resolveSource }
            val source = TranslateLanguage.fromLanguageTag(sourceTag)
            val target = TranslateLanguage.fromLanguageTag(targetTag)
            if (source == null || target == null) {
                onResult(Result.failure(IllegalArgumentException("ML Kit does not support this language pair.")))
                return@resolveSource
            }
            if (source == target) {
                onResult(Result.success(LanguageConverterModel.finish(prepared, prepared.providerText)))
                return@resolveSource
            }
            val options = TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(target).build()
            val translator = Translation.getClient(options)
            onStatus("Preparing on-device translation model…")
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
                .addOnSuccessListener {
                    onStatus("Translating ${prepared.sourceLanguage} → ${prepared.targetLanguage}…")
                    translator.translate(prepared.providerText)
                        .addOnSuccessListener { translated ->
                            translator.close()
                            onResult(Result.success(LanguageConverterModel.finish(prepared, translated)))
                        }
                        .addOnFailureListener { error -> translator.close(); onResult(Result.failure(error)) }
                }
                .addOnFailureListener { error -> translator.close(); onResult(Result.failure(error)) }
        }
    }

    private fun resolveSource(name: String, text: String, callback: (Result<String>) -> Unit) {
        if (!name.equals("Auto", true)) {
            callback(Result.success(tags[name] ?: name.lowercase()))
            return
        }
        val identifier = LanguageIdentification.getClient()
        identifier.identifyLanguage(text)
            .addOnSuccessListener { tag ->
                identifier.close()
                if (tag == "und") callback(Result.failure(IllegalArgumentException("Language could not be detected.")))
                else callback(Result.success(tag))
            }
            .addOnFailureListener { error -> identifier.close(); callback(Result.failure(error)) }
    }
}
