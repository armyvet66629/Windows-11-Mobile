package com.example.windows11mobile.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationData(
    val packageName: String,
    val count: Int,
    val summary: String?,
    val sender: String? = null,
    val content: String? = null,
    val postTime: Long = 0L
)

data class AppNotificationData(
    val packageName: String,
    val totalCount: Int,
    val recentNotifications: List<NotificationData>
)

data class MediaData(
    val title: String? = null,
    val artist: String? = null,
    val albumArt: android.graphics.Bitmap? = null,
    val isPlaying: Boolean = false,
    val packageName: String? = null
)

object NotificationManager {
    private val _notifications = MutableStateFlow<Map<String, AppNotificationData>>(emptyMap())
    val notifications = _notifications.asStateFlow()

    private val _currentMedia = MutableStateFlow(MediaData())
    val currentMedia = _currentMedia.asStateFlow()

    fun updateAll(data: Map<String, AppNotificationData>) {
        _notifications.value = data
    }

    fun updateMedia(data: MediaData) {
        _currentMedia.value = data
    }

    fun clearNotifications(packageName: String) {
        _notifications.value = _notifications.value.filterKeys { it != packageName }
    }
}
