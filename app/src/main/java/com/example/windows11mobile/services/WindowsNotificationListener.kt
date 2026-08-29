package com.example.windows11mobile.services

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.windows11mobile.data.AppNotificationData
import com.example.windows11mobile.data.MediaData
import com.example.windows11mobile.data.NotificationData
import com.example.windows11mobile.data.NotificationManager

class WindowsNotificationListener : NotificationListenerService() {

    private var mediaSessionManager: MediaSessionManager? = null
    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateMediaState(controllers)
    }

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            refreshMedia()
        }
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            refreshMedia()
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    }

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
        
        mediaSessionManager?.let {
            val componentName = ComponentName(this, WindowsNotificationListener::class.java)
            it.addOnActiveSessionsChangedListener(sessionListener, componentName)
            updateMediaState(it.getActiveSessions(componentName))
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_MEDIA_PLAY_PAUSE -> handleMediaAction(MEDIA_ACTION_PLAY_PAUSE)
            ACTION_MEDIA_SKIP_NEXT -> handleMediaAction(MEDIA_ACTION_SKIP_NEXT)
            ACTION_MEDIA_SKIP_PREVIOUS -> handleMediaAction(MEDIA_ACTION_SKIP_PREVIOUS)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var activeControllers: List<MediaController> = emptyList()

    private fun updateMediaState(controllers: List<MediaController>?) {
        activeControllers.forEach { it.unregisterCallback(callback) }
        activeControllers = controllers ?: emptyList()
        activeControllers.forEach { it.registerCallback(callback) }
        refreshMedia()
    }

    private fun handleMediaAction(action: Int) {
        val controller = activeControllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: activeControllers.firstOrNull()
        
        controller?.transportControls?.let { transport ->
            when (action) {
                MEDIA_ACTION_PLAY_PAUSE -> {
                    if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
                        transport.pause()
                    } else {
                        transport.play()
                    }
                }
                MEDIA_ACTION_SKIP_NEXT -> transport.skipToNext()
                MEDIA_ACTION_SKIP_PREVIOUS -> transport.skipToPrevious()
            }
        }
    }

    private fun refreshMedia() {
        val controller = activeControllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: activeControllers.firstOrNull()

        if (controller != null) {
            val metadata = controller.metadata
            val playbackState = controller.playbackState
            
            NotificationManager.updateMedia(
                MediaData(
                    title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
                    artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
                    albumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
                        ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART),
                    isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING,
                    packageName = controller.packageName
                )
            )
        } else {
            NotificationManager.updateMedia(MediaData())
        }
    }

    private fun updateNotifications() {
        try {
            val activeNotifications = activeNotifications
            if (activeNotifications == null) {
                NotificationManager.updateAll(emptyMap())
                return
            }
            
            val grouped = activeNotifications.groupBy { it.packageName }
            
            val notificationDataMap = grouped.mapValues { (packageName, notifications) ->
                val recentList = notifications.mapNotNull { sbn ->
                    val notification = sbn.notification ?: return@mapNotNull null
                    val extras = notification.extras ?: return@mapNotNull null
                    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                    
                    if (title == null && text == null) return@mapNotNull null
                    
                    NotificationData(
                        packageName = packageName,
                        count = 1,
                        summary = if (title != null && text != null) "$title: $text" else title ?: text,
                        sender = title,
                        content = text,
                        postTime = sbn.postTime
                    )
                }.sortedByDescending { it.postTime }

                AppNotificationData(
                    packageName = packageName,
                    totalCount = recentList.size,
                    recentNotifications = recentList
                )
            }.filterValues { it.totalCount > 0 }
            
            NotificationManager.updateAll(notificationDataMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val ACTION_MEDIA_PLAY_PAUSE = "com.example.windows11mobile.ACTION_MEDIA_PLAY_PAUSE"
        const val ACTION_MEDIA_SKIP_NEXT = "com.example.windows11mobile.ACTION_MEDIA_SKIP_NEXT"
        const val ACTION_MEDIA_SKIP_PREVIOUS = "com.example.windows11mobile.ACTION_MEDIA_SKIP_PREVIOUS"
        
        private const val MEDIA_ACTION_PLAY_PAUSE = 0
        private const val MEDIA_ACTION_SKIP_NEXT = 1
        private const val MEDIA_ACTION_SKIP_PREVIOUS = 2
    }
}
