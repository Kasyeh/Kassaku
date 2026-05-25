package com.example.kassaku.ui.components.skeleton

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard dimensions for skeleton components.
 * Ensures consistency across all skeleton implementations.
 * These values MUST match the real content dimensions to prevent layout shift.
 */
object SkeletonDefaults {
    
    // Corner radius
    val TextCornerRadius: Dp = 4.dp
    val CardCornerRadius: Dp = 12.dp
    val ButtonCornerRadius: Dp = 8.dp
    
    // Text skeleton heights (match Material 3 typography)
    val TextHeightLarge: Dp = 24.dp      // Title/headline
    val TextHeightMedium: Dp = 18.dp     // Subtitle/amount
    val TextHeightSmall: Dp = 14.dp      // Body/caption
    val TextHeightExtraSmall: Dp = 12.dp // Small labels
    
    // Common widths
    val TextWidthShort: Dp = 60.dp
    val TextWidthMedium: Dp = 100.dp
    val TextWidthLong: Dp = 160.dp
    val TextWidthExtraLong: Dp = 200.dp
    
    // Icon/Avatar sizes
    val IconSizeSmall: Dp = 24.dp
    val IconSizeMedium: Dp = 40.dp
    val IconSizeLarge: Dp = 56.dp
    val AvatarSize: Dp = 72.dp
    
    // Transaction item dimensions (must match RiwayatItemRow)
    val TransactionItemHeight: Dp = 72.dp
    val TransactionItemPaddingHorizontal: Dp = 16.dp
    val TransactionItemPaddingVertical: Dp = 12.dp
    
    // Balance card dimensions
    val BalanceCardHeight: Dp = 180.dp
    val SummaryBoxHeight: Dp = 80.dp
    
    // Impian item dimensions
    val ImpianItemHeight: Dp = 100.dp
    val ImpianImageSize: Dp = 80.dp
    
    // Chart dimensions
    val ChartHeight: Dp = 200.dp
    
    // Spacing (aligned with KassakuSpacing)
    val ItemSpacing: Dp = 12.dp
    val SectionSpacing: Dp = 20.dp
}
