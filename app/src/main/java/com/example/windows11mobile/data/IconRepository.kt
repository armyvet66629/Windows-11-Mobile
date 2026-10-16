package com.example.windows11mobile.data

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.windows11mobile.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WindowsIconInfo(
    val name: String,
    val resourceName: String,
    @param:DrawableRes val resId: Int
)

object IconRepository {

    private var cachedIcons: List<WindowsIconInfo>? = null

    suspend fun getWindowsIcons(context: Context): List<WindowsIconInfo> {
        cachedIcons?.let { return it }
        return withContext(Dispatchers.IO) {
            val icons = mutableListOf<WindowsIconInfo>()
            val fields = R.drawable::class.java.fields

            for (field in fields) {
                val resName = field.name
                if (resName.startsWith("ic_win_")) {
                    try {
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        if (resId != 0) {
                            val displayName = resName.removePrefix("ic_win_")
                                .replace('_', ' ')
                                .split(' ')
                                .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                            
                            icons.add(WindowsIconInfo(displayName, resName, resId))
                        }
                    } catch (_: Exception) {}
                }
            }

            val sorted = icons.sortedBy { it.name }
            cachedIcons = sorted
            sorted
        }
    }

    @DrawableRes
    fun getIconByName(context: Context, name: String?): Int? {
        if (name.isNullOrBlank()) return null
        val cleanName = name.lowercase().trim()
        val resName = if (cleanName.startsWith("ic_win_")) cleanName else "ic_win_" + cleanName.replace(' ', '_').replace('-', '_').replace('.', '_')
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        return if (resId != 0) resId else null
    }

    @DrawableRes
    fun getIconForApp(context: Context, packageName: String?, label: String? = null): Int? {
        val pkgLower = packageName?.lowercase() ?: ""
        val labelLower = label?.lowercase() ?: ""

        // 1. Check label first (highest precision)
        if (labelLower.isNotBlank()) {
            val mappedRes = when {
                labelLower.contains("phone") || labelLower.contains("dialer") || labelLower.contains("call") -> "ic_win_phone"
                labelLower.contains("message") || labelLower.contains("messaging") || labelLower.contains("sms") || labelLower.contains("chat") -> "ic_win_message"
                labelLower.contains("calendar") || labelLower.contains("date") || labelLower.contains("schedule") -> "ic_win_calendar"
                labelLower.contains("setting") -> "ic_win_settings"
                labelLower.contains("camera") || labelLower.contains("picture") -> "ic_win_camera"
                labelLower.contains("photo") || labelLower.contains("gallery") -> "ic_win_photos"
                labelLower.contains("music") || labelLower.contains("spotify") || labelLower.contains("audio") -> "ic_win_music"
                labelLower.contains("browser") || labelLower.contains("edge") || labelLower.contains("chrome") || labelLower.contains("internet") -> "ic_win_browser"
                labelLower.contains("mail") || labelLower.contains("gmail") || labelLower.contains("outlook") || labelLower.contains("email") -> "ic_win_mail"
                labelLower.contains("map") || labelLower.contains("navigation") || labelLower.contains("gps") -> "ic_win_map"
                labelLower.contains("weather") || labelLower.contains("forecast") -> "ic_win_weather"
                labelLower.contains("calc") -> "ic_win_calculator"
                labelLower.contains("clock") || labelLower.contains("alarm") || labelLower.contains("time") -> "ic_win_clock"
                labelLower.contains("contact") || labelLower.contains("people") || labelLower.contains("address") -> "ic_win_people"
                labelLower.contains("store") || labelLower.contains("shop") || labelLower.contains("play") || labelLower.contains("market") -> "ic_win_marketplace"
                labelLower.contains("tube") || labelLower.contains("youtube") || labelLower.contains("video") -> "ic_win_youtube"
                labelLower.contains("file") || labelLower.contains("folder") || labelLower.contains("storage") -> "ic_win_folder"
                else -> null
            }
            if (mappedRes != null) {
                val resId = context.resources.getIdentifier(mappedRes, "drawable", context.packageName)
                if (resId != 0) return resId
            }
        }

        // 2. Check package name keywords
        val knownMap = mapOf(
            "dialer" to "ic_win_phone",
            "phone" to "ic_win_phone",
            "messaging" to "ic_win_message",
            "message" to "ic_win_message",
            "calendar" to "ic_win_calendar",
            "settings" to "ic_win_settings",
            "camera" to "ic_win_camera",
            "photos" to "ic_win_photos",
            "gallery" to "ic_win_pictures",
            "music" to "ic_win_music",
            "spotify" to "ic_win_music",
            "chrome" to "ic_win_browser",
            "browser" to "ic_win_browser",
            "edge" to "ic_win_browser",
            "emmx" to "ic_win_browser",
            "gmail" to "ic_win_mail",
            "mail" to "ic_win_mail",
            "outlook" to "ic_win_mail",
            "maps" to "ic_win_map",
            "weather" to "ic_win_weather",
            "calculator" to "ic_win_calculator",
            "clock" to "ic_win_clock",
            "contacts" to "ic_win_people",
            "people" to "ic_win_people",
            "store" to "ic_win_marketplace",
            "vending" to "ic_win_marketplace",
            "youtube" to "ic_win_youtube"
        )

        for ((key, resName) in knownMap) {
            if (pkgLower.contains(key)) {
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (resId != 0) return resId
            }
        }

        // 3. Try direct lookup by short name
        if (labelLower.isNotBlank()) {
            val cleanLabel = labelLower.replace(' ', '_').replace('-', '_')
            val resId = context.resources.getIdentifier("ic_win_$cleanLabel", "drawable", context.packageName)
            if (resId != 0) return resId
        }

        if (pkgLower.isNotBlank()) {
            val shortName = pkgLower.substringAfterLast('.').replace('.', '_')
            val resId = context.resources.getIdentifier("ic_win_$shortName", "drawable", context.packageName)
            if (resId != 0) return resId
        }

        return null
    }
}
