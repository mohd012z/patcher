package com.msa.patcher.modify.diff

import org.junit.Assert.*
import org.junit.Test

class WorkspaceDiffTest {
    @Test fun textPreviewShowsChangedLinesAndIsBounded() {
        val preview = WorkspaceDiff.textPreview("a\nb", "a\nc")
        assertTrue(preview.contains("- b"))
        assertTrue(preview.contains("+ c"))
        assertTrue(preview.length <= WorkspaceDiff.MAX_PREVIEW_CHARS)
    }
}
