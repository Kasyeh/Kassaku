package com.example.kassaku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.data.remote.model.StatistikData
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*

/**
 * Compact mini trend chart showing 6-month income vs expense bars.
 * iOS Finance style - clean, minimal, data-focused.
 */
@Composable
fun MiniTrendChart(
    statistikData: StatistikData?,
    onChartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val surfaceColor = if (isDark) Color(0xFF1F2937) else Color.White
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0)
    val labelColor = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryLabelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = PremiumShadowPrimary,
                ambientColor = PremiumShadowSecondary
            )
            .clickable { onChartClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KassakuSpacing.cardInnerLarge)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tren 6 Bulan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = labelColor,
                    letterSpacing = 0.sp
                )
                
                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = StitchPrimary, label = "Masuk")
                    LegendDot(color = StitchAccentRed, label = "Keluar")
                }
            }
            
            Spacer(modifier = Modifier.height(KassakuSpacing.elementGap + 4.dp))
            
            // Mini Chart
            if (statistikData != null && statistikData.labels.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxVal = maxOf(
                        statistikData.pemasukan.maxOrNull() ?: 1.0,
                        statistikData.pengeluaran.maxOrNull() ?: 1.0
                    ).coerceAtLeast(1.0)
                    
                    statistikData.labels.forEachIndexed { index, label ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Income bar
                                val incomeHeight = (statistikData.pemasukan.getOrElse(index) { 0.0 } / maxVal)
                                    .toFloat().coerceIn(0.02f, 1f)
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .fillMaxHeight(incomeHeight)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    StitchPrimary,
                                                    StitchPrimary.copy(alpha = 0.7f)
                                                )
                                            )
                                        )
                                )
                                
                                // Expense bar
                                val expenseHeight = (statistikData.pengeluaran.getOrElse(index) { 0.0 } / maxVal)
                                    .toFloat().coerceIn(0.02f, 1f)
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .fillMaxHeight(expenseHeight)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    StitchAccentRed,
                                                    StitchAccentRed.copy(alpha = 0.7f)
                                                )
                                            )
                                        )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = label.take(3), // Abbreviated month
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = secondaryLabelColor
                            )
                        }
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada data statistik",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryLabelColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Lihat detail statistik",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StitchPrimary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun LegendDot(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (LocalIsDark.current) iOSSecondaryLabelDark else iOSSecondaryLabelLight
        )
    }
}
