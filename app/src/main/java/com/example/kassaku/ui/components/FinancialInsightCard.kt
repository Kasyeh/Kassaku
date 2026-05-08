package com.example.kassaku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingFlat
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.data.remote.model.StatistikData
import com.example.kassaku.ui.theme.*

/**
 * iOS Finance-style insight card that displays a human-readable financial insight
 * based on statistik data comparison.
 */
@Composable
fun FinancialInsightCard(
    statistikData: StatistikData?,
    currentMonthExpense: Double,
    currentMonthIncome: Double,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val backgroundColor = if (isDark) InsightCardDark else InsightCardLight
    val labelColor = if (isDark) iOSLabelDark else iOSLabelLight
    val secondaryLabelColor = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight
    
    // Calculate insight from statistik data
    val insight = calculateInsight(statistikData, currentMonthExpense, currentMonthIncome)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = PremiumShadowPrimary,
                ambientColor = PremiumShadowSecondary
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trend Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        insight.iconColor.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = insight.icon,
                    contentDescription = null,
                    tint = insight.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = labelColor,
                    letterSpacing = (-0.2).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryLabelColor,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

private data class InsightResult(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: androidx.compose.ui.graphics.Color
)

private fun calculateInsight(
    statistikData: StatistikData?,
    currentMonthExpense: Double,
    currentMonthIncome: Double
): InsightResult {
    if (statistikData == null || statistikData.pengeluaran.size < 2) {
        return InsightResult(
            title = "Mulai Catat Keuanganmu",
            description = "Catat uang masuk dan keluarmu untuk melihat tips di sini.",
            icon = Icons.Rounded.TrendingFlat,
            iconColor = TrendNeutral
        )
    }
    
    val pengeluaranList = statistikData.pengeluaran
    val pemasukanList = statistikData.pemasukan
    
    // Compare current month with previous month
    val prevMonthExpense = pengeluaranList.getOrElse(pengeluaranList.size - 2) { 0.0 }
    val prevMonthIncome = pemasukanList.getOrElse(pemasukanList.size - 2) { 0.0 }
    
    // Calculate savings rate
    val savingsRate = if (currentMonthIncome > 0) {
        ((currentMonthIncome - currentMonthExpense) / currentMonthIncome * 100).toInt()
    } else 0
    
    // Calculate expense change percentage
    val expenseChange = if (prevMonthExpense > 0) {
        ((currentMonthExpense - prevMonthExpense) / prevMonthExpense * 100).toInt()
    } else 0
    
    return when {
        // Positive: Savings increased significantly
        savingsRate >= 30 -> InsightResult(
            title = "Tabungan Bagus! 🎉",
            description = "Kamu menabung ${savingsRate}% dari pemasukan bulan ini. Pertahankan!",
            icon = Icons.Rounded.TrendingUp,
            iconColor = TrendPositive
        )
        
        // Positive: Expense decreased
        expenseChange < -10 -> InsightResult(
            title = "Belanja Menurun",
            description = "Belanja bulan ini ${kotlin.math.abs(expenseChange)}% lebih rendah dari bulan lalu. Bagus!",
            icon = Icons.Rounded.TrendingDown,
            iconColor = TrendPositive
        )
        
        // Warning: Expense increased significantly
        expenseChange > 20 -> InsightResult(
            title = "Belanja Meningkat",
            description = "Belanja naik ${expenseChange}% dibanding bulan lalu. Pantau terus ya.",
            icon = Icons.Rounded.TrendingUp,
            iconColor = TrendNegative
        )
        
        // Neutral: Balanced
        savingsRate in 10..29 -> InsightResult(
            title = "Keuangan Stabil",
            description = "Kamu menabung ${savingsRate}% dari pemasukan. Tetap konsisten!",
            icon = Icons.Rounded.TrendingFlat,
            iconColor = StitchPrimary
        )
        
        // Default
        else -> InsightResult(
            title = "Pantau Terus",
            description = "Cek statistik lengkap untuk analisis lebih detail.",
            icon = Icons.Rounded.TrendingFlat,
            iconColor = TrendNeutral
        )
    }
}
