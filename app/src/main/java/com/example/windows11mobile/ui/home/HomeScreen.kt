package com.example.windows11mobile.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.windows11mobile.data.*
import com.example.windows11mobile.ui.components.*
import com.example.windows11mobile.ui.theme.FluentIcons
import com.example.windows11mobile.ui.theme.Windows11MobileTheme
import com.example.windows11mobile.ui.widgets.CalendarWidget
import com.example.windows11mobile.ui.widgets.WidgetPickerDialog
import java.util.*
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.drawable.toBitmap

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
    val recentPhotos by viewModel.recentPhotos.collectAsStateWithLifecycle()
    val openFolderId by viewModel.openFolderId.collectAsStateWithLifecycle()
    val tilePictureEnabled by viewModel.tilePictureEnabled.collectAsStateWithLifecycle()
    val tileBlurRadius by viewModel.tileBlurRadius.collectAsStateWithLifecycle()
    val homeScreenBlurEnabled by viewModel.homeScreenBlurEnabled.collectAsStateWithLifecycle()
    val wallpaperUri by viewModel.wallpaperUri.collectAsStateWithLifecycle()
    val isEditModeState = rememberUpdatedState(isEditMode)
    val swipeDownEnabled by viewModel.swipeDownForNotifications.collectAsStateWithLifecycle()
    val showMoreTiles by viewModel.showMoreTiles.collectAsStateWithLifecycle()

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val columns = when {
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT -> if (showMoreTiles) 6 else 4
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM -> 6
        else -> 8
    }

    val gridState = rememberLazyGridState()
    
    val noiseBitmap = remember {
        val w = 128
        val h = 128
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val random = Random(42)
        for (x in 0 until w) {
            for (y in 0 until h) {
                val brightness = random.nextInt(255)
                bitmap.setPixel(x, y, android.graphics.Color.argb(brightness, 255, 255, 255))
            }
        }
        bitmap.asImageBitmap()
    }
    
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Optimized Painter for Blur Effects
    val wallpaperPainter = wallpaperUri?.let { model ->
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(model)
                .size(Size.ORIGINAL)
                .crossfade(true)
                .build()
        )
    }

    val scrollOffset = remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex * 250f + gridState.firstVisibleItemScrollOffset
        }
    }
    
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    
    var draggingTileId by remember { mutableStateOf<String?>(null) }
    var draggingTileSize by remember { mutableStateOf(IntSize.Zero) }
    var hoveredTileId by remember { mutableStateOf<String?>(null) }
    var dragStartPointerOffset by remember { mutableStateOf(Offset.Zero) }
    var pointerPosition by remember { mutableStateOf(Offset.Zero) }
    
    var backgroundMenuExpanded by remember { mutableStateOf(false) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<HomeTile?>(null) }
    var folderSourceCenter by remember { mutableStateOf(Offset.Zero) }
    var gridPosition by remember { mutableStateOf(Offset.Zero) }

    val shortcuts = remember(explodedTileId) {
        val tile = tiles.find { it.id == explodedTileId }
        tile?.packageName?.let { viewModel.getShortcuts(it) } ?: emptyList()
    }

    var pendingWidgetId by remember { mutableIntStateOf(-1) }
    var pendingWidgetInfo by remember { mutableStateOf<AppWidgetProviderInfo?>(null) }

    val isAnyOverlayOpen = explodedTileId != null || openFolderId != null || backgroundMenuExpanded || 
                           showWidgetPicker || showNewFolderDialog || folderToRename != null

    LaunchedEffect(draggingTileId) {
        viewModel.setIsDragging(draggingTileId != null)
    }

    LaunchedEffect(draggingTileId, pointerPosition) {
        val draggingId = draggingTileId ?: return@LaunchedEffect
        val layoutInfo = gridState.layoutInfo
        val relativePointer = pointerPosition - gridPosition
        
        val currentCenter = relativePointer + Offset(
            draggingTileSize.width / 2f - dragStartPointerOffset.x,
            draggingTileSize.height / 2f - dragStartPointerOffset.y
        )
        
        val targetItem = layoutInfo.visibleItemsInfo.filter { it.key != draggingId }.find { other ->
            val otherCenterX = other.offset.x + other.size.width / 2f
            val otherCenterY = other.offset.y + other.size.height / 2f
            val distSq = (currentCenter.x - otherCenterX) * (currentCenter.x - otherCenterX) +
                         (currentCenter.y - otherCenterY) * (currentCenter.y - otherCenterY)
            val swapRadius = other.size.width * 0.35f
            distSq < swapRadius * swapRadius
        }
        val candidateItem = if (targetItem == null) {
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
                    viewModel.moveTile(fromIndex, toIndex)
                }
            }
        }
    }

    LaunchedEffect(draggingTileId, pointerPosition) {
        if (draggingTileId != null) {
            val threshold = screenHeightPx * 0.15f
            while (draggingTileId != null) {
                val distFromTop = pointerPosition.y
                val distFromBottom = screenHeightPx - pointerPosition.y
                if (distFromTop < threshold) {
                    val scrollAmount = (threshold - distFromTop) / 5f
                    gridState.dispatchRawDelta(-scrollAmount)
                } else if (distFromBottom < threshold) {
                    val scrollAmount = (threshold - distFromBottom) / 5f
                    gridState.dispatchRawDelta(scrollAmount)
                }
                delay(16)
            }
        }
    }

    val widgetConfigLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: pendingWidgetId
            if (appWidgetId != -1) {
                val appWidgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)
                if (appWidgetInfo != null) viewModel.addWidgetTile(appWidgetId, appWidgetInfo.loadLabel(context.packageManager))
            }
        } else if (pendingWidgetId != -1) viewModel.appWidgetHost.deleteAppWidgetId(pendingWidgetId)
        pendingWidgetId = -1; pendingWidgetInfo = null
    }

    val bindWidgetLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: pendingWidgetId
            val info = pendingWidgetInfo
            if (appWidgetId != -1 && info != null) {
                if (info.configure != null) {
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply { component = info.configure; putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) }
                    widgetConfigLauncher.launch(intent)
                } else {
                    viewModel.addWidgetTile(appWidgetId, info.loadLabel(context.packageManager))
                    pendingWidgetId = -1; pendingWidgetInfo = null
                }
            }
        } else if (pendingWidgetId != -1) {
            viewModel.appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = -1; pendingWidgetInfo = null
        }
    }

    if (showWidgetPicker) {
        WidgetPickerDialog(availableWidgets = availableWidgets, onDismiss = { showWidgetPicker = false }, onWidgetSelected = { info ->
                showWidgetPicker = false
                val appWidgetId = viewModel.allocateWidgetId()
                val success = AppWidgetManager.getInstance(context).bindAppWidgetIdIfAllowed(appWidgetId, info.provider)
                if (success) {
                    if (info.configure != null) {
                        pendingWidgetId = appWidgetId; pendingWidgetInfo = info
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply { component = info.configure; putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) }
                        widgetConfigLauncher.launch(intent)
                    } else viewModel.addWidgetTile(appWidgetId, info.loadLabel(context.packageManager))
                } else {
                    pendingWidgetId = appWidgetId; pendingWidgetInfo = info
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId); putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider) }
                    bindWidgetLauncher.launch(intent)
                }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.Transparent)
            .pointerInput(swipeDownEnabled, isAnyOverlayOpen) {
                if (!swipeDownEnabled || isAnyOverlayOpen) return@pointerInput
                awaitEachGesture {
                    val firstDown = awaitFirstDown(pass = PointerEventPass.Initial)
                    var totalDrag = Offset.Zero
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val dragChange = event.changes.firstOrNull { it.id == firstDown.id } ?: break
                        if (dragChange.pressed) {
                            totalDrag += dragChange.position - dragChange.previousPosition
                            if (totalDrag.y > 150f && totalDrag.x.absoluteValue < totalDrag.y * 0.5f && gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset <= 0) {
                                viewModel.expandNotifications(); dragChange.consume(); break
                            }
                        } else break
                    }
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        pointerPosition = event.changes.first().position
                        if (draggingTileId != null) {
                            if (event.changes.all { !it.pressed }) {
                                val targetId = hoveredTileId
                                val draggingId = draggingTileId
                                if (draggingId != null) {
                                    val fromIndex = tiles.indexOfFirst { it.id == draggingId }
                                    if (targetId != null) {
                                        val toIndex = tiles.indexOfFirst { it.id == targetId }
                                        if (fromIndex != -1 && toIndex != -1) viewModel.moveTile(fromIndex, toIndex)
                                    } else if (fromIndex != -1) viewModel.moveTile(fromIndex, fromIndex)
                                }
                                draggingTileId = null; hoveredTileId = null; viewModel.setIsDragging(false)
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
                        val holdJob = launch { delay(1300); if (!isConsumedElsewhere && draggingTileId == null && explodedTileId == null && openFolderId == null) { backgroundMenuExpanded = true; haptics.performHapticFeedback(HapticFeedbackType.LongPress) } }
                        try {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                if (event.changes.any { it.isConsumed }) { isConsumedElsewhere = true; holdJob.cancel() }
                                if (event.changes.all { !it.pressed }) { if (!isConsumedElsewhere && !backgroundMenuExpanded) { if (isEditMode) viewModel.setEditMode(false) }; break }
                            }
                        } finally { holdJob.cancel() }
                    }
                }
            }
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize().onGloballyPositioned { gridPosition = it.positionInRoot() },
            contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 4.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(items = tiles, key = { _, tile -> tile.id }, span = { _, tile -> GridItemSpan(tile.spanX.coerceAtMost(columns)) }) { _, tile ->
                val isDragging = draggingTileId == tile.id
                val wobbleTransition = rememberInfiniteTransition(label = "wobble")
                val wobbleRotation by wobbleTransition.animateFloat(initialValue = -1f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(150, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "wobbleRotate")
                val zIndexValue by animateFloatAsState(targetValue = if (isDragging) 100f else 1f, label = "dragZIndex")
                var itemPosition by remember { mutableStateOf(Offset.Zero) }
                var itemSize by remember { mutableStateOf(IntSize.Zero) }
                Box(
                    modifier = Modifier.onGloballyPositioned { coords -> itemPosition = coords.positionInRoot(); itemSize = coords.size }.then(if (!isDragging) Modifier.animateItem() else Modifier).zIndex(zIndexValue).graphicsLayer { alpha = if (isDragging) 0f else 1f; rotationZ = if (isEditMode && !isDragging) wobbleRotation else 0f }
                        .pointerInput(tile.id) {
                            coroutineScope {
                                awaitEachGesture {
                                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                    val isInResizeZone = down.position.x > (size.width - 48.dp.toPx()) && down.position.y > (size.height - 48.dp.toPx())
                                    if (isEditModeState.value && !isInResizeZone) down.consume()
                                    var dragStarted = false
                                    var hasMovedSignificant = false
                                    val isHoldTriggered = BooleanArray(1) { false }
                                    val holdJob = launch { if (!isEditModeState.value) { delay(750); if (draggingTileId == null) { viewModel.explodeTile(tile.id); isHoldTriggered[0] = true; haptics.performHapticFeedback(HapticFeedbackType.LongPress) } } else isHoldTriggered[0] = true }
                                    try {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                                            val totalDrag = pointer.position - down.position
                                            val isMoving = totalDrag.getDistance() > viewConfiguration.touchSlop
                                            if (isMoving) hasMovedSignificant = true
                                            if (pointer.pressed) {
                                                if ((isHoldTriggered[0] || isEditModeState.value) && !isInResizeZone) {
                                                    pointer.consume()
                                                    if (!dragStarted && (isEditModeState.value || isMoving)) {
                                                        dragStarted = true; holdJob.cancel()
                                                        if (explodedTileId == tile.id) viewModel.explodeTile(null)
                                                        if (!isEditModeState.value) { viewModel.setEditMode(true); viewModel.setIsDragging(true); haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
                                                        draggingTileId = tile.id; draggingTileSize = itemSize; dragStartPointerOffset = down.position
                                                    }
                                                } else if (hasMovedSignificant && !dragStarted) holdJob.cancel()
                                            } else {
                                                holdJob.cancel()
                                                if (!dragStarted && !isHoldTriggered[0] && !hasMovedSignificant) {
                                                    if (isInResizeZone && isEditModeState.value) { viewModel.resizeTile(tile.id); haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
                                                    else if (tile.isFolder) { folderSourceCenter = itemPosition + Offset(itemSize.width / 2f, itemSize.height / 2f); viewModel.openFolder(tile.id) }
                                                    else if (tile.packageName != null) onAppClick(tile.packageName)
                                                    else if (tile.specialType == HomeTile.TYPE_CLOCK || tile.specialType == HomeTile.TYPE_CLOCK_WEATHER) { try { context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS)) } catch (e: Exception) {} }
                                                }
                                                break
                                            }
                                        }
                                    } finally { holdJob.cancel() }
                                }
                            }
                        }
                ) {
                    val isHoverTarget = hoveredTileId == tile.id
                    val pulseTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by pulseTransition.animateFloat(initialValue = 1.05f, targetValue = 1.15f, animationSpec = infiniteRepeatable(animation = tween(400, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "pulse")
                    val hoverScale by animateFloatAsState(targetValue = if (isHoverTarget) pulseScale else 1f, animationSpec = spring(stiffness = Spring.StiffnessLow), label = "hoverScale")
                    Box {
                        HomeTileItem(
                            tile = tile, isEditMode = isEditMode, isHovered = isHoverTarget, tileOpacity = tileOpacity, weatherData = weatherData, 
                            recentNotifications = tile.packageName?.let { allNotifications[it]?.recentNotifications } ?: emptyList(),
                            allNotifications = allNotifications,
                            currentMedia = currentMedia, calendarEvents = calendarEvents, contacts = contacts, weatherAppPackage = weatherAppPackage, 
                            recentPhotos = recentPhotos, tilePictureEnabled = tilePictureEnabled, 
                            tileBlurRadius = tileBlurRadius, homeScreenBlurEnabled = homeScreenBlurEnabled, wallpaperPainter = wallpaperPainter, 
                            scrollOffset = scrollOffset.value, screenWidthPx = screenWidthPx, screenHeightPx = screenHeightPx, 
                            noiseBitmap = noiseBitmap, onResize = { viewModel.resizeTile(tile.id) }, onPlayPause = { viewModel.mediaPlayPause() }, 
                            onSkipNext = { viewModel.mediaSkipNext() }, onSkipPrevious = { viewModel.mediaSkipPrevious() }, 
                            topNews = topNews, widgetHost = viewModel.appWidgetHost, modifier = Modifier.scale(hoverScale)
                        )
                        if (isEditMode && !tile.isSpacer) {
                            Box(modifier = Modifier.matchParentSize().zIndex(20f), contentAlignment = Alignment.BottomEnd) {
                                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
                                        Icon(imageVector = FluentIcons.Open, contentDescription = "Resize", modifier = Modifier.size(18.dp).graphicsLayer(rotationZ = 90f), tint = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                            Box(modifier = Modifier.matchParentSize().border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).zIndex(15f))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = isEditMode, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut(), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).zIndex(500f)) {
            Button(onClick = { viewModel.setEditMode(false) }, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Done", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        draggingTileId?.let { id ->
            tiles.find { it.id == id }?.let { tile ->
                Box(modifier = Modifier.size(with(density) { draggingTileSize.width.toDp() }, with(density) { draggingTileSize.height.toDp() }).graphicsLayer { translationX = pointerPosition.x - dragStartPointerOffset.x; translationY = pointerPosition.y - dragStartPointerOffset.y; scaleX = 1.15f; scaleY = 1.15f; shadowElevation = 24.dp.toPx(); shape = RoundedCornerShape(12.dp); clip = false }.zIndex(1000f)) {
                    HomeTileItem(
                        tile = tile, isEditMode = true, isHovered = false, tileOpacity = tileOpacity, weatherData = weatherData, 
                        recentNotifications = tile.packageName?.let { allNotifications[it]?.recentNotifications } ?: emptyList(),
                        allNotifications = allNotifications,
                        currentMedia = currentMedia, calendarEvents = calendarEvents, contacts = contacts, weatherAppPackage = weatherAppPackage, 
                        recentPhotos = recentPhotos, tilePictureEnabled = tilePictureEnabled, 
                        tileBlurRadius = tileBlurRadius, homeScreenBlurEnabled = homeScreenBlurEnabled, wallpaperPainter = wallpaperPainter, 
                        scrollOffset = scrollOffset.value, screenWidthPx = screenWidthPx, screenHeightPx = screenHeightPx, 
                        noiseBitmap = noiseBitmap, onResize = { viewModel.resizeTile(tile.id) }, onPlayPause = { viewModel.mediaPlayPause() }, 
                        onSkipNext = { viewModel.mediaSkipNext() }, onSkipPrevious = { viewModel.mediaSkipPrevious() }, 
                        topNews = topNews, widgetHost = viewModel.appWidgetHost
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
                        ActionButton(text = "Add Widget", icon = Icons.Rounded.Widgets, onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); backgroundMenuExpanded = false; showWidgetPicker = true })
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(text = "Create folder", icon = Icons.Rounded.CreateNewFolder, onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); backgroundMenuExpanded = false; showNewFolderDialog = true })
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(text = "Home Settings", icon = Icons.Rounded.Settings, onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); backgroundMenuExpanded = false })
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionButton(text = "Rearrange tiles", icon = Icons.Rounded.Reorder, onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); backgroundMenuExpanded = false; viewModel.setEditMode(true) })
                    }
                }
            }
        }

        val openFolder = tiles.find { it.id == openFolderId }
        AnimatedVisibility(visible = openFolder != null, enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.1f, animationSpec = tween(150), transformOrigin = TransformOrigin(folderSourceCenter.x / with(density) { configuration.screenWidthDp.dp.toPx() }, folderSourceCenter.y / with(density) { configuration.screenHeightDp.dp.toPx() })), exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.1f, animationSpec = tween(150))) {
            if (openFolder != null) {
                Box(modifier = Modifier.fillMaxSize().zIndex(300f).pointerInput(Unit) { detectTapGestures { viewModel.openFolder(null) } }, contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight().padding(16.dp).pointerInput(Unit) { detectTapGestures { } }) {
                        FluentSurface(modifier = Modifier.fillMaxWidth().heightIn(max = (configuration.screenHeightDp * 0.8).dp).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), alpha = 0.85f, effect = FluentEffect.ACRYLIC, blurRadius = 120, tintColor = Color.Black.copy(alpha = 0.3f)) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { haptics.performHapticFeedback(HapticFeedbackType.LongPress); folderToRename = openFolder }) {
                                    Text(text = openFolder.label, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface); Spacer(modifier = Modifier.width(8.dp)); Icon(Icons.Rounded.Edit, contentDescription = "Rename", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Box(modifier = Modifier.weight(1f, fill = false)) {
                                    LazyVerticalGrid(columns = GridCells.Fixed(3), verticalArrangement = Arrangement.spacedBy(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 80.dp)) {
                                        itemsIndexed(openFolder.subTiles) { i, subTile ->
                                            var itemVisible by remember { mutableStateOf(false) }
                                            var subDragOffset by remember { mutableStateOf(Offset.Zero) }
                                            var isSubDragging by remember { mutableStateOf(false) }
                                            var subItemPosition by remember { mutableStateOf(Offset.Zero) }
                                            var subItemSize by remember { mutableStateOf(IntSize.Zero) }
                                            LaunchedEffect(Unit) { delay(10L * i); itemVisible = true }
                                            if (itemVisible) {
                                                HomeTileItem(
                                                    tile = subTile, tileOpacity = tileOpacity, recentNotifications = emptyList(), 
                                                    allNotifications = allNotifications,
                                                    currentMedia = null, calendarEvents = emptyList(), contacts = emptyList(), 
                                                    weatherAppPackage = null, recentPhotos = recentPhotos, 
                                                    tilePictureEnabled = tilePictureEnabled, tileBlurRadius = tileBlurRadius, 
                                                    homeScreenBlurEnabled = homeScreenBlurEnabled, wallpaperPainter = wallpaperPainter, 
                                                    scrollOffset = 0f, screenWidthPx = screenWidthPx, screenHeightPx = screenHeightPx, 
                                                    noiseBitmap = noiseBitmap, modifier = Modifier.onGloballyPositioned { coords -> subItemPosition = coords.positionInRoot(); subItemSize = coords.size }.zIndex(if (isSubDragging) 100f else 1f).graphicsLayer { translationX = subDragOffset.x; translationY = subDragOffset.y; val scale = if (isSubDragging) 1.2f else 1f; scaleX = scale; scaleY = scale }
                                                    .pointerInput(subTile.id) {
                                                        coroutineScope {
                                                            awaitEachGesture {
                                                                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                                                var dragStarted = false
                                                                var hasMovedSignificant = false
                                                                val isHoldTriggered = BooleanArray(1) { false }
                                                                val holdJob = launch { delay(750); if (!isSubDragging) { haptics.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.explodeTile(subTile.id); isHoldTriggered[0] = true } }
                                                                try {
                                                                    while (true) {
                                                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                                                        val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                                                                        val totalDrag = pointer.position - down.position
                                                                        val isMoving = totalDrag.getDistance() > viewConfiguration.touchSlop
                                                                        if (isMoving) hasMovedSignificant = true
                                                                        if (pointer.pressed) {
                                                                            if (isHoldTriggered[0] || isMoving) {
                                                                                pointer.consume()
                                                                                if (!dragStarted && isMoving) { dragStarted = true; isSubDragging = true; holdJob.cancel() }
                                                                                if (dragStarted) {
                                                                                    subDragOffset += pointer.position - pointer.previousPosition
                                                                                    if (subDragOffset.getDistance() > 350f) {
                                                                                        draggingTileId = subTile.id; draggingTileSize = subItemSize; dragStartPointerOffset = pointerPosition - (subItemPosition + subDragOffset)
                                                                                        val relativePointer = pointerPosition - gridPosition
                                                                                        val layoutInfo = gridState.layoutInfo
                                                                                        val targetItem = layoutInfo.visibleItemsInfo.minByOrNull { other ->
                                                                                            val centerX = other.offset.x + other.size.width / 2f
                                                                                            val centerY = other.offset.y + other.size.height / 2f
                                                                                            (relativePointer.x - centerX) * (relativePointer.x - centerX) + (relativePointer.y - centerY) * (relativePointer.y - centerY)
                                                                                        }
                                                                                        viewModel.removeTileFromFolder(openFolder.id, subTile.id, toIndex = targetItem?.index ?: tiles.size); viewModel.openFolder(null); viewModel.setEditMode(true); isSubDragging = false; return@awaitEachGesture
                                                                                    }
                                                                                }
                                                                            }
                                                                        } else {
                                                                            holdJob.cancel()
                                                                            if (!dragStarted && !isHoldTriggered[0] && !hasMovedSignificant) { if (subTile.packageName != null) { onAppClick(subTile.packageName); viewModel.openFolder(null) } }
                                                                            isSubDragging = false; subDragOffset = Offset.Zero; break
                                                                        }
                                                                    }
                                                                } finally { holdJob.cancel() }
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Box(modifier = Modifier.matchParentSize().padding(24.dp), contentAlignment = Alignment.BottomEnd) {
                                FloatingActionButton(onClick = onAddAppsClick, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, shape = CircleShape, modifier = Modifier.size(56.dp).shadow(8.dp, CircleShape)) { Icon(Icons.Rounded.Add, contentDescription = "Add Apps") }
                            }
                        }
                    }
                }
            }
        }

        val (explodedTile, _) = remember(explodedTileId, tiles) {
            if (explodedTileId == null) null to false
            else {
                val mainTile = tiles.find { it.id == explodedTileId }
                if (mainTile != null) mainTile to false
                else {
                    val subTile = tiles.flatMap { it.subTiles }.find { it.id == explodedTileId }
                    subTile to (subTile != null)
                }
            }
        }
        if (explodedTile != null && draggingTileId == null) {
            AdvancedFluentMenu(
                tile = explodedTile, onDismiss = { viewModel.explodeTile(null) }, onResize = { viewModel.resizeTile(explodedTile.id, it) },
                onRemove = { viewModel.removeTile(explodedTile.id); viewModel.explodeTile(null) }, onRename = { folderToRename = explodedTile; viewModel.explodeTile(null) },
                onMoveTile = { viewModel.setEditMode(true); viewModel.explodeTile(null) }, onClearNotifications = { explodedTile.packageName?.let { viewModel.clearNotifications(it) }; viewModel.explodeTile(null) },
                onAppSettings = { try { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", explodedTile.packageName, null); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (e: Exception) {}; viewModel.explodeTile(null) },
                onUninstall = {
                    val pkgName = explodedTile.packageName
                    if (pkgName != null) {
                        try { val intent = Intent(Intent.ACTION_DELETE).apply { data = Uri.parse("package:$pkgName"); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }; context.startActivity(intent) }
                        catch (e: Exception) { try { val fallback = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("market://details?id=$pkgName"); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }; context.startActivity(fallback) } catch (e2: Exception) {} }
                    }
                    viewModel.explodeTile(null)
                },
                onShare = { try { val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Check out ${explodedTile.label}!") }; context.startActivity(Intent.createChooser(intent, "Share")) } catch (e: Exception) {}; viewModel.explodeTile(null) },
                onCheckForUpdates = { try { context.startActivity(Intent("com.google.android.finsky.VIEW_MY_DOWNLOADS").apply { setPackage("com.android.vending"); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (e: Exception) { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.android.vending")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (e2: Exception) {} }; viewModel.explodeTile(null) },
                shortcuts = shortcuts, onShortcutClick = { viewModel.launchShortcut(it); viewModel.explodeTile(null) }, tileOpacity = tileOpacity
            )
        }
        if (showNewFolderDialog) RenameFolderDialog(currentName = "New Folder", onDismiss = { showNewFolderDialog = false }, onRename = { newName -> viewModel.addEmptyFolder(newName); showNewFolderDialog = false })
        if (folderToRename != null) RenameFolderDialog(currentName = folderToRename!!.label, onDismiss = { folderToRename = null }, onRename = { newName -> viewModel.renameFolder(folderToRename!!.id, newName); folderToRename = null })
    }
}

@Composable
fun HomeTileItem(
    tile: HomeTile, modifier: Modifier = Modifier, isEditMode: Boolean = false, isHovered: Boolean = false, tileOpacity: Float = 0.25f,
    weatherData: WeatherData? = null, recentNotifications: List<NotificationData> = emptyList(), 
    allNotifications: Map<String, AppNotificationData> = emptyMap(),
    currentMedia: MediaData? = null, calendarEvents: List<CalendarEvent> = emptyList(), contacts: List<Contact> = emptyList(), 
    weatherAppPackage: String? = null, recentPhotos: List<Uri> = emptyList(), 
    tilePictureEnabled: Boolean = false, tileBlurRadius: Float = 60f, homeScreenBlurEnabled: Boolean = true, 
    wallpaperPainter: Painter? = null, scrollOffset: Float = 0f, screenWidthPx: Float = 0f, screenHeightPx: Float = 0f, 
    noiseBitmap: ImageBitmap? = null, onResize: () -> Unit = {}, onPlayPause: () -> Unit = {}, onSkipNext: () -> Unit = {}, 
    onSkipPrevious: () -> Unit = {}, topNews: List<NewsArticle> = emptyList(), widgetHost: AppWidgetHost? = null
) {
    val ratio = tile.spanX.toFloat() / tile.spanY.toFloat()
    val context = LocalContext.current
    var tilePosition by remember { mutableStateOf(Offset.Zero) }
    val icon = remember(tile.packageName, tile.label) {
        val pm = context.packageManager
        var d = tile.packageName?.let { pkg -> try { pm.getApplicationIcon(pkg) } catch (_: Exception) { null } }
        if (d == null) {
            val fallbacks = when (tile.label.lowercase()) {
                "settings" -> listOf("com.android.settings", "com.google.android.settings"); "calendar" -> listOf("com.google.android.calendar", "com.android.calendar"); "people" -> listOf("com.android.contacts", "com.google.android.contacts"); "messaging" -> listOf("com.google.android.apps.messaging", "com.android.messaging"); "phone" -> listOf("com.android.dialer", "com.android.phone"); else -> emptyList()
            }
            for (pkg in fallbacks) { try { val iconFound = pm.getApplicationIcon(pkg); if (iconFound != null) { d = iconFound; break } } catch (_: Exception) {} }
        }
        d
    }

    Box(modifier = modifier.onGloballyPositioned { coords -> tilePosition = coords.positionInRoot() }) {
        if (tile.isSpacer) {
            if (isEditMode) Box(modifier = Modifier.aspectRatio(ratio).clip(RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)))
            else Spacer(modifier = Modifier.aspectRatio(ratio))
        } else if (tile.isWidget && tile.widgetId != null) {
            Box {
                Box(modifier = Modifier.aspectRatio(ratio).scale(if (isHovered) 1.1f else 1.0f).clip(RoundedCornerShape(12.dp)).then(if (isHovered) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)) {
                    WidgetHostItem(widgetId = tile.widgetId, sharedHost = widgetHost, size = tile.size)
                }
                if (isEditMode) {
                    Box(modifier = Modifier.matchParentSize().zIndex(20f), contentAlignment = Alignment.BottomEnd) {
                        Box(modifier = Modifier.size(48.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onResize() }, contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
                                Icon(imageVector = FluentIcons.Open, contentDescription = "Resize", modifier = Modifier.size(18.dp).graphicsLayer(rotationZ = 90f), tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).zIndex(15f))
                }
            }
        } else {
            val isStandardTile = !tile.isWidget && !tile.isSpacer
            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            
            Box(
                modifier = Modifier
                    .aspectRatio(ratio)
                    .scale(if (isHovered) 1.1f else 1.0f)
                    .clip(RoundedCornerShape(12.dp))
                    .then(if (isHovered) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)
            ) {
                // Background Layer for Picture Mode (Parallax)
                if (tilePictureEnabled && wallpaperPainter != null && isStandardTile) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .then(if (homeScreenBlurEnabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    Modifier.graphicsLayer { 
                                        // Match FluentSurface's intensified blur logic for high radii
                                        val radiusMultiplier = 1f + (tileBlurRadius / 500f)
                                        val radius = tileBlurRadius * radiusMultiplier
                                        
                                        val blur = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                                        val matrix = ColorMatrix().apply {
                                            setSaturation(1.8f + (tileBlurRadius / 500f)) 
                                        }
                                        val colorFilter = RenderEffect.createColorFilterEffect(
                                            ColorMatrixColorFilter(matrix)
                                        )
                                        renderEffect = RenderEffect.createChainEffect(blur, colorFilter).asComposeRenderEffect()
                                    }
                                } else {
                                    Modifier.blur((tileBlurRadius / 4f).dp)
                                }
                            } else Modifier)
                            .drawBehind {
                                val s = 3.5f
                                val sw = screenWidthPx
                                val sh = screenHeightPx
                                val offsetX = (sw * (s - 1f)) / 2f
                                val offsetY = (sh * (s - 1f)) / 2f
                                val parallaxY = scrollOffset * 0.2f
                                
                                drawIntoCanvas { canvas ->
                                    canvas.save()
                                    canvas.translate(-tilePosition.x - offsetX, -tilePosition.y - offsetY - parallaxY)
                                    canvas.scale(s, s)
                                    with(wallpaperPainter) {
                                        draw(size = androidx.compose.ui.geometry.Size(sw, sh))
                                    }
                                    canvas.restore()
                                }
                            }
                    )
                }

                // Shared Acrylic Surface Theme - Styled to match Settings Page
                FluentSurface(
                    modifier = Modifier.fillMaxSize(),
                    alpha = tileOpacity,
                    effect = if (isStandardTile && (homeScreenBlurEnabled || tilePictureEnabled)) FluentEffect.ACRYLIC else FluentEffect.NONE,
                    blurRadius = tileBlurRadius.toInt(),
                    tintColor = if (isDark) Color.Black.copy(alpha = 0.25f * tileOpacity) else Color.White.copy(alpha = 0.2f * tileOpacity),
                    luminosityAlpha = if (isDark) 0.15f * tileOpacity else 0.25f * tileOpacity,
                    borderAlpha = if (isHovered) 0.6f else 0.2f
                ) {
                    Box(modifier = Modifier.fillMaxSize()) { 
                        TileDispatcher(tile, weatherData, recentNotifications, allNotifications, currentMedia, calendarEvents, contacts, weatherAppPackage, recentPhotos, icon, onPlayPause, onSkipNext, onSkipPrevious, topNews, tileOpacity) 
                    }
                }

                if (isEditMode) {
                    Box(modifier = Modifier.matchParentSize().zIndex(20f), contentAlignment = Alignment.BottomEnd) {
                        Box(modifier = Modifier.size(48.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onResize() }, contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
                                Icon(imageVector = FluentIcons.Open, contentDescription = "Resize", modifier = Modifier.size(18.dp).graphicsLayer(rotationZ = 90f), tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).zIndex(15f))
                }
                
                if (tile.notificationCount > 0 && !isEditMode) {
                    Box(modifier = Modifier.matchParentSize().padding(4.dp), contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            tonalElevation = 16.dp,
                            shadowElevation = 20.dp,
                            modifier = Modifier.sizeIn(minWidth = 36.dp, minHeight = 36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = if (tile.notificationCount > 99) "99+" else tile.notificationCount.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
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
private fun TileDispatcher(
    tile: HomeTile, weatherData: WeatherData?, recentNotifications: List<NotificationData>, 
    allNotifications: Map<String, AppNotificationData>, currentMedia: MediaData?,
    calendarEvents: List<CalendarEvent>, contacts: List<Contact>, weatherAppPackage: String?, recentPhotos: List<Uri>,
    icon: Drawable?, onPlayPause: () -> Unit, onSkipNext: () -> Unit, onSkipPrevious: () -> Unit, topNews: List<NewsArticle>,
    tileOpacity: Float
) {
    val context = LocalContext.current
    when {
        tile.isFolder -> FolderTileContent(tile, allNotifications)
        tile.specialType == HomeTile.TYPE_CLOCK_WEATHER -> FlippingTileContainer(isLive = true, front = { ClockWeatherTileContent(tile = tile, weatherData = weatherData, onWeatherClick = { if (weatherAppPackage == "web") context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))) else context.packageManager.getLaunchIntentForPackage(weatherAppPackage ?: "")?.let { context.startActivity(it) } }) }, back = { WeatherForecastBack(weatherData = weatherData) })
        tile.specialType == HomeTile.TYPE_CLOCK || tile.packageName?.lowercase()?.contains("clock") == true -> ClockTileContent(tile)
        tile.specialType == HomeTile.TYPE_WEATHER || tile.packageName?.lowercase()?.contains("weather") == true -> WeatherTileContent(tile = tile, weatherData = weatherData, onWeatherClick = { if (weatherAppPackage == "web") context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))) else context.packageManager.getLaunchIntentForPackage(weatherAppPackage ?: "")?.let { context.startActivity(it) } })
        tile.specialType == HomeTile.TYPE_PHOTOS || tile.packageName?.lowercase()?.contains("photos") == true || tile.packageName?.lowercase()?.contains("gallery") == true -> PhotoLiveTile(tile, recentPhotos, tileOpacity)
        tile.specialType == HomeTile.TYPE_MUSIC || (isMusicApp(tile.packageName) && currentMedia?.packageName == tile.packageName) -> { val isPlaying = currentMedia?.isPlaying == true; FlippingTileContainer(isLive = currentMedia?.title != null, forceBack = isPlaying, front = { StandardTileContent(tile, icon) }, back = { MusicLiveTile(tile = tile, media = currentMedia, onPlayPause = onPlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious, tileOpacity = tileOpacity) }) }
        tile.packageName?.lowercase()?.contains("calendar") == true || tile.specialType == "calendar" -> FlippingTileContainer(isLive = true, front = { StandardTileContent(tile, icon) }, back = { CalendarWidget(calendarEvents) })
        tile.packageName?.lowercase()?.contains("people") == true || tile.packageName?.lowercase()?.contains("contacts") == true -> FlippingTileContainer(isLive = true, front = { StandardTileContent(tile, icon) }, back = { PeopleTileBack(contacts) })
        tile.specialType == HomeTile.TYPE_SETTINGS -> SettingsLiveTile(tile)
        tile.packageName?.lowercase()?.contains("youtube") == true && (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) -> { val isPlaying = currentMedia?.packageName?.contains("youtube") == true && currentMedia?.isPlaying == true; FlippingTileContainer(isLive = isPlaying || recentNotifications.isNotEmpty(), forceBack = isPlaying, front = { StandardTileContent(tile, icon) }, back = { YouTubeLiveTile(tile = tile, media = currentMedia, recentNotifications = recentNotifications, onPlayPause = onPlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious, tileOpacity = tileOpacity) }) }
        (tile.packageName?.contains("com.microsoft.news") == true || tile.packageName?.contains("msn.news") == true || tile.packageName?.contains("bingnews") == true) && (tile.size != TileSize.SMALL) -> {
            val newsNotifications = recentNotifications.filter { it.bigPicture != null || it.largeIcon != null }
            if (newsNotifications.isNotEmpty()) {
                NotificationNewsLiveTile(tile, newsNotifications, tileOpacity)
            } else {
                FlippingTileContainer(isLive = topNews.isNotEmpty(), front = { StandardTileContent(tile, icon) }, back = { NewsLiveTileBack(articles = topNews, tileOpacity = tileOpacity) })
            }
        }
        tile.packageName == "com.google.android.googlequicksearchbox" && (tile.size != TileSize.SMALL) -> { FlippingTileContainer(isLive = topNews.isNotEmpty(), front = { StandardTileContent(tile, icon) }, back = { NewsLiveTileBack(articles = topNews, tileOpacity = tileOpacity) }) }
        isCommunicationApp(tile.packageName) && (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) -> { val pkg = tile.packageName ?: ""; val isMusic = isMusicApp(pkg) && currentMedia?.packageName == pkg; val backContent: @Composable () -> Unit = { when { isMusic -> MusicLiveTile(tile = tile, media = currentMedia, onPlayPause = onPlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious, tileOpacity = tileOpacity); pkg.contains("dialer", true) || pkg.contains("phone", true) -> PhoneLiveTile(tile, recentNotifications); pkg.contains("gmail", true) || pkg.contains("mail", true) || pkg.contains("outlook", true) -> GmailLiveTile(tile, recentNotifications); else -> MessagesLiveTile(tile, recentNotifications) } }; val isPlaying = isMusic && currentMedia?.isPlaying == true; FlippingTileContainer(isLive = isPlaying || recentNotifications.isNotEmpty(), forceBack = isPlaying, front = { StandardTileContent(tile, icon) }, back = { backContent() }) }
        else -> { if (recentNotifications.isNotEmpty() && tile.size != TileSize.SMALL) FlippingTileContainer(isLive = true, front = { StandardTileContent(tile, icon) }, back = { GenericNotificationLiveTile(tile, recentNotifications) }) else if (tile.size == TileSize.LARGE) FlippingTileContainer(isLive = true, front = { StandardTileContent(tile, icon) }, back = { DateBackSide() }) else StandardTileContent(tile, icon) }
    }
}

@Composable
fun WidgetHostItem(widgetId: Int, sharedHost: AppWidgetHost? = null, size: TileSize = TileSize.MEDIUM) {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = sharedHost ?: remember { AppWidgetHost(context, APPWIDGET_HOST_ID) }
    val appWidgetInfo = remember(widgetId) { try { appWidgetManager.getAppWidgetInfo(widgetId) } catch (e: Exception) { null } }
    if (appWidgetInfo != null) {
        key(widgetId) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx -> try { appWidgetHost.createView(ctx, widgetId, appWidgetInfo).apply { setAppWidget(widgetId, appWidgetInfo); setPadding(0, 0, 0, 0) } } catch (e: Exception) {
                AppWidgetHostView(ctx)
            } },
                update = { view -> val width = (size.spanX * 100).coerceAtLeast(100); val height = (size.spanY * 100).coerceAtLeast(100); val options = Bundle().apply { putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, width); putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, height); putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, width * 2); putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, height * 2) }; appWidgetManager.updateAppWidgetOptions(widgetId, options) }
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error); Text("Widget not found", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) } }
    }
}

@Composable
fun RenameFolderDialog(currentName: String, onDismiss: () -> Unit, onRename: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rename Folder", fontWeight = FontWeight.Bold) }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }, confirmButton = { Button(onClick = { onRename(text) }) { Text("Rename") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }, shape = RoundedCornerShape(24.dp))
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun HomeScreenPreview() { 
    val context = LocalContext.current
    val application = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { context.applicationContext as Application } ?: context.applicationContext as Application
    val settingsRepository = remember { RealSettingsRepository(context) }
    val rssRepository = remember { RssRepository() }
    val viewModel = remember { HomeViewModel(settingsRepository, rssRepository, application) }
    Windows11MobileTheme { HomeScreen(viewModel = viewModel, onAppClick = {}) }
}

private const val APPWIDGET_HOST_ID = 1024
