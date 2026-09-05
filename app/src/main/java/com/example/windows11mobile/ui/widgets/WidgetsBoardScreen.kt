package com.example.windows11mobile.ui.widgets

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.theme.FluentIcons
import com.example.windows11mobile.ui.components.FluentIcon
import com.example.windows11mobile.ui.news.NewsFeedViewModel
import com.example.windows11mobile.ui.news.NewsCard
import com.example.windows11mobile.ui.news.NewsHeader
import com.example.windows11mobile.ui.news.CustomizeFeedDialog
import com.example.windows11mobile.ui.home.WidgetHostItem
import com.example.windows11mobile.data.TileSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.windows11mobile.data.MediaData
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WidgetsBoardScreen(
    newsViewModel: NewsFeedViewModel,
    appWidgetHost: android.appwidget.AppWidgetHost,
    showTaskbar: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val articles by newsViewModel.articles.collectAsStateWithLifecycle()
    val boardWidgets by newsViewModel.boardWidgets.collectAsStateWithLifecycle()
    val tileOpacity by newsViewModel.tileOpacity.collectAsStateWithLifecycle()
    val isLoading by newsViewModel.isLoading.collectAsStateWithLifecycle()
    val preferredCategories by newsViewModel.preferredCategories.collectAsStateWithLifecycle()
    val rssFeeds by newsViewModel.rssFeeds.collectAsStateWithLifecycle()
    val tasks by newsViewModel.tasks.collectAsStateWithLifecycle()
    val calendarEvents by newsViewModel.calendarEvents.collectAsStateWithLifecycle()
    val availableWidgets by newsViewModel.availableWidgets.collectAsStateWithLifecycle()
    val currentMedia by newsViewModel.currentMedia.collectAsStateWithLifecycle()
    val recentPhotos by newsViewModel.recentPhotos.collectAsStateWithLifecycle()
    
    val haptics = LocalHapticFeedback.current
    
    var isEditMode by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 for Widgets, 1 for News
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCustomizeDialog by remember { mutableStateOf(false) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var pendingWidgetInfo by remember { mutableStateOf<android.appwidget.AppWidgetProviderInfo?>(null) }
    var pendingWidgetId by remember { mutableIntStateOf(-1) }

    // Reordering State
    var draggingId by remember { mutableStateOf<String?>(null) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    val widgetConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: pendingWidgetId
            if (appWidgetId != -1) {
                val appWidgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)
                if (appWidgetInfo != null) {
                    newsViewModel.addBoardWidget(appWidgetId, appWidgetInfo.loadLabel(context.packageManager))
                }
            }
        } else if (pendingWidgetId != -1) {
            appWidgetHost.deleteAppWidgetId(pendingWidgetId)
        }
        pendingWidgetId = -1
        pendingWidgetInfo = null
    }

    val bindWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
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
                    newsViewModel.addBoardWidget(appWidgetId, info.loadLabel(context.packageManager))
                    pendingWidgetId = -1
                    pendingWidgetInfo = null
                }
            }
        } else if (pendingWidgetId != -1) {
            appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = -1
            pendingWidgetInfo = null
        }
    }

    if (showWidgetPicker) {
        WidgetPickerDialog(
            availableWidgets = availableWidgets,
            onDismiss = { showWidgetPicker = false },
            onWidgetSelected = { info ->
                showWidgetPicker = false
                val appWidgetId = appWidgetHost.allocateAppWidgetId()
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
                        newsViewModel.addBoardWidget(appWidgetId, info.loadLabel(context.packageManager))
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

    var widgetToDelete by remember { mutableStateOf<String?>(null) }

    if (widgetToDelete != null) {
        AlertDialog(
            onDismissRequest = { widgetToDelete = null },
            title = { Text("Remove Widget") },
            text = { Text("Are you sure you want to remove this widget from your board?") },
            confirmButton = {
                TextButton(onClick = {
                    newsViewModel.removeBoardWidget(widgetToDelete!!)
                    widgetToDelete = null
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { widgetToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCustomizeDialog) {
        CustomizeFeedDialog(
            selectedCategories = preferredCategories,
            rssFeeds = rssFeeds,
            onDismiss = { showCustomizeDialog = false },
            onSaveCategories = { newsViewModel.updateCategories(it) },
            onAddRssFeed = { newsViewModel.addRssFeed(it) },
            onRemoveRssFeed = { newsViewModel.removeRssFeed(it) }
        )
    }

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    var currentTime by remember { 
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) 
    }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(10000)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { newsViewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { isEditMode = true },
                            onTap = { isEditMode = false }
                        )
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    bottom = 120.dp, 
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 20.dp
                )
            ) {
                // Master Dashboard Header
                item {
                    FluentSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        alpha = 0.45f,
                        effect = FluentEffect.ACRYLIC,
                        blurRadius = 150,
                        tintColor = Color.Black.copy(alpha = 0.25f),
                        luminosityAlpha = 0.2f
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = greeting,
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        letterSpacing = (-1.5).sp
                                    )
                                    Text(
                                        text = currentTime,
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .clickable { photoPickerLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (profileImageUri != null) {
                                        AsyncImage(
                                            model = profileImageUri,
                                            contentDescription = "Profile",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            Icons.Rounded.Person, 
                                            contentDescription = "Profile",
                                            modifier = Modifier.size(36.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Integrated Navigation Pill
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(Color.White.copy(alpha = 0.08f)),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TabItem(
                                    text = "Widgets",
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 }
                                )
                                Spacer(modifier = Modifier.width(32.dp))
                                TabItem(
                                    text = "News",
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    onDoubleClick = { showCustomizeDialog = true }
                                )
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // Widgets Section Title
                    item {
                        Text(
                            "MY DASHBOARD",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                    }

                    // Widgets List Content
                    itemsIndexed(boardWidgets, key = { _, it -> it.id }) { index, item ->
                        val surfaceAlpha = if (item.isWidget) 0f else 0.4f
                        val surfaceEffect = if (item.isWidget) FluentEffect.MICA else FluentEffect.ACRYLIC
                        
                        val itemHeight = when(item.size) {
                            TileSize.SMALL -> 120.dp
                            TileSize.MEDIUM -> 240.dp
                            TileSize.WIDE -> 240.dp
                            TileSize.LARGE -> 480.dp
                            else -> 240.dp
                        }

                        val isDragging = draggingId == item.id

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .graphicsLayer {
                                    alpha = if (isDragging) 0.5f else 1.0f
                                    scaleX = if (isDragging) 1.02f else 1.0f
                                    scaleY = if (isDragging) 1.02f else 1.0f
                                }
                        ) {
                            // Container for widget/card content - Remove background for widgets
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = { 
                                            isEditMode = true
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                            ) {
                                // For non-app-widgets, keep the FluentSurface effect
                                if (!item.isWidget) {
                                    FluentSurface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        alpha = surfaceAlpha,
                                        effect = surfaceEffect,
                                        blurRadius = 80,
                                        tintColor = Color.Black.copy(alpha = 0.2f),
                                        borderAlpha = 0.1f
                                    ) {
                                        BoardContent(item, itemHeight, newsViewModel, currentMedia, calendarEvents, tasks, appWidgetHost, recentPhotos)
                                    }
                                } else {
                                    // For App Widgets, just show the content directly with no background
                                    Box(modifier = Modifier.fillMaxWidth().height(itemHeight)) {
                                        WidgetHostItem(widgetId = item.widgetId!!, sharedHost = appWidgetHost)
                                    }
                                }
                            }

                            if (isEditMode) {
                                // Reorder Handle (Center)
                                var accumulatedDrag by remember { mutableFloatStateOf(0f) }
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .pointerInput(item.id) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { 
                                                    draggingId = item.id 
                                                    draggingIndex = index
                                                    accumulatedDrag = 0f
                                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                },
                                                onDragEnd = { 
                                                    draggingId = null
                                                    draggingIndex = -1
                                                    accumulatedDrag = 0f
                                                },
                                                onDragCancel = { 
                                                    draggingId = null
                                                    draggingIndex = -1
                                                    accumulatedDrag = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    accumulatedDrag += dragAmount.y
                                                    
                                                    // Vertical reordering logic
                                                    val threshold = 150f // Pivot point distance
                                                    if (accumulatedDrag > threshold && draggingIndex < boardWidgets.size - 1) {
                                                        newsViewModel.moveBoardWidget(draggingIndex, draggingIndex + 1)
                                                        draggingIndex++
                                                        accumulatedDrag = 0f
                                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    } else if (accumulatedDrag < -threshold && draggingIndex > 0) {
                                                        newsViewModel.moveBoardWidget(draggingIndex, draggingIndex - 1)
                                                        draggingIndex--
                                                        accumulatedDrag = 0f
                                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                }
                                            )
                                        }
                                )

                                // Resize Button (Top Right)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .zIndex(10f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable {
                                                val nextSize = when (item.size) {
                                                    TileSize.SMALL -> TileSize.MEDIUM
                                                    TileSize.MEDIUM -> TileSize.WIDE
                                                    TileSize.WIDE -> TileSize.LARGE
                                                    TileSize.LARGE -> TileSize.SMALL
                                                }
                                                newsViewModel.resizeBoardWidget(item.id, nextSize)
                                            },
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

                                // Delete Button (Top Left)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .zIndex(10f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error)
                                            .clickable { widgetToDelete = item.id },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                                
                                // Selection Border
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                                )
                            }
                        }
                    }

                    item {
                        FluentSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .clickable { showWidgetPicker = true },
                            shape = RoundedCornerShape(24.dp),
                            alpha = 0.5f,
                            effect = FluentEffect.ACRYLIC,
                            blurRadius = 80
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Add Widget",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                } else {
                    // News Feed Content
                    if (isLoading && articles.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        items(articles) { article ->
                            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                            NewsCard(
                                article = article,
                                tileOpacity = tileOpacity,
                                onClick = { uriHandler.openUri(article.url) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardContent(
    item: com.example.windows11mobile.data.HomeTile,
    itemHeight: androidx.compose.ui.unit.Dp,
    newsViewModel: NewsFeedViewModel,
    currentMedia: MediaData?,
    calendarEvents: List<com.example.windows11mobile.data.CalendarEvent>,
    tasks: List<com.example.windows11mobile.ui.news.TodoTask>,
    appWidgetHost: android.appwidget.AppWidgetHost,
    recentPhotos: List<Uri>
) {
    Box(modifier = Modifier.fillMaxWidth().height(itemHeight)) {
        when {
            item.isWidget && item.widgetId != null -> {
                WidgetHostItem(widgetId = item.widgetId, sharedHost = appWidgetHost)
            }
            item.specialType == "calendar" -> CalendarWidget(calendarEvents)
            item.specialType == "music" -> MusicBoardWidget(
                media = currentMedia,
                onPlayPause = { newsViewModel.mediaPlayPause() },
                onSkipNext = { newsViewModel.mediaSkipNext() },
                onSkipPrevious = { newsViewModel.mediaSkipPrevious() }
            )
            item.specialType == "tasks" -> TasksWidget(
                tasks = tasks,
                onToggle = { newsViewModel.toggleTask(it) },
                onAdd = { newsViewModel.addTask(it) },
                onClear = { newsViewModel.clearTasks() }
            )
            item.specialType == "photos" -> PhotosBoardWidget(recentPhotos)
            item.specialType == "system" -> SystemBoardWidget()
        }
    }
}

@Composable
fun SystemBoardWidget() {
    val context = LocalContext.current
    
    var brightness by remember { mutableStateOf(0.5f) }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.SettingsSuggest, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "System Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Toggles Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SystemToggle(
                icon = Icons.Rounded.Wifi,
                label = "WiFi",
                enabled = true,
                onClick = { 
                    try {
                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (e: Exception) {}
                }
            )
            SystemToggle(
                icon = Icons.Rounded.Bluetooth,
                label = "Bluetooth",
                enabled = true,
                onClick = { 
                    try {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (e: Exception) {}
                }
            )
            SystemToggle(
                icon = Icons.Rounded.FlashlightOn,
                label = "Flash",
                enabled = false,
                onClick = { 
                    // Toggling flashlight requires CameraManager
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Brightness Slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.LightMode, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(12.dp))
            Slider(
                value = brightness,
                onValueChange = { brightness = it },
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun SystemToggle(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) MaterialTheme.colorScheme.primary 
                    else Color.White.copy(alpha = 0.05f)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun TabItem(
    text: String, 
    selected: Boolean, 
    onClick: () -> Unit,
    onDoubleClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onDoubleClick = onDoubleClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (selected) 1f else 0.5f)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun CalendarWidget(
    events: List<com.example.windows11mobile.data.CalendarEvent>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).setData(
                        Uri.parse("content://com.android.calendar/time/")
                    )
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.calendar")
                        ?: context.packageManager.getLaunchIntentForPackage("com.android.calendar")
                    if (intent != null) context.startActivity(intent)
                }
            }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FluentIcon(
                FluentIcons.Calendar, 
                contentDescription = null, 
                size = 24.dp,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFE74C3C), Color(0xFFC0392B)))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (events.isEmpty()) {
            Text(
                "No upcoming events",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                events.take(3).forEach { event ->
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.startTime))
                    EventItem(event.title, timeStr, true, Color(event.color))
                }
            }
        }
    }
}

@Composable
fun EventItem(title: String, time: String, active: Boolean, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 32.dp)
                .clip(CircleShape)
                .background(if (active) color else Color.Gray.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = time, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TasksWidget(
    tasks: List<com.example.windows11mobile.ui.news.TodoTask>,
    onToggle: (String) -> Unit,
    onAdd: (String) -> Unit,
    onClear: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var newTaskText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth().padding(20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FluentIcon(
                    Icons.Rounded.CheckCircle, 
                    contentDescription = null, 
                    size = 24.dp,
                    tint = Color(0xFF2ECC71)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "To Do",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (tasks.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text(
                        "Clear List", 
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            tasks.forEach { task ->
                TaskItem(task.text, task.isCompleted, onToggle = { onToggle(task.id) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = newTaskText,
            onValueChange = { newTaskText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Add a task...") },
            trailingIcon = {
                IconButton(onClick = {
                    if (newTaskText.isNotBlank()) {
                        onAdd(newTaskText)
                        newTaskText = ""
                    }
                }) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun MusicBoardWidget(
    media: MediaData?,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        // Album Art Background
        if (media?.albumArt != null) {
            androidx.compose.foundation.Image(
                bitmap = media.albumArt.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.6f),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.MusicNote, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (media?.isPlaying == true) "NOW PLAYING" else "MUSIC",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (media?.title != null) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
                Text(
                    text = media.artist ?: "Unknown Artist",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Rounded.SkipPrevious, 
                            contentDescription = "Previous", 
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = onPlayPause,
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(
                            imageVector = if (media.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Rounded.SkipNext, 
                            contentDescription = "Next", 
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                Text(
                    "Nothing playing",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun PhotosBoardWidget(localPhotos: List<Uri>) {
    val context = LocalContext.current
    val fallbackPhotos = remember {
        listOf(
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
            "https://images.unsplash.com/photo-1511884642898-4c92249e20b6",
            "https://images.unsplash.com/photo-1434725039720-abb26e22ebe1",
            "https://images.unsplash.com/photo-1470770841072-f978cf4d019e"
        ).map { Uri.parse(it) }
    }

    val currentPhotos = if (localPhotos.isNotEmpty()) localPhotos else fallbackPhotos
    if (currentPhotos.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(currentPhotos) {
        while (currentPhotos.size > 1) {
            delay(10000)
            currentIndex = (currentIndex + 1) % currentPhotos.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        type = "image/*"
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.photos")
                        ?: context.packageManager.getLaunchIntentForPackage("com.android.gallery3d")
                    if (intent != null) context.startActivity(intent)
                }
            }
    ) {
        AnimatedContent(
            targetState = currentPhotos[currentIndex % currentPhotos.size],
            transitionSpec = {
                fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
            },
            label = "photoCycle",
            modifier = Modifier.fillMaxSize()
        ) { photoUri ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUri)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Overlay Title - Styled like other board widgets
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Photos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun TaskItem(text: String, completed: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = completed, 
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2ECC71))
        )
        Text(
            text = text, 
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
        )
    }
}
