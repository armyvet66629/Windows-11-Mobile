package com.example.windows11mobile.ui.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.data.RealAppRepository
import com.example.windows11mobile.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val appRepository: RealAppRepository,
    application: Application
) : AndroidViewModel(application) {

    val isDarkMode: StateFlow<Boolean?> = repository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wallpaperUri: StateFlow<String?> = repository.wallpaperUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pinnedApps: StateFlow<Set<String>> = repository.pinnedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val installedApps: StateFlow<List<AppInfo>> = appRepository.observeApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weatherAppPackage: StateFlow<String?> = repository.weatherAppPackage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val useFahrenheit: StateFlow<Boolean> = repository.useFahrenheit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showTaskbar: StateFlow<Boolean> = repository.showTaskbar
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pageOrder: StateFlow<List<String>> = repository.pageOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenPages: StateFlow<Set<String>> = repository.hiddenPages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val statusBarMode: StateFlow<String> = repository.statusBarMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "auto")

    val accentColor: StateFlow<Int> = repository.accentColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFF0078D4.toInt())

    val tileOpacity: StateFlow<Float> = repository.tileOpacity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.25f)

    val hiddenNativeWidgets: StateFlow<Set<String>> = repository.hiddenNativeWidgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val swipeDownForNotifications: StateFlow<Boolean> = repository.swipeDownForNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val showMoreTiles: StateFlow<Boolean> = repository.showMoreTiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val tilePictureEnabled: StateFlow<Boolean> = repository.tilePictureEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val tileBlurRadius: StateFlow<Float> = repository.tileBlurRadius
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 80f)

    val homeScreenBlurEnabled: StateFlow<Boolean> = repository.homeScreenBlurEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val useSystemWallpaper: StateFlow<Boolean> = repository.useSystemWallpaper
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val accentColorOverlayEnabled: StateFlow<Boolean> = repository.accentColorOverlayEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val solidTilesEnabled: StateFlow<Boolean> = repository.solidTilesEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val squareTilesEnabled: StateFlow<Boolean> = repository.squareTilesEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val rssFeeds: StateFlow<Set<String>> = repository.rssFeeds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }

    fun setWallpaperUri(uri: String?) {
        viewModelScope.launch {
            repository.setWallpaperUri(uri)
        }
    }

    fun pinApp(packageName: String) {
        viewModelScope.launch {
            repository.pinApp(packageName)
        }
    }

    fun unpinApp(packageName: String) {
        viewModelScope.launch {
            repository.unpinApp(packageName)
        }
    }

    fun setWeatherAppPackage(packageName: String?) {
        viewModelScope.launch {
            repository.setWeatherAppPackage(packageName)
        }
    }

    fun setAccentColor(color: Int) {
        viewModelScope.launch {
            repository.setAccentColor(color)
        }
    }

    fun setAccentColorOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAccentColorOverlayEnabled(enabled)
        }
    }

    fun setSolidTilesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSolidTilesEnabled(enabled)
        }
    }

    fun setSquareTilesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSquareTilesEnabled(enabled)
        }
    }

    fun setUseFahrenheit(enabled: Boolean) {
        viewModelScope.launch {
            repository.setUseFahrenheit(enabled)
        }
    }

    fun setShowTaskbar(enabled: Boolean) {
        viewModelScope.launch {
            repository.setShowTaskbar(enabled)
        }
    }

    fun setPageOrder(order: List<String>) {
        viewModelScope.launch {
            repository.setPageOrder(order)
        }
    }

    fun setHiddenPages(pages: Set<String>) {
        viewModelScope.launch {
            repository.setHiddenPages(pages)
        }
    }

    fun setStatusBarMode(mode: String) {
        viewModelScope.launch {
            repository.setStatusBarMode(mode)
        }
    }

    fun setTileOpacity(opacity: Float) {
        viewModelScope.launch {
            repository.setTileOpacity(opacity)
        }
    }

    fun setNativeWidgetVisibility(id: String, visible: Boolean) {
        viewModelScope.launch {
            repository.setNativeWidgetVisibility(id, visible)
        }
    }

    fun setSwipeDownForNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSwipeDownForNotifications(enabled)
        }
    }

    fun setShowMoreTiles(enabled: Boolean) {
        viewModelScope.launch {
            repository.setShowMoreTiles(enabled)
        }
    }

    fun setTilePictureEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setTilePictureEnabled(enabled)
        }
    }

    fun setTileBlurRadius(radius: Float) {
        viewModelScope.launch {
            repository.setTileBlurRadius(radius)
        }
    }

    fun setHomeScreenBlurEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHomeScreenBlurEnabled(enabled)
        }
    }

    fun setUseSystemWallpaper(enabled: Boolean) {
        viewModelScope.launch {
            repository.setUseSystemWallpaper(enabled)
        }
    }

    fun addRssFeed(url: String) {
        viewModelScope.launch {
            repository.addRssFeed(url)
        }
    }

    fun removeRssFeed(url: String) {
        viewModelScope.launch {
            repository.removeRssFeed(url)
        }
    }

    fun restartLauncher(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val mainIntent = Intent.makeRestartActivityTask(intent?.component)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
    private val appRepository: RealAppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] ?: throw IllegalArgumentException("Application required")
        return SettingsViewModel(repository, appRepository, application) as T
    }
}
