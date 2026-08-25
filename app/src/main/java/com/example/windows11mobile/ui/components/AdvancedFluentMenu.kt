package com.example.windows11mobile.ui.components

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
    shortcuts: List<ShortcutInfo> = emptyList(),
    onShortcutClick: (ShortcutInfo) -> Unit = {},
    modifier: Modifier = Modifier,
    tileOpacity: Float = 0.45f
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    // Ensure higher contrast in light mode
    val effectiveAlpha = if (isDark) tileOpacity else 0.85f
    
    val appIcon = remember(tile.packageName) {
        tile.packageName?.let { pkg ->
            try { context.packageManager.getApplicationIcon(pkg) } catch (_: Exception) { null }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss, interactionSource = null, indication = null),
        contentAlignment = Alignment.Center
    ) {
        // Smoke Overlay for Modal Dimming
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) SmokeDark else SmokeLight)
        )

        FluentSurface(
            modifier = Modifier
                .width(340.dp)
                .heightIn(max = 600.dp)
                .padding(16.dp)
                .clickable(enabled = false) {}, // Consume clicks on the menu itself
            shape = RoundedCornerShape(32.dp),
            alpha = effectiveAlpha,
            effect = FluentEffect.ACRYLIC,
            blurRadius = 60
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    if (appIcon != null) {
                        AsyncImage(
                            model = appIcon,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp)
                                .shadow(if (!isDark) 4.dp else 0.dp, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(FluentIcons.Apps, contentDescription = null, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = tile.label,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold, // Authentic Segoe UI Variable Semibold
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = tile.packageName ?: "App Widget",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.6f else 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tile.notificationContent ?: tile.notificationSummary ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Reply Box placeholder
                            OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                placeholder = { Text("Reply to notification...", fontSize = 13.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                ),
                                trailingIcon = {
                                    IconButton(onClick = {}) {
                                        Icon(
                                            Icons.Rounded.Send, 
                                            contentDescription = "Send", 
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                singleLine = true,
                                readOnly = true // As it's a context menu interaction placeholder
                            )
                        }
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

                // Shortcuts Section
                if (shortcuts.isNotEmpty()) {
                    Text(
                        text = "SHORTCUTS",
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

                        ShortcutActionButton(
                            text = label.toString(),
                            icon = shortcutIcon,
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
                Spacer(modifier = Modifier.height(10.dp))
                ActionButton(
                    text = if (tile.isWidget) "Remove Widget" else "Unpin from Start",
                    icon = if (tile.isWidget) FluentIcons.Delete else FluentIcons.Pin,
                    onClick = onRemove,
                    contentColor = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(10.dp))
                ActionButton(
                    text = "App Settings",
                    icon = FluentIcons.Settings,
                    onClick = onAppSettings
                )
                Spacer(modifier = Modifier.height(10.dp))
                ActionButton(
                    text = "Dismiss Menu",
                    icon = FluentIcons.Close,
                    onClick = onDismiss
                )
            }
        }
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

@Composable
fun ShortcutActionButton(
    text: String,
    icon: android.graphics.drawable.Drawable?,
    onClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.08f),
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.AutoMirrored.Rounded.Launch, contentDescription = null, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Normal)
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.08f),
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FluentIcon(
                imageVector = icon, 
                contentDescription = null, 
                size = 22.dp,
                gradient = if (contentColor == MaterialTheme.colorScheme.onSurface) {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                } else null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Normal)
        }
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
