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
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
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
import com.example.windows11mobile.ui.home.ClockWeatherTileContent
import com.example.windows11mobile.ui.home.PhotoLiveTile
import com.example.windows11mobile.ui.home.PhoneLiveTile
import com.example.windows11mobile.ui.home.MessagesLiveTile
import com.example.windows11mobile.ui.home.GmailLiveTile
import com.example.windows11mobile.ui.home.YouTubeLiveTile
import com.example.windows11mobile.ui.theme.FluentIcons
import com.example.windows11mobile.ui.components.FluentIcon

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAppClick: (String) -> Unit,
    onAddAppsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tiles by viewModel.tiles.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()
    val explodedTileId by viewModel.explodedTileId.collectAsStateWithLifecycle()
    val weatherData by viewModel.weather.collectAsStateWithLifecycle()
    val allNotifications by viewModel.recentNotifications.collectAsStateWithLifecycle()
    val currentMedia by viewModel.currentMedia.collectAsStateWithLifecycle()
    val weatherAppPackage by viewModel.weatherAppPackage.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val availableWidgets by viewModel.availableWidgets.collectAsStateWithLifecycle()
    val topNews by viewModel.topNews.collectAsStateWithLifecycle()
    val openFolderId by viewModel.openFolderId.collectAsStateWithLifecycle()
    val isEditModeState = rememberUpdatedState(isEditMode)

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val columns = when {
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT -> 4
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM -> 6
        else -> 8
    }

    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    
    var draggingTileId by remember { mutableStateOf<String?>(null) }
    var draggingTileSize by remember { mutableStateOf(IntSize.Zero) }
    var hoveredTileId by remember { mutableStateOf<String?>(null) }
    var dragStartPointerOffset by remember { mutableStateOf(Offset.Zero) }
    var pointerPosition by remember { mutableStateOf(Offset.Zero) }
    
    var selectedTileForMenu by remember { mutableStateOf<HomeTile?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var backgroundMenuExpanded by remember { mutableStateOf(false) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<HomeTile?>(null) }
    var folderSourceCenter by remember { mutableStateOf(Offset.Zero) }
    var gridPosition by remember { mutableStateOf(Offset.Zero) }

    var pendingWidgetInfo by remember { mutableStateOf<android.appwidget.AppWidgetProviderInfo?>(null) }
    var pendingWidgetId by remember { mutableIntStateOf(-1) }

    // Sync dragging state to ViewModel to disable pager scrolling
    LaunchedEffect(draggingTileId) {
        viewModel.setIsDragging(draggingTileId != null)
    }

    // Central Drag Engine: Handles reordering and folder-hovering for ALL drags
    LaunchedEffect(draggingTileId, pointerPosition) {
        val draggingId = draggingTileId ?: return@LaunchedEffect
        val layoutInfo = gridState.layoutInfo
        val relativePointer = pointerPosition - gridPosition
        
        // Find the "current center" of the ghost tile
        val currentCenter = relativePointer + Offset(
            draggingTileSize.width / 2f - dragStartPointerOffset.x,
            draggingTileSize.height / 2f - dragStartPointerOffset.y
        )
        
        // Find items whose center is within radius. Use radius based detection for better feel.
        val targetItem = layoutInfo.visibleItemsInfo.filter { it.key != draggingId }.find { other ->
            val otherCenterX = other.offset.x + other.size.width / 2f
            val otherCenterY = other.offset.y + other.size.height / 2f
            val distSq = (currentCenter.x - otherCenterX) * (currentCenter.x - otherCenterX) +
                         (currentCenter.y - otherCenterY) * (currentCenter.y - otherCenterY)
            
            // SWAP LOGIC: Tighter radius (35%) to prevent "fighting"
            val swapRadius = other.size.width * 0.35f
            distSq < swapRadius * swapRadius
        }
        
        val candidateItem = if (targetItem == null) {
            // If no swap, check for folder grouping with a larger radius (50%)
            layoutInfo.visibleItemsInfo.filter { it.key != draggingId }.find { other ->
                val otherCenterX = other.offset.x + other.size.width / 2f
                val otherCenterY = other.offset.y + other.size.height / 2f
                val distSq = (currentCenter.x - otherCenterX) * (currentCenter.x - otherCenterX) +
                             (currentCenter.y - otherCenterY) * (currentCenter.y - otherCenterY)
                val folderRadius = other.size.width * 0.5f
                distSq < folderRadius * folderRadius
            }
        } else null

        val targetTile = (targetItem ?: candidateItem)?.let { target -> tiles.find { it.id == target.key } }
        val isFolderCandidate = targetTile != null && !targetTile.isWidget && !targetTile.isSpacer
        
        if (candidateItem != null && isFolderCandidate) {
            hoveredTileId = targetTile.id
        } else {
            hoveredTileId = null
            
            if (targetItem != null) {
                val fromIndex = tiles.indexOfFirst { it.id == draggingId }
                val toIndex = targetItem.index
                if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                    viewModel.swapTiles(fromIndex, toIndex)
                }
            }
        }
    }

    // Auto-scroll logic to follow dragging tile
    LaunchedEffect(draggingTileId, pointerPosition) {
        if (draggingTileId != null) {
            val threshold = screenHeightPx * 0.15f // 15% from top/bottom
            
            while (draggingTileId != null) {
                val distFromTop = pointerPosition.y
                val distFromBottom = screenHeightPx - pointerPosition.y
                
                if (distFromTop < threshold) {
                    val scrollAmount = (threshold - distFromTop) / 5f
                    val oldIndex = gridState.firstVisibleItemIndex
                    gridState.dispatchRawDelta(-scrollAmount)
                    if (gridState.firstVisibleItemIndex != oldIndex) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                } else if (distFromBottom < threshold) {
                    val scrollAmount = (threshold - distFromBottom) / 5f
                    val oldIndex = gridState.firstVisibleItemIndex
                    gridState.dispatchRawDelta(scrollAmount)
                    if (gridState.firstVisibleItemIndex != oldIndex) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                delay(16) // ~60fps scroll check
            }
        }
    }

    // Haptic feedback for scrolling the home screen (only on new row)
    LaunchedEffect(gridState.firstVisibleItemIndex) {
        if (gridState.isScrollInProgress) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val shortcuts = remember(selectedTileForMenu) {
        selectedTileForMenu?.packageName?.let { viewModel.getShortcuts(it) } ?: emptyList()
    }

    val widgetConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: pendingWidgetId
            if (appWidgetId != -1) {
                val appWidgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)
                if (appWidgetInfo != null) {
                    viewModel.addWidgetTile(appWidgetId, appWidgetInfo.loadLabel(context.packageManager))
                }
            }
        } else if (pendingWidgetId != -1) {
            viewModel.appWidgetHost.deleteAppWidgetId(pendingWidgetId)
        }
        pendingWidgetId = -1
        pendingWidgetInfo = null
    }

    val bindWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: pendingWidgetId
            val info = pendingWidgetInfo
            if (appWidgetId != -1 && info != null) {
                if (info.configure != null) {
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                        component = info.configure
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    widgetConfigLauncher.launch(intent)
                } else {
                    viewModel.addWidgetTile(appWidgetId, info.loadLabel(context.packageManager))
                    pendingWidgetId = -1
                    pendingWidgetInfo = null
                }
            }
        } else if (pendingWidgetId != -1) {
            viewModel.appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = -1
            pendingWidgetInfo = null
        }
    }

    if (showWidgetPicker) {
        com.example.windows11mobile.ui.widgets.WidgetPickerDialog(
            availableWidgets = availableWidgets,
            onDismiss = { showWidgetPicker = false },
            onWidgetSelected = { info ->
                showWidgetPicker = false
                val appWidgetId = viewModel.allocateWidgetId()
                val success = AppWidgetManager.getInstance(context).bindAppWidgetIdIfAllowed(
                    appWidgetId,
                    info.provider
                )
                
                if (success) {
                    if (info.configure != null) {
                        pendingWidgetId = appWidgetId
                        pendingWidgetInfo = info
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                            component = info.configure
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        widgetConfigLauncher.launch(intent)
                    } else {
                        viewModel.addWidgetTile(appWidgetId, info.loadLabel(context.packageManager))
                    }
                } else {
                    pendingWidgetId = appWidgetId
                    pendingWidgetInfo = info
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                    }
                    bindWidgetLauncher.launch(intent)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        pointerPosition = event.changes.first().position
                        
                        if (draggingTileId != null) {
                            if (event.changes.all { !it.pressed }) {
                                // CENTRAL DROP HANDLER
                                val targetId = hoveredTileId
                                val draggingId = draggingTileId
                                if (draggingId != null) {
                                    val fromIndex = tiles.indexOfFirst { it.id == draggingId }
                                    if (targetId != null) {
                                        val toIndex = tiles.indexOfFirst { it.id == targetId }
                                        if (fromIndex != -1 && toIndex != -1) {
                                            viewModel.moveTile(fromIndex, toIndex)
                                        }
                                    } else if (fromIndex != -1) {
                                        viewModel.moveTile(fromIndex, fromIndex)
                                    }
                                }
                                draggingTileId = null
                                hoveredTileId = null
                                viewModel.setIsDragging(false)
                            }
                        }
                    }
                }
            }
            .pointerInput(isEditMode) {
                coroutineScope {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Main)
                        var isConsumedElsewhere = false
                        
                        val holdJob = launch {
                            // Increased delay to 1300ms for desktop menu to give apps priority
                            delay(1300)
                            if (!isConsumedElsewhere && draggingTileId == null && explodedTileId == null && openFolderId == null) {
                                backgroundMenuExpanded = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        
                        try {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                if (event.changes.any { it.isConsumed }) {
                                    isConsumedElsewhere = true
                                    holdJob.cancel()
                                }
                                if (event.changes.all { !it.pressed }) {
                                    if (!isConsumedElsewhere && !backgroundMenuExpanded) {
                                        if (isEditMode) viewModel.setEditMode(false)
                                    }
                                    break
                                }
                            }
                        } finally {
                            holdJob.cancel()
                        }
                    }
                }
            }
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { gridPosition = it.positionInRoot() },
            contentPadding = PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, 
                bottom = 120.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(
                items = tiles,
                key = { _, tile -> tile.id },
                span = { _, tile -> GridItemSpan(tile.spanX.coerceAtMost(columns)) }
            ) { _, tile ->
                val isDragging = draggingTileId == tile.id
                
                val wobbleTransition = rememberInfiniteTransition(label = "wobble")
                val wobbleRotation by wobbleTransition.animateFloat(
                    initialValue = -1f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(150, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "wobbleRotate"
                )
                
                val zIndexValue by animateFloatAsState(
                    targetValue = if (isDragging) 100f else 1f,
                    label = "dragZIndex"
                )

                var itemPosition by remember { mutableStateOf(Offset.Zero) }
                var itemSize by remember { mutableStateOf(IntSize.Zero) }

                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            itemPosition = coords.positionInRoot()
                            itemSize = coords.size
                        }
                        .then(if (!isDragging) Modifier.animateItem() else Modifier)
                        .zIndex(zIndexValue)
                        .graphicsLayer {
                            alpha = if (isDragging) 0f else 1f
                            rotationZ = if (isEditMode && !isDragging) wobbleRotation else 0f
                        }
                        .pointerInput(tile.id) {
                            coroutineScope {
                                awaitEachGesture {
                                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                    // If we are in edit mode, consume immediately to lock out parent pager
                                    if (isEditModeState.value) {
                                        down.consume()
                                    }
                                    
                                    var dragStarted = false
                                    var hasMovedSignificant = false
                                    val isHoldTriggered = BooleanArray(1) { false }
                                    
                                    val holdJob = launch {
                                        if (!isEditModeState.value) {
                                            // Consistent delay
                                            delay(850)
                                            if (draggingTileId == null) {
                                                viewModel.explodeTile(tile.id)
                                                isHoldTriggered[0] = true
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        } else {
                                            isHoldTriggered[0] = true
                                        }
                                    }
                                
                                    try {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                                            
                                            val totalDrag = pointer.position - down.position
                                            // Lower threshold for starting drag once hold is active
                                            val isMoving = totalDrag.getDistance() > viewConfiguration.touchSlop
                                            if (isMoving) hasMovedSignificant = true
                                            
                                            if (pointer.pressed) {
                                                if (isHoldTriggered[0] || isEditModeState.value) {
                                                    pointer.consume()
                                                    
                                                    if (!dragStarted && (isEditModeState.value || isMoving)) {
                                                        dragStarted = true
                                                        holdJob.cancel()
                                                        
                                                        if (explodedTileId == tile.id) {
                                                            viewModel.explodeTile(null)
                                                        }
                                                        
                                                        if (!isEditModeState.value) {
                                                            viewModel.setEditMode(true)
                                                            viewModel.setIsDragging(true)
                                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        }
                                                        
                                                        draggingTileId = tile.id
                                                        draggingTileSize = itemSize
                                                        dragStartPointerOffset = down.position
                                                    }
                                                } else if (hasMovedSignificant && !dragStarted) {
                                                    holdJob.cancel()
                                                }
                                            } else {
                                                // Up
                                                holdJob.cancel()
                                                if (!dragStarted && !isHoldTriggered[0] && !hasMovedSignificant) {
                                                    // This was a click
                                                    if (tile.isFolder) {
                                                        folderSourceCenter = itemPosition + Offset(itemSize.width / 2f, itemSize.height / 2f)
                                                        viewModel.openFolder(tile.id)
                                                    } else if (tile.packageName != null) {
                                                        onAppClick(tile.packageName)
                                                    } else if (tile.specialType == HomeTile.TYPE_CLOCK || tile.specialType == HomeTile.TYPE_CLOCK_WEATHER) {
                                                        try { context.startActivity(Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)) } catch (e: Exception) {}
                                                    }
                                                }
                                                break
                                            }
                                        }
                                    } finally {
                                        holdJob.cancel()
                                    }
                                }
                            }
                        }
                ) {
                    val isHoverTarget = hoveredTileId == tile.id
                    val pulseTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by pulseTransition.animateFloat(
                        initialValue = 1.05f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )
                    
                    val hoverScale by animateFloatAsState(
                        targetValue = if (isHoverTarget) pulseScale else 1f, 
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "hoverScale"
                    )
                    
                    Box {
                        HomeTileItem(
                            tile = tile,
                            isEditMode = isEditMode,
                            isHovered = isHoverTarget,
                            tileOpacity = tileOpacity,
                            weatherData = weatherData,
                            recentNotifications = tile.packageName?.let { allNotifications[it]?.recentNotifications } ?: emptyList(),
                            currentMedia = currentMedia,
                            calendarEvents = calendarEvents,
                            contacts = contacts,
                            weatherAppPackage = weatherAppPackage,
                            pointerPosition = pointerPosition,
                            onResize = { viewModel.resizeTile(tile.id) },
                            onPlayPause = { viewModel.mediaPlayPause() },
                            onSkipNext = { viewModel.mediaSkipNext() },
                            onSkipPrevious = { viewModel.mediaSkipPrevious() },
                            topNews = topNews,
                            widgetHost = viewModel.appWidgetHost,
                            modifier = Modifier.scale(hoverScale)
                        )
                        
                        if (isEditMode && !tile.isSpacer) {
                            Box(modifier = Modifier.matchParentSize().zIndex(20f), contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { viewModel.resizeTile(tile.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = FluentIcons.Open,
                                            contentDescription = "Resize",
                                            modifier = Modifier.size(20.dp).graphicsLayer(rotationZ = 90f),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                            Box(modifier = Modifier.matchParentSize().border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).zIndex(15f))
                        }
                    }
                }
            }
        }

        // Done button in Edit Mode
        AnimatedVisibility(
            visible = isEditMode,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).zIndex(500f)
        ) {
            Button(
                onClick = { viewModel.setEditMode(false) },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Done", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        // Dragging Overlay (The "Ghost" Tile)
        draggingTileId?.let { id ->
            tiles.find { it.id == id }?.let { tile ->
                Box(
                    modifier = Modifier
                        .size(with(density) { draggingTileSize.width.toDp() }, with(density) { draggingTileSize.height.toDp() })
                        .graphicsLayer {
                            translationX = pointerPosition.x - dragStartPointerOffset.x
                            translationY = pointerPosition.y - dragStartPointerOffset.y
                            scaleX = 1.15f
                            scaleY = 1.15f
                            shadowElevation = 24.dp.toPx()
                            shape = RoundedCornerShape(12.dp)
                            clip = false
                        }
                        .zIndex(1000f)
                ) {
                    HomeTileItem(
                        tile = tile,
                        isEditMode = true,
                        isHovered = false,
                        tileOpacity = tileOpacity,
                        weatherData = weatherData,
                        recentNotifications = tile.packageName?.let { allNotifications[it]?.recentNotifications } ?: emptyList(),
                        currentMedia = currentMedia,
                        calendarEvents = calendarEvents,
                        contacts = contacts,
                        weatherAppPackage = weatherAppPackage,
                        pointerPosition = pointerPosition,
                        onResize = { viewModel.resizeTile(tile.id) },
                        onPlayPause = { viewModel.mediaPlayPause() },
                        onSkipNext = { viewModel.mediaSkipNext() },
                        onSkipPrevious = { viewModel.mediaSkipPrevious() },
                        topNews = topNews,
                        widgetHost = viewModel.appWidgetHost
                    )
                }
            }
        }

        AnimatedVisibility(visible = backgroundMenuExpanded, enter = fadeIn() + scaleIn(initialScale = 0.95f), exit = fadeOut() + scaleOut(targetScale = 0.95f)) {
            Box(modifier = Modifier.fillMaxSize().zIndex(100f), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)).pointerInput(Unit) { detectTapGestures { backgroundMenuExpanded = false } })
                FluentSurface(modifier = Modifier.width(280.dp).padding(16.dp), shape = RoundedCornerShape(24.dp), alpha = 0.8f, effect = FluentEffect.ACRYLIC, blurRadius = 120, tintColor = Color.Black.copy(alpha = 0.25f), luminosityAlpha = 0.2f) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DESKTOP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))
                        ActionButton(text = "Add Widget", icon = Icons.Rounded.Widgets, onClick = { backgroundMenuExpanded = false; showWidgetPicker = true })
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(text = "Create folder", icon = Icons.Rounded.CreateNewFolder, onClick = { backgroundMenuExpanded = false; showNewFolderDialog = true })
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(text = "Home Settings", icon = Icons.Rounded.Settings, onClick = { backgroundMenuExpanded = false })
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(text = "Rearrange tiles", icon = Icons.Rounded.Reorder, onClick = { backgroundMenuExpanded = false; viewModel.setEditMode(true) })
                    }
                }
            }
        }

        val explodedTile = tiles.find { it.id == explodedTileId }
        if (explodedTile != null && draggingTileId == null) {
            AdvancedFluentMenu(
                tile = explodedTile,
                onDismiss = { viewModel.explodeTile(null) },
                onResize = { viewModel.resizeTile(explodedTile.id, it) },
                onRemove = { viewModel.removeTile(explodedTile.id); viewModel.explodeTile(null) },
                onRename = { folderToRename = explodedTile; viewModel.explodeTile(null) },
                onMoveTile = { viewModel.setEditMode(true); viewModel.explodeTile(null) },
                onAppSettings = { 
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", explodedTile.packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                    viewModel.explodeTile(null)
                },
                onClearNotifications = { 
                    explodedTile.packageName?.let { viewModel.clearNotifications(it) }
                    viewModel.explodeTile(null)
                },
                onShare = {
                    try {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out ${explodedTile.label}!")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share"))
                    } catch (e: Exception) {}
                    viewModel.explodeTile(null)
                },
                onUninstall = {
                    try {
                        val intent = Intent(Intent.ACTION_DELETE).apply {
                            data = Uri.fromParts("package", explodedTile.packageName, null)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                    viewModel.explodeTile(null)
                },
                shortcuts = shortcuts,
                onShortcutClick = { viewModel.launchShortcut(it); viewModel.explodeTile(null) },
                tileOpacity = tileOpacity
            )
        }

        if (showNewFolderDialog) {
            RenameFolderDialog(currentName = "New Folder", onDismiss = { showNewFolderDialog = false }, onRename = { newName -> viewModel.addEmptyFolder(newName); showNewFolderDialog = false })
        }

        if (folderToRename != null) {
            RenameFolderDialog(currentName = folderToRename!!.label, onDismiss = { folderToRename = null }, onRename = { newName -> viewModel.renameFolder(folderToRename!!.id, newName); folderToRename = null })
        }

        val openFolder = tiles.find { it.id == openFolderId }
        AnimatedVisibility(
            visible = openFolder != null,
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.1f, animationSpec = tween(150), transformOrigin = androidx.compose.ui.graphics.TransformOrigin(folderSourceCenter.x / with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.toPx() }, folderSourceCenter.y / with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx() })),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.1f, animationSpec = tween(150))
        ) {
            if (openFolder != null) {
                Box(modifier = Modifier.fillMaxSize().zIndex(300f).pointerInput(Unit) { detectTapGestures { viewModel.openFolder(null) } }, contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight()
                            .padding(16.dp)
                            .pointerInput(Unit) { detectTapGestures { } }
                    ) {
                        FluentSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp), 
                            alpha = 0.85f, 
                            effect = FluentEffect.ACRYLIC, 
                            blurRadius = 120,
                            tintColor = Color.Black.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { folderToRename = openFolder }
                                ) {
                                    Text(text = openFolder.label, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Rounded.Edit, contentDescription = "Rename", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3), 
                                    verticalArrangement = Arrangement.spacedBy(16.dp), 
                                    horizontalArrangement = Arrangement.spacedBy(16.dp), 
                                    modifier = Modifier.heightIn(max = 400.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    itemsIndexed(openFolder.subTiles) { i, subTile ->
                                        var itemVisible by remember { mutableStateOf(false) }
                                        var subDragOffset by remember { mutableStateOf(Offset.Zero) }
                                        var isSubDragging by remember { mutableStateOf(false) }
                                        var subItemPosition by remember { mutableStateOf(Offset.Zero) }
                                        var subItemSize by remember { mutableStateOf(IntSize.Zero) }
                                        
                                        LaunchedEffect(Unit) { delay(10L * i); itemVisible = true }
                                        
                                        AnimatedVisibility(
                                            visible = itemVisible, 
                                            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.1f, animationSpec = tween(150), transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center) + expandIn(expandFrom = Alignment.Center, animationSpec = tween(150))
                                        ) {
                                            HomeTileItem(
                                                tile = subTile, 
                                                tileOpacity = tileOpacity, 
                                                modifier = Modifier
                                                    .onGloballyPositioned { coords ->
                                                        subItemPosition = coords.positionInRoot()
                                                        subItemSize = coords.size
                                                    }
                                                    .zIndex(if (isSubDragging) 100f else 1f)
                                                    .graphicsLayer {
                                                        translationX = subDragOffset.x
                                                        translationY = subDragOffset.y
                                                        val scale = if (isSubDragging) 1.2f else 1f
                                                        scaleX = scale
                                                        scaleY = scale
                                                    }
                                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { 
                                                        if (!isSubDragging) {
                                                            if (subTile.packageName != null) {
                                                                onAppClick(subTile.packageName)
                                                                viewModel.openFolder(null)
                                                            }
                                                        }
                                                    }
                                                    .pointerInput(subTile.id) {
                                                        detectDragGestures(
                                                            onDragStart = { isSubDragging = true },
                                                            onDragEnd = {
                                                                isSubDragging = false
                                                                subDragOffset = Offset.Zero
                                                            },
                                                            onDragCancel = {
                                                                isSubDragging = false
                                                                subDragOffset = Offset.Zero
                                                            },
                                                            onDrag = { change, dragAmount ->
                                                                change.consume()
                                                                subDragOffset += dragAmount
                                                                
                                                                val currentGlobalPosition = subItemPosition + subDragOffset
                                                                
                                                                if (subDragOffset.getDistance() > 350f) {
                                                                    val handOffOffset = currentGlobalPosition
                                                                    
                                                                    draggingTileId = subTile.id
                                                                    draggingTileSize = subItemSize
                                                                    dragStartPointerOffset = pointerPosition - handOffOffset
                                                                    
                                                                    val relativePointer = pointerPosition - gridPosition
                                                                    val layoutInfo = gridState.layoutInfo
                                                                    val targetItem = layoutInfo.visibleItemsInfo.minByOrNull { other ->
                                                                        val centerX = other.offset.x + other.size.width / 2f
                                                                        val centerY = other.offset.y + other.size.height / 2f
                                                                        (relativePointer.x - centerX) * (relativePointer.x - centerX) +
                                                                        (relativePointer.y - centerY) * (relativePointer.y - centerY)
                                                                    }
                                                                    val dropIndex = targetItem?.index ?: tiles.size

                                                                    viewModel.removeTileFromFolder(openFolder.id, subTile.id, toIndex = dropIndex)
                                                                    viewModel.openFolder(null)
                                                                    viewModel.setEditMode(true)
                                                                    isSubDragging = false
                                                                }
                                                            }
                                                        )
                                                    }
                                            )
                                        }
                                    }
                                }
                            }

                            // FAB in Folder (Lower Right)
                            Box(modifier = Modifier.matchParentSize().padding(24.dp), contentAlignment = Alignment.BottomEnd) {
                                FloatingActionButton(
                                    onClick = onAddAppsClick,
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(56.dp).shadow(8.dp, CircleShape)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Add Apps")
                                }
                            }
                        }
                    }
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
    isHovered: Boolean = false,
    tileOpacity: Float = 0.25f,
    weatherData: com.example.windows11mobile.data.WeatherData? = null,
    recentNotifications: List<com.example.windows11mobile.data.NotificationData> = emptyList(),
    currentMedia: com.example.windows11mobile.data.MediaData? = null,
    calendarEvents: List<com.example.windows11mobile.data.CalendarEvent> = emptyList(),
    contacts: List<com.example.windows11mobile.data.Contact> = emptyList(),
    weatherAppPackage: String? = null,
    pointerPosition: Offset = Offset.Zero,
    onResize: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    topNews: List<com.example.windows11mobile.data.NewsArticle> = emptyList(),
    widgetHost: android.appwidget.AppWidgetHost? = null
) {
    val ratio = tile.spanX.toFloat() / tile.spanY.toFloat()
    val context = LocalContext.current
    var tilePosition by remember { mutableStateOf(Offset.Zero) }
    val localPointerPosition = remember(pointerPosition, tilePosition) { pointerPosition - tilePosition }
    
    val icon = remember(tile.packageName, tile.label) {
        val pm = context.packageManager
        var d = tile.packageName?.let { pkg -> try { pm.getApplicationIcon(pkg) } catch (_: Exception) { null } }
        if (d == null) {
            val fallbacks = when (tile.label.lowercase()) {
                "settings" -> listOf("com.android.settings", "com.google.android.settings"); "calendar" -> listOf("com.google.android.calendar", "com.android.calendar"); "people" -> listOf("com.android.contacts", "com.google.android.contacts"); "messaging" -> listOf("com.google.android.apps.messaging", "com.android.messaging"); "phone" -> listOf("com.google.android.dialer", "com.android.phone"); else -> emptyList()
            }
            for (pkg in fallbacks) { try { val iconFound = pm.getApplicationIcon(pkg); if (iconFound != null) { d = iconFound; break } } catch (_: Exception) {} }
        }
        d
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates -> tilePosition = coordinates.positionInRoot() }
    ) {
        if (tile.isSpacer) {
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .aspectRatio(ratio)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                )
            } else {
                Spacer(modifier = Modifier.aspectRatio(ratio))
            }
        } else if (tile.isWidget && tile.widgetId != null) {
            Box(modifier = Modifier.aspectRatio(ratio).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))) {
                WidgetHostItem(widgetId = tile.widgetId, sharedHost = widgetHost)
            }
        } else {
            Box {
                FluentSurface(
                    modifier = Modifier.aspectRatio(ratio)
                        .scale(if (isHovered) 1.1f else 1.0f)
                        .then(if (isHovered) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier),
                    alpha = tileOpacity,
                    effect = FluentEffect.ACRYLIC,
                    blurRadius = if (tile.specialType != null || tile.notificationSender != null) 240 else 120,
                    tintColor = if (tile.specialType != null || tile.notificationSender != null) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f),
                    luminosityAlpha = if (tile.specialType != null || tile.notificationSender != null) 0.3f else 0.2f,
                    color = MaterialTheme.colorScheme.surface,
                    lightRevealPosition = localPointerPosition
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            tile.isFolder -> FolderTileContent(tile)
                            tile.specialType == HomeTile.TYPE_CLOCK_WEATHER -> FlippingTileContainer(
                                isLive = true,
                                front = { ClockWeatherTileContent(tile = tile, weatherData = weatherData, onWeatherClick = { if (weatherAppPackage == "web") context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))) else context.packageManager.getLaunchIntentForPackage(weatherAppPackage ?: "")?.let { context.startActivity(it) } }) },
                                back = { WeatherForecastBack(weatherData = weatherData) }
                            )
                            tile.specialType == HomeTile.TYPE_CLOCK -> ClockTileContent(tile)
                            tile.specialType == HomeTile.TYPE_WEATHER -> WeatherTileContent(tile = tile, weatherData = weatherData, onWeatherClick = { if (weatherAppPackage == "web") context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))) else context.packageManager.getLaunchIntentForPackage(weatherAppPackage ?: "")?.let { context.startActivity(it) } })
                            tile.specialType == HomeTile.TYPE_PHOTOS -> PhotoLiveTile(tile)
                            tile.specialType == HomeTile.TYPE_MUSIC -> { val isPlaying = currentMedia?.isPlaying == true; FlippingTileContainer(isLive = currentMedia?.title != null, forceBack = isPlaying, front = { StandardTileContent(tile, icon) }, back = { MusicLiveTile(tile = tile, media = currentMedia, onPlayPause = onPlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious) }) }
                            tile.packageName?.lowercase()?.contains("calendar") == true || tile.specialType == "calendar" -> FlippingTileContainer(isLive = calendarEvents.isNotEmpty(), front = { StandardTileContent(tile, icon) }, back = { com.example.windows11mobile.ui.widgets.CalendarWidget(calendarEvents) })
                            tile.packageName?.lowercase()?.contains("people") == true || tile.packageName?.lowercase()?.contains("contacts") == true -> FlippingTileContainer(isLive = contacts.isNotEmpty(), front = { StandardTileContent(tile, icon) }, back = { PeopleTileBack(contacts) })
                            tile.specialType == HomeTile.TYPE_SETTINGS -> SettingsLiveTile(tile)
                            tile.packageName?.lowercase()?.contains("youtube") == true && (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) -> {
                                val isPlaying = currentMedia?.packageName?.contains("youtube") == true && currentMedia?.isPlaying == true
                                FlippingTileContainer(
                                    isLive = isPlaying || recentNotifications.isNotEmpty(),
                                    forceBack = isPlaying,
                                    front = { StandardTileContent(tile, icon) },
                                    back = { 
                                        YouTubeLiveTile(
                                            tile = tile, 
                                            media = currentMedia, 
                                            recentNotifications = recentNotifications,
                                            onPlayPause = onPlayPause,
                                            onSkipNext = onSkipNext,
                                            onSkipPrevious = onSkipPrevious
                                        ) 
                                    }
                                )
                            }
                            tile.packageName == "com.google.android.googlequicksearchbox" && (tile.size != TileSize.SMALL) -> {
                                FlippingTileContainer(
                                    isLive = topNews.isNotEmpty(),
                                    front = { StandardTileContent(tile, icon) },
                                    back = { NewsLiveTileBack(articles = topNews) }
                                )
                            }
                            isCommunicationApp(tile.packageName) && (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) -> { val pkg = tile.packageName ?: ""; val isMusic = isMusicApp(pkg) && currentMedia?.packageName == pkg; val backContent: @Composable () -> Unit = { when { isMusic -> MusicLiveTile(tile = tile, media = currentMedia, onPlayPause = onPlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious); pkg.contains("dialer", true) || pkg.contains("phone", true) -> PhoneLiveTile(tile, recentNotifications); pkg.contains("gmail", true) || pkg.contains("mail", true) || pkg.contains("outlook", true) -> GmailLiveTile(tile, recentNotifications); else -> MessagesLiveTile(tile, recentNotifications) } }; val isPlaying = isMusic && currentMedia?.isPlaying == true; FlippingTileContainer(isLive = isPlaying || recentNotifications.isNotEmpty(), forceBack = isPlaying, front = { StandardTileContent(tile, icon) }, back = { backContent() }) }
                            else -> { 
                                if (recentNotifications.isNotEmpty() && tile.size != TileSize.SMALL) {
                                    FlippingTileContainer(
                                        isLive = true,
                                        front = { StandardTileContent(tile, icon) },
                                        back = { GenericNotificationLiveTile(tile, recentNotifications) }
                                    )
                                } else if (tile.size == TileSize.LARGE) {
                                    FlippingTileContainer(isLive = true, front = { StandardTileContent(tile, icon) }, back = { DateBackSide() }) 
                                } else {
                                    StandardTileContent(tile, icon)
                                }
                            }
                        }
                        if (tile.notificationCount > 0 && !isEditMode) {
                            Row(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 8.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) { Text(text = if (tile.notificationCount > 99) "99+" else tile.notificationCount.toString(), style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.W300, color = MaterialTheme.colorScheme.onSurface)); Spacer(modifier = Modifier.width(6.dp)); val badgeIcon = when { tile.packageName?.contains("messaging") == true -> Icons.AutoMirrored.Rounded.Chat; tile.packageName?.contains("gmail") == true || tile.packageName?.contains("mail") == true -> Icons.Rounded.Email; tile.packageName?.contains("dialer") == true || tile.packageName?.contains("phone") == true -> Icons.Rounded.Phone; else -> Icons.Rounded.Notifications }; Icon(imageVector = badgeIcon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface) }
                        }
                    }
                }
                
                if (isEditMode && !tile.isSpacer) {
                    Box(modifier = Modifier.matchParentSize().zIndex(20f), contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onResize() },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = FluentIcons.Open,
                                    contentDescription = "Resize",
                                    modifier = Modifier.size(18.dp).graphicsLayer(rotationZ = 90f),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).zIndex(15f))
                }
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, contentColor: Color = MaterialTheme.colorScheme.onSurface) {
    TextButton(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.textButtonColors(contentColor = contentColor)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun isCommunicationApp(packageName: String?): Boolean {
    val pkg = packageName?.lowercase() ?: return false
    return pkg.contains("messaging") || pkg.contains("message") || pkg.contains("sms") || pkg.contains("gmail") || pkg.contains("mail") || pkg.contains("dialer") || pkg.contains("phone") || pkg.contains("contacts") || pkg.contains("people") || pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("messenger")
}

fun isMusicApp(packageName: String?): Boolean {
    val pkg = packageName?.lowercase() ?: return false
    return pkg.contains("music") || pkg.contains("spotify") || pkg.contains("pandora") || pkg.contains("tidal") || pkg.contains("soundcloud") || pkg.contains("youtube.music")
}

@Composable
fun WidgetHostItem(widgetId: Int, sharedHost: android.appwidget.AppWidgetHost? = null) {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = sharedHost ?: remember { android.appwidget.AppWidgetHost(context, 1024) }
    val appWidgetInfo = remember(widgetId) { 
        try { 
            appWidgetManager.getAppWidgetInfo(widgetId) 
        } catch (e: Exception) { 
            android.util.Log.e("WidgetHostItem", "Error getting widget info for $widgetId", e)
            null 
        } 
    }
    
    if (appWidgetInfo != null) { 
        key(widgetId) { 
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(8.dp), 
                factory = { ctx -> 
                    try {
                        appWidgetHost.createView(ctx, widgetId, appWidgetInfo).apply { 
                            setAppWidget(widgetId, appWidgetInfo) 
                        } 
                    } catch (e: Exception) {
                        android.util.Log.e("WidgetHostItem", "Error creating widget view", e)
                        android.appwidget.AppWidgetHostView(ctx) // Fallback empty view
                    }
                }, 
                update = { view -> }
            ) 
        } 
    } else { 
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) { 
            Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Widget not found", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) 
            } 
        } 
    }
}

@Composable
fun RenameFolderDialog(currentName: String, onDismiss: () -> Unit, onRename: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rename Folder", fontWeight = FontWeight.Bold) }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }, confirmButton = { Button(onClick = { onRename(text) }) { Text("Rename") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }, shape = RoundedCornerShape(24.dp))
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun HomeScreenPreview() { val context = LocalContext.current; val application = context.applicationContext as android.app.Application; val settingsRepository = remember { com.example.windows11mobile.data.RealSettingsRepository(context) }; val viewModel = remember { HomeViewModel(settingsRepository, application) }; com.example.windows11mobile.ui.theme.Windows11MobileTheme { HomeScreen(viewModel = viewModel, onAppClick = {}) } }
