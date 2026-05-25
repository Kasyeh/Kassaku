package com.example.kassaku.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import com.example.kassaku.ui.components.BottomNavItem
import com.example.kassaku.data.remote.model.BalanceData
import com.example.kassaku.data.remote.model.RiwayatItem
import com.example.kassaku.data.remote.model.StatistikData
import com.example.kassaku.data.remote.model.MotivasiItem
import com.example.kassaku.ui.components.formatCurrencyFlexible
import com.example.kassaku.ui.components.notifications.NotificationInboxSheet
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.viewmodel.NotificationUiState
import com.example.kassaku.viewmodel.PemasukanResult
import com.example.kassaku.viewmodel.PengeluaranResult
import com.example.kassaku.viewmodel.RiwayatUiState
import com.example.kassaku.viewmodel.TargetPengeluaranResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import kotlin.math.abs
import com.example.kassaku.ui.components.skeleton.HomeScreenSkeleton
import com.example.kassaku.ui.components.skeleton.SkeletonTransactionList
import com.example.kassaku.ui.components.form.TransactionFormSheet
import com.example.kassaku.ui.components.form.TransactionFormState
import com.example.kassaku.ui.components.form.TransactionFormData
import com.example.kassaku.ui.components.FinancialInsightCard
import com.example.kassaku.ui.components.HeroTotalBalanceCard
import com.example.kassaku.ui.components.MiniTrendChart
import com.example.kassaku.ui.components.ConfettiEffect
import com.example.kassaku.utils.formatDisplayDate
import com.example.kassaku.utils.formatDisplayTime
import com.example.kassaku.ui.components.SmartNudgeCard
import com.example.kassaku.utils.BalanceVisibilityPreferences
import com.example.kassaku.utils.isEmulator

// Data model for simplified transaction usage
data class Transaction(
    val id: String,
    val name: String,
    val date: String,
    val time: String? = null,
    val amount: Double,
    val type: TransactionType,
)

enum class TransactionType {
    INCOME, EXPENSE
}

// Track if welcome dialog has been shown this app session
private var hasShownWelcomeThisSession = false

class ThousandSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val formattedText = originalText.reversed().chunked(3).joinToString(".").reversed()
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val separators = (offset - 1).coerceAtLeast(0) / 3
                return offset + separators
            }
            override fun transformedToOriginal(offset: Int): Int {
                val separators = formattedText.take(offset).count { it == '.' }
                return offset - separators
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    navController: NavController,
    openNotificationInboxSignal: Int = 0,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    var showPemasukanSheet by remember { mutableStateOf(false) }
    var showPengeluaranSheet by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showOverBudgetAlert by remember { mutableStateOf(false) }
    var hasShownOverBudgetAlert by remember { mutableStateOf(false) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    val hapticManager = remember { com.example.kassaku.utils.HapticManager(context) }
    
    // Form states
    var pemasukanFormState by remember { mutableStateOf<TransactionFormState>(TransactionFormState.Idle) }
    var pengeluaranFormState by remember { mutableStateOf<TransactionFormState>(TransactionFormState.Idle) }
    var showConfetti by remember { mutableStateOf(false) }
    
    // Welcome Greeting States
    var showWelcomeDialog by remember { mutableStateOf(false) }
    val isNewUser = remember { mutableStateOf(false) }

    // iOS-inspired Colors
    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val surfaceColor = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight
    val labelColor = if (isDark) iOSLabelDark else iOSLabelLight
    val secondaryLabelColor = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight
    val accentColor = StitchPrimary
    val accentRed = StitchAccentRed

    LaunchedEffect(key1 = homeViewModel) {
        launch {
            homeViewModel.pemasukanResult.collectLatest { result ->
                if (result is PemasukanResult.Success) {
                    hapticManager.success()
                    showConfetti = true
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    homeViewModel.resetPemasukanResult()
                    pemasukanFormState = TransactionFormState.Success
                    showPemasukanSheet = false
                    homeViewModel.fetchRiwayatTransaksi(userId)
                    homeViewModel.loadBalanceData(userId)
                    homeViewModel.fetchStatistik(userId)
                } else if (result is PemasukanResult.Error) {
                    pemasukanFormState = TransactionFormState.Error(result.message)
                    homeViewModel.resetPemasukanResult()
                }
            }
        }
        launch {
            homeViewModel.pengeluaranResult.collectLatest { result ->
                if (result is PengeluaranResult.Success) {
                    hapticManager.success()
                    showConfetti = true
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    homeViewModel.resetPengeluaranResult()
                    pengeluaranFormState = TransactionFormState.Success
                    showPengeluaranSheet = false
                    homeViewModel.fetchRiwayatTransaksi(userId)
                    homeViewModel.loadBalanceData(userId)
                    homeViewModel.fetchStatistik(userId)
                } else if (result is PengeluaranResult.Error) {
                    pengeluaranFormState = TransactionFormState.Error(result.message)
                    homeViewModel.resetPengeluaranResult()
                }
            }
        }
        launch {
            homeViewModel.targetPengeluaranResult.collectLatest { result ->
                when (result) {
                    is TargetPengeluaranResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        homeViewModel.resetTargetPengeluaranResult()
                        if (result.isOverBudget) {
                            showOverBudgetAlert = true
                        }
                    }
                    is TargetPengeluaranResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        homeViewModel.resetTargetPengeluaranResult()
                    }
                    else -> {}
                }
            }
        }
    }

    val balanceData by homeViewModel.balanceData.collectAsStateWithLifecycle()
    val riwayatUiState by homeViewModel.riwayatUiState.collectAsStateWithLifecycle()
    val statistikData by homeViewModel.statistikData.collectAsStateWithLifecycle()
    val notificationUiState by homeViewModel.notificationUiState.collectAsStateWithLifecycle()
    val notificationUnreadCount by homeViewModel.notificationUnreadCount.collectAsStateWithLifecycle()
    val smartNudges by homeViewModel.smartNudges.collectAsStateWithLifecycle()
    val isHomeRefreshing by homeViewModel.isHomeRefreshing.collectAsStateWithLifecycle()
    val username = balanceData?.username ?: "User"
    val balanceVisibilityPreferences = remember(context) { BalanceVisibilityPreferences(context) }
    var isBalanceVisible by rememberSaveable {
        mutableStateOf(balanceVisibilityPreferences.isHomeBalanceVisible())
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(key1 = userId) {
        if (userId != 0) {
            homeViewModel.loadBalanceData(userId)
            homeViewModel.fetchRiwayatTransaksi(userId)
            homeViewModel.fetchStatistik(userId)
            homeViewModel.fetchNotifications()
            if (!hasShownWelcomeThisSession) {
                showWelcomeDialog = true
                hasShownWelcomeThisSession = true
            }
        }
    }

    LaunchedEffect(openNotificationInboxSignal) {
        if (openNotificationInboxSignal > 0) {
            showNotificationSheet = true
            homeViewModel.fetchNotifications()
        }
    }

    LaunchedEffect(balanceData) {
        val data = balanceData
        if (data != null && data.isOverBudget && !hasShownOverBudgetAlert) {
            showOverBudgetAlert = true
            hasShownOverBudgetAlert = true
        }
    }

    val transactions = remember(riwayatUiState) {
        when (val state = riwayatUiState) {
            is RiwayatUiState.Success -> {
                state.riwayatItems.take(3).map { item ->
                    val type = if (item.tipe?.lowercase(Locale.ROOT) == "pemasukan") TransactionType.INCOME else TransactionType.EXPENSE
                    val amount = item.nominal ?: 0.0
                    Transaction(
                        id = item.idTransaksi.toString(),
                        name = item.kategori ?: "Tanpa Jenis",
                        date = item.tanggal ?: "Tanggal tidak diketahui",
                        time = if (item.tanggal?.contains(" ") == true) item.tanggal.split(" ")[1].substring(0, 5) else null,
                        amount = if (type == TransactionType.EXPENSE) -abs(amount) else abs(amount),
                        type = type,
                    )
                }
            }
            else -> emptyList()
        }
    }

    val incomeVal = balanceData?.pemasukan?.toDoubleOrNull() ?: 0.0
    val expenseVal = balanceData?.pengeluaran?.toDoubleOrNull() ?: 0.0
    val isExpenseHigherThanIncome = expenseVal > incomeVal && incomeVal > 0

    val greetingDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID")).format(Date())
    }
    val isInitialLoading = balanceData == null && userId != 0

    val isEmulator = remember { isEmulator() }
    val infiniteTransition = rememberInfiniteTransition(label = "expenseBlink")
    val overspendingAlpha by if (isEmulator) {
        remember { mutableStateOf(0.7f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "expenseAlpha"
        )
    }

    if (isInitialLoading) {
        HomeScreenSkeleton(
            modifier = Modifier.fillMaxSize(),
            isDarkTheme = isDark
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isHomeRefreshing,
            onRefresh = {
                if (userId != 0) {
                    hapticManager.lightTick()
                    homeViewModel.refreshHomeScreen(userId)
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = KassakuSpacing.screenHorizontal,
                    end = KassakuSpacing.screenHorizontal,
                    top = KassakuSpacing.screenVertical,
                    bottom = KassakuSpacing.listBottom
                ),
                verticalArrangement = Arrangement.spacedBy(KassakuSpacing.cardGap)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val avatarUrl = balanceData?.avatar
                        val initial = if (username.isNotEmpty()) username.take(1).uppercase() else "?"
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(StitchPrimary, Color(0xFF60A5FA))))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(surfaceColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarUrl != null) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = initial,
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            brush = Brush.linearGradient(listOf(StitchPrimary, Color(0xFF60A5FA)))
                                        )
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = greetingDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() },
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryLabelColor,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Assalamu'alaikum,",
                                style = MaterialTheme.typography.bodySmall,
                                color = secondaryLabelColor,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = username,
                                style = MaterialTheme.typography.titleLarge,
                                color = labelColor,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap)
                    ) {
                        IconButton(
                            onClick = {
                                showNotificationSheet = true
                                homeViewModel.fetchNotifications()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, color = secondaryLabelColor.copy(alpha = 0.2f), shape = CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = labelColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                if (notificationUnreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(17.dp)
                                            .background(StitchNegative, CircleShape)
                                            .border(2.dp, surfaceColor, CircleShape)
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (notificationUnreadCount > 99) "99+" else notificationUnreadCount.toString(),
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        IconButton(
                            onClick = { navController.navigate("chatbot") },
                            modifier = Modifier
                                .size(44.dp)
                                .border(1.dp, color = secondaryLabelColor.copy(alpha = 0.2f), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SmartToy,
                                contentDescription = "Asisten AI",
                                tint = labelColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -30 },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400))
                ) {
                    val currentBalance = balanceData?.saldo?.toDoubleOrNull() ?: 0.0
                    val currencyCode = balanceData?.currency ?: "IDR"
                    val currencyFormat = balanceData?.currencyFormat ?: "standard"
                    HeroTotalBalanceCard(
                        balance = currentBalance,
                        income = balanceData?.pemasukan?.toDoubleOrNull() ?: 0.0,
                        expense = balanceData?.pengeluaran?.toDoubleOrNull() ?: 0.0,
                        targetExpense = balanceData?.targetPengeluaran?.toDoubleOrNull(),
                        currencyCode = currencyCode,
                        currencyFormat = currencyFormat,
                        isBalanceVisible = isBalanceVisible,
                        isExpenseHigherThanIncome = isExpenseHigherThanIncome,
                        overspendingAlpha = overspendingAlpha,
                        isDark = isDark,
                        onToggleVisibility = {
                            isBalanceVisible = !isBalanceVisible
                            balanceVisibilityPreferences.setHomeBalanceVisible(isBalanceVisible)
                        },
                        onExpenseSectionClick = { showTargetDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                HomePrimaryActionsRow(
                    onIncomeClick = {
                        hapticManager.lightTick()
                        showPemasukanSheet = true
                    },
                    onExpenseClick = {
                        hapticManager.lightTick()
                        showPengeluaranSheet = true
                    }
                )
                    }
                }

                // Tampilkan Smart Nudges jika ada
                if (smartNudges.isNotEmpty()) {
                    items(smartNudges.size) { index ->
                        SmartNudgeCard(
                            nudge = smartNudges[index],
                            isDark = isDark,
                            onActionClick = { actionType ->
                                when (actionType) {
                                    "NAVIGATE_IMPIAN", "NAVIGATE_TABUNGAN" -> {
                                        navController.navigate(com.example.kassaku.ui.components.BottomNavItem.Impian.route) {
                                            popUpTo(com.example.kassaku.AppDestinations.HOME_ROUTE) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    "NAVIGATE_RIWAYAT" -> {
                                        navController.navigate(com.example.kassaku.ui.components.BottomNavItem.Riwayat.route) {
                                            popUpTo(com.example.kassaku.AppDestinations.HOME_ROUTE) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    "NAVIGATE_ADD_TRANSACTION" -> showPengeluaranSheet = true
                                }
                            }
                        )
                    }
                }

                // Tampilkan Motivasi Slider
                val data = statistikData
                if (data != null && !data.motivasi.isNullOrEmpty()) {
                    item {
                        MotivationCarousel(
                            motivasi = data.motivasi,
                            isDark = isDark
                        )
                    }
                }

                item {
                    HomeSectionTitle(title = "Jelajahi Fitur")
                }
                item {
                    HomeExploreRow(
                        isDark = isDark,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(com.example.kassaku.AppDestinations.HOME_ROUTE) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                item {
                    HomeSectionTitle(title = "Ringkasan Keuangan")
                }
                item {
                    val income = balanceData?.pemasukan?.toDoubleOrNull() ?: 0.0
                    val expense = balanceData?.pengeluaran?.toDoubleOrNull() ?: 0.0
                    AnimatedVisibility(visible = isVisible) {
                        FinancialInsightCard(statistikData = statistikData, currentMonthExpense = expense, currentMonthIncome = income)
                    }
                }
                item {
                    AnimatedVisibility(visible = isVisible) {
                        MiniTrendChart(statistikData = statistikData, onChartClick = { navController.navigate(BottomNavItem.Statistik.route) {
                                            popUpTo(com.example.kassaku.AppDestinations.HOME_ROUTE) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        } })
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Catatan Terbaru",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = labelColor
                        )
                        Text(text = "Lihat Semua", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentColor, modifier = Modifier.clickable { navController.navigate(BottomNavItem.Riwayat.route) {
                                            popUpTo(com.example.kassaku.AppDestinations.HOME_ROUTE) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        } })
                    }
                }
                when (riwayatUiState) {
                    is RiwayatUiState.Loading, is RiwayatUiState.Idle -> { item { SkeletonTransactionList(itemCount = 3, isDarkTheme = isDark) } }
                    is RiwayatUiState.Success -> {
                        if (transactions.isEmpty()) {
                            item { Column(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Rounded.ReceiptLong, null, tint = secondaryLabelColor.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(modifier = Modifier.height(16.dp)); Text("Belum ada catatan", fontWeight = FontWeight.Medium, color = labelColor) } }
                        } else {
                            itemsIndexed(transactions) { _, transaction -> TransactionItemCard(transaction = transaction, isDark = isDark, currencyCode = balanceData?.currency ?: "IDR", currencyFormat = balanceData?.currencyFormat ?: "standard") }
                        }
                    }
                    is RiwayatUiState.Error -> { item { Text("Gagal memuat data", color = MaterialTheme.colorScheme.error) } }
                }
            }
            PullRefreshIndicator(
                refreshing = isHomeRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = surfaceColor,
                contentColor = StitchPrimary
            )
        }

        if (showConfetti) {
            ConfettiEffect(isActive = true, modifier = Modifier.fillMaxSize())
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(2500); showConfetti = false }
        }

        if (showOverBudgetAlert) {
            AlertDialog(
                onDismissRequest = { showOverBudgetAlert = false },
                title = { Text("Belanja Melebihi Batas") },
                text = { Text("Belanja bulan ini sudah melebihi batas yang kamu tetapkan.") },
                confirmButton = { Button(onClick = { showOverBudgetAlert = false }) { Text("Mengerti") } },
                shape = RoundedCornerShape(20.dp),
                containerColor = surfaceColor
            )
        }

        if (showTargetDialog) {
            SetTargetDialog(currentTarget = balanceData?.targetPengeluaran?.toDoubleOrNull() ?: 0.0, onDismissRequest = { showTargetDialog = false }, onConfirm = { newTarget -> showTargetDialog = false; homeViewModel.simpanTargetPengeluaran(userId, newTarget.toLong()) })
        }

        TransactionFormSheet(isVisible = showPemasukanSheet, isExpense = false, formState = pemasukanFormState, onDismiss = { showPemasukanSheet = false }, onSubmit = { formData -> pemasukanFormState = TransactionFormState.Submitting; homeViewModel.tambahPemasukan(userId, formData.amount.toLong(), formData.category, formData.notes, java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(java.util.Date(formData.date))) })
        TransactionFormSheet(isVisible = showPengeluaranSheet, isExpense = true, formState = pengeluaranFormState, onDismiss = { showPengeluaranSheet = false }, onSubmit = { formData -> pengeluaranFormState = TransactionFormState.Submitting; homeViewModel.tambahPengeluaran(userId, formData.amount.toLong(), formData.category, formData.notes, java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(java.util.Date(formData.date))) })

        NotificationInboxSheet(isVisible = showNotificationSheet, notificationState = notificationUiState, unreadCount = notificationUnreadCount, onDismiss = { showNotificationSheet = false }, onRetry = { homeViewModel.fetchNotifications() }, onMarkAllRead = { homeViewModel.markAllNotificationsAsRead() }, onItemClick = { })
        
        if (showWelcomeDialog) {
            WelcomeGreetingDialog(username = username, avatarUrl = balanceData?.avatar, isNewUser = isNewUser.value, onDismiss = { showWelcomeDialog = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeGreetingDialog(username: String, avatarUrl: String?, isNewUser: Boolean, onDismiss: () -> Unit) {
    val isDark = LocalIsDark.current
    val surfaceColor = if (isDark) Color(0xFF1C1C1E) else Color.White
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Box(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(28.dp)).background(surfaceColor).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(100.dp).shadow(20.dp, CircleShape, spotColor = StitchPrimary.copy(alpha = 0.5f)).border(3.dp, StitchPrimary, CircleShape).padding(4.dp).clip(CircleShape)) {
                        if (avatarUrl != null) {
                            AsyncImage(model = avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(StitchPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = StitchPrimary, modifier = Modifier.size(50.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = if (isNewUser) "Selamat Datang di KasSaku!" else "Selamat Datang Kembali,", style = MaterialTheme.typography.bodyLarge, color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = username, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (isDark) iOSLabelDark else iOSLabelLight, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = if (isNewUser) "Mari mulai kelola keuanganmu dengan lebih pintar dan bijak." else "Senang melihatmu lagi! Yuk cek catatan keuanganmu hari ini.", style = MaterialTheme.typography.bodyMedium, color = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)) {
                        Text("Lanjutkan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    )
}

@Composable
private fun HomeSectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val labelColor = if (isDark) iOSLabelDark else iOSLabelLight
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = labelColor,
        modifier = modifier.padding(
            start = KassakuSpacing.sectionTitleInset,
            top = KassakuSpacing.sectionTitleInset,
            bottom = KassakuSpacing.elementGap
        )
    )
}

@Composable
fun HomePrimaryActionsRow(
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap)
    ) {
        ActionButton(
            text = "Pemasukan",
            icon = Icons.Rounded.Add,
            backgroundColor = StitchPrimary,
            contentColor = Color.White,
            onClick = onIncomeClick,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "Pengeluaran",
            icon = Icons.Rounded.Remove,
            backgroundColor = StitchAccentRed,
            contentColor = Color.White,
            onClick = onExpenseClick,
            modifier = Modifier.weight(1f)
        )
    }
}

private data class HomeExploreItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val tint: Color
)

@Composable
fun HomeExploreRow(
    isDark: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        HomeExploreItem("Riwayat", Icons.Rounded.History, BottomNavItem.Riwayat.route, StitchPrimary),
        HomeExploreItem("Tabungan", Icons.Rounded.Stars, BottomNavItem.Impian.route, Color(0xFF059669)),
        HomeExploreItem("Ringkasan", Icons.Rounded.Analytics, BottomNavItem.Statistik.route, Color(0xFFD97706)),
        HomeExploreItem("Profil", Icons.Rounded.AccountCircle, BottomNavItem.Profil.route, Color(0xFF0284C7))
    )
    val surface = if (isDark) Color(0xFF1F2937) else Color.White
    val borderColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFE2E8F0)
    val textColor = if (isDark) Color(0xFFE5E7EB) else Color(0xFF334155)

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KassakuSpacing.chipRowGap),
        contentPadding = PaddingValues(horizontal = KassakuSpacing.sectionTitleInset)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .width(112.dp)
                    .height(104.dp)
                    .clickable { onNavigate(item.route) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(KassakuSpacing.elementGap),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(item.tint.copy(alpha = if (isDark) 0.18f else 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, contentDescription = item.title, tint = item.tint, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surface = if (isDark) Color(0xFF1F2937) else Color.White
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0)
    val textColor = if (isDark) Color(0xFFE5E7EB) else Color(0xFF334155)

    Card(
        modifier = modifier
            .height(124.dp)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = PremiumShadowPrimary,
                ambientColor = PremiumShadowSecondary
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(KassakuSpacing.cardInner),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = if (isDark) 0.14f else 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeatureMenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surface = if (isDark) Color(0xFF1F2937) else Color.White
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0)
    val labelColor = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = PremiumShadowPrimary,
                ambientColor = PremiumShadowSecondary
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KassakuSpacing.cardInnerLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = gradientColors.last().copy(alpha = 0.30f))
                    .background(Brush.linearGradient(gradientColors), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(KassakuSpacing.elementGap + 4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = labelColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryColor
                )
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, backgroundColor: Color, contentColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val gradientColors = if (backgroundColor == StitchPrimary) listOf(Color(0xFF10B981), Color(0xFF059669)) else if (backgroundColor == StitchAccentRed) listOf(Color(0xFFEF4444), Color(0xFFDC2626)) else listOf(backgroundColor, backgroundColor.copy(alpha = 0.85f))
    Box(modifier = modifier.height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp), spotColor = backgroundColor.copy(alpha = 0.3f)).clip(RoundedCornerShape(16.dp)).background(Brush.horizontalGradient(gradientColors)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: Transaction,
    isDark: Boolean,
    currencyCode: String = "IDR",
    currencyFormat: String = "compact"
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val surface = if (isDark) Color(0xFF1F2937) else Color.White
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0)
    val labelColor = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryLabel = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentColor = if (isIncome) StitchPrimary else StitchAccentRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = PremiumShadowPrimary, ambientColor = PremiumShadowSecondary),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KassakuSpacing.cardInner),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(44.dp).background(if (isIncome) StitchPrimary.copy(alpha = 0.10f) else StitchAccentRed.copy(alpha = 0.10f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Icon(if (isIncome) Icons.Rounded.Add else Icons.Rounded.Remove, null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(KassakuSpacing.elementGap + 2.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = labelColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(com.example.kassaku.utils.formatDisplayDate(transaction.date), style = MaterialTheme.typography.bodySmall, color = secondaryLabel)
            }
            Text(
                "${if (isIncome) "+" else "-"}${formatCurrencyFlexible(abs(transaction.amount), currencyCode, currencyFormat)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = if (isIncome) BudgetSafeGreen else labelColor,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetTargetDialog(currentTarget: Double, onDismissRequest: () -> Unit, onConfirm: (Double) -> Unit) {
    var targetAmount by remember { mutableStateOf(if (currentTarget > 0) currentTarget.toLong().toString() else "") }
    AlertDialog(onDismissRequest = onDismissRequest, shape = RoundedCornerShape(28.dp), title = { Text("Atur Batas Belanja", fontWeight = FontWeight.ExtraBold) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Atur batas maksimal belanja per bulan.", style = MaterialTheme.typography.bodyMedium, color = StitchTextSecondary); OutlinedTextField(value = targetAmount, onValueChange = { targetAmount = it.filter { c -> c.isDigit() } }, label = { Text("Batas Belanja") }, prefix = { Text("Rp ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = ThousandSeparatorVisualTransformation()) } }, confirmButton = { Button(onClick = { onConfirm(targetAmount.toDoubleOrNull() ?: 0.0) }, colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)) { Text("Simpan", color = StitchTextPrimary) } }, dismissButton = { TextButton(onClick = onDismissRequest) { Text("Batal") } }, containerColor = if(isSystemInDarkTheme()) StitchSurfaceDark else StitchSurfaceLight)
}

@Composable
fun AnimatedCurrency(
    amount: Double,
    currencyCode: String = "IDR",
    currencyFormat: String = "compact",
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    val targetFloat = amount.toFloat()
    val animatedAmount by animateFloatAsState(
        targetValue = targetFloat, 
        animationSpec = tween(1500, easing = FastOutSlowInEasing), 
        label = "amount"
    )
    
    val displayAmount = if (animatedAmount == targetFloat) amount else animatedAmount.toDouble()
    
    Text(
        text = formatCurrencyFlexible(displayAmount, currencyCode, currencyFormat),
        style = style,
        color = color,
        modifier = modifier,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}
