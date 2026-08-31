package com.msa.patcher.analyze

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

object StaticApkScanner {
    fun scan(context: Context, uri: Uri, mode: ScanMode): ScanResult {
        val started = System.currentTimeMillis()
        val entries = mutableListOf<String>()
        val textBySource = linkedMapOf<String, String>()
        val bytesBySource = linkedMapOf<String, ByteArray>()
        val byteBudget = if (mode == ScanMode.DEEP) 12 * 1024 * 1024 else 1024 * 1024
        val perEntryLimit = if (mode == ScanMode.DEEP) 1024 * 1024 else 256 * 1024
        var sampled = 0

        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    entries += entry.name
                    if (entry.isDirectory) continue
                    val forceManifest = entry.name == "AndroidManifest.xml"
                    val shouldRead = forceManifest || (sampled < byteBudget && shouldSample(entry.name, mode))
                    if (!shouldRead) continue
                    val remaining = if (forceManifest) 2 * 1024 * 1024 else (byteBudget - sampled).coerceAtLeast(0)
                    val limit = minOf(if (forceManifest) 2 * 1024 * 1024 else perEntryLimit, remaining)
                    if (limit <= 0) continue
                    val bytes = readLimited(zip, limit)
                    if (!forceManifest) sampled += bytes.size
                    bytesBySource[entry.name] = bytes
                    val printable = printableStrings(bytes)
                    if (printable.isNotBlank()) textBySource[entry.name] = printable
                }
            }
        } ?: error("Unable to open selected APK")

        val inventory = ScanHeuristics.inventory(entries)
        val joinedText = entries.joinToString("\n") + "\n" + textBySource.values.joinToString("\n")
        val keywordHints = ScanHeuristics.keywordHints(joinedText)
        val analyzersRun = linkedSetOf<String>()
        val analyzersLimited = linkedSetOf<String>()
        val warnings = mutableListOf<String>()
        val findings = mutableListOf<ScanFinding>()

        findings += ScanFinding(
            "Overview", "Archive inventory",
            "${entries.size} ZIP entries; ${inventory.dexCount} DEX; ${inventory.nativeCount} native libraries; sampled ${sampled / 1024} KiB.",
            "CONFIRMED", "APK archive", EvidenceType.INVENTORY
        )
        findings += ScanFinding("APK Structure", "Archive structure", "${entries.size} entries inventoried.", "CONFIRMED", "APK archive", EvidenceType.INVENTORY)

        analyzersRun += AnalyzerNames.MANIFEST
        val manifest = ManifestAnalyzer.analyze(bytesBySource["AndroidManifest.xml"])
        findings += manifest.findings
        if (manifest.limited) {
            analyzersLimited += AnalyzerNames.MANIFEST
            manifest.warning?.let { warnings += it }
        }

        analyzersRun += AnalyzerNames.DEX
        val dexBytes = bytesBySource.filterKeys { Regex("^classes\\d*\\.dex$", RegexOption.IGNORE_CASE).matches(it) }
        findings += DexAnalyzer.analyze(dexBytes, textBySource.filterKeys { it.endsWith(".dex", true) })
        if (inventory.dexCount > dexBytes.size) {
            analyzersLimited += AnalyzerNames.DEX
            warnings += "DEX analysis sampled ${dexBytes.size} of ${inventory.dexCount} DEX files within scan budget."
        }

        analyzersRun += AnalyzerNames.RESOURCES
        findings += ResourceAnalyzer.analyze(entries, textBySource)

        analyzersRun += AnalyzerNames.NETWORK
        findings += NetworkAnalyzer.analyze(textBySource)
        if (mode == ScanMode.QUICK) analyzersLimited += AnalyzerNames.NETWORK

        analyzersRun += AnalyzerNames.NATIVE
        findings += NativeAnalyzer.analyze(entries, textBySource)
        if (mode == ScanMode.QUICK && inventory.nativeCount > 0) analyzersLimited += AnalyzerNames.NATIVE

        analyzersRun += AnalyzerNames.SIGNING
        findings += SigningAnalyzer.analyze(entries)
        analyzersLimited += AnalyzerNames.SIGNING
        warnings += "Signing analyzer inventories archive signing artifacts; APK Signature Scheme v2/v3/v4 cryptographic verification is not performed."

        analyzersRun += AnalyzerNames.HEURISTICS
        keywordHints.forEach { hint ->
            val category = when (hint) {
                "OkHttp", "Retrofit", "Firebase" -> "SDK & Network"
                "Frida", "Xposed" -> "Hook & Runtime"
                "Root-related strings" -> "Root & Virtualization"
                else -> "Resources & Build"
            }
            findings += ScanFinding(category, "$hint static indicator", "Matched in bounded APK static data. This does not prove runtime behaviour.", "MEDIUM", "sampled static data", EvidenceType.HEURISTIC)
        }
        findings += ProtectionAnalyzer.analyze(entries, textBySource)

        val uniqueFindings = findings.distinctBy { listOf(it.category, it.title, it.detail, it.source, it.evidenceType.name) }
        return ScanResult(
            mode = mode,
            entriesScanned = entries.size,
            dexCount = inventory.dexCount,
            nativeCount = inventory.nativeCount,
            abis = inventory.abis,
            frameworkHints = inventory.frameworkHints,
            keywordHints = keywordHints,
            findings = uniqueFindings,
            durationMs = System.currentTimeMillis() - started,
            sampledBytes = sampled,
            analyzersRun = analyzersRun,
            analyzersLimited = analyzersLimited,
            warnings = warnings.distinct()
        )
    }

    private fun shouldSample(name: String, mode: ScanMode): Boolean {
        val n = name.lowercase()
        if (Regex("^classes\\d*\\.dex$").matches(n)) return true
        if (n.endsWith(".xml") || n.endsWith(".json") || n.endsWith(".txt") || n.endsWith(".properties") || n.endsWith(".yaml") || n.endsWith(".yml")) return true
        return mode == ScanMode.DEEP && (n.endsWith(".so") || n.endsWith(".js") || n.endsWith(".html") || n.endsWith(".smali") || n.endsWith(".cfg") || n.endsWith(".conf"))
    }

    private fun readLimited(zip: ZipInputStream, max: Int): ByteArray {
        val out = ByteArrayOutputStream(max.coerceAtMost(64 * 1024))
        val buffer = ByteArray(16 * 1024)
        var left = max
        while (left > 0) {
            val n = zip.read(buffer, 0, minOf(buffer.size, left))
            if (n <= 0) break
            out.write(buffer, 0, n)
            left -= n
        }
        return out.toByteArray()
    }

    private fun printableStrings(bytes: ByteArray): String {
        val target = StringBuilder()
        var run = StringBuilder()
        for (b in bytes) {
            val c = (b.toInt() and 0xff).toChar()
            if (c.code in 32..126) run.append(c) else {
                if (run.length >= 5) target.append(run).append('\n')
                run = StringBuilder()
            }
        }
        if (run.length >= 5) target.append(run).append('\n')
        return target.toString()
    }
}
