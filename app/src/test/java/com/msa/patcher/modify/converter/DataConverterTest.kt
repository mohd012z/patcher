package com.msa.patcher.modify.converter

import org.junit.Assert.*
import org.junit.Test

class DataConverterTest {
    @Test fun convertsIntegerRadices() {
        val r = DataConverter.convert("255", DetectedInputType.DECIMAL)
        assertEquals("0xFF", r.hexadecimal)
        assertEquals("11111111", r.binary)
        assertEquals("377", r.octal)
    }

    @Test fun convertsTextAndBase64() {
        val text = DataConverter.convert("Hello", DetectedInputType.TEXT)
        assertEquals("SGVsbG8=", text.base64)
        assertEquals("48 65 6C 6C 6F", text.hexBytes)
        val decoded = DataConverter.convert("SGVsbG8=", DetectedInputType.BASE64)
        assertEquals("Hello", decoded.text)
    }

    @Test fun endianAndDetectionWork() {
        val r = DataConverter.convert("0x1234", DetectedInputType.HEX)
        assertEquals("1234", r.bigEndianHex)
        assertEquals("3412", r.littleEndianHex)
        assertTrue(DataConverter.detect("1010").ambiguous)
    }

    @Test fun urlRoundTrip() {
        val encoded = DataConverter.urlEncode("a b+c")
        assertEquals("a b+c", DataConverter.urlDecode(encoded))
    }
}
