package com.example.kassaku.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import coil.compose.AsyncImage
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.TrendingUp
import com.example.kassaku.data.remote.model.BudgetKategoriItem
import com.example.kassaku.data.remote.model.MotivasiItem
import com.example.kassaku.viewmodel.BudgetActionResult
import java.util.Locale
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier

@Composable
fun StatistikScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    onNavigateToImpian: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textPrimary = if (isDark) iOSLabelDark else iOSLabelLight


    val statistikData by homeViewModel.statistikData.collectAsStateWithLifecycle()
    val balanceData by homeViewModel.balanceData.collectAsStateWithLifecycle()
    val impianUiState by homeViewModel.impianUiState.collectAsStateWithLifecycle()
    val budgetActionResult by homeViewModel.budgetActionResult.collectAsStateWithLifecycle()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showHapusBudgetDialog by remember { mutableStateOf(false) }
    var budgetIdToDelete by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(budgetActionResult) {
        if (budgetActionResult is BudgetActionResult.Success) {
            showBudgetDialog = false
            homeViewModel.resetBudgetActionResult()
        }
    }

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
                    text = "Analisis Arus Kas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            item {
                val data = statistikData
                if (data != null) {
                    CashflowChartsSection(
                        data = data,
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
                if (data != null && !data.motivasi.isNullOrEmpty()) {
                    MotivationCarousel(
                        motivasi = data.motivasi,
                        isDark = isDark
                    )
                }
            }

            item {
                val data = statistikData
                if (data != null) {
                    BudgetKategoriSection(
                        budgets = data.budgetKategori ?: emptyList(),
                        onTambahClick = { showBudgetDialog = true },
                        onDeleteClick = { budgetId -> 
                            budgetIdToDelete = budgetId
                            showHapusBudgetDialog = true
                        },
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
                        isDark = isDark,
                        onNavigateToImpian = onNavigateToImpian
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

        if (showBudgetDialog) {
            BudgetManagementDialog(
                onDismiss = { showBudgetDialog = false },
                onConfirm = { kategori, nominal, periode, tglMulai, tglAkhir ->
                    homeViewModel.simpanBudgetKategori(userId, kategori, nominal, periode, tglMulai, tglAkhir)
                },
                kategoriList = statistikData?.kategoriList ?: emptyList(),
                isDark = isDark,
                isLoading = budgetActionResult is BudgetActionResult.Loading
            )
        }

        if (showHapusBudgetDialog) {
            HapusBudgetDialog(
                onDismissRequest = { 
                    showHapusBudgetDialog = false 
                    budgetIdToDelete = null
                },
                onConfirm = { password ->
                    showHapusBudgetDialog = false
                    budgetIdToDelete?.let { id ->
                        homeViewModel.hapusBudgetKategori(id, userId, password)
                    }
                    budgetIdToDelete = null
                }
            )
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
    isDark: Boolean,
    onNavigateToImpian: () -> Unit
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
                        onClick = onNavigateToImpian,
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
    onClick: () -> Unit,
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
            .clickable(onClick = onClick)
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

@Composable
fun BudgetKategoriSection(
    budgets: List<BudgetKategoriItem>,
    onTambahClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    isDark: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budget per Kategori",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else iOSLabelLight
            )
            
            TextButton(
                onClick = onTambahClick,
                colors = ButtonDefaults.textButtonColors(contentColor = StitchPrimary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (budgets.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) iOSSecondaryBackgroundDark else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Belum ada budget kategori. Atur sekarang!",
                        fontSize = 12.sp,
                        color = iOSTertiaryLabel,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            budgets.forEach { budget ->
                BudgetCard(
                    budget = budget,
                    onDelete = { onDeleteClick(budget.id) },
                    isDark = isDark
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BudgetCard(
    budget: BudgetKategoriItem,
    onDelete: () -> Unit,
    isDark: Boolean
) {
    val progress = budget.percentage?.toFloat()?.div(100f)?.coerceIn(0f, 1.2f) ?: 0f
    val isOver = budget.over ?: false
    val statusColor = when {
        isOver -> BudgetDangerRed
        progress > 0.75f -> BudgetWarningAmber
        else -> BudgetSafeGreen
    }

    val icon = when (budget.kategori.lowercase()) {
        "makanan", "minuman", "food" -> Icons.Default.Restaurant
        "transportasi", "transport", "bensin" -> Icons.Default.DirectionsCar
        "belanja", "shopping" -> Icons.Default.ShoppingBag
        "kesehatan", "obat", "health" -> Icons.Default.MedicalServices
        "hiburan", "entertainment", "game" -> Icons.Default.SportsEsports
        "pendidikan", "school", "education" -> Icons.Default.School
        "tagihan", "bills", "listrik", "air" -> Icons.Default.Payments
        "cicilan" -> Icons.Default.CreditCard
        "investasi", "investment" -> Icons.Default.TrendingUp
        else -> Icons.Default.Category
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) iOSSecondaryBackgroundDark else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(statusColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = budget.kategori.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            text = budget.periodeLabel ?: budget.periode,
                            fontSize = 10.sp,
                            color = iOSSecondaryLabelLight
                        )
                    }
                }
                
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = StitchNegative.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "Terpakai", fontSize = 10.sp, color = iOSSecondaryLabelLight)
                    Text(text = formatCurrency(budget.spent ?: 0.0), fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Limit", fontSize = 10.sp, color = iOSSecondaryLabelLight)
                    Text(text = formatCurrency(budget.nominal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = statusColor,
                    trackColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA),
                )
            }

            if (isOver || progress > 0.75f) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isOver) "OVER BUDGET" else "HAMPIR LIMIT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagementDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String, String?, String?) -> Unit,
    kategoriList: List<String>,
    isDark: Boolean,
    isLoading: Boolean
) {
    var kategori by remember { mutableStateOf("") }
    var nominal by remember { mutableStateOf("") }
    var periode by remember { mutableStateOf("bulanan") }
    var tglMulai by remember { mutableStateOf("") }
    var tglAkhir by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Budget Kategori", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = kategori,
                    onValueChange = { kategori = it },
                    label = { Text("Kategori") },
                    placeholder = { Text("Cth: Makanan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = nominal,
                    onValueChange = { if (it.all { char -> char.isDigit() }) nominal = it },
                    label = { Text("Limit Budget (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    prefix = { Text("Rp ") }
                )

                Column {
                    Text("Periode", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("mingguan", "bulanan", "custom").forEach { p ->
                            FilterChip(
                                selected = periode == p,
                                onClick = { periode = p },
                                label = { Text(p.replaceFirstChar { it.uppercase() }) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                if (periode == "custom") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tglMulai,
                            onValueChange = { tglMulai = it },
                            label = { Text("Mulai") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )
                        OutlinedTextField(
                            value = tglAkhir,
                            onValueChange = { tglAkhir = it },
                            label = { Text("Akhir") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (kategori.isNotBlank() && nominal.isNotBlank()) {
                        onConfirm(kategori, nominal.toLong(), periode, tglMulai.ifBlank { null }, tglAkhir.ifBlank { null })
                    }
                },
                enabled = !isLoading && kategori.isNotBlank() && nominal.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Simpan Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = if (isDark) iOSGroupedBackgroundDark else Color.White
    )
}

@Composable
fun HapusBudgetDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Verifikasi Password", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Silakan masukkan password Anda untuk menghapus budget kategori ini.")
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal")
            }
        }
    )
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MotivationCarousel(
    motivasi: List<MotivasiItem>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { motivasi.size }
    val scope = rememberCoroutineScope()
    
    // Auto-scroll logic
    LaunchedEffect(pagerState) {
        while (true) {
            kotlinx.coroutines.delay(6000)
            if (motivasi.size > 1) {
                val nextSlide = (pagerState.currentPage + 1) % motivasi.size
                pagerState.animateScrollToPage(nextSlide)
            }
        }
    }

    val backgroundColor = if (isDark) iOSSecondaryBackgroundDark else Color.White
    val textPrimary = if (isDark) Color.White else iOSLabelLight
    val textSecondary = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFB74D), Color(0xFFFF8A65))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Motivasi Hari Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Inspirasi untuk perjalanan finansialmu",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (motivasi.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1}/${motivasi.size}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Carousel Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val item = motivasi[page]
                    MotivationSlide(item = item, isDark = isDark)
                }

                // Dots Navigation
                if (motivasi.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(motivasi.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (isSelected) 24.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) StitchPrimary else textSecondary.copy(alpha = 0.3f)
                                    )
                                    .animateContentSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MotivationSlide(item: MotivasiItem, isDark: Boolean) {
    if (item.tipe == "image" && !item.foto.isNullOrEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "http://10.0.2.2:8000/storage/${item.foto}",
                contentDescription = "Motivasi",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                            startY = 300f
                        )
                    )
            )
            if (!item.isi.isNullOrEmpty()) {
                Text(
                    text = "\"${item.isi}\"",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp),
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Decorative quotes
            Text(
                text = "\"",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = StitchPrimary.copy(alpha = 0.1f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-8).dp, y = (-16).dp)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.isi ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else iOSLabelLight,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(20.dp).height(2.dp).background(StitchPrimary.copy(alpha = 0.3f), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KasSaku",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = StitchPrimary.copy(alpha = 0.5f),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.width(20.dp).height(2.dp).background(StitchPrimary.copy(alpha = 0.3f), CircleShape))
                }
            }

            Text(
                text = "\"",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = StitchPrimary.copy(alpha = 0.1f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 16.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }
    }
}

@Composable
fun CashflowChartsSection(
    data: com.example.kassaku.data.remote.model.StatistikData,
    isDark: Boolean
) {
    val periodOrder = listOf("7d", "30d", "3m", "12m")
    val periodLabel = mapOf(
        "7d" to "7 Hari",
        "30d" to "30 Hari",
        "3m" to "3 Bulan",
        "12m" to "12 Bulan"
    )

    val availableSeries = data.cashflowSeries ?: emptyMap()
    val defaultPeriod = if (!data.defaultCashflowPeriod.isNullOrBlank()) data.defaultCashflowPeriod else "30d"
    var selectedPeriod by remember(data, defaultPeriod) {
        mutableStateOf(
            when {
                availableSeries.containsKey(defaultPeriod) -> defaultPeriod!!
                availableSeries.containsKey("30d") -> "30d"
                availableSeries.isNotEmpty() -> availableSeries.keys.first()
                else -> "30d"
            }
        )
    }

    val currentSeries = availableSeries[selectedPeriod]
    val labels = currentSeries?.labels ?: data.labels
    val income = currentSeries?.income ?: data.pemasukan
    val expense = currentSeries?.expense ?: data.pengeluaran
    val net = currentSeries?.net ?: if (data.net.isNotEmpty()) data.net else income.zip(expense) { i, e -> i - e }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Grafik Arus Kas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else iOSLabelLight
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periodOrder.forEach { key ->
                val selected = key == selectedPeriod
                FilterChip(
                    selected = selected,
                    onClick = { selectedPeriod = key },
                    label = {
                        Text(
                            text = periodLabel[key] ?: key,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StitchPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        CashflowTrendChart(
            labels = labels,
            income = income,
            expense = expense,
            net = net,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        NetBarChart(
            labels = labels,
            net = net,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        CashflowInsightCard(
            periodKey = selectedPeriod,
            periodLabel = periodLabel[selectedPeriod] ?: selectedPeriod,
            labels = labels,
            income = income,
            expense = expense,
            net = net,
            series = currentSeries,
            isDark = isDark
        )
    }
}

@Composable
fun CashflowTrendChart(
    labels: List<String>,
    income: List<Double>,
    expense: List<Double>,
    net: List<Double>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceColor = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight
    val textSecondary = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChartLegendItem(label = "Masuk", color = StitchPrimary)
                ChartLegendItem(label = "Keluar", color = StitchAccentRed)
                ChartLegendItem(label = "Net", color = Color(0xFF334155))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 26.dp)
            ) {
                if (labels.isEmpty()) return@Canvas

                val allValues = (income + expense + net + listOf(0.0))
                val maxValue = allValues.maxOrNull() ?: 1.0
                val minValue = allValues.minOrNull() ?: 0.0
                val range = (maxValue - minValue).coerceAtLeast(1.0)

                fun yOf(value: Double): Float {
                    val pct = (value - minValue) / range
                    return (size.height - (pct * size.height)).toFloat()
                }

                val spacing = size.width / (labels.size - 1).coerceAtLeast(1)

                for (i in 0..4) {
                    val y = i * (size.height / 4f)
                    drawLine(
                        color = textSecondary.copy(alpha = 0.12f),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (minValue <= 0.0 && maxValue >= 0.0) {
                    val zeroY = yOf(0.0)
                    drawLine(
                        color = Color(0xFF334155).copy(alpha = 0.35f),
                        start = androidx.compose.ui.geometry.Offset(0f, zeroY),
                        end = androidx.compose.ui.geometry.Offset(size.width, zeroY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }

                fun drawSeries(values: List<Double>, color: Color, dashed: Boolean = false) {
                    if (values.size < 2) return
                    val path = androidx.compose.ui.graphics.Path()
                    val points = values.mapIndexed { idx, v ->
                        androidx.compose.ui.geometry.Offset(idx * spacing, yOf(v))
                    }

                    path.moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val c1 = androidx.compose.ui.geometry.Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                        val c2 = androidx.compose.ui.geometry.Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                        path.cubicTo(c1.x, c1.y, c2.x, c2.y, p1.x, p1.y)
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round,
                            pathEffect = if (dashed) androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 7f)) else null
                        )
                    )
                }

                drawSeries(income, StitchPrimary)
                drawSeries(expense, StitchAccentRed)
                drawSeries(net, Color(0xFF334155), dashed = true)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 44.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun NetBarChart(
    labels: List<String>,
    net: List<Double>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceColor = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight
    val textSecondary = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Net Arus Kas",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else iOSLabelLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(modifier = Modifier.fillMaxSize()) {
                if (labels.isEmpty()) return@Canvas

                val values = net + listOf(0.0)
                val maxAbs = (values.maxOfOrNull { kotlin.math.abs(it) } ?: 1.0).coerceAtLeast(1.0)
                val centerY = size.height / 2f
                val barWidth = (size.width / labels.size.coerceAtLeast(1)) * 0.55f
                val spacing = size.width / labels.size.coerceAtLeast(1)

                drawLine(
                    color = textSecondary.copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(0f, centerY),
                    end = androidx.compose.ui.geometry.Offset(size.width, centerY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )

                net.forEachIndexed { index, value ->
                    val barHeight = ((kotlin.math.abs(value) / maxAbs) * (size.height * 0.42f)).toFloat()
                    val left = index * spacing + (spacing - barWidth) / 2f
                    val top = if (value >= 0) centerY - barHeight else centerY
                    val color = if (value >= 0) StitchPrimary else StitchAccentRed

                    drawRoundRect(
                        color = color.copy(alpha = 0.85f),
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                }
            }
        }
    }
}

@Composable
fun CashflowInsightCard(
    periodKey: String,
    periodLabel: String,
    labels: List<String>,
    income: List<Double>,
    expense: List<Double>,
    net: List<Double>,
    series: com.example.kassaku.data.remote.model.CashflowPeriodData?,
    isDark: Boolean
) {
    val totalNet = series?.totalNet ?: net.sum()
    val changePct = series?.changePct ?: 0.0
    val maxExpenseValue = series?.maxExpenseValue ?: (expense.maxOrNull() ?: 0.0)
    val maxExpenseLabel = series?.maxExpenseLabel ?: run {
        val idx = expense.indices.maxByOrNull { expense[it] } ?: -1
        if (idx >= 0 && idx < labels.size) labels[idx] else "-"
    }

    val changeColor = if (changePct >= 0) StitchPrimary else StitchAccentRed
    val surfaceColor = if (isDark) iOSSecondaryBackgroundDark else Color.White
    val textColor = if (isDark) Color.White else iOSLabelLight

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Insight $periodLabel",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Net periode ini ${formatCurrency(totalNet)} (${if (changePct >= 0) "+" else ""}${String.format(Locale.US, "%.1f", changePct)}%).",
                fontSize = 12.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pengeluaran puncak di $maxExpenseLabel sebesar ${formatCurrency(maxExpenseValue)}.",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(changeColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(((kotlin.math.abs(changePct) / 100.0).coerceIn(0.1, 1.0)).toFloat())
                        .background(changeColor, RoundedCornerShape(12.dp))
                )
            }
        }
    }
}
