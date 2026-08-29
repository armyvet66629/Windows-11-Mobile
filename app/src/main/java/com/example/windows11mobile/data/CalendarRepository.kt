package com.example.windows11mobile.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.*

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val color: Int
)

class CalendarRepository(private val context: Context) {
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events

    suspend fun updateEvents() = withContext(Dispatchers.IO) {
        val eventsList = mutableListOf<CalendarEvent>()
        
        val now = System.currentTimeMillis()
        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, now)
        ContentUris.appendId(builder, now + 24 * 60 * 60 * 1000 * 7) // Next 7 days

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.DISPLAY_COLOR
        )

        try {
            context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                CalendarContract.Instances.BEGIN + " ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                val titleIdx = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
                val beginIdx = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endIdx = cursor.getColumnIndex(CalendarContract.Instances.END)
                val colorIdx = cursor.getColumnIndex(CalendarContract.Instances.DISPLAY_COLOR)

                while (cursor.moveToNext()) {
                    eventsList.add(
                        CalendarEvent(
                            id = cursor.getLong(idIdx),
                            title = cursor.getString(titleIdx) ?: "No Title",
                            startTime = cursor.getLong(beginIdx),
                            endTime = cursor.getLong(endIdx),
                            color = cursor.getInt(colorIdx)
                        )
                    )
                }
            }
            _events.value = eventsList
        } catch (e: SecurityException) {
            // No permission
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
