package com.example.windows11mobile.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    
    var showAddAppDialog by remember { mutableStateOf(false) }
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
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                Text(
                    "Personalization",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, // Higher contrast
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Dark Mode Toggle
            item {
                SettingsToggleItem(
                    title = "Dark Mode",
                    subtitle = "Switch between light and dark theme",
                    icon = if (isDarkMode == true) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    checked = isDarkMode ?: false,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }

            // Wallpaper Picker
            item {
                SettingsClickableItem(
                    title = "Wallpaper",
                    subtitle = "Change the launcher background image",
                    icon = Icons.Rounded.Wallpaper,
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

            // Tile Opacity Slider
            item {
                val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()
                FluentSurface(
                    modifier = Modifier.fillMaxWidth(),
                    alpha = 0.3f,
                    effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
                    blurRadius = 30,
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
                            Text("Tile Opacity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${(tileOpacity * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "System",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, // Higher contrast
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingsClickableItem(
                    title = "Enable Live Tiles (Notifications)",
                    subtitle = "Allow the launcher to show notifications on tiles",
                    icon = Icons.Rounded.NotificationsActive,
                    onClick = {
                        context.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                )
            }

            item {
                SettingsClickableItem(
                    title = "About",
                    subtitle = "Windows 11 Mobile Launcher v1.0",
                    icon = Icons.Rounded.Info,
                    onClick = {}
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Taskbar & Dock",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, // Higher contrast
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Text(
                    "Pinned Apps",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            val pinnedAppsList = pinnedApps.toList()
            items(pinnedAppsList) { packageName ->
                val appInfo = installedApps.find { it.packageName == packageName }
                PinnedAppItem(
                    appInfo = appInfo,
                    packageName = packageName,
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
}

@Composable
fun PinnedAppItem(
    appInfo: AppInfo?,
    packageName: String,
    onUnpin: () -> Unit
) {
    FluentSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        alpha = 0.3f,
        effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
        blurRadius = 30,
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (appInfo != null) {
                    Text(
                        text = packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
    onCheckedChange: (Boolean) -> Unit
) {
    FluentSurface(
        modifier = Modifier.fillMaxWidth(),
        alpha = 0.3f,
        effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
        blurRadius = 30,
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
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
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
    trailingContent: @Composable (() -> Unit)? = null
) {
    FluentSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        alpha = 0.3f,
        effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
        blurRadius = 30,
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
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(Icons.Rounded.ChevronRight, contentDescription = null)
            }
        }
    }
}
