package com.example.windows11mobile.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Dest : NavKey {
    @Serializable
    data object Desktop : Dest

    @Serializable
    data object StartMenu : Dest

    @Serializable
    data object Settings : Dest

    @Serializable
    data object AppDrawer : Dest

    @Serializable
    data object NewsFeed : Dest
}
