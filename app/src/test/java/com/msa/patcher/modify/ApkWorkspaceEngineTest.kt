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
                zip.putNextEntry(ZipEntry(path)); zip.write(text.toByteArray()); zip.closeEntry()
            }
            add("assets/config.json", """{"mode":"old"}""")
            add("res/values/strings.xml", "<resources><string name=\"x\">Hello</string></resources>")
            add("classes.dex", "dex-static-value")
            add("META-INF/CERT.SF", "old-signature")
            add("AndroidManifest.xml", """<manifest android:versionCode="1" android:versionName="1"><application android:label="Demo"></application></manifest>""")
        }
        return out.toByteArray()
    }

    @Test fun replaceUndoDiffAndRebuild() {
        val dir = Files.createTempDirectory("msa-workspace-test").toFile()
        try {
            val engine = ApkWorkspaceEngine(dir)
            engine.extract(ByteArrayInputStream(sampleZip()), "sample.apk")
            assertTrue(engine.isReady())
            assertTrue(engine.allEntries("sample.apk").any { it.path == "classes.dex" && !it.editable })
            engine.replace("assets/config.json", ByteArrayInputStream("""{"mode":"new"}""".toByteArray()))
            assertEquals("""{"mode":"new"}""", engine.readText("assets/config.json"))
            assertEquals(1, engine.diffEntries().size)
            assertTrue(engine.undoLatestForPath("assets/config.json"))
            assertEquals("""{"mode":"old"}""", engine.readText("assets/config.json"))

            engine.replace("assets/config.json", ByteArrayInputStream("""{"mode":"new"}""".toByteArray()))
            val rebuilt = engine.rebuild(File(dir, "rebuilt.apk"))
            ZipFile(rebuilt).use { zip ->
                assertNotNull(zip.getEntry("assets/config.json"))
                assertNotNull(zip.getEntry("classes.dex"))
                assertNull(zip.getEntry("META-INF/CERT.SF"))
                assertEquals("""{"mode":"new"}""", zip.getInputStream(zip.getEntry("assets/config.json")).bufferedReader().readText())
            }
        } finally { dir.deleteRecursively() }
    }

    @Test fun updatesAndInspectsPlaintextManifest() {
        val dir = Files.createTempDirectory("msa-manifest-test").toFile()
        try {
            val engine = ApkWorkspaceEngine(dir)
            engine.extract(ByteArrayInputStream(sampleZip()), "sample.apk")
            assertEquals("1", engine.manifestMetadata().versionName)
            engine.updatePlaintextManifest("2.0", 2, "Changed")
            assertEquals("2.0", engine.manifestMetadata().versionName)
            assertEquals(2L, engine.manifestMetadata().versionCode)
            assertEquals("Changed", engine.manifestMetadata().appLabel)
        } finally { dir.deleteRecursively() }
    }
}
