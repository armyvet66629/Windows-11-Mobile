package com.example.windows11mobile.ui.home

import com.example.windows11mobile.data.PhoneTileData
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import coil.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
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
import androidx.compose.ui.unit.toSize
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
    val useSystemWallpaper by viewModel.useSystemWallpaper.collectAsStateWithLifecycle()
    val accentColorOverlayEnabled by viewModel.accentColorOverlayEnabled.collectAsStateWithLifecycle()
    val solidTilesEnabled by viewModel.solidTilesEnabled.collectAsStateWithLifecycle()
    val squareTilesEnabled by viewModel.squareTilesEnabled.collectAsStateWithLifecycle()
    val phoneData by viewModel.phoneTileData.collectAsStateWithLifecycle()
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

    val wallpaperSource: Any? = remember(wallpaperUri, useSystemWallpaper) {
        if (!useSystemWallpaper && wallpaperUri != null) {
            wallpaperUri
        } else {
            try {
                @SuppressLint("MissingPermission")
                val drawable = WallpaperManager.getInstance(context).drawable
                drawable
            } catch (_: Exception) {
                null
            }
        }
    }

    var wallpaperBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(wallpaperSource) {
        val src = wallpaperSource ?: run {
            wallpaperBitmap = null
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            val bmp: Bitmap? = when (src) {
                is String -> {
                    try {
                        val loader = ImageLoader(context)
                        val req = ImageRequest.Builder(context)
                            .data(src)
                            .allowHardware(false)
                            .build()
                        val result = loader.execute(req)
                        (result.drawable as? BitmapDrawable)?.bitmap
                    } catch (_: Exception) { null }
                }
                is Drawable -> {
                    (src as? BitmapDrawable)?.bitmap ?: run {
                        try {
                            val w = src.intrinsicWidth.coerceAtLeast(1)
                            val h = src.intrinsicHeight.coerceAtLeast(1)
                            val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                            val c = Canvas(b)
                            src.setBounds(0, 0, w, h)
                            src.draw(c)
                            b
                        } catch (_: Exception) { null }
                    }
                }
                else -> null
            }
            wallpaperBitmap = bmp?.asImageBitmap()
        }
    }

    // Optimized Painter for Blur / Tile Picture Effects
    val wallpaperPainter = wallpaperSource?.let { model ->
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(model)
                .size(Size.ORIGINAL)
                .crossfade(true)
                .build()
        )
    }

    val scrollState = rememberScrollState()
    val scrollPx = scrollState.value.toFloat()

    var gridPosition by remember { mutableStateOf(Offset.Zero) }

    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels.toFloat()
    val screenHeightPx = displayMetrics.heightPixels.toFloat()
    
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

    val layoutParallaxX = (gridPosition.x * 0.15f)
    val layoutParallaxY = (gridPosition.y * 0.15f + scrollPx * 0.15f)

    val shortcuts = remember(explodedTileId) {
        val tile = tiles.find { it.id == explodedTileId }
        tile?.packageName?.let { viewModel.getShortcuts(it) } ?: emptyList()
    }

    var pendingWidgetId by remember { mutableIntStateOf(-1) }
    var pendingWidgetInfo by remember { mutableStateOf<AppWidgetProviderInfo?>(null) }

    val isAnyOverlayOpen = explodedTileId != null || openFolderId != null || backgroundMenuExpanded || 
                           showWidgetPicker || showNewFolderDialog || folderToRename != null

    val tilePositions = remember { mutableStateMapOf<String, Offset>() }
    val tileSizes = remember { mutableStateMapOf<String, IntSize>() }

    var lastSwappedTileId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(draggingTileId) {
        viewModel.setIsDragging(draggingTileId != null)
        if (draggingTileId == null) lastSwappedTileId = null
    }

    LaunchedEffect(draggingTileId, pointerPosition) {
        val draggingId = draggingTileId ?: run {
            lastSwappedTileId = null
            return@LaunchedEffect
        }
        
        val targetTile = tiles.filter { it.id != draggingId }.find { other ->
            val otherPos = tilePositions[other.id] ?: return@find false
            val otherSize = tileSizes[other.id] ?: return@find false
            val otherCenter = otherPos + Offset(otherSize.width / 2f, otherSize.height / 2f)
            val distSq = (pointerPosition.x - otherCenter.x) * (pointerPosition.x - otherCenter.x) +
                         (pointerPosition.y - otherCenter.y) * (pointerPosition.y - otherCenter.y)
            val swapRadius = otherSize.width * 0.45f
            distSq < swapRadius * swapRadius
        }

        val isFolderCandidate = targetTile != null && !targetTile.isWidget && !targetTile.isSpacer
        
        if (targetTile != null && isFolderCandidate) {
            hoveredTileId = targetTile.id
        } else {
            hoveredTileId = null
            if (targetTile != null && targetTile.id != lastSwappedTileId) {
                val fromIndex = tiles.indexOfFirst { it.id == draggingId }
                val toIndex = tiles.indexOfFirst { it.id == targetTile.id }
                if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                    lastSwappedTileId = targetTile.id
                    viewModel.moveTile(fromIndex, toIndex)
                }
            } else if (targetTile == null) {
                lastSwappedTileId = null
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
                    scrollState.dispatchRawDelta(-scrollAmount)
                } else if (distFromBottom < threshold) {
                    val scrollAmount = (threshold - distFromBottom) / 5f
                    scrollState.dispatchRawDelta(scrollAmount)
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
                            if (totalDrag.y > 150f && totalDrag.x.absoluteValue < totalDrag.y * 0.5f && scrollState.value <= 0) {
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
        val rows = remember(tiles, columns) {
            val result = mutableListOf<List<HomeTile>>()
            var currentRow = mutableListOf<HomeTile>()
            var currentSpan = 0
            for (tile in tiles) {
                val tileSpan = tile.spanX.coerceAtMost(columns)
                if (currentSpan + tileSpan > columns) {
                    if (currentRow.isNotEmpty()) {
                        result.add(currentRow)
                        currentRow = mutableListOf()
                        currentSpan = 0
                    }
                }
                currentRow.add(tile)
                currentSpan += tileSpan
            }
            if (currentRow.isNotEmpty()) {
                result.add(currentRow)
            }
            result
        }

        val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
        val sidePadding = 8.dp
        val spacing = 2.dp
        val availableWidth = (screenWidthDp - sidePadding - spacing * (columns - 1)).coerceAtLeast(100.dp)
        val unitSizeDp = availableWidth / columns

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { coords -> gridPosition = coords.positionInWindow() }
                .padding(
                    start = 4.dp, 
                    end = 4.dp, 
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 4.dp, 
                    bottom = 120.dp
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            rows.forEach { rowTiles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    rowTiles.forEach { tile ->
                        val isDragging = draggingTileId == tile.id
                        val wobbleTransition = rememberInfiniteTransition(label = "wobble")
                        val wobbleRotation by wobbleTransition.animateFloat(
                            initialValue = -1f, targetValue = 1f, 
                            animationSpec = infiniteRepeatable(animation = tween(150, easing = LinearEasing), repeatMode = RepeatMode.Reverse), 
                            label = "wobbleRotate"
                        )
                        val zIndexValue by animateFloatAsState(
                            targetValue = if (isDragging) 100f else 1f, 
                            label = "dragZIndex"
                        )
                        var itemPosition by remember { mutableStateOf(Offset.Zero) }
                        var itemSize by remember { mutableStateOf(IntSize.Zero) }

                        val spanX = tile.spanX.coerceAtMost(columns)
                        val spanY = tile.spanY
                        val tileWidthDp = unitSizeDp * spanX + spacing * (spanX - 1)
                        val tileHeightDp = unitSizeDp * spanY + spacing * (spanY - 1)

                        Box(
                            modifier = Modifier
                                .size(width = tileWidthDp, height = tileHeightDp)
                                .onGloballyPositioned { coords -> 
                                    itemPosition = coords.positionInRoot()
                                    itemSize = coords.size 
                                    tilePositions[tile.id] = coords.positionInWindow()
                                    tileSizes[tile.id] = coords.size
                                }
                                .zIndex(zIndexValue)
                                .graphicsLayer { 
                                    alpha = if (isDragging) 0f else 1f
                                    rotationZ = if (isEditMode && !isDragging) wobbleRotation else 0f 
                                }
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
                                    tileBlurRadius = tileBlurRadius, homeScreenBlurEnabled = homeScreenBlurEnabled, 
                                    accentColorOverlayEnabled = accentColorOverlayEnabled, solidTilesEnabled = solidTilesEnabled, squareTilesEnabled = squareTilesEnabled,
                                    phoneData = phoneData,
                                    wallpaperPainter = wallpaperPainter, wallpaperBitmap = wallpaperBitmap, 
                                    parallaxX = layoutParallaxX, parallaxY = layoutParallaxY, scrollOffset = scrollPx, screenWidthPx = screenWidthPx, screenHeightPx = screenHeightPx, 
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
                        tileBlurRadius = tileBlurRadius, homeScreenBlurEnabled = homeScreenBlurEnabled, 
                        accentColorOverlayEnabled = accentColorOverlayEnabled, solidTilesEnabled = solidTilesEnabled, squareTilesEnabled = squareTilesEnabled,
                        phoneData = phoneData,
                        wallpaperPainter = wallpaperPainter, wallpaperBitmap = wallpaperBitmap, 
                        parallaxX = layoutParallaxX, parallaxY = layoutParallaxY, scrollOffset = scrollPx, screenWidthPx = screenWidthPx, screenHeightPx = screenHeightPx, 
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
                var draggingSubTileId by remember { mutableStateOf<String?>(null) }
                var subTileTouchOffset by remember { mutableStateOf(Offset.Zero) }
                var hoveredSubTileId by remember { mutableStateOf<String?>(null) }
                var lastSwappedSubTileId by remember { mutableStateOf<String?>(null) }
                val subTilePositions = remember { mutableStateMapOf<String, Offset>() }
                val subTileSizes = remember { mutableStateMapOf<String, IntSize>() }
                var folderSurfaceBounds by remember { mutableStateOf(Rect.Zero) }

                Box(modifier = Modifier.fillMaxSize().zIndex(300f).pointerInput(Unit) { detectTapGestures { viewModel.openFolder(null) } }, contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight().padding(16.dp).pointerInput(Unit) { detectTapGestures { } }) {
                        FluentSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = (configuration.screenHeightDp * 0.8).dp)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                                .onGloballyPositioned { coords ->
                                    folderSurfaceBounds = androidx.compose.ui.geometry.Rect(coords.positionInRoot(), coords.size.toSize())
                                },
                            shape = RoundedCornerShape(24.dp), alpha = 0.85f, effect = FluentEffect.ACRYLIC, blurRadius = 120, tintColor = Color.Black.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { haptics.performHapticFeedback(HapticFeedbackType.LongPress); folderToRename = openFolder }) {
                                    Text(text = openFolder.label, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface); Spacer(modifier = Modifier.width(8.dp)); Icon(Icons.Rounded.Edit, contentDescription = "Rename", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Box(modifier = Modifier.weight(1f, fill = false)) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3), 
                                        verticalArrangement = Arrangement.spacedBy(24.dp), 
                                        horizontalArrangement = Arrangement.spacedBy(24.dp), 
                                        modifier = Modifier.fillMaxWidth(), 
                                        contentPadding = PaddingValues(bottom = 80.dp)
                                    ) {
                                        itemsIndexed(items = openFolder.subTiles, key = { _, sub -> sub.id }) { i, subTile ->
                                            var itemVisible by remember { mutableStateOf(false) }
                                            val isDragging = draggingSubTileId == subTile.id
                                            
                                            LaunchedEffect(Unit) { delay(10L * i); itemVisible = true }
                                            
                                            if (itemVisible) {
                                                val isHovered = hoveredSubTileId == subTile.id
                                                val pulseTransition = rememberInfiniteTransition(label = "subPulse")
                                                val pulseScale by pulseTransition.animateFloat(initialValue = 1.05f, targetValue = 1.15f, animationSpec = infiniteRepeatable(animation = tween(400, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "subPulse")
                                                val hoverScale by animateFloatAsState(targetValue = if (isHovered) pulseScale else 1f, label = "subHoverScale")
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .onGloballyPositioned { coords -> 
                                                            subTilePositions[subTile.id] = coords.positionInRoot()
                                                            subTileSizes[subTile.id] = coords.size 
                                                        }
                                                        .then(if (!isDragging) Modifier.animateItem() else Modifier)
                                                        .zIndex(if (isDragging) 100f else 1f)
                                                        .graphicsLayer { 
                                                            alpha = if (isDragging) 0f else 1f
                                                            val scale = hoverScale
                                                            scaleX = scale
                                                            scaleY = scale
                                                        }
                                                        .pointerInput(subTile.id) {
                                                            coroutineScope {
                                                                awaitEachGesture {
                                                                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                                                    var dragStarted = false
                                                                    var hasMovedSignificant = false
                                                                    val isHoldTriggered = BooleanArray(1) { false }
                                                                    val holdJob = launch { delay(750); if (draggingSubTileId == null) { haptics.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.explodeTile(subTile.id); isHoldTriggered[0] = true } }
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
                                                                                    if (!dragStarted && isMoving) { 
                                                                                        dragStarted = true
                                                                                        draggingSubTileId = subTile.id
                                                                                        subTileTouchOffset = down.position
                                                                                        lastSwappedSubTileId = null
                                                                                        holdJob.cancel()
                                                                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                                    }
                                                                                    if (dragStarted) {
                                                                                        val currentSize = subTileSizes[subTile.id] ?: IntSize.Zero
                                                                                        val draggedTopLeft = pointerPosition - subTileTouchOffset
                                                                                        val draggedCenter = draggedTopLeft + Offset(currentSize.width / 2f, currentSize.height / 2f)
                                                                                        
                                                                                        // Check if dragged far outside folder panel to pop out
                                                                                        val bufferPx = with(density) { 32.dp.toPx() }
                                                                                        val isOutsideFolder = !folderSurfaceBounds.inflate(bufferPx).contains(pointerPosition)
                                                                                        
                                                                                        if (isOutsideFolder) {
                                                                                            val targetTile = tiles.minByOrNull { other ->
                                                                                                val otherPos = tilePositions[other.id] ?: return@minByOrNull Float.MAX_VALUE
                                                                                                val otherSize = tileSizes[other.id] ?: return@minByOrNull Float.MAX_VALUE
                                                                                                val otherCenter = otherPos + Offset(otherSize.width / 2f, otherSize.height / 2f)
                                                                                                (pointerPosition.x - otherCenter.x) * (pointerPosition.x - otherCenter.x) + (pointerPosition.y - otherCenter.y) * (pointerPosition.y - otherCenter.y)
                                                                                            }
                                                                                            val toIndex = if (targetTile != null) tiles.indexOfFirst { it.id == targetTile.id } else tiles.size
                                                                                            viewModel.removeTileFromFolder(openFolder.id, subTile.id, toIndex = toIndex)
                                                                                            viewModel.openFolder(null)
                                                                                            viewModel.setEditMode(true)
                                                                                            viewModel.setIsDragging(true)
                                                                                            draggingTileId = subTile.id
                                                                                            draggingTileSize = currentSize
                                                                                            dragStartPointerOffset = subTileTouchOffset
                                                                                            draggingSubTileId = null
                                                                                            lastSwappedSubTileId = null
                                                                                            return@awaitEachGesture
                                                                                        }
                                                                                        
                                                                                        // Rearrange logic inside folder
                                                                                        val targetSubItem = openFolder.subTiles.find { other ->
                                                                                            if (other.id == subTile.id) return@find false
                                                                                            val otherPos = subTilePositions[other.id] ?: return@find false
                                                                                            val otherSize = subTileSizes[other.id] ?: return@find false
                                                                                            val otherCenter = otherPos + Offset(otherSize.width / 2f, otherSize.height / 2f)
                                                                                            val distSq = (draggedCenter.x - otherCenter.x) * (draggedCenter.x - otherCenter.x) +
                                                                                                         (draggedCenter.y - otherCenter.y) * (draggedCenter.y - otherCenter.y)
                                                                                            val swapRadius = otherSize.width * 0.45f
                                                                                            distSq < swapRadius * swapRadius
                                                                                        }
                                                                                        
                                                                                        if (targetSubItem != null) {
                                                                                            if (targetSubItem.id != lastSwappedSubTileId) {
                                                                                                val fromIdx = openFolder.subTiles.indexOfFirst { it.id == subTile.id }
                                                                                                val toIdx = openFolder.subTiles.indexOfFirst { it.id == targetSubItem.id }
                                                                                                if (fromIdx != -1 && toIdx != -1 && fromIdx != toIdx) {
                                                                                                    lastSwappedSubTileId = targetSubItem.id
                                                                                                    viewModel.moveTileInsideFolder(openFolder.id, fromIdx, toIdx)
                                                                                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                                                }
                                                                                            }
                                                                                        } else {
                                                                                            lastSwappedSubTileId = null
                                                                                        }
                                                                                        hoveredSubTileId = targetSubItem?.id
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                holdJob.cancel()
                                                                                if (!dragStarted && !isHoldTriggered[0] && !hasMovedSignificant) { 
                                                                                    if (subTile.packageName != null) { 
                                                                                        onAppClick(subTile.packageName)
                                                                                        viewModel.openFolder(null) 
                                                                                    } 
                                                                                }
                                                                                draggingSubTileId = null
                                                                                hoveredSubTileId = null
                                                                                lastSwappedSubTileId = null
                                                                                break
                                                                            }
                                                                        }
                                                                    } finally { holdJob.cancel() }
                                                                }
                                                            }
                                                        }
                                                ) {
                                                    HomeTileItem(
                                                        tile = subTile, tileOpacity = tileOpacity, recentNotifications = emptyList(), 
                                                        allNotifications = allNotifications,
                                                        currentMedia = null, calendarEvents = emptyList(), contacts = emptyList(), 
                                                        weatherAppPackage = null, recentPhotos = recentPhotos, 
                                                        tilePictureEnabled = tilePictureEnabled, tileBlurRadius = tileBlurRadius, 
                                                        homeScreenBlurEnabled = homeScreenBlurEnabled, wallpaperPainter = wallpaperPainter, wallpaperBitmap = wallpaperBitmap, 
                                                        scrollOffset = 0f, screenWidthPx = screenWidthPx, screenHeightPx = screenHeightPx, 
                                                        noiseBitmap = noiseBitmap
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Box(modifier = Modifier.matchParentSize().padding(24.dp), contentAlignment = Alignment.BottomEnd) {
                                FloatingActionButton(onClick = onAddAppsClick, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, shape = CircleShape, modifier = Modifier.size(56.dp).shadow(8.dp, CircleShape)) { Icon(Icons.Rounded.Add, contentDescription = "Add Apps") }
                            }

                            // Floating Item Overlay for folder rearranging
                            draggingSubTileId?.let { id ->
                                val tile = openFolder.subTiles.find { it.id == id }
                                if (tile != null) {
                                    val size = subTileSizes[id] ?: IntSize.Zero
                                    val touchOffset = subTileTouchOffset
                                    val originX = if (size.width > 0) (touchOffset.x / size.width.toFloat()).coerceIn(0f, 1f) else 0.5f
                                    val originY = if (size.height > 0) (touchOffset.y / size.height.toFloat()).coerceIn(0f, 1f) else 0.5f

                                    Box(modifier = Modifier
                                        .size(with(density) { size.width.toDp() }, with(density) { size.height.toDp() })
                                        .graphicsLayer {
                                            val localX = pointerPosition.x - folderSurfaceBounds.left - touchOffset.x
                                            val localY = pointerPosition.y - folderSurfaceBounds.top - touchOffset.y
                                            translationX = localX
                                            translationY = localY
                                            scaleX = 1.15f
                                            scaleY = 1.15f
                                            transformOrigin = TransformOrigin(originX, originY)
                                            shadowElevation = 16.dp.toPx()
                                        }
                                        .zIndex(1000f)
                                    ) {
                                        HomeTileItem(
                                            tile = tile, tileOpacity = tileOpacity, recentNotifications = emptyList(), 
                                            allNotifications = allNotifications,
                                            currentMedia = null, calendarEvents = emptyList(), contacts = emptyList(), 
                                            weatherAppPackage = null, recentPhotos = recentPhotos, 
                                            tilePictureEnabled = tilePictureEnabled, tileBlurRadius = tileBlurRadius, 
                                            homeScreenBlurEnabled = homeScreenBlurEnabled, wallpaperPainter = wallpaperPainter, wallpaperBitmap = wallpaperBitmap, 
                                            scrollOffset = 0f, screenWidthPx = screenWidthPx, screenHeightPx = screenHeightPx, 
                                            noiseBitmap = noiseBitmap
                                        )
                                    }
                                }
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
    accentColorOverlayEnabled: Boolean = false, solidTilesEnabled: Boolean = false, squareTilesEnabled: Boolean = false,
    phoneData: PhoneTileData = PhoneTileData(),
    wallpaperPainter: Painter? = null, wallpaperBitmap: ImageBitmap? = null, parallaxX: Float = 0f, parallaxY: Float = 0f, scrollOffset: Float = 0f, screenWidthPx: Float = 0f, screenHeightPx: Float = 0f, 
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

    val cornerRadius = if (squareTilesEnabled) 0.dp else 12.dp
    val tileShape = RoundedCornerShape(cornerRadius)
    val accentColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.onGloballyPositioned { coords -> tilePosition = coords.positionInWindow() }) {
        if (tile.isSpacer) {
            if (isEditMode) Box(modifier = Modifier.aspectRatio(ratio).clip(tileShape).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), tileShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)))
            else Spacer(modifier = Modifier.aspectRatio(ratio))
        } else if (tile.isWidget && tile.widgetId != null) {
            Box {
                Box(modifier = Modifier.aspectRatio(ratio).scale(if (isHovered) 1.1f else 1.0f).clip(tileShape).then(if (accentColorOverlayEnabled) Modifier.border(1.dp, accentColor.copy(alpha = 0.85f), tileShape) else Modifier).then(if (isHovered) Modifier.border(2.dp, accentColor, tileShape) else Modifier)) {
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
                    .clip(tileShape)
                    .then(if (accentColorOverlayEnabled) Modifier.border(1.dp, accentColor.copy(alpha = 0.85f), tileShape) else Modifier)
                    .then(if (isHovered) Modifier.border(2.dp, accentColor, tileShape) else Modifier)
            ) {
                // Layer 1: Background Layer for Fluent Effects (Parallax + Blur) / Tile Picture Mode
                if (!solidTilesEnabled && (wallpaperBitmap != null || wallpaperPainter != null) && isStandardTile && (homeScreenBlurEnabled || tilePictureEnabled)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .then(if (homeScreenBlurEnabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    Modifier.graphicsLayer { 
                                        val radiusMultiplier = 1.1f + (tileBlurRadius / 600f)
                                        val radius = tileBlurRadius * radiusMultiplier
                                        
                                        val blur = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                                        val matrix = ColorMatrix().apply { 
                                            setSaturation(1.2f + (tileBlurRadius / 1000f)) 
                                            val contrast = 1.05f + (tileBlurRadius / 1500f)
                                            val translate = (-0.5f * contrast + 0.5f) * 255f
                                            postConcat(ColorMatrix(floatArrayOf(
                                                contrast, 0f, 0f, 0f, translate,
                                                0f, contrast, 0f, 0f, translate,
                                                0f, 0f, contrast, 0f, translate,
                                                0f, 0f, 0f, 1f, 0f
                                            )))
                                        }
                                        val colorFilter = RenderEffect.createColorFilterEffect(
                                            ColorMatrixColorFilter(matrix)
                                        )
                                        renderEffect = RenderEffect.createChainEffect(blur, colorFilter).asComposeRenderEffect()
                                    }
                                } else {
                                    Modifier.blur((tileBlurRadius / 2.5f).dp)
                                }
                            } else Modifier)
                            .graphicsLayer {
                                if (tilePictureEnabled) {
                                    translationX = parallaxX
                                    translationY = parallaxY
                                }
                            }
                            .drawBehind {
                                val bitmap = wallpaperBitmap
                                if (bitmap != null) {
                                    val bw = bitmap.width.toFloat()
                                    val bh = bitmap.height.toFloat()
                                    if (bw <= 0f || bh <= 0f) return@drawBehind

                                    val sw = screenWidthPx
                                    val sh = screenHeightPx
                                    val isPicMode = tilePictureEnabled
                                    val s = if (isPicMode) 1.0f else 3.5f

                                    val maxParallax = if (isPicMode) 1200f else 0f
                                    val topBuffer = if (isPicMode) 600f else 0f
                                    val viewportH = sh + maxParallax

                                    val scale = maxOf(sw / bw, viewportH / bh) * s
                                    val targetW = bw * scale
                                    val targetH = bh * scale

                                    val cropX = (targetW - sw * s) / 2f
                                    val cropY = (targetH - viewportH * s) / 2f

                                    val offsetX = if (isPicMode) 0f else (sw * (s - 1f)) / 2f
                                    val offsetY = if (isPicMode) 0f else (sh * (s - 1f)) / 2f

                                    val finalX = (-tilePosition.x - cropX - offsetX).roundToInt()
                                    val finalY = (-tilePosition.y - cropY - topBuffer - offsetY).roundToInt()
                                    val finalW = targetW.roundToInt()
                                    val finalH = targetH.roundToInt()

                                    drawImage(
                                        image = bitmap,
                                        dstOffset = IntOffset(finalX, finalY),
                                        dstSize = IntSize(finalW, finalH)
                                    )
                                } else if (wallpaperPainter != null) {
                                    val painter = wallpaperPainter
                                    val isPicMode = tilePictureEnabled
                                    val s = if (isPicMode) 1.0f else 3.5f
                                    val sw = screenWidthPx
                                    val sh = screenHeightPx
                                    
                                    val iw = painter.intrinsicSize.width
                                    val ih = painter.intrinsicSize.height
                                    val maxParallax = if (isPicMode) 1200f else 0f
                                    val topBuffer = if (isPicMode) 600f else 0f
                                    val viewportH = sh + maxParallax

                                    val scale = if (iw > 0f && ih > 0f && !iw.isNaN() && !ih.isNaN()) {
                                        maxOf(sw / iw, viewportH / ih)
                                    } else 1.0f

                                    val targetW = (if (iw > 0f && !iw.isNaN()) iw * scale else sw) * s
                                    val targetH = (if (ih > 0f && !ih.isNaN()) ih * scale else viewportH) * s

                                    val cropX = (targetW - sw * s) / 2f
                                    val cropY = (targetH - viewportH * s) / 2f

                                    val offsetX = if (isPicMode) 0f else (sw * (s - 1f)) / 2f
                                    val offsetY = if (isPicMode) 0f else (sh * (s - 1f)) / 2f
                                    
                                    drawIntoCanvas { canvas ->
                                        canvas.save()
                                        canvas.translate(-tilePosition.x - cropX - offsetX, -tilePosition.y - cropY - topBuffer - offsetY)
                                        with(painter) {
                                            draw(size = androidx.compose.ui.geometry.Size(targetW, targetH))
                                        }
                                        canvas.restore()
                                    }
                                }
                            }
                    )
                }

                // Layer 2: Shared Acrylic Surface / Solid Tile Base
                val currentOpacity = if (solidTilesEnabled) 1.0f else (if (tilePictureEnabled) tileOpacity * 0.4f else tileOpacity)
                val solidBgColor = if (accentColorOverlayEnabled) accentColor else (if (isDark) Color(0xFF1E1E1E) else Color(0xFFE8E8E8))

                if (solidTilesEnabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(solidBgColor)
                    )
                } else {
                    FluentSurface(
                        modifier = Modifier.fillMaxSize(),
                        alpha = currentOpacity,
                        effect = if (isStandardTile && homeScreenBlurEnabled) FluentEffect.ACRYLIC else FluentEffect.NONE,
                        blurRadius = tileBlurRadius.toInt(),
                        tintColor = if (isDark) Color.Black.copy(alpha = 0.3f * currentOpacity) else Color(0xFFB0B0B0).copy(alpha = 0.25f * currentOpacity), 
                        luminosityAlpha = if (isDark) 0.15f * currentOpacity else 0.25f * currentOpacity,
                        borderAlpha = 0f
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }

                // Layer 3: Accent Color Overlay (if enabled)
                if (accentColorOverlayEnabled && !solidTilesEnabled) {
                    val accentOverlayAlpha = if (tilePictureEnabled) 0.35f else 0.65f
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(accentColor.copy(alpha = accentOverlayAlpha))
                    )
                }

                // Layer 4: Subtle Gloss Highlight Overlay (if accent overlay or solid tiles enabled)
                if (accentColorOverlayEnabled || solidTilesEnabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                    )
                }

                // Layer 5: Tile Content (Icon + Label)
                Box(modifier = Modifier.fillMaxSize()) { 
                    TileDispatcher(tile, weatherData, recentNotifications, allNotifications, currentMedia, calendarEvents, contacts, weatherAppPackage, recentPhotos, icon, onPlayPause, onSkipNext, onSkipPrevious, topNews, currentOpacity, tilePictureEnabled, phoneData = phoneData) 
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
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(if (tile.size == TileSize.SMALL) 2.dp else 6.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        if (tile.size == TileSize.SMALL) {
                            Box(
                                modifier = Modifier
                                    .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tile.notificationCount > 99) "99+" else tile.notificationCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (tile.size != TileSize.MEDIUM) {
                            Box(
                                modifier = Modifier
                                    .sizeIn(minWidth = 22.dp, minHeight = 22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tile.notificationCount > 99) "99+" else tile.notificationCount.toString(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
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
    tileOpacity: Float, tilePictureEnabled: Boolean = false,
    phoneData: PhoneTileData = PhoneTileData()
) {
    val context = LocalContext.current
    val isPhoneApp = tile.packageName?.lowercase()?.let { it.contains("dialer") || it.contains("phone") } == true || tile.specialType == "phone"
    when {
        tile.isFolder -> FolderTileContent(tile, allNotifications, tilePictureEnabled)
        isPhoneApp -> PhoneLiveTile(tile = tile, phoneData = phoneData, recentNotifications = recentNotifications, defaultIconDrawable = icon, tileOpacity = tileOpacity)
        tile.specialType == HomeTile.TYPE_CLOCK_WEATHER -> FlippingTileContainer(isLive = true, front = { ClockWeatherTileContent(tile = tile, weatherData = weatherData, onWeatherClick = { if (weatherAppPackage == "web") context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))) else context.packageManager.getLaunchIntentForPackage(weatherAppPackage ?: "")?.let { context.startActivity(it) } }) }, back = { WeatherForecastBack(weatherData = weatherData) })
        tile.specialType == HomeTile.TYPE_CLOCK || tile.packageName?.lowercase()?.contains("clock") == true -> ClockTileContent(tile)
        tile.specialType == HomeTile.TYPE_WEATHER || tile.packageName?.lowercase()?.contains("weather") == true -> WeatherTileContent(tile = tile, weatherData = weatherData, onWeatherClick = { if (weatherAppPackage == "web") context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))) else context.packageManager.getLaunchIntentForPackage(weatherAppPackage ?: "")?.let { context.startActivity(it) } })
        tile.specialType == HomeTile.TYPE_PHOTOS || tile.packageName?.lowercase()?.contains("photos") == true || tile.packageName?.lowercase()?.contains("gallery") == true -> PhotoLiveTile(tile, recentPhotos, tileOpacity)
        tile.specialType == HomeTile.TYPE_MUSIC || (isMusicApp(tile.packageName) && currentMedia?.packageName == tile.packageName) -> { val isPlaying = currentMedia?.isPlaying == true; FlippingTileContainer(isLive = currentMedia?.title != null, forceBack = isPlaying, front = { StandardTileContent(tile, icon) }, back = { MusicLiveTile(tile = tile, media = currentMedia, onPlayPause = onPlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious, tileOpacity = tileOpacity) }) }
        tile.packageName?.lowercase()?.contains("calendar") == true || tile.specialType == "calendar" -> FlippingTileContainer(isLive = true, front = { DateBackSide() }, back = { CalendarTileBack(calendarEvents) })
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
        isCommunicationApp(tile.packageName) && (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) -> { val pkg = tile.packageName ?: ""; val isMusic = isMusicApp(pkg) && currentMedia?.packageName == pkg; val backContent: @Composable () -> Unit = { when { isMusic -> MusicLiveTile(tile = tile, media = currentMedia, onPlayPause = onPlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious, tileOpacity = tileOpacity); pkg.contains("dialer", true) || pkg.contains("phone", true) -> PhoneLiveTile(tile, phoneData, recentNotifications, icon, tileOpacity); pkg.contains("gmail", true) || pkg.contains("mail", true) || pkg.contains("outlook", true) -> GmailLiveTile(tile, recentNotifications); else -> MessagesLiveTile(tile, recentNotifications) } }; val isPlaying = isMusic && currentMedia?.isPlaying == true; FlippingTileContainer(isLive = isPlaying || recentNotifications.isNotEmpty(), forceBack = isPlaying, front = { StandardTileContent(tile, icon) }, back = { backContent() }) }
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
