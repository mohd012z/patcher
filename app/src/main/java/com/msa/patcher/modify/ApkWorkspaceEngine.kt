package com.msa.patcher.modify

import com.msa.patcher.modify.diff.DiffEntry
import com.msa.patcher.modify.diff.DiffValidationState
import com.msa.patcher.modify.diff.WorkspaceDiff
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
    private var backupSequence = 0

    init { filesDir.mkdirs(); backupDir.mkdirs() }

    fun extract(input: InputStream, sourceName: String): WorkspaceSnapshot {
        clearDirectory(filesDir); clearDirectory(backupDir); methods.clear(); mutations.clear(); backupSequence = 0
        ZipInputStream(BufferedInputStream(input)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val normalized = WorkspacePolicy.normalizeArchivePath(entry.name)
                    ?: throw IllegalArgumentException("Unsafe APK entry path: ${entry.name}")
                val out = safeFile(filesDir, normalized)
                methods[normalized] = entry.method
                if (entry.isDirectory) out.mkdirs() else {
                    out.parentFile?.mkdirs()
                    BufferedOutputStream(out.outputStream()).use { zis.copyTo(it) }
                }
                zis.closeEntry(); entry = zis.nextEntry
            }
        }
        return snapshot(sourceName)
    }

    fun snapshot(sourceName: String): WorkspaceSnapshot {
        val entries = methods.keys.sorted().mapNotNull { path ->
            val f = safeFile(filesDir, path)
            if (!f.isFile) return@mapNotNull null
            val preview = if (f.length() <= WorkspacePolicy.MAX_TEXT_BYTES) f.readBytes() else ByteArray(0)
            WorkspaceEntry(path, f.length(), methods[path] ?: ZipEntry.DEFLATED, WorkspacePolicy.isEditable(path), preview.isNotEmpty() && WorkspacePolicy.isTextEditable(path, preview))
        }
        return WorkspaceSnapshot(workspaceRoot, sourceName, entries, mutations.toList())
    }

    fun isReady(): Boolean = methods.isNotEmpty()
    fun mutationCount(): Int = mutations.size
    fun allEntries(sourceName: String): List<WorkspaceEntry> = snapshot(sourceName).entries
    fun editableEntries(sourceName: String): List<WorkspaceEntry> = snapshot(sourceName).entries.filter { it.editable }

    fun readEntryBytes(path: String, maxBytes: Int = WorkspacePolicy.MAX_PREVIEW_BYTES): ByteArray? {
        val p = requireKnownPath(path)
        val file = safeFile(filesDir, p)
        if (!file.isFile || file.length() > maxBytes) return null
        return file.readBytes()
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
        require(p != "AndroidManifest.xml") { "Use the manifest metadata editor for AndroidManifest.xml." }
        val target = safeFile(filesDir, p)
        require(target.isFile) { "Entry does not exist." }
        backupBeforeChange(p, target, "Replace", detail)
        BufferedOutputStream(target.outputStream()).use { replacement.copyTo(it) }
    }

    fun manifestMetadata(): ManifestTextEditor.Metadata {
        val bytes = readEntryBytes("AndroidManifest.xml", WorkspacePolicy.MAX_TEXT_BYTES) ?: return ManifestTextEditor.Metadata(null, null, null, false)
        return ManifestTextEditor.inspect(bytes)
    }

    fun updatePlaintextManifest(versionName: String?, versionCode: Long?, appLabel: String?): String {
        val path = "AndroidManifest.xml"
        val target = safeFile(filesDir, path)
        require(target.isFile) { "AndroidManifest.xml not found." }
        val bytes = target.readBytes()
        require(ManifestTextEditor.isPlaintext(bytes)) { "Binary AndroidManifest.xml is LIMITED in V8.4 Modify Workspace." }
        val result = ManifestTextEditor.update(bytes.toString(Charsets.UTF_8), versionName, versionCode, appLabel)
        if (result.changed) {
            backupBeforeChange(path, target, "Manifest metadata", result.message)
            target.writeText(result.text, Charsets.UTF_8)
        }
        return result.message
    }

    fun undoLast(): Boolean {
        val mutation = mutations.removeLastOrNull() ?: return false
        return restoreMutation(mutation)
    }

    fun undoLatestForPath(path: String): Boolean {
        val p = WorkspacePolicy.normalizeArchivePath(path) ?: return false
        val index = mutations.indexOfLast { it.path == p }
        if (index < 0) return false
        val mutation = mutations.removeAt(index)
        return restoreMutation(mutation)
    }

    fun mutationLog(): List<String> = mutations.mapIndexed { index, m -> "${index + 1}. ${m.action}: ${m.path} — ${m.detail}" }

    fun diffEntries(): List<DiffEntry> = mutations.groupBy { it.path }.mapNotNull { (path, list) ->
        val firstBackup = list.firstOrNull()?.beforeBackup ?: return@mapNotNull null
        val current = safeFile(filesDir, path)
        if (!firstBackup.isFile || !current.isFile) return@mapNotNull null
        val preview = if (firstBackup.length() <= WorkspacePolicy.MAX_TEXT_BYTES && current.length() <= WorkspacePolicy.MAX_TEXT_BYTES && WorkspacePolicy.isTextLike(path)) {
            val before = firstBackup.readBytes()
            val after = current.readBytes()
            if (!before.any { it == 0.toByte() } && !after.any { it == 0.toByte() }) WorkspaceDiff.textPreview(before.toString(Charsets.UTF_8), after.toString(Charsets.UTF_8)) else "Binary/file replacement."
        } else "Binary/file replacement."
        DiffEntry(path, list.last().action, firstBackup.length(), current.length(), preview, if (WorkspacePolicy.isReadOnlyBinary(path)) DiffValidationState.LIMITED else DiffValidationState.READY)
    }.sortedBy { it.path }

    fun rebuild(output: File): File {
        output.parentFile?.mkdirs()
        ZipOutputStream(BufferedOutputStream(output.outputStream())).use { zos ->
            methods.keys.sorted().forEach { path ->
                if (WorkspacePolicy.isSignatureArtifact(path)) return@forEach
                val f = safeFile(filesDir, path)
                if (!f.isFile) return@forEach
                val method = methods[path] ?: ZipEntry.DEFLATED
                val entry = ZipEntry(path).apply { this.method = method }
                if (method == ZipEntry.STORED) {
                    val bytes = f.readBytes(); val crc = CRC32().apply { update(bytes) }
                    entry.size = bytes.size.toLong(); entry.compressedSize = bytes.size.toLong(); entry.crc = crc.value
                    zos.putNextEntry(entry); zos.write(bytes)
                } else {
                    zos.putNextEntry(entry); BufferedInputStream(f.inputStream()).use { it.copyTo(zos) }
                }
                zos.closeEntry()
            }
        }
        return output
    }

    private fun backupBeforeChange(path: String, target: File, action: String, detail: String) {
        val backup = File(backupDir, "${backupSequence++}_${path.replace('/', '_')}.bak")
        target.copyTo(backup, overwrite = true)
        mutations += WorkspaceMutation(path, action, backup, detail)
    }

    private fun restoreMutation(mutation: WorkspaceMutation): Boolean {
        val backup = mutation.beforeBackup ?: return false
        if (!backup.isFile) return false
        val target = safeFile(filesDir, mutation.path)
        target.parentFile?.mkdirs(); backup.copyTo(target, overwrite = true); backup.delete(); return true
    }

    private fun requireKnownPath(path: String): String {
        val p = WorkspacePolicy.normalizeArchivePath(path) ?: throw IllegalArgumentException("Unsafe path.")
        require(methods.containsKey(p)) { "APK entry not found." }
        return p
    }

    private fun requireEditable(path: String): String {
        val p = requireKnownPath(path)
        require(WorkspacePolicy.isEditable(p)) { "Protected or unsupported APK entry." }
        return p
    }

    private fun safeFile(base: File, path: String): File {
        val target = File(base, path); val baseCanonical = base.canonicalFile; val targetCanonical = target.canonicalFile
        require(targetCanonical.path == baseCanonical.path || targetCanonical.path.startsWith(baseCanonical.path + File.separator)) { "Workspace path escape rejected." }
        return targetCanonical
    }

    private fun clearDirectory(dir: File) { if (dir.exists()) dir.listFiles()?.forEach { it.deleteRecursively() }; dir.mkdirs() }
}
