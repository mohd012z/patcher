package com.msa.patcher.modify.ui

import kotlin.math.roundToInt

enum class ZoomViewKey(val storageKey: String) {
    EDITOR("editor"),
    SNAPSHOT("snapshot"),
    SEARCH("search"),
    DIFF("diff"),
    BUILD("build"),
    AI("ai")
}

class PerViewZoomController(
    private val minPercent: Int = 50,
    private val maxPercent: Int = 300,
    private val defaultPercent: Int = 100
) {
    private val values = mutableMapOf<ZoomViewKey, Int>()

    init {
        require(minPercent > 0)
        require(maxPercent >= minPercent)
        require(defaultPercent in minPercent..maxPercent)
    }

    fun percent(view: ZoomViewKey): Int = values[view] ?: defaultPercent

    fun setPercent(view: ZoomViewKey, percent: Int): Int {
        val clamped = percent.coerceIn(minPercent, maxPercent)
        values[view] = clamped
        return clamped
    }

    fun scale(view: ZoomViewKey, scaleFactor: Float): Int {
        if (!scaleFactor.isFinite() || scaleFactor <= 0f) return percent(view)
        val next = (percent(view) * scaleFactor).roundToInt()
        return setPercent(view, next)
    }

    fun reset(view: ZoomViewKey): Int = setPercent(view, defaultPercent)
}
