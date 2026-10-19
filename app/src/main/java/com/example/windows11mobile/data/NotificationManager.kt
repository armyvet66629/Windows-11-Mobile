package com.example.windows11mobile.data

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationData(
    val packageName: String,
    val count: Int,
    val summary: String?,
    val sender: String? = null,
    val content: String? = null,
    val postTime: Long = 0L,
    val largeIcon: Bitmap? = null,
    val bigPicture: Bitmap? = null,
    val address: String? = null
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
        Log.d("NotificationManager", "updateAll: posting ${data.size} app notifications -> ${data.mapValues { it.value.totalCount }}")
        _notifications.value = data
    }

    fun updateMedia(data: MediaData) {
        _currentMedia.value = data
    }

    fun clearNotifications(packageName: String) {
        Log.d("NotificationManager", "clearNotifications for $packageName")
        _notifications.value = _notifications.value.filterKeys { it != packageName }
    }
}
