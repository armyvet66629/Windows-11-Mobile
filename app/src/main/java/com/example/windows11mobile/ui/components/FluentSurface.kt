package com.example.windows11mobile.ui.components

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
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
import java.util.Random

enum class FluentEffect {
    MICA,
    ACRYLIC,
    SMOKE,
    NONE
}

/**
 * A highly customizable Fluent Surface that implements the Acrylic and Mica effects.
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
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val random = Random(42)
        for (x in 0 until w) {
            for (y in 0 until h) {
                val brightness = random.nextInt(255)
                bitmap.setPixel(x, y, android.graphics.Color.argb(brightness, 255, 255, 255))
            }
        }
        bitmap.asImageBitmap()
    }

    val isDark = color.luminance() < 0.5f
    val blurIntensity = (blurRadius.toFloat() / 250f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .shadow(
                elevation = when (effect) {
                    FluentEffect.MICA -> 2.dp
                    FluentEffect.ACRYLIC -> (24.dp * (1f + blurIntensity)).coerceAtMost(48.dp)
                    FluentEffect.SMOKE -> 32.dp
                    FluentEffect.NONE -> 0.dp
                },
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(shape)
    ) {
        // 1. Background Blur Layer + Vibrant Saturation Chain
        if (effect != FluentEffect.NONE) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.graphicsLayer {
                                if (blurRadius > 0) {
                                    // Balanced Gaussian optimization
                                    val radiusMultiplier = 1.2f + (blurRadius.toFloat() / 500f)
                                    val radius = blurRadius.toFloat() * radiusMultiplier
                                    
                                    val blur = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                                    
                                    val matrix = ColorMatrix().apply {
                                        // Natural saturation to avoid blue/unnatural tints
                                        setSaturation(1.2f + (blurRadius.toFloat() / 1000f)) 
                                        
                                        // minimal contrast boost
                                        val contrast = 1.05f + (blurRadius.toFloat() / 1500f)
                                        val translate = (-0.5f * contrast + 0.5f) * 255f
                                        
                                        postConcat(
                                            ColorMatrix(floatArrayOf(
                                                contrast, 0f, 0f, 0f, translate,
                                                0f, contrast, 0f, 0f, translate,
                                                0f, 0f, contrast, 0f, translate,
                                                0f, 0f, 0f, 1f, 0f
                                            ))
                                        )
                                    }
                                    val colorFilter = RenderEffect.createColorFilterEffect(
                                        ColorMatrixColorFilter(matrix)
                                    )
                                    renderEffect = RenderEffect.createChainEffect(blur, colorFilter).asComposeRenderEffect()
                                }
                            }
                        } else {
                            Modifier.blur(blurRadius.dp)
                        }
                    )
            )
        }

        // 2. Gaussian Luminosity & Material Layer (The "Glow" behind the tint)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    if (isDark) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = (luminosityAlpha * (1.1f + blurIntensity * 0.2f)).coerceAtMost(0.45f)),
                                Color.Black.copy(alpha = (luminosityAlpha * (1.3f + blurIntensity * 0.3f)).coerceAtMost(0.55f))
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFD0D0D0).copy(alpha = (luminosityAlpha * (1.3f + blurIntensity * 0.3f)).coerceAtMost(0.5f)),
                                Color(0xFFB0B0B0).copy(alpha = (luminosityAlpha * (1.6f + blurIntensity * 0.4f)).coerceAtMost(0.6f))
                            )
                        )
                    }
                )
        )

        // 3. Tint & Material Layer (Acrylic vs Mica differentiation)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    when (effect) {
                        FluentEffect.MICA -> color.copy(alpha = (0.88f + blurIntensity * 0.05f).coerceAtMost(0.96f))
                        FluentEffect.ACRYLIC -> color.copy(alpha = (alpha * 0.85f).coerceAtLeast(0.1f))
                        FluentEffect.SMOKE -> if (isDark) Color.Black.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.45f)
                        FluentEffect.NONE -> color.copy(alpha = alpha)
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

        // 4. Noise/Texture Layer - Primary dither layer to prevent banding
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val shader = ImageShader(noiseBitmap, TileMode.Repeated, TileMode.Repeated)
                    val paint = Paint().apply {
                        this.shader = shader
                        this.alpha = noiseOpacity * 2.5f 
                        this.blendMode = if (isDark) BlendMode.Screen else BlendMode.Overlay
                    }
                    onDrawWithContent {
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

        // 6. Fluent Border (Defined Edge)
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.2.dp, // Thicker, clearly defined edge
                    brush = if (lightRevealPosition != null) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = lightRevealPosition,
                            radius = 300f
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = borderAlpha * 1.2f),
                                Color.White.copy(alpha = borderAlpha * 0.5f),
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
