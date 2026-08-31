package com.msa.patcher.modify.ui

class BottomToolbarController {
    fun viewLabel(mode: WorkspaceViewMode): String = when (mode) {
        WorkspaceViewMode.FOCUS -> "Focus"
        WorkspaceViewMode.SPLIT -> "Split"
        WorkspaceViewMode.INSPECT -> "Inspect"
    }

    fun commandHubLabel(): String = "\u26A1 Tools"
}
