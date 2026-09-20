package com.example.windows11mobile.ui.home

import android.app.Application
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.windows11mobile.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import com.example.windows11mobile.services.WindowsNotificationListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val rssRepository: RssRepository,
    application: Application
) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val weatherRepository = WeatherRepository(context)
    val weather = weatherRepository.weather

    private val contactsRepository = ContactsRepository.getInstance(context)
    val contacts = contactsRepository.contacts

    private val calendarRepository = CalendarRepository(context)
    val calendarEvents = calendarRepository.events

    private val appRepository = RealAppRepository(context)
    private val photosRepository = PhotosRepository(context)
    
    val installedApps = appRepository.observeApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _photosTrigger = MutableStateFlow(0L)
    @OptIn(ExperimentalCoroutinesApi::class)
    val recentPhotos = _photosTrigger.flatMapLatest {
        flow {
            while(true) {
                val photos = photosRepository.getRecentPhotos(20)
                emit(photos)
                delay(120000) // 2 mins
            }
        }
    }.distinctUntilChanged()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshPhotos() {
        _photosTrigger.value = System.currentTimeMillis()
    }
    
    private val newsRepository = RealNewsRepository(null)
    private val _newsTrigger = MutableStateFlow(0L)
    @OptIn(ExperimentalCoroutinesApi::class)
    val topNews = combine(settingsRepository.rssFeeds, _newsTrigger) { feeds, _ -> feeds }
    .flatMapLatest { urls ->
        flow {
            while(true) {
                val newsApiArticles = try {
                    newsRepository.getTopHeadlines()
                } catch (e: Exception) {
                    emptyList()
                }
                
                val rssArticles = try {
                    rssRepository.fetchFeeds(urls)
                } catch (e: Exception) {
                    emptyList()
                }
                
                val combined = (newsApiArticles + rssArticles).sortedByDescending { it.publishedAt }
                val distinct = combined.distinctBy { it.url }
                
                emit(distinct)
                delay(1800000) // 30 mins
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshNews() {
        _newsTrigger.value = System.currentTimeMillis()
    }

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
    val tiles = combine(_rawTiles, NotificationManager.notifications, settingsRepository.hiddenNativeWidgets) { tiles, notifications, hidden ->
        fun mapTile(tile: HomeTile): HomeTile {
            val appNotification = notifications[tile.packageName]
            val lastNotification = appNotification?.recentNotifications?.firstOrNull()
            
            val updatedSubTiles = tile.subTiles.map { mapTile(it) }
            val totalCount = if (tile.isFolder) {
                updatedSubTiles.sumOf { it.notificationCount }
            } else {
                appNotification?.totalCount ?: 0
            }
            
            return tile.copy(
                notificationCount = totalCount,
                notificationSummary = lastNotification?.summary,
                notificationSender = lastNotification?.sender,
                notificationContent = lastNotification?.content,
                notificationTime = lastNotification?.postTime,
                subTiles = updatedSubTiles
            )
        }
        
        tiles.filter { it.specialType == null || !hidden.contains(it.specialType) }.map { mapTile(it) }
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
        SharingStarted.Eagerly,
        0.25f
    )

    val weatherAppPackage = settingsRepository.weatherAppPackage.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val wallpaperUri = settingsRepository.wallpaperUri.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val useFahrenheit = settingsRepository.useFahrenheit.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val swipeDownForNotifications = settingsRepository.swipeDownForNotifications.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val showMoreTiles = settingsRepository.showMoreTiles.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val tilePictureEnabled = settingsRepository.tilePictureEnabled.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )

    val tileBlurRadius = settingsRepository.tileBlurRadius.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        80f
    )

    val homeScreenBlurEnabled = settingsRepository.homeScreenBlurEnabled.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true
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
                            val hasClockWeather = decoded.any { it.specialType == HomeTile.TYPE_CLOCK_WEATHER }
                            if (!hasClockWeather && _rawTiles.value.isEmpty()) {
                                val migrated = listOf(
                                    HomeTile("clock_weather", null, "Clock & Weather", TileSize.WIDE, specialType = HomeTile.TYPE_CLOCK_WEATHER)
                                ) + decoded.filter { 
                                    it.specialType != HomeTile.TYPE_CLOCK && it.specialType != HomeTile.TYPE_WEATHER 
                                }
                                _rawTiles.value = migrated
                                saveTiles()
                            } else if (_rawTiles.value.isEmpty() || (_rawTiles.value != decoded && !_isEditMode.value)) {
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

    private var saveJob: Job? = null
    private fun saveTiles() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(1000)
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
        }
    }

    fun moveTile(fromIndex: Int, toIndex: Int) {
        val list = _rawTiles.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val fromItem = list[fromIndex]
            val toItem = list[toIndex]
            if (fromIndex == toIndex) { saveTiles(); return }
            val isEligibleMember = { tile: HomeTile -> !tile.isWidget && !tile.isSpacer }
            if (toItem.isFolder && isEligibleMember(fromItem)) {
                val updatedFolder = if (fromItem.isFolder) toItem.copy(subTiles = toItem.subTiles + fromItem.subTiles) else toItem.copy(subTiles = toItem.subTiles + fromItem.copy(size = TileSize.SMALL))
                list.removeAt(fromIndex)
                val adjustedToIndex = list.indexOfFirst { it.id == toItem.id }
                if (adjustedToIndex != -1) list[adjustedToIndex] = updatedFolder
                _rawTiles.value = list; saveTiles(); return
            }
            if (fromItem.isFolder && isEligibleMember(toItem)) {
                val updatedFolder = fromItem.copy(subTiles = fromItem.subTiles + toItem.copy(size = TileSize.SMALL))
                val firstIdx = fromIndex.coerceAtMost(toIndex); val secondIdx = fromIndex.coerceAtLeast(toIndex)
                list.removeAt(secondIdx); list.removeAt(firstIdx)
                val insertPos = if (toIndex > fromIndex) toIndex - 1 else toIndex
                list.add(insertPos.coerceIn(0, list.size), updatedFolder)
                _rawTiles.value = list; saveTiles(); return
            }
            if (isEligibleMember(fromItem) && isEligibleMember(toItem)) {
                val newFolder = HomeTile(id = UUID.randomUUID().toString(), label = "New Folder", isFolder = true, size = TileSize.MEDIUM, subTiles = listOf(toItem.copy(size = TileSize.SMALL), fromItem.copy(size = TileSize.SMALL)))
                val firstIdx = fromIndex.coerceAtMost(toIndex); val secondIdx = fromIndex.coerceAtLeast(toIndex)
                list.removeAt(secondIdx); list.removeAt(firstIdx)
                val insertPos = if (toIndex > fromIndex) toIndex - 1 else toIndex
                list.add(insertPos.coerceIn(0, list.size), newFolder)
                _rawTiles.value = list; saveTiles(); return
            }
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _rawTiles.value = list; saveTiles()
        }
    }

    fun removeTileFromFolder(folderId: String, tileId: String, toIndex: Int = -1) {
        val list = _rawTiles.value.toMutableList()
        val folderIndex = list.indexOfFirst { it.id == folderId }
        if (folderIndex == -1) return
        val folder = list[folderIndex]
        val tileToRemove = folder.subTiles.find { it.id == tileId } ?: return
        val updatedSubTiles = folder.subTiles.filter { it.id != tileId }
        if (updatedSubTiles.isEmpty()) list.removeAt(folderIndex)
        else if (updatedSubTiles.size == 1) list[folderIndex] = updatedSubTiles[0].copy(size = TileSize.MEDIUM)
        else list[folderIndex] = folder.copy(subTiles = updatedSubTiles)
        if (toIndex != -1) {
            val restoredTile = tileToRemove.copy(size = TileSize.MEDIUM)
            val finalIndex = if (toIndex > folderIndex && updatedSubTiles.size <= 1) toIndex - 1 else toIndex
            list.add(finalIndex.coerceIn(0, list.size), restoredTile)
        }
        _rawTiles.value = list; saveTiles()
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
        _rawTiles.value = _rawTiles.value.map { if (it.id == id) it.copy(size = newSize) else it }
        saveTiles()
    }

    fun setEditMode(enabled: Boolean) {
        _isEditMode.value = enabled
        if (enabled) {
            _explodedTileId.value = null
            val current = _rawTiles.value.toMutableList()
            val totalDesiredSlots = 40
            if (current.size < totalDesiredSlots) {
                repeat(totalDesiredSlots - current.size) {
                    current.add(HomeTile(id = "spacer_${UUID.randomUUID()}", label = "", isSpacer = true, size = TileSize.SMALL))
                }
            }
            _rawTiles.value = current
        } else {
            _rawTiles.value = _rawTiles.value.filter { !it.isSpacer }
            saveTiles()
        }
    }

    fun explodeTile(id: String?) {
        _explodedTileId.value = id
        if (id != null) _isEditMode.value = false
    }

    fun openFolder(id: String?) { _openFolderId.value = id }

    fun setIsDragging(dragging: Boolean) {
        _isDragging.value = dragging
        if (dragging) _explodedTileId.value = null
    }

    fun renameFolder(id: String, newName: String) {
        _rawTiles.value = _rawTiles.value.map { if (it.id == id) it.copy(label = newName) else it }
        saveTiles()
    }

    fun addTile(packageName: String, label: String) {
        val openId = _openFolderId.value
        if (openId != null) {
            val list = _rawTiles.value.toMutableList()
            val folderIndex = list.indexOfFirst { it.id == openId }
            if (folderIndex != -1) {
                val folder = list[folderIndex]
                val newSubTile = HomeTile(id = UUID.randomUUID().toString(), packageName = packageName, label = label, size = TileSize.SMALL)
                list[folderIndex] = folder.copy(subTiles = folder.subTiles + newSubTile)
                _rawTiles.value = list; saveTiles(); return
            }
        }
        val newTile = HomeTile(id = UUID.randomUUID().toString(), packageName = packageName, label = label, size = TileSize.MEDIUM)
        _rawTiles.value = _rawTiles.value + newTile; saveTiles()
    }

    fun addEmptyFolder(label: String) {
        val newFolder = HomeTile(id = UUID.randomUUID().toString(), label = label, isFolder = true, size = TileSize.MEDIUM)
        _rawTiles.value = _rawTiles.value + newFolder; saveTiles()
    }

    fun addWidgetTile(widgetId: Int, label: String) {
        val newTile = HomeTile(id = UUID.randomUUID().toString(), label = label, size = TileSize.WIDE, widgetId = widgetId, isWidget = true)
        _rawTiles.value = _rawTiles.value + newTile; saveTiles()
    }

    fun removeTile(id: String) {
        val currentList = _rawTiles.value
        val tile = currentList.find { it.id == id } ?: currentList.flatMap { it.subTiles }.find { it.id == id }
        if (tile?.isWidget == true && tile.widgetId != null) appWidgetHost.deleteAppWidgetId(tile.widgetId)
        val newList = currentList.filter { it.id != id }.map { 
            if (it.isFolder) it.copy(subTiles = it.subTiles.filter { sub -> sub.id != id }) else it
        }.filter { !it.isFolder || it.subTiles.isNotEmpty() }
        _rawTiles.value = newList; _explodedTileId.value = null; saveTiles()
    }

    fun getShortcuts(packageName: String): List<ShortcutInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()
        val query = LauncherApps.ShortcutQuery().apply { setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED); setPackage(packageName) }
        return try { launcherApps.getShortcuts(query, Process.myUserHandle()) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    fun launchShortcut(shortcut: ShortcutInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return
        try { launcherApps.startShortcut(shortcut, null, null) } catch (e: Exception) {}
    }

    fun startWidgetListening() { appWidgetHost.startListening() }
    fun stopWidgetListening() { appWidgetHost.stopListening() }
    fun allocateWidgetId(): Int { return appWidgetHost.allocateAppWidgetId() }
    fun setWeatherAppPackage(packageName: String?) { viewModelScope.launch { settingsRepository.setWeatherAppPackage(packageName) } }
    fun clearNotifications(packageName: String) { NotificationManager.clearNotifications(packageName) }

    fun onHomeButtonPressed() {
        _homeButtonPressed.tryEmit(Unit)
        refreshPhotos()
        refreshNews()
        viewModelScope.launch { contactsRepository.updateRecentActivity() }
    }

    fun mediaPlayPause() { context.startService(Intent(context, WindowsNotificationListener::class.java).apply { action = WindowsNotificationListener.ACTION_MEDIA_PLAY_PAUSE }) }
    fun mediaSkipNext() { context.startService(Intent(context, WindowsNotificationListener::class.java).apply { action = WindowsNotificationListener.ACTION_MEDIA_SKIP_NEXT }) }
    fun mediaSkipPrevious() { context.startService(Intent(context, WindowsNotificationListener::class.java).apply { action = WindowsNotificationListener.ACTION_MEDIA_SKIP_PREVIOUS }) }

    fun expandNotifications() {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val expandMethod = statusBarManager.getMethod("expandNotificationsPanel")
            expandMethod.invoke(statusBarService)
        } catch (e: Exception) { Log.e("HomeViewModel", "Failed to expand notifications", e) }
    }

    companion object { private const val APPWIDGET_HOST_ID = 1024 }
}

class HomeViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val rssRepository: RssRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(settingsRepository, rssRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
