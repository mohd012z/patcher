package com.msa.patcher.analyze

import java.net.URI

object NetworkAnalyzer {
    private val urlRegex = Regex("(?i)\\bhttps?://[a-z0-9._~:/?#\\[\\]@!$&'()*+,;=%-]+")

    fun analyze(textBySource: Map<String, String>): List<ScanFinding> {
        val seen = linkedMapOf<String, Pair<String, String>>()
        textBySource.forEach { (source, text) ->
            urlRegex.findAll(text).forEach matchLoop@ { match ->
                val raw = match.value.trimEnd('.', ',', ';', ')', ']', '}', '\'', '"')
                val normalized = normalize(raw) ?: return@matchLoop
                seen.putIfAbsent(normalized, source to raw)
            }
        }
        return seen.entries.map { (normalized, pair) ->
            val (source, raw) = pair
            val host = runCatching { URI(normalized).host }.getOrNull().orEmpty()
            ScanFinding(
                category = "SDK & Network",
                title = if (host.isNotBlank()) "Endpoint: $host" else "Static URL endpoint",
                detail = raw,
                confidence = "CONFIRMED",
                source = source,
                evidenceType = EvidenceType.NETWORK
            )
        }
    }

    private fun normalize(value: String): String? = runCatching {
        val uri = URI(value)
        val scheme = uri.scheme?.lowercase() ?: return null
        val host = uri.host?.lowercase() ?: return null
        val portPart = if (uri.port >= 0) ":${uri.port}" else ""
        val path = uri.rawPath.orEmpty().ifBlank { "/" }
        val query = uri.rawQuery?.let { "?$it" }.orEmpty()
        "$scheme://$host$portPart$path$query"
    }.getOrNull()
}
