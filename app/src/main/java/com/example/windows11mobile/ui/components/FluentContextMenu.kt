package com.example.windows11mobile.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.windows11mobile.data.BlurRepository

@Composable
fun FluentContextMenu(
    isVisible: Boolean,
    title: String,
    subtitle: String? = null,
    icon: Any? = null,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val blurAmount by BlurRepository.getInstance(context).blurAmount.collectAsStateWithLifecycle(initialValue = 20.dp)
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(150, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150, easing = FastOutSlowInEasing))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
        ) {
            // Fluent Translucent Blur Backdrop using blurAmount
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blurAmount)
                    .background(
                        if (isDark) Color.Black.copy(alpha = 0.20f) 
                        else Color.White.copy(alpha = 0.12f)
                    )
                    .clickable(onClick = onDismiss, interactionSource = null, indication = null)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, _ -> }
                    }
            )

            // Menu Panel (Auto-positioned with 12dp minimum margin from screen edges)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .width(340.dp)
                        .heightIn(max = 600.dp)
                        .shadow(30.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isDark) Color(0xFF1E1E1E).copy(alpha = 0.85f) 
                            else Color(0xFFFAFAFA).copy(alpha = 0.85f)
                        )
                        .clickable(enabled = false) {}
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, _ -> }
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(16.dp), // 16dp padding around entire menu
                        verticalArrangement = Arrangement.spacedBy(8.dp) // 8dp vertical spacing between menu items
                    ) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                when (icon) {
                                    is ImageVector -> Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp).padding(4.dp))
                                    is Drawable -> AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(48.dp))
                                    else -> Icon(Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(20.dp).padding(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-0.2).sp
                                )
                                if (subtitle != null) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Action Items & Sections
                        content()
                    }
                }
            }
        }
    }
}
