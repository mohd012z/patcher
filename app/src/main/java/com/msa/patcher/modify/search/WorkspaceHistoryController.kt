package com.msa.patcher.modify.search

class WorkspaceHistoryController(private val maxRecent: Int = 12) {
    init { require(maxRecent > 0) { "maxRecent must be > 0" } }

    private val recentPaths = mutableListOf<String>()
    private val favoritePaths = linkedSetOf<String>()

    fun restore(recents: List<String>, favorites: List<String>) {
        recentPaths.clear()
        recents.map(String::trim).filter(String::isNotEmpty).distinct().take(maxRecent).forEach(recentPaths::add)
        favoritePaths.clear()
        favorites.map(String::trim).filter(String::isNotEmpty).distinct().forEach(favoritePaths::add)
    }

    fun addRecent(path: String) {
        val clean = path.trim()
        if (clean.isEmpty()) return
        recentPaths.remove(clean)
        recentPaths.add(0, clean)
        while (recentPaths.size > maxRecent) recentPaths.removeAt(recentPaths.lastIndex)
    }

    fun recent(): List<String> = recentPaths.toList()

    fun toggleFavorite(path: String): Boolean {
        val clean = path.trim()
        if (clean.isEmpty()) return false
        return if (favoritePaths.remove(clean)) false else {
            favoritePaths.add(clean)
            true
        }
    }

    fun isFavorite(path: String): Boolean = favoritePaths.contains(path.trim())
    fun favorites(): List<String> = favoritePaths.toList()
}
