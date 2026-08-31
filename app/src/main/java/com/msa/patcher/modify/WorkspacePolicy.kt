package com.msa.patcher.modify

object WorkspacePolicy {
    const val MAX_TEXT_BYTES = 512 * 1024

    private val protectedPrefixes = listOf(
        "META-INF/",
        "lib/"
    )

    private val protectedExact = setOf(
        "resources.arsc"
    )

    fun normalizeArchivePath(raw: String): String? {
        val path = raw.replace('\\', '/').trimStart('/')
        if (path.isBlank()) return null
        val parts = path.split('/')
        if (parts.any { it.isBlank() || it == "." || it == ".." }) return null
        if (raw.startsWith("/") || raw.startsWith("\\")) return null
        return parts.joinToString("/")
    }

    fun isSignatureArtifact(path: String): Boolean {
        val p = path.uppercase()
        if (!p.startsWith("META-INF/")) return false
        return p.endsWith(".SF") ||
            p.endsWith(".RSA") ||
            p.endsWith(".DSA") ||
            p.endsWith(".EC") ||
            p == "META-INF/MANIFEST.MF"
    }

    fun isDex(path: String): Boolean =
        Regex("""(^|/)classes(\d*)\.dex$""", RegexOption.IGNORE_CASE).containsMatchIn(path)

    fun isEditable(path: String): Boolean {
        val p = normalizeArchivePath(path) ?: return false
        if (protectedPrefixes.any { p.startsWith(it, ignoreCase = true) }) return false
        if (protectedExact.any { p.equals(it, ignoreCase = true) }) return false
        if (isDex(p)) return false
        if (p == "AndroidManifest.xml") return true
        return p.startsWith("assets/") || p.startsWith("res/")
    }

    fun isTextEditable(path: String, bytes: ByteArray): Boolean {
        val p = normalizeArchivePath(path) ?: return false
        if (!isEditable(p)) return false
        if (bytes.size > MAX_TEXT_BYTES) return false
        val extensionOkay = p.endsWith(".xml", true) ||
            p.endsWith(".txt", true) ||
            p.endsWith(".json", true) ||
            p.endsWith(".properties", true) ||
            p.endsWith(".csv", true) ||
            p.endsWith(".html", true) ||
            p.endsWith(".js", true) ||
            p.endsWith(".css", true)
        if (!extensionOkay) return false
        if (bytes.any { it == 0.toByte() }) return false
        return runCatching { bytes.toString(Charsets.UTF_8) }.isSuccess
    }
}
