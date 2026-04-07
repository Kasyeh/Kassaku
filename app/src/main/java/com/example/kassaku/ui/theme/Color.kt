package com.example.kassaku.ui.theme

import androidx.compose.ui.graphics.Color

// Light & Dark Primary Colors
val ToscaPrimary = Color(0xFF10B981)  // Emerald 500
val ToscaContainer = Color(0xFFECFDF5) // Emerald 50 untuk container
val SoftRed = Color(0xFFEF4444)       // Rose 500 untuk pengeluaran/error
val GoldAccent = Color(0xFFF59E0B)    // Amber 500 untuk tertiary

// Light Theme Colors
val LightBackground = Color(0xFFF5F5F5)  // Background abu-abu muda netral
val WhiteSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1E1E1E)
val LightSurfaceVariant = Color(0xFFF8F8F8) // Abu-abu sangat muda untuk cards/chips
val LightOnSurfaceVariant = Color(0xFF424242) // Readable text on variants

// Dark Theme Colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)       // Slightly lighter than background
val DarkOnSurface = Color(0xFFE0E0E0)
val DarkSurfaceVariant = Color(0xFF2C2C2C)  // Darker gray for cards/chips
val DarkOnSurfaceVariant = Color(0xFFBDBDBD)   // Lighter text for dark variants

// Design Specific Colors
val StitchPrimary = Color(0xFF10B981)
val StitchPrimaryDark = Color(0xFF059669)
val StitchPrimaryLight = Color(0x2610B981) // 15% opacity
val StitchNegative = Color(0xFFEF4444)
val StitchAccentRed = Color(0xFFEF4444)
val StitchBackgroundLight = Color(0xFFF8FAFC)
val StitchBackgroundDark = Color(0xFF111827)
val StitchTextPrimary = Color(0xFF1E293B)
val StitchTextSecondary = Color(0xFF64748B)
val StitchSurfaceLight = Color(0xFFFFFFFF)
val StitchSurfaceDark = Color(0xFF1F2937)

// === iOS-Inspired Color Palette ===
// Background colors (Apple HIG style)
val iOSBackgroundLight = Color(0xFFF2F2F7)       // iOS system background
val iOSBackgroundDark = Color(0xFF000000)         // Pure black for dark mode
val iOSGroupedBackgroundLight = Color(0xFFFFFFFF) // Grouped content background
val iOSGroupedBackgroundDark = Color(0xFF1C1C1E)  // Dark grouped background
val iOSSecondaryBackgroundLight = Color(0xFFF2F2F7)
val iOSSecondaryBackgroundDark = Color(0xFF2C2C2E)

// Label/Text colors (Apple HIG)
val iOSLabelLight = Color(0xFF000000)             // Primary label
val iOSLabelDark = Color(0xFFFFFFFF)
val iOSSecondaryLabelLight = Color(0xFF8E8E93)    // Secondary label
val iOSSecondaryLabelDark = Color(0xFF8E8E93)
val iOSTertiaryLabel = Color(0xFFC7C7CC)          // Tertiary label

// Separator/Divider
val iOSSeparatorLight = Color(0x33C6C6C8)         // iOS separator with alpha
val iOSSeparatorDark = Color(0x33545458)

// Soft shadow colors for iOS-style cards
val iOSShadowLight = Color(0x0A000000)            // ~4% opacity black
val iOSShadowMedium = Color(0x14000000)           // ~8% opacity black

// System colors (used sparingly as accent)
val iOSBlue = Color(0xFF007AFF)
val iOSGreen = Color(0xFF34C759)
val iOSRed = Color(0xFFFF3B30)

// === Finance-Grade Premium Colors ===
// Card background gradients for depth
val FinanceCardGradientStart = Color(0xFFFFFFFF)
val FinanceCardGradientEnd = Color(0xFFF8FAFC)
val FinanceCardGradientDarkStart = Color(0xFF1C1C1E)
val FinanceCardGradientDarkEnd = Color(0xFF2C2C2E)

// Insight card backgrounds (soft, calming)
val InsightCardLight = Color(0xFFF0F9FF)      // Soft blue tint
val InsightCardDark = Color(0xFF172554)        // Deep blue for dark mode
val InsightCardAccent = Color(0xFF3B82F6)      // Blue accent for insights

// Budget/Progress indicators
val BudgetSafeGreen = Color(0xFF10B981)        // Clean green for safe budget
val BudgetWarningAmber = Color(0xFFF59E0B)     // Amber for warning (80%+)
val BudgetDangerRed = Color(0xFFEF4444)        // Red for over budget

// Premium shadow layers
val PremiumShadowPrimary = Color(0x1A000000)   // 10% black - primary shadow
val PremiumShadowSecondary = Color(0x0D000000) // 5% black - ambient shadow

// Trend chart colors  
val TrendPositive = Color(0xFF059669)          // Emerald for positive trends
val TrendNegative = Color(0xFFDC2626)          // Red for negative trends
val TrendNeutral = Color(0xFF6B7280)           // Gray for neutral

// Text emphasis for financial data
val FinanceAmountPrimary = Color(0xFF0F172A)   // Very dark for main amounts
val FinanceAmountSecondary = Color(0xFF475569) // Muted for secondary amounts
