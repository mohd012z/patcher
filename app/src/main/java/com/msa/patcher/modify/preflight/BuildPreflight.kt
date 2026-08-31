package com.msa.patcher.modify.preflight

data class PreflightReport(
    val ready: Boolean,
    val errors: List<String>,
    val warnings: List<String>,
    val summary: String
)

object BuildPreflight {
    fun check(workspaceReady: Boolean, entryCount: Int, mutationCount: Int): PreflightReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        if (!workspaceReady) errors += "Create a workspace first."
        if (workspaceReady && entryCount <= 0) errors += "Workspace has no APK file entries."
        if (workspaceReady && mutationCount == 0) warnings += "No modifications are currently recorded."
        if (workspaceReady) warnings += "Modified APK output is unsigned; stale META-INF signature metadata is omitted."
        val ready = errors.isEmpty()
        val summary = if (ready) "Preflight READY${if (warnings.isNotEmpty()) " with ${warnings.size} warning(s)" else ""}." else "Preflight BLOCKED: ${errors.size} error(s)."
        return PreflightReport(ready, errors, warnings, summary)
    }
}
