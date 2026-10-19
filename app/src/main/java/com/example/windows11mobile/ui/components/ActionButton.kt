package com.example.windows11mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Any? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitle: String? = null,
    timestamp: String? = null,
    avatar: Any? = null
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (avatar != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    when (avatar) {
                        is ImageVector -> Icon(avatar, contentDescription = null, modifier = Modifier.size(20.dp).padding(4.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        is android.graphics.drawable.Drawable -> AsyncImage(model = avatar, contentDescription = null, modifier = Modifier.size(32.dp))
                        else -> Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(20.dp).padding(4.dp))
                    }
                }
            } else {
                when (icon) {
                    is ImageVector -> {
                        Icon(
                            imageVector = icon, 
                            contentDescription = null, 
                            modifier = Modifier.size(20.dp).padding(4.dp),
                            tint = contentColor
                        )
                    }
                    is android.graphics.drawable.Drawable -> {
                        AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(20.dp).padding(4.dp))
                    }
                    else -> {
                        Icon(Icons.AutoMirrored.Rounded.Launch, contentDescription = null, modifier = Modifier.size(20.dp).padding(4.dp), tint = contentColor)
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text, 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.Normal,
                    color = contentColor,
                    letterSpacing = (-0.1).sp
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (timestamp != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
