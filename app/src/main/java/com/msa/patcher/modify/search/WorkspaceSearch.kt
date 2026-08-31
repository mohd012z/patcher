package com.msa.patcher.modify.search

object WorkspaceSearch {
    const val MAX_SEARCH_TEXT_BYTES = 512 * 1024
    const val MAX_CONTEXT_CHARS = 180

    fun scopeOf(path: String): SearchScope = when {
        path.equals("AndroidManifest.xml", true) -> SearchScope.MANIFEST
        path.startsWith("res/", true) -> SearchScope.RESOURCES
        path.startsWith("assets/", true) -> SearchScope.ASSETS
        Regex("(^|/)classes(\\d*)\\.dex$", RegexOption.IGNORE_CASE).containsMatchIn(path) -> SearchScope.DEX
        path.startsWith("lib/", true) || path.endsWith(".so", true) -> SearchScope.NATIVE
        path.endsWith(".json", true) || path.endsWith(".properties", true) ||
            path.endsWith(".xml", true) || path.endsWith(".txt", true) ||
            path.endsWith(".csv", true) || path.endsWith(".html", true) ||
            path.endsWith(".js", true) || path.endsWith(".css", true) -> SearchScope.CONFIG
        else -> SearchScope.ALL
    }

    fun searchPaths(entries: List<SearchEntry>, query: String, scope: SearchScope): List<SearchHit> {
        val q = query.trim()
        val filtered = entries.filter { matchesScope(it.path, scope) }
        if (q.isEmpty()) {
            return filtered.map { entry ->
                SearchHit(entry.path, "PATH", entry.path, entry.editable, entry.textEditable)
            }
        }
        return filtered.filter { it.path.contains(q, ignoreCase = true) }.map { entry ->
            SearchHit(entry.path, "PATH", entry.path, entry.editable, entry.textEditable)
        }
    }

    fun searchContent(
        entries: List<SearchEntry>,
        query: String,
        scope: SearchScope,
        reader: (String, Int) -> ByteArray?
    ): List<SearchHit> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        val hits = mutableListOf<SearchHit>()
        entries.filter { matchesScope(it.path, scope) }.forEach { entry ->
            val bytes = reader(entry.path, MAX_SEARCH_TEXT_BYTES) ?: return@forEach
            val actualScope = scopeOf(entry.path)
            val text = if (actualScope == SearchScope.DEX || actualScope == SearchScope.NATIVE) {
                extractAsciiStrings(bytes)
            } else {
                printableText(bytes)
            } ?: return@forEach
            val indexes = findAll(text, q)
            if (indexes.isEmpty()) return@forEach
            val first = indexes.first()
            val start = (first - 70).coerceAtLeast(0)
            val end = (first + q.length + 110).coerceAtMost(text.length)
            val context = text.substring(start, end)
                .replace('\n', ' ')
                .replace('\r', ' ')
                .take(MAX_CONTEXT_CHARS)
            hits += SearchHit(
                path = entry.path,
                kind = if (entry.textEditable) "TEXT" else "STATIC STRING",
                context = context,
                editable = entry.editable,
                textEditable = entry.textEditable,
                matchCount = indexes.size
            )
        }
        return hits
    }

    private fun matchesScope(path: String, scope: SearchScope): Boolean {
        if (scope == SearchScope.ALL) return true
        val actual = scopeOf(path)
        return actual == scope || (scope == SearchScope.CONFIG && actual == SearchScope.RESOURCES && isTextLike(path))
    }

    private fun isTextLike(path: String): Boolean = listOf(
        ".xml", ".txt", ".json", ".properties", ".csv", ".html", ".js", ".css"
    ).any { path.endsWith(it, true) }

    private fun printableText(bytes: ByteArray): String? {
        if (bytes.isEmpty()) return null
        if (bytes.any { it == 0.toByte() } && bytes.size < 32) return null
        val decoded = bytes.toString(Charsets.UTF_8)
        val printable = decoded.count { it == '\n' || it == '\r' || it == '\t' || !it.isISOControl() }
        if (printable.toDouble() / decoded.length.coerceAtLeast(1) < 0.72) return null
        return decoded
    }

    private fun extractAsciiStrings(bytes: ByteArray): String? {
        val out = StringBuilder()
        val current = StringBuilder()
        fun flush() {
            if (current.length >= 4) {
                if (out.isNotEmpty()) out.append('\n')
                out.append(current)
            }
            current.setLength(0)
        }
        bytes.forEach { b ->
            val c = b.toInt() and 0xff
            if (c in 32..126) current.append(c.toChar()) else flush()
            if (out.length >= MAX_SEARCH_TEXT_BYTES) return@forEach
        }
        flush()
        return out.toString().takeIf { it.isNotBlank() }
    }

    private fun findAll(text: String, query: String): List<Int> {
        val result = mutableListOf<Int>()
        var from = 0
        while (from < text.length) {
            val index = text.indexOf(query, from, ignoreCase = true)
            if (index < 0) break
            result += index
            from = index + query.length.coerceAtLeast(1)
        }
        return result
    }
}
