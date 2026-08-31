package com.msa.patcher.scripts

data class ScriptDefinition(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val mode: String,
    val builtIn: Boolean,
    val src: String
)
