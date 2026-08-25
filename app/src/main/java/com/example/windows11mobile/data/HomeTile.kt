package com.example.windows11mobile.data

import kotlinx.serialization.Serializable

@Serializable
enum class TileSize(val spanX: Int, val spanY: Int) {
    SMALL(1, 1),
    MEDIUM(2, 2),
    WIDE(4, 2),
    LARGE(4, 4)
}

@Serializable
data class HomeTile(
    val id: String,
    val packageName: String? = null,
    val label: String,
    val size: TileSize = TileSize.MEDIUM,
    val widgetId: Int? = null,
    val isWidget: Boolean = false,
    val specialType: String? = null,
    val notificationCount: Int = 0,
    val notificationSummary: String? = null,
    val notificationSender: String? = null,
    val notificationContent: String? = null,
    val notificationTime: Long? = null
) {
    val spanX: Int get() = size.spanX
    val spanY: Int get() = size.spanY

    companion object {
        const val TYPE_CLOCK = "system_clock"
        const val TYPE_WEATHER = "system_weather"
        const val TYPE_PHOTOS = "system_photos"
    }
}
