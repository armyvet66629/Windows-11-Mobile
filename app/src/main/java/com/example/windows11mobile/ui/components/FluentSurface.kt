package com.example.windows11mobile.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp

enum class FluentEffect {
    MICA,
    ACRYLIC,
    SMOKE
}

@Composable
fun FluentSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp), // Fluent 2 uses tighter corners for some elements, but 8dp is standard
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    alpha: Float = 0.7f,
    effect: FluentEffect = FluentEffect.ACRYLIC,
    blurRadius: Int = 30,
    noiseOpacity: Float = 0.03f,
    borderAlpha: Float = 0.2f,
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
                    FluentEffect.ACRYLIC -> 16.dp
                    FluentEffect.SMOKE -> 32.dp
                },
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(shape)
    ) {
        // 1. Background layer: Blur + Tint
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
                .background(
                    when (effect) {
                        FluentEffect.MICA -> {
                            color.copy(alpha = 0.8f)
                        }
                        FluentEffect.ACRYLIC -> {
                            color.copy(alpha = alpha)
                        }
                        FluentEffect.SMOKE -> {
                            if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.2f)
                        }
                    }
                )
        )

        // 2. Effect Layer: Noise and Luminosity
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

        // 3. Smoke Specific Overlay
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

        // 4. Fluent Border
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 0.5.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = borderAlpha),
                            Color.White.copy(alpha = borderAlpha * 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = shape
                )
        )

        // 5. Content layer
        Box(
            modifier = Modifier,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}
