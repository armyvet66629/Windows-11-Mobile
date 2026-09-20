package com.example.windows11mobile.ui.shell

import android.annotation.SuppressLint
import android.app.Application
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.windows11mobile.data.RssRepository
import com.example.windows11mobile.ui.settings.SettingsViewModelFactory
import com.example.windows11mobile.ui.theme.rememberWallpaperColor
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.components.WindowsDock
import com.example.windows11mobile.ui.home.HomeViewModelFactory
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
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
    val newsRepository = remember { RealNewsRepository(null) }
    val rssRepository = remember { RssRepository() }
    
    val application = context.applicationContext as Application
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(settingsRepository, rssRepository, application)
    )

    var isShellVisible by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, homeViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    homeViewModel.startWidgetListening()
                    isShellVisible = true
                }
                Lifecycle.Event.ON_STOP -> {
                    homeViewModel.stopWidgetListening()
                    isShellVisible = false
                }
                else -> {}
            }
        }
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            homeViewModel.startWidgetListening()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            homeViewModel.stopWidgetListening()
        }
    }

    val wallpaperUri by settingsRepository.wallpaperUri.collectAsStateWithLifecycle(initialValue = null)
    val useSystemWallpaper by settingsRepository.useSystemWallpaper.collectAsStateWithLifecycle(initialValue = false)
    val showTaskbar by settingsRepository.showTaskbar.collectAsStateWithLifecycle(initialValue = false)
    val pinnedApps by settingsRepository.pinnedApps.collectAsStateWithLifecycle(initialValue = emptySet())
    val installedApps by homeViewModel.installedApps.collectAsStateWithLifecycle()
    val isDragging by homeViewModel.isDragging.collectAsStateWithLifecycle()
    val isEditMode by homeViewModel.isEditMode.collectAsStateWithLifecycle()
    val pageOrder by settingsRepository.pageOrder.collectAsStateWithLifecycle(initialValue = SettingsRepository.DEFAULT_PAGE_ORDER)
    val hiddenPages by settingsRepository.hiddenPages.collectAsStateWithLifecycle(initialValue = emptySet())
    val tilePictureEnabled by settingsRepository.tilePictureEnabled.collectAsStateWithLifecycle(initialValue = false)
    val tileBlurRadius by settingsRepository.tileBlurRadius.collectAsStateWithLifecycle(initialValue = 60f)
    val homeScreenBlurEnabled by settingsRepository.homeScreenBlurEnabled.collectAsStateWithLifecycle(initialValue = true)
    
    val visiblePages = remember(pageOrder, hiddenPages) {
        pageOrder.filter { it !in hiddenPages || it == "desktop" || it == "apps" }
    }

    val pagerState = rememberPagerState(
        initialPage = visiblePages.indexOf("desktop").coerceAtLeast(0)
    ) { visiblePages.size }
    
    LaunchedEffect(homeViewModel) {
        homeViewModel.homeButtonPressed.collect {
            // 1. Clear backstack to return to home from Settings etc
            while (backStack.size > 1) {
                backStack.removeAt(backStack.size - 1)
            }
            
            // 2. Scroll pager back to desktop
            val desktopIndex = visiblePages.indexOf("desktop")
            if (desktopIndex != -1 && pagerState.currentPage != desktopIndex) {
                pagerState.animateScrollToPage(
                    page = desktopIndex,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            }
        }
    }
    
    LaunchedEffect(pagerState.currentPage) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    val currentRoute = backStack.lastOrNull()

    val shellAlpha = 1f 
    val shellScale by animateFloatAsState(
        targetValue = if (isShellVisible) 1f else 1.05f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "shellScale"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = shellAlpha
                scaleX = shellScale
                scaleY = shellScale
            },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
            // Background Wallpaper Logic
            if (tilePictureEnabled) {
                // Strictly black background for Picture Mode to prevent ghosting/overlap
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            } else {
                // Resolve wallpaper source: Custom URI or System Drawable
                val wallpaperSource = remember(wallpaperUri, useSystemWallpaper) {
                    if (!useSystemWallpaper && wallpaperUri != null) {
                        wallpaperUri
                    } else {
                        try {
                            WallpaperManager.getInstance(context).drawable
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                if (wallpaperSource != null) {
                    AsyncImage(
                        model = wallpaperSource,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            if (homeScreenBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                 renderEffect = RenderEffect.createBlurEffect(
                                    tileBlurRadius / 4f, tileBlurRadius / 4f, Shader.TileMode.CLAMP
                                 ).asComposeRenderEffect()
                            }
                        },
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                } else {
                    // PREMIUM GRADIENT FALLBACK
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (currentRoute in listOf(Dest.Desktop, Dest.AppDrawer, Dest.NewsFeed, null)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
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
                                        if (intent != null) context.startActivity(intent)
                                    },
                                    onAddAppsClick = {
                                        scope.launch {
                                            val targetIndex = visiblePages.indexOf("apps")
                                            if (targetIndex != -1) pagerState.animateScrollToPage(targetIndex)
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
                                                if (targetIndex != -1) pagerState.animateScrollToPage(targetIndex)
                                            }
                                        } else {
                                            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                            if (intent != null) context.startActivity(intent)
                                        }
                                    },
                                    onSettingsClick = { backStack.add(Dest.Settings) },
                                    onPinToTaskbar = { app -> viewModel.pinApp(app.packageName) },
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
                                SettingsScreen(viewModel = settingsViewModel, onBack = onBack)
                            }
                            is Dest.StartMenu -> NavEntry(key) {
                                val drawerViewModel: AppDrawerViewModel = viewModel(
                                    factory = AppDrawerViewModelFactory(appRepository, settingsRepository, context)
                                )
                                val scope = rememberCoroutineScope()
                                StartMenuScreen(
                                    viewModel = drawerViewModel,
                                    onAppClick = { app ->
                                        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                        if (intent != null) context.startActivity(intent)
                                        onBack()
                                    },
                                    onAllAppsClick = {
                                        onBack()
                                        scope.launch {
                                            val targetIndex = visiblePages.indexOf("apps")
                                            if (targetIndex != -1) pagerState.animateScrollToPage(targetIndex)
                                        }
                                    },
                                    onSettingsClick = { backStack.add(Dest.Settings) },
                                    onPowerClick = { /* Handle Power */ },
                                    onBack = onBack
                                )
                            }
                            else -> NavEntry(key) { Text("Unknown Route") }
                        }
                    }
                }
                
                // Centered Taskbar - ALWAYS visible if enabled, on top of everything
                if (showTaskbar && currentRoute != Dest.StartMenu) {
                    WindowsDock(
                        pinnedApps = pinnedApps,
                        installedApps = installedApps,
                        onAppClick = { packageName ->
                            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                            if (intent != null) context.startActivity(intent)
                        },
                        onStartClick = { backStack.add(Dest.StartMenu) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .zIndex(1000f) // Ensure it's above NavDisplay
                    )
                }
            }
        }
    }
}

