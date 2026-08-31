package com.msa.patcher.modify

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ApkWorkspaceEngineTest {
    private fun sampleZip(): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun add(path: String, text: String) {
                zip.putNextEntry(ZipEntry(path))
                zip.write(text.toByteArray())
                zip.closeEntry()
            }
            add("assets/config.json", """{"mode":"old"}""")
            add("classes.dex", "dex-bytes")
            add("META-INF/CERT.SF", "old-signature")
            add("AndroidManifest.xml", """<manifest android:versionCode="1" android:versionName="1"><application android:label="Demo"></application></manifest>""")
        }
        return out.toByteArray()
    }

    @Test fun replaceUndoAndRebuild() {
        val dir = Files.createTempDirectory("msa-workspace-test").toFile()
        try {
            val engine = ApkWorkspaceEngine(dir)
            engine.extract(ByteArrayInputStream(sampleZip()), "sample.apk")
            engine.replace("assets/config.json", ByteArrayInputStream("""{"mode":"new"}""".toByteArray()))
            assertEquals("""{"mode":"new"}""", engine.readText("assets/config.json"))
            assertTrue(engine.undoLast())
            assertEquals("""{"mode":"old"}""", engine.readText("assets/config.json"))

            engine.replace("assets/config.json", ByteArrayInputStream("""{"mode":"new"}""".toByteArray()))
            val rebuilt = engine.rebuild(File(dir, "rebuilt.apk"))
            ZipFile(rebuilt).use { zip ->
                assertNotNull(zip.getEntry("assets/config.json"))
                assertNotNull(zip.getEntry("classes.dex"))
                assertNull(zip.getEntry("META-INF/CERT.SF"))
                val text = zip.getInputStream(zip.getEntry("assets/config.json")).bufferedReader().readText()
                assertEquals("""{"mode":"new"}""", text)
            }
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test fun updatesPlaintextManifest() {
        val dir = Files.createTempDirectory("msa-manifest-test").toFile()
        try {
            val engine = ApkWorkspaceEngine(dir)
            engine.extract(ByteArrayInputStream(sampleZip()), "sample.apk")
            engine.updatePlaintextManifest("2.0", 2, "Changed")
            val text = engine.readText("AndroidManifest.xml")
            assertTrue(text.contains("""android:versionName="2.0""""))
            assertTrue(text.contains("""android:versionCode="2""""))
            assertTrue(text.contains("""android:label="Changed""""))
        } finally {
            dir.deleteRecursively()
        }
    }
}
