package com.example.windows11mobile.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import android.content.ContentUris
import android.provider.MediaStore
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.windows11mobile.ui.theme.FluentIcons
import com.example.windows11mobile.ui.components.FluentIcon
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
    val dateFormat = when (tile.size) {
        TileSize.SMALL -> null
        TileSize.MEDIUM -> SimpleDateFormat("EEE, d", Locale.getDefault())
        TileSize.WIDE, TileSize.LARGE -> SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (tile.size == TileSize.LARGE) {
            Box(modifier = Modifier.size(240.dp)) {
                AnalogClock(currentTime)
            }
            
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = if (tile.size == TileSize.WIDE) Alignment.Start else Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = timeFormat.format(currentTime),
                    style = when(tile.size) {
                        TileSize.SMALL -> MaterialTheme.typography.titleLarge
                        TileSize.MEDIUM -> MaterialTheme.typography.displayMedium
                        TileSize.WIDE -> MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp)
                        else -> MaterialTheme.typography.displayLarge
                    },
                    fontWeight = FontWeight.W300,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                if (tile.size != TileSize.SMALL) {
                    dateFormat?.let {
                        Text(
                            text = it.format(currentTime),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = if (tile.size == TileSize.WIDE) TextAlign.Start else TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClockWeatherTileContent(
    tile: HomeTile,
    weatherData: com.example.windows11mobile.data.WeatherData? = null,
    onWeatherClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable {
                    try {
                        context.startActivity(Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS))
                    } catch (e: Exception) {
                        val intent = context.packageManager.getLaunchIntentForPackage("com.android.deskclock")
                            ?: context.packageManager.getLaunchIntentForPackage("com.google.android.deskclock")
                        if (intent != null) context.startActivity(intent)
                    }
                }
        ) {
            Text(
                text = timeFormat.format(currentTime),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                fontWeight = FontWeight.W300,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateFormat.format(currentTime),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.clickable { onWeatherClick() }
        ) {
            val icon = getWeatherIcon(weatherData?.condition ?: "Unknown")
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (weatherData?.condition?.contains("Rain") == true) Color(0xFF3498DB) else Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${weatherData?.temperature?.toInt() ?: "--"}° ${weatherData?.condition ?: "Loading..."}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = weatherData?.locationName ?: "Waiting for location...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
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

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        drawCircle(
            color = onSurfaceColor.copy(alpha = 0.05f),
            radius = radius,
            center = center
        )

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

        drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = center)
    }
}

@Composable
fun WeatherTileContent(
    tile: HomeTile,
    weatherData: com.example.windows11mobile.data.WeatherData? = null,
    onWeatherClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp).clickable { onWeatherClick() },
        horizontalAlignment = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (tile.size == TileSize.SMALL || tile.size == TileSize.MEDIUM) Arrangement.Center else Arrangement.Start
        ) {
            val icon = getWeatherIcon(weatherData?.condition ?: "Unknown")
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (tile.size == TileSize.LARGE) 64.dp else 48.dp),
                tint = if (weatherData?.condition?.contains("Rain") == true) Color(0xFF3498DB) else Color(0xFFFFD700)
            )
            if (tile.size != TileSize.SMALL) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${weatherData?.temperature?.toInt() ?: "--"}°",
                    style = when(tile.size) {
                        TileSize.LARGE -> MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp)
                        TileSize.WIDE -> MaterialTheme.typography.displayMedium
                        else -> MaterialTheme.typography.headlineLarge
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        if (tile.size != TileSize.SMALL) {
            Text(
                text = weatherData?.condition ?: "Unknown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = weatherData?.locationName ?: "Detecting...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
            Spacer(modifier = Modifier.height(16.dp))
            
            if (tile.size == TileSize.LARGE && weatherData != null) {
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
                    weatherData.hourlyForecast.take(5).forEach { item ->
                        HourlyItem(item.time, "${item.temp.toInt()}°", getWeatherIcon(item.icon))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (weatherData != null) {
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
                    weatherData.dailyForecast.take(5).forEach { item ->
                        ForecastItem(item.day.substring(5), "${item.temp.toInt()}°", getWeatherIcon(item.icon))
                    }
                }
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

fun getWeatherIcon(condition: String): ImageVector {
    return when (condition) {
        "Rainy", "Rain Showers", "Drizzle" -> Icons.Rounded.Umbrella
        "Snowy" -> Icons.Rounded.AcUnit
        "Thunderstorm" -> Icons.Rounded.Thunderstorm
        "Foggy" -> Icons.Rounded.FilterDrama
        "Partly Cloudy" -> Icons.Rounded.Cloud
        else -> Icons.Rounded.WbSunny
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
        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                var count = 0
                while (cursor.moveToNext() && count < 20) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    photos.add(contentUri)
                    count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        while (photos.isNotEmpty()) {
            delay(6000)
            currentIndex = (currentIndex + 1) % photos.size
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (photos.isNotEmpty()) {
            AnimatedContent(
                targetState = photos[currentIndex],
                transitionSpec = {
                    (fadeIn(tween(1000)) + scaleIn(initialScale = 1.1f)) togetherWith fadeOut(tween(1000))
                },
                label = "photoTransition",
                modifier = Modifier.fillMaxSize()
            ) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            val pm = context.packageManager
            val appIcon = remember(tile.packageName) {
                tile.packageName?.let { pkg ->
                    try { pm.getApplicationIcon(pkg) } catch (_: Exception) { null }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    AsyncImage(
                        model = appIcon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        alpha = 0.6f
                    )
                } else {
                    Icon(
                        Icons.Rounded.Photo,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        
        if (tile.size != TileSize.SMALL) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tile.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PhoneLiveTile(tile: HomeTile, recentNotifications: List<com.example.windows11mobile.data.NotificationData> = emptyList()) {
    val items = recentNotifications.map { Triple(it.sender ?: "Unknown", it.content ?: "", null) }
    
    if (items.isNotEmpty()) {
        LiveTileList(
            tile = tile,
            items = items,
            icon = Icons.Rounded.Phone,
            headerColor = Color(0xFF2ECC71)
        )
    } else {
        StandardTileContent(tile, null)
    }
}

@Composable
fun MessagesLiveTile(tile: HomeTile, recentNotifications: List<com.example.windows11mobile.data.NotificationData> = emptyList()) {
    val items = recentNotifications.map { Triple(it.sender ?: "Unknown", it.content ?: "", null) }
    
    if (items.isNotEmpty()) {
        LiveTileList(
            tile = tile,
            items = items,
            icon = FluentIcons.Message,
            headerColor = Color(0xFF3498DB)
        )
    } else {
        StandardTileContent(tile, null)
    }
}

@Composable
fun GmailLiveTile(tile: HomeTile, recentNotifications: List<com.example.windows11mobile.data.NotificationData> = emptyList()) {
    val items = recentNotifications.map { Triple(it.sender ?: "Unknown", it.content ?: "", null) }
    
    if (items.isNotEmpty()) {
        LiveTileList(
            tile = tile,
            items = items,
            icon = FluentIcons.Mail,
            headerColor = Color(0xFFE74C3C)
        )
    } else {
        StandardTileContent(tile, null)
    }
}

@Composable
fun FlippingTileContainer(
    isLive: Boolean,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    forceBack: Boolean = false
) {
    var isFlipped by remember { mutableStateOf(false) }
    
    LaunchedEffect(forceBack) {
        if (forceBack) isFlipped = true
    }

    if (isLive && !forceBack) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(5000 + (Math.random() * 5000).toLong())
                isFlipped = !isFlipped
            }
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "tileFlip"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationX = rotation
                cameraDistance = 8f * density
            }
    ) {
        if (rotation <= 90f) {
            Box(Modifier.fillMaxSize()) {
                front()
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { 
                        rotationX = 180f 
                    }
            ) {
                back()
            }
        }
    }
}

@Composable
fun PeopleTileBack(contacts: List<com.example.windows11mobile.data.Contact>) {
    var currentIndex by remember { mutableIntStateOf(0) }
    
    if (contacts.isNotEmpty()) {
        LaunchedEffect(contacts) {
            while (true) {
                delay(4000)
                currentIndex = (currentIndex + 1) % contacts.size
            }
        }

        val contact = contacts[currentIndex]
        
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (contact.photoUri != null) 64.dp else 48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (contact.photoUri != null) {
                        AsyncImage(
                            model = contact.photoUri,
                            contentDescription = contact.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = contact.name.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun MusicLiveTile(
    tile: HomeTile, 
    media: com.example.windows11mobile.data.MediaData? = null,
    onPlayPause: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {}
) {
    val brandingColor = remember(tile.packageName) {
        when {
            tile.packageName?.contains("youtube.music") == true -> Color(0xFFFF0000)
            tile.packageName?.contains("spotify") == true -> Color(0xFF1DB954)
            else -> Color(0xFFE91E63)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (media?.albumArt != null) {
            Image(
                bitmap = media.albumArt.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.5f),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(brandingColor.copy(alpha = 0.2f), Color.Transparent))
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FluentIcon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    size = 18.dp,
                    tint = brandingColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (media?.isPlaying == true) "NOW PLAYING" else tile.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = brandingColor,
                    letterSpacing = 1.2.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (media != null && media.title != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                    Text(
                        text = media.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }

                if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onSkipPrevious) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = null, tint = Color.White)
                        }
                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                if (media.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(onClick = onSkipNext) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            } else {
                StandardTileContent(tile, null)
            }
        }
    }
}

@Composable
fun SettingsLiveTile(tile: HomeTile) {
    var wifiEnabled by remember { mutableStateOf(true) }
    var bluetoothEnabled by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {
        Text(
            text = "QUICK SETTINGS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickToggle(
                icon = if (wifiEnabled) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                enabled = wifiEnabled,
                onClick = { wifiEnabled = !wifiEnabled },
                modifier = Modifier.weight(1f)
            )
            QuickToggle(
                icon = if (bluetoothEnabled) Icons.Rounded.Bluetooth else Icons.Rounded.BluetoothDisabled,
                enabled = bluetoothEnabled,
                onClick = { bluetoothEnabled = !bluetoothEnabled },
                modifier = Modifier.weight(1f)
            )
        }
        
        if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickToggle(
                    icon = Icons.Rounded.Flight,
                    enabled = false,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                QuickToggle(
                    icon = Icons.Rounded.FlashlightOn,
                    enabled = false,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickToggle(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.05f)
            )
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun LiveTileList(
    tile: HomeTile,
    items: List<Triple<String, String, String?>>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    headerColor: Color
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(items) {
        while (items.size > 1) {
            delay(4000)
            currentIndex = (currentIndex + 1) % items.size
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FluentIcon(
                imageVector = icon,
                contentDescription = null,
                size = 18.dp,
                tint = headerColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            val titleText = if (tile.notificationCount > 0) "${tile.label.uppercase()} (${tile.notificationCount})" else tile.label.uppercase()
            Text(
                text = titleText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = headerColor
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = items[currentIndex],
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            },
            label = "liveTileTransition"
        ) { (title, subtitle, photo) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(headerColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photo != null) {
                        AsyncImage(
                            model = photo,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = title.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = headerColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = if (tile.size == TileSize.LARGE) 4 else 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun YouTubeLiveTile(
    tile: HomeTile,
    media: com.example.windows11mobile.data.MediaData? = null,
    recentNotifications: List<com.example.windows11mobile.data.NotificationData> = emptyList()
) {
    val isYouTubeMedia = media?.packageName?.contains("youtube") == true
    val displayTitle = if (isYouTubeMedia) media!!.title else recentNotifications.firstOrNull()?.content ?: recentNotifications.firstOrNull()?.summary
    val displayArtist = if (isYouTubeMedia) media!!.artist else recentNotifications.firstOrNull()?.sender
    val albumArt = if (isYouTubeMedia) media!!.albumArt else null

    Box(modifier = Modifier.fillMaxSize()) {
        if (albumArt != null) {
            Image(
                bitmap = albumArt.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.4f),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Red.copy(alpha = 0.2f), Color.Transparent))
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isYouTubeMedia && media?.isPlaying == true) "NOW PLAYING" else "LAST WATCHED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Red,
                    letterSpacing = 1.2.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (displayTitle != null) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
                if (displayArtist != null) {
                    Text(
                        text = displayArtist,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                StandardTileContent(tile, null)
            }
        }
    }
}

@Composable
fun DateBackSide() {
    val date = Date()
    val dayFormat = SimpleDateFormat("d", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
    val weekdayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = weekdayFormat.format(date).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = dayFormat.format(date),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.W200,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = monthFormat.format(date),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun FolderTileContent(tile: HomeTile) {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val columns = 2
            val iconSize = if (tile.size == TileSize.SMALL) 12.dp else 24.dp
            val spacing = if (tile.size == TileSize.SMALL) 4.dp else 8.dp
            
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                items(tile.subTiles.take(4)) { subTile ->
                    val context = LocalContext.current
                    val icon = remember(subTile.packageName) {
                        subTile.packageName?.let { pkg ->
                            try { context.packageManager.getApplicationIcon(pkg) } catch (_: Exception) { null }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (icon != null) {
                            AsyncImage(
                                model = icon,
                                contentDescription = null,
                                modifier = Modifier.size(iconSize)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(iconSize * 0.8f),
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
        
        if (tile.size != TileSize.SMALL) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tile.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
