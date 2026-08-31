package com.example.windows11mobile.ui.home

import android.app.Application
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
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
import com.example.windows11mobile.data.WeatherRepository
import com.example.windows11mobile.data.NewsArticle
import com.example.windows11mobile.data.RealNewsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

import com.example.windows11mobile.services.WindowsNotificationListener

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    application: Application
) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val weatherRepository = WeatherRepository(context)
    val weather = weatherRepository.weather

    private val contactsRepository = com.example.windows11mobile.data.ContactsRepository.getInstance(context)
    val contacts = contactsRepository.contacts

    private val calendarRepository = com.example.windows11mobile.data.CalendarRepository(context)
    val calendarEvents = calendarRepository.events

    private val appRepository = com.example.windows11mobile.data.RealAppRepository(context)
    val installedApps = flow {
        emit(appRepository.getInstalledApps())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val newsRepository = RealNewsRepository(null)
    val topNews = flow {
        while(true) {
            emit(newsRepository.getTopHeadlines())
            delay(3600000) // 1 hour
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _homeButtonPressed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homeButtonPressed = _homeButtonPressed.asSharedFlow()

    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost = AppWidgetHost(context, APPWIDGET_HOST_ID)

    val availableWidgets = flow {
        val providers = appWidgetManager.installedProviders
        val grouped = providers.groupBy { it.provider.packageName }
        emit(grouped)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _rawTiles = MutableStateFlow<List<HomeTile>>(emptyList())
    val tiles = combine(_rawTiles, NotificationManager.notifications) { tiles, notifications ->
        tiles.map { tile ->
            val appNotification = notifications[tile.packageName]
            val lastNotification = appNotification?.recentNotifications?.firstOrNull()
            tile.copy(
                notificationCount = appNotification?.totalCount ?: 0,
                notificationSummary = lastNotification?.summary,
                notificationSender = lastNotification?.sender,
                notificationContent = lastNotification?.content,
                notificationTime = lastNotification?.postTime
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentNotifications = NotificationManager.notifications
    val currentMedia = NotificationManager.currentMedia

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private val _explodedTileId = MutableStateFlow<String?>(null)
    val explodedTileId = _explodedTileId.asStateFlow()

    private val _openFolderId = MutableStateFlow<String?>(null)
    val openFolderId = _openFolderId.asStateFlow()

    private val _isDragging = MutableStateFlow(false)
    val isDragging = _isDragging.asStateFlow()

    val tileOpacity = settingsRepository.tileOpacity.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.25f
    )

    val weatherAppPackage = settingsRepository.weatherAppPackage.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val useFahrenheit = settingsRepository.useFahrenheit.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    init {
        viewModelScope.launch {
            while(true) {
                contactsRepository.updateContacts()
                calendarRepository.updateEvents()
                delay(600000) // 10 mins
            }
        }
        viewModelScope.launch {
            useFahrenheit.collect { f ->
                weatherRepository.updateWeather(f)
            }
        }
        viewModelScope.launch {
            while(true) {
                weatherRepository.updateWeather(useFahrenheit.value)
                if (weather.value == null) {
                    delay(30000) // Retry in 30s if failed/no permission yet
                } else {
                    delay(1800000) // 30 mins
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.homeTiles.collect { json ->
                if (json != null) {
                    try {
                        val decoded = Json.decodeFromString<List<HomeTile>>(json)
                        if (decoded.isNotEmpty()) {
                            // Migration: Check if the new Clock & Weather tile is missing
                            val hasClockWeather = decoded.any { it.specialType == HomeTile.TYPE_CLOCK_WEATHER }
                            
                            if (!hasClockWeather && _rawTiles.value.isEmpty()) {
                                val migrated = listOf(
                                    HomeTile("clock_weather", null, "Clock & Weather", TileSize.WIDE, specialType = HomeTile.TYPE_CLOCK_WEATHER)
                                ) + decoded.filter { 
                                    it.specialType != HomeTile.TYPE_CLOCK && it.specialType != HomeTile.TYPE_WEATHER 
                                }
                                _rawTiles.value = migrated
                                saveTiles()
                            } else if (_rawTiles.value.isEmpty() || 
                                (_rawTiles.value != decoded && !_isEditMode.value)) {
                                
                                // FORCE SPECIAL TYPES for migration on existing tiles
                                val migrated = decoded.map { tile ->
                                    when {
                                        tile.id == "clock_weather" -> tile.copy(specialType = HomeTile.TYPE_CLOCK_WEATHER)
                                        tile.id == "photos" -> tile.copy(specialType = HomeTile.TYPE_PHOTOS)
                                        tile.id == "music" -> tile.copy(specialType = HomeTile.TYPE_MUSIC)
                                        tile.id == "settings_tile" -> tile.copy(specialType = HomeTile.TYPE_SETTINGS)
                                        else -> tile
                                    }
                                }
                                _rawTiles.value = migrated
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
        HomeTile("clock_weather", null, "Clock & Weather", TileSize.WIDE, specialType = HomeTile.TYPE_CLOCK_WEATHER),
        HomeTile("music", null, "Music", TileSize.WIDE, specialType = HomeTile.TYPE_MUSIC),
        HomeTile("photos", "com.google.android.apps.photos", "Photos", TileSize.MEDIUM, specialType = HomeTile.TYPE_PHOTOS),
        HomeTile("settings_tile", null, "Quick Settings", TileSize.MEDIUM, specialType = HomeTile.TYPE_SETTINGS),
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

    private var saveJob: kotlinx.coroutines.Job? = null
    private fun saveTiles() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(1000) // Debounce saves
            // CRITICAL: Filter out spacers before saving to persistent storage
            val tilesToSave = _rawTiles.value.filter { !it.isSpacer }
            settingsRepository.setHomeTiles(Json.encodeToString(tilesToSave))
        }
    }

    fun swapTiles(fromIndex: Int, toIndex: Int) {
        val list = _rawTiles.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _rawTiles.value = list
            // Don't save on every swap to avoid IO churn during drag
        }
    }

    fun moveTile(fromIndex: Int, toIndex: Int) {
        val list = _rawTiles.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val fromItem = list[fromIndex]
            val toItem = list[toIndex]

            if (fromIndex == toIndex) {
                saveTiles()
                return
            }

            val isEligibleMember = { tile: HomeTile -> !tile.isWidget && !tile.isSpacer }

            // Case 1: Drop onto an existing folder
            if (toItem.isFolder && isEligibleMember(fromItem)) {
                val updatedFolder = if (fromItem.isFolder) {
                    // Merge two folders
                    toItem.copy(subTiles = toItem.subTiles + fromItem.subTiles)
                } else {
                    // Add app/system tile to folder
                    toItem.copy(subTiles = toItem.subTiles + fromItem.copy(size = TileSize.SMALL))
                }
                
                list.removeAt(fromIndex)
                val adjustedToIndex = list.indexOfFirst { it.id == toItem.id }
                if (adjustedToIndex != -1) {
                    list[adjustedToIndex] = updatedFolder
                }
                _rawTiles.value = list
                saveTiles()
                return
            }

            // Case 2: Drop a folder onto an eligible app (Merge app into folder)
            if (fromItem.isFolder && isEligibleMember(toItem)) {
                val updatedFolder = fromItem.copy(
                    subTiles = fromItem.subTiles + toItem.copy(size = TileSize.SMALL)
                )
                val firstIdx = fromIndex.coerceAtMost(toIndex)
                val secondIdx = fromIndex.coerceAtLeast(toIndex)
                list.removeAt(secondIdx)
                list.removeAt(firstIdx)
                // Insert at the toIndex position (which might have shifted)
                val insertPos = if (toIndex > fromIndex) toIndex - 1 else toIndex
                list.add(insertPos.coerceIn(0, list.size), updatedFolder)
                _rawTiles.value = list
                saveTiles()
                return
            }

            // Case 3: Drop app onto app (Create new folder)
            if (isEligibleMember(fromItem) && isEligibleMember(toItem)) {
                val newFolder = HomeTile(
                    id = UUID.randomUUID().toString(),
                    label = "New Folder",
                    isFolder = true,
                    size = TileSize.MEDIUM,
                    subTiles = listOf(toItem.copy(size = TileSize.SMALL), fromItem.copy(size = TileSize.SMALL))
                )
                val firstIdx = fromIndex.coerceAtMost(toIndex)
                val secondIdx = fromIndex.coerceAtLeast(toIndex)
                list.removeAt(secondIdx)
                list.removeAt(firstIdx)
                val insertPos = if (toIndex > fromIndex) toIndex - 1 else toIndex
                list.add(insertPos.coerceIn(0, list.size), newFolder)
                _rawTiles.value = list
                saveTiles()
                return
            }

            // Otherwise, just reorder
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _rawTiles.value = list
            saveTiles()
        }
    }

    fun removeTileFromFolder(folderId: String, tileId: String, toIndex: Int = -1) {
        val list = _rawTiles.value.toMutableList()
        val folderIndex = list.indexOfFirst { it.id == folderId }
        if (folderIndex == -1) return

        val folder = list[folderIndex]
        val tileToRemove = folder.subTiles.find { it.id == tileId } ?: return
        
        val updatedSubTiles = folder.subTiles.filter { it.id != tileId }
        
        if (updatedSubTiles.isEmpty()) {
            // Remove folder entirely if empty
            list.removeAt(folderIndex)
        } else if (updatedSubTiles.size == 1) {
            // Dissolve folder if only one item left
            val remainingItem = updatedSubTiles[0].copy(size = TileSize.MEDIUM)
            list[folderIndex] = remainingItem
        } else {
            // Update folder with remaining items
            list[folderIndex] = folder.copy(subTiles = updatedSubTiles)
        }

        // If toIndex is provided, place the extracted tile back into the main grid
        if (toIndex != -1) {
            val restoredTile = tileToRemove.copy(size = TileSize.MEDIUM)
            val finalIndex = if (toIndex > folderIndex && updatedSubTiles.size <= 1) toIndex - 1 else toIndex
            list.add(finalIndex.coerceIn(0, list.size), restoredTile)
        }

        _rawTiles.value = list
        saveTiles()
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
        if (enabled) {
            _explodedTileId.value = null
            // Pad with spacers to fill EVERY empty slot and extra rows
            val current = _rawTiles.value.toMutableList()
            // Assume COMPACT 4 columns, MEDIUM 6, etc. 
            // We'll add enough spacers to fill at least 40 slots (enough for 10 rows on compact)
            val totalDesiredSlots = 40
            val existingCount = current.size
            if (existingCount < totalDesiredSlots) {
                repeat(totalDesiredSlots - existingCount) {
                    current.add(HomeTile(id = "spacer_${UUID.randomUUID()}", label = "", isSpacer = true, size = TileSize.SMALL))
                }
            }
            _rawTiles.value = current
        } else {
            // Remove spacers when leaving edit mode
            _rawTiles.value = _rawTiles.value.filter { !it.isSpacer }
            saveTiles()
        }
    }

    fun explodeTile(id: String?) {
        _explodedTileId.value = id
        if (id != null) _isEditMode.value = false
    }

    fun openFolder(id: String?) {
        _openFolderId.value = id
    }

    fun setIsDragging(dragging: Boolean) {
        _isDragging.value = dragging
        if (dragging) {
            _explodedTileId.value = null
        }
    }

    fun renameFolder(id: String, newName: String) {
        _rawTiles.value = _rawTiles.value.map {
            if (it.id == id) it.copy(label = newName) else it
        }
        saveTiles()
    }

    fun addTile(packageName: String, label: String) {
        val openId = _openFolderId.value
        if (openId != null) {
            val list = _rawTiles.value.toMutableList()
            val folderIndex = list.indexOfFirst { it.id == openId }
            if (folderIndex != -1) {
                val folder = list[folderIndex]
                val newSubTile = HomeTile(
                    id = UUID.randomUUID().toString(),
                    packageName = packageName,
                    label = label,
                    size = TileSize.SMALL
                )
                list[folderIndex] = folder.copy(subTiles = folder.subTiles + newSubTile)
                _rawTiles.value = list
                saveTiles()
                return
            }
        }
        
        val newTile = HomeTile(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            label = label,
            size = TileSize.MEDIUM
        )
        _rawTiles.value = _rawTiles.value + newTile
        saveTiles()
    }

    fun addEmptyFolder(label: String) {
        val newFolder = HomeTile(
            id = UUID.randomUUID().toString(),
            label = label,
            isFolder = true,
            size = TileSize.MEDIUM
        )
        _rawTiles.value = _rawTiles.value + newFolder
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
        android.util.Log.d("HomeViewModel", "Removing tile: $id")
        val currentList = _rawTiles.value
        
        // Find in main list or subfolders
        val tile = currentList.find { it.id == id } ?: currentList.flatMap { it.subTiles }.find { it.id == id }
        
        if (tile?.isWidget == true && tile.widgetId != null) {
            appWidgetHost.deleteAppWidgetId(tile.widgetId)
        }
        
        // Recursive removal
        val newList = currentList.filter { it.id != id }.map { 
            if (it.isFolder) {
                val updatedSubTiles = it.subTiles.filter { sub -> sub.id != id }
                it.copy(subTiles = updatedSubTiles)
            } else it
        }.filter { !it.isFolder || it.subTiles.isNotEmpty() } // Clean up empty folders if any
        
        _rawTiles.value = newList
        _explodedTileId.value = null
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

    fun setWeatherAppPackage(packageName: String?) {
        viewModelScope.launch {
            settingsRepository.setWeatherAppPackage(packageName)
        }
    }

    fun clearNotifications(packageName: String) {
        NotificationManager.clearNotifications(packageName)
    }

    fun onHomeButtonPressed() {
        _homeButtonPressed.tryEmit(Unit)
    }

    fun mediaPlayPause() {
        val intent = Intent(context, WindowsNotificationListener::class.java).apply {
            action = WindowsNotificationListener.ACTION_MEDIA_PLAY_PAUSE
        }
        context.startService(intent)
    }

    fun mediaSkipNext() {
        val intent = Intent(context, WindowsNotificationListener::class.java).apply {
            action = WindowsNotificationListener.ACTION_MEDIA_SKIP_NEXT
        }
        context.startService(intent)
    }

    fun mediaSkipPrevious() {
        val intent = Intent(context, WindowsNotificationListener::class.java).apply {
            action = WindowsNotificationListener.ACTION_MEDIA_SKIP_PREVIOUS
        }
        context.startService(intent)
    }

    fun refreshNotifications() {
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
