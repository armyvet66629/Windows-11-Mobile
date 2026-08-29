package com.example.windows11mobile.ui.components

import android.content.Intent
import android.net.Uri
import android.content.pm.ShortcutInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.windows11mobile.data.HomeTile
import com.example.windows11mobile.data.TileSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.text.SimpleDateFormat
import java.util.*

import com.example.windows11mobile.ui.theme.SmokeDark
import com.example.windows11mobile.ui.theme.SmokeLight

import com.example.windows11mobile.ui.theme.FluentIcons
import androidx.compose.ui.graphics.Brush

@Composable
fun AdvancedFluentMenu(
    tile: HomeTile,
    onDismiss: () -> Unit,
    onResize: (TileSize) -> Unit,
    onRemove: () -> Unit = {},
    onMoveTile: () -> Unit = {},
    onAppSettings: () -> Unit,
    onCheckForUpdates: () -> Unit = {},
    onUninstall: () -> Unit = {},
    onShare: () -> Unit = {},
    onRefreshTile: () -> Unit = {},
    onClearNotifications: () -> Unit = {},
    onRename: () -> Unit = {},
    shortcuts: List<ShortcutInfo> = emptyList(),
    onShortcutClick: (ShortcutInfo) -> Unit = {},
    modifier: Modifier = Modifier,
    tileOpacity: Float = 0.45f
) {
    val context = LocalContext.current
    val appIcon = remember(tile.packageName) {
        tile.packageName?.let { pkg ->
            try { context.packageManager.getApplicationIcon(pkg) } catch (_: Exception) { null }
        }
    }

    FluentContextMenu(
        isVisible = true,
        title = tile.label,
        subtitle = tile.packageName ?: "App Widget",
        icon = appIcon,
        onDismiss = onDismiss
    ) {
        // Quick Actions Row (New Windows 11 Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                icon = if (tile.isWidget) FluentIcons.Delete else FluentIcons.Pin,
                contentDescription = "Pin/Unpin",
                onClick = onRemove
            )
            QuickActionButton(
                icon = FluentIcons.Share,
                contentDescription = "Share",
                onClick = onShare
            )
            if (!tile.isWidget && tile.specialType == null) {
                QuickActionButton(
                    icon = FluentIcons.Uninstall,
                    contentDescription = "Uninstall",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onUninstall
                )
            }
            QuickActionButton(
                icon = FluentIcons.Info,
                contentDescription = "App Info",
                onClick = onAppSettings
            )
        }

        // Notification Section
        if (tile.notificationCount > 0) {
            FluentSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                alpha = 0.2f,
                effect = FluentEffect.MICA,
                blurRadius = 0,
                borderAlpha = 0.15f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tile.notificationSender ?: "Notification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val timeStr = remember(tile.notificationTime) {
                                if (tile.notificationTime != null && tile.notificationTime > 0) {
                                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                                    sdf.format(Date(tile.notificationTime))
                                } else ""
                            }
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onClearNotifications,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    FluentIcons.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tile.notificationContent ?: tile.notificationSummary ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Category-Specific Actions
        val pkg = tile.packageName?.lowercase() ?: ""
        when {
            tile.specialType == HomeTile.TYPE_WEATHER || tile.specialType == HomeTile.TYPE_CLOCK_WEATHER -> {
                ActionButton(
                    text = "Change Location",
                    icon = FluentIcons.Search,
                    onClick = { /* Implement in ViewModel */ }
                )
                Spacer(modifier = Modifier.height(10.dp))
                ActionButton(
                    text = "Refresh Weather",
                    icon = FluentIcons.Refresh,
                    onClick = onRefreshTile
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
            }
            tile.specialType == HomeTile.TYPE_PHOTOS -> {
                ActionButton(
                    text = "Choose Albums",
                    icon = FluentIcons.Photos,
                    onClick = { /* Implement */ }
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
            }
            pkg.contains("dialer") || pkg.contains("phone") -> {
                ActionButton(
                    text = "New Call",
                    icon = FluentIcons.Call,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL)
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                        onDismiss()
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
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
            }
            pkg.contains("calendar") -> {
                ActionButton(
                    text = "New Event",
                    icon = FluentIcons.Calendar,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_INSERT)
                                .setData(Uri.parse("content://com.android.calendar/events"))
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
            }
        }

        // Resize Quick Actions
        Text(
            text = "RESIZE MODE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResizeButton(TileSize.SMALL, currentSize = tile.size, modifier = Modifier.weight(1f), onClick = { onResize(TileSize.SMALL) })
            ResizeButton(TileSize.MEDIUM, currentSize = tile.size, modifier = Modifier.weight(1f), onClick = { onResize(TileSize.MEDIUM) })
            ResizeButton(TileSize.WIDE, currentSize = tile.size, modifier = Modifier.weight(1f), onClick = { onResize(TileSize.WIDE) })
            ResizeButton(TileSize.LARGE, currentSize = tile.size, modifier = Modifier.weight(1f), onClick = { onResize(TileSize.LARGE) })
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = Color.White.copy(alpha = 0.1f))

        // Standard App Actions
        if (!tile.isWidget && tile.specialType == null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
        }

        // Shortcuts Section
        if (shortcuts.isNotEmpty()) {
            val shortcutSectionTitle = remember(tile.packageName) {
                val pkgName = tile.packageName?.lowercase() ?: ""
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
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            shortcuts.forEach { shortcut ->
                val label = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                    shortcut.shortLabel ?: shortcut.longLabel ?: "Shortcut"
                } else {
                    "Shortcut"
                }
                
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
                        onShortcutClick(shortcut)
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
        }

        // Action Buttons
        if (tile.isFolder) {
            ActionButton(
                text = "Rename folder",
                icon = Icons.Rounded.Edit,
                onClick = {
                    onRename()
                    onDismiss()
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        ActionButton(
            text = "Move Tile",
            icon = FluentIcons.Open,
            onClick = {
                onMoveTile()
                onDismiss()
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        ActionButton(
            text = "Check for Updates",
            icon = FluentIcons.Search,
            onClick = onCheckForUpdates
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        FluentIcon(
            imageVector = icon,
            contentDescription = contentDescription,
            size = 24.dp,
            tint = tint
        )
    }
}

@Composable
fun ResizeButton(
    size: TileSize,
    currentSize: TileSize,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isSelected = size == currentSize
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        val iconSize = when(size) {
            TileSize.SMALL -> 12.dp
            TileSize.MEDIUM -> 18.dp
            TileSize.WIDE -> 18.dp
            TileSize.LARGE -> 24.dp
        }
        
        Box(
            modifier = Modifier
                .size(
                    width = if (size == TileSize.WIDE) 28.dp else iconSize,
                    height = iconSize
                )
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                    RoundedCornerShape(2.dp)
                )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun AdvancedFluentMenuPreview() {
    val tile = HomeTile(
        id = "1",
        packageName = "com.google.android.apps.messaging",
        label = "Messages",
        notificationCount = 1,
        notificationSender = "John Doe",
        notificationContent = "Hey, are we still meeting today at 5 PM for the design review?",
        notificationTime = System.currentTimeMillis() - 300000
    )
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
            AdvancedFluentMenu(
                tile = tile,
                onDismiss = {},
                onResize = {},
                onAppSettings = {}
            )
        }
    }
}
