package com.example.kassaku.ui.components.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.kassaku.ui.theme.DarkSurfaceVariant
import com.example.kassaku.ui.theme.LightSurfaceVariant

/**
 * Skeleton for the main balance card on HomeScreen.
 * Matches BalanceSummaryBox dimensions.
 */
@Composable
fun SkeletonBalanceCard(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) DarkSurfaceVariant else LightSurfaceVariant
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SkeletonDefaults.BalanceCardHeight)
            .clip(RoundedCornerShape(SkeletonDefaults.CardCornerRadius))
            .background(backgroundColor)
            .padding(20.dp)
            .semantics { contentDescription = "Memuat saldo..." }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Greeting text
            SkeletonText(
                width = SkeletonDefaults.TextWidthLong,
                height = SkeletonDefaults.TextHeightMedium,
                shimmerBrush = shimmerBrush
            )
            
            // Balance label
            SkeletonText(
                width = SkeletonDefaults.TextWidthMedium,
                height = SkeletonDefaults.TextHeightSmall,
                shimmerBrush = shimmerBrush
            )
            
            // Balance amount (larger)
            SkeletonText(
                width = SkeletonDefaults.TextWidthExtraLong,
                height = SkeletonDefaults.TextHeightLarge + 8.dp, // Larger for amount
                shimmerBrush = shimmerBrush
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Target info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonText(
                    width = SkeletonDefaults.TextWidthMedium,
                    height = SkeletonDefaults.TextHeightSmall,
                    shimmerBrush = shimmerBrush
                )
                SkeletonText(
                    width = SkeletonDefaults.TextWidthMedium,
                    height = SkeletonDefaults.TextHeightSmall,
                    shimmerBrush = shimmerBrush
                )
            }
        }
    }
}

/**
 * Skeleton for income/expense summary boxes.
 * Used for the side-by-side summary boxes on HomeScreen.
 */
@Composable
fun SkeletonSummaryBox(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) DarkSurfaceVariant else LightSurfaceVariant
    
    Box(
        modifier = modifier
            .height(SkeletonDefaults.SummaryBoxHeight)
            .clip(RoundedCornerShape(SkeletonDefaults.CardCornerRadius))
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder
            SkeletonCircle(
                size = SkeletonDefaults.IconSizeSmall + 8.dp,
                shimmerBrush = shimmerBrush
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column {
                SkeletonText(
                    width = SkeletonDefaults.TextWidthShort,
                    height = SkeletonDefaults.TextHeightExtraSmall,
                    shimmerBrush = shimmerBrush
                )
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonText(
                    width = SkeletonDefaults.TextWidthMedium,
                    height = SkeletonDefaults.TextHeightMedium,
                    shimmerBrush = shimmerBrush
                )
            }
        }
    }
}

/**
 * Skeleton for action button placeholder.
 */
@Composable
fun SkeletonActionButton(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) DarkSurfaceVariant else LightSurfaceVariant
    
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(SkeletonDefaults.ButtonCornerRadius))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SkeletonCircle(
                size = SkeletonDefaults.IconSizeSmall,
                shimmerBrush = shimmerBrush
            )
            Spacer(modifier = Modifier.width(8.dp))
            SkeletonText(
                width = SkeletonDefaults.TextWidthShort,
                height = SkeletonDefaults.TextHeightSmall,
                shimmerBrush = shimmerBrush
            )
        }
    }
}
