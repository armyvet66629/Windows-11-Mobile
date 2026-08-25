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

object NotificationManager {
    private val _notifications = MutableStateFlow<Map<String, NotificationData>>(emptyMap())
    val notifications = _notifications.asStateFlow()

    fun updateAll(data: Map<String, NotificationData>) {
        _notifications.value = data
    }
}
