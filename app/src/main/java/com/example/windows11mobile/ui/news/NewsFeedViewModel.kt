package com.example.windows11mobile.ui.news

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.windows11mobile.data.HomeTile
import com.example.windows11mobile.data.NewsArticle
import com.example.windows11mobile.data.NewsRepository
import com.example.windows11mobile.data.SettingsRepository
import com.example.windows11mobile.data.CalendarRepository
import com.example.windows11mobile.data.CalendarEvent
import com.example.windows11mobile.data.NotificationManager
import com.example.windows11mobile.data.TileSize
import com.example.windows11mobile.services.WindowsNotificationListener
import android.content.Intent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class TodoTask(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false
)

class NewsFeedViewModel(
    private val repository: NewsRepository,
    private val rssRepository: com.example.windows11mobile.data.RssRepository,
    private val settingsRepository: SettingsRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val calendarRepository = CalendarRepository(context)
    val calendarEvents = calendarRepository.events

    private val photosRepository = com.example.windows11mobile.data.PhotosRepository(context)
    val recentPhotos = flow {
        while(true) {
            emit(photosRepository.getRecentPhotos(20))
            delay(120000) // 2 mins
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _tasks = MutableStateFlow<List<TodoTask>>(emptyList())
    val tasks: StateFlow<List<TodoTask>> = _tasks.asStateFlow()

    private val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())
    val articles: StateFlow<List<NewsArticle>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _boardWidgets = MutableStateFlow<List<HomeTile>>(emptyList())
    
    val hiddenNativeWidgets = settingsRepository.hiddenNativeWidgets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptySet()
    )

    val boardWidgets: StateFlow<List<HomeTile>> = combine(_boardWidgets, hiddenNativeWidgets) { widgets, hidden ->
        widgets.filter { it.specialType == null || !hidden.contains(it.specialType) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMedia = NotificationManager.currentMedia

    private val appWidgetManager = AppWidgetManager.getInstance(context)

    val availableWidgets = flow {
        val providers = appWidgetManager.installedProviders
        val grouped = providers.groupBy { it.provider.packageName }
        emit(grouped)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val preferredCategories = settingsRepository.preferredNewsCategories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsRepository.DEFAULT_NEWS_CATEGORIES
    )

    val rssFeeds = settingsRepository.rssFeeds.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsRepository.DEFAULT_RSS_FEEDS
    )

    val tileOpacity = settingsRepository.tileOpacity.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.25f
    )

    init {
        viewModelScope.launch {
            while(true) {
                calendarRepository.updateEvents()
                kotlinx.coroutines.delay(600000) // 10 mins
            }
        }

        viewModelScope.launch {
            combine(preferredCategories, rssFeeds) { _, _ -> }.collect {
                refresh()
            }
        }

        viewModelScope.launch {
            settingsRepository.rssFeeds.first().let { feeds ->
                if (!feeds.contains("https://blackhawkup.com/posts/feed/")) {
                    settingsRepository.addRssFeed("https://blackhawkup.com/posts/feed/")
                }
            }
        }

        viewModelScope.launch {
            settingsRepository.boardWidgets.collect { json ->
                if (json != null) {
                    try {
                        var decoded: List<HomeTile> = Json.decodeFromString(json)
                        var changed = false
                        
                        // Migration: Add music widget if missing or move to top
                        val musicWidget = decoded.find { it.specialType == "music" }
                        if (musicWidget == null) {
                            decoded = listOf(HomeTile("music_widget", null, "Music", TileSize.WIDE, specialType = "music")) + decoded
                            changed = true
                        } else if (decoded.indexOf(musicWidget) != 0) {
                            // Move existing music widget to top if requested
                            decoded = listOf(musicWidget) + decoded.filter { it.specialType != "music" }
                            changed = true
                        }
                        
                        // Migration: Ensure Photos widget is WIDE and all App Widgets are at least MEDIUM
                        decoded = decoded.map { 
                            if (it.specialType == "photos" && it.size != TileSize.WIDE) {
                                changed = true
                                it.copy(size = TileSize.WIDE)
                            } else if (it.isWidget && it.size == TileSize.SMALL) {
                                changed = true
                                it.copy(size = TileSize.WIDE)
                            } else it
                        }
                        
                        // Migration: Add photos widget if missing
                        if (!decoded.any { it.specialType == "photos" }) {
                            decoded = decoded + HomeTile("photos_widget", null, "Photos", TileSize.WIDE, specialType = "photos")
                            changed = true
                        }
                        
                        // Migration: Add system toggles if missing
                        if (!decoded.any { it.specialType == "system" }) {
                            decoded = decoded + HomeTile("system_widget", null, "System Settings", TileSize.WIDE, specialType = "system")
                            changed = true
                        }
                        
                        if (changed) {
                            saveBoardWidgets(decoded)
                        }
                        _boardWidgets.value = decoded
                    } catch (e: Exception) {
                        _boardWidgets.value = getDefaultBoardWidgets()
                    }
                } else if (_boardWidgets.value.isEmpty()) {
                    _boardWidgets.value = getDefaultBoardWidgets()
                }
            }
        }
    }

    private fun getDefaultBoardWidgets() = listOf(
        HomeTile("music_widget", null, "Music", TileSize.WIDE, specialType = "music"),
        HomeTile("calendar", null, "Calendar", specialType = "calendar"),
        HomeTile("tasks", null, "To Do", specialType = "tasks"),
        HomeTile("photos_widget", null, "Photos", TileSize.WIDE, specialType = "photos"),
        HomeTile("system_widget", null, "System Toggles", TileSize.WIDE, specialType = "system")
    )

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

    fun addBoardWidget(widgetId: Int, label: String) {
        val newWidget = HomeTile(
            id = UUID.randomUUID().toString(),
            label = label,
            size = TileSize.WIDE, // Force WIDE for new widgets
            widgetId = widgetId,
            isWidget = true
        )
        val updated = _boardWidgets.value + newWidget
        _boardWidgets.value = updated
        saveBoardWidgets(updated)
    }

    fun removeBoardWidget(id: String) {
        val updated = _boardWidgets.value.filter { it.id != id }
        _boardWidgets.value = updated
        saveBoardWidgets(updated)
    }

    fun resizeBoardWidget(id: String, newSize: com.example.windows11mobile.data.TileSize) {
        val updated = _boardWidgets.value.map {
            if (it.id == id) it.copy(size = newSize) else it
        }
        _boardWidgets.value = updated
        saveBoardWidgets(updated)
    }

    fun moveBoardWidget(fromIndex: Int, toIndex: Int) {
        val current = _boardWidgets.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _boardWidgets.value = current
            saveBoardWidgets(current)
        }
    }

    private fun saveBoardWidgets(widgets: List<HomeTile>) {
        viewModelScope.launch {
            settingsRepository.setBoardWidgets(Json.encodeToString(widgets))
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val feedUrls = rssFeeds.value
                android.util.Log.d("NewsFeedViewModel", "Refreshing feeds. Count: ${feedUrls.size}. URLs: $feedUrls")
                
                val newsApiArticles = try {
                    repository.getTopHeadlines(preferredCategories.value)
                } catch (e: Exception) {
                    android.util.Log.e("NewsFeedViewModel", "NewsAPI Error", e)
                    emptyList()
                }
                
                val rssArticles = try {
                    android.util.Log.d("NewsFeedViewModel", "Calling rssRepository.fetchFeeds...")
                    rssRepository.fetchFeeds(feedUrls)
                } catch (e: Exception) {
                    android.util.Log.e("NewsFeedViewModel", "RSS Fetch Error", e)
                    emptyList()
                }
                
                android.util.Log.d("NewsFeedViewModel", "Combined Results: ${newsApiArticles.size} NewsAPI, ${rssArticles.size} RSS")
                
                val combined = (newsApiArticles + rssArticles).sortedByDescending { it.publishedAt }
                val result = combined.distinctBy { it.url }
                android.util.Log.d("NewsFeedViewModel", "Emitting ${result.size} articles to UI")
                _articles.value = result
            } catch (e: Exception) {
                android.util.Log.e("NewsFeedViewModel", "Refresh Global Error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategories(categories: Set<String>) {
        viewModelScope.launch {
            settingsRepository.setNewsCategories(categories)
        }
    }

    fun addRssFeed(url: String) {
        viewModelScope.launch {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url
            settingsRepository.addRssFeed(formattedUrl)
        }
    }

    fun removeRssFeed(url: String) {
        viewModelScope.launch {
            settingsRepository.removeRssFeed(url)
        }
    }

    fun addTask(text: String) {
        val newTask = TodoTask(text = text)
        _tasks.value = _tasks.value + newTask
    }

    fun toggleTask(id: String) {
        _tasks.value = _tasks.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun removeTask(id: String) {
        _tasks.value = _tasks.value.filter { it.id != id }
    }

    fun clearTasks() {
        _tasks.value = emptyList()
    }
}

class NewsFeedViewModelFactory(
    private val repository: NewsRepository,
    private val rssRepository: com.example.windows11mobile.data.RssRepository,
    private val settingsRepository: SettingsRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsFeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsFeedViewModel(repository, rssRepository, settingsRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
