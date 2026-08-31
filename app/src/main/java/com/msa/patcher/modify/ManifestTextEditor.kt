package com.msa.patcher.modify

object ManifestTextEditor {
    data class Result(val text: String, val changed: Boolean, val message: String)
    data class Metadata(val versionName: String?, val versionCode: Long?, val appLabel: String?, val plaintext: Boolean)

    fun isPlaintext(bytes: ByteArray): Boolean {
        if (bytes.isEmpty() || bytes.size > WorkspacePolicy.MAX_TEXT_BYTES) return false
        if (bytes.any { it == 0.toByte() }) return false
        val text = runCatching { bytes.toString(Charsets.UTF_8) }.getOrNull() ?: return false
        return text.contains("<manifest") && text.contains("</manifest>")
    }

    fun inspect(bytes: ByteArray): Metadata {
        if (!isPlaintext(bytes)) return Metadata(null, null, null, false)
        val text = bytes.toString(Charsets.UTF_8)
        val versionName = Regex("""android:versionName\s*=\s*"([^"]*)"""").find(text)?.groupValues?.getOrNull(1)
        val versionCode = Regex("""android:versionCode\s*=\s*"([^"]*)"""").find(text)?.groupValues?.getOrNull(1)?.toLongOrNull()
        val appTag = Regex("""<application\b[^>]*>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)).find(text)?.value
        val appLabel = appTag?.let { Regex("""android:label\s*=\s*"([^"]*)"""").find(it)?.groupValues?.getOrNull(1) }
            ?.takeUnless { it.startsWith("@") }
        return Metadata(versionName, versionCode, appLabel, true)
    }

    fun update(original: String, versionName: String? = null, versionCode: Long? = null, appLabel: String? = null): Result {
        if (!original.contains("<manifest")) return Result(original, false, "AndroidManifest.xml is not plaintext XML.")
        var out = original
        var changed = false
        if (!versionName.isNullOrBlank()) {
            val regex = Regex("""android:versionName\s*=\s*"[^"]*"""")
            if (regex.containsMatchIn(out)) {
                out = out.replace(regex, """android:versionName="${xmlEscape(versionName)}"""")
                changed = true
            }
        }
        if (versionCode != null && versionCode >= 0) {
            val regex = Regex("""android:versionCode\s*=\s*"[^"]*"""")
            if (regex.containsMatchIn(out)) {
                out = out.replace(regex, """android:versionCode="$versionCode"""")
                changed = true
            }
        }
        if (!appLabel.isNullOrBlank()) {
            val appTag = Regex("""<application\b[^>]*>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            val match = appTag.find(out)
            if (match != null) {
                val labelRegex = Regex("""android:label\s*=\s*"([^"]*)"""")
                val oldTag = match.value
                val label = labelRegex.find(oldTag)?.groupValues?.getOrNull(1)
                if (label != null && !label.startsWith("@")) {
                    val newTag = oldTag.replace(labelRegex, """android:label="${xmlEscape(appLabel)}"""")
                    out = out.replaceRange(match.range, newTag)
                    changed = true
                }
            }
        }
        return Result(out, changed, if (changed) "Plaintext manifest metadata updated." else "No supported direct plaintext manifest fields were changed.")
    }

    private fun xmlEscape(value: String): String = value.replace("&", "&amp;")
        .replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
}
