package com.msa.patcher.analyze

object ManifestAnalyzer {
    data class ManifestAnalysis(val findings: List<ScanFinding>, val limited: Boolean, val warning: String? = null)

    fun analyze(bytes: ByteArray?): ManifestAnalysis {
        if (bytes == null || bytes.isEmpty()) return ManifestAnalysis(emptyList(), true, "AndroidManifest.xml was not sampled.")
        return if (looksTextXml(bytes)) analyzeText(bytes.toString(Charsets.UTF_8)) else analyzeBinary(bytes)
    }

    private fun looksTextXml(bytes: ByteArray): Boolean {
        val head = bytes.take(80).toByteArray().toString(Charsets.UTF_8).trimStart('\uFEFF', ' ', '\n', '\r', '\t')
        return head.startsWith("<")
    }

    private fun analyzeText(xml: String): ManifestAnalysis {
        val findings = mutableListOf<ScanFinding>()
        val manifestTag = Regex("<manifest\\b([^>]*)>", RegexOption.IGNORE_CASE).find(xml)?.groupValues?.getOrNull(1).orEmpty()
        val pkg = attr(manifestTag, "package")
        if (!pkg.isNullOrBlank()) findings += finding("Package", pkg)
        val versionName = attr(manifestTag, "versionName")
        if (!versionName.isNullOrBlank()) findings += finding("Version name", versionName)
        val permissions = Regex("<uses-permission\\b[^>]*?(?:android:)?name\\s*=\\s*[\"']([^\"']+)", RegexOption.IGNORE_CASE).findAll(xml).map { it.groupValues[1] }.distinct().toList()
        if (permissions.isNotEmpty()) findings += finding("Permissions", permissions.joinToString("\n"), "STRONG")
        val components = listOf("activity", "service", "receiver", "provider").sumOf { Regex("<$it\\b", RegexOption.IGNORE_CASE).findAll(xml).count() }
        if (components > 0) findings += finding("Application components", "$components declared components", "CONFIRMED")
        return ManifestAnalysis(findings, false)
    }

    private fun attr(tag: String, name: String): String? =
        Regex("(?:android:)?${Regex.escape(name)}\\s*=\\s*[\"']([^\"']+)", RegexOption.IGNORE_CASE).find(tag)?.groupValues?.getOrNull(1)

    private fun finding(title: String, detail: String, confidence: String = "CONFIRMED") =
        ScanFinding("APK Structure", title, detail, confidence, "AndroidManifest.xml", EvidenceType.MANIFEST)

    private fun analyzeBinary(bytes: ByteArray): ManifestAnalysis {
        return runCatching {
            val parsed = BinaryAxml.read(bytes)
            val findings = mutableListOf<ScanFinding>()
            parsed.manifestAttributes["package"]?.takeIf { it.isNotBlank() }?.let { findings += finding("Package", it) }
            parsed.manifestAttributes["versionName"]?.takeIf { it.isNotBlank() }?.let { findings += finding("Version name", it) }
            parsed.manifestAttributes["versionCode"]?.takeIf { it.isNotBlank() }?.let { findings += finding("Version code", it) }
            parsed.usesSdk.takeIf { it.isNotEmpty() }?.let { findings += finding("SDK requirements", it.entries.joinToString { e -> "${e.key}=${e.value}" }) }
            if (parsed.permissions.isNotEmpty()) findings += finding("Permissions", parsed.permissions.sorted().joinToString("\n"), "STRONG")
            if (parsed.components.isNotEmpty()) {
                val exported = parsed.components.count { it.exported == "true" }
                findings += finding("Application components", "${parsed.components.size} components; exported=true: $exported\n" + parsed.components.take(40).joinToString("\n") { "${it.type}: ${it.name}${it.exported?.let { e -> " (exported=$e)" }.orEmpty()}" }, "STRONG")
            }
            ManifestAnalysis(findings, findings.isEmpty(), if (findings.isEmpty()) "Binary manifest decoded but no supported metadata was extracted." else null)
        }.getOrElse { ManifestAnalysis(emptyList(), true, "Binary AndroidManifest.xml decode limited: ${it.message ?: it::class.java.simpleName}") }
    }

    private object BinaryAxml {
        private const val RES_STRING_POOL_TYPE = 0x0001
        private const val RES_XML_START_ELEMENT_TYPE = 0x0102
        private const val UTF8_FLAG = 0x00000100

        data class Component(val type: String, val name: String, val exported: String?)
        data class Parsed(
            val manifestAttributes: MutableMap<String, String> = linkedMapOf(),
            val usesSdk: MutableMap<String, String> = linkedMapOf(),
            val permissions: MutableSet<String> = linkedSetOf(),
            val components: MutableList<Component> = mutableListOf()
        )

        fun read(bytes: ByteArray): Parsed {
            require(bytes.size >= 8) { "AXML too small" }
            val result = Parsed()
            var strings: List<String> = emptyList()
            var offset = 8
            while (offset + 8 <= bytes.size) {
                val type = u16(bytes, offset)
                val headerSize = u16(bytes, offset + 2)
                val chunkSize = u32(bytes, offset + 4)
                if (headerSize < 8 || chunkSize < headerSize || offset + chunkSize > bytes.size) break
                when (type) {
                    RES_STRING_POOL_TYPE -> strings = parseStringPool(bytes, offset, headerSize)
                    RES_XML_START_ELEMENT_TYPE -> parseStartElement(bytes, offset, chunkSize, strings, result)
                }
                offset += chunkSize
            }
            return result
        }

        private fun parseStringPool(bytes: ByteArray, off: Int, headerSize: Int): List<String> {
            if (off + 28 > bytes.size) return emptyList()
            val stringCount = u32(bytes, off + 8)
            val flags = u32(bytes, off + 16)
            val stringsStart = u32(bytes, off + 20)
            if (stringCount < 0 || stringCount > 100000) return emptyList()
            val offsetsBase = off + headerSize
            val dataBase = off + stringsStart
            val utf8 = flags and UTF8_FLAG != 0
            return List(stringCount) { i ->
                val rel = u32(bytes, offsetsBase + i * 4)
                val pos = dataBase + rel
                if (pos !in bytes.indices) "" else if (utf8) readUtf8(bytes, pos) else readUtf16(bytes, pos)
            }
        }

        private fun parseStartElement(bytes: ByteArray, off: Int, chunkSize: Int, strings: List<String>, result: Parsed) {
            if (strings.isEmpty() || off + 36 > bytes.size) return
            val nameIdx = u32(bytes, off + 20)
            val tag = strings.getOrNull(nameIdx).orEmpty()
            val attrStart = u16(bytes, off + 24)
            val attrSize = u16(bytes, off + 26).coerceAtLeast(20)
            val attrCount = u16(bytes, off + 28)
            val attrs = linkedMapOf<String, String>()
            val base = off + 16 + attrStart
            for (i in 0 until attrCount) {
                val p = base + i * attrSize
                if (p + 20 > off + chunkSize || p + 20 > bytes.size) break
                val name = strings.getOrNull(u32(bytes, p + 4)).orEmpty()
                val rawIdx = u32(bytes, p + 8)
                val dataType = bytes[p + 15].toInt() and 0xff
                val data = u32(bytes, p + 16)
                val value = when {
                    rawIdx >= 0 -> strings.getOrNull(rawIdx).orEmpty()
                    dataType == 0x03 -> strings.getOrNull(data).orEmpty()
                    dataType == 0x12 -> if (data != 0) "true" else "false"
                    dataType in 0x10..0x11 -> data.toString()
                    else -> "0x${data.toUInt().toString(16)}"
                }
                if (name.isNotBlank()) attrs[name] = value
            }
            when (tag) {
                "manifest" -> result.manifestAttributes.putAll(attrs)
                "uses-sdk" -> result.usesSdk.putAll(attrs.filterKeys { it == "minSdkVersion" || it == "targetSdkVersion" || it == "maxSdkVersion" })
                "uses-permission", "uses-permission-sdk-23" -> attrs["name"]?.takeIf { it.isNotBlank() }?.let { result.permissions += it }
                "activity", "activity-alias", "service", "receiver", "provider" -> {
                    val name = attrs["name"].orEmpty()
                    if (name.isNotBlank()) result.components += Component(tag, name, attrs["exported"])
                }
            }
        }

        private fun readUtf8(bytes: ByteArray, start: Int): String {
            var p = start
            val (_, p1) = readLen8(bytes, p); p = p1
            val (byteLen, p2) = readLen8(bytes, p); p = p2
            if (byteLen < 0 || p + byteLen > bytes.size) return ""
            return bytes.copyOfRange(p, p + byteLen).toString(Charsets.UTF_8)
        }

        private fun readLen8(bytes: ByteArray, start: Int): Pair<Int, Int> {
            if (start >= bytes.size) return 0 to start
            val first = bytes[start].toInt() and 0xff
            return if (first and 0x80 == 0) first to (start + 1) else {
                if (start + 1 >= bytes.size) 0 to (start + 1) else (((first and 0x7f) shl 8) or (bytes[start + 1].toInt() and 0xff)) to (start + 2)
            }
        }

        private fun readUtf16(bytes: ByteArray, start: Int): String {
            var p = start
            if (p + 2 > bytes.size) return ""
            var len = u16(bytes, p); p += 2
            if (len and 0x8000 != 0) {
                if (p + 2 > bytes.size) return ""
                len = ((len and 0x7fff) shl 16) or u16(bytes, p); p += 2
            }
            val byteLen = len * 2
            if (p + byteLen > bytes.size) return ""
            return bytes.copyOfRange(p, p + byteLen).toString(Charsets.UTF_16LE)
        }

        private fun u16(b: ByteArray, o: Int): Int = (b[o].toInt() and 0xff) or ((b[o + 1].toInt() and 0xff) shl 8)
        private fun u32(b: ByteArray, o: Int): Int = (b[o].toInt() and 0xff) or ((b[o + 1].toInt() and 0xff) shl 8) or ((b[o + 2].toInt() and 0xff) shl 16) or ((b[o + 3].toInt() and 0xff) shl 24)
    }
}
