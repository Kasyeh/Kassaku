package com.example.kassaku.ui.components.skeleton

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.kassaku.utils.isEmulator

/**
 * Shimmer effect configuration for skeleton loading.
 * Uses subtle opacity range (0.08 - 0.18) for professional appearance.
 */
object ShimmerConfig {
    const val ANIMATION_DURATION_MS = 1100
    const val SHIMMER_WIDTH = 200f
}

/**
 * Creates an animated shimmer brush for skeleton loading effect.
 * Animation moves horizontally from left to right.
 */
@Composable
fun rememberShimmerBrush(
    isDarkTheme: Boolean = isSystemInDarkTheme()
): Brush {
    val shimmerColors = if (isDarkTheme) {
        listOf(
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.05f)
        )
    } else {
        listOf(
            Color.Black.copy(alpha = 0.06f),
            Color.Black.copy(alpha = 0.14f),
            Color.Black.copy(alpha = 0.06f)
        )
    }

    val isEmulator = remember { isEmulator() }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by if (isEmulator) {
        remember { mutableStateOf(500f) }
    } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = ShimmerConfig.ANIMATION_DURATION_MS,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerOffset"
        )
    }

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - ShimmerConfig.SHIMMER_WIDTH, 0f),
        end = Offset(translateAnimation, 0f)
    )
}

/**
 * Creates a static skeleton brush without animation.
 * Use for testing or performance-critical scenarios.
 */
@Composable
fun rememberStaticSkeletonBrush(
    isDarkTheme: Boolean = isSystemInDarkTheme()
): Brush {
    val color = if (isDarkTheme) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }
    return Brush.linearGradient(listOf(color, color))
}
