package com.example.windows11mobile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface SettingsRepository {
    val isDarkMode: Flow<Boolean?>
    val wallpaperUri: Flow<String?>
    val pinnedApps: Flow<Set<String>>
    val homeTiles: Flow<String?>
    val preferredNewsCategories: Flow<Set<String>>
    val rssFeeds: Flow<Set<String>>
    val tileOpacity: Flow<Float>

    suspend fun setDarkMode(isDarkMode: Boolean)
    suspend fun setWallpaperUri(uri: String)
    suspend fun pinApp(packageName: String)
    suspend fun unpinApp(packageName: String)
    suspend fun setHomeTiles(tilesJson: String)
    suspend fun addNewsCategory(category: String)
    suspend fun removeNewsCategory(category: String)
    suspend fun setNewsCategories(categories: Set<String>)
    suspend fun addRssFeed(url: String)
    suspend fun removeRssFeed(url: String)
    suspend fun setTileOpacity(opacity: Float)

    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val PINNED_APPS = stringSetPreferencesKey("pinned_apps")
        val HOME_TILES = stringPreferencesKey("home_tiles")
        val PREFERRED_NEWS_CATEGORIES = stringSetPreferencesKey("preferred_news_categories")
        val RSS_FEEDS = stringSetPreferencesKey("rss_feeds")
        val TILE_OPACITY = floatPreferencesKey("tile_opacity")
        
        val DEFAULT_PINNED_APPS = setOf(
            "com.android.chrome",
            "com.google.android.apps.messaging",
            "com.android.settings",
            "com.google.android.calendar"
        )

        val DEFAULT_NEWS_CATEGORIES = setOf("business", "technology", "sports", "entertainment")
        val DEFAULT_RSS_FEEDS = setOf("https://www.theverge.com/rss/index.xml")
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

    override val preferredNewsCategories: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.PREFERRED_NEWS_CATEGORIES] ?: SettingsRepository.DEFAULT_NEWS_CATEGORIES
    }

    override val rssFeeds: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.RSS_FEEDS] ?: SettingsRepository.DEFAULT_RSS_FEEDS
    }

    override val tileOpacity: Flow<Float> = dataStore.data.map { preferences ->
        preferences[SettingsRepository.TILE_OPACITY] ?: 0.25f
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
}
