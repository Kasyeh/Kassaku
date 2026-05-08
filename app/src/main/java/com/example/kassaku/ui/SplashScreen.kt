package com.example.kassaku.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.R
import com.example.kassaku.ui.theme.StitchPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    // Animation States
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "Alpha"
    )
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500) // Ensure user sees the branding
        onSplashFinished()
    }

    // Background Gradient (Subtle and Premium)
    val bgBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Container with soft shadow
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
                    .shadow(
                        elevation = if (isDark) 0.dp else 20.dp,
                        shape = CircleShape,
                        spotColor = StitchPrimary.copy(alpha = 0.2f)
                    )
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E293B) else Color.White),
                contentAlignment = Alignment.Center
            ) {
                 Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo KasSaku",
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Text
            Text(
                text = "KasSaku",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1E293B),
                modifier = Modifier.alpha(alphaAnim.value)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Kelola uangmu dengan mudah",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                modifier = Modifier.alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Custom Thin Loader (iOS Style)
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .alpha(if (startAnimation) 1f else 0f),
                color = StitchPrimary,
                strokeWidth = 2.5.dp,
                trackColor = StitchPrimary.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
        
        // Version Footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(alphaAnim.value)
        ) {
            Text(
                text = "v1.0.0",
                fontSize = 12.sp,
                color = if(isDark) Color.DarkGray else Color.LightGray
            )
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen {}
}
