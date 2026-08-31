package com.msa.patcher.analyze

enum class ArchitectureFamily(val label: String) {
    STANDARD("Standard Android"), APK_ENGINEERING("APK Engineering Tool"), NATIVE_HOOK("Native Hook / ART"),
    ROOT_FRAMEWORK("Root Framework Tool"), MEMORY_PROCESS("Memory / Process Tool"), FLUTTER_AOT("Flutter / AOT"),
    EXTREME_MULTIDEX("Extreme Multidex"), HYBRID("Hybrid")
}

data class AnalysisPlan(val categories: List<String>) {
    companion object {
        val ALL = listOf(
            "Overview", "APK Structure", "DEX & Code", "Native & JNI", "Resources & Build", "SDK & Network",
            "Root & Virtualization", "Hook & Runtime", "Signing & Integrity", "Memory & Process", "Protection", "Report & Evidence"
        )
        fun quick() = AnalysisPlan(listOf("Overview", "APK Structure", "DEX & Code", "Native & JNI", "SDK & Network", "Signing & Integrity", "Protection", "Report & Evidence"))
        fun deep(family: ArchitectureFamily): AnalysisPlan = when (family) {
            ArchitectureFamily.MEMORY_PROCESS -> AnalysisPlan(ALL.filterNot { it == "Resources & Build" })
            ArchitectureFamily.APK_ENGINEERING -> AnalysisPlan(ALL.filterNot { it == "Memory & Process" })
            else -> AnalysisPlan(ALL)
        }
    }
}
