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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.kassaku.ui.theme.DarkSurfaceVariant
import com.example.kassaku.ui.theme.LightSurfaceVariant
import com.example.kassaku.ui.theme.StitchBackgroundDark
import com.example.kassaku.ui.theme.StitchBackgroundLight
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchSurfaceDark
import com.example.kassaku.ui.theme.StitchSurfaceLight
import com.example.kassaku.ui.theme.*

/**
 * Complete skeleton for the HomeScreen.
 * Shows shimmer loading for balance card, summary boxes, action buttons, and transactions.
 */
@Composable
fun HomeScreenSkeleton(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) iOSBackgroundDark else iOSBackgroundLight
    val surfaceColor = if (isDarkTheme) StitchSurfaceDark else StitchSurfaceLight
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else LightSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .semantics { contentDescription = "Memuat halaman utama..." }
    ) {
        // Top Header Background (matches HomeScreen header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    color = StitchPrimary,
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Header row skeleton (greeting + icons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        SkeletonText(
                            width = 120.dp,
                            height = SkeletonDefaults.TextHeightSmall,
                            shimmerBrush = shimmerBrush
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SkeletonText(
                            width = 100.dp,
                            height = SkeletonDefaults.TextHeightLarge,
                            shimmerBrush = shimmerBrush
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonCircle(size = 40.dp, shimmerBrush = shimmerBrush)
                        SkeletonCircle(size = 40.dp, shimmerBrush = shimmerBrush)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Balance Card Skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = androidx.compose.ui.graphics.Color(0x14000000)
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(surfaceColor)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // "TOTAL SALDO" label
                        SkeletonText(
                            width = 80.dp,
                            height = SkeletonDefaults.TextHeightExtraSmall,
                            shimmerBrush = shimmerBrush
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Balance amount
                        SkeletonText(
                            width = 180.dp,
                            height = 36.dp,
                            shimmerBrush = shimmerBrush
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Income/Expense summary boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SkeletonSummaryBox(
                                modifier = Modifier.weight(1f),
                                isDarkTheme = isDarkTheme
                            )
                            SkeletonSummaryBox(
                                modifier = Modifier.weight(1f),
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Action Buttons Skeleton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBackground)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SkeletonCircle(size = 32.dp, shimmerBrush = shimmerBrush)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                SkeletonText(width = 60.dp, height = 12.dp, shimmerBrush = shimmerBrush)
                                Spacer(modifier = Modifier.height(4.dp))
                                SkeletonText(width = 50.dp, height = 12.dp, shimmerBrush = shimmerBrush)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBackground)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SkeletonCircle(size = 32.dp, shimmerBrush = shimmerBrush)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                SkeletonText(width = 60.dp, height = 12.dp, shimmerBrush = shimmerBrush)
                                Spacer(modifier = Modifier.height(4.dp))
                                SkeletonText(width = 50.dp, height = 12.dp, shimmerBrush = shimmerBrush)
                            }
                        }
                    }
                }

                // Section header skeleton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonText(
                        width = 140.dp,
                        height = SkeletonDefaults.TextHeightMedium,
                        shimmerBrush = shimmerBrush
                    )
                    SkeletonText(
                        width = 80.dp,
                        height = SkeletonDefaults.TextHeightSmall,
                        shimmerBrush = shimmerBrush
                    )
                }

                // Transaction list skeleton (3 items for HomeScreen preview)
                SkeletonTransactionList(
                    itemCount = 3,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

/**
 * Full skeleton for RiwayatScreen (Transaction History).
 */
@Composable
fun RiwayatScreenSkeleton(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) iOSBackgroundDark else iOSBackgroundLight

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .semantics { contentDescription = "Memuat riwayat transaksi..." }
    ) {
        // Header skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonText(
                width = 160.dp,
                height = SkeletonDefaults.TextHeightLarge,
                shimmerBrush = shimmerBrush
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonCircle(size = 40.dp, shimmerBrush = shimmerBrush)
                SkeletonCircle(size = 40.dp, shimmerBrush = shimmerBrush)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter tabs skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                SkeletonBox(
                    width = 80.dp,
                    height = 36.dp,
                    cornerRadius = 18.dp,
                    shimmerBrush = shimmerBrush
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction list skeleton (5-7 items for full screen)
        SkeletonTransactionList(
            itemCount = 6,
            isDarkTheme = isDarkTheme
        )
    }
}

/**
 * Full skeleton for ImpianScreen (Savings Goals).
 */
@Composable
fun ImpianScreenSkeleton(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) iOSBackgroundDark else iOSBackgroundLight

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .semantics { contentDescription = "Memuat daftar impian..." }
    ) {
        // Header skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonText(
                width = 120.dp,
                height = SkeletonDefaults.TextHeightLarge,
                shimmerBrush = shimmerBrush
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonCircle(size = 40.dp, shimmerBrush = shimmerBrush)
                SkeletonCircle(size = 40.dp, shimmerBrush = shimmerBrush)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Impian list skeleton (3-4 items)
        SkeletonImpianList(
            itemCount = 4,
            isDarkTheme = isDarkTheme
        )
    }
}

/**
 * Full skeleton for StatistikScreen.
 */
@Composable
fun StatistikScreenSkeleton(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) iOSBackgroundDark else iOSBackgroundLight

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .semantics { contentDescription = "Memuat statistik keuangan..." }
    ) {
        // Header skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonText(
                width = 140.dp,
                height = SkeletonDefaults.TextHeightLarge,
                shimmerBrush = shimmerBrush
            )
            SkeletonCircle(size = 40.dp, shimmerBrush = shimmerBrush)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Summary stats skeleton
        SkeletonStatSummary(
            rowCount = 3,
            isDarkTheme = isDarkTheme
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Chart skeleton
        SkeletonChartView(isDarkTheme = isDarkTheme)
    }
}

/**
 * Full skeleton for ProfileScreen.
 */
@Composable
fun ProfileScreenSkeleton(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val shimmerBrush = rememberShimmerBrush(isDarkTheme)
    val backgroundColor = if (isDarkTheme) iOSBackgroundDark else iOSBackgroundLight
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else LightSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .semantics { contentDescription = "Memuat profil..." },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar skeleton
        SkeletonCircle(size = SkeletonDefaults.AvatarSize, shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.height(16.dp))

        // Username skeleton
        SkeletonText(
            width = 120.dp,
            height = SkeletonDefaults.TextHeightLarge,
            shimmerBrush = shimmerBrush
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats row skeleton (3 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SkeletonText(width = 60.dp, height = 20.dp, shimmerBrush = shimmerBrush)
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonText(width = 50.dp, height = 12.dp, shimmerBrush = shimmerBrush)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Chart skeleton
        SkeletonChartView(isDarkTheme = isDarkTheme)

        Spacer(modifier = Modifier.height(24.dp))

        // Monthly table skeleton (6 rows)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(6) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBackground)
                )
            }
        }
    }
}
