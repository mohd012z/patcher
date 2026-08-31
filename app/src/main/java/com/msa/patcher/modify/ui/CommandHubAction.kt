package com.msa.patcher.modify.ui

enum class CommandHubAction(
    val title: String,
    val availableNow: Boolean = true
) {
    OPEN_FILE("Files"),
    MANIFEST("Manifest"),
    SEARCH("Search"),
    REPLACE("Replace"),
    RECENT("Recent"),
    FAVORITES("Favorites"),
    CONVERTER("Converter"),
    CRYPTO("Crypto Lab", false),
    LANGUAGE("Language"),
    COLOR("Color Lab", false),
    CODE("Code Tools"),
    DIFF("Diff"),
    BUILD("Build"),
    AI("AI"),
    SETTINGS("Settings")
}
