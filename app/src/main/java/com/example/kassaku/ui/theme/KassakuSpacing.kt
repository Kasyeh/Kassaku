package com.example.kassaku.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing tokens for consistent, breathable layouts across KasSaku screens.
 */
object KassakuSpacing {
    /** Horizontal inset for screen content */
    val screenHorizontal: Dp = 24.dp

    /** Top/bottom inset inside scroll areas */
    val screenVertical: Dp = 20.dp

    /** Extra space at the end of scrollable lists (above bottom nav) */
    val listBottom: Dp = 36.dp

    /** Gap between major sections (e.g. "Ringkasan Keuangan" block) */
    val sectionGap: Dp = 28.dp

    /** Gap between cards or list items */
    val cardGap: Dp = 20.dp

    /** Standard inner padding for cards and surfaces */
    val cardInner: Dp = 20.dp

    /** Hero / primary cards (balance, detail) */
    val cardInnerLarge: Dp = 24.dp

    /** Compact chips, icon+label rows */
    val elementGap: Dp = 12.dp

    /** Horizontal gap in chip / action rows */
    val chipRowGap: Dp = 14.dp

    /** Section title inset from content edge */
    val sectionTitleInset: Dp = 4.dp
}
