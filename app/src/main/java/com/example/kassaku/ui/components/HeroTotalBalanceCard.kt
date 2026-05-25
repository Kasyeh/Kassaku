package com.example.kassaku.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.PremiumShadowSecondary
import com.example.kassaku.ui.theme.StitchAccentRed
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchPrimaryDark
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private fun currencySymbol(currencyCode: String): String = when (currencyCode.uppercase()) {
    "USD" -> "$"
    "MYR" -> "RM"
    "EUR" -> "€"
    "SGD" -> "S$"
    else -> "Rp"
}

private fun formatHeroAmount(amount: Double, currencyCode: String): String {
    val locale = when (currencyCode.uppercase()) {
        "USD" -> Locale("en", "US")
        "MYR" -> Locale("ms", "MY")
        "EUR" -> Locale("fr", "FR")
        "SGD" -> Locale("en", "SG")
        else -> Locale("in", "ID")
    }
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = if (currencyCode.uppercase() == "IDR") 0 else 2
        minimumFractionDigits = if (currencyCode.uppercase() == "IDR") 0 else 2
    }
    return formatter.format(abs(amount))
}

@Composable
fun HeroTotalBalanceCard(
    balance: Double,
    income: Double,
    expense: Double,
    targetExpense: Double?,
    currencyCode: String,
    currencyFormat: String,
    isBalanceVisible: Boolean,
    isExpenseHigherThanIncome: Boolean,
    overspendingAlpha: Float,
    isDark: Boolean,
    onToggleVisibility: () -> Unit,
    onExpenseSectionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    val scaleIn by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "heroScale"
    )

    val amountGradient = Brush.linearGradient(
        colors = listOf(
            Color.White,
            Color(0xFFECFDF5),
            Color(0xFF6EE7B7)
        )
    )
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF059669),
            StitchPrimary,
            StitchPrimaryDark,
            Color(0xFF0369A1)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scaleIn)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(36.dp),
                spotColor = StitchPrimary.copy(alpha = if (isDark) 0.45f else 0.35f),
                ambientColor = PremiumShadowSecondary
            )
            .clip(RoundedCornerShape(36.dp))
            .background(cardGradient)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(36.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 36.dp, y = (-48).dp)
                .alpha(glowPulse * 0.35f)
                .background(Color.White, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-28).dp, y = 24.dp)
                .alpha(glowPulse * 0.22f)
                .background(Color(0xFF34D399), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KassakuSpacing.cardInnerLarge + 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "TOTAL SALDO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.82f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Dompet aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA7F3D0),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Tampilkan saldo",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isBalanceVisible) {
                val prefix = currencySymbol(currencyCode)
                val useStandardLayout = currencyFormat.equals("standard", ignoreCase = true)
                if (useStandardLayout) {
                    HeroAnimatedBalanceText(
                        prefix = prefix,
                        amount = balance,
                        currencyCode = currencyCode,
                        gradient = amountGradient,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val flexibleAmountText = formatCurrencyFlexible(balance, currencyCode, currencyFormat)
                    val flexibleFontSize = balanceFontSizeFor(flexibleAmountText)
                    Text(
                        text = flexibleAmountText,
                        style = TextStyle(
                            fontSize = flexibleFontSize,
                            lineHeight = (flexibleFontSize.value + 4).sp,
                            fontWeight = FontWeight.Black,
                            brush = amountGradient
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }
            } else {
                Text(
                    text = "${currencySymbol(currencyCode)} •••••••",
                    style = TextStyle(
                        fontSize = 40.sp,
                        lineHeight = 46.sp,
                        fontWeight = FontWeight.Black,
                        brush = amountGradient
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(KassakuSpacing.cardInner))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black.copy(alpha = if (isDark) 0.22f else 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroBalanceStatColumn(
                    label = "Pemasukan",
                    amountText = formatCurrencyFlexible(income, currencyCode, currencyFormat),
                    icon = Icons.Rounded.ArrowDownward,
                    iconTint = Color(0xFF86EFAC),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(52.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                HeroBalanceStatColumn(
                    label = "Pengeluaran",
                    amountText = formatCurrencyFlexible(expense, currencyCode, currencyFormat),
                    icon = Icons.Rounded.ArrowUpward,
                    iconTint = Color(0xFFFDA4AF),
                    amountColor = if (isExpenseHigherThanIncome) Color(0xFFFFCDD2) else Color.White,
                    amountAlpha = if (isExpenseHigherThanIncome) overspendingAlpha else 1f,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onExpenseSectionClick),
                    progress = targetExpense?.takeIf { it > 0 }?.let { (expense / it).toFloat().coerceIn(0f, 1f) }
                )
            }
        }
    }
}

@Composable
private fun HeroAnimatedBalanceText(
    prefix: String,
    amount: Double,
    currencyCode: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    val target = amount.toFloat()
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "heroAmount"
    )
    val displayValue = if (animated == target) amount else animated.toDouble()
    val balanceText = "$prefix ${formatHeroAmount(displayValue, currencyCode)}"
    val balanceFontSize = balanceFontSizeFor(balanceText)
    Text(
        text = balanceText,
        style = TextStyle(
            fontSize = balanceFontSize,
            lineHeight = (balanceFontSize.value + 4).sp,
            fontWeight = FontWeight.Black,
            brush = gradient,
            letterSpacing = 0.sp
        ),
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = false
    )
}

private fun balanceFontSizeFor(text: String) = when {
    text.length <= 14 -> 42.sp
    text.length <= 17 -> 36.sp
    text.length <= 20 -> 31.sp
    else -> 27.sp
}

@Composable
private fun HeroBalanceStatColumn(
    label: String,
    amountText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    amountColor: Color = Color.White,
    amountAlpha: Float = 1f,
    progress: Float? = null
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color.White.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.72f),
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = amountText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Black
            ),
            color = amountColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(amountAlpha)
        )
        if (progress != null) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = StitchAccentRed.copy(alpha = 0.85f),
                trackColor = Color.White.copy(alpha = 0.16f)
            )
        }
    }
}
