package com.msa.patcher.modify.search

import org.junit.Assert.*
import org.junit.Test

class WorkspaceSearchTest {
    private val entries = listOf(
        SearchEntry("AndroidManifest.xml", 100, true, true),
        SearchEntry("res/values/strings.xml", 120, true, true),
        SearchEntry("assets/config.json", 80, true, true),
        SearchEntry("classes.dex", 500, false, false),
        SearchEntry("lib/arm64-v8a/libx.so", 600, false, false)
    )

    @Test fun pathSearchIsCaseInsensitiveAndScoped() {
        assertEquals("res/values/strings.xml", WorkspaceSearch.searchPaths(entries, "STRINGS", SearchScope.RESOURCES).single().path)
        assertTrue(WorkspaceSearch.searchPaths(entries, "libx", SearchScope.ASSETS).isEmpty())
    }

    @Test fun emptyQueryListsScopedEntries() {
        assertEquals(1, WorkspaceSearch.searchPaths(entries, "", SearchScope.DEX).size)
    }

    @Test fun contentSearchReturnsBoundedContext() {
        val data = mapOf("assets/config.json" to "{\"api_url\":\"https://example\"}".toByteArray())
        val hit = WorkspaceSearch.searchContent(entries, "api_url", SearchScope.ASSETS) { path, _ -> data[path] }.single()
        assertEquals("assets/config.json", hit.path)
        assertEquals(1, hit.matchCount)
        assertTrue(hit.context.contains("api_url"))
    }
}
