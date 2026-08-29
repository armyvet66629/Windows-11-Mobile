package com.example.windows11mobile.ui.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.components.FluentSurface

@Composable
fun WidgetPickerDialog(
    availableWidgets: Map<String, List<AppWidgetProviderInfo>>,
    onDismiss: () -> Unit,
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    val appList = remember(availableWidgets) {
        availableWidgets.keys.mapNotNull { pkg ->
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                val label = packageManager.getApplicationLabel(appInfo).toString()
                val icon = packageManager.getApplicationIcon(appInfo)
                AppWithWidgets(pkg, label, icon, availableWidgets[pkg] ?: emptyList())
            } catch (_: Exception) {
                null
            }
        }.sortedBy { it.label }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FluentSurface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            alpha = 0.85f,
            effect = FluentEffect.ACRYLIC,
            blurRadius = 120
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Widgets",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(appList) { app ->
                        AppWidgetSection(app, onWidgetSelected)
                    }
                }
            }
        }
    }
}

data class AppWithWidgets(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable,
    val widgets: List<AppWidgetProviderInfo>
)

@Composable
fun AppWidgetSection(
    app: AppWithWidgets,
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        FluentSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(16.dp),
            alpha = 0.1f,
            effect = FluentEffect.MICA,
            borderAlpha = 0.1f
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = app.icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.rotate(if (expanded) 90f else 0f)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                app.widgets.forEach { widget ->
                    WidgetPreviewCard(widget, onWidgetSelected)
                }
            }
        }
    }
}

@Composable
fun WidgetPreviewCard(
    info: AppWidgetProviderInfo,
    onWidgetSelected: (AppWidgetProviderInfo) -> Unit
) {
    val context = LocalContext.current
    val preview = remember(info) {
        try {
            info.loadPreviewImage(context, 0)
        } catch (e: Exception) {
            null
        }
    }

    FluentSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWidgetSelected(info) },
        shape = RoundedCornerShape(12.dp),
        alpha = 0.05f,
        effect = FluentEffect.MICA,
        borderAlpha = 0.05f
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = info.loadLabel(context.packageManager),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (preview != null) {
                    Image(
                        bitmap = preview.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        Icons.Rounded.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Min size: ${info.minWidth / 70}x${info.minHeight / 70}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Helper extension for rotation animation
@Composable
fun Modifier.rotate(degrees: Float): Modifier = this.then(
    Modifier.graphicsLayer { rotationZ = degrees }
)
