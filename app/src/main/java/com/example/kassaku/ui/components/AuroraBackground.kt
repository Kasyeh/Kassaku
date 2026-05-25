package com.example.kassaku.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import android.os.Build
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.utils.isEmulator
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    content: @Composable () -> Unit
) {
    // Colors for the blobs
    val primaryColor = StitchPrimary
    val secondaryColor = Color(0xFF0EA5E9) // Sky Blue
    val tertiaryColor = Color(0xFF8B5CF6) // Violet

    // Animate Blobs (Disabled on Emulator for Performance)
    val isEmulator = remember { isEmulator() }
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraInfinite")

    // Blob 1 Animation
    val float1 by if (isEmulator) {
        remember { mutableStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Blob1"
        )
    }

    // Blob 2 Animation
    val float2 by if (isEmulator) {
        remember { mutableStateOf(1f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(22000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Blob2"
        )
    }

    // Blob 3 Animation
    val float3 by if (isEmulator) {
        remember { mutableStateOf(2f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(18000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Blob3"
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        val baseColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        
        // Base Background
        Box(modifier = Modifier.fillMaxSize().background(baseColor))

        // Canvas for Blobs with Blur effect simulated via Brush gradients
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Blob 1 - Top Leftish
            val x1 = (width * 0.2f) + (cos(float1) * width * 0.1f)
            val y1 = (height * 0.2f) + (sin(float1) * height * 0.1f)
            drawAuroraBlob(
                color = primaryColor,
                center = Offset(x1.toFloat(), y1.toFloat()),
                radius = width * 0.6f,
                alpha = 0.25f
            )

            // Blob 2 - Center Rightish
            val x2 = (width * 0.8f) + (cos(float2) * width * 0.15f)
            val y2 = (height * 0.5f) + (sin(float2) * height * 0.1f)
            drawAuroraBlob(
                color = secondaryColor,
                center = Offset(x2.toFloat(), y2.toFloat()),
                radius = width * 0.5f,
                alpha = 0.2f
            )

            // Blob 3 - Bottom Leftish
            val x3 = (width * 0.3f) + (cos(float3) * width * 0.1f)
            val y3 = (height * 0.8f) + (sin(float3) * height * 0.15f)
            drawAuroraBlob(
                color = tertiaryColor,
                center = Offset(x3.toFloat(), y3.toFloat()),
                radius = width * 0.7f,
                alpha = 0.15f
            )
        }

        // Frost/Overlay to smooth everything out
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.4f),
                            baseColor.copy(alpha = 0.1f),
                            baseColor.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        content()
    }
}

private fun DrawScope.drawAuroraBlob(
    color: Color,
    center: Offset,
    radius: Float,
    alpha: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = 0f)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
