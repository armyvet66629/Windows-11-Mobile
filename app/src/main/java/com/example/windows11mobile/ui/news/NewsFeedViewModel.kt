package com.example.windows11mobile.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.windows11mobile.data.NewsArticle
import com.example.windows11mobile.data.NewsRepository
import com.example.windows11mobile.data.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewsFeedViewModel(
    private val repository: NewsRepository,
    private val rssRepository: com.example.windows11mobile.data.RssRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())
    val articles: StateFlow<List<NewsArticle>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
            combine(preferredCategories, rssFeeds) { _, _ -> }.collect {
                refresh()
            }
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
}

class NewsFeedViewModelFactory(
    private val repository: NewsRepository,
    private val rssRepository: com.example.windows11mobile.data.RssRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsFeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsFeedViewModel(repository, rssRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
