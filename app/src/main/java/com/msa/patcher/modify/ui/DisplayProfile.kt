package com.msa.patcher.modify.ui

enum class UiDensity { COMPACT, NORMAL, LARGE }
enum class LayoutMode { PORTRAIT, LANDSCAPE, TABLET }

data class DisplayProfile(
    val widthDp: Int,
    val heightDp: Int,
    val density: Float,
    val defaultDensity: UiDensity,
    val layoutMode: LayoutMode
) {
    companion object {
        fun from(widthDp: Int, heightDp: Int, density: Float): DisplayProfile {
            require(widthDp > 0) { "widthDp must be > 0" }
            require(heightDp > 0) { "heightDp must be > 0" }
            require(density > 0f) { "density must be > 0" }

            val mode = when {
                widthDp >= 600 -> LayoutMode.TABLET
                widthDp > heightDp -> LayoutMode.LANDSCAPE
                else -> LayoutMode.PORTRAIT
            }

            val densityMode = when {
                widthDp < 420 -> UiDensity.COMPACT
                widthDp >= 840 -> UiDensity.NORMAL
                else -> UiDensity.NORMAL
            }

            return DisplayProfile(
                widthDp = widthDp,
                heightDp = heightDp,
                density = density,
                defaultDensity = densityMode,
                layoutMode = mode
            )
        }
    }
}
