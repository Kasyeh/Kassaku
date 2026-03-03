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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.kassaku.ui.theme.DarkSurfaceVariant
import com.example.kassaku.ui.theme.LightSurfaceVariant

/**
 * Skeleton for a single Impian (savings goal) item.
 * Matches ImpianItemRow dimensions.
 * 
 * Layout:
 * [Image]  [Name]
 *          [Price]
 *          [Deadline]
 */
@Composable
fun SkeletonImpianItem(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) DarkSurfaceVariant else LightSurfaceVariant
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SkeletonDefaults.ImpianItemHeight)
            .clip(RoundedCornerShape(SkeletonDefaults.CardCornerRadius))
            .background(backgroundColor)
            .padding(12.dp)
            .semantics { contentDescription = "Memuat impian..." }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image placeholder
            SkeletonBox(
                width = SkeletonDefaults.ImpianImageSize,
                height = SkeletonDefaults.ImpianImageSize,
                cornerRadius = 8.dp,
                shimmerBrush = shimmerBrush
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Name
                SkeletonText(
                    width = SkeletonDefaults.TextWidthLong,
                    height = SkeletonDefaults.TextHeightMedium,
                    shimmerBrush = shimmerBrush
                )
                
                // Price
                SkeletonText(
                    width = SkeletonDefaults.TextWidthMedium,
                    height = SkeletonDefaults.TextHeightSmall,
                    shimmerBrush = shimmerBrush
                )
                
                // Deadline
                SkeletonText(
                    width = SkeletonDefaults.TextWidthMedium - 20.dp,
                    height = SkeletonDefaults.TextHeightExtraSmall,
                    shimmerBrush = shimmerBrush
                )
            }
            
            // Delete icon placeholder
            SkeletonCircle(
                size = 32.dp,
                shimmerBrush = shimmerBrush
            )
        }
    }
}

/**
 * Skeleton for Impian list showing multiple impian items.
 * 
 * @param itemCount Number of skeleton items to show (default: 3)
 */
@Composable
fun SkeletonImpianList(
    modifier: Modifier = Modifier,
    itemCount: Int = 3,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SkeletonDefaults.ItemSpacing)
    ) {
        repeat(itemCount) {
            SkeletonImpianItem(isDarkTheme = isDarkTheme)
        }
    }
}
