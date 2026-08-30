package com.example.windows11mobile.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.ui.components.FluentSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val wallpaperUri by viewModel.wallpaperUri.collectAsStateWithLifecycle()
    val pinnedApps by viewModel.pinnedApps.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val weatherAppPackage by viewModel.weatherAppPackage.collectAsStateWithLifecycle()
    val useFahrenheit by viewModel.useFahrenheit.collectAsStateWithLifecycle()
    val showTaskbar by viewModel.showTaskbar.collectAsStateWithLifecycle()
    val pageOrder by viewModel.pageOrder.collectAsStateWithLifecycle()
    val hiddenPages by viewModel.hiddenPages.collectAsStateWithLifecycle()
    val statusBarMode by viewModel.statusBarMode.collectAsStateWithLifecycle()
    val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()
    
    var showAddAppDialog by remember { mutableStateOf(false) }
    var showWeatherPicker by remember { mutableStateOf(false) }
    var showPageManager by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            // Take persistable URI permission
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not supported or fails
            }
            viewModel.setWallpaperUri(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Personalization",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Dark Mode Toggle
            item {
                SettingsToggleItem(
                    title = "Dark Mode",
                    subtitle = "Switch between light and dark theme",
                    icon = if (isDarkMode == true) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    checked = isDarkMode ?: false,
                    tileOpacity = tileOpacity,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }

            // Status Bar Style
            item {
                FluentSurface(
                    modifier = Modifier.fillMaxWidth(),
                    alpha = tileOpacity,
                    effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
                    blurRadius = 80,
                    tintColor = Color.Black.copy(alpha = 0.15f),
                    luminosityAlpha = 0.1f,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ViewStream, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Status Bar Icons", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = listOf("auto", "light", "dark")
                            options.forEach { mode ->
                                val isSelected = statusBarMode == mode
                                Button(
                                    onClick = { viewModel.setStatusBarMode(mode) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(mode.uppercase(), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Accent Color Picker
            item {
                val accentColorInt by viewModel.accentColor.collectAsStateWithLifecycle()
                val colors = listOf(
                    0xFF0078D4, // Windows Blue
                    0xFF4A4EDD, // Cobalt
                    0xFF00B294, // Teal
                    0xFF10893E, // Green
                    0xFFD83B01, // Orange
                    0xFFE81123, // Red
                    0xFFB4009E, // Purple
                    0xFF5D5A58  // Grey
                ).map { it.toInt() }

                FluentSurface(
                    modifier = Modifier.fillMaxWidth(),
                    alpha = tileOpacity,
                    effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
                    blurRadius = 80,
                    tintColor = Color.Black.copy(alpha = 0.15f),
                    luminosityAlpha = 0.1f,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Palette, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Accent Color", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colors.forEach { colorInt ->
                                val color = Color(colorInt)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (accentColorInt == colorInt) 3.dp else 0.dp,
                                            color = if (accentColorInt == colorInt) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setAccentColor(colorInt) }
                                )
                            }
                        }
                    }
                }
            }

            // Wallpaper Picker
            item {
                SettingsClickableItem(
                    title = "Wallpaper",
                    subtitle = "Change the launcher background image",
                    icon = Icons.Rounded.Wallpaper,
                    tileOpacity = tileOpacity,
                    onClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

            // Default Weather App
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
                    onClick = { showWeatherPicker = true }
                )
            }

            item {
                SettingsToggleItem(
                    title = "Use Fahrenheit",
                    subtitle = "Toggle between Celsius and Fahrenheit",
                    icon = Icons.Rounded.Thermostat,
                    checked = useFahrenheit,
                    tileOpacity = tileOpacity,
                    onCheckedChange = { viewModel.setUseFahrenheit(it) }
                )
            }

            // Tile Opacity Slider
            item {
                FluentSurface(
                    modifier = Modifier.fillMaxWidth(),
                    alpha = tileOpacity,
                    effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
                    blurRadius = 80,
                    tintColor = Color.Black.copy(alpha = 0.15f),
                    luminosityAlpha = 0.1f,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Opacity, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Tile Opacity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${(tileOpacity * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Slider(
                            value = tileOpacity,
                            onValueChange = { viewModel.setTileOpacity(it) },
                            valueRange = 0.05f..0.95f,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Text(
                            "Adjust how transparent the tiles appear on the home screen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Launcher Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            item {
                SettingsClickableItem(
                    title = "Set as Default Launcher",
                    subtitle = "Assign Windows 11 Mobile as your primary home screen",
                    icon = Icons.Rounded.Home,
                    tileOpacity = tileOpacity,
                    onClick = {
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback for older Android versions
                            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "System",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            item {
                val isNotificationServiceEnabled = remember(context) {
                    val enabledPackages = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                    enabledPackages?.contains(context.packageName) == true
                }
                
                SettingsClickableItem(
                    title = "Enable Live Tiles (Notifications)",
                    subtitle = if (isNotificationServiceEnabled) "Active • Receiving live updates" else "Inactive • Tap to grant access",
                    icon = if (isNotificationServiceEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsPaused,
                    tileOpacity = tileOpacity,
                    onClick = {
                        try {
                            context.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            // Hint to the user
                            android.widget.Toast.makeText(context, "Please toggle 'Windows 11 Mobile' OFF and then ON if already enabled.", android.widget.Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            // Fallback
                        }
                    },
                    trailingContent = {
                        if (isNotificationServiceEnabled) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = "Active", tint = Color(0xFF2ECC71))
                        } else {
                            Icon(Icons.Rounded.Warning, contentDescription = "Required", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            item {
                SettingsClickableItem(
                    title = "Battery Optimization",
                    subtitle = "Recommended: Disable for reliable Live Tiles",
                    icon = Icons.Rounded.BatteryChargingFull,
                    tileOpacity = tileOpacity,
                    onClick = {
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Taskbar & Dock",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            item {
                SettingsToggleItem(
                    title = "Show Taskbar",
                    subtitle = "Display quick-access app icons at the bottom",
                    icon = Icons.Rounded.WebAsset,
                    checked = showTaskbar,
                    tileOpacity = tileOpacity,
                    onCheckedChange = { viewModel.setShowTaskbar(it) }
                )
            }

            item {
                SettingsClickableItem(
                    title = "Page Manager",
                    subtitle = "Manage home screen pages and their order",
                    icon = Icons.Rounded.Layers,
                    tileOpacity = tileOpacity,
                    onClick = { showPageManager = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Pinned Apps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            val pinnedAppsList = pinnedApps.toList()
            items(pinnedAppsList) { packageName ->
                val appInfo = installedApps.find { it.packageName == packageName }
                PinnedAppItem(
                    appInfo = appInfo,
                    packageName = packageName,
                    tileOpacity = tileOpacity,
                    onUnpin = { viewModel.unpinApp(packageName) }
                )
            }

            item {
                Button(
                    onClick = { showAddAppDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add App to Dock")
                }
            }
        }
    }

    if (showAddAppDialog) {
        AddAppToDockDialog(
            apps = installedApps.filter { it.packageName !in pinnedApps },
            onDismiss = { showAddAppDialog = false },
            onAppSelect = { app ->
                viewModel.pinApp(app.packageName)
                showAddAppDialog = false
            }
        )
    }

    if (showWeatherPicker) {
        WeatherAppPickerDialog(
            apps = installedApps,
            onDismiss = { showWeatherPicker = false },
            onAppSelect = { pkg ->
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
fun PageManagerDialog(
    currentOrder: List<String>,
    hiddenPages: Set<String>,
    onDismiss: () -> Unit,
    onOrderChange: (List<String>) -> Unit,
    onHiddenPagesChange: (Set<String>) -> Unit
) {
    var tempOrder by remember { mutableStateOf(currentOrder) }
    var tempHidden by remember { mutableStateOf(hiddenPages) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Pages", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Rearrange your screens or hide them completely. Hidden pages won't appear in the main launcher flow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tempOrder.size) { index ->
                            val pageId = tempOrder[index]
                            val isHidden = tempHidden.contains(pageId)
                            val pageName = when(pageId) {
                                "notes" -> "Notes"
                                "board" -> "Widgets & News"
                                "desktop" -> "Start Screen"
                                "apps" -> "App List"
                                "people" -> "People Hub"
                                else -> pageId
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isHidden) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pageName, 
                                        fontWeight = FontWeight.Bold,
                                        color = if (isHidden) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isHidden) {
                                        Text(
                                            "Hidden", 
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Hide Toggle
                                    IconButton(
                                        onClick = {
                                            tempHidden = if (isHidden) tempHidden - pageId else tempHidden + pageId
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = if (isHidden) "Show" else "Hide",
                                            tint = if (isHidden) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Move Controls
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val mutable = tempOrder.toMutableList()
                                                val item = mutable.removeAt(index)
                                                mutable.add(index - 1, item)
                                                tempOrder = mutable
                                            }
                                        },
                                        enabled = index > 0,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Move Up")
                                    }
                                    IconButton(
                                        onClick = {
                                            if (index < tempOrder.size - 1) {
                                                val mutable = tempOrder.toMutableList()
                                                val item = mutable.removeAt(index)
                                                mutable.add(index + 1, item)
                                                tempOrder = mutable
                                            }
                                        },
                                        enabled = index < tempOrder.size - 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Move Down")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onOrderChange(tempOrder)
                onHiddenPagesChange(tempHidden)
                onDismiss()
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun WeatherAppPickerDialog(
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onAppSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Weather App", fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                LazyColumn {
                    val weatherApps = apps.filter { 
                        it.name.contains("Weather", ignoreCase = true) || 
                        it.packageName.contains("weather", ignoreCase = true) 
                    }
                    items(weatherApps) { app ->
                        ListItem(
                            headlineContent = { Text(app.name, fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                val icon = remember(app.icon) { 
                                    app.icon?.let { d ->
                                        try {
                                            val width = if (d.intrinsicWidth > 0) d.intrinsicWidth else 512
                                            val height = if (d.intrinsicHeight > 0) d.intrinsicHeight else 512
                                            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                                            val canvas = android.graphics.Canvas(bitmap)
                                            d.setBounds(0, 0, canvas.width, canvas.height)
                                            d.draw(canvas)
                                            bitmap.asImageBitmap()
                                        } catch (e: Exception) { null }
                                    }
                                }
                                if (icon != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            },
                            modifier = Modifier.clickable { onAppSelect(app.packageName) }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("Web Search (Default)", color = MaterialTheme.colorScheme.primary) },
                            leadingContent = { Icon(Icons.Rounded.Language, contentDescription = null) },
                            modifier = Modifier.clickable { onAppSelect("web") }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun PinnedAppItem(
    appInfo: AppInfo?,
    packageName: String,
    tileOpacity: Float = 0.3f,
    onUnpin: () -> Unit
) {
    FluentSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        alpha = tileOpacity,
        effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
        blurRadius = 120,
        tintColor = Color.Black.copy(alpha = 0.15f),
        luminosityAlpha = 0.12f,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = appInfo?.icon?.toBitmap()?.asImageBitmap()
            if (icon != null) {
                androidx.compose.foundation.Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appInfo?.name ?: packageName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (appInfo != null) {
                    Text(
                        text = packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            IconButton(onClick = onUnpin) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Unpin",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppToDockDialog(
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onAppSelect: (AppInfo) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add to Dock") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(apps) { app ->
                        ListItem(
                            headlineContent = { Text(app.name) },
                            supportingContent = { Text(app.packageName) },
                            leadingContent = {
                                val icon = app.icon?.toBitmap()?.asImageBitmap()
                                if (icon != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = icon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Icon(Icons.Rounded.Apps, contentDescription = null)
                                }
                            },
                            modifier = Modifier.clickable { onAppSelect(app) }
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    tileOpacity: Float = 0.3f,
    onCheckedChange: (Boolean) -> Unit
) {
    FluentSurface(
        modifier = Modifier.fillMaxWidth(),
        alpha = tileOpacity,
        effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
        blurRadius = 120,
        tintColor = Color.Black.copy(alpha = 0.2f),
        luminosityAlpha = 0.15f,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)
            }
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tileOpacity: Float = 0.3f,
    trailingContent: @Composable (() -> Unit)? = null
) {
    FluentSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        alpha = tileOpacity,
        effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
        blurRadius = 120,
        tintColor = Color.Black.copy(alpha = 0.2f),
        luminosityAlpha = 0.15f,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)
            }
            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(Icons.Rounded.ChevronRight, contentDescription = null)
            }
        }
    }
}
