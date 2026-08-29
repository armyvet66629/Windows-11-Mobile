package com.example.windows11mobile.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp

enum class FluentEffect {
    MICA,
    ACRYLIC,
    SMOKE
}

/**
 * A highly customizable Fluent Surface that implements the Acrylic and Mica effects.
 * For high-fidelity Acrylic (like in Windows 11), use a high [blurRadius] (60-100)
 * and adjust [luminosityAlpha].
 */
@Composable
fun FluentSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    alpha: Float = 0.7f,
    effect: FluentEffect = FluentEffect.ACRYLIC,
    blurRadius: Int = 30,
    tintColor: Color = Color.Transparent,
    luminosityAlpha: Float = 0.12f,
    noiseOpacity: Float = 0.03f,
    borderAlpha: Float = 0.2f,
    lightRevealPosition: Offset? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val noiseBitmap = remember {
        val w = 128
        val h = 128
        val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        val random = java.util.Random(42)
        for (x in 0 until w) {
            for (y in 0 until h) {
                val brightness = random.nextInt(255)
                bitmap.setPixel(x, y, android.graphics.Color.argb(brightness, 255, 255, 255))
            }
        }
        bitmap.asImageBitmap()
    }

    val isDark = color.luminance() < 0.5f

    Box(
        modifier = modifier
            .shadow(
                elevation = when (effect) {
                    FluentEffect.MICA -> 2.dp
                    FluentEffect.ACRYLIC -> 24.dp // Increased for better depth
                    FluentEffect.SMOKE -> 32.dp
                },
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(shape)
    ) {
        // 1. Background Blur Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.graphicsLayer {
                            if (blurRadius > 0) {
                                val radius = blurRadius.toFloat()
                                renderEffect = RenderEffect.createBlurEffect(
                                    radius,
                                    radius,
                                    Shader.TileMode.CLAMP
                                ).asComposeRenderEffect()
                            }
                        }
                    } else {
                        Modifier.blur(blurRadius.dp)
                    }
                )
        )

        // 2. Luminosity Layer (The "Glow" behind the tint)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    if (isDark) Color.Black.copy(alpha = luminosityAlpha)
                    else Color.White.copy(alpha = luminosityAlpha)
                )
        )

        // 3. Tint Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    when (effect) {
                        FluentEffect.MICA -> color.copy(alpha = 0.8f)
                        FluentEffect.ACRYLIC -> color.copy(alpha = alpha)
                        FluentEffect.SMOKE -> if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.3f)
                    }
                )
        )

        // 3.1 Extra Darkening Tint if provided
        if (tintColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(tintColor)
            )
        }

        // 4. Noise/Texture Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val shader = ImageShader(noiseBitmap, TileMode.Repeated, TileMode.Repeated)
                    val paint = Paint().apply {
                        this.shader = shader
                        this.alpha = noiseOpacity
                        this.blendMode = if (isDark) BlendMode.Screen else BlendMode.Overlay
                    }
                    onDrawWithContent {
                        drawContent()
                        if (effect == FluentEffect.ACRYLIC || effect == FluentEffect.MICA) {
                            drawIntoCanvas { canvas ->
                                canvas.drawRect(0f, 0f, size.width, size.height, paint)
                            }
                        }
                    }
                }
        )

        // 5. Smoke Specific Gradient
        if (effect == FluentEffect.SMOKE) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
            )
        }

        // 6. Fluent Border (Inner Stroke)
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 0.8.dp, // Slightly thicker for definition
                    brush = if (lightRevealPosition != null) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.5f),
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            center = lightRevealPosition,
                            radius = 240f
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = borderAlpha),
                                Color.White.copy(alpha = borderAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    },
                    shape = shape
                )
        )

        // 7. Content layer
        Box(
            modifier = Modifier,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}
