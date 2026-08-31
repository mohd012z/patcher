package com.msa.patcher.modify.search

enum class SearchScope {
    ALL, MANIFEST, RESOURCES, ASSETS, DEX, NATIVE, CONFIG
}

data class SearchEntry(
    val path: String,
    val size: Long,
    val editable: Boolean,
    val textEditable: Boolean
)

data class SearchHit(
    val path: String,
    val kind: String,
    val context: String,
    val editable: Boolean,
    val textEditable: Boolean,
    val matchCount: Int = 1
)
