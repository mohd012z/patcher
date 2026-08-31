package com.msa.patcher.modify.help

data class Suggestion(val value: String, val reason: String)

data class SuggestionContext(
    val sourceName: String = "selected.apk",
    val currentVersionName: String? = null,
    val currentVersionCode: Long? = null,
    val currentLabel: String? = null,
    val selectedPath: String? = null,
    val selectedText: String? = null
)

object WorkspaceSuggestions {
    fun forField(fieldId: String, context: SuggestionContext): List<Suggestion> = when (fieldId) {
        "search" -> buildList {
            context.selectedPath?.substringAfterLast('/')?.takeIf { it.isNotBlank() }?.let {
                add(Suggestion(it, "Selected file name"))
            }
            add(Suggestion("AndroidManifest.xml", "Open manifest quickly"))
            add(Suggestion("res/", "Browse resources"))
            add(Suggestion("assets/", "Browse assets"))
        }.distinctBy { it.value }
        "versionName" -> listOfNotNull(
            context.currentVersionName?.let { Suggestion(incrementVersionName(it), "Increment current version") },
            Suggestion("8.4", "V8.4 workspace release example")
        ).distinctBy { it.value }
        "versionCode" -> listOfNotNull(
            context.currentVersionCode?.let { Suggestion((it + 1).toString(), "Increment current versionCode") },
            Suggestion("84", "V8.4 workspace release example")
        ).distinctBy { it.value }
        "appLabel" -> listOfNotNull(
            context.currentLabel?.let { Suggestion(it, "Keep current label") },
            context.sourceName.substringBeforeLast('.').takeIf { it.isNotBlank() }?.let { Suggestion(it, "Derived from APK filename") }
        ).distinctBy { it.value }
        "outputName" -> listOf(
            Suggestion(context.sourceName.substringBeforeLast('.') + "_modified_unsigned.apk", "Derived from selected APK")
        )
        "converter" -> buildList {
            context.selectedText?.trim()?.takeIf { it.isNotBlank() && it.length <= 120 }?.let { add(Suggestion(it, "Selected editor text")) }
            add(Suggestion("255", "Decimal conversion example"))
            add(Suggestion("0xFF", "Hex conversion example"))
            add(Suggestion("SGVsbG8=", "Base64 conversion example"))
        }.distinctBy { it.value }
        "language" -> buildList {
            context.selectedText?.trim()?.takeIf { it.isNotBlank() && it.length <= 2000 }?.let { add(Suggestion(it, "Selected editor text")) }
            add(Suggestion("Hello %1\$s", "Translation example with protected Android placeholder"))
        }.distinctBy { it.value }
        "smali" -> listOf(
            Suggestion("()V", "Void return method"),
            Suggestion("()Z", "Boolean return method"),
            Suggestion("()Ljava/lang/String;", "Object return method")
        )
        else -> emptyList()
    }

    private fun incrementVersionName(value: String): String {
        val parts = value.split('.').toMutableList()
        val last = parts.lastOrNull()?.toIntOrNull() ?: return value
        parts[parts.lastIndex] = (last + 1).toString()
        return parts.joinToString(".")
    }
}
