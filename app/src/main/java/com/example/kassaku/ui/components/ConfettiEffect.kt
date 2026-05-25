package com.example.kassaku.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color,
    var rotation: Float,
    var rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    colors: List<Color> = listOf(
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFF3B82F6), // Blue
        Color(0xFFEC4899), // Pink
        Color(0xFF8B5CF6)  // Violet
    )
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }

    // Initialize particles when active and size is known
    LaunchedEffect(isActive, size) {
        if (isActive && size.width > 0 && size.height > 0) {
            val newParticles = List(100) {
                Particle(
                    x = size.width / 2f + (Random.nextFloat() - 0.5f) * 100f,
                    y = size.height * 0.4f, // Start slightly above center
                    vx = (Random.nextFloat() - 0.5f) * 20f,
                    vy = -Random.nextFloat() * 25f - 10f, // Initial upward velocity
                    size = Random.nextFloat() * 15f + 10f,
                    color = colors.random(),
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 15f
                )
            }
            particles = newParticles
        }
    }

    // Animation loop
    LaunchedEffect(isActive, particles.isNotEmpty()) {
        if (isActive && particles.isNotEmpty()) {
            var lastFrameTime = System.nanoTime()
            withInfiniteAnimationFrameNanos { frameTimeNanos ->
                val dt = (frameTimeNanos - lastFrameTime) / 10_000_000f // time step
                lastFrameTime = frameTimeNanos

                particles = particles.mapNotNull { p ->
                    // Apply physics
                    p.vy += 0.5f * dt // Gravity
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.rotation += p.rotationSpeed * dt

                    // Keep particle if it's still on screen (with some margin)
                    if (p.y < size.height + 100f) {
                        p
                    } else {
                        null
                    }
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
    ) {
        particles.forEach { p ->
            withTransform({
                translate(p.x, p.y)
                rotate(p.rotation)
            }) {
                drawRect(
                    color = p.color,
                    size = androidx.compose.ui.geometry.Size(p.size, p.size / 2f)
                )
            }
        }
    }
}
