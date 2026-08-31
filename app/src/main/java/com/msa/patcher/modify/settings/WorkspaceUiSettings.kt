package com.msa.patcher.modify.settings

enum class ButtonSize { SMALL, MEDIUM, LARGE }
enum class ButtonSpacing { TIGHT, NORMAL, WIDE }
enum class ChromeSize { COMPACT, NORMAL }
enum class WorkspaceViewModeSetting { FOCUS, SPLIT, INSPECT }
enum class OrientationMode { AUTO, PORTRAIT, LANDSCAPE }

data class WorkspaceUiSettings(
    val uiScalePercent: Int = 100,
    val fontSp: Int = 14,
    val editorFontSp: Int = 13,
    val buttonSize: ButtonSize = ButtonSize.SMALL,
    val buttonSpacing: ButtonSpacing = ButtonSpacing.TIGHT,
    val cornerRadiusDp: Int = 8,
    val toolbarSize: ChromeSize = ChromeSize.COMPACT,
    val tabSize: ChromeSize = ChromeSize.COMPACT,
    val aiBubbleSize: ButtonSize = ButtonSize.SMALL,
    val searchCompact: Boolean = true,
    val autoHideUi: Boolean = true,
    val defaultViewMode: WorkspaceViewModeSetting = WorkspaceViewModeSetting.FOCUS,
    val orientationMode: OrientationMode = OrientationMode.AUTO,
    val wordWrap: Boolean = true,
    val lineNumbers: Boolean = false,
    val rememberZoomPerView: Boolean = true
) {
    fun normalized(): WorkspaceUiSettings = copy(
        uiScalePercent = uiScalePercent.coerceIn(80, 130),
        fontSp = fontSp.coerceIn(10, 24),
        editorFontSp = editorFontSp.coerceIn(9, 24),
        cornerRadiusDp = cornerRadiusDp.coerceIn(0, 20)
    )
}
