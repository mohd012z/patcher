package com.msa.patcher.analyze

data class EntryInventory(
    val dexCount: Int,
    val nativeCount: Int,
    val abis: Set<String>,
    val frameworkHints: Set<String>
)

object ScanHeuristics {
    fun inventory(entries: List<String>): EntryInventory {
        var dex = 0
        var native = 0
        val abis = linkedSetOf<String>()
        val frameworks = linkedSetOf<String>()
        for (name in entries) {
            val n = name.lowercase()
            if (Regex("^classes\\d*\\.dex$").matches(n)) dex++
            if (n.endsWith(".so")) {
                native++
                val parts = name.split('/')
                if (parts.size >= 3 && parts[0] == "lib") abis += parts[1]
            }
            if (n.contains("flutter_assets") || n.contains("libflutter.so")) frameworks += "Flutter"
            if (n.contains("libil2cpp.so")) frameworks += "Unity/IL2CPP"
            if (n.contains("assets/index.android.bundle")) frameworks += "React Native"
        }
        return EntryInventory(dex, native, abis, frameworks)
    }

    fun keywordHints(text: String): Set<String> {
        val lower = text.lowercase()
        val hints = linkedSetOf<String>()
        if (Regex("(^|[^a-z])(?:lib)?frida(?:[-_.]|$)").containsMatchIn(lower)) hints += "Frida"
        if (lower.contains("de.robv.android.xposed") || lower.contains("xposedbridge")) hints += "Xposed"
        if (lower.contains("okhttp3") || lower.contains("okhttp/")) hints += "OkHttp"
        if (lower.contains("retrofit2") || lower.contains("retrofit/")) hints += "Retrofit"
        if (lower.contains("com.google.firebase") || lower.contains("firebase")) hints += "Firebase"
        if (lower.contains("com.facebook.react") || lower.contains("reactnative")) hints += "React Native"
        if (lower.contains("io.flutter") || lower.contains("flutterengine")) hints += "Flutter"
        if (lower.contains("magisk") || lower.contains("su/bin") || lower.contains("/system/xbin/su")) hints += "Root-related strings"
        return hints
    }
}
