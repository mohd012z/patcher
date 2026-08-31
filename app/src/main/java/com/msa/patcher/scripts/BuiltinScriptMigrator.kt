package com.msa.patcher.scripts

import android.content.Context
import java.io.File

data class MigrationResult(val refreshed: Boolean, val copied: Int, val preservedUserFiles: Int)

class BuiltinScriptMigrator(private val context: Context) {
    fun sync(bundleVersion: Int, targetDir: File): MigrationResult {
        targetDir.mkdirs()
        val marker = File(targetDir, ".msa_builtin_bundle_version")
        val current = marker.takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull() ?: -1
        if (current == bundleVersion) return MigrationResult(false, 0, countUserFiles(targetDir))

        val registry = ScriptRegistry(context).load()
        var copied = 0
        registry.filter { it.builtIn }.forEach { script ->
            val names = listOf(script.src, "${script.id}.prop")
            names.forEach { name ->
                runCatching {
                    context.assets.open("scripts/$name").use { input ->
                        File(targetDir, name).outputStream().use { input.copyTo(it) }
                    }
                    copied++
                }
            }
        }
        File(targetDir, "index.json").outputStream().use { out -> context.assets.open("scripts/index.json").use { it.copyTo(out) } }
        marker.writeText(bundleVersion.toString())
        return MigrationResult(true, copied, countUserFiles(targetDir))
    }

    private fun countUserFiles(dir: File): Int = dir.listFiles()?.count {
        it.isFile && !it.name.matches(Regex("\\d+\\.(sh|prop)")) && it.name !in setOf("index.json", ".msa_builtin_bundle_version")
    } ?: 0
}
