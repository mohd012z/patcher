package com.msa.patcher.modify.converter

import java.math.BigInteger
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Base64

object DataConverter {
    const val MAX_INPUT_CHARS = 64 * 1024

    fun detect(raw: String): Detection {
        val s = raw.trim()
        if (s.isEmpty()) return Detection(DetectedInputType.UNKNOWN, 0, false, "Empty input")
        if (s.startsWith("0x", true) && s.drop(2).matches(Regex("[0-9a-fA-F]+"))) return Detection(DetectedInputType.HEX, 100, false, "0x prefix")
        if (s.startsWith("0b", true) && s.drop(2).matches(Regex("[01]+"))) return Detection(DetectedInputType.BINARY, 100, false, "0b prefix")
        if (s.startsWith("0o", true) && s.drop(2).matches(Regex("[0-7]+"))) return Detection(DetectedInputType.OCTAL, 100, false, "0o prefix")
        if (s.matches(Regex("-?[0-9]+"))) {
            val ambiguous = s.matches(Regex("[01]+")) || (s.length > 1 && s.matches(Regex("[0-7]+")))
            return Detection(DetectedInputType.DECIMAL, if (ambiguous) 65 else 95, ambiguous, if (ambiguous) "Digits could represent another radix; decimal chosen" else "Decimal digits")
        }
        if (looksLikeBase64(s)) return Detection(DetectedInputType.BASE64, 80, true, "Base64-shaped text; may also be ordinary text")
        return Detection(DetectedInputType.TEXT, 90, false, "Plain text")
    }

    fun convert(raw: String, forced: DetectedInputType? = null): ConversionResult {
        require(raw.length <= MAX_INPUT_CHARS) { "Input exceeds 64 KiB character limit." }
        val type = forced ?: detect(raw).type
        return when (type) {
            DetectedInputType.DECIMAL -> fromInteger(BigInteger(raw.trim(), 10))
            DetectedInputType.HEX -> fromInteger(BigInteger(raw.trim().removePrefixIgnoreCase("0x"), 16))
            DetectedInputType.BINARY -> fromInteger(BigInteger(raw.trim().removePrefixIgnoreCase("0b"), 2))
            DetectedInputType.OCTAL -> fromInteger(BigInteger(raw.trim().removePrefixIgnoreCase("0o"), 8))
            DetectedInputType.BASE64 -> fromBytes(Base64.getDecoder().decode(raw.trim()), "Decoded Base64")
            DetectedInputType.TEXT -> fromBytes(raw.toByteArray(Charsets.UTF_8), "UTF-8 text")
            DetectedInputType.UNKNOWN -> ConversionResult(note = "Unable to detect input format.")
        }
    }

    fun urlEncode(text: String): String = URLEncoder.encode(text, Charsets.UTF_8.name())
    fun urlDecode(text: String): String = URLDecoder.decode(text, Charsets.UTF_8.name())

    fun bytesToHuman(bytes: Long): String {
        require(bytes >= 0) { "Bytes must be non-negative." }
        if (bytes < 1024) return "$bytes B"
        val kib = bytes / 1024.0
        if (kib < 1024) return "%.2f KiB".format(kib)
        val mib = kib / 1024.0
        if (mib < 1024) return "%.2f MiB".format(mib)
        return "%.2f GiB".format(mib / 1024.0)
    }

    private fun fromInteger(value: BigInteger): ConversionResult {
        val bytes = unsignedBytes(value)
        val hexBytes = bytes.joinToString(" ") { "%02X".format(it.toInt() and 0xff) }
        val reversed = bytes.reversedArray().joinToString("") { "%02X".format(it.toInt() and 0xff) }
        val normal = bytes.joinToString("") { "%02X".format(it.toInt() and 0xff) }
        return ConversionResult(
            decimal = value.toString(10),
            hexadecimal = "0x" + value.toString(16).uppercase(),
            binary = value.toString(2),
            octal = value.toString(8),
            hexBytes = hexBytes,
            bigEndianHex = normal,
            littleEndianHex = reversed,
            note = "Integer conversion"
        )
    }

    private fun fromBytes(bytes: ByteArray, note: String): ConversionResult {
        val text = bytes.toString(Charsets.UTF_8)
        val hex = bytes.joinToString(" ") { "%02X".format(it.toInt() and 0xff) }
        return ConversionResult(
            text = text,
            base64 = Base64.getEncoder().encodeToString(bytes),
            hexBytes = hex,
            urlEncoded = urlEncode(text),
            bigEndianHex = hex.replace(" ", ""),
            littleEndianHex = bytes.reversedArray().joinToString("") { "%02X".format(it.toInt() and 0xff) },
            note = note
        )
    }

    private fun unsignedBytes(value: BigInteger): ByteArray {
        val raw = value.toByteArray()
        return if (raw.size > 1 && raw[0] == 0.toByte()) raw.copyOfRange(1, raw.size) else raw
    }

    private fun looksLikeBase64(s: String): Boolean {
        if (s.length < 4 || s.length % 4 != 0) return false
        if (!s.matches(Regex("[A-Za-z0-9+/]*={0,2}"))) return false
        return runCatching { Base64.getDecoder().decode(s) }.isSuccess
    }

    private fun String.removePrefixIgnoreCase(prefix: String): String =
        if (startsWith(prefix, true)) substring(prefix.length) else this
}
