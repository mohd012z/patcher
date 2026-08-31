package com.msa.patcher.analyze

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

object StaticApkScanner {
    fun scan(context: Context, uri: Uri, mode: ScanMode): ScanResult {
        val started = System.currentTimeMillis()
        val entries = mutableListOf<String>()
        val textSample = StringBuilder()
        val byteBudget = if (mode == ScanMode.DEEP) 6 * 1024 * 1024 else 768 * 1024
        var sampled = 0

        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    entries += entry.name
                    if (!entry.isDirectory && sampled < byteBudget && shouldSample(entry.name, mode)) {
                        val remaining = byteBudget - sampled
                        val bytes = readLimited(zip, remaining.coerceAtMost(512 * 1024))
                        sampled += bytes.size
                        appendPrintable(textSample, bytes)
                    }
                }
            }
        } ?: error("Unable to open selected APK")

        val inventory = ScanHeuristics.inventory(entries)
        val keywordHints = ScanHeuristics.keywordHints(entries.joinToString("\n") + "\n" + textSample)
        val findings = buildList {
            add(ScanFinding("APK Structure", "Archive inventory", "${entries.size} ZIP entries; ${inventory.dexCount} primary DEX; ${inventory.nativeCount} native libraries.", "CONFIRMED"))
            if (inventory.abis.isNotEmpty()) add(ScanFinding("Native & JNI", "ABI inventory", inventory.abis.sorted().joinToString(", "), "CONFIRMED"))
            inventory.frameworkHints.forEach { add(ScanFinding("Resources & Build", "$it indicators", "Framework-related archive paths were observed.", "STRONG")) }
            keywordHints.forEach { hint ->
                val category = when (hint) {
                    "OkHttp", "Retrofit", "Firebase" -> "SDK & Network"
                    "Frida", "Xposed" -> "Hook & Runtime"
                    "Root-related strings" -> "Root & Virtualization"
                    else -> "Resources & Build"
                }
                add(ScanFinding(category, "$hint indicator", "Static string/path evidence observed. This does not prove runtime behaviour.", "MEDIUM"))
            }
            if (keywordHints.isEmpty() && inventory.frameworkHints.isEmpty()) add(ScanFinding("Overview", "No major framework/runtime indicators", "No selected heuristic matched in the sampled static data.", "WEAK"))
        }

        return ScanResult(mode, entries.size, inventory.dexCount, inventory.nativeCount, inventory.abis, inventory.frameworkHints, keywordHints, findings, System.currentTimeMillis() - started)
    }

    private fun shouldSample(name: String, mode: ScanMode): Boolean {
        val n = name.lowercase()
        if (n.endsWith(".dex") || n.endsWith(".xml") || n.endsWith(".json") || n.endsWith(".txt") || n.endsWith(".properties")) return true
        return mode == ScanMode.DEEP && (n.endsWith(".so") || n.endsWith(".js") || n.endsWith(".html"))
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

    private fun appendPrintable(target: StringBuilder, bytes: ByteArray) {
        var run = StringBuilder()
        for (b in bytes) {
            val c = (b.toInt() and 0xff).toChar()
            if (c.code in 32..126) {
                run.append(c)
            } else {
                if (run.length >= 5) target.append(run).append('\n')
                run = StringBuilder()
            }
        }
        if (run.length >= 5) target.append(run).append('\n')
    }
}
