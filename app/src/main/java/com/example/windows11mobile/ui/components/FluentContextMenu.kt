package com.example.windows11mobile.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.windows11mobile.ui.theme.SmokeDark
import com.example.windows11mobile.ui.theme.SmokeLight

@Composable
fun FluentContextMenu(
    isVisible: Boolean,
    title: String,
    subtitle: String? = null,
    icon: Any? = null,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.95f, animationSpec = tween(150)),
        exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.95f, animationSpec = tween(120))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
        ) {
            // Backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) SmokeDark.copy(alpha = 0.8f) else SmokeLight.copy(alpha = 0.6f))
                    .clickable(onClick = onDismiss, interactionSource = null, indication = null)
            )

            // Menu Panel
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                FluentSurface(
                    modifier = Modifier
                        .width(340.dp)
                        .heightIn(max = 600.dp)
                        .padding(16.dp)
                        .clickable(enabled = false) {}, // Consume clicks
                    shape = RoundedCornerShape(32.dp),
                    alpha = if (isDark) 0.5f else 0.85f,
                    effect = FluentEffect.ACRYLIC,
                    blurRadius = 150,
                    tintColor = Color.Black.copy(alpha = 0.25f),
                    luminosityAlpha = 0.2f
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
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .shadow(if (!isDark) 4.dp else 0.dp, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                when (icon) {
                                    is ImageVector -> Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
                                    is android.graphics.drawable.Drawable -> AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(52.dp))
                                    else -> Icon(Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(28.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-0.5).sp
                                )
                                if (subtitle != null) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Action Items
                        content()
                    }
                }
            }
        }
    }
}
