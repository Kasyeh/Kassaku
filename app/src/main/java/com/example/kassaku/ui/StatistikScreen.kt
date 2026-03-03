package com.example.kassaku.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.viewmodel.ImpianUiState
import com.example.kassaku.ui.components.skeleton.SkeletonChartView
import androidx.compose.material.icons.filled.Stars

@Composable
fun StatistikScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textPrimary = if (isDark) iOSLabelDark else iOSLabelLight


    val statistikData by homeViewModel.statistikData.collectAsStateWithLifecycle()
    val balanceData by homeViewModel.balanceData.collectAsStateWithLifecycle()
    val impianUiState by homeViewModel.impianUiState.collectAsStateWithLifecycle()


    LaunchedEffect(key1 = userId) {
        if (userId != 0) {
            homeViewModel.fetchStatistik(userId)
            homeViewModel.loadBalanceData(userId)
            homeViewModel.fetchImpian(userId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Curved Header Background (matching HomeScreen)
        Column(modifier = Modifier.fillMaxSize()) {
            // Standard Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .statusBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Statistik",
                        color = textPrimary,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    IconButton(
                        onClick = { homeViewModel.logout() },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = textPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Content
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            item {
                Text(
                    text = "Analisis Keuangan 6 Bulan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            item {
                val data = statistikData
                if (data != null) {
                    SmoothLineChart(
                        labels = data.labels,
                        pemasukan = data.pemasukan,
                        pengeluaran = data.pengeluaran,
                        isDark = isDark
                    )
                } else {
                    SkeletonChartView(
                        isDarkTheme = isDark,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                val data = statistikData
                if (data != null) {
                    FinancialInsightsSection(
                        data = data,
                        isDark = isDark
                    )
                }
            }

            item {
                val data = statistikData
                val impianState = impianUiState
                if (data != null && impianState is ImpianUiState.Success) {
                    DreamProjectionSection(
                        data = data,
                        impianItems = impianState.impianItems,
                        isDark = isDark
                    )
                }
            }

            item {
                val data = statistikData
                if (data != null) {
                    ProgressComparisonSection(
                        data = data,
                        isDark = isDark
                    )
                }
            }
        }
    }
}
}

@Composable
fun StatSummaryRow(label: String, value: String, color: Color, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = if(isDark) Color(0xFF94A3B8) else StitchTextSecondary, fontSize = 14.sp)
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun SmoothLineChart(
    labels: List<String>,
    pemasukan: List<Double>,
    pengeluaran: List<Double>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceColor = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight
    val textSecondary = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight
    val primaryColor = StitchPrimary
    val accentRed = StitchAccentRed
    
    var selectedIndex by remember { mutableStateOf(-1) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0x0A000000)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grafik Performa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else iOSLabelLight
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChartLegendItem(label = "Masuk", color = primaryColor)
                    ChartLegendItem(label = "Keluar", color = accentRed)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                selectedIndex = calculateSelectedIndex(offset.x, size.width.toFloat(), labels.size)
                            },
                            onDrag = { change, _ ->
                                selectedIndex = calculateSelectedIndex(change.position.x, size.width.toFloat(), labels.size)
                            },
                            onDragEnd = { selectedIndex = -1 },
                            onDragCancel = { selectedIndex = -1 }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val index = calculateSelectedIndex(offset.x, size.width.toFloat(), labels.size)
                                selectedIndex = if (selectedIndex == index) -1 else index
                            }
                        )
                    }
            ) {
                val animationProgress = remember { androidx.compose.animation.core.Animatable(0f) }
                LaunchedEffect(Unit) {
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    )
                }

                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 30.dp, top = 10.dp)) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (labels.size - 1).coerceAtLeast(1)
                    
                    val maxVal = (pemasukan + pengeluaran).maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
                    val graphMax = maxVal * 1.2
                    
                    // Draw horizontal grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = height - (i * height / gridLines)
                        drawLine(
                            color = textSecondary.copy(alpha = 0.05f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw Pemasukan Path (Smooth Curve)
                    drawSmoothLine(
                        data = pemasukan,
                        maxVal = graphMax,
                        height = height,
                        spacing = spacing,
                        color = primaryColor,
                        progress = animationProgress.value
                    )

                    // Draw Pengeluaran Path (Smooth Curve)
                    drawSmoothLine(
                        data = pengeluaran,
                        maxVal = graphMax,
                        height = height,
                        spacing = spacing,
                        color = accentRed,
                        progress = animationProgress.value
                    )

                    // Draw Selection Highlight
                    if (selectedIndex != -1 && selectedIndex < labels.size) {
                        val x = selectedIndex * spacing
                        drawLine(
                            color = textSecondary.copy(alpha = 0.2f),
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }
                }

                // Month Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEachIndexed { index, label ->
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedIndex == index) primaryColor else textSecondary,
                            modifier = Modifier.width(30.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Tooltip
                if (selectedIndex != -1 && selectedIndex < labels.size) {
                    TooltipView(
                        month = labels[selectedIndex],
                        masuk = pemasukan[selectedIndex],
                        keluar = pengeluaran[selectedIndex],
                        isDark = isDark,
                        modifier = Modifier
                            .align(if (selectedIndex < labels.size / 2) Alignment.TopEnd else Alignment.TopStart)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

fun calculateSelectedIndex(x: Float, width: Float, count: Int): Int {
    val spacing = width / (count - 1).coerceAtLeast(1)
    return (x / spacing).roundToInt().coerceIn(0, count - 1)
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmoothLine(
    data: List<Double>,
    maxVal: Double,
    height: Float,
    spacing: Float,
    color: Color,
    progress: Float
) {
    if (data.size < 2) return
    
    val path = androidx.compose.ui.graphics.Path()
    val points = data.mapIndexed { index, value ->
        androidx.compose.ui.geometry.Offset(
            x = index * spacing,
            y = height - (value / maxVal * height).toFloat()
        )
    }

    path.moveTo(points[0].x, points[0].y)
    
    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val controlPoint1 = androidx.compose.ui.geometry.Offset(p0.x + (p1.x - p0.x) / 2, p0.y)
        val controlPoint2 = androidx.compose.ui.geometry.Offset(p0.x + (p1.x - p0.x) / 2, p1.y)
        path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
    }

    drawPath(
        path = path,
        color = color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        ),
        alpha = progress
    )

    // Draw dots
    points.forEach { point ->
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = point,
            alpha = progress
        )
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = point,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
            alpha = progress
        )
    }
}

@Composable
fun TooltipView(month: String, masuk: Double, keluar: Double, isDark: Boolean, modifier: Modifier = Modifier) {
    val net = masuk - keluar
    val netColor = if (net >= 0) StitchPrimary else StitchAccentRed
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2C2C2E) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.wrapContentSize()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = month, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDark) Color.White else Color.Black)
            TooltipRow(label = "Masuk", value = formatCurrency(masuk), color = StitchPrimary)
            TooltipRow(label = "Keluar", value = formatCurrency(keluar), color = StitchAccentRed)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = if(isDark) Color.Gray else Color.LightGray)
            TooltipRow(label = "Net", value = formatCurrency(net), color = netColor, isBold = true)
        }
    }
}

@Composable
fun TooltipRow(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Text(text = "$label:", fontSize = 10.sp, color = iOSSecondaryLabelLight)
        Text(text = value, fontSize = 10.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, color = color)
    }
}

fun formatCurrency(amount: Double): String {
    return "Rp ${String.format("%,.0f", amount).replace(',', '.')}"
}


@Composable
fun FinancialInsightsSection(
    data: com.example.kassaku.data.remote.model.StatistikData,
    isDark: Boolean
) {
    val labels = data.labels
    val pemasukan = data.pemasukan
    val pengeluaran = data.pengeluaran
    
    // Calculations
    val nets = pemasukan.zip(pengeluaran) { inc, exp -> inc - exp }
    val avgSavings = if (nets.isNotEmpty()) nets.average() else 0.0
    
    // Only calculate comparative insights if we have contextual data (more than 1 month)
    val hasEnoughData = labels.size > 1
    
    val mostWastefulIndex = if (hasEnoughData) pengeluaran.indices.maxByOrNull { pengeluaran[it] } ?: -1 else -1
    val mostProductiveIndex = if (hasEnoughData) nets.indices.maxByOrNull { nets[it] } ?: -1 else -1
    
    val trend = if (nets.size >= 2) {
        val last = nets.last()
        val prev = nets[nets.size - 2]
        if (last > prev) "Meningkat" else if (last < prev) "Menurun" else "Stabil"
    } else "Stabil"

    Text(
        text = "Insight Keuangan",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (isDark) Color.White else iOSLabelLight
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InsightCard(
            title = "Rata-rata Tabungan",
            value = formatCurrency(avgSavings),
            icon = Icons.Default.PieChart,
            color = StitchPrimary,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
        InsightCard(
            title = "Bulan Terhemat",
            value = if (mostProductiveIndex != -1) labels[mostProductiveIndex] else "-",
            icon = Icons.Default.PieChart, // Should be something else but let's stick to simple ones first
            color = Color(0xFF34C759),
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InsightCard(
            title = "Bulan Terboros",
            value = if (mostWastefulIndex != -1) labels[mostWastefulIndex] else "-",
            icon = Icons.Default.PieChart,
            color = StitchAccentRed,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
        InsightCard(
            title = "Tren 6 Bulan",
            value = trend,
            icon = Icons.Default.PieChart,
            color = if (trend == "Meningkat") StitchPrimary else if (trend == "Menurun") StitchAccentRed else Color.Gray,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun InsightCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) iOSSecondaryBackgroundDark else Color.White
        ),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(text = title, fontSize = 10.sp, color = iOSSecondaryLabelLight)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
            }
        }
    }
}

@Composable
fun ProgressComparisonSection(
    data: com.example.kassaku.data.remote.model.StatistikData,
    isDark: Boolean
) {
    val currentInc = data.pemasukan.lastOrNull() ?: 0.0
    val currentExp = data.pengeluaran.lastOrNull() ?: 0.0
    val prevInc = if (data.pemasukan.size >= 2) data.pemasukan[data.pemasukan.size - 2] else 0.0
    
    val progress = if (currentInc > 0) (currentExp / currentInc).toFloat().coerceIn(0f, 1f) else 0f
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) iOSSecondaryBackgroundDark else Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rasio Pengeluaran",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (progress > 0.8f) StitchAccentRed else StitchPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (progress > 0.8f) StitchAccentRed else StitchPrimary,
                trackColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA),
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ComparisonItem(
                    label = "Bulan ini vs Lalu",
                    current = currentInc,
                    previous = prevInc
                )
            }
        }
    }
}

@Composable
fun ComparisonItem(label: String, current: Double, previous: Double) {
    val diff = current - previous
    val percent = if (previous > 0) (diff / previous * 100) else 0.0
    val color = if (diff >= 0) StitchPrimary else StitchAccentRed
    val icon = if (diff >= 0) "↑" else "↓"
    
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, fontSize = 12.sp, color = iOSSecondaryLabelLight)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$icon ${String.format("%.1f", Math.abs(percent))}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = iOSSecondaryLabelLight
        )
    }
}

@Composable
fun DreamProjectionSection(
    data: com.example.kassaku.data.remote.model.StatistikData,
    impianItems: List<com.example.kassaku.data.remote.model.ImpianItem>,
    isDark: Boolean
) {
    if (impianItems.isEmpty()) return

    val pemasukan = data.pemasukan
    val pengeluaran = data.pengeluaran
    val nets = pemasukan.zip(pengeluaran) { inc, exp -> inc - exp }
    val avgSavings = if (nets.isNotEmpty()) nets.average().coerceAtLeast(0.0) else 0.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Proyeksi Impian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else iOSLabelLight
                )
                Text(
                    text = "Estimasi pencapaian dari rata tabungan",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = iOSSecondaryLabelLight,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = StitchPrimary,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isDark) StitchPrimary.copy(alpha = 0.1f) else StitchPrimary.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid-like list
        impianItems.take(6).chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    DreamProjectionCard(
                        item = item,
                        avgSavings = avgSavings,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DreamProjectionCard(
    item: com.example.kassaku.data.remote.model.ImpianItem,
    avgSavings: Double,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val harga = item.hargaBarang?.toDouble() ?: 0.0
    val reachPerc = if (harga > 0) (avgSavings / harga * 100).coerceIn(0.0, 100.0) else 0.0
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) iOSSecondaryBackgroundDark else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = StitchPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.namaBarang ?: "-",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = if (isDark) Color.White else Color.Black
                    )
                    Text(
                        text = formatCurrency(harga),
                        fontSize = 10.sp,
                        color = iOSSecondaryLabelLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Kontribusi Bulanan",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = iOSSecondaryLabelLight,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${String.format("%.1f", reachPerc)}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = StitchPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            LinearProgressIndicator(
                progress = { (reachPerc / 100f).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = StitchPrimary,
                trackColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA),
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Tabungan rata-rata Anda mencakup ${String.format("%.1f", reachPerc)}% dari target ini setiap bulannya.",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = iOSSecondaryLabelLight,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 12.sp
            )
        }
    }
}

