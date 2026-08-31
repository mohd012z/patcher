package com.msa.patcher.analyze

import com.msa.patcher.model.ApkSummary

object ArchitectureClassifier {
    fun classify(summary: ApkSummary, evidence: Set<String> = emptySet()): ArchitectureFamily {
        val e = evidence.map { it.lowercase() }.toSet()
        val apkEngineering = e.any { it.contains("apktool") || it.contains("aapt2") || it.contains("jadx") }
        val memory = e.any { it.contains("/proc/self/maps") || it.contains("ptrace") || it.contains("luaj") }
        val hook = e.any { it.contains("lsplant") || it.contains("dobbyhook") || it.contains("art_method") }
        val flutter = e.any { it.contains("libflutter.so") || it.contains("flutter_assets") }
        val root = e.any { it.contains("/system/bin/su") || it.contains("/data/adb/magisk") }
        val hits = listOf(apkEngineering, memory, hook, flutter, root).count { it }
        if (hits > 1) return ArchitectureFamily.HYBRID
        return when {
            apkEngineering -> ArchitectureFamily.APK_ENGINEERING
            memory -> ArchitectureFamily.MEMORY_PROCESS
            hook -> ArchitectureFamily.NATIVE_HOOK
            flutter -> ArchitectureFamily.FLUTTER_AOT
            root -> ArchitectureFamily.ROOT_FRAMEWORK
            summary.dexCount >= 20 -> ArchitectureFamily.EXTREME_MULTIDEX
            else -> ArchitectureFamily.STANDARD
        }
    }
}
