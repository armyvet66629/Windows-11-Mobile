package com.example.windows11mobile.ui.components

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Random

// Reusable Noise Bitmap cache
private val noiseBitmap: ImageBitmap by lazy {
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

@Composable
fun rememberFluentBlurModifier(blurRadius: Int): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.graphicsLayer {
            if (blurRadius > 0) {
                val radius = blurRadius.toFloat() * 1.2f
                val blur = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                val matrix = ColorMatrix().apply {
                    setSaturation(1.15f)
                }
                val colorFilter = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(matrix))
                renderEffect = RenderEffect.createChainEffect(blur, colorFilter).asComposeRenderEffect()
            }
        }
    } else {
        Modifier.blur(blurRadius.dp)
    }
}

/**
 * 1. MicaSurface: Optimized for homescreen (8-12dp blur, low tint, subtle noise 3-5%, low motion).
 */
@Composable
fun MicaSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    blurRadius: Int = 10,
    tintColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
    noiseOpacity: Float = 0.04f,
    content: @Composable BoxScope.() -> Unit
) {
    val blurModifier = rememberFluentBlurModifier(blurRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .then(blurModifier)
            .background(tintColor)
            .drawBehind {
                val paint = Paint().apply {
                    color = Color.White
                    alpha = noiseOpacity
                }
                drawIntoCanvas { canvas ->
                    val patternWidth = noiseBitmap.width.toFloat()
                    val patternHeight = noiseBitmap.height.toFloat()
                    var x = 0f
                    while (x < size.width) {
                        var y = 0f
                        while (y < size.height) {
                            canvas.drawImage(noiseBitmap, Offset(x, y), paint)
                            y += patternHeight
                        }
                        x += patternWidth
                    }
                }
            }
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), shape),
        content = content
    )
}

/**
 * 2. AcrylicSurface: Optimized for app drawer (20-30dp blur, medium white/blue tint, 8-12% noise, open animation).
 */
@Composable
fun AcrylicSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    isOpen: Boolean = true,
    blurRadius: Int = 25,
    tintColor: Color = Color.White.copy(alpha = 0.35f),
    noiseOpacity: Float = 0.10f,
    content: @Composable BoxScope.() -> Unit
) {
    val animatedBlur by animateIntAsState(
        targetValue = if (isOpen) blurRadius else 0,
        animationSpec = tween(200, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "acrylicBlur"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = tween(200, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "acrylicAlpha"
    )

    val blurModifier = rememberFluentBlurModifier(animatedBlur)
    Box(
        modifier = modifier
            .graphicsLayer { alpha = animatedAlpha }
            .clip(shape)
            .then(blurModifier)
            .background(tintColor)
            .drawBehind {
                val paint = Paint().apply {
                    color = Color.White
                    alpha = noiseOpacity * animatedAlpha
                }
                drawIntoCanvas { canvas ->
                    val patternWidth = noiseBitmap.width.toFloat()
                    val patternHeight = noiseBitmap.height.toFloat()
                    var x = 0f
                    while (x < size.width) {
                        var y = 0f
                        while (y < size.height) {
                            canvas.drawImage(noiseBitmap, Offset(x, y), paint)
                            y += patternHeight
                        }
                        x += patternWidth
                    }
                }
            }
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), shape),
        content = content
    )
}

/**
 * 3. SmokeSurface: Optimized for quick settings & menus (10-16dp blur, dark desaturated tint 40-60%, slide-down animation).
 */
@Composable
fun SmokeSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    isVisible: Boolean = true,
    blurRadius: Int = 14,
    tintColor: Color = Color(0xFF1E1E1E).copy(alpha = 0.50f),
    noiseOpacity: Float = 0.025f,
    content: @Composable BoxScope.() -> Unit
) {
    val slideAnim by animateFloatAsState(
        targetValue = if (isVisible) 0f else -50f,
        animationSpec = tween(250, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "smokeSlide"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(250, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "smokeAlpha"
    )

    val blurModifier = rememberFluentBlurModifier(blurRadius)
    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = slideAnim
                alpha = alphaAnim
            }
            .clip(shape)
            .then(blurModifier)
            .background(tintColor)
            .drawBehind {
                val paint = Paint().apply {
                    color = Color.White
                    alpha = noiseOpacity
                }
                drawIntoCanvas { canvas ->
                    val patternWidth = noiseBitmap.width.toFloat()
                    val patternHeight = noiseBitmap.height.toFloat()
                    var x = 0f
                    while (x < size.width) {
                        var y = 0f
                        while (y < size.height) {
                            canvas.drawImage(noiseBitmap, Offset(x, y), paint)
                            y += patternHeight
                        }
                        x += patternWidth
                    }
                }
            }
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), shape),
        content = content
    )
}

/**
 * 4. TintedAcrylicSurface: Optimized for notification shade (16-22dp blur, medium tint 25-40%, medium noise, expansion animation).
 */
@Composable
fun TintedAcrylicSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
    isExpanded: Boolean = true,
    blurRadius: Int = 18,
    tintColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
    noiseOpacity: Float = 0.07f,
    content: @Composable BoxScope.() -> Unit
) {
    val expandAnim by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(280, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
        label = "tintedExpand"
    )

    val currentBlur = (blurRadius.toFloat() * expandAnim).toInt()
    val blurModifier = rememberFluentBlurModifier(currentBlur)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleY = 0.95f + (0.05f * expandAnim)
                alpha = expandAnim
            }
            .clip(shape)
            .then(blurModifier)
            .background(tintColor)
            .drawBehind {
                val paint = Paint().apply {
                    color = Color.White
                    alpha = noiseOpacity * expandAnim
                }
                drawIntoCanvas { canvas ->
                    val patternWidth = noiseBitmap.width.toFloat()
                    val patternHeight = noiseBitmap.height.toFloat()
                    var x = 0f
                    while (x < size.width) {
                        var y = 0f
                        while (y < size.height) {
                            canvas.drawImage(noiseBitmap, Offset(x, y), paint)
                            y += patternHeight
                        }
                        x += patternWidth
                    }
                }
            }
            .border(0.5.dp, Color.White.copy(alpha = 0.15f * expandAnim), shape),
        content = content
    )
}
