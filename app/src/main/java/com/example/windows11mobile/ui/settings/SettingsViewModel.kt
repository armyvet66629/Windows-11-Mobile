package com.example.windows11mobile.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.data.AppRepository
import com.example.windows11mobile.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean?> = repository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wallpaperUri: StateFlow<String?> = repository.wallpaperUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pinnedApps: StateFlow<Set<String>> = repository.pinnedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val tileOpacity: StateFlow<Float> = repository.tileOpacity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.25f)

    val weatherAppPackage: StateFlow<String?> = repository.weatherAppPackage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val accentColor: StateFlow<Int> = repository.accentColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_ACCENT_COLOR)

    val useFahrenheit: StateFlow<Boolean> = repository.useFahrenheit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showTaskbar: StateFlow<Boolean> = repository.showTaskbar
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pageOrder: StateFlow<List<String>> = repository.pageOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_PAGE_ORDER)

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    init {
        viewModelScope.launch {
            _installedApps.value = appRepository.getInstalledApps()
        }
    }

    fun setDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(isDarkMode)
        }
    }

    fun setWallpaperUri(uri: String) {
        viewModelScope.launch {
            repository.setWallpaperUri(uri)
        }
    }

    fun setTileOpacity(opacity: Float) {
        viewModelScope.launch {
            repository.setTileOpacity(opacity)
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

    fun setUseFahrenheit(useFahrenheit: Boolean) {
        viewModelScope.launch {
            repository.setUseFahrenheit(useFahrenheit)
        }
    }

    fun setShowTaskbar(show: Boolean) {
        viewModelScope.launch {
            repository.setShowTaskbar(show)
        }
    }

    fun setPageOrder(order: List<String>) {
        viewModelScope.launch {
            repository.setPageOrder(order)
        }
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
    private val appRepository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, appRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
