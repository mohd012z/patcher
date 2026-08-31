package com.msa.patcher.modify.assistant

data class AssistantContext(
    val sourceName: String? = null,
    val selectedPath: String? = null,
    val selectedText: String? = null,
    val searchQuery: String? = null,
    val converterValue: String? = null,
    val buildError: String? = null
) {
    fun bounded(maxChars: Int = 4000): String = buildString {
        sourceName?.let { append("APK: ").append(it).append('\n') }
        selectedPath?.let { append("Path: ").append(it).append('\n') }
        searchQuery?.let { append("Search: ").append(it).append('\n') }
        converterValue?.let { append("Converter: ").append(it).append('\n') }
        buildError?.let { append("Build: ").append(it).append('\n') }
        selectedText?.let { append("Text:\n").append(it) }
    }.take(maxChars)
}
