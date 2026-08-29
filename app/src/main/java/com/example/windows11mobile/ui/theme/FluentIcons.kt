package com.example.windows11mobile.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object FluentIcons {
    val Home = Icons.Rounded.Home
    val Settings = Icons.Rounded.Settings
    val Search = Icons.Rounded.Search
    val News = Icons.Rounded.Article
    val Apps = Icons.Rounded.Apps
    val Info = Icons.Rounded.Info
    val Message = Icons.AutoMirrored.Rounded.Message
    val Mail = Icons.Rounded.Email
    val Calendar = Icons.Rounded.CalendarToday
    val Photos = Icons.Rounded.PhotoLibrary
    val Weather = Icons.Rounded.WbSunny
    val Call = Icons.Rounded.Call
    val Edit = Icons.Rounded.Edit
    val Delete = Icons.Rounded.Delete
    val Uninstall = Icons.Rounded.DeleteForever
    val Share = Icons.Rounded.Share
    val Star = Icons.Rounded.Star
    val Refresh = Icons.Rounded.Refresh
    val Pin = Icons.Rounded.PushPin
    val Unpin = Icons.Rounded.PushPin // Should be unpin icon
    val Widgets = Icons.Rounded.Widgets
    val Open = Icons.Rounded.OpenInFull
    val Close = Icons.Rounded.Close

    // Fluent 2 style Start Icon (simplified)
    val Start: ImageVector
        get() = ImageVector.Builder(
            name = "FluentStart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(3f, 3f)
                horizontalLineToRelative(8.5f)
                verticalLineToRelative(8.5f)
                horizontalLineToRelative(-8.5f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(12.5f, 3f)
                horizontalLineToRelative(8.5f)
                verticalLineToRelative(8.5f)
                horizontalLineToRelative(-8.5f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(3f, 12.5f)
                horizontalLineToRelative(8.5f)
                verticalLineToRelative(8.5f)
                horizontalLineToRelative(-8.5f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(12.5f, 12.5f)
                horizontalLineToRelative(8.5f)
                verticalLineToRelative(8.5f)
                horizontalLineToRelative(-8.5f)
                close()
            }
        }.build()
}
