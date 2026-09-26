package com.example.windows11mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Any? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    FluentSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        alpha = 0.12f,
        effect = FluentEffect.ACRYLIC,
        blurRadius = 40,
        tintColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFD0D0D0).copy(alpha = 0.15f),
        luminosityAlpha = 0.08f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (icon) {
                is ImageVector -> {
                    FluentIcon(
                        imageVector = icon, 
                        contentDescription = null, 
                        size = 22.dp,
                        gradient = if (contentColor == MaterialTheme.colorScheme.onSurface) {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        } else null,
                        tint = contentColor
                    )
                }
                is android.graphics.drawable.Drawable -> {
                    AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(22.dp))
                }
                else -> {
                    Icon(Icons.AutoMirrored.Rounded.Launch, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}
