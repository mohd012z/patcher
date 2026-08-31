package com.msa.patcher.modify.help

data class FieldHelpSpec(
    val id: String,
    val title: String,
    val description: String,
    val example: String
)

object FieldHelp {
    private val specs = listOf(
        FieldHelpSpec("search", "Search", "Search a file path or readable content in the current workspace.", "Example: app_name, res/drawable, api_url"),
        FieldHelpSpec("versionName", "Version Name", "Human-readable app version. Changing it is supported only for plaintext manifest metadata.", "Example: 8.4"),
        FieldHelpSpec("versionCode", "Version Code", "Non-negative integer Android version code for plaintext manifest metadata.", "Example: 84"),
        FieldHelpSpec("appLabel", "App Label", "Direct plaintext application label. Resource references such as @string/app_name remain LIMITED.", "Example: My Authorized App"),
        FieldHelpSpec("outputName", "Output Name", "Filename for the rebuilt unsigned APK export.", "Example: MyApp_modified_unsigned.apk"),
        FieldHelpSpec("converter", "Converter Input", "Paste a decimal, hex, binary, octal, Base64, text, URL or byte value. Auto Detect never modifies files.", "Example: 255 or 0xFF or SGVsbG8="),
        FieldHelpSpec("language", "Language Converter", "Translate text while preserving Android placeholders and markup when Preserve Format is enabled.", "Example: Hello %1\$s"),
        FieldHelpSpec("smali", "Smali Helper", "Explain or insert ordinary Smali syntax templates. Preview before insertion.", "Example method return type: Z, I, V, Ljava/lang/String;")
    ).associateBy { it.id }

    fun get(id: String): FieldHelpSpec? = specs[id]
    fun all(): List<FieldHelpSpec> = specs.values.toList()
}
