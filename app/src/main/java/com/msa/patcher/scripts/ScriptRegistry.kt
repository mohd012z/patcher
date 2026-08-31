package com.msa.patcher.scripts

import android.content.Context
import org.json.JSONObject

class ScriptRegistry(private val context: Context) {
    fun load(): List<ScriptDefinition> {
        val text = context.assets.open("scripts/index.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val arr = root.getJSONArray("scripts")
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(ScriptDefinition(
                    o.getString("id"), o.getString("title"), o.getString("category"),
                    o.optString("description"), o.optString("mode", "file"), o.optBoolean("builtIn", true), o.optString("src")
                ))
            }
        }
    }
    fun bundleVersion(): Int {
        val text = context.assets.open("scripts/index.json").bufferedReader().use { it.readText() }
        return JSONObject(text).optInt("bundleVersion", 0)
    }
    fun analysisOnly(): List<ScriptDefinition> = load().filterNot { it.category.startsWith("13 ") }
    fun legacyOnly(): List<ScriptDefinition> = load().filter { it.category.startsWith("13 ") }
}
