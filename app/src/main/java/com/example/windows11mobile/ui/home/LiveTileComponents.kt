package com.example.windows11mobile.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.windows11mobile.data.HomeTile
import com.example.windows11mobile.data.TileSize
import com.example.windows11mobile.data.Contact
import com.example.windows11mobile.ui.components.FluentIcon
import com.example.windows11mobile.ui.theme.FluentIcons
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@Composable
fun ClockTileContent(tile: HomeTile) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                try {
                    context.startActivity(Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS))
                } catch (e: Exception) {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.android.deskclock")
                        ?: context.packageManager.getLaunchIntentForPackage("com.google.android.deskclock")
                    if (intent != null) context.startActivity(intent)
                }
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
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
        verticalAlignment = Alignment.Bottom,
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
                            ?: context.packageManager.getLaunchIntentForPackage("com.android.deskclock")
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
            modifier = Modifier
                .clickable { onWeatherClick() }
                .padding(bottom = 20.dp) // Move info higher
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
fun WeatherForecastBack(weatherData: com.example.windows11mobile.data.WeatherData?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Cloud,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "5-DAY FORECAST",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val forecast = weatherData?.dailyForecast?.take(5) ?: emptyList()
            if (forecast.isEmpty()) {
                Text(
                    "Forecast data unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                forecast.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f) // Equal distribution for perfect alignment
                    ) {
                        Text(
                            text = day.day,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = getWeatherIcon(day.icon),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = if (day.icon.contains("Rain")) Color(0xFF3498DB) else Color(0xFFFFD700)
                        )
                        Text(
                            text = "${day.temp.toInt()}°",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.9f
        
        // Face
        drawCircle(color = Color.White.copy(alpha = 0.1f), radius = radius, center = center)
        
        // Hour Hand
        val hourAngle = (hour + minute / 60f) * 30f - 90f
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(
                center.x + radius * 0.5f * Math.cos(Math.toRadians(hourAngle.toDouble())).toFloat(),
                center.y + radius * 0.5f * Math.sin(Math.toRadians(hourAngle.toDouble())).toFloat()
            ),
            strokeWidth = 6.dp.toPx()
        )
        
        // Minute Hand
        val minuteAngle = (minute + second / 60f) * 6f - 90f
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(
                center.x + radius * 0.7f * Math.cos(Math.toRadians(minuteAngle.toDouble())).toFloat(),
                center.y + radius * 0.7f * Math.sin(Math.toRadians(minuteAngle.toDouble())).toFloat()
            ),
            strokeWidth = 4.dp.toPx()
        )
        
        // Second Hand
        val secondAngle = second * 6f - 90f
        drawLine(
            color = Color.Red,
            start = center,
            end = Offset(
                center.x + radius * 0.8f * Math.cos(Math.toRadians(secondAngle.toDouble())).toFloat(),
                center.y + radius * 0.8f * Math.sin(Math.toRadians(secondAngle.toDouble())).toFloat()
            ),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun WeatherTileContent(
    tile: HomeTile,
    weatherData: com.example.windows11mobile.data.WeatherData? = null,
    onWeatherClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onWeatherClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = weatherData?.locationName ?: "Loading...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = weatherData?.condition ?: "Checking skies",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            val icon = getWeatherIcon(weatherData?.condition ?: "Unknown")
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (weatherData?.condition?.contains("Rain") == true) Color(0xFF3498DB) else Color(0xFFFFD700)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${weatherData?.temperature?.toInt() ?: "--"}°",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.W200,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "H: ${weatherData?.highTemp?.toInt() ?: "--"}°",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "L: ${weatherData?.lowTemp?.toInt() ?: "--"}°",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weatherData?.hourlyForecast?.take(4)?.forEach { forecast ->
                    HourlyItem(forecast.time, "${forecast.temp.toInt()}°", getWeatherIcon(forecast.icon))
                }
            }
        }
    }
}

@Composable
fun HourlyItem(time: String, temp: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFFFFD700))
        Text(text = temp, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ForecastItem(day: String, temp: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = day, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color(0xFFFFD700))
        Text(text = temp, style = MaterialTheme.typography.labelMedium)
    }
}

fun getWeatherIcon(condition: String): ImageVector {
    return when {
        condition.contains("Cloud", true) -> Icons.Rounded.Cloud
        condition.contains("Rain", true) || condition.contains("Drizzle", true) -> Icons.Rounded.WaterDrop
        condition.contains("Snow", true) -> Icons.Rounded.AcUnit
        condition.contains("Thunder", true) -> Icons.Rounded.Thunderstorm
        condition.contains("Clear", true) || condition.contains("Sun", true) -> Icons.Rounded.WbSunny
        else -> Icons.Rounded.WbCloudy
    }
}

@Composable
fun PhotoLiveTile(tile: HomeTile) {
    var photoIndex by remember { mutableIntStateOf(0) }
    val photos = listOf(
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
        "https://images.unsplash.com/photo-1511884642898-4c92249e20b6",
        "https://images.unsplash.com/photo-1434725039720-abb26e22ebe1",
        "https://images.unsplash.com/photo-1470770841072-f978cf4d019e"
    )

    LaunchedEffect(Unit) {
        while(true) {
            delay(10000)
            photoIndex = (photoIndex + 1) % photos.size
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = photos[photoIndex],
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        if (tile.size != TileSize.SMALL) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Photos",
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
    
    // When forceBack changes, immediately sync isFlipped
    LaunchedEffect(forceBack) {
        if (forceBack) {
            isFlipped = true
        }
    }

    // Auto-flip logic only runs when not forced to back
    if (isLive) {
        LaunchedEffect(forceBack) {
            if (!forceBack) {
                while (true) {
                    delay(5000 + (Math.random() * 5000).toLong())
                    isFlipped = !isFlipped
                }
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
fun PeopleTileBack(contacts: List<Contact>) {
    val favorites = remember(contacts) { contacts.filter { it.isStarred }.take(9) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(favorites) { contact ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    if (contact.photoUri != null) {
                        AsyncImage(
                            model = contact.photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = contact.name.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
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
                                imageVector = if (media.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
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
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FluentIcon(
                imageVector = FluentIcons.Settings,
                contentDescription = null,
                size = 18.dp,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "QUICK SETTINGS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickToggle(icon = Icons.Rounded.Wifi, enabled = true, onClick = { })
            QuickToggle(icon = Icons.Rounded.Bluetooth, enabled = true, onClick = { })
            QuickToggle(icon = Icons.Rounded.FlashlightOn, enabled = false, onClick = { })
        }
        
        if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickToggle(icon = Icons.AutoMirrored.Rounded.AirplaneTicket, enabled = false, onClick = { })
                QuickToggle(icon = Icons.Rounded.ScreenRotation, enabled = true, onClick = { })
                QuickToggle(
                    icon = Icons.Rounded.BrightnessAuto, 
                    enabled = true, 
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
                if (photo != null) {
                    AsyncImage(
                        model = photo,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    recentNotifications: List<com.example.windows11mobile.data.NotificationData> = emptyList(),
    onPlayPause: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {}
) {
    val isYouTubeMedia = media?.packageName?.contains("youtube") == true
    val isPlaying = isYouTubeMedia && media?.isPlaying == true
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
                    text = if (isPlaying) "NOW PLAYING" else "YOUTUBE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Red,
                    letterSpacing = 1.2.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (displayTitle != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = if (tile.size == TileSize.LARGE) 4 else 2,
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
                }
                
                if (isYouTubeMedia && (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE)) {
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
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
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
                Box(modifier = Modifier.weight(1f)) {
                    StandardTileContent(tile, null)
                }
            }
        }
    }
}

@Composable
fun GenericNotificationLiveTile(tile: HomeTile, recentNotifications: List<com.example.windows11mobile.data.NotificationData> = emptyList()) {
    val items = recentNotifications.map { Triple(it.sender ?: tile.label, it.content ?: it.summary ?: "", null) }
    
    if (items.isNotEmpty()) {
        LiveTileList(
            tile = tile,
            items = items,
            icon = Icons.Rounded.Notifications,
            headerColor = MaterialTheme.colorScheme.primary
        )
    } else {
        StandardTileContent(tile, null)
    }
}

@Composable
fun NewsLiveTileBack(articles: List<com.example.windows11mobile.data.NewsArticle>) {
    var currentIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(articles) {
        while (articles.size > 1) {
            delay(5000)
            currentIndex = (currentIndex + 1) % articles.size
        }
    }

    if (articles.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No news available", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        val article = articles[currentIndex]
        Box(modifier = Modifier.fillMaxSize()) {
            if (article.urlToImage != null) {
                AsyncImage(
                    model = article.urlToImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.5f),
                    contentScale = ContentScale.Crop
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
            } else {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)))
            }

            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOP NEWS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = article.source.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
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
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
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

@Composable
fun StandardTileContent(tile: HomeTile, icon: android.graphics.drawable.Drawable?) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Alignment.Start else Alignment.CenterHorizontally, verticalArrangement = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Arrangement.Top else Arrangement.Center) {
        Box(modifier = if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) Modifier.size(32.dp) else Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (icon != null) AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(when (tile.size) { TileSize.SMALL -> 28.dp; TileSize.MEDIUM -> 64.dp; TileSize.WIDE -> 40.dp; TileSize.LARGE -> 72.dp }))
            else FluentIcon(imageVector = when (tile.label.lowercase()) { "settings" -> FluentIcons.Settings; "calendar" -> FluentIcons.Calendar; "people" -> Icons.Rounded.Person; "messaging" -> FluentIcons.Message; "phone" -> Icons.Rounded.Phone; "camera" -> Icons.Rounded.CameraAlt; "mail", "gmail" -> FluentIcons.Mail; "maps" -> Icons.Rounded.Map; "photos" -> FluentIcons.Photos; else -> FluentIcons.Apps }, contentDescription = null, size = when (tile.size) { TileSize.SMALL -> 28.dp; TileSize.MEDIUM -> 64.dp; TileSize.WIDE -> 40.dp; TileSize.LARGE -> 72.dp }, gradient = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
        }
        if (tile.size != TileSize.SMALL) {
            if (tile.size == TileSize.WIDE || tile.size == TileSize.LARGE) {
                Spacer(modifier = Modifier.height(8.dp)); Text(text = if (tile.notificationCount > 0) "${tile.label} (${tile.notificationCount})" else tile.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                if (tile.notificationSummary != null) { Spacer(modifier = Modifier.height(4.dp)); Text(text = tile.notificationSummary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = if (tile.size == TileSize.LARGE) 6 else 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface) }
                else { Spacer(modifier = Modifier.height(4.dp)); Text(text = "No new notifications", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) }
            } else { Text(text = if (tile.notificationCount > 0) "${tile.label} (${tile.notificationCount})" else tile.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}
