package com.example.windows11mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.windows11mobile.data.AppInfo

@Composable
fun WindowsDock(
    pinnedApps: Set<String>,
    installedApps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        FluentSurface(
            modifier = Modifier.wrapContentWidth().fillMaxHeight(),
            shape = RoundedCornerShape(36.dp),
            alpha = 0.4f,
            effect = FluentEffect.ACRYLIC,
            blurRadius = 120,
            tintColor = Color.Black.copy(alpha = 0.2f),
            luminosityAlpha = 0.2f
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onStartClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Apps,
                        contentDescription = "Start",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                // Pinned Apps
                pinnedApps.forEach { packageName ->
                    val appInfo = installedApps.find { it.packageName == packageName }
                    DockItem(
                        appInfo = appInfo,
                        packageName = packageName,
                        onClick = { onAppClick(packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun DockItem(
    appInfo: AppInfo?,
    packageName: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val icon = remember(appInfo) {
        appInfo?.icon?.toBitmap()?.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            androidx.compose.foundation.Image(
                bitmap = icon,
                contentDescription = appInfo?.name,
                modifier = Modifier.size(32.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            )
        }
    }
}
