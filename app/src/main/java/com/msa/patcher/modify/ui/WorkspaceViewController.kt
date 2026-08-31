package com.msa.patcher.modify.ui

enum class WorkspaceViewMode {
    FOCUS,
    SPLIT,
    INSPECT
}

data class WorkspaceViewState(
    val showNavigator: Boolean,
    val showSnapshot: Boolean,
    val showEditor: Boolean,
    val showInventory: Boolean,
    val label: String
)

class WorkspaceViewController {
    fun stateFor(mode: WorkspaceViewMode): WorkspaceViewState = when (mode) {
        WorkspaceViewMode.FOCUS -> WorkspaceViewState(
            showNavigator = false,
            showSnapshot = false,
            showEditor = true,
            showInventory = false,
            label = "Focus"
        )
        WorkspaceViewMode.SPLIT -> WorkspaceViewState(
            showNavigator = true,
            showSnapshot = true,
            showEditor = true,
            showInventory = false,
            label = "Split"
        )
        WorkspaceViewMode.INSPECT -> WorkspaceViewState(
            showNavigator = true,
            showSnapshot = false,
            showEditor = true,
            showInventory = true,
            label = "Inspect"
        )
    }

    fun titleFor(mode: WorkspaceViewMode): String = when (mode) {
        WorkspaceViewMode.FOCUS -> "Focus — editor first"
        WorkspaceViewMode.SPLIT -> "Split — loaded snapshot + editor"
        WorkspaceViewMode.INSPECT -> "Inspect — navigator + inventory"
    }
}
