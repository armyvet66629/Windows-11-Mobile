package com.example.windows11mobile.ui.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.data.IconPackManager
import com.example.windows11mobile.data.IconRepository
import com.example.windows11mobile.data.WindowsIconInfo
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.news.CustomizeFeedDialog
import java.io.File
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val wallpaperUri by viewModel.wallpaperUri.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val weatherAppPackage by viewModel.weatherAppPackage.collectAsStateWithLifecycle()
    val useFahrenheit by viewModel.useFahrenheit.collectAsStateWithLifecycle()
    val showTaskbar by viewModel.showTaskbar.collectAsStateWithLifecycle()
    val pageOrder by viewModel.pageOrder.collectAsStateWithLifecycle()
    val hiddenPages by viewModel.hiddenPages.collectAsStateWithLifecycle()
    val statusBarMode by viewModel.statusBarMode.collectAsStateWithLifecycle()
    val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()
    val rssFeeds by viewModel.rssFeeds.collectAsStateWithLifecycle()
    val hiddenNativeWidgets by viewModel.hiddenNativeWidgets.collectAsStateWithLifecycle()
    val swipeDownForNotifications by viewModel.swipeDownForNotifications.collectAsStateWithLifecycle()
    val showMoreTiles by viewModel.showMoreTiles.collectAsStateWithLifecycle()
    val tilePictureEnabled by viewModel.tilePictureEnabled.collectAsStateWithLifecycle()
    val tileBlurRadius by viewModel.tileBlurRadius.collectAsStateWithLifecycle()
    val homeScreenBlurEnabled by viewModel.homeScreenBlurEnabled.collectAsStateWithLifecycle()
    val accentColorInt by viewModel.accentColor.collectAsStateWithLifecycle()
    val pinnedApps by viewModel.pinnedApps.collectAsStateWithLifecycle()
    val useSystemWallpaper by viewModel.useSystemWallpaper.collectAsStateWithLifecycle()
    val accentColorOverlayEnabled by viewModel.accentColorOverlayEnabled.collectAsStateWithLifecycle()
    val solidTilesEnabled by viewModel.solidTilesEnabled.collectAsStateWithLifecycle()
    val squareTilesEnabled by viewModel.squareTilesEnabled.collectAsStateWithLifecycle()
    val selectedIconPack by viewModel.selectedIconPack.collectAsStateWithLifecycle()
    
    var showIconPackPicker by remember { mutableStateOf(false) }
    
    val categories = listOf("Personalization", "Launcher", "System", "Widgets", "About")
    val pagerState = rememberPagerState(initialPage = 0) { categories.size }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                // Take persistable permission immediately
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("SettingsScreen", "Failed to take permission for picked image", e)
            }
            // Use the image directly without cropping to avoid "Editing is not supported" errors
            viewModel.setWallpaperUri(uri.toString())
            viewModel.setUseSystemWallpaper(false)
        }
    }

    var showWeatherPicker by remember { mutableStateOf(false) }
    var showPageManager by remember { mutableStateOf(false) }
    var showRssManager by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
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

        // Dynamic Wallpaper Background - Now Clear
        if (wallpaperSource != null) {
            AsyncImage(
                model = wallpaperSource,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }

        // Overlay to darken background slightly for glass effect
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))

        Scaffold(
            topBar = {
                // Glassy Top Section - Now extends into status bar correctly
                FluentSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                    alpha = 0.4f,
                    effect = FluentEffect.ACRYLIC,
                    blurRadius = tileBlurRadius.toInt(),
                    tintColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 
                        Color.Black.copy(alpha = 0.25f) 
                    else 
                        Color(0xFFB0B0B0).copy(alpha = 0.15f),
                    luminosityAlpha = 0.2f,
                    borderAlpha = 0.1f
                ) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        TopAppBar(
                            title = { 
                                Text(
                                    "Settings", 
                                    style = MaterialTheme.typography.headlineMedium, 
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.padding(start = 8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 16.dp,
                            divider = {},
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                    height = 4.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        ) {
                            categories.forEachIndexed { index, title ->
                                val isSelected = pagerState.currentPage == index
                                Tab(
                                    selected = isSelected,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                    text = {
                                        Text(
                                            title.uppercase(),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            letterSpacing = if (isSelected) 1.5.sp else 0.5.sp
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 16.dp,
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (categories[pageIndex]) {
                        "Personalization" -> {
                            item { GlassySectionHeader("Appearance", tileBlurRadius.toInt()) }
                            item {
                                SettingsToggleItem(
                                    title = "Dark Mode",
                                    subtitle = "Switch between light and dark theme",
                                    icon = if (isDarkMode == true) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                    checked = isDarkMode ?: false,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setDarkMode(it) }
                                )
                            }
                            item {
                                SettingsClickableItem(
                                    title = "Wallpaper",
                                    subtitle = "Change and crop launcher background",
                                    icon = Icons.Rounded.Wallpaper,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onClick = {
                                        pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                ) {
                                    if (wallpaperUri != null) {
                                        AsyncImage(
                                            model = wallpaperUri,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            item {
                                SettingsClickableItem(
                                    title = "System Wallpaper Picker",
                                    subtitle = "Use the official Pixel wallpaper app",
                                    icon = Icons.Rounded.SettingsSuggest,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onClick = {
                                        // Set to use system wallpaper and clear custom uri
                                        viewModel.setUseSystemWallpaper(true)
                                        viewModel.setWallpaperUri(null)
                                        try {
                                            val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                                            context.startActivity(Intent.createChooser(intent, "Select Wallpaper"))
                                        } catch (e: Exception) {
                                            Log.e("SettingsScreen", "Failed to launch system wallpaper picker", e)
                                        }
                                    }
                                )
                            }
                            item { GlassySectionHeader("System Colors", tileBlurRadius.toInt()) }
                            item {
                                AccentColorSection(accentColorInt ?: 0, tileOpacity, tileBlurRadius.toInt()) { viewModel.setAccentColor(it) }
                            }
                            item {
                                StatusBarStyleSection(statusBarMode ?: "auto", tileOpacity, tileBlurRadius.toInt()) { viewModel.setStatusBarMode(it) }
                            }
                        }
                        "Launcher" -> {
                            item { GlassySectionHeader("Performance", tileBlurRadius.toInt()) }
                            item {
                                SettingsToggleItem(
                                    title = "Home Screen Blur",
                                    subtitle = "Enable glass/acrylic effect on tiles",
                                    icon = Icons.Rounded.BlurOn,
                                    checked = homeScreenBlurEnabled,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setHomeScreenBlurEnabled(it) }
                                )
                            }
                            item { GlassySectionHeader("Visuals", tileBlurRadius.toInt()) }
                            item {
                                BlurIntensitySection(tileBlurRadius, tileOpacity) { viewModel.setTileBlurRadius(it) }
                            }
                            item {
                                TileOpacitySection(tileOpacity, viewModel::setTileOpacity)
                            }
                            item {
                                SettingsToggleItem(
                                    title = "Accent Color Overlay",
                                    subtitle = "Tint tiles with Windows Phone accent color",
                                    icon = Icons.Rounded.Palette,
                                    checked = accentColorOverlayEnabled,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setAccentColorOverlayEnabled(it) }
                                )
                            }
                            item {
                                SettingsToggleItem(
                                    title = "Solid Tiles",
                                    subtitle = "Use 100% opaque solid tile backgrounds",
                                    icon = Icons.Rounded.Square,
                                    checked = solidTilesEnabled,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setSolidTilesEnabled(it) }
                                )
                            }
                            item {
                                val currentPackLabel = remember(selectedIconPack) {
                                    if (selectedIconPack == "default" || selectedIconPack.isBlank()) "System Default"
                                    else {
                                        try {
                                            context.packageManager.getApplicationLabel(
                                                context.packageManager.getApplicationInfo(selectedIconPack, 0)
                                            ).toString()
                                        } catch (_: Exception) { selectedIconPack }
                                    }
                                }
                                SettingsClickableItem(
                                    title = "Icon Pack",
                                    subtitle = currentPackLabel,
                                    icon = Icons.Rounded.Apps,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onClick = { showIconPackPicker = true }
                                )
                            }
                            item {
                                SettingsToggleItem(
                                    title = "Square Tiles",
                                    subtitle = "Use sharp square corners instead of rounded corners",
                                    icon = Icons.Rounded.CropSquare,
                                    checked = squareTilesEnabled,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setSquareTilesEnabled(it) }
                                )
                            }
                            item {
                                SettingsToggleItem(
                                    title = "Tile Picture",
                                    subtitle = "Show wallpaper through tiles with parallax",
                                    icon = Icons.Rounded.BurstMode,
                                    checked = tilePictureEnabled,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setTilePictureEnabled(it) }
                                )
                            }
                            item {
                                SettingsToggleItem(
                                    title = "Show more tiles",
                                    subtitle = "Increase home screen columns",
                                    icon = Icons.Rounded.GridView,
                                    checked = showMoreTiles,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setShowMoreTiles(it) }
                                )
                            }
                            item { GlassySectionHeader("Advanced", tileBlurRadius.toInt()) }
                            item {
                                SettingsClickableItem(
                                    title = "Page Manager",
                                    subtitle = "Manage home screen pages",
                                    icon = Icons.Rounded.Layers,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onClick = { showPageManager = true }
                                )
                            }
                            item {
                                SettingsClickableItem(
                                    title = "Restart Launcher",
                                    subtitle = "Apply deep changes",
                                    icon = Icons.Rounded.Refresh,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onClick = { viewModel.restartLauncher(context) }
                                )
                            }
                        }
                        "System" -> {
                            item { GlassySectionHeader("Defaults", tileBlurRadius.toInt()) }
                            item {
                                val selectedAppName = remember(weatherAppPackage, installedApps) {
                                    if (weatherAppPackage == "web") "Web Search"
                                    else installedApps.find { it.packageName == weatherAppPackage }?.name ?: "Not Set"
                                }
                                SettingsClickableItem(
                                    title = "Default Weather App",
                                    subtitle = "Currently: $selectedAppName",
                                    icon = Icons.Rounded.Cloud,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onClick = { showWeatherPicker = true }
                                )
                            }
                            item {
                                SettingsToggleItem(
                                    title = "Use Fahrenheit",
                                    subtitle = "Temperature units",
                                    icon = Icons.Rounded.Thermostat,
                                    checked = useFahrenheit,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setUseFahrenheit(it) }
                                )
                            }
                            item { GlassySectionHeader("Gestures & UI", tileBlurRadius.toInt()) }
                            item {
                                SettingsToggleItem(
                                    title = "Show Taskbar",
                                    subtitle = "Quick-access dock at bottom",
                                    icon = Icons.Rounded.WebAsset,
                                    checked = showTaskbar,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setShowTaskbar(it) }
                                )
                            }
                            item {
                                SettingsToggleItem(
                                    title = "Swipe Down Gestures",
                                    subtitle = "Pull down for notifications",
                                    icon = Icons.Rounded.Notifications,
                                    checked = swipeDownForNotifications,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onCheckedChange = { viewModel.setSwipeDownForNotifications(it) }
                                )
                            }
                            item {
                                SettingsClickableItem(
                                    title = "Set as Default Home",
                                    subtitle = "Make Windows 11 your primary launcher",
                                    icon = Icons.Rounded.Home,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                                        } catch (e: Exception) {
                                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                        }
                                    }
                                )
                            }
                        }
                        "Widgets" -> {
                            item { GlassySectionHeader("Feed Settings", tileBlurRadius.toInt()) }
                            item {
                                SettingsClickableItem(
                                    title = "Customize RSS Feeds",
                                    subtitle = "${rssFeeds.size} sources active",
                                    icon = Icons.Rounded.RssFeed,
                                    tileOpacity = tileOpacity,
                                    blurRadius = tileBlurRadius.toInt(),
                                    onClick = { showRssManager = true }
                                )
                            }
                        }
                        "About" -> {
                            item { GlassySectionHeader("Windows 11 Mobile", tileBlurRadius.toInt()) }
                            item {
                                FluentSurface(
                                    modifier = Modifier.fillMaxWidth(),
                                    alpha = tileOpacity,
                                    effect = FluentEffect.ACRYLIC,
                                    blurRadius = tileBlurRadius.toInt(),
                                    tintColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 
                                        Color.Black.copy(alpha = 0.3f * tileOpacity) 
                                    else 
                                        Color(0xFFB0B0B0).copy(alpha = 0.25f * tileOpacity),
                                    shape = RoundedCornerShape(20.dp),
                                    luminosityAlpha = 0.1f * tileOpacity
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        val versionName = remember(context) {
                                            try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.3.6" }
                                            catch (_: Exception) { "1.3.6" }
                                        }
                                        Text("Version $versionName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("A high-fidelity Windows 11 launcher built with Jetpack Compose.", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Developed by armyvet66629", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRssManager) {
        CustomizeFeedDialog(
            selectedCategories = emptySet(),
            rssFeeds = rssFeeds,
            onDismiss = { showRssManager = false },
            onSaveCategories = { },
            onAddRssFeed = { viewModel.addRssFeed(it) },
            onRemoveRssFeed = { viewModel.removeRssFeed(it) }
        )
    }

    if (showIconPackPicker) {
        IconPackPickerDialog(
            selectedPack = selectedIconPack,
            onDismiss = { showIconPackPicker = false },
            onPackSelected = { viewModel.setSelectedIconPack(it) }
        )
    }

    if (showWeatherPicker) {
        WeatherAppPicker(
            apps = installedApps,
            onDismiss = { showWeatherPicker = false },
            onAppSelected = { pkg ->
                viewModel.setWeatherAppPackage(pkg)
                showWeatherPicker = false
            }
        )
    }

    if (showPageManager) {
        PageManagerDialog(
            currentOrder = pageOrder,
            hiddenPages = hiddenPages,
            onDismiss = { showPageManager = false },
            onOrderChange = { viewModel.setPageOrder(it) },
            onHiddenPagesChange = { viewModel.setHiddenPages(it) }
        )
    }
}

@Composable
fun IconPackPickerDialog(
    selectedPack: String,
    onDismiss: () -> Unit,
    onPackSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var showBrowser by remember { mutableStateOf(false) }
    val installedPacks = remember(context) {
        IconPackManager.getInstalledIconPacks(context)
    }

    if (showBrowser) {
        WindowsIconsBrowserDialog(onDismiss = { showBrowser = false })
    }

    Dialog(onDismissRequest = onDismiss) {
        FluentSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            alpha = 0.95f,
            effect = FluentEffect.ACRYLIC,
            blurRadius = 120,
            tintColor = Color.Black.copy(alpha = 0.35f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Select Icon Pack",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(installedPacks) { pack ->
                        val isSelected = pack.packageName == selectedPack || (selectedPack == "default" && pack.packageName == "default")
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .clickable {
                                    onPackSelected(pack.packageName)
                                    onDismiss()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (pack.icon != null) {
                                    AsyncImage(
                                        model = pack.icon,
                                        contentDescription = pack.label,
                                        modifier = Modifier.size(36.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Apps,
                                        contentDescription = pack.label,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = pack.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (pack.packageName == "windowsicons") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showBrowser = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Browse 1,260 WindowsIcons", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun WindowsIconsBrowserDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val allIcons by produceState<List<WindowsIconInfo>>(initialValue = emptyList(), context) {
        value = IconRepository.getWindowsIcons(context)
    }

    val filteredIcons = remember(searchQuery, allIcons) {
        if (searchQuery.isBlank()) allIcons
        else allIcons.filter { it.name.lowercase().contains(searchQuery.lowercase()) || it.resourceName.lowercase().contains(searchQuery.lowercase()) }
    }

    Dialog(onDismissRequest = onDismiss) {
        FluentSurface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            alpha = 0.95f,
            effect = FluentEffect.ACRYLIC,
            blurRadius = 120,
            tintColor = Color.Black.copy(alpha = 0.35f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "WindowsIcons (${filteredIcons.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Templarian Windows Phone Vector Set",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 1,260 icons...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 72.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredIcons, key = { it.resourceName }) { iconInfo ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = iconInfo.resId),
                                contentDescription = iconInfo.name,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = iconInfo.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun GlassySectionHeader(text: String, blurRadius: Int = 80) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 6.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AccentColorSection(current: Int, opacity: Float, blurRadius: Int, onSelect: (Int) -> Unit) {
    val colors = listOf(
        0xFF0078D4, // Windows Blue
        0xFF4A4EDD, // Cobalt
        0xFF00B294, // Teal
        0xFF10893E, // Green
        0xFFD83B01, // Orange
        0xFFE81123, // Red
        0xFFB4009E, // Purple
        0xFF5D5A58, // Grey
        0xFFFFB900, // Gold
        0xFFE3008C, // Shock Pink
        0xFF00188F, // Navy
        0xFF00BCF2, // Blue
        0xFF212121, // Black
        0xFF68217A, // Indigo
        0xFF004E8C, // Dark Blue
        0xFF008272  // Slate
    ).map { it.toInt() }

    FluentSurface(
        modifier = Modifier.fillMaxWidth(),
        alpha = opacity,
        effect = FluentEffect.ACRYLIC,
        blurRadius = blurRadius,
        tintColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 
            Color.Black.copy(alpha = 0.12f) 
        else 
            Color(0xFFB0B0B0).copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Palette, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Accent Color", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grid for more colors
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val rowSize = 8
                val rows = colors.chunked(rowSize)
                rows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        row.forEach { colorInt ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorInt))
                                    .border(
                                        width = if (current == colorInt) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable { onSelect(colorInt) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBarStyleSection(current: String, opacity: Float, blurRadius: Int, onSelect: (String) -> Unit) {
    FluentSurface(
        modifier = Modifier.fillMaxWidth(), 
        alpha = opacity, 
        effect = FluentEffect.ACRYLIC, 
        blurRadius = blurRadius, 
        tintColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 
            Color.Black.copy(alpha = 0.12f) 
        else 
            Color(0xFFB0B0B0).copy(alpha = 0.08f), 
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.ViewStream, contentDescription = null, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(16.dp)); Text("Status Bar Icons", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("auto", "light", "dark").forEach { mode -> Button(onClick = { onSelect(mode) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (current == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), contentColor = if (current == mode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface), shape = RoundedCornerShape(8.dp)) { Text(mode.uppercase(), style = MaterialTheme.typography.labelSmall) } } }
        }
    }
}

@Composable
fun BlurIntensitySection(current: Float, opacity: Float, onChange: (Float) -> Unit) {
    FluentSurface(
        modifier = Modifier.fillMaxWidth(), 
        alpha = opacity, 
        effect = FluentEffect.ACRYLIC, 
        blurRadius = current.toInt(), 
        tintColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 
            Color.Black.copy(alpha = 0.12f) 
        else 
            Color(0xFFB0B0B0).copy(alpha = 0.08f), 
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.BlurOn, contentDescription = null, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(16.dp)); Text("Glass Blur", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.weight(1f)); Text("${current.toInt()}px", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) }
            Slider(value = current, onValueChange = onChange, valueRange = 0f..250f, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun TileOpacitySection(current: Float, onChange: (Float) -> Unit) {
    var sliderValue by remember(current) { mutableFloatStateOf(current) }
    
    FluentSurface(
        modifier = Modifier.fillMaxWidth(),
        alpha = (current * 0.8f).coerceAtLeast(0.15f), // Scale container alpha with setting but keep readable
        effect = FluentEffect.ACRYLIC,
        blurRadius = 80,
        tintColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 
            Color.Black.copy(alpha = 0.08f * current) 
        else 
            Color(0xFFB0B0B0).copy(alpha = 0.05f * current),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Opacity, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Tile Transparency", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("${(current * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onChange(sliderValue) },
                valueRange = 0.05f..1f,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SettingsToggleItem(title: String, subtitle: String, icon: ImageVector, checked: Boolean, tileOpacity: Float, blurRadius: Int = 100, onCheckedChange: (Boolean) -> Unit) {
    FluentSurface(
        modifier = Modifier.fillMaxWidth(),
        alpha = tileOpacity,
        effect = FluentEffect.ACRYLIC,
        blurRadius = blurRadius,
        tintColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 
            Color.Black.copy(alpha = 0.25f * tileOpacity) 
        else 
            Color(0xFFD0D0D0).copy(alpha = 0.15f * tileOpacity),
        shape = RoundedCornerShape(16.dp),
        luminosityAlpha = 0.1f * tileOpacity,
        borderAlpha = 0.12f
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun SettingsClickableItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, tileOpacity: Float, blurRadius: Int = 100, trailingContent: @Composable (() -> Unit)? = null) {
    FluentSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        alpha = tileOpacity,
        effect = FluentEffect.ACRYLIC,
        blurRadius = blurRadius,
        tintColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 
            Color.Black.copy(alpha = 0.25f * tileOpacity) 
        else 
            Color(0xFFD0D0D0).copy(alpha = 0.15f * tileOpacity),
        shape = RoundedCornerShape(16.dp),
        luminosityAlpha = 0.1f * tileOpacity,
        borderAlpha = 0.12f
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun WeatherAppPicker(apps: List<AppInfo>, onDismiss: () -> Unit, onAppSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Weather App") },
        text = {
            LazyColumn {
                item {
                    ListItem(
                        headlineContent = { Text("Web Search") },
                        supportingContent = { Text("Open weather in browser") },
                        modifier = Modifier.clickable { onAppSelected("web") }
                    )
                }
                items(apps) { app ->
                    ListItem(
                        headlineContent = { Text(app.name) },
                        supportingContent = { Text(app.packageName) },
                        modifier = Modifier.clickable { onAppSelected(app.packageName) }
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun PageManagerDialog(currentOrder: List<String>, hiddenPages: Set<String>, onDismiss: () -> Unit, onOrderChange: (List<String>) -> Unit, onHiddenPagesChange: (Set<String>) -> Unit) {
    val pageNames = mapOf(
        "route_buddy" to "Route Buddy",
        "notes" to "Sticky Notes",
        "board" to "Widgets Board",
        "desktop" to "Home Screen",
        "apps" to "App List",
        "people" to "People Hub"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Pages") },
        text = {
            Column {
                currentOrder.forEach { page ->
                    val displayName = pageNames[page] ?: page.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Checkbox(
                            checked = !hiddenPages.contains(page),
                            onCheckedChange = { isVisible ->
                                val newHidden = if (isVisible) hiddenPages - page else hiddenPages + page
                                onHiddenPagesChange(newHidden)
                            }
                        )
                        Text(displayName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } }
    )
}
