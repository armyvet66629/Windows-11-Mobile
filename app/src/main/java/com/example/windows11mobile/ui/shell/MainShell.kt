package com.example.windows11mobile.ui.shell

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil.compose.AsyncImage
import com.example.windows11mobile.data.RealAppRepository
import com.example.windows11mobile.data.RealNewsRepository
import com.example.windows11mobile.navigation.Dest
import com.example.windows11mobile.ui.apps.AppDrawerScreen
import com.example.windows11mobile.ui.apps.AppDrawerViewModel
import com.example.windows11mobile.ui.apps.AppDrawerViewModelFactory
import com.example.windows11mobile.ui.home.HomeScreen
import com.example.windows11mobile.ui.home.HomeViewModel
import com.example.windows11mobile.ui.news.NewsFeedScreen
import com.example.windows11mobile.ui.news.NewsFeedViewModel
import com.example.windows11mobile.ui.news.NewsFeedViewModelFactory
import com.example.windows11mobile.data.SettingsRepository
import com.example.windows11mobile.ui.settings.SettingsScreen
import com.example.windows11mobile.ui.settings.SettingsViewModel
import com.example.windows11mobile.ui.settings.SettingsViewModelFactory
import com.example.windows11mobile.ui.theme.rememberWallpaperColor
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.components.FluentEffect

@Composable
fun MainShell(
    backStack: NavBackStack<NavKey>,
    settingsRepository: SettingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appRepository = remember { RealAppRepository(context) }
    val newsRepository = remember { RealNewsRepository(null) } // No API key for now
    val rssRepository = remember { com.example.windows11mobile.data.RssRepository() }
    
    val application = context.applicationContext as android.app.Application
    val homeViewModel: HomeViewModel = viewModel(
        factory = com.example.windows11mobile.ui.home.HomeViewModelFactory(settingsRepository, application)
    )

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, homeViewModel) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                homeViewModel.startWidgetListening()
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                homeViewModel.stopWidgetListening()
            }
        }
        
        // Handle current state if already started
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
            homeViewModel.startWidgetListening()
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            homeViewModel.stopWidgetListening()
        }
    }
    val wallpaperUri by settingsRepository.wallpaperUri.collectAsStateWithLifecycle(initialValue = null)
    
    val pagerState = rememberPagerState(initialPage = 1) { 3 }
    val currentRoute = backStack.lastOrNull()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Ensure background shows through
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Wallpaper (Edge-to-Edge)
            if (wallpaperUri != null) {
                AsyncImage(
                    model = wallpaperUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Wallpaper-aware Mica Overlay
                val micaColor = rememberWallpaperColor(wallpaperUri, MaterialTheme.colorScheme.background)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(micaColor.copy(alpha = 0.5f))
                )
            } else {
                // Default Mica background if no wallpaper is set
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            // Content Area (Edge-to-Edge now that Dock is removed)
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (currentRoute in listOf(Dest.Desktop, Dest.AppDrawer, Dest.NewsFeed, null)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true
                    ) { page ->
                        when (page) {
                            0 -> {
                                val viewModel: AppDrawerViewModel = viewModel(
                                    factory = AppDrawerViewModelFactory(appRepository, settingsRepository, context)
                                )
                                AppDrawerScreen(
                                    viewModel = viewModel,
                                    onAppClick = { app ->
                                        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                        if (intent != null) {
                                            context.startActivity(intent)
                                        }
                                    },
                                    onSettingsClick = {
                                        backStack.add(Dest.Settings)
                                    },
                                    onPinToTaskbar = { _ ->
                                        // Taskbar removed
                                    },
                                    onAddToHomeScreen = { app ->
                                        homeViewModel.addTile(app.packageName, app.name)
                                    }
                                )
                            }
                            1 -> {
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    onAppClick = { packageName ->
                                        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                                        if (intent != null) {
                                            context.startActivity(intent)
                                        }
                                    }
                                )
                            }
                            2 -> {
                                val viewModel: NewsFeedViewModel = viewModel(
                                    factory = NewsFeedViewModelFactory(newsRepository, rssRepository, settingsRepository)
                                )
                                NewsFeedScreen(viewModel = viewModel)
                            }
                        }
                    }
                } else {
                    NavDisplay(
                        backStack = backStack,
                        onBack = onBack,
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) { key ->
                        when (key) {
                            is Dest.Settings -> NavEntry(key) {
                                val settingsViewModel: SettingsViewModel = viewModel(
                                    factory = SettingsViewModelFactory(settingsRepository, appRepository)
                                )
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onBack = onBack
                                )
                            }
                            is Dest.StartMenu -> NavEntry(key) { StartMenuScreen() }
                            else -> NavEntry(key) { Text("Unknown Route") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StartMenuScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Start Menu")
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun MainShellPreview() {
    val context = LocalContext.current
    val backStack = remember { NavBackStack<NavKey>(Dest.Desktop) }
    val settingsRepository = remember { com.example.windows11mobile.data.RealSettingsRepository(context) }
    com.example.windows11mobile.ui.theme.Windows11MobileTheme {
        MainShell(
            backStack = backStack,
            settingsRepository = settingsRepository,
            onBack = {}
        )
    }
}
