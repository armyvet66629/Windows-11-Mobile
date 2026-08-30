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

    private val _tasks = MutableStateFlow<List<TodoTask>>(emptyList())
    val tasks: StateFlow<List<TodoTask>> = _tasks.asStateFlow()

    private val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())
    val articles: StateFlow<List<NewsArticle>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _boardWidgets = MutableStateFlow<List<HomeTile>>(emptyList())
    val boardWidgets: StateFlow<List<HomeTile>> = _boardWidgets.asStateFlow()

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
                        val decoded: List<HomeTile> = Json.decodeFromString(json)
                        // Migration: Add music widget if missing
                        if (!decoded.any { it.specialType == "music" }) {
                            val migrated = listOf(
                                HomeTile("music_widget", null, "Music", TileSize.WIDE, specialType = "music")
                            ) + decoded
                            _boardWidgets.value = migrated
                            saveBoardWidgets(migrated)
                        } else {
                            _boardWidgets.value = decoded
                        }
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
        HomeTile("tasks", null, "To Do", specialType = "tasks")
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

    private fun saveBoardWidgets(widgets: List<HomeTile>) {
        viewModelScope.launch {
            settingsRepository.setBoardWidgets(Json.encodeToString(widgets))
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val newsApiArticles = repository.getTopHeadlines(preferredCategories.value)
            val rssArticles = rssRepository.fetchFeeds(rssFeeds.value)
            
            val combined = (newsApiArticles + rssArticles).sortedByDescending { it.publishedAt }
            _articles.value = combined.distinctBy { it.url }
            _isLoading.value = false
        }
    }

    fun updateCategories(categories: Set<String>) {
        viewModelScope.launch {
            settingsRepository.setNewsCategories(categories)
        }
    }

    fun addRssFeed(url: String) {
        viewModelScope.launch {
            settingsRepository.addRssFeed(url)
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
