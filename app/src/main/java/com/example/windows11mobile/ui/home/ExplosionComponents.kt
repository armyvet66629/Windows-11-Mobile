package com.example.windows11mobile.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.windows11mobile.data.HomeTile
import com.example.windows11mobile.data.TileSize
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.theme.FluentIcons
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ExplodedTileView(
    tile: HomeTile,
    onDismiss: () -> Unit,
    onAppClick: (String) -> Unit,
    onResize: () -> Unit = {},
    onRemove: () -> Unit = {},
    onRename: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val subTiles = remember(tile) { getSubTilesForApp(tile, onAppClick, context, onResize, onRemove, onRename) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Deep background dim
        var bgVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { bgVisible = true }
        
        AnimatedVisibility(
            visible = bgVisible,
            enter = fadeIn(tween(600)),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }

        // Main Anchor Tile and Sub-tiles
        Box(contentAlignment = Alignment.Center) {
            // Sub-tiles glide outward in ripple style
            subTiles.forEachIndexed { index, subTile ->
                AnimatedSubTile(
                    data = subTile,
                    index = index,
                    onDismiss = onDismiss
                )
            }

            // Central Anchored Tile (Zoomed)
            val zoom by animateFloatAsState(
                targetValue = if (bgVisible) 1.15f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.6f),
                label = "mainZoom"
            )
            
            Box(
                modifier = Modifier
                    .scale(zoom)
                    .size(120.dp) // Uniform size for the circular anchor
                    .clip(CircleShape)
                    .zIndex(10f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            ) {
                HomeTileItem(
                    tile = tile.copy(size = TileSize.MEDIUM) // Force medium/square for circle
                )
            }
        }
    }
}

data class SubTileData(
    val label: String,
    val icon: Any,
    val action: () -> Unit,
    val color: Color = Color(0xFF0078D4)
)

@Composable
fun AnimatedSubTile(
    data: SubTileData,
    index: Int,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    
    LaunchedEffect(Unit) {
        delay(index * 90L) // Slower staggered ripple
        visible = true
    }

    // Optimized distance for mobile frames (narrow horizontal, taller vertical)
    val horizontalDist = with(density) { 110.dp.toPx() }
    val verticalDist = with(density) { 150.dp.toPx() }
    
    val targetOffset = when (index) {
        0 -> Offset(-horizontalDist, -verticalDist)
        1 -> Offset(horizontalDist, -verticalDist)
        2 -> Offset(-horizontalDist, verticalDist)
        else -> Offset(horizontalDist, verticalDist)
    }

    val glide by animateOffsetAsState(
        targetValue = if (visible) targetOffset else Offset.Zero,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.5f),
        label = "glide"
    )
    
    // Floating "Breath" animation for extra depth
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else when(index) {
            0 -> -15f
            1 -> 15f
            2 -> -15f
            else -> 15f
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "rotation"
    )

    val opacity by animateFloatAsState(if (visible) 1f else 0f, label = "opacity")
    val scale by animateFloatAsState(if (visible) 1f else 0.3f, label = "scale")

    Box(
        modifier = Modifier
            .offset { 
                IntOffset(
                    glide.x.roundToInt(), 
                    (glide.y + floatAnim).roundToInt() // Added breathing float animation
                ) 
            }
            .scale(scale)
            .graphicsLayer {
                alpha = opacity
                rotationZ = rotation
            }
    ) {
        ExplosionSubTile(data, onDismiss)
    }
}

@Composable
fun ExplosionSubTile(
    data: SubTileData,
    onDismiss: () -> Unit
) {
    // Light Reveal Effect Brush
    val revealBrush = Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
        radius = 400f,
        center = Offset(200f, 0f)
    )

    FluentSurface(
        modifier = Modifier
            .size(110.dp) // Smaller to stay in frame
            .clickable { 
                data.action()
                onDismiss()
            },
        shape = RoundedCornerShape(24.dp),
        alpha = 0.8f,
        effect = FluentEffect.ACRYLIC,
        blurRadius = 140, 
        tintColor = Color.Black.copy(alpha = 0.3f),
        luminosityAlpha = 0.25f,
        borderAlpha = 0.3f
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Static reveal highlight (simulating Fluent light reveal)
            Box(modifier = Modifier.fillMaxSize().background(revealBrush))
            
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(data.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    when (val icon = data.icon) {
                        is ImageVector -> Icon(icon, contentDescription = null, tint = data.color, modifier = Modifier.size(24.dp))
                        is String -> Text(icon, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = data.color)
                        else -> Icon(Icons.Rounded.Apps, contentDescription = null, tint = data.color)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = data.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun getSubTilesForApp(
    tile: HomeTile, 
    onAppClick: (String) -> Unit, 
    context: android.content.Context,
    onResize: () -> Unit = {},
    onRemove: () -> Unit = {},
    onRename: () -> Unit = {}
): List<SubTileData> {
    val pkg = tile.packageName ?: ""
    val pkgLower = pkg.lowercase()
    val openApp = { onAppClick(pkg) }

    val commonSubTiles = mutableListOf(
        SubTileData("Resize", FluentIcons.Open, onResize, Color(0xFF0078D4)),
        SubTileData("Remove from Start", FluentIcons.Delete, onRemove, Color(0xFFE74C3C))
    )
    
    if (tile.isFolder) {
        commonSubTiles.add(0, SubTileData("Rename", Icons.Rounded.Edit, onRename, Color(0xFF9B59B6)))
    }

    return when {
        pkgLower.contains("dialer") || pkgLower.contains("phone") -> listOf(
            SubTileData("New Call", Icons.Rounded.Call, openApp, Color(0xFF2ECC71)),
            SubTileData("Contacts", Icons.Rounded.Person, openApp, Color(0xFF2ECC71))
        ) + commonSubTiles
        pkgLower.contains("messaging") || pkgLower.contains("sms") -> listOf(
            SubTileData("Compose", Icons.Rounded.Edit, openApp, Color(0xFF3498DB)),
            SubTileData("Recent", Icons.AutoMirrored.Rounded.Chat, openApp, Color(0xFF3498DB))
        ) + commonSubTiles
        pkgLower.contains("photos") || tile.specialType == HomeTile.TYPE_PHOTOS -> listOf(
            SubTileData("Gallery", Icons.Rounded.PhotoLibrary, openApp, Color(0xFFF39C12)),
            SubTileData("Favorites", Icons.Rounded.Favorite, openApp, Color(0xFFE74C3C))
        ) + commonSubTiles
        pkgLower.contains("gmail") || pkgLower.contains("mail") -> listOf(
            SubTileData("Compose", Icons.Rounded.Edit, openApp, Color(0xFFE74C3C)),
            SubTileData("Inbox", Icons.Rounded.Email, openApp, Color(0xFFE74C3C))
        ) + commonSubTiles
        tile.specialType == HomeTile.TYPE_WEATHER || tile.specialType == HomeTile.TYPE_CLOCK_WEATHER -> {
            val openWeather = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))
                    context.startActivity(intent)
                } catch (e: Exception) {}
            }
            val openAlarms = {
                try {
                    context.startActivity(Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS))
                } catch (e: Exception) {}
            }
            
            listOf(
                SubTileData("Weather", Icons.Rounded.Cloud, openWeather, Color(0xFF3498DB)),
                SubTileData("Alarms", Icons.Rounded.Alarm, openAlarms, Color(0xFF9B59B6)),
                SubTileData("Resize", FluentIcons.Open, onResize, Color(0xFFE67E22)),
                SubTileData("Remove from Start", FluentIcons.Delete, onRemove, Color(0xFF7F8C8D))
            )
        }
        else -> listOf(
            SubTileData("Open", Icons.AutoMirrored.Rounded.Launch, openApp),
            SubTileData("Resize", FluentIcons.Open, onResize),
            SubTileData("Remove from Start", FluentIcons.Delete, onRemove, Color(0xFFE74C3C)),
            SubTileData("Share", Icons.Rounded.Share, openApp)
        )
    }
}
