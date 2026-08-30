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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.example.windows11mobile.ui.widgets.WidgetsBoardScreen
import com.example.windows11mobile.ui.people.PeopleHubScreen
import com.example.windows11mobile.ui.people.PeopleViewModel
import com.example.windows11mobile.ui.people.PeopleViewModelFactory
import com.example.windows11mobile.ui.notes.NotesScreen
import com.example.windows11mobile.ui.notes.NotesViewModel
import com.example.windows11mobile.data.SettingsRepository
import com.example.windows11mobile.ui.settings.SettingsScreen
import com.example.windows11mobile.ui.settings.SettingsViewModel
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.windows11mobile.ui.settings.SettingsViewModelFactory
import com.example.windows11mobile.ui.theme.rememberWallpaperColor
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.components.WindowsDock
import kotlinx.coroutines.launch

@Composable
fun MainShell(
    backStack: NavBackStack<NavKey>,
    settingsRepository: SettingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
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
    val showTaskbar by settingsRepository.showTaskbar.collectAsStateWithLifecycle(initialValue = false)
    val pinnedApps by settingsRepository.pinnedApps.collectAsStateWithLifecycle(initialValue = emptySet())
    val installedApps by homeViewModel.installedApps.collectAsStateWithLifecycle()
    val isDragging by homeViewModel.isDragging.collectAsStateWithLifecycle()
    val isEditMode by homeViewModel.isEditMode.collectAsStateWithLifecycle()
    val pageOrder by settingsRepository.pageOrder.collectAsStateWithLifecycle(initialValue = SettingsRepository.DEFAULT_PAGE_ORDER)
    val hiddenPages by settingsRepository.hiddenPages.collectAsStateWithLifecycle(initialValue = emptySet())
    
    val visiblePages = remember(pageOrder, hiddenPages) {
        pageOrder.filter { it !in hiddenPages || it == "desktop" || it == "apps" }
    }

    val pagerState = rememberPagerState(
        initialPage = visiblePages.indexOf("desktop").coerceAtLeast(0)
    ) { visiblePages.size }
    
    // Handle home button press to return to desktop
    LaunchedEffect(homeViewModel) {
        homeViewModel.homeButtonPressed.collect {
            val desktopIndex = visiblePages.indexOf("desktop")
            if (desktopIndex != -1 && pagerState.currentPage != desktopIndex) {
                pagerState.animateScrollToPage(desktopIndex)
            }
        }
    }
    
    // Haptic feedback for page swiping
    LaunchedEffect(pagerState.currentPage) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    val currentRoute = backStack.lastOrNull()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Ensure background shows through
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0.dp) // Disable automatic insets to allow content behind status bar
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
            // Background Wallpaper (Edge-to-Edge)
            if (wallpaperUri != null) {
                AsyncImage(
                    model = wallpaperUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Default Mica background if no wallpaper is set
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            // Content Area (Edge-to-Edge)
            // Apply only bottom padding for the navigation bar/dock if needed, 
            // but for a launcher, we usually want full edge-to-edge.
            Box(modifier = Modifier.fillMaxSize()) {
                if (currentRoute in listOf(Dest.Desktop, Dest.AppDrawer, Dest.NewsFeed, null)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            reverseLayout = false,
                            userScrollEnabled = !isDragging && !isEditMode
                        ) { pageIndex ->
                            val pageId = visiblePages.getOrNull(pageIndex) ?: ""
                            when (pageId) {
                                "board" -> {
                                    val viewModel: NewsFeedViewModel = viewModel(
                                        factory = NewsFeedViewModelFactory(newsRepository, rssRepository, settingsRepository, context)
                                    )
                                    WidgetsBoardScreen(
                                        newsViewModel = viewModel,
                                        appWidgetHost = homeViewModel.appWidgetHost,
                                        showTaskbar = showTaskbar
                                    )
                                }
                                "desktop" -> {
                                    val scope = rememberCoroutineScope()
                                    HomeScreen(
                                        viewModel = homeViewModel,
                                        onAppClick = { packageName ->
                                            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                                            if (intent != null) {
                                                context.startActivity(intent)
                                            }
                                        },
                                        onAddAppsClick = {
                                            scope.launch {
                                                val targetIndex = visiblePages.indexOf("apps")
                                                if (targetIndex != -1) {
                                                    pagerState.animateScrollToPage(targetIndex)
                                                }
                                            }
                                        }
                                    )
                                }
                                "apps" -> {
                                    val viewModel: AppDrawerViewModel = viewModel(
                                        factory = AppDrawerViewModelFactory(appRepository, settingsRepository, context)
                                    )
                                    val scope = rememberCoroutineScope()
                                    val currentOpenFolderId by homeViewModel.openFolderId.collectAsStateWithLifecycle()
                                    
                                    AppDrawerScreen(
                                        viewModel = viewModel,
                                        onAppClick = { app ->
                                            if (currentOpenFolderId != null) {
                                                homeViewModel.addTile(app.packageName, app.name)
                                                homeViewModel.openFolder(null)
                                                scope.launch {
                                                    val targetIndex = visiblePages.indexOf("desktop")
                                                    if (targetIndex != -1) {
                                                        pagerState.animateScrollToPage(targetIndex)
                                                    }
                                                }
                                            } else {
                                                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                if (intent != null) {
                                                    context.startActivity(intent)
                                                }
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
                                "people" -> {
                                    val viewModel: PeopleViewModel = viewModel(
                                        factory = PeopleViewModelFactory(application)
                                    )
                                    PeopleHubScreen(viewModel = viewModel)
                                }
                                "notes" -> {
                                    val viewModel: NotesViewModel = viewModel(
                                        factory = object : ViewModelProvider.Factory {
                                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                return NotesViewModel(settingsRepository) as T
                                            }
                                        }
                                    )
                                    NotesScreen(viewModel = viewModel)
                                }
                            }
                        }

                        // Taskbar Overlay
                        if (showTaskbar) {
                            WindowsDock(
                                pinnedApps = pinnedApps,
                                installedApps = installedApps,
                                onAppClick = { packageName ->
                                    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                                    if (intent != null) {
                                        context.startActivity(intent)
                                    }
                                },
                                onStartClick = {
                                    // Could toggle start menu or jump to app drawer
                                    // For now, let's jump to App Drawer (page 2)
                                    // pagerState.animateScrollToPage(2)
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                            )
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
    val appWidgetHost = remember { android.appwidget.AppWidgetHost(context, 1024) }
    com.example.windows11mobile.ui.theme.Windows11MobileTheme {
        MainShell(
            backStack = backStack,
            settingsRepository = settingsRepository,
            onBack = {}
        )
    }
}
