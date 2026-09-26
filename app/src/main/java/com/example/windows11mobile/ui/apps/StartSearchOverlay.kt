package com.example.windows11mobile.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.windows11mobile.data.AppInfo
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.components.FluentIcon
import com.example.windows11mobile.ui.theme.FluentIcons

@Composable
fun StartSearchOverlay(
    searchQuery: String,
    filteredApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    
    AnimatedVisibility(
        visible = searchQuery.isNotEmpty(),
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 }
    ) {
        FluentSurface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp), // Below the search bar
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            alpha = 0.95f,
            effect = FluentEffect.ACRYLIC,
            blurRadius = 150,
            tintColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 
                Color.Black.copy(alpha = 0.25f) 
            else 
                Color(0xFFB0B0B0).copy(alpha = 0.15f),
            luminosityAlpha = 0.15f
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Apps Results
                if (filteredApps.isNotEmpty()) {
                    item {
                        SearchCategoryHeader("APPS")
                    }
                    items(filteredApps.take(5)) { app ->
                        SearchResultItem(
                            title = app.name,
                            subtitle = "Installed App",
                            icon = FluentIcons.Apps,
                            onClick = { onAppClick(app) }
                        )
                    }
                }

                // Web Search
                item {
                    SearchCategoryHeader("WEB RESULTS")
                }
                item {
                    SearchResultItem(
                        title = "Search for \"$searchQuery\"",
                        subtitle = "Open in browser",
                        icon = Icons.AutoMirrored.Rounded.OpenInNew,
                        onClick = { uriHandler.openUri("https://www.bing.com/search?q=$searchQuery") }
                    )
                }

                // Quick Actions
                item {
                    SearchCategoryHeader("QUICK ACTIONS")
                }
                item {
                    SearchResultItem(
                        title = "Settings",
                        subtitle = "System Preferences",
                        icon = FluentIcons.Settings,
                        onClick = { /* Navigate to settings */ }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchCategoryHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SearchResultItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val surfaceTint = if (isDark) Color.Black.copy(alpha = 0.12f) else Color(0xFFB0B0B0).copy(alpha = 0.08f)
    
    FluentSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        alpha = 0.15f,
        effect = FluentEffect.ACRYLIC,
        blurRadius = 40,
        tintColor = surfaceTint,
        borderAlpha = 0.1f
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                FluentIcon(
                    imageVector = icon,
                    contentDescription = null,
                    size = 20.dp,
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
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
