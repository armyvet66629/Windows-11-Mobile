package com.example.windows11mobile.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Build
import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.data.AppRepository
import com.example.windows11mobile.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppDrawerViewModel(
    private val repository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val tileOpacity: StateFlow<Float> = settingsRepository.tileOpacity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.25f)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    
    val filteredApps: StateFlow<List<AppInfo>> = combine(_allApps, _searchQuery) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _allApps.value = repository.getInstalledApps()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun getShortcuts(packageName: String): List<ShortcutInfo> {
        return repository.getShortcuts(packageName)
    }

    fun launchShortcut(shortcut: ShortcutInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                launcherApps.startShortcut(shortcut, null, null)
            } catch (e: Exception) {
                // Log error
            }
        }
    }
}

class AppDrawerViewModelFactory(
    private val repository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppDrawerViewModel(repository, settingsRepository, context) as T
    }
}
