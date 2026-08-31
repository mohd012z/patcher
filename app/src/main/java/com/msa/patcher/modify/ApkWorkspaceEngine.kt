package com.msa.patcher.modify

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ApkWorkspaceEngine(private val workspaceRoot: File) {
    private val filesDir = File(workspaceRoot, "files")
    private val backupDir = File(workspaceRoot, "backups")
    private val methods = linkedMapOf<String, Int>()
    private val mutations = mutableListOf<WorkspaceMutation>()

    init {
        filesDir.mkdirs()
        backupDir.mkdirs()
    }

    fun extract(input: InputStream, sourceName: String): WorkspaceSnapshot {
        clearDirectory(filesDir)
        clearDirectory(backupDir)
        methods.clear()
        mutations.clear()

        ZipInputStream(BufferedInputStream(input)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val normalized = WorkspacePolicy.normalizeArchivePath(entry.name)
                    ?: throw IllegalArgumentException("Unsafe APK entry path: ${entry.name}")
                val out = safeFile(filesDir, normalized)
                methods[normalized] = entry.method
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    BufferedOutputStream(out.outputStream()).use { output ->
                        zis.copyTo(output)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return snapshot(sourceName)
    }

    fun snapshot(sourceName: String): WorkspaceSnapshot {
        val entries = methods.keys.sorted().mapNotNull { path ->
            val f = safeFile(filesDir, path)
            if (!f.isFile) return@mapNotNull null
            val preview = if (f.length() <= WorkspacePolicy.MAX_TEXT_BYTES) f.readBytes() else ByteArray(0)
            WorkspaceEntry(
                path = path,
                size = f.length(),
                method = methods[path] ?: ZipEntry.DEFLATED,
                editable = WorkspacePolicy.isEditable(path),
                textEditable = preview.isNotEmpty() && WorkspacePolicy.isTextEditable(path, preview)
            )
        }
        return WorkspaceSnapshot(workspaceRoot, sourceName, entries, mutations.toList())
    }

    fun readText(path: String): String {
        val p = requireEditable(path)
        val file = safeFile(filesDir, p)
        val bytes = file.readBytes()
        require(WorkspacePolicy.isTextEditable(p, bytes)) { "Entry is not bounded plaintext." }
        return bytes.toString(Charsets.UTF_8)
    }

    fun writeText(path: String, text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        require(bytes.size <= WorkspacePolicy.MAX_TEXT_BYTES) { "Text exceeds 512 KiB limit." }
        val p = requireEditable(path)
        val target = safeFile(filesDir, p)
        require(target.isFile) { "Entry does not exist." }
        require(WorkspacePolicy.isTextEditable(p, target.readBytes())) { "Entry is not plaintext editable." }
        backupBeforeChange(p, target, "Edit text", "Saved plaintext entry")
        target.writeBytes(bytes)
    }

    fun replace(path: String, replacement: InputStream, detail: String = "Replaced entry") {
        val p = requireEditable(path)
        require(p != "AndroidManifest.xml") {
            "Use the manifest metadata editor for AndroidManifest.xml."
        }
        val target = safeFile(filesDir, p)
        require(target.isFile) { "Entry does not exist." }
        backupBeforeChange(p, target, "Replace", detail)
        BufferedOutputStream(target.outputStream()).use { output ->
            replacement.copyTo(output)
        }
    }

    fun updatePlaintextManifest(
        versionName: String?,
        versionCode: Long?,
        appLabel: String?
    ): String {
        val path = "AndroidManifest.xml"
        val target = safeFile(filesDir, path)
        require(target.isFile) { "AndroidManifest.xml not found." }
        val bytes = target.readBytes()
        require(ManifestTextEditor.isPlaintext(bytes)) {
            "Binary AndroidManifest.xml is LIMITED in V8.3 Modify Workspace."
        }
        val result = ManifestTextEditor.update(
            bytes.toString(Charsets.UTF_8),
            versionName,
            versionCode,
            appLabel
        )
        if (result.changed) {
            backupBeforeChange(path, target, "Manifest metadata", result.message)
            target.writeText(result.text, Charsets.UTF_8)
        }
        return result.message
    }

    fun undoLast(): Boolean {
        val mutation = mutations.removeLastOrNull() ?: return false
        val backup = mutation.beforeBackup ?: return false
        val target = safeFile(filesDir, mutation.path)
        target.parentFile?.mkdirs()
        backup.copyTo(target, overwrite = true)
        backup.delete()
        return true
    }

    fun mutationLog(): List<String> =
        mutations.mapIndexed { index, m ->
            "${index + 1}. ${m.action}: ${m.path} — ${m.detail}"
        }

    fun rebuild(output: File): File {
        output.parentFile?.mkdirs()
        ZipOutputStream(BufferedOutputStream(output.outputStream())).use { zos ->
            methods.keys.sorted().forEach { path ->
                if (WorkspacePolicy.isSignatureArtifact(path)) return@forEach
                val f = safeFile(filesDir, path)
                if (!f.isFile) return@forEach

                val method = methods[path] ?: ZipEntry.DEFLATED
                val entry = ZipEntry(path)
                entry.method = method
                if (method == ZipEntry.STORED) {
                    val bytes = f.readBytes()
                    val crc = CRC32().apply { update(bytes) }
                    entry.size = bytes.size.toLong()
                    entry.compressedSize = bytes.size.toLong()
                    entry.crc = crc.value
                    zos.putNextEntry(entry)
                    zos.write(bytes)
                } else {
                    zos.putNextEntry(entry)
                    BufferedInputStream(f.inputStream()).use { it.copyTo(zos) }
                }
                zos.closeEntry()
            }
        }
        return output
    }

    fun editableEntries(sourceName: String): List<WorkspaceEntry> =
        snapshot(sourceName).entries.filter { it.editable }

    private fun backupBeforeChange(
        path: String,
        target: File,
        action: String,
        detail: String
    ) {
        val backup = File(backupDir, "${mutations.size}_${path.replace('/', '_')}.bak")
        target.copyTo(backup, overwrite = true)
        mutations += WorkspaceMutation(path, action, backup, detail)
    }

    private fun requireEditable(path: String): String {
        val p = WorkspacePolicy.normalizeArchivePath(path)
            ?: throw IllegalArgumentException("Unsafe path.")
        require(WorkspacePolicy.isEditable(p)) { "Protected or unsupported APK entry." }
        return p
    }

    private fun safeFile(base: File, path: String): File {
        val target = File(base, path)
        val baseCanonical = base.canonicalFile
        val targetCanonical = target.canonicalFile
        require(
            targetCanonical.path == baseCanonical.path ||
                targetCanonical.path.startsWith(baseCanonical.path + File.separator)
        ) { "Workspace path escape rejected." }
        return targetCanonical
    }

    private fun clearDirectory(dir: File) {
        if (dir.exists()) dir.listFiles()?.forEach { it.deleteRecursively() }
        dir.mkdirs()
    }
}
