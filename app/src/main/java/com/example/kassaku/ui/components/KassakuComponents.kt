package com.example.kassaku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.kassaku.R
import com.example.kassaku.ui.theme.StitchSurfaceDark
import com.example.kassaku.ui.theme.StitchTextPrimary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.kassaku.ui.theme.StitchAccentRed
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchSurfaceLight
import com.example.kassaku.ui.theme.StitchTextSecondary
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun IconButtonSmall(
    icon: ImageVector,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isDark) StitchSurfaceDark else Color.White)
            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isDark) Color.White else StitchTextPrimary
        )
    }
}


fun formatCurrencyFlexible(
    amount: Number,
    currencyCode: String = "IDR",
    formatMode: String = "compact"
): String {
    if (formatMode.equals("standard", ignoreCase = true)) {
        return formatCurrencyExact(amount.toDouble(), currencyCode)
    }

    val absAmount = abs(amount.toDouble())
    val symbol = when (currencyCode.uppercase()) {
        "USD" -> "$"
        "MYR" -> "RM"
        "EUR" -> "€"
        "SGD" -> "S$"
        else -> "Rp"
    }

    return when {
        absAmount >= 1_000_000_000 -> {
            val formatted = String.format("%.1f", amount.toDouble() / 1_000_000_000).replace(".0", "")
            "$symbol $formatted miliar"
        }
        absAmount >= 100_000_000 -> {
            val formatted = String.format("%.1f", amount.toDouble() / 1_000_000).replace(".0", "")
            "$symbol $formatted jt"
        }
        else -> {
            formatCurrencyExact(amount.toDouble(), currencyCode)
        }
    }
}

fun formatCurrencyExact(amount: Double, currencyCode: String = "IDR"): String {
    val locale = when (currencyCode.uppercase()) {
        "USD" -> Locale("en", "US")
        "MYR" -> Locale("ms", "MY")
        "EUR" -> Locale("fr", "FR")
        "SGD" -> Locale("en", "SG")
        else -> Locale("in", "ID")
    }

    val format = NumberFormat.getCurrencyInstance(locale)
    if (currencyCode.uppercase() == "IDR") {
        format.maximumFractionDigits = 0
    } else {
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
    }
    
    var result = format.format(amount)
    
    // Replace default currency symbols with our preferred ones if needed
    result = result.replace("Rp", "Rp ")
                   .replace("US$", "$ ")
                   .replace("$", "$ ")
                   .replace("RM", "RM ")
                   .replace("SGD", "S$ ")
                   .replace("€", "€ ")
                   // Clean up double spaces
                   .replace("  ", " ")
                   .trim()
                   
    return result
}
@Composable
fun LogoutConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    isDark: Boolean
) {
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Help,
                contentDescription = null,
                tint = StitchPrimary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Apakah yakin ingin logout?",
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = textPrimary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ya, Keluar!", color = StitchTextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal", color = StitchAccentRed, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = surfaceColor
    )
}

/**
 * Reusable premium empty-state illustration composable.
 * Renders a glowing background, a bouncing leather wallet with a gold snap flap, a sticking out white receipt,
 * and a falling cash bill matching the web platform's aesthetic precisely.
 */
@Composable
fun EmptyStateLottie(
    message: String,
    subtitle: String? = null,
    animationSize: Dp = 120.dp,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wallet_illustration")
    
    // Slow bouncing animation for the wallet Y translation
    val walletHoverY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wallet_hover"
    )
    
    // Smooth breathing scale for the wallet
    val walletScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wallet_scale"
    )

    // Pulsing background glow aura alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Coin/Cash drop keyframes
    val cashDropY by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cash_drop_y"
    )

    val cashAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2200
                0f at 0 using LinearEasing
                1f at 660 using LinearEasing // 30% duration
                1f at 1100 using LinearEasing // 50% duration
                0f at 1540 using LinearEasing // 70% duration
                0f at 2200 using LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "cash_alpha"
    )

    val labelColor = if (isDark) Color.White else StitchTextPrimary
    val secondaryColor = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
    val primaryGlowColor = StitchPrimary

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visual envelope container
        Box(
            modifier = Modifier.size(animationSize * 1.33f),
            contentAlignment = Alignment.Center
        ) {
            // Pulse Glow Background Sphere
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryGlowColor.copy(alpha = glowAlpha),
                            primaryGlowColor.copy(alpha = 0f)
                        )
                    ),
                    radius = size.width / 2.2f
                )
            }

            // Falling Cash Bill (Behind wallet)
            Text(
                text = "💵",
                fontSize = 26.sp,
                modifier = Modifier
                    .offset(y = cashDropY.dp)
                    .graphicsLayer {
                        alpha = cashAlpha
                    }
            )

            // Bouncing Leather Wallet Card
            Box(
                modifier = Modifier
                    .offset(y = walletHoverY.dp)
                    .graphicsLayer {
                        scaleX = walletScale
                        scaleY = walletScale
                    }
                    .width(96.dp)
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Sticking out receipt paper
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 16.dp, y = (-12).dp)
                        .graphicsLayer {
                            rotationZ = 6f
                        }
                        .width(32.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDark) Color(0xFFF1F5F9) else Color.White)
                        .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(2.dp))
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFCBD5E1)))
                    Box(modifier = Modifier.fillMaxWidth(0.75f).height(1.dp).background(Color(0xFFCBD5E1)))
                    Box(modifier = Modifier.fillMaxWidth(0.5f).height(1.dp).background(Color(0xFFCBD5E1)))
                }

                // Wallet Main Body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFB45309), // amber-700
                                    Color(0xFF78350F)  // amber-900
                                )
                            )
                        )
                        .border(BorderStroke(4.dp, Color(0xFFD97706)), RoundedCornerShape(20.dp)) // border-amber-600
                ) {
                    // Inner pocket seam line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-16).dp)
                            .background(Color(0x40000000))
                    )
                }

                // Wallet Button Flap
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 0.dp, y = 8.dp)
                        .width(32.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                        .background(Color(0xFF92400E)) // amber-800
                        .border(
                            BorderStroke(2.dp, Color(0xFFD97706)), // border-amber-600
                            RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Golden Snap Button
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBBF24)) // amber-400
                            .border(BorderStroke(1.dp, Color(0xFFFDE68A)), CircleShape) // border-amber-300
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message Title
        Text(
            text = message,
            fontWeight = FontWeight.Black,
            color = labelColor,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontWeight = FontWeight.Bold,
                color = secondaryColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
