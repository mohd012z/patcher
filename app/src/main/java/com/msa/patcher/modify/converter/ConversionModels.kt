package com.msa.patcher.modify.converter

enum class DetectedInputType { DECIMAL, HEX, BINARY, OCTAL, BASE64, TEXT, UNKNOWN }

data class Detection(val type: DetectedInputType, val confidence: Int, val ambiguous: Boolean, val note: String)

data class ConversionResult(
    val decimal: String? = null,
    val hexadecimal: String? = null,
    val binary: String? = null,
    val octal: String? = null,
    val text: String? = null,
    val base64: String? = null,
    val hexBytes: String? = null,
    val urlEncoded: String? = null,
    val littleEndianHex: String? = null,
    val bigEndianHex: String? = null,
    val note: String = ""
)
