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
 * Skeleton for a single transaction item row.
 * Dimensions match RiwayatItemRow exactly to prevent layout shift.
 * 
 * Layout:
 * [Circle Icon]  [Title Text]           [Amount]
 *                [Category/Date]
 */
@Composable
fun SkeletonTransactionItem(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) DarkSurfaceVariant else LightSurfaceVariant
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SkeletonDefaults.TransactionItemHeight)
            .clip(RoundedCornerShape(SkeletonDefaults.CardCornerRadius))
            .background(backgroundColor)
            .padding(
                horizontal = SkeletonDefaults.TransactionItemPaddingHorizontal,
                vertical = SkeletonDefaults.TransactionItemPaddingVertical
            )
            .semantics { contentDescription = "Memuat transaksi..." }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon placeholder
            SkeletonCircle(
                size = SkeletonDefaults.IconSizeMedium,
                shimmerBrush = shimmerBrush
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Title and subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                SkeletonText(
                    width = SkeletonDefaults.TextWidthLong,
                    height = SkeletonDefaults.TextHeightMedium,
                    shimmerBrush = shimmerBrush
                )
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonText(
                    width = SkeletonDefaults.TextWidthMedium,
                    height = SkeletonDefaults.TextHeightSmall,
                    shimmerBrush = shimmerBrush
                )
            }
            
            // Amount placeholder (right-aligned)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                SkeletonText(
                    width = SkeletonDefaults.TextWidthMedium,
                    height = SkeletonDefaults.TextHeightMedium,
                    shimmerBrush = shimmerBrush
                )
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonText(
                    width = SkeletonDefaults.TextWidthShort,
                    height = SkeletonDefaults.TextHeightExtraSmall,
                    shimmerBrush = shimmerBrush
                )
            }
        }
    }
}

/**
 * Skeleton for transaction list showing multiple transaction items.
 * 
 * @param itemCount Number of skeleton items to show (default: 5)
 */
@Composable
fun SkeletonTransactionList(
    modifier: Modifier = Modifier,
    itemCount: Int = 5,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SkeletonDefaults.ItemSpacing)
    ) {
        repeat(itemCount) {
            SkeletonTransactionItem(isDarkTheme = isDarkTheme)
        }
    }
}
