package com.msa.patcher.modify.search

data class ReplacePreviewResult(
    val find: String,
    val replacement: String,
    val ignoreCase: Boolean,
    val matchCount: Int,
    val changed: Boolean,
    val beforePreview: String,
    val afterPreview: String,
    val resultText: String
)

object PlaintextReplacePreview {
    const val MAX_PREVIEW_CHARS = 1200
    const val MAX_MATCHES = 10_000

    fun build(
        text: String,
        find: String,
        replacement: String,
        ignoreCase: Boolean = false,
        maxPreviewChars: Int = MAX_PREVIEW_CHARS
    ): ReplacePreviewResult {
        require(find.isNotEmpty()) { "Find text must not be empty." }
        require(maxPreviewChars > 0) { "maxPreviewChars must be > 0" }

        val regex = Regex(Regex.escape(find), if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet())
        var count = 0
        val result = regex.replace(text) { _ ->
            count++
            require(count <= MAX_MATCHES) { "Replace preview exceeds $MAX_MATCHES matches." }
            replacement
        }
        return ReplacePreviewResult(
            find = find,
            replacement = replacement,
            ignoreCase = ignoreCase,
            matchCount = count,
            changed = count > 0 && result != text,
            beforePreview = text.take(maxPreviewChars),
            afterPreview = result.take(maxPreviewChars),
            resultText = result
        )
    }
}
