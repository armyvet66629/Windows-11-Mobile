package com.example.windows11mobile.ui.theme

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Remembers a color sampled from the wallpaper URI using the Palette API.
 * This is used to implement the Mica effect which is wallpaper-aware.
 */
@Composable
fun rememberWallpaperColor(wallpaperUri: String?, defaultColor: Color): Color {
    val context = LocalContext.current
    var sampledColor by remember { mutableStateOf(defaultColor) }

    LaunchedEffect(wallpaperUri) {
        if (wallpaperUri == null) {
            sampledColor = defaultColor
            return@LaunchedEffect
        }

        val color = withContext(Dispatchers.IO) {
            try {
                val uri = wallpaperUri.toUri()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val palette = Palette.from(bitmap).generate()
                    // We want a muted but representative color for Mica
                    val dominantColor = palette.getMutedColor(defaultColor.toArgb())
                    Color(dominantColor)
                } else {
                    defaultColor
                }
            } catch (e: Exception) {
                // Log error or handle gracefully
                defaultColor
            }
        }
        sampledColor = color
    }

    return sampledColor
}
