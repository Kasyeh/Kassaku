package com.example.kassaku.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.ExperimentalMaterialApi
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
import com.example.kassaku.ui.theme.KassakuSpacing
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.WarningAmber
import com.example.kassaku.data.remote.model.DreamForecastItem
import com.example.kassaku.data.remote.model.ImpianItem
import com.example.kassaku.data.remote.model.BudgetKategoriItem
import com.example.kassaku.data.remote.model.CashflowPeriodData
import com.example.kassaku.data.remote.model.MotivasiItem
import com.example.kassaku.data.remote.model.RiwayatItem
import com.example.kassaku.data.remote.model.StatistikData
import com.example.kassaku.data.remote.model.StatistikSummaryData
import com.example.kassaku.ui.components.form.TransactionFormSheet
import com.example.kassaku.ui.components.form.BudgetFormSheet
import com.example.kassaku.ui.components.form.TransactionFormState
import com.example.kassaku.viewmodel.BudgetActionResult
import com.example.kassaku.viewmodel.PemasukanResult
import com.example.kassaku.viewmodel.PengeluaranResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

private enum class CashflowChartUiState {
    Loading,
    Empty,
    Sparse,
    Ready
}

private data class CashflowResolvedData(
    val periodKey: String,
    val periodLabel: String,
    val series: CashflowPeriodData,
    val uiState: CashflowChartUiState,
    val selectedIndex: Int
)

private val periodOrder = listOf("7d", "30d", "3m", "12m")

private val periodLabelMap = mapOf(
    "7d" to "7 Hari",
    "30d" to "30 Hari",
    "3m" to "3 Bulan",
    "12m" to "12 Bulan"
)

private fun resolveCashflowPeriodData(
    data: StatistikData,
    periodKey: String
): CashflowPeriodData {
    data.cashflowSeries?.get(periodKey)?.let { series ->
        if (series.labels.isNotEmpty()) {
            return series
        }
    }

    val fallbackIncome = data.pemasukan
    val fallbackExpense = data.pengeluaran
    val fallbackNet = if (data.net.isNotEmpty()) {
        data.net
    } else {
        fallbackIncome.zip(fallbackExpense) { income, expense -> income - expense }
    }

    return CashflowPeriodData(
        labels = data.labels,
        income = fallbackIncome,
        expense = fallbackExpense,
        net = fallbackNet,
        totalIncome = fallbackIncome.sum(),
        totalExpense = fallbackExpense.sum(),
        totalNet = fallbackNet.sum(),
        changePct = 0.0,
        maxExpenseLabel = data.labels.getOrNull(
            fallbackExpense.indices.maxByOrNull { fallbackExpense[it] } ?: -1
        ),
        maxExpenseValue = fallbackExpense.maxOrNull() ?: 0.0
    )
}

private fun resolveCashflowUiState(series: CashflowPeriodData): CashflowChartUiState {
    if (series.labels.isEmpty()) {
        return CashflowChartUiState.Empty
    }

    val nonZeroPoints = series.income.indices.count { index ->
        (series.income.getOrElse(index) { 0.0 } != 0.0) ||
            (series.expense.getOrElse(index) { 0.0 } != 0.0) ||
            (series.net.getOrElse(index) { 0.0 } != 0.0)
    }

    return when {
        nonZeroPoints == 0 -> CashflowChartUiState.Empty
        nonZeroPoints == 1 -> CashflowChartUiState.Sparse
        else -> CashflowChartUiState.Ready
    }
}

private fun isDailyCashflowPeriod(periodKey: String): Boolean = periodKey == "7d" || periodKey == "30d"

private fun safeSelectedIndex(labels: List<String>, currentIndex: Int): Int {
    if (labels.isEmpty() || currentIndex < 0) {
        return -1
    }
    return currentIndex.coerceAtMost(labels.lastIndex)
}

private fun formatSignedPercent(value: Double): String {
    val sign = if (value > 0) "+" else ""
    return "$sign${String.format(Locale.US, "%.1f", value)}%"
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StatistikScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    onNavigateToImpian: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textPrimary = if (isDark) iOSLabelDark else iOSLabelLight

    val statistikData by homeViewModel.statistikData.collectAsStateWithLifecycle()
    val balanceData by homeViewModel.balanceData.collectAsStateWithLifecycle()
    val currencyCode = balanceData?.currency ?: "IDR"
    val currencyFormat = balanceData?.currencyFormat ?: "standard"
    val impianUiState by homeViewModel.impianUiState.collectAsStateWithLifecycle()
    val budgetActionResult by homeViewModel.budgetActionResult.collectAsStateWithLifecycle()
    val aiInsightState by homeViewModel.aiInsightState.collectAsStateWithLifecycle()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showHapusBudgetDialog by remember { mutableStateOf(false) }
    var showPemasukanSheet by remember { mutableStateOf(false) }
    var showPengeluaranSheet by remember { mutableStateOf(false) }
    var pemasukanFormState by remember { mutableStateOf<TransactionFormState>(TransactionFormState.Idle) }
    var pengeluaranFormState by remember { mutableStateOf<TransactionFormState>(TransactionFormState.Idle) }
    var budgetIdToDelete by remember { mutableStateOf<Int?>(null) }
    var selectedCashflowPeriod by remember { mutableStateOf("30d") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Ringkasan", "Grafik", "Anggaran")
    LaunchedEffect(homeViewModel, userId) {
        launch {
            homeViewModel.pemasukanResult.collectLatest { result ->
                when (result) {
                    is PemasukanResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        homeViewModel.resetPemasukanResult()
                        pemasukanFormState = TransactionFormState.Success
                        showPemasukanSheet = false
                        homeViewModel.loadBalanceData(userId)
                        homeViewModel.fetchStatistik(userId)
                    }
                    is PemasukanResult.Error -> {
                        pemasukanFormState = TransactionFormState.Error(result.message)
                        homeViewModel.resetPemasukanResult()
                    }
                    else -> Unit
                }
            }
        }
        launch {
            homeViewModel.pengeluaranResult.collectLatest { result ->
                when (result) {
                    is PengeluaranResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        homeViewModel.resetPengeluaranResult()
                        pengeluaranFormState = TransactionFormState.Success
                        showPengeluaranSheet = false
                        homeViewModel.loadBalanceData(userId)
                        homeViewModel.fetchStatistik(userId)
                    }
                    is PengeluaranResult.Error -> {
                        pengeluaranFormState = TransactionFormState.Error(result.message)
                        homeViewModel.resetPengeluaranResult()
                    }
                    else -> Unit
                }
            }
        }
    }

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

    LaunchedEffect(statistikData) {
        val defaultPeriod = statistikData?.defaultCashflowPeriod
        val availableSeries = statistikData?.cashflowSeries.orEmpty()
        selectedCashflowPeriod = when {
            !defaultPeriod.isNullOrBlank() && availableSeries.containsKey(defaultPeriod) -> defaultPeriod
            availableSeries.containsKey("30d") -> "30d"
            availableSeries.isNotEmpty() -> availableSeries.keys.first()
            else -> "30d"
        }
    }

    LaunchedEffect(selectedCashflowPeriod, userId) {
        if (userId != 0) {
            homeViewModel.fetchAiInsight(userId, selectedCashflowPeriod)
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
                Spacer(modifier = Modifier.height(16.dp))
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
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = textPrimary,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title, 
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            selectedContentColor = StitchPrimary,
                            unselectedContentColor = textPrimary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Content
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = KassakuSpacing.screenHorizontal,
                    end = KassakuSpacing.screenHorizontal,
                    top = 8.dp, // Reduced top padding since TabRow is above
                    bottom = KassakuSpacing.listBottom
                ),
                verticalArrangement = Arrangement.spacedBy(KassakuSpacing.sectionGap)
            ) {

                when (selectedTabIndex) {
                    0 -> { // Ringkasan


            item {
                val data = statistikData
                if (data != null) {
                    MonthlySummarySection(
                        summary = data.summary,
                        fallbackSeries = resolveCashflowPeriodData(data, selectedCashflowPeriod),
                        isDark = isDark,
                        currencyCode = currencyCode,
                        currencyFormat = currencyFormat
                    )
                }
            }

            item {
                val data = statistikData
                if (data != null) {
                    FinancialInsightsSection(
                        data = data,
                        selectedPeriod = selectedCashflowPeriod,
                        aiInsightState = aiInsightState,
                        isDark = isDark,
                        currencyCode = currencyCode,
                        currencyFormat = currencyFormat
                    )
                }
            }

                        item {
                            val data = statistikData
                            if (data != null) {
                                RecentActivitySection(
                                    transactions = data.recentTransactions.orEmpty(),
                                    isDark = isDark,
                                    currencyCode = currencyCode,
                                    currencyFormat = currencyFormat
                                )
                            }
                        }
                    }
                    1 -> { // Grafik
                        item {
                            val data = statistikData
                            if (data != null) {
                                CashflowChartsSection(
                                    data = data,
                                    isDark = isDark,
                                    selectedPeriod = selectedCashflowPeriod,
                                    onPeriodChange = { selectedCashflowPeriod = it },
                                    includeCategoryChart = true,
                                    currencyCode = currencyCode,
                                    currencyFormat = currencyFormat
                                )
                            }
                        }
            
                        item {
                            val data = statistikData
                            if (data != null) {
                                ProgressComparisonSection(
                                    data = data,
                                    selectedPeriod = selectedCashflowPeriod,
                                    isDark = isDark,
                                    currencyCode = currencyCode,
                                    currencyFormat = currencyFormat
                                )
                            }
                        }
                    }
                    2 -> { // Anggaran
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
                                    isDark = isDark,
                                    currencyCode = currencyCode,
                                    currencyFormat = currencyFormat
                                )
                            }
                        }

            item {
                val data = statistikData
                val impianState = impianUiState
                if (data != null) {
                    DreamForecastSection(
                        forecastItems = data.dreamForecast.orEmpty(),
                        fallbackImpianItems = if (impianState is ImpianUiState.Success) impianState.impianItems else emptyList(),
                        isDark = isDark,
                        onNavigateToImpian = onNavigateToImpian
                    )
                }
            }

                    }
                }
            }

        BudgetFormSheet(
            isVisible = showBudgetDialog,
            isLoading = budgetActionResult is BudgetActionResult.Loading,
            onDismiss = { showBudgetDialog = false },
            onSubmit = { formData ->
                homeViewModel.simpanBudgetKategori(
                    userId = userId,
                    kategori = formData.category,
                    nominal = formData.amount,
                    periode = formData.period,
                    tanggalMulai = formData.startDate,
                    tanggalAkhir = formData.endDate
                )
            },
            isDark = isDark
        )

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

        TransactionFormSheet(
            isVisible = showPemasukanSheet,
            isExpense = false,
            formState = pemasukanFormState,
            onDismiss = { showPemasukanSheet = false },
            onSubmit = { formData ->
                pemasukanFormState = TransactionFormState.Submitting
                homeViewModel.tambahPemasukan(
                    userId,
                    formData.amount.toLong(),
                    formData.category,
                    formData.notes,
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(java.util.Date(formData.date))
                )
            }
        )

        TransactionFormSheet(
            isVisible = showPengeluaranSheet,
            isExpense = true,
            formState = pengeluaranFormState,
            onDismiss = { showPengeluaranSheet = false },
            onSubmit = { formData ->
                pengeluaranFormState = TransactionFormState.Submitting
                homeViewModel.tambahPengeluaran(
                    userId,
                    formData.amount.toLong(),
                    formData.category,
                    formData.notes,
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(java.util.Date(formData.date))
                )
            }
        )
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
        Column(modifier = Modifier.padding(KassakuSpacing.cardInnerLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grafik Perkembangan",
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
        Column(modifier = Modifier.padding(KassakuSpacing.elementGap + 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = month, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDark) Color.White else Color.Black)
            TooltipRow(label = "Masuk", value = formatCurrency(masuk), color = StitchPrimary)
            TooltipRow(label = "Keluar", value = formatCurrency(keluar), color = StitchAccentRed)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = if(isDark) Color.Gray else Color.LightGray)
            TooltipRow(label = "Selisih", value = formatCurrency(net), color = netColor, isBold = true)
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

fun formatCurrency(amount: Double, currencyCode: String = "IDR", formatMode: String = "standard"): String {
    return com.example.kassaku.ui.components.formatCurrencyFlexible(amount, currencyCode, formatMode)
}

private fun fallbackSummaryFromSeries(series: CashflowPeriodData): StatistikSummaryData {
    return StatistikSummaryData(
        monthlyPemasukan = series.totalIncome,
        monthlyPengeluaran = series.totalExpense
    )
}



@Composable
private fun MonthlySummarySection(
    summary: StatistikSummaryData?,
    fallbackSeries: CashflowPeriodData,
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    val resolvedSummary = summary ?: fallbackSummaryFromSeries(fallbackSeries)
    Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)) {
        Text(
            text = "Bulan Ini",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else iOSLabelLight
        )
        MonthlyMoneyCard(
            title = "Pemasukan Bulan Ini",
            value = resolvedSummary.monthlyPemasukan,
            subtitle = "Total uang masuk bulan berjalan",
            toneColor = StitchPrimary,
            isDark = isDark,
            currencyCode = currencyCode,
            currencyFormat = currencyFormat
        )
        MonthlyMoneyCard(
            title = "Pengeluaran Bulan Ini",
            value = resolvedSummary.monthlyPengeluaran,
            subtitle = resolvedSummary.targetPengeluaran?.let { "Target ${formatCurrency(it)}" } ?: "Target belum diatur",
            toneColor = StitchAccentRed,
            isDark = isDark,
            progress = resolvedSummary.targetProgressPercent,
            isWarning = resolvedSummary.isOverBudget,
            currencyCode = currencyCode,
            currencyFormat = currencyFormat
        )
    }
}

@Composable
private fun MonthlyMoneyCard(
    title: String,
    value: Double,
    subtitle: String,
    toneColor: Color,
    isDark: Boolean,
    progress: Double? = null,
    isWarning: Boolean = false,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) iOSGroupedBackgroundDark else Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, toneColor.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KassakuSpacing.cardInnerLarge),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight)
            Text(
                text = formatCurrency(value, currencyCode, currencyFormat),
                fontSize = if (formatCurrency(value, currencyCode, currencyFormat).length > 18) 24.sp else 28.sp,
                fontWeight = FontWeight.Black,
                color = toneColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.sp
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isWarning) StitchAccentRed else if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            progress?.let {
                LinearProgressIndicator(
                    progress = { (it / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp)),
                    color = if (isWarning) StitchAccentRed else toneColor,
                    trackColor = toneColor.copy(alpha = 0.12f)
                )
            }
        }
    }
}


@Composable
fun FinancialInsightsSection(
    data: StatistikData,
    selectedPeriod: String,
    aiInsightState: com.example.kassaku.viewmodel.AiInsightUiState,
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    val summary = data.summary

    // Use the top-level 6-month series (same as backend's buildSixMonthInsights) for fallbacks
    val sixMonthLabels = data.labels
    val sixMonthNet = if (data.net.isNotEmpty()) data.net else {
        data.pemasukan.zip(data.pengeluaran) { inc, exp -> inc - exp }
    }
    val sixMonthPengeluaran = data.pengeluaran

    // Always prefer summary from API (computed by backend using 6-month data, same as Web)
    val avgSavings = summary?.avgSavings ?: if (sixMonthNet.isNotEmpty()) sixMonthNet.average() else 0.0

    // Fallback: require at least 2 active months (matching backend logic)
    val activeMonthsWithExp = sixMonthPengeluaran.count { it > 0.0 }
    val activeMonthsWithNet = sixMonthNet.count { it != 0.0 }

    val fallbackMostWasteful = if (activeMonthsWithExp >= 2) {
        val idx = sixMonthPengeluaran.indices.maxByOrNull { sixMonthPengeluaran[it] } ?: -1
        if (idx >= 0) sixMonthLabels.getOrNull(idx) else null
    } else null

    val fallbackMostProductive = if (activeMonthsWithNet >= 2) {
        val idx = sixMonthNet.indices.maxByOrNull { sixMonthNet[it] } ?: -1
        if (idx >= 0) sixMonthLabels.getOrNull(idx) else null
    } else null

    val fallbackTrend = if (sixMonthNet.size >= 2) {
        val last = sixMonthNet.last()
        val prev = sixMonthNet[sixMonthNet.size - 2]
        if (last > prev) "Meningkat" else if (last < prev) "Menurun" else "Stabil"
    } else "Stabil"

    val trend = summary?.trend ?: fallbackTrend
    val mostProductive = summary?.mostProductiveMonth ?: fallbackMostProductive ?: "-"
    val mostWasteful = summary?.mostWastefulMonth ?: fallbackMostWasteful ?: "-"

    // AI Insight Card
    AiInsightCard(aiInsightState = aiInsightState, isDark = isDark)
    
    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Tips Keuangan",
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
            value = formatCurrency(avgSavings, currencyCode, currencyFormat),
            icon = Icons.Default.Payments,
            color = StitchPrimary,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
        InsightCard(
            title = "Bulan Terhemat",
            value = mostProductive,
            icon = Icons.Default.WorkspacePremium,
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
            value = mostWasteful,
            icon = Icons.Default.WarningAmber,
            color = StitchAccentRed,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
        InsightCard(
            title = "Tren Finansial",
            value = trend,
            icon = when (trend) {
                "Meningkat" -> Icons.AutoMirrored.Filled.TrendingUp
                "Menurun" -> Icons.AutoMirrored.Filled.TrendingDown
                else -> Icons.AutoMirrored.Filled.TrendingFlat
            },
            color = if (trend == "Meningkat") StitchPrimary else if (trend == "Menurun") StitchAccentRed else Color.Gray,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AiInsightCard(
    aiInsightState: com.example.kassaku.viewmodel.AiInsightUiState,
    isDark: Boolean
) {
    AnimatedVisibility(
        visible = aiInsightState !is com.example.kassaku.viewmodel.AiInsightUiState.Idle,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -20 })
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9) // Slate 800 or Slate 100
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(KassakuSpacing.cardInnerLarge)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = StitchPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "KasSaku AI Insight",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else iOSLabelLight
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (aiInsightState) {
                    is com.example.kassaku.viewmodel.AiInsightUiState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = StitchPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sedang menganalisis...",
                                fontSize = 14.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B) // Slate 400 or 500
                            )
                        }
                    }
                    is com.example.kassaku.viewmodel.AiInsightUiState.Success -> {
                        val parsedInsight = androidx.core.text.HtmlCompat.fromHtml(
                            aiInsightState.insight,
                            androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
                        ).toString()
                        
                        Text(
                            text = parsedInsight,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155) // Slate 300 or 700
                        )
                    }
                    is com.example.kassaku.viewmodel.AiInsightUiState.Error -> {
                        Text(
                            text = aiInsightState.message,
                            fontSize = 14.sp,
                            color = StitchAccentRed,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    else -> {}
                }
            }
        }
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
            modifier = Modifier.padding(KassakuSpacing.cardInner),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(text = title, fontSize = 10.sp, color = iOSSecondaryLabelLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun ProgressComparisonSection(
    data: StatistikData,
    selectedPeriod: String,
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    val series = remember(data, selectedPeriod) { resolveCashflowPeriodData(data, selectedPeriod) }
    val summary = data.summary
    val monthlyPemasukan = summary?.monthlyPemasukan ?: (series.income.lastOrNull() ?: 0.0)
    val monthlyPengeluaran = summary?.monthlyPengeluaran ?: (series.expense.lastOrNull() ?: 0.0)
    val prevMonthPemasukan = summary?.prevMonthPemasukan ?: if (series.income.size >= 2) series.income[series.income.size - 2] else 0.0
    val prevMonthPengeluaran = summary?.prevMonthPengeluaran ?: if (series.expense.size >= 2) series.expense[series.expense.size - 2] else 0.0

    val incDiff = monthlyPemasukan - prevMonthPemasukan
    val expDiff = monthlyPengeluaran - prevMonthPengeluaran

    // Determine trend for motivational text
    val nets = series.income.zip(series.expense) { inc, exp -> inc - exp }
    val trend = summary?.trend ?: if (nets.size >= 2) {
        val last = nets.last()
        val prev = nets[nets.size - 2]
        if (last > prev) "Meningkat" else if (last < prev) "Menurun" else "Stabil"
    } else "Stabil"
    val periodLabel = periodLabelMap[selectedPeriod] ?: selectedPeriod

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFF1E1E2D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perkembangan $periodLabel",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.5.sp
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pemasukan vs Lalu
            PerformaBarItem(
                label = "Uang Masuk vs Lalu",
                diff = incDiff,
                progress = if (prevMonthPemasukan > 0) (monthlyPemasukan / prevMonthPemasukan).toFloat().coerceIn(0f, 1f) else 1f,
                barColor = StitchPrimary,
                isDiffPositiveGood = true,
                currencyCode = currencyCode,
                currencyFormat = currencyFormat
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pengeluaran vs Lalu
            PerformaBarItem(
                label = "Uang Keluar vs Lalu",
                diff = expDiff,
                progress = if (prevMonthPengeluaran > 0) (monthlyPengeluaran / prevMonthPengeluaran).toFloat().coerceIn(0f, 1f) else 1f,
                barColor = StitchAccentRed,
                isDiffPositiveGood = false,
                currencyCode = currencyCode,
                currencyFormat = currencyFormat
            )

            // Motivational text
            Spacer(modifier = Modifier.height(28.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color.White.copy(alpha = 0.06f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "\"${if (trend == "Meningkat") "Keadaan keuangan pada $periodLabel membaik dibanding sebelumnya." else if (trend == "Menurun") "Hati-hati dengan belanja pada $periodLabel agar uangmu tetap sehat." else "Keadaan keuangan pada $periodLabel cenderung stabil."}\"",
                fontSize = 10.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PerformaBarItem(
    label: String,
    diff: Double,
    progress: Float,
    barColor: Color,
    isDiffPositiveGood: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    val isPositive = diff >= 0
    val diffColor = if (isDiffPositiveGood) {
        if (isPositive) Color(0xFF34D399) else Color(0xFFF87171)
    } else {
        if (isPositive) Color(0xFFF87171) else Color(0xFF34D399)
    }
    val arrow = if (isPositive) "↑" else "↓"
    val formattedDiff = formatCurrency(Math.abs(diff), currencyCode, currencyFormat)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = "$arrow $formattedDiff",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = diffColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = Color.White.copy(alpha = 0.05f),
        )
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
private fun SectionTitle(
    title: String,
    subtitle: String,
    isDark: Boolean,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else iOSLabelLight
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DreamForecastSection(
    forecastItems: List<DreamForecastItem>,
    fallbackImpianItems: List<ImpianItem>,
    isDark: Boolean,
    onNavigateToImpian: () -> Unit
) {
    val items = if (forecastItems.isNotEmpty()) {
        forecastItems
    } else {
        fallbackImpianItems.take(3).map { item ->
            DreamForecastItem(
                idImpian = item.idImpian,
                namaBarang = item.namaBarang,
                fotoBarang = item.fotoBarang,
                hargaBarang = item.hargaBarang,
                deadline = item.deadline,
                keterangan = item.keterangan,
                avgSavings = 0.0,
                reachPercent = item.persentaseProgress ?: 0.0
            )
        }
    }

    if (items.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(
            title = "Perkiraan Tabungan",
            subtitle = "Estimasi pencapaian mengikuti data statistik web",
            isDark = isDark,
            iconColor = StitchPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.take(3).forEach { item ->
                DreamForecastCard(
                    item = item,
                    isDark = isDark,
                    onClick = onNavigateToImpian
                )
            }
        }
    }
}

@Composable
private fun DreamForecastCard(
    item: DreamForecastItem,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val progress = (item.reachPercent / 100.0).toFloat().coerceIn(0f, 1f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) iOSGroupedBackgroundDark else Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KassakuSpacing.cardInner),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(StitchPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Stars, contentDescription = null, tint = StitchPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.namaBarang ?: "Impian",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color.White else iOSLabelLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatCurrency(item.hargaBarang ?: 0.0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${String.format(Locale.US, "%.1f", item.reachPercent)}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = StitchPrimary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = StitchPrimary,
                trackColor = StitchPrimary.copy(alpha = 0.12f)
            )

            Text(
                text = "Rata tabungan ${formatCurrency(item.avgSavings)} per bulan",
                fontSize = 11.sp,
                color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentActivitySection(
    transactions: List<RiwayatItem>,
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    if (transactions.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(
            title = "Aktivitas Terbaru",
            subtitle = "5 transaksi terbaru seperti halaman statistik web",
            isDark = isDark,
            iconColor = StitchPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) iOSGroupedBackgroundDark else Color.White)
        ) {
            Column(
                modifier = Modifier.padding(KassakuSpacing.cardInner),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                transactions.take(5).forEachIndexed { index, item ->
                    RecentTransactionRow(
                        item = item, 
                        isDark = isDark,
                        currencyCode = currencyCode,
                        currencyFormat = currencyFormat
                    )
                    if (index != transactions.take(5).lastIndex) {
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionRow(
    item: RiwayatItem,
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    val isIncome = item.tipe?.lowercase(Locale.ROOT) == "pemasukan"
    val tone = if (isIncome) StitchPrimary else StitchAccentRed
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tone.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = tone,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.kategori ?: "Tanpa Jenis",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else iOSLabelLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.tanggal ?: item.createdAt ?: "-",
                fontSize = 11.sp,
                color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "${if (isIncome) "+" else "-"}${formatCurrency(item.nominal ?: 0.0, currencyCode, currencyFormat)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = tone,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
                    text = "Perkiraan Tabungan",
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
        Column(modifier = Modifier.padding(KassakuSpacing.cardInner)) {
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
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Batas Belanja per Jenis",
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
                        text = "Belum ada batas belanja. Atur sekarang!",
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
                    isDark = isDark,
                    currencyCode = currencyCode,
                    currencyFormat = currencyFormat
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
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
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
        "investasi", "investment" -> Icons.AutoMirrored.Filled.TrendingUp
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
        Column(modifier = Modifier.padding(KassakuSpacing.cardInnerLarge)) {
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
                    Text(text = formatCurrency(budget.spent ?: 0.0, currencyCode, currencyFormat), fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Limit", fontSize = 10.sp, color = iOSSecondaryLabelLight)
                    Text(text = formatCurrency(budget.nominal, currencyCode, currencyFormat), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = statusColor)
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


@Composable
fun HapusBudgetDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Masukkan Kata Sandi", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Masukkan kata sandi kamu untuk menghapus batas belanja ini.")
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
    data: StatistikData,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    isDark: Boolean,
    includeCategoryChart: Boolean = false,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    val resolvedSeries = remember(data, selectedPeriod) {
        resolveCashflowPeriodData(data, selectedPeriod)
    }
    val periodLabel = periodLabelMap[selectedPeriod] ?: selectedPeriod
    val uiState = remember(resolvedSeries) { resolveCashflowUiState(resolvedSeries) }
    var selectedIndex by remember(selectedPeriod, resolvedSeries.labels) {
        mutableStateOf(-1)
    }
    val safeIndex = safeSelectedIndex(resolvedSeries.labels, selectedIndex)

    val resolvedData = CashflowResolvedData(
        periodKey = selectedPeriod,
        periodLabel = periodLabel,
        series = resolvedSeries,
        uiState = uiState,
        selectedIndex = safeIndex
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Analisis Arus Kas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else iOSLabelLight
            )
            Text(
                text = "Pantau pemasukan, pengeluaran, dan net cashflow pada periode yang paling relevan.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periodOrder.forEach { key ->
                FilterChip(
                    selected = key == selectedPeriod,
                    onClick = {
                        onPeriodChange(key)
                        selectedIndex = -1
                    },
                    label = {
                        Text(
                            text = periodLabelMap[key] ?: key,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StitchPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = if (isDark) iOSSecondaryBackgroundDark else Color.White,
                        labelColor = if (isDark) Color.White else iOSLabelLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CashflowSummaryCards(
            series = resolvedData.series,
            periodLabel = resolvedData.periodLabel,
            isDark = isDark,
            currencyCode = currencyCode,
            currencyFormat = currencyFormat
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (resolvedData.uiState == CashflowChartUiState.Empty) {
            CashflowEmptyStateCard(
                title = "Belum ada aktivitas pada periode ini",
                description = "Tambahkan pemasukan atau pengeluaran agar pola arus kas mulai terlihat.",
                isDark = isDark
            )
        } else {
            CashflowTrendChart(
                labels = resolvedData.series.labels,
                income = resolvedData.series.income,
                expense = resolvedData.series.expense,
                net = resolvedData.series.net,
                selectedIndex = resolvedData.selectedIndex,
                onSelectedIndexChange = { selectedIndex = it },
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            NetBarChart(
                labels = resolvedData.series.labels,
                net = resolvedData.series.net,
                selectedIndex = resolvedData.selectedIndex,
                onSelectedIndexChange = { selectedIndex = it },
                isDark = isDark
            )

            if (resolvedData.uiState == CashflowChartUiState.Sparse) {
                Spacer(modifier = Modifier.height(12.dp))
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = "Data masih terbatas. Insight akan semakin akurat setelah transaksi bertambah.",
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        CashflowInsightCard(
            resolved = resolvedData,
            isDark = isDark,
            currencyCode = currencyCode,
            currencyFormat = currencyFormat
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Grafik atas menampilkan tren masuk dan keluar. Grafik bawah menyorot net arus kas tiap titik periode.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight
        )

        if (includeCategoryChart) {
            Spacer(modifier = Modifier.height(KassakuSpacing.sectionGap))
            CategoryChartSection(
                data = data,
                selectedPeriod = selectedPeriod,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun CashflowSummaryCards(
    series: CashflowPeriodData,
    periodLabel: String,
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)) {
        Text(
            text = "Ringkasan $periodLabel",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight,
            letterSpacing = 1.sp
        )

        SummaryMetricCard(
            title = "Total Pemasukan",
            subtitle = "Akumulasi periode aktif",
            value = formatCurrency(series.totalIncome, currencyCode, currencyFormat),
            toneColor = StitchPrimary,
            isDark = isDark,
            modifier = Modifier.fillMaxWidth()
        )
        SummaryMetricCard(
            title = "Total Pengeluaran",
            subtitle = "Pengeluaran periode aktif",
            value = formatCurrency(series.totalExpense, currencyCode, currencyFormat),
            toneColor = StitchAccentRed,
            isDark = isDark,
            modifier = Modifier.fillMaxWidth()
        )

        SummaryMetricCard(
            title = "Net Cashflow",
            subtitle = "Selisih masuk vs keluar",
            value = formatCurrency(series.totalNet, currencyCode, currencyFormat),
            toneColor = when {
                series.totalNet > 0 -> StitchPrimary
                series.totalNet < 0 -> StitchAccentRed
                else -> if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
            },
            isDark = isDark,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    subtitle: String,
    value: String,
    toneColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = toneColor.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) iOSGroupedBackgroundDark else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            toneColor.copy(alpha = if (isDark) 0.28f else 0.18f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KassakuSpacing.cardInner)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(toneColor, toneColor.copy(alpha = 0.45f))
                        )
                    )
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title.uppercase(Locale.getDefault()),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight
                )
                Text(
                    text = value,
                    fontSize = if (value.length > 20) 18.sp else if (value.length > 16) 21.sp else 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = toneColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight
                )
            }
        }
    }
}

@Composable
private fun CashflowEmptyStateCard(
    title: String,
    description: String,
    isDark: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) iOSGroupedBackgroundDark else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PieChart,
                contentDescription = null,
                tint = StitchPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else iOSLabelLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CashflowTrendChart(
    labels: List<String>,
    income: List<Double>,
    expense: List<Double>,
    net: List<Double>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
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
        Column(modifier = Modifier.padding(KassakuSpacing.cardInner)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tren Uang Masuk dan Keluar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else iOSLabelLight
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChartLegendItem(label = "Masuk", color = StitchPrimary)
                    ChartLegendItem(label = "Keluar", color = StitchAccentRed)
                    ChartLegendItem(label = "Net", color = Color(0xFF334155))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 26.dp)
                    .pointerInput(labels, selectedIndex) {
                        detectTapGestures { offset ->
                            onSelectedIndexChange(calculateSelectedIndex(offset.x, size.width.toFloat(), labels.size))
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
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

                        points.forEachIndexed { index, point ->
                            val isSelected = index == selectedIndex
                            drawCircle(
                                color = color,
                                radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
                                center = point
                            )
                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = 8.dp.toPx(),
                                    center = point,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }

                    if (selectedIndex >= 0 && selectedIndex < labels.size) {
                        val x = selectedIndex * spacing
                        drawLine(
                            color = textSecondary.copy(alpha = 0.25f),
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, size.height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }

                    drawSeries(income, StitchPrimary)
                    drawSeries(expense, StitchAccentRed)
                    drawSeries(net, Color(0xFF334155), dashed = true)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEachIndexed { index, label ->
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = if (index == selectedIndex) StitchPrimary else textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 44.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                if (selectedIndex >= 0 && selectedIndex < labels.size) {
                    TooltipView(
                        month = labels[selectedIndex],
                        masuk = income.getOrElse(selectedIndex) { 0.0 },
                        keluar = expense.getOrElse(selectedIndex) { 0.0 },
                        isDark = isDark,
                        modifier = Modifier
                            .align(if (selectedIndex < labels.size / 2) Alignment.TopEnd else Alignment.TopStart)
                            .padding(8.dp)
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
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
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
        Column(modifier = Modifier.padding(KassakuSpacing.cardInner)) {
            Text(
                text = "Net Arus Kas",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else iOSLabelLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(labels, selectedIndex) {
                        detectTapGestures { offset ->
                            val widthPerItem = size.width.toFloat() / labels.size.coerceAtLeast(1)
                            onSelectedIndexChange((offset.x / widthPerItem).toInt().coerceIn(0, labels.lastIndex))
                        }
                    }
            ) {
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
                        val color = when {
                            value > 0 -> StitchPrimary
                            value < 0 -> StitchAccentRed
                            else -> textSecondary.copy(alpha = 0.45f)
                        }

                        drawRoundRect(
                            color = if (index == selectedIndex) color else color.copy(alpha = 0.65f),
                            topLeft = androidx.compose.ui.geometry.Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight.coerceAtLeast(4f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CashflowInsightCard(
    resolved: CashflowResolvedData,
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "standard"
) {
    val series = resolved.series
    val totalNet = series.totalNet
    val changePct = series.changePct
    val maxExpenseValue = series.maxExpenseValue
    val maxExpenseLabel = series.maxExpenseLabel ?: "-"
    val changeColor = if (changePct >= 0) StitchPrimary else StitchAccentRed
    val surfaceColor = if (isDark) iOSSecondaryBackgroundDark else Color.White
    val textColor = if (isDark) Color.White else iOSLabelLight
    val headline = when {
        totalNet > 0 -> "Arus kas periode ini masih sehat"
        totalNet < 0 -> "Pengeluaran periode ini lebih besar dari pemasukan"
        else -> "Belum ada pergerakan arus kas pada periode ini"
    }
    val description = if (maxExpenseValue > 0) {
        "Net cashflow ${if (changePct >= 0) "naik" else "turun"} ${formatSignedPercent(kotlin.math.abs(changePct))} dibanding periode sebelumnya. Pengeluaran tertinggi terjadi pada $maxExpenseLabel."
    } else {
        "Belum ada lonjakan pengeluaran di periode ini."
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(KassakuSpacing.cardInner)) {
            Text(
                text = "Insight ${resolved.periodLabel}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = headline,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = textColor.copy(alpha = 0.88f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Net: ${formatCurrency(totalNet, currencyCode, currencyFormat)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                if (maxExpenseValue > 0) {
                    Text(
                        text = "Puncak: ${formatCurrency(maxExpenseValue, currencyCode, currencyFormat)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
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
