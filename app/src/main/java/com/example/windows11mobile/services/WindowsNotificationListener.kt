package com.example.windows11mobile.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.windows11mobile.data.NotificationData
import com.example.windows11mobile.data.NotificationManager

class WindowsNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateNotifications()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        updateNotifications()
    }

    private fun updateNotifications() {
        val activeNotifications = activeNotifications ?: return
        
        // Group by package name
        val grouped = activeNotifications.groupBy { it.packageName }
        
        val notificationDataMap = grouped.mapValues { (packageName, notifications) ->
            val count = notifications.size
            val lastNotification = notifications.lastOrNull()
            val extras = lastNotification?.notification?.extras
            val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val postTime = lastNotification?.postTime ?: 0L
            
            val summary = if (title != null && text != null) {
                "$title: $text"
            } else {
                title ?: text
            }
            
            NotificationData(
                packageName = packageName,
                count = count,
                summary = summary,
                sender = title,
                content = text,
                postTime = postTime
            )
        }
        
        NotificationManager.updateAll(notificationDataMap)
    }
}
