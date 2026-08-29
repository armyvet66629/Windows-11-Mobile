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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.items
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
    val currentTiles by rememberUpdatedState(tiles)
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()
    val explodedTileId by viewModel.explodedTileId.collectAsStateWithLifecycle()
    val weatherData by viewModel.weather.collectAsStateWithLifecycle()
    val allNotifications by viewModel.recentNotifications.collectAsStateWithLifecycle()
    val currentMedia by viewModel.currentMedia.collectAsStateWithLifecycle()
    val weatherAppPackage by viewModel.weatherAppPackage.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val openFolderId by viewModel.openFolderId.collectAsStateWithLifecycle()

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val columns = when {
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT -> 4
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM -> 6
        else -> 8
    }

    val gridState = rememberLazyGridState()
    val context = LocalContext.current
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
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<HomeTile?>(null) }
    var folderSourceCenter by remember { mutableStateOf(Offset.Zero) }

    // Auto-scroll logic to follow dragging tile
    LaunchedEffect(draggingTileId, pointerPosition) {
        if (draggingTileId != null) {
            val threshold = screenHeightPx * 0.15f // 15% from top/bottom
            
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
                delay(16) // ~60fps scroll check
            }
        }
    }

    val shortcuts = remember(selectedTileForMenu) {
        selectedTileForMenu?.packageName?.let { viewModel.getShortcuts(it) } ?: emptyList()
    }

    val widgetPickerLauncher = rememberLauncherForActivityResult(
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        pointerPosition = event.changes.first().position
                    }
                }
            }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { 
                    if (isEditMode) viewModel.setEditMode(false) 
                    backgroundMenuExpanded = false
                },
                onLongClick = { backgroundMenuExpanded = true }
            )
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isEditMode) {
                    detectTapGestures(
                        onTap = {
                            if (isEditMode) viewModel.setEditMode(false)
                            backgroundMenuExpanded = false
                        },
                        onLongPress = {
                            backgroundMenuExpanded = true
                        }
                    )
                },
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(
                items = tiles,
                key = { _, tile -> tile.id },
                span = { _, tile -> GridItemSpan(tile.spanX.coerceAtMost(columns)) }
            ) { index, tile ->
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
                
                val scale by animateFloatAsState(
                    targetValue = if (isDragging) 1.15f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "dragScale"
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
                        .scale(scale)
                        .pointerInput(tile.id, isEditMode) {
                            if (isEditMode) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        draggingTileId = tile.id
                                        draggingTileSize = itemSize
                                        dragStartPointerOffset = offset
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        
                                        val layoutInfo = gridState.layoutInfo
                                        val currentCenter = pointerPosition + Offset(
                                            itemSize.width / 2f - dragStartPointerOffset.x,
                                            itemSize.height / 2f - dragStartPointerOffset.y
                                        )
                                        
                                        val targetItem = layoutInfo.visibleItemsInfo.filter { it.key != tile.id }.find { other ->
                                            val otherCenterX = other.offset.x + other.size.width / 2f
                                            val otherCenterY = other.offset.y + other.size.height / 2f
                                            val hitBoxWidth = other.size.width * 0.48f
                                            val hitBoxHeight = other.size.height * 0.48f
                                            currentCenter.x > otherCenterX - hitBoxWidth &&
                                            currentCenter.x < otherCenterX + hitBoxWidth &&
                                            currentCenter.y > otherCenterY - hitBoxHeight &&
                                            currentCenter.y < otherCenterY + hitBoxHeight
                                        }
                                        
                                        var isFolderDropZone = false
                                        targetItem?.let { target ->
                                            val targetCenterX = target.offset.x + target.size.width / 2f
                                            val targetCenterY = target.offset.y + target.size.height / 2f
                                            val folderZoneWidth = target.size.width * 0.35f
                                            val folderZoneHeight = target.size.height * 0.35f
                                            
                                            if (currentCenter.x > targetCenterX - folderZoneWidth &&
                                                currentCenter.x < targetCenterX + folderZoneWidth &&
                                                currentCenter.y > targetCenterY - folderZoneHeight &&
                                                currentCenter.y < targetCenterY + folderZoneHeight) {
                                                isFolderDropZone = true
                                            }
                                        }

                                        val targetTile = targetItem?.let { target -> currentTiles.find { it.id == target.key } }
                                        if (isFolderDropZone && (targetTile?.isFolder == true || (targetTile != null && targetTile.specialType == null && !targetTile.isWidget && !targetTile.isSpacer))) {
                                            hoveredTileId = targetTile.id
                                        } else {
                                            hoveredTileId = null
                                        }

                                        if (targetItem != null && !isFolderDropZone) {
                                            val fromIndex = currentTiles.indexOfFirst { it.id == tile.id }
                                            val toIndex = targetItem.index
                                            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                                                viewModel.swapTiles(fromIndex, toIndex)
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        val targetId = hoveredTileId
                                        if (targetId != null) {
                                            val fromIndex = currentTiles.indexOfFirst { it.id == tile.id }
                                            val toIndex = currentTiles.indexOfFirst { it.id == targetId }
                                            if (fromIndex != -1 && toIndex != -1) {
                                                viewModel.moveTile(fromIndex, toIndex)
                                            }
                                        }
                                        draggingTileId = null
                                        hoveredTileId = null
                                    },
                                    onDragCancel = { 
                                        draggingTileId = null
                                        hoveredTileId = null
                                    }
                                )
                            } else {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        draggingTileId = tile.id
                                        draggingTileSize = itemSize
                                        dragStartPointerOffset = offset
                                        viewModel.explodeTile(tile.id)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        
                                        // Dismiss menu and start reordering if we were holding
                                        viewModel.explodeTile(null)
                                        if (!isEditMode) viewModel.setEditMode(true)
                                        
                                        val layoutInfo = gridState.layoutInfo
                                        val currentCenter = pointerPosition + Offset(
                                            itemSize.width / 2f - dragStartPointerOffset.x,
                                            itemSize.height / 2f - dragStartPointerOffset.y
                                        )
                                        
                                        val targetItem = layoutInfo.visibleItemsInfo.filter { it.key != tile.id }.find { other ->
                                            val otherCenterX = other.offset.x + other.size.width / 2f
                                            val otherCenterY = other.offset.y + other.size.height / 2f
                                            val hitBoxWidth = other.size.width * 0.48f
                                            val hitBoxHeight = other.size.height * 0.48f
                                            currentCenter.x > otherCenterX - hitBoxWidth &&
                                            currentCenter.x < otherCenterX + hitBoxWidth &&
                                            currentCenter.y > otherCenterY - hitBoxHeight &&
                                            currentCenter.y < otherCenterY + hitBoxHeight
                                        }
                                        
                                        var isFolderDropZone = false
                                        targetItem?.let { target ->
                                            val targetCenterX = target.offset.x + target.size.width / 2f
                                            val targetCenterY = target.offset.y + target.size.height / 2f
                                            val folderZoneWidth = target.size.width * 0.35f
                                            val folderZoneHeight = target.size.height * 0.35f
                                            
                                            if (currentCenter.x > targetCenterX - folderZoneWidth &&
                                                currentCenter.x < targetCenterX + folderZoneWidth &&
                                                currentCenter.y > targetCenterY - folderZoneHeight &&
                                                currentCenter.y < targetCenterY + folderZoneHeight) {
                                                isFolderDropZone = true
                                            }
                                        }

                                        val targetTile = targetItem?.let { target -> currentTiles.find { it.id == target.key } }
                                        if (isFolderDropZone && (targetTile?.isFolder == true || (targetTile != null && targetTile.specialType == null && !targetTile.isWidget && !targetTile.isSpacer))) {
                                            hoveredTileId = targetTile.id
                                        } else {
                                            hoveredTileId = null
                                        }

                                        if (targetItem != null && !isFolderDropZone) {
                                            val fromIndex = currentTiles.indexOfFirst { it.id == tile.id }
                                            val toIndex = targetItem.index
                                            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                                                viewModel.swapTiles(fromIndex, toIndex)
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        val targetId = hoveredTileId
                                        if (targetId != null) {
                                            val fromIndex = currentTiles.indexOfFirst { it.id == tile.id }
                                            val toIndex = currentTiles.indexOfFirst { it.id == targetId }
                                            if (fromIndex != -1 && toIndex != -1) {
                                                viewModel.moveTile(fromIndex, toIndex)
                                            }
                                        }
                                        draggingTileId = null
                                        hoveredTileId = null
                                    },
                                    onDragCancel = { 
                                        draggingTileId = null
                                        hoveredTileId = null
                                    }
                                )
                            }
                        }
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (draggingTileId == null && explodedTileId == null) {
                                    if (tile.isFolder) {
                                        folderSourceCenter = itemPosition + Offset(itemSize.width / 2f, itemSize.height / 2f)
                                        viewModel.openFolder(tile.id)
                                    } else if (tile.packageName != null) {
                                        onAppClick(tile.packageName)
                                    } else if (tile.specialType == HomeTile.TYPE_CLOCK || tile.specialType == HomeTile.TYPE_CLOCK_WEATHER) {
                                        try { context.startActivity(Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)) } catch (e: Exception) {}
                                    }
                                }
                            }
                        )
                ) {
                    HomeTileItem(
                        tile = tile,
                        isEditMode = isEditMode,
                        isHovered = hoveredTileId == tile.id,
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
                        widgetHost = viewModel.appWidgetHost
                    )
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
                        ActionButton(text = "Add Widget", icon = Icons.Rounded.Widgets, onClick = { backgroundMenuExpanded = false; val appWidgetId = viewModel.allocateWidgetId(); val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) }; widgetPickerLauncher.launch(intent) })
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
                    FluentSurface(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight()
                            .padding(16.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .pointerInput(Unit) { detectTapGestures { } }, 
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
                            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                                itemsIndexed(openFolder.subTiles) { i, subTile ->
                                    var itemVisible by remember { mutableStateOf(false) }
                                    var subDragOffset by remember { mutableStateOf(Offset.Zero) }
                                    var isSubDragging by remember { mutableStateOf(false) }
                                    
                                    LaunchedEffect(Unit) { delay(10L * i); itemVisible = true }
                                    
                                    AnimatedVisibility(
                                        visible = itemVisible, 
                                        enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.1f, animationSpec = tween(150), transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center) + expandIn(expandFrom = Alignment.Center, animationSpec = tween(150))
                                    ) {
                                        HomeTileItem(
                                            tile = subTile, 
                                            tileOpacity = tileOpacity, 
                                            modifier = Modifier
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
                                                    detectDragGesturesAfterLongPress(
                                                        onDragStart = { isSubDragging = true },
                                                        onDragEnd = {
                                                            if (subDragOffset.getDistance() > 200f) {
                                                                viewModel.removeTileFromFolder(openFolder.id, subTile.id, toIndex = 0)
                                                                viewModel.openFolder(null)
                                                            }
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
                                                        }
                                                    )
                                                }
                                        )
                                    }
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
                if (isEditMode) {
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).size(24.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)).clickable { onResize() }, contentAlignment = Alignment.Center) { Icon(imageVector = FluentIcons.Open, contentDescription = "Resize", modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = 90f), tint = MaterialTheme.colorScheme.onPrimary) }
                    Box(modifier = Modifier.fillMaxSize().border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)))
                }
            }
        } else {
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
                        tile.specialType == HomeTile.TYPE_CLOCK_WEATHER -> ClockWeatherTileContent(tile = tile, weatherData = weatherData, onWeatherClick = { if (weatherAppPackage == "web") context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))) else context.packageManager.getLaunchIntentForPackage(weatherAppPackage ?: "")?.let { context.startActivity(it) } })
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
                                back = { YouTubeLiveTile(tile, currentMedia, recentNotifications) }
                            )
                        }
                        isCommunicationApp(tile.packageName) && (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) -> { val pkg = tile.packageName ?: ""; val isMusic = isMusicApp(pkg) && currentMedia?.packageName == pkg; val backContent: @Composable () -> Unit = { when { isMusic -> MusicLiveTile(tile = tile, media = currentMedia, onPlayPause = onPlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious); pkg.contains("dialer", true) || pkg.contains("phone", true) -> PhoneLiveTile(tile, recentNotifications); pkg.contains("gmail", true) || pkg.contains("mail", true) || pkg.contains("outlook", true) -> GmailLiveTile(tile, recentNotifications); else -> MessagesLiveTile(tile, recentNotifications) } }; val isPlaying = isMusic && currentMedia?.isPlaying == true; FlippingTileContainer(isLive = isPlaying || recentNotifications.isNotEmpty(), forceBack = isPlaying, front = { StandardTileContent(tile, icon) }, back = { backContent() }) }
                        else -> { if (tile.size == TileSize.LARGE) FlippingTileContainer(isLive = true, front = { StandardTileContent(tile, icon) }, back = { DateBackSide() }) else StandardTileContent(tile, icon) }
                    }
                    if (tile.notificationCount > 0 && !isEditMode) {
                        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 8.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) { Text(text = if (tile.notificationCount > 99) "99+" else tile.notificationCount.toString(), style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.W300, color = MaterialTheme.colorScheme.onSurface)); Spacer(modifier = Modifier.width(6.dp)); val badgeIcon = when { tile.packageName?.contains("messaging") == true -> Icons.AutoMirrored.Rounded.Chat; tile.packageName?.contains("gmail") == true || tile.packageName?.contains("mail") == true -> Icons.Rounded.Email; tile.packageName?.contains("dialer") == true || tile.packageName?.contains("phone") == true -> Icons.Rounded.Phone; else -> Icons.Rounded.Notifications }; Icon(imageVector = badgeIcon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface) }
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
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, contentColor: Color = MaterialTheme.colorScheme.onSurface) {
    TextButton(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.textButtonColors(contentColor = contentColor)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StandardTileContent(tile: HomeTile, icon: android.graphics.drawable.Drawable?) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Alignment.Start else Alignment.CenterHorizontally, verticalArrangement = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Arrangement.Top else Arrangement.Center) {
        Box(modifier = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Modifier.size(32.dp) else Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (icon != null) AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(when (tile.size) { TileSize.SMALL -> 28.dp; TileSize.MEDIUM -> 64.dp; TileSize.WIDE -> 40.dp; TileSize.LARGE -> 72.dp }))
            else FluentIcon(imageVector = when (tile.label.lowercase()) { "settings" -> FluentIcons.Settings; "calendar" -> FluentIcons.Calendar; "people" -> Icons.Rounded.Person; "messaging" -> FluentIcons.Message; "phone" -> Icons.Rounded.Phone; "camera" -> Icons.Rounded.CameraAlt; "mail", "gmail" -> FluentIcons.Mail; "maps" -> Icons.Rounded.Map; "photos" -> FluentIcons.Photos; else -> FluentIcons.Apps }, contentDescription = null, size = when (tile.size) { TileSize.SMALL -> 28.dp; TileSize.MEDIUM -> 64.dp; TileSize.WIDE -> 40.dp; TileSize.LARGE -> 72.dp }, gradient = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
        }
        if (tile.size != TileSize.SMALL) {
            if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
                Spacer(modifier = Modifier.height(8.dp)); Text(text = if (tile.notificationCount > 0) "${tile.label} (${tile.notificationCount})" else tile.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                if (tile.notificationSummary != null) { Spacer(modifier = Modifier.height(4.dp)); Text(text = tile.notificationSummary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = if (tile.size == TileSize.LARGE) 6 else 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface) }
                else { Spacer(modifier = Modifier.height(4.dp)); Text(text = "No new notifications", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) }
            } else { Text(text = if (tile.notificationCount > 0) "${tile.label} (${tile.notificationCount})" else tile.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}

@Composable
fun WidgetHostItem(widgetId: Int, sharedHost: android.appwidget.AppWidgetHost? = null) {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = sharedHost ?: remember { android.appwidget.AppWidgetHost(context, 1024) }
    val appWidgetInfo = remember(widgetId) { try { appWidgetManager.getAppWidgetInfo(widgetId) } catch (e: Exception) { null } }
    if (appWidgetInfo != null) { key(widgetId) { AndroidView(modifier = Modifier.fillMaxSize().padding(8.dp), factory = { ctx -> appWidgetHost.createView(ctx, widgetId, appWidgetInfo).apply { setAppWidget(widgetId, appWidgetInfo) } }, update = { view -> }) } }
    else { Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error); Text("Widget not found", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) } } }
}

@Composable
fun RenameFolderDialog(currentName: String, onDismiss: () -> Unit, onRename: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rename Folder", fontWeight = FontWeight.Bold) }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }, confirmButton = { Button(onClick = { onRename(text) }) { Text("Rename") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }, shape = RoundedCornerShape(24.dp))
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun HomeScreenPreview() { val context = LocalContext.current; val application = context.applicationContext as android.app.Application; val settingsRepository = remember { com.example.windows11mobile.data.RealSettingsRepository(context) }; val viewModel = remember { HomeViewModel(settingsRepository, application) }; com.example.windows11mobile.ui.theme.Windows11MobileTheme { HomeScreen(viewModel = viewModel, onAppClick = {}) } }
