package com.example.kassaku.ui.components.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.kassaku.ui.theme.DarkSurfaceVariant
import com.example.kassaku.ui.theme.LightSurfaceVariant

/**
 * Skeleton for the financial chart on StatistikScreen and ProfileScreen.
 * Shows placeholder bars to represent the chart area.
 */
@Composable
fun SkeletonChartView(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) DarkSurfaceVariant else LightSurfaceVariant
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Memuat grafik statistik..." }
    ) {
        // Chart area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SkeletonDefaults.ChartHeight)
                .clip(RoundedCornerShape(SkeletonDefaults.CardCornerRadius))
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            // Simulated bar chart
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 6 pairs of bars (income/expense for 6 months)
                val barHeights = listOf(0.7f, 0.5f, 0.85f, 0.6f, 0.9f, 0.4f)
                barHeights.forEach { heightFraction ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height((SkeletonDefaults.ChartHeight - 56.dp) * heightFraction)
                        ) {
                            // Income bar
                            SkeletonBox(
                                width = 12.dp,
                                height = (SkeletonDefaults.ChartHeight - 56.dp) * heightFraction,
                                cornerRadius = 4.dp,
                                shimmerBrush = shimmerBrush
                            )
                            // Expense bar (shorter)
                            SkeletonBox(
                                width = 12.dp,
                                height = (SkeletonDefaults.ChartHeight - 56.dp) * heightFraction * 0.7f,
                                cornerRadius = 4.dp,
                                shimmerBrush = shimmerBrush
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonCircle(
                size = 12.dp,
                shimmerBrush = shimmerBrush
            )
            Spacer(modifier = Modifier.width(6.dp))
            SkeletonText(
                width = SkeletonDefaults.TextWidthShort,
                height = SkeletonDefaults.TextHeightExtraSmall,
                shimmerBrush = shimmerBrush
            )
            
            Spacer(modifier = Modifier.width(24.dp))
            
            SkeletonCircle(
                size = 12.dp,
                shimmerBrush = shimmerBrush
            )
            Spacer(modifier = Modifier.width(6.dp))
            SkeletonText(
                width = SkeletonDefaults.TextWidthShort,
                height = SkeletonDefaults.TextHeightExtraSmall,
                shimmerBrush = shimmerBrush
            )
        }
    }
}

/**
 * Skeleton for stat summary rows on StatistikScreen.
 */
@Composable
fun SkeletonStatSummary(
    modifier: Modifier = Modifier,
    rowCount: Int = 3,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(rowCount) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonText(
                    width = SkeletonDefaults.TextWidthMedium,
                    height = SkeletonDefaults.TextHeightSmall,
                    shimmerBrush = shimmerBrush
                )
                SkeletonText(
                    width = SkeletonDefaults.TextWidthMedium + 20.dp,
                    height = SkeletonDefaults.TextHeightMedium,
                    shimmerBrush = shimmerBrush
                )
            }
        }
    }
}
