package com.example.windows11mobile.ui.apps

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.data.AppRepository
import com.example.windows11mobile.data.SettingsRepository
import com.example.windows11mobile.ui.components.FluentSurface
import com.microsoft.fluentui.tokenized.menu.Menu
import com.microsoft.fluentui.tokenized.listitem.ListItem

import com.example.windows11mobile.ui.theme.FluentIcons
import com.example.windows11mobile.ui.components.FluentIcon
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerScreen(
    viewModel: AppDrawerViewModel,
    onAppClick: (AppInfo) -> Unit,
    onSettingsClick: () -> Unit,
    onPinToTaskbar: (AppInfo) -> Unit,
    onAddToHomeScreen: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val apps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search apps") },
            leadingIcon = { 
                FluentIcon(
                    FluentIcons.Search, 
                    contentDescription = null,
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ) 
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
            ),
            singleLine = true
        )

        // Apps List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for taskbar
        ) {
            // Launcher Settings entry at the top
            item {
                LauncherSettingsItem(
                    tileOpacity = tileOpacity,
                    onClick = onSettingsClick
                )
            }

            val grouped = apps.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '?' }
            
            grouped.forEach { (letter, appList) ->
                item {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }
                
                items(appList) { app ->
                    AppItem(
                        app = app,
                        tileOpacity = tileOpacity,
                        getShortcuts = { viewModel.getShortcuts(app.packageName) },
                        onShortcutClick = { viewModel.launchShortcut(it) },
                        onClick = { onAppClick(app) },
                        onPinToTaskbar = { onPinToTaskbar(app) },
                        onAddToHomeScreen = { onAddToHomeScreen(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun LauncherSettingsItem(
    tileOpacity: Float = 0.3f,
    onClick: () -> Unit
) {
    FluentSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        alpha = tileOpacity,
        effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
        blurRadius = 40
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                FluentIcon(
                    imageVector = FluentIcons.Settings,
                    contentDescription = null,
                    size = 28.dp,
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Launcher Settings",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Personalize your experience",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItem(
    app: AppInfo,
    tileOpacity: Float = 0.25f,
    getShortcuts: () -> List<android.content.pm.ShortcutInfo> = { emptyList() },
    onShortcutClick: (android.content.pm.ShortcutInfo) -> Unit = {},
    onClick: () -> Unit,
    onPinToTaskbar: () -> Unit,
    onAddToHomeScreen: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val shortcuts = remember(showMenu) { if (showMenu) getShortcuts() else emptyList() }

    Box {
        FluentSurface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                ),
            shape = RoundedCornerShape(16.dp),
            alpha = tileOpacity,
            effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
            blurRadius = 30
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = app.icon?.toBitmap()?.asImageBitmap()
                if (icon != null) {
                    Image(
                        bitmap = icon,
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    val fallbackIcon = when(app.name.lowercase()) {
                        "settings" -> FluentIcons.Settings
                        "calendar" -> FluentIcons.Calendar
                        "people", "contacts" -> Icons.Rounded.Person
                        "messaging", "messages" -> FluentIcons.Message
                        "phone", "dialer" -> Icons.Rounded.Phone
                        "camera" -> Icons.Rounded.CameraAlt
                        "mail", "gmail", "outlook" -> FluentIcons.Mail
                        "maps", "navigation" -> Icons.Rounded.Map
                        "photos", "gallery" -> FluentIcons.Photos
                        else -> null
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fallbackIcon != null) {
                            FluentIcon(
                                imageVector = fallbackIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                size = 28.dp,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                        } else {
                            Text(
                                text = app.name.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Menu(
            opened = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            FluentSurface(
                shape = MaterialTheme.shapes.medium,
                alpha = 0.9f,
                effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
                blurRadius = 25
            ) {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    if (shortcuts.isNotEmpty()) {
                        shortcuts.forEach { shortcut ->
                            val label = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                                shortcut.shortLabel ?: shortcut.longLabel ?: "Shortcut"
                            } else "Shortcut"
                            
                            val shortcutIcon = remember(shortcut) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                                    try {
                                        val launcherApps = context.getSystemService(android.content.Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
                                        launcherApps.getShortcutIconDrawable(shortcut, context.resources.displayMetrics.densityDpi)
                                    } catch (_: Exception) {
                                        null
                                    }
                                } else null
                            }

                            ListItem.Item(
                                text = label.toString(),
                                leadingAccessoryContent = if (shortcutIcon != null) {
                                    { AsyncImage(model = shortcutIcon, contentDescription = null, modifier = Modifier.size(20.dp)) }
                                } else null,
                                onClick = {
                                    onShortcutClick(shortcut)
                                    showMenu = false
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }

                    ListItem.Item(
                        text = "Pin to Taskbar",
                        leadingAccessoryContent = {
                            FluentIcon(FluentIcons.Pin, contentDescription = null, size = 20.dp)
                        },
                        onClick = {
                            onPinToTaskbar()
                            showMenu = false
                        }
                    )
                    ListItem.Item(
                        text = "Add to Home Screen",
                        leadingAccessoryContent = {
                            FluentIcon(FluentIcons.Home, contentDescription = null, size = 20.dp)
                        },
                        onClick = {
                            onAddToHomeScreen()
                            showMenu = false
                        }
                    )
                    ListItem.Item(
                        text = "App Info",
                        leadingAccessoryContent = {
                            FluentIcon(FluentIcons.Info, contentDescription = null, size = 20.dp)
                        },
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                            }
                            context.startActivity(intent)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppDrawerScreenPreview() {
    val repository = remember {
        object : AppRepository {
            override suspend fun getInstalledApps(): List<AppInfo> = listOf(
                AppInfo("Calculator", "com.calc", null),
                AppInfo("Calendar", "com.cal", null),
                AppInfo("Camera", "com.cam", null),
                AppInfo("Settings", "com.settings", null)
            )
            override fun getShortcuts(packageName: String): List<android.content.pm.ShortcutInfo> = emptyList()
        }
    }
    val context = LocalContext.current
    val settingsRepository = remember { com.example.windows11mobile.data.RealSettingsRepository(context) }
    val viewModel = remember { AppDrawerViewModel(repository, settingsRepository, context) }
    
    com.example.windows11mobile.ui.theme.Windows11MobileTheme {
        AppDrawerScreen(
            viewModel = viewModel,
            onAppClick = {},
            onSettingsClick = {},
            onPinToTaskbar = {},
            onAddToHomeScreen = {}
        )
    }
}
