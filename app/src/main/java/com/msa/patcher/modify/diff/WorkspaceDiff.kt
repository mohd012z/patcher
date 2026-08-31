package com.msa.patcher.modify.diff

enum class DiffValidationState { READY, WARNING, LIMITED }

data class DiffEntry(
    val path: String,
    val operation: String,
    val beforeSize: Long,
    val afterSize: Long,
    val preview: String,
    val validationState: DiffValidationState = DiffValidationState.READY
)

object WorkspaceDiff {
    const val MAX_PREVIEW_CHARS = 1200

    fun textPreview(before: String, after: String): String {
        if (before == after) return "No textual difference."
        val beforeLines = before.lines()
        val afterLines = after.lines()
        val max = maxOf(beforeLines.size, afterLines.size)
        val out = StringBuilder()
        for (i in 0 until max) {
            val b = beforeLines.getOrNull(i)
            val a = afterLines.getOrNull(i)
            if (b == a) continue
            if (b != null) out.append("- ").append(b).append('\n')
            if (a != null) out.append("+ ").append(a).append('\n')
            if (out.length >= MAX_PREVIEW_CHARS) break
        }
        return out.toString().take(MAX_PREVIEW_CHARS).ifBlank { "Binary or structural difference." }
    }
}
