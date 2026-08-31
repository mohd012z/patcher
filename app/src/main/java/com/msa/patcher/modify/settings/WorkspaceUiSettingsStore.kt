package com.msa.patcher.modify.settings

import android.content.SharedPreferences

interface SettingsBackend {
    fun getString(key: String, defaultValue: String): String
    fun getInt(key: String, defaultValue: Int): Int
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putString(key: String, value: String)
    fun putInt(key: String, value: Int)
    fun putBoolean(key: String, value: Boolean)
}

class SharedPreferencesSettingsBackend(
    private val prefs: SharedPreferences
) : SettingsBackend {
    override fun getString(key: String, defaultValue: String): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    override fun getInt(key: String, defaultValue: Int): Int =
        prefs.getInt(key, defaultValue)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}

class WorkspaceUiSettingsStore(
    private val backend: SettingsBackend
) {
    fun load(): WorkspaceUiSettings {
        val defaults = WorkspaceUiSettings()

        return WorkspaceUiSettings(
            uiScalePercent = backend.getInt(KEY_UI_SCALE, defaults.uiScalePercent),
            fontSp = backend.getInt(KEY_FONT_SP, defaults.fontSp),
            editorFontSp = backend.getInt(KEY_EDITOR_FONT_SP, defaults.editorFontSp),
            buttonSize = backend.enumValue(KEY_BUTTON_SIZE, defaults.buttonSize),
            buttonSpacing = backend.enumValue(KEY_BUTTON_SPACING, defaults.buttonSpacing),
            cornerRadiusDp = backend.getInt(KEY_CORNER_RADIUS, defaults.cornerRadiusDp),
            toolbarSize = backend.enumValue(KEY_TOOLBAR_SIZE, defaults.toolbarSize),
            tabSize = backend.enumValue(KEY_TAB_SIZE, defaults.tabSize),
            aiBubbleSize = backend.enumValue(KEY_AI_BUBBLE_SIZE, defaults.aiBubbleSize),
            searchCompact = backend.getBoolean(KEY_SEARCH_COMPACT, defaults.searchCompact),
            autoHideUi = backend.getBoolean(KEY_AUTO_HIDE, defaults.autoHideUi),
            defaultViewMode = backend.enumValue(KEY_VIEW_MODE, defaults.defaultViewMode),
            orientationMode = backend.enumValue(KEY_ORIENTATION, defaults.orientationMode),
            wordWrap = backend.getBoolean(KEY_WORD_WRAP, defaults.wordWrap),
            lineNumbers = backend.getBoolean(KEY_LINE_NUMBERS, defaults.lineNumbers),
            rememberZoomPerView = backend.getBoolean(KEY_REMEMBER_ZOOM, defaults.rememberZoomPerView)
        ).normalized()
    }

    fun save(settings: WorkspaceUiSettings) {
        val s = settings.normalized()
        backend.putInt(KEY_UI_SCALE, s.uiScalePercent)
        backend.putInt(KEY_FONT_SP, s.fontSp)
        backend.putInt(KEY_EDITOR_FONT_SP, s.editorFontSp)
        backend.putString(KEY_BUTTON_SIZE, s.buttonSize.name)
        backend.putString(KEY_BUTTON_SPACING, s.buttonSpacing.name)
        backend.putInt(KEY_CORNER_RADIUS, s.cornerRadiusDp)
        backend.putString(KEY_TOOLBAR_SIZE, s.toolbarSize.name)
        backend.putString(KEY_TAB_SIZE, s.tabSize.name)
        backend.putString(KEY_AI_BUBBLE_SIZE, s.aiBubbleSize.name)
        backend.putBoolean(KEY_SEARCH_COMPACT, s.searchCompact)
        backend.putBoolean(KEY_AUTO_HIDE, s.autoHideUi)
        backend.putString(KEY_VIEW_MODE, s.defaultViewMode.name)
        backend.putString(KEY_ORIENTATION, s.orientationMode.name)
        backend.putBoolean(KEY_WORD_WRAP, s.wordWrap)
        backend.putBoolean(KEY_LINE_NUMBERS, s.lineNumbers)
        backend.putBoolean(KEY_REMEMBER_ZOOM, s.rememberZoomPerView)
    }

    private inline fun <reified T : Enum<T>> SettingsBackend.enumValue(
        key: String,
        defaultValue: T
    ): T {
        val raw = getString(key, defaultValue.name)
        return enumValues<T>().firstOrNull { it.name == raw } ?: defaultValue
    }

    private companion object {
        const val KEY_UI_SCALE = "ui_scale_percent"
        const val KEY_FONT_SP = "font_sp"
        const val KEY_EDITOR_FONT_SP = "editor_font_sp"
        const val KEY_BUTTON_SIZE = "button_size"
        const val KEY_BUTTON_SPACING = "button_spacing"
        const val KEY_CORNER_RADIUS = "corner_radius_dp"
        const val KEY_TOOLBAR_SIZE = "toolbar_size"
        const val KEY_TAB_SIZE = "tab_size"
        const val KEY_AI_BUBBLE_SIZE = "ai_bubble_size"
        const val KEY_SEARCH_COMPACT = "search_compact"
        const val KEY_AUTO_HIDE = "auto_hide_ui"
        const val KEY_VIEW_MODE = "default_view_mode"
        const val KEY_ORIENTATION = "orientation_mode"
        const val KEY_WORD_WRAP = "word_wrap"
        const val KEY_LINE_NUMBERS = "line_numbers"
        const val KEY_REMEMBER_ZOOM = "remember_zoom_per_view"
    }
}
