package com.example.windows11mobile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface SettingsRepository {
    val isDarkMode: Flow<Boolean?>
    val wallpaperUri: Flow<String?>
    val pinnedApps: Flow<Set<String>>
    val homeTiles: Flow<String?>
    val boardWidgets: Flow<String?>
    val preferredNewsCategories: Flow<Set<String>>
    val rssFeeds: Flow<Set<String>>
    val tileOpacity: Flow<Float>
    val weatherAppPackage: Flow<String?>
    val accentColor: Flow<Int>
    val useFahrenheit: Flow<Boolean>
    val showTaskbar: Flow<Boolean>
    val pageOrder: Flow<List<String>>
    val hiddenPages: Flow<Set<String>>
    val statusBarMode: Flow<String> // "auto", "light", "dark"
    val notesJson: Flow<String?>

    suspend fun setDarkMode(isDarkMode: Boolean)
    suspend fun setWallpaperUri(uri: String)
    suspend fun pinApp(packageName: String)
    suspend fun unpinApp(packageName: String)
    suspend fun setHomeTiles(tilesJson: String)
    suspend fun setBoardWidgets(widgetsJson: String)
    suspend fun addNewsCategory(category: String)
    suspend fun removeNewsCategory(category: String)
    suspend fun setNewsCategories(categories: Set<String>)
    suspend fun addRssFeed(url: String)
    suspend fun removeRssFeed(url: String)
    suspend fun setTileOpacity(opacity: Float)
    suspend fun setWeatherAppPackage(packageName: String?)
    suspend fun setAccentColor(color: Int)
    suspend fun setUseFahrenheit(useFahrenheit: Boolean)
    suspend fun setShowTaskbar(show: Boolean)
    suspend fun setPageOrder(order: List<String>)
    suspend fun setHiddenPages(pages: Set<String>)
    suspend fun setStatusBarMode(mode: String)
    suspend fun setNotesJson(json: String)

    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val PINNED_APPS = stringSetPreferencesKey("pinned_apps")
        val HOME_TILES = stringPreferencesKey("home_tiles")
        val BOARD_WIDGETS = stringPreferencesKey("board_widgets")
        val PREFERRED_NEWS_CATEGORIES = stringSetPreferencesKey("preferred_news_categories")
        val RSS_FEEDS = stringSetPreferencesKey("rss_feeds")
        val TILE_OPACITY = floatPreferencesKey("tile_opacity")
        val WEATHER_APP_PACKAGE = stringPreferencesKey("weather_app_package")
        val ACCENT_COLOR = intPreferencesKey("accent_color")
        val USE_FAHRENHEIT = booleanPreferencesKey("use_fahrenheit")
        val SHOW_TASKBAR = booleanPreferencesKey("show_taskbar")
        val PAGE_ORDER = stringPreferencesKey("page_order")
        val HIDDEN_PAGES = stringSetPreferencesKey("hidden_pages")
        val STATUS_BAR_MODE = stringPreferencesKey("status_bar_mode")
        val NOTES_JSON = stringPreferencesKey("notes_json")
        
        val DEFAULT_PINNED_APPS = setOf(
            "com.android.settings",
            "com.google.android.calendar",
            "com.google.android.apps.messaging",
            "com.google.android.dialer"
        )
        val DEFAULT_NEWS_CATEGORIES = setOf("technology", "business", "science")
        val DEFAULT_RSS_FEEDS = setOf(
            "https://www.theverge.com/rss/index.xml",
            "https://blackhawkup.com/posts/feed/"
        )
        val DEFAULT_ACCENT_COLOR = 0xFF0078D4.toInt()
        val DEFAULT_PAGE_ORDER = listOf("notes", "board", "desktop", "apps", "people")
    }
}

class RealSettingsRepository(private val context: Context) : SettingsRepository {
    private val dataStore = context.dataStore

    override val isDarkMode: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.IS_DARK_MODE]
    }

    override val wallpaperUri: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.WALLPAPER_URI]
    }

    override val pinnedApps: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.PINNED_APPS] ?: SettingsRepository.DEFAULT_PINNED_APPS
    }

    override val homeTiles: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.HOME_TILES]
    }

    override val boardWidgets: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.BOARD_WIDGETS]
    }

    override val preferredNewsCategories: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.PREFERRED_NEWS_CATEGORIES] ?: SettingsRepository.DEFAULT_NEWS_CATEGORIES
    }

    override val rssFeeds: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.RSS_FEEDS] ?: SettingsRepository.DEFAULT_RSS_FEEDS
    }

    override val tileOpacity: Flow<Float> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.TILE_OPACITY] ?: 0.25f
    }

    override val weatherAppPackage: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.WEATHER_APP_PACKAGE]
    }

    override val accentColor: Flow<Int> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.ACCENT_COLOR] ?: SettingsRepository.DEFAULT_ACCENT_COLOR
    }

    override val useFahrenheit: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.USE_FAHRENHEIT] ?: false
    }

    override val showTaskbar: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.SHOW_TASKBAR] ?: false
    }

    override val pageOrder: Flow<List<String>> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.PAGE_ORDER]?.split(",") ?: SettingsRepository.DEFAULT_PAGE_ORDER
    }

    override val hiddenPages: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.HIDDEN_PAGES] ?: emptySet()
    }

    override val statusBarMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.STATUS_BAR_MODE] ?: "auto"
    }

    override val notesJson: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.NOTES_JSON]
    }

    override suspend fun setDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.IS_DARK_MODE] = isDarkMode
        }
    }

    override suspend fun setWallpaperUri(uri: String) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.WALLPAPER_URI] = uri
        }
    }

    override suspend fun pinApp(packageName: String) {
        dataStore.edit { preferences ->
            val current = preferences[SettingsRepository.PINNED_APPS] ?: SettingsRepository.DEFAULT_PINNED_APPS
            preferences[SettingsRepository.PINNED_APPS] = current + packageName
        }
    }

    override suspend fun unpinApp(packageName: String) {
        dataStore.edit { preferences ->
            val current = preferences[SettingsRepository.PINNED_APPS] ?: SettingsRepository.DEFAULT_PINNED_APPS
            preferences[SettingsRepository.PINNED_APPS] = current - packageName
        }
    }

    override suspend fun setHomeTiles(tilesJson: String) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.HOME_TILES] = tilesJson
        }
    }

    override suspend fun setBoardWidgets(widgetsJson: String) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.BOARD_WIDGETS] = widgetsJson
        }
    }

    override suspend fun addNewsCategory(category: String) {
        dataStore.edit { preferences ->
            val current = preferences[SettingsRepository.PREFERRED_NEWS_CATEGORIES] ?: SettingsRepository.DEFAULT_NEWS_CATEGORIES
            preferences[SettingsRepository.PREFERRED_NEWS_CATEGORIES] = current + category
        }
    }

    override suspend fun removeNewsCategory(category: String) {
        dataStore.edit { preferences ->
            val current = preferences[SettingsRepository.PREFERRED_NEWS_CATEGORIES] ?: SettingsRepository.DEFAULT_NEWS_CATEGORIES
            preferences[SettingsRepository.PREFERRED_NEWS_CATEGORIES] = current - category
        }
    }

    override suspend fun setNewsCategories(categories: Set<String>) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.PREFERRED_NEWS_CATEGORIES] = categories
        }
    }

    override suspend fun addRssFeed(url: String) {
        dataStore.edit { preferences ->
            val current = preferences[SettingsRepository.RSS_FEEDS] ?: SettingsRepository.DEFAULT_RSS_FEEDS
            preferences[SettingsRepository.RSS_FEEDS] = current + url
        }
    }

    override suspend fun removeRssFeed(url: String) {
        dataStore.edit { preferences ->
            val current = preferences[SettingsRepository.RSS_FEEDS] ?: SettingsRepository.DEFAULT_RSS_FEEDS
            preferences[SettingsRepository.RSS_FEEDS] = current - url
        }
    }

    override suspend fun setTileOpacity(opacity: Float) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.TILE_OPACITY] = opacity
        }
    }

    override suspend fun setWeatherAppPackage(packageName: String?) {
        dataStore.edit { preferences ->
            if (packageName == null) {
                preferences.remove(SettingsRepository.WEATHER_APP_PACKAGE)
            } else {
                preferences[SettingsRepository.WEATHER_APP_PACKAGE] = packageName
            }
        }
    }

    override suspend fun setAccentColor(color: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.ACCENT_COLOR] = color
        }
    }

    override suspend fun setUseFahrenheit(useFahrenheit: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.USE_FAHRENHEIT] = useFahrenheit
        }
    }

    override suspend fun setShowTaskbar(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.SHOW_TASKBAR] = show
        }
    }

    override suspend fun setPageOrder(order: List<String>) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.PAGE_ORDER] = order.joinToString(",")
        }
    }

    override suspend fun setHiddenPages(pages: Set<String>) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.HIDDEN_PAGES] = pages
        }
    }

    override suspend fun setStatusBarMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.STATUS_BAR_MODE] = mode
        }
    }

    override suspend fun setNotesJson(json: String) {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.NOTES_JSON] = json
        }
    }
}
