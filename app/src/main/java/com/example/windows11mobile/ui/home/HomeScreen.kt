package com.example.windows11mobile.ui.home

import android.app.Activity
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import coil.compose.AsyncImage
import com.example.windows11mobile.data.HomeTile
import com.example.windows11mobile.data.TileSize
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.components.AdvancedFluentMenu
import com.example.windows11mobile.ui.home.ClockTileContent
import com.example.windows11mobile.ui.home.WeatherTileContent
import com.example.windows11mobile.ui.home.PhotoLiveTile
import com.example.windows11mobile.ui.home.RichNotificationContent
import com.example.windows11mobile.ui.theme.FluentIcons
import com.example.windows11mobile.ui.components.FluentIcon

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tiles by viewModel.tiles.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val columns = when {
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT -> 4
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM -> 6
        else -> 8
    }

    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    
    // Drag and drop state
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    
    var selectedTileForMenu by remember { mutableStateOf<HomeTile?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    
    var backgroundMenuExpanded by remember { mutableStateOf(false) }

    // Shortcuts for the selected tile
    val shortcuts = remember(selectedTileForMenu) {
        selectedTileForMenu?.packageName?.let { viewModel.getShortcuts(it) } ?: emptyList()
    }

    val widgetConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (appWidgetId != -1) {
                val appWidgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)
                val label = appWidgetInfo?.loadLabel(context.packageManager) ?: "Widget"
                viewModel.addWidgetTile(appWidgetId, label)
            }
        }
    }

    val widgetPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (appWidgetId != -1) {
                val appWidgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)
                if (appWidgetInfo?.configure != null) {
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                        component = appWidgetInfo.configure
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    widgetConfigLauncher.launch(intent)
                } else {
                    val label = appWidgetInfo?.loadLabel(context.packageManager) ?: "Widget"
                    viewModel.addWidgetTile(appWidgetId, label)
                }
            }
        }
    }

    // Removed redundant startWidgetListening from HomeScreen as it's handled in MainShell

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { 
                    if (isEditMode) viewModel.setEditMode(false) 
                    backgroundMenuExpanded = false
                },
                onLongClick = {
                    backgroundMenuExpanded = true
                }
            )
    ) {
        // Optional: Add a subtle wallpaper background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Tiles Grid
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(
                items = tiles,
                key = { _, tile -> tile.id },
                span = { _, tile -> GridItemSpan(tile.spanX.coerceAtMost(columns)) }
            ) { index, tile ->
                val isDragging = draggingItemIndex == index
                
                // Animate scale and z-index for the dragging item
                val scale by animateFloatAsState(
                    targetValue = if (isDragging) 1.08f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "dragScale"
                )
                val zIndexValue by animateFloatAsState(
                    targetValue = if (isDragging) 10f else 1f,
                    label = "dragZIndex"
                )
                val elevation by animateFloatAsState(
                    targetValue = if (isDragging) 16f else 0f,
                    label = "dragElevation"
                )

                Box(
                    modifier = Modifier
                        .animateItem() // Smoothly animate grid shifts
                        .zIndex(zIndexValue)
                ) {
                    HomeTileItem(
                        tile = tile,
                        isEditMode = isEditMode,
                        tileOpacity = tileOpacity,
                        onResize = { viewModel.resizeTile(tile.id) },
                        widgetHost = viewModel.appWidgetHost,
                        modifier = Modifier
                            .scale(scale)
                            .shadow(elevation.dp, RoundedCornerShape(12.dp))
                            .graphicsLayer {
                                translationX = if (isDragging) dragOffset.x else 0f
                                translationY = if (isDragging) dragOffset.y else 0f
                            }
                            .pointerInput(tile.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { _ ->
                                        draggingItemIndex = index
                                        dragOffset = Offset.Zero
                                        viewModel.setEditMode(true)
                                    },
                                    onDragEnd = {
                                        draggingItemIndex = null
                                        dragOffset = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggingItemIndex = null
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount

                                        // Find which item we are currently over
                                        val layoutInfo = gridState.layoutInfo
                                        val draggingItem = layoutInfo.visibleItemsInfo.find { it.index == draggingItemIndex }
                                        
                                        if (draggingItem != null) {
                                            val currentCenter = Offset(
                                                draggingItem.offset.x + draggingItem.size.width / 2f + dragOffset.x,
                                                draggingItem.offset.y + draggingItem.size.height / 2f + dragOffset.y
                                            )

                                            val targetItem = layoutInfo.visibleItemsInfo.find { other ->
                                                other.index != draggingItemIndex &&
                                                currentCenter.x > other.offset.x && currentCenter.x < other.offset.x + other.size.width &&
                                                currentCenter.y > other.offset.y && currentCenter.y < other.offset.y + other.size.height
                                            }

                                            if (targetItem != null) {
                                                val fromIndex = draggingItemIndex!!
                                                val toIndex = targetItem.index
                                                
                                                viewModel.moveTile(fromIndex, toIndex)
                                                
                                                // Adjust dragOffset to prevent jumping when indices swap
                                                draggingItemIndex = toIndex
                                                dragOffset = Offset(
                                                    currentCenter.x - (targetItem.offset.x + targetItem.size.width / 2f),
                                                    currentCenter.y - (targetItem.offset.y + targetItem.size.height / 2f)
                                                )
                                            }
                                        }
                                    }
                                )
                            },
                        onClick = { 
                            if (draggingItemIndex == null && tile.packageName != null) {
                                onAppClick(tile.packageName)
                            }
                        },
                        onLongClick = { 
                            if (draggingItemIndex == null) {
                                // Clear background menu if tile is long-pressed
                                backgroundMenuExpanded = false
                                selectedTileForMenu = tile 
                                menuExpanded = true
                            }
                        }
                    )
                }
            }
        }

        // Background Context Menu
        AnimatedVisibility(
            visible = backgroundMenuExpanded,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().zIndex(100f),
                contentAlignment = Alignment.Center
            ) {
                // Backdrop
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .pointerInput(Unit) { detectTapGestures { backgroundMenuExpanded = false } }
                )
                
                FluentSurface(
                    modifier = Modifier.width(280.dp).padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    alpha = 0.8f,
                    effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
                    blurRadius = 60
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "DESKTOP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                        )
                        
                        com.example.windows11mobile.ui.components.ActionButton(
                            text = "Add Widget",
                            icon = Icons.Rounded.Widgets,
                            onClick = {
                                backgroundMenuExpanded = false
                                val appWidgetId = viewModel.allocateWidgetId()
                                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                }
                                widgetPickerLauncher.launch(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        com.example.windows11mobile.ui.components.ActionButton(
                            text = "Home Settings",
                            icon = Icons.Rounded.Settings,
                            onClick = {
                                backgroundMenuExpanded = false
                                // Could navigate to home settings
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        com.example.windows11mobile.ui.components.ActionButton(
                            text = "Edit Tiles",
                            icon = Icons.Rounded.Edit,
                            onClick = {
                                backgroundMenuExpanded = false
                                viewModel.setEditMode(true)
                            }
                        )
                    }
                }
            }
        }

        // Advanced Fluent Menu Overlay
        val currentTile = selectedTileForMenu
        AnimatedVisibility(
            visible = menuExpanded && currentTile != null,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f)
        ) {
            if (currentTile != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(100f)
                ) {
                    // Backdrop blur/dim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                            .pointerInput(Unit) {
                                detectTapGestures { 
                                    menuExpanded = false
                                    selectedTileForMenu = null
                                }
                            }
                    )

                    AdvancedFluentMenu(
                        tile = currentTile,
                        tileOpacity = tileOpacity.coerceAtLeast(0.4f),
                        shortcuts = shortcuts,
                        onShortcutClick = { shortcut ->
                            viewModel.launchShortcut(shortcut)
                        },
                        onDismiss = { 
                            menuExpanded = false
                            selectedTileForMenu = null 
                        },
                        onResize = { size ->
                            viewModel.resizeTile(currentTile.id, size)
                            menuExpanded = false
                            selectedTileForMenu = null
                        },
                        onRemove = {
                            viewModel.removeTile(currentTile.id)
                            menuExpanded = false
                            selectedTileForMenu = null
                        },
                        onAppSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${currentTile.packageName}")
                            }
                            context.startActivity(intent)
                            menuExpanded = false
                            selectedTileForMenu = null
                        },
                        onCheckForUpdates = {
                            val packageName = currentTile.packageName
                            if (packageName != null) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                                }
                            }
                            menuExpanded = false
                            selectedTileForMenu = null
                        },
                        onMoveTile = {
                            viewModel.setEditMode(true)
                            menuExpanded = false
                            selectedTileForMenu = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTileItem(
    tile: HomeTile,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    tileOpacity: Float = 0.25f,
    onResize: () -> Unit = {},
    widgetHost: android.appwidget.AppWidgetHost? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val ratio = tile.spanX.toFloat() / tile.spanY.toFloat()
    val context = LocalContext.current
    
    // Background color based on theme and tile type
    val backgroundColor = when (tile.specialType) {
        HomeTile.TYPE_CLOCK, HomeTile.TYPE_WEATHER -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface.copy(alpha = tileOpacity.coerceAtLeast(0.1f))
    }

    val icon = remember(tile.packageName, tile.label) {
        val pm = context.packageManager
        var d = tile.packageName?.let { pkg ->
            try { pm.getApplicationIcon(pkg) } catch (_: Exception) { null }
        }
        
        if (d == null) {
            // Try common fallbacks for system apps
            val fallbacks = when (tile.label.lowercase()) {
                "settings" -> listOf("com.android.settings", "com.google.android.settings")
                "calendar" -> listOf("com.google.android.calendar", "com.android.calendar")
                "people" -> listOf("com.android.contacts", "com.google.android.contacts")
                "messaging" -> listOf("com.google.android.apps.messaging", "com.android.messaging")
                "phone" -> listOf("com.google.android.dialer", "com.android.phone")
                else -> emptyList()
            }
            for (pkg in fallbacks) {
                try {
                    val iconFound = pm.getApplicationIcon(pkg)
                    if (iconFound != null) {
                        d = iconFound
                        break
                    }
                } catch (_: Exception) {}
            }
        }
        d
    }

    FluentSurface(
        modifier = modifier
            .aspectRatio(ratio)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(),
                onClick = {
                    if (tile.specialType == HomeTile.TYPE_CLOCK) {
                        try {
                            val intent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = context.packageManager.getLaunchIntentForPackage("com.android.deskclock")
                                ?: context.packageManager.getLaunchIntentForPackage("com.google.android.deskclock")
                            if (intent != null) context.startActivity(intent)
                        }
                    } else if (tile.specialType == HomeTile.TYPE_WEATHER) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))
                        context.startActivity(intent)
                    } else {
                        onClick()
                    }
                },
                onLongClick = onLongClick
            ),
        alpha = if (tile.specialType == HomeTile.TYPE_CLOCK || tile.specialType == HomeTile.TYPE_WEATHER) 0.9f else tileOpacity,
        effect = if (tile.specialType == HomeTile.TYPE_CLOCK || tile.specialType == HomeTile.TYPE_WEATHER) FluentEffect.ACRYLIC else FluentEffect.MICA,
        blurRadius = 40,
        color = backgroundColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                tile.isWidget && tile.widgetId != null -> {
                    WidgetHostItem(widgetId = tile.widgetId, sharedHost = widgetHost)
                }
                tile.specialType == HomeTile.TYPE_CLOCK -> {
                    ClockTileContent(tile)
                }
                tile.specialType == HomeTile.TYPE_WEATHER -> {
                    WeatherTileContent(tile)
                }
                tile.specialType == HomeTile.TYPE_PHOTOS -> {
                    PhotoLiveTile(tile)
                }
                (tile.packageName?.contains("gmail") == true || tile.packageName?.contains("messaging") == true) && 
                tile.notificationSender != null && (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) -> {
                    RichNotificationContent(tile)
                }
                else -> {
                    StandardTileContent(tile, icon)
                }
            }

            // Notification Counter Badge (Bottom Right)
            if (tile.notificationCount > 0 && !isEditMode && tile.specialType == null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = if (tile.notificationCount > 99) "99+" else tile.notificationCount.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Resize Handle (Bottom Right Corner)
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        .combinedClickable(
                            onClick = onResize
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FluentIcons.Open,
                        contentDescription = "Resize",
                        modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = 90f),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Optional: Jiggle animation or border for edit mode
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
fun StandardTileContent(
    tile: HomeTile,
    icon: android.graphics.drawable.Drawable?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Arrangement.Top else Arrangement.Center
    ) {
        Box(
            modifier = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
                Modifier.size(32.dp)
            } else {
                Modifier.weight(1f).fillMaxWidth()
            },
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                AsyncImage(
                    model = icon,
                    contentDescription = null,
                    modifier = Modifier.size(
                        when (tile.size) {
                            TileSize.SMALL -> 24.dp
                            TileSize.MEDIUM -> 48.dp
                            TileSize.WIDE -> 32.dp
                            TileSize.LARGE -> 48.dp
                        }
                    )
                )
            } else {
                val fallbackIcon = when (tile.label.lowercase()) {
                    "settings" -> FluentIcons.Settings
                    "calendar" -> FluentIcons.Calendar
                    "people" -> Icons.Rounded.Person
                    "messaging" -> FluentIcons.Message
                    "phone" -> Icons.Rounded.Phone
                    "camera" -> Icons.Rounded.CameraAlt
                    "mail", "gmail" -> FluentIcons.Mail
                    "maps" -> Icons.Rounded.Map
                    "photos" -> FluentIcons.Photos
                    else -> FluentIcons.Apps
                }
                
                // Use FluentIcon with gradient for system icons
                FluentIcon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    size = when (tile.size) {
                        TileSize.SMALL -> 24.dp
                        TileSize.MEDIUM -> 48.dp
                        TileSize.WIDE -> 32.dp
                        TileSize.LARGE -> 48.dp
                    },
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
            }
        }

        if (tile.size != TileSize.SMALL) {
            if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tile.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (tile.notificationSummary != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tile.notificationSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (tile.size == TileSize.LARGE) 6 else 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No new notifications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Text(
                    text = tile.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun WidgetHostItem(widgetId: Int, sharedHost: android.appwidget.AppWidgetHost? = null) {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = sharedHost ?: remember { android.appwidget.AppWidgetHost(context, 1024) }
    
    // Use a derived state to ensure we react to changes in widgetId or management state
    val appWidgetInfo = remember(widgetId) { 
        try {
            appWidgetManager.getAppWidgetInfo(widgetId)
        } catch (e: Exception) {
            null
        }
    }

    if (appWidgetInfo != null) {
        // Use key to force recreation if widgetId changes
        key(widgetId) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp), // Fluent spacing for widgets
                factory = { ctx ->
                    appWidgetHost.createView(ctx, widgetId, appWidgetInfo).apply {
                        // Crucial for rendering: ensure the view knows its ID and info
                        setAppWidget(widgetId, appWidgetInfo)
                    }
                },
                update = { view ->
                    // Standard update is handled by AppWidgetHost, but we ensure the view is still bound
                    // Avoid calling setAppWidget here as it can cause re-layout loops
                }
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Widget not found",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val settingsRepository = remember { com.example.windows11mobile.data.RealSettingsRepository(context) }
    val viewModel = remember { HomeViewModel(settingsRepository, application) }
    com.example.windows11mobile.ui.theme.Windows11MobileTheme {
        HomeScreen(viewModel = viewModel, onAppClick = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun HomeScreenTabletPreview() {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val settingsRepository = remember { com.example.windows11mobile.data.RealSettingsRepository(context) }
    val viewModel = remember { HomeViewModel(settingsRepository, application) }
    com.example.windows11mobile.ui.theme.Windows11MobileTheme {
        HomeScreen(viewModel = viewModel, onAppClick = {})
    }
}
