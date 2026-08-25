package com.example.windows11mobile

import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.data.AppRepository
import com.example.windows11mobile.ui.apps.AppDrawerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

import com.example.windows11mobile.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class AppDrawerViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    
    // Fake Repository
    class FakeAppRepository : AppRepository {
        var apps = listOf<AppInfo>()
        override suspend fun getInstalledApps(): List<AppInfo> = apps
    }

    class FakeSettingsRepository : SettingsRepository {
        override val isDarkMode: Flow<Boolean?> = flowOf(false)
        override val wallpaperUri: Flow<String?> = flowOf(null)
        override val pinnedApps: Flow<Set<String>> = flowOf(emptySet())
        override val homeTiles: Flow<String?> = flowOf(null)
        override val preferredNewsCategories: Flow<Set<String>> = flowOf(emptySet())
        override val rssFeeds: Flow<Set<String>> = flowOf(emptySet())
        override val tileOpacity: Flow<Float> = flowOf(0.25f)

        override suspend fun setDarkMode(isDarkMode: Boolean) {}
        override suspend fun setWallpaperUri(uri: String) {}
        override suspend fun pinApp(packageName: String) {}
        override suspend fun unpinApp(packageName: String) {}
        override suspend fun setHomeTiles(tilesJson: String) {}
        override suspend fun addNewsCategory(category: String) {}
        override suspend fun removeNewsCategory(category: String) {}
        override suspend fun setNewsCategories(categories: Set<String>) {}
        override suspend fun addRssFeed(url: String) {}
        override suspend fun removeRssFeed(url: String) {}
        override suspend fun setTileOpacity(opacity: Float) {}
    }

    private lateinit var viewModel: AppDrawerViewModel
    private lateinit var repository: FakeAppRepository
    private lateinit var settingsRepository: FakeSettingsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAppRepository()
        settingsRepository = FakeSettingsRepository()
        repository.apps = listOf(
            AppInfo("Calculator", "com.calc", null),
            AppInfo("Calendar", "com.cal", null),
            AppInfo("Camera", "com.cam", null),
            AppInfo("Settings", "com.settings", null)
        )
        viewModel = AppDrawerViewModel(repository, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search query filters apps correctly`() = runTest {
        viewModel.onSearchQueryChange("Cal")
        
        val filtered = viewModel.filteredApps.first()
        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.name == "Calculator" })
        assertTrue(filtered.any { it.name == "Calendar" })
    }

    @Test
    fun `empty search query returns all apps`() = runTest {
        viewModel.onSearchQueryChange("")
        
        val filtered = viewModel.filteredApps.first()
        assertEquals(4, filtered.size)
    }
}
