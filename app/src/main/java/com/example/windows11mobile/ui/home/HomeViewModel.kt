package com.example.windows11mobile.ui.home

import android.app.Application
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.windows11mobile.data.HomeTile
import com.example.windows11mobile.data.NotificationManager
import com.example.windows11mobile.data.SettingsRepository
import com.example.windows11mobile.data.TileSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    application: Application
) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost = AppWidgetHost(context, APPWIDGET_HOST_ID)

    private val _rawTiles = MutableStateFlow<List<HomeTile>>(emptyList())
    val tiles = combine(_rawTiles, NotificationManager.notifications) { tiles, notifications ->
        tiles.map { tile ->
            val notification = notifications[tile.packageName]
            tile.copy(
                notificationCount = notification?.count ?: 0,
                notificationSummary = notification?.summary,
                notificationSender = notification?.sender,
                notificationContent = notification?.content,
                notificationTime = notification?.postTime
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    val tileOpacity = settingsRepository.tileOpacity.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.25f
    )

    init {
        viewModelScope.launch {
            settingsRepository.homeTiles.collect { json ->
                if (json != null) {
                    try {
                        val decoded = Json.decodeFromString<List<HomeTile>>(json)
                        if (decoded.isNotEmpty()) {
                            // Only update local state if it's currently empty OR
                            // if the new data is different and we're not in edit mode
                            // This allows external updates (like adding a tile) to show up,
                            // while preventing the reorder-race-condition.
                            if (_rawTiles.value.isEmpty() || 
                                (_rawTiles.value != decoded && !_isEditMode.value)) {
                                _rawTiles.value = decoded
                            }
                        } else if (_rawTiles.value.isEmpty()) {
                            _rawTiles.value = getDefaultTiles()
                        }
                    } catch (e: Exception) {
                        if (_rawTiles.value.isEmpty()) _rawTiles.value = getDefaultTiles()
                    }
                } else if (_rawTiles.value.isEmpty()) {
                    _rawTiles.value = getDefaultTiles()
                }
            }
        }
    }

    private fun getDefaultTiles() = listOf(
        HomeTile("clock", null, "Clock", TileSize.LARGE, specialType = HomeTile.TYPE_CLOCK),
        HomeTile("weather", null, "Weather", TileSize.WIDE, specialType = HomeTile.TYPE_WEATHER),
        HomeTile("photos", "com.google.android.apps.photos", "Photos", TileSize.MEDIUM, specialType = HomeTile.TYPE_PHOTOS),
        HomeTile(UUID.randomUUID().toString(), "com.android.settings", "Settings", TileSize.MEDIUM),
        HomeTile(UUID.randomUUID().toString(), "com.google.android.calendar", "Calendar", TileSize.MEDIUM),
        HomeTile(UUID.randomUUID().toString(), "com.google.android.apps.messaging", "Messaging", TileSize.MEDIUM),
        HomeTile(UUID.randomUUID().toString(), "com.google.android.dialer", "Phone", TileSize.SMALL),
        HomeTile(UUID.randomUUID().toString(), "com.android.chrome", "Edge", TileSize.SMALL),
        HomeTile(UUID.randomUUID().toString(), "com.google.android.apps.maps", "Maps", TileSize.MEDIUM),
        HomeTile(UUID.randomUUID().toString(), "com.google.android.youtube", "YouTube", TileSize.WIDE),
        HomeTile(UUID.randomUUID().toString(), "com.android.contacts", "People", TileSize.MEDIUM),
        HomeTile(UUID.randomUUID().toString(), "com.android.camera2", "Camera", TileSize.MEDIUM)
    )

    private fun saveTiles() {
        viewModelScope.launch {
            settingsRepository.setHomeTiles(Json.encodeToString(_rawTiles.value))
        }
    }

    fun moveTile(fromIndex: Int, toIndex: Int) {
        val list = _rawTiles.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _rawTiles.value = list
            saveTiles()
        }
    }

    fun resizeTile(id: String) {
        _rawTiles.value = _rawTiles.value.map {
            if (it.id == id) {
                val nextSize = when (it.size) {
                    TileSize.SMALL -> TileSize.MEDIUM
                    TileSize.MEDIUM -> TileSize.WIDE
                    TileSize.WIDE -> TileSize.LARGE
                    TileSize.LARGE -> TileSize.SMALL
                }
                it.copy(size = nextSize)
            } else it
        }
        saveTiles()
    }

    fun resizeTile(id: String, newSize: TileSize) {
        _rawTiles.value = _rawTiles.value.map {
            if (it.id == id) it.copy(size = newSize) else it
        }
        saveTiles()
    }

    fun setEditMode(enabled: Boolean) {
        _isEditMode.value = enabled
    }

    fun addTile(packageName: String, label: String) {
        val newTile = HomeTile(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            label = label,
            size = TileSize.MEDIUM
        )
        _rawTiles.value = _rawTiles.value + newTile
        saveTiles()
    }

    fun addWidgetTile(widgetId: Int, label: String) {
        val newTile = HomeTile(
            id = UUID.randomUUID().toString(),
            label = label,
            size = TileSize.WIDE, // Widgets default to Wide
            widgetId = widgetId,
            isWidget = true
        )
        _rawTiles.value = _rawTiles.value + newTile
        saveTiles()
    }

    fun removeTile(id: String) {
        val tile = _rawTiles.value.find { it.id == id }
        if (tile?.isWidget == true && tile.widgetId != null) {
            appWidgetHost.deleteAppWidgetId(tile.widgetId)
        }
        _rawTiles.value = _rawTiles.value.filter { it.id != id }
        saveTiles()
    }

    fun getShortcuts(packageName: String): List<ShortcutInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()
        
        val query = LauncherApps.ShortcutQuery().apply {
            setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or 
                         LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or 
                         LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            setPackage(packageName)
        }
        return try {
            // Use specific user handle if possible, otherwise default to current
            launcherApps.getShortcuts(query, android.os.Process.myUserHandle()) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun launchShortcut(shortcut: ShortcutInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return
        
        try {
            launcherApps.startShortcut(shortcut, null, null)
        } catch (e: Exception) {
            // Log or handle
        }
    }

    override fun onCleared() {
        super.onCleared()
        // We don't stopListening here because the host might be shared or needed until activity dies
    }

    fun startWidgetListening() {
        appWidgetHost.startListening()
    }

    fun stopWidgetListening() {
        appWidgetHost.stopListening()
    }

    fun allocateWidgetId(): Int {
        return appWidgetHost.allocateAppWidgetId()
    }

    companion object {
        private const val APPWIDGET_HOST_ID = 1024
    }
}

class HomeViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(settingsRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
