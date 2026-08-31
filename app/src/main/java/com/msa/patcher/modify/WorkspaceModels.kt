package com.msa.patcher.modify

import java.io.File

data class WorkspaceEntry(
    val path: String,
    val size: Long,
    val method: Int,
    val editable: Boolean,
    val textEditable: Boolean
)

data class WorkspaceMutation(
    val path: String,
    val action: String,
    val beforeBackup: File?,
    val detail: String
)

data class WorkspaceSnapshot(
    val root: File,
    val sourceName: String,
    val entries: List<WorkspaceEntry>,
    val mutations: List<WorkspaceMutation>
)
