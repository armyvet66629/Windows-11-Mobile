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
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.components.FluentContextMenu
import com.example.windows11mobile.ui.components.ActionButton

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
    modifier: Modifier = Modifier,
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val apps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }

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
            placeholder = { 
                Text(
                    "Search apps and web", 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                ) 
            },
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

        Box(modifier = Modifier.fillMaxSize()) {
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
                        FluentSurface(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            alpha = tileOpacity * 0.6f,
                            effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
                            blurRadius = 40,
                            tintColor = Color.Black.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = letter.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                            )
                        }
                    }
                    
                    item {
                        FluentSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            alpha = tileOpacity * 0.4f,
                            effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
                            blurRadius = 60,
                            tintColor = Color.Black.copy(alpha = 0.05f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                appList.forEach { app ->
                                    AppItem(
                                        app = app,
                                        tileOpacity = 0f, // Transparent since parent has the background
                                        onClick = { onAppClick(app) },
                                        onLongClick = { selectedAppForMenu = app }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Global Search Overlay
            StartSearchOverlay(
                searchQuery = searchQuery,
                filteredApps = apps,
                onAppClick = onAppClick,
                onDismiss = { viewModel.onSearchQueryChange("") }
            )

            // Custom Fluent Context Menu
            val currentApp = selectedAppForMenu
            if (currentApp != null) {
                val shortcuts = remember(currentApp) { viewModel.getShortcuts(currentApp.packageName) }
                val pkg = currentApp.packageName.lowercase()
                
                FluentContextMenu(
                    isVisible = true,
                    title = currentApp.name,
                    subtitle = currentApp.packageName,
                    icon = currentApp.icon,
                    onDismiss = { selectedAppForMenu = null }
                ) {
                    // App-Specific Primary Actions
                    when {
                        pkg.contains("dialer") || pkg.contains("phone") -> {
                            ActionButton(
                                text = "New Call",
                                icon = FluentIcons.Call,
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_DIAL))
                                    } catch (e: Exception) {}
                                    selectedAppForMenu = null
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                        }
                        pkg.contains("messaging") || pkg.contains("message") || pkg.contains("sms") -> {
                            ActionButton(
                                text = "Compose Text",
                                icon = FluentIcons.Edit,
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")))
                                    } catch (e: Exception) {}
                                    selectedAppForMenu = null
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                        }
                    }

                    if (shortcuts.isNotEmpty()) {
                        val shortcutSectionTitle = remember(currentApp.packageName) {
                            val pkgName = currentApp.packageName.lowercase()
                            when {
                                pkgName.contains("dialer") || pkgName.contains("phone") -> "FREQUENT CONTACTS"
                                pkgName.contains("messaging") || pkgName.contains("message") || pkgName.contains("sms") || pkgName.contains("whatsapp") || pkgName.contains("telegram") -> "RECENT CONVERSATIONS"
                                pkgName.contains("calendar") -> "UPCOMING EVENTS"
                                pkgName.contains("mail") || pkgName.contains("outlook") || pkgName.contains("gmail") -> "RECENT EMAILS"
                                pkgName.contains("camera") -> "SHOOTING MODES"
                                pkgName.contains("chrome") || pkgName.contains("browser") || pkgName.contains("edge") -> "RECENT TABS"
                                else -> "SHORTCUTS"
                            }
                        }
                        
                        Text(
                            text = shortcutSectionTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
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

                            ActionButton(
                                text = label.toString(),
                                icon = shortcutIcon ?: Icons.AutoMirrored.Rounded.Launch,
                                onClick = {
                                    viewModel.launchShortcut(shortcut)
                                    selectedAppForMenu = null
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                    }

                    ActionButton(
                        text = "Pin to Taskbar",
                        icon = FluentIcons.Pin,
                        onClick = {
                            onPinToTaskbar(currentApp)
                            selectedAppForMenu = null
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ActionButton(
                        text = "Add to Home Screen",
                        icon = FluentIcons.Home,
                        onClick = {
                            onAddToHomeScreen(currentApp)
                            selectedAppForMenu = null
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                    
                    ActionButton(
                        text = "Share this App",
                        icon = FluentIcons.Share,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Check out ${currentApp.name} at https://play.google.com/store/apps/details?id=${currentApp.packageName}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share ${currentApp.name}"))
                            selectedAppForMenu = null
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ActionButton(
                        text = "Rate & Review",
                        icon = FluentIcons.Star,
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${currentApp.packageName}")))
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${currentApp.packageName}")))
                            }
                            selectedAppForMenu = null
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

                    ActionButton(
                        text = "Uninstall App",
                        icon = FluentIcons.Uninstall,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DELETE).apply {
                                data = Uri.fromParts("package", currentApp.packageName, null)
                            }
                            context.startActivity(intent)
                            selectedAppForMenu = null
                        },
                        contentColor = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ActionButton(
                        text = "App Info",
                        icon = FluentIcons.Info,
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", currentApp.packageName, null)
                            }
                            context.startActivity(intent)
                            selectedAppForMenu = null
                        }
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
        blurRadius = 80,
        tintColor = Color.Black.copy(alpha = 0.15f),
        luminosityAlpha = 0.1f
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
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box {
        FluentSurface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(16.dp),
            alpha = tileOpacity,
            effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
            blurRadius = 60,
            tintColor = Color.Black.copy(alpha = 0.1f),
            luminosityAlpha = 0.08f
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
