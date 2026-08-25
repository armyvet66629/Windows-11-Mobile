package com.example.windows11mobile.ui.home

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.windows11mobile.ui.theme.FluentIcons
import com.example.windows11mobile.ui.components.FluentIcon
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import coil.compose.AsyncImage
import com.example.windows11mobile.data.HomeTile
import com.example.windows11mobile.data.TileSize
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockTileContent(tile: HomeTile) {
    var currentTime by remember { mutableStateOf(Date()) }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
    val secondsFormat = SimpleDateFormat("ss", Locale.getDefault())
    val periodFormat = SimpleDateFormat("a", Locale.getDefault())
    
    val dateFormat = when (tile.size) {
        TileSize.SMALL -> null
        TileSize.MEDIUM -> SimpleDateFormat("EEE, d", Locale.getDefault())
        TileSize.WIDE, TileSize.LARGE -> SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (tile.size == TileSize.LARGE) {
            // Analog Clock for Large Tile
            AnalogClock(currentTime)
            
            // Digital time overlay at bottom
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = timeFormat.format(currentTime),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface
                )
                dateFormat?.let {
                    Text(
                        text = it.format(currentTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = if (tile.size == TileSize.WIDE) Alignment.Start else Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeFormat.format(currentTime),
                        style = when(tile.size) {
                            TileSize.SMALL -> MaterialTheme.typography.titleLarge
                            TileSize.MEDIUM -> MaterialTheme.typography.displaySmall
                            TileSize.WIDE -> MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
                            else -> MaterialTheme.typography.displayLarge
                        },
                        fontWeight = FontWeight.W300,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (tile.size == TileSize.WIDE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(
                                text = secondsFormat.format(currentTime),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = periodFormat.format(currentTime),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                dateFormat?.let {
                    Text(
                        text = it.format(currentTime),
                        style = when(tile.size) {
                            TileSize.MEDIUM -> MaterialTheme.typography.labelSmall
                            else -> MaterialTheme.typography.titleMedium
                        },
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = if (tile.size == TileSize.WIDE) TextAlign.Start else TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AnalogClock(time: Date) {
    val calendar = Calendar.getInstance().apply { this.time = time }
    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = Modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // Clock face circle (subtle)
        drawCircle(
            color = onSurfaceColor.copy(alpha = 0.05f),
            radius = radius,
            center = center
        )

        // Hour marks
        for (i in 0 until 12) {
            val angle = i * 30 * (Math.PI / 180)
            val start = Offset(
                center.x + (radius - 15) * cos(angle).toFloat(),
                center.y + (radius - 15) * sin(angle).toFloat()
            )
            val end = Offset(
                center.x + radius * cos(angle).toFloat(),
                center.y + radius * sin(angle).toFloat()
            )
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.2f),
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx()
            )
        }

        // Hour hand
        val hourAngle = (hour * 30 + minute * 0.5) * (Math.PI / 180) - Math.PI / 2
        drawLine(
            color = onSurfaceColor,
            start = center,
            end = Offset(
                center.x + (radius * 0.5f) * cos(hourAngle).toFloat(),
                center.y + (radius * 0.5f) * sin(hourAngle).toFloat()
            ),
            strokeWidth = 6.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Minute hand
        val minuteAngle = (minute * 6) * (Math.PI / 180) - Math.PI / 2
        drawLine(
            color = onSurfaceColor.copy(alpha = 0.7f),
            start = center,
            end = Offset(
                center.x + (radius * 0.8f) * cos(minuteAngle).toFloat(),
                center.y + (radius * 0.8f) * sin(minuteAngle).toFloat()
            ),
            strokeWidth = 4.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Second hand
        val secondAngle = (second * 6) * (Math.PI / 180) - Math.PI / 2
        drawLine(
            color = primaryColor,
            start = center,
            end = Offset(
                center.x + (radius * 0.9f) * cos(secondAngle).toFloat(),
                center.y + (radius * 0.9f) * sin(secondAngle).toFloat()
            ),
            strokeWidth = 2.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Center dot
        drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = center)
    }
}

@Composable
fun WeatherTileContent(tile: HomeTile) {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalAlignment = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (tile.size == TileSize.SMALL || tile.size == TileSize.MEDIUM) Arrangement.Center else Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Rounded.WbSunny,
                contentDescription = null,
                modifier = Modifier.size(if (tile.size == TileSize.LARGE) 64.dp else 48.dp),
                tint = Color(0xFFFFD700)
            )
            if (tile.size != TileSize.SMALL) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "72°",
                    style = when(tile.size) {
                        TileSize.LARGE -> MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp)
                        TileSize.WIDE -> MaterialTheme.typography.displayMedium
                        else -> MaterialTheme.typography.headlineLarge
                    },
                    fontWeight = FontWeight.W200
                )
            }
        }
        
        if (tile.size != TileSize.SMALL) {
            Text(
                text = "Sunny",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cupertino, CA",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
            Spacer(modifier = Modifier.height(16.dp))
            
            if (tile.size == TileSize.LARGE) {
                // Hourly forecast for Large tile
                Text(
                    text = "Hourly Forecast",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HourlyItem("1PM", "72°", Icons.Rounded.WbSunny)
                    HourlyItem("2PM", "73°", Icons.Rounded.WbSunny)
                    HourlyItem("3PM", "74°", Icons.Rounded.WbSunny)
                    HourlyItem("4PM", "72°", Icons.Rounded.Cloud)
                    HourlyItem("5PM", "70°", Icons.Rounded.Cloud)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Daily forecast
            Text(
                text = "Next 5 Days",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ForecastItem("Mon", "72°", Icons.Rounded.WbSunny)
                ForecastItem("Tue", "75°", Icons.Rounded.WbSunny)
                ForecastItem("Wed", "68°", Icons.Rounded.WbSunny)
                ForecastItem("Thu", "70°", Icons.Rounded.Cloud)
                ForecastItem("Fri", "74°", Icons.Rounded.WbSunny)
            }
        }
    }
}

@Composable
fun HourlyItem(time: String, temp: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFFFFD700))
        Text(temp, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ForecastItem(day: String, temp: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFFFFD700))
        Text(temp, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PhotoLiveTile(tile: HomeTile) {
    val context = LocalContext.current
    val photos = remember { mutableStateListOf<android.net.Uri>() }
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            var count = 0
            while (cursor.moveToNext() && count < 10) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                photos.add(contentUri)
                count++
            }
        }
        
        while (photos.isNotEmpty()) {
            delay(5000)
            currentIndex = (currentIndex + 1) % photos.size
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (photos.isNotEmpty()) {
            AnimatedContent(
                targetState = photos[currentIndex],
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "photoTransition"
            ) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            // Placeholder if no photos
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No Photos", style = MaterialTheme.typography.labelSmall)
            }
        }
        
        // App label overlay for photos
        if (tile.size != TileSize.SMALL) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = tile.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun RichNotificationContent(tile: HomeTile) {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon
            val icon = when (tile.label.lowercase()) {
                "gmail", "mail" -> FluentIcons.Mail
                "messaging", "messages" -> FluentIcons.Message
                else -> Icons.Rounded.Notifications
            }
            
            FluentIcon(
                imageVector = icon,
                contentDescription = null,
                size = 18.dp,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tile.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (tile.notificationSender != null) {
            Text(
                text = tile.notificationSender!!,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tile.notificationContent ?: tile.notificationSummary ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (tile.size == TileSize.LARGE) 10 else 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "No new messages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}
