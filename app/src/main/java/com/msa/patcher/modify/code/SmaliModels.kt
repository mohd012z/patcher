package com.msa.patcher.modify.code

data class SmaliSnippet(
    val id: String,
    val title: String,
    val code: String,
    val explanation: String,
    val registers: String,
    val category: String
)

data class SmaliSuggestion(val snippet: SmaliSnippet?, val reason: String)
