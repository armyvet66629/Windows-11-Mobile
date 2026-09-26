package com.example.windows11mobile.ui.shell

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.ui.apps.AppDrawerViewModel
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.theme.FluentIcons

@Composable
fun StartMenuScreen(
    viewModel: AppDrawerViewModel,
    onAppClick: (AppInfo) -> Unit,
    onAllAppsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPowerClick: () -> Unit,
    onBack: () -> Unit
) {
    val apps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()
    val tileBlurRadius by viewModel.tileBlurRadius.collectAsStateWithLifecycle()
    
    // We'll treat the first 18 apps as "Pinned" for the start menu
    val pinnedApps = remember(apps) { apps.take(18) }
    
    // Recent/Recommended apps (taking the next 6)
    val recommendedApps = remember(apps) { apps.drop(18).take(6) }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val surfaceTint = if (isDark) Color.Black.copy(alpha = 0.3f * tileOpacity) else Color(0xFFB0B0B0).copy(alpha = 0.25f * tileOpacity)
    val luminosityAlpha = if (isDark) 0.15f * tileOpacity else 0.25f * tileOpacity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onBack),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Dimmed background
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

        FluentSurface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) { }, // Consume clicks
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            alpha = 0.92f, // Slightly higher for more depth
            effect = FluentEffect.ACRYLIC,
            blurRadius = tileBlurRadius.toInt(),
            tintColor = surfaceTint,
            luminosityAlpha = luminosityAlpha,
            borderAlpha = 0.4f // Stronger edge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // Search Bar (matches App Drawer style)
                Spacer(modifier = Modifier.height(24.dp))
                FluentSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    alpha = tileOpacity,
                    effect = FluentEffect.ACRYLIC,
                    blurRadius = tileBlurRadius.toInt(),
                    tintColor = surfaceTint,
                    luminosityAlpha = luminosityAlpha,
                    borderAlpha = 0.15f
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search, 
                            contentDescription = null, 
                            tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Search for apps, settings, and documents",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }

                // Pinned Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "PINNED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = onAllAppsClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("All apps", fontWeight = FontWeight.Bold)
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pinnedApps) { app ->
                        StartMenuAppItem(app, onClick = { onAppClick(app) })
                    }
                }

                // Recommended Section
                Text(
                    "RECOMMENDED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                )

                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    recommendedApps.chunked(2).forEach { rowApps ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            rowApps.forEach { app ->
                                RecommendedItem(app, modifier = Modifier.weight(1f), onClick = { onAppClick(app) })
                            }
                            if (rowApps.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Footer Section
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Profile
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("User Name", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Power Button
                    IconButton(
                        onClick = onPowerClick,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Rounded.PowerSettingsNew, contentDescription = "Power", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun StartMenuAppItem(app: AppInfo, onClick: () -> Unit) {
    val context = LocalContext.current
    val icon = remember(app) { app.icon?.toBitmap()?.asImageBitmap() }

    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RecommendedItem(app: AppInfo, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val icon = remember(app) { app.icon?.toBitmap()?.asImageBitmap() }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(app.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Recently added", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}
