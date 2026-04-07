package com.example.kassaku.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.kassaku.ui.components.BottomNavItem
import com.example.kassaku.data.remote.model.BalanceData
import com.example.kassaku.data.remote.model.RiwayatItem
import com.example.kassaku.data.remote.model.StatistikData
import com.example.kassaku.ui.components.LogoutConfirmationDialog
import com.example.kassaku.ui.components.formatCurrencyFlexible
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.viewmodel.PemasukanResult
import com.example.kassaku.viewmodel.PengeluaranResult
import com.example.kassaku.viewmodel.RiwayatUiState
import com.example.kassaku.viewmodel.TargetPengeluaranResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import com.example.kassaku.ui.components.skeleton.HomeScreenSkeleton
import com.example.kassaku.ui.components.skeleton.SkeletonTransactionList
import com.example.kassaku.ui.components.form.TransactionFormSheet
import com.example.kassaku.ui.components.form.TransactionFormState
import com.example.kassaku.ui.components.form.TransactionFormData
import com.example.kassaku.ui.components.FinancialInsightCard
import com.example.kassaku.ui.components.MiniTrendChart
import com.example.kassaku.utils.formatDisplayDate
import com.example.kassaku.utils.formatDisplayTime

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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    var showPemasukanSheet by remember { mutableStateOf(false) }
    var showPengeluaranSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showOverBudgetAlert by remember { mutableStateOf(false) }
    var hasShownOverBudgetAlert by remember { mutableStateOf(false) }
    
    // Form states
    var pemasukanFormState by remember { mutableStateOf<TransactionFormState>(TransactionFormState.Idle) }
    var pengeluaranFormState by remember { mutableStateOf<TransactionFormState>(TransactionFormState.Idle) }

    // iOS-inspired Colors
    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val surfaceColor = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight
    val labelColor = if (isDark) iOSLabelDark else iOSLabelLight
    val secondaryLabelColor = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight
    val accentColor = StitchPrimary // Keep teal as single accent
    val accentRed = StitchAccentRed

    LaunchedEffect(key1 = homeViewModel) {
        launch {
            homeViewModel.pemasukanResult.collectLatest { result ->
                if (result is PemasukanResult.Success) {
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
    val targetPengeluaranResult by homeViewModel.targetPengeluaranResult.collectAsStateWithLifecycle()
    val username = balanceData?.username ?: "User"

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(key1 = userId) {
        if (userId != 0) {
            homeViewModel.loadBalanceData(userId)
            homeViewModel.fetchRiwayatTransaksi(userId)
            homeViewModel.fetchStatistik(userId)
        }
    }

    // Check for over budget when balance data is loaded
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
                        name = item.kategori ?: "Tanpa Kategori",
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // iOS-style: No colored header background, clean minimalist look

        Column(modifier = Modifier.fillMaxSize()) {
            // iOS-style Header with Large Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .statusBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Professional Finance Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Assalamu'alaikum,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryLabelColor,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = username,
                            style = MaterialTheme.typography.headlineSmall,
                            color = labelColor,
                            fontWeight = FontWeight.Bold 
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { /* Notification Action */ },
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, color = secondaryLabelColor.copy(alpha = 0.2f), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = "Notifikasi",
                                tint = labelColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, color = secondaryLabelColor.copy(alpha = 0.2f), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = labelColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Premium Balance Card
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -30 },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = PremiumShadowPrimary,
                                ambientColor = PremiumShadowSecondary
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (isDark) 
                                        listOf(FinanceCardGradientDarkStart, FinanceCardGradientDarkEnd)
                                    else 
                                        listOf(FinanceCardGradientStart, FinanceCardGradientEnd)
                                )
                            )
                    ) {
                         // Subtle background pattern or decoration could go here
                        
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Total Saldo",
                                style = MaterialTheme.typography.labelLarge,
                                color = secondaryLabelColor,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val currentBalance = balanceData?.saldo?.toDoubleOrNull() ?: 0.0
                            AnimatedCurrency(
                                amount = currentBalance,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-1).sp
                                ),
                                color = labelColor
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Summary Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Income
                                val income = balanceData?.pemasukan?.toDoubleOrNull() ?: 0.0
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(accentColor.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowDownward,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Pemasukan",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = secondaryLabelColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatCurrencyFlexible(income),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = labelColor
                                    )
                                }

                                // Separator
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(if (isDark) iOSSeparatorDark else iOSSeparatorLight)
                                )

                                // Expense
                                val expense = balanceData?.pengeluaran?.toDoubleOrNull() ?: 0.0
                                val target = balanceData?.targetPengeluaran?.toDoubleOrNull()
                                
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showTargetDialog = true }
                                        .padding(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(accentRed.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowUpward,
                                                contentDescription = null,
                                                tint = accentRed,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Pengeluaran",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = secondaryLabelColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatCurrencyFlexible(expense),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = labelColor
                                    )
                                    
                                    // Target Progress
                                    if (target != null && target > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val progress = (expense / target).toFloat().coerceIn(0f, 1f)
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .clip(RoundedCornerShape(1.dp)),
                                            color = if (progress >= 1f) accentRed else accentRed.copy(alpha = 0.7f),
                                            trackColor = accentRed.copy(alpha = 0.1f)
                                        )
                                    } else {
                                        // Hint to set target
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Set Batas",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accentColor,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } // Header Column Closes
            
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions & List with iOS spacing
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Action Buttons - iOS style
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionButton(
                                text = "Pemasukan",
                                icon = Icons.Rounded.Add,
                                backgroundColor = accentColor,
                                contentColor = Color.White,
                                onClick = { showPemasukanSheet = true },
                                modifier = Modifier.weight(1f)
                            )

                            ActionButton(
                                text = "Pengeluaran",
                                icon = Icons.Rounded.Remove,
                                backgroundColor = accentRed,
                                contentColor = Color.White,
                                onClick = { showPengeluaranSheet = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        ActionButton(
                            text = "Setor Impian",
                            icon = Icons.Filled.Paid,
                            backgroundColor = StitchPrimary,
                            contentColor = Color.White,
                            onClick = {
                                navController.navigate(BottomNavItem.Impian.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Financial Insight Card - iOS Finance style
                item {
                    val income = balanceData?.pemasukan?.toDoubleOrNull() ?: 0.0
                    val expense = balanceData?.pengeluaran?.toDoubleOrNull() ?: 0.0
                    
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { 40 },
                            animationSpec = tween(durationMillis = 500, delayMillis = 200)
                        ) + fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200))
                    ) {
                        FinancialInsightCard(
                            statistikData = statistikData,
                            currentMonthExpense = expense,
                            currentMonthIncome = income
                        )
                    }
                }

                // Mini Trend Chart - Compact 6-month view
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { 40 },
                            animationSpec = tween(durationMillis = 500, delayMillis = 300)
                        ) + fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 300))
                    ) {
                        MiniTrendChart(
                            statistikData = statistikData,
                            onChartClick = {
                                navController.navigate(BottomNavItem.Statistik.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }

                // iOS Section Header - Transaksi Terbaru
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transaksi Terbaru",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelColor,
                            letterSpacing = 0.sp
                        )
                        Text(
                            text = "Lihat Semua",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = accentColor,
                            modifier = Modifier.clickable {
                                navController.navigate(BottomNavItem.Riwayat.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }

                // Transaction List
                when (riwayatUiState) {
                    is RiwayatUiState.Loading, is RiwayatUiState.Idle -> {
                        item { 
                            SkeletonTransactionList(
                                itemCount = 3,
                                isDarkTheme = isDark,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    is RiwayatUiState.Success -> {
                        if (transactions.isEmpty()) {
                            // iOS-style aesthetic empty state
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(
                                                secondaryLabelColor.copy(alpha = 0.08f),
                                                RoundedCornerShape(16.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.ReceiptLong, 
                                            contentDescription = null, 
                                            tint = secondaryLabelColor.copy(alpha = 0.5f), 
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Belum ada transaksi",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = labelColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Transaksi akan muncul di sini",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = secondaryLabelColor
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(transactions) { index, transaction ->
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = slideInVertically(
                                        initialOffsetY = { 50 },
                                        animationSpec = tween(durationMillis = 500, delayMillis = index * 100)
                                    ) + fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = index * 100))
                                ) {
                                    TransactionItemCard(transaction = transaction, isDark = isDark)
                                }
                            }
                        }
                    }
                    is RiwayatUiState.Error -> {
                        item { Text("Gagal memuat data", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        } // Main Column for the screen content closes here

        // Over Budget Alert Dialog - iOS Style
        if (showOverBudgetAlert) {
            AlertDialog(
                onDismissRequest = { showOverBudgetAlert = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(accentRed.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = accentRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                title = { 
                    Text(
                        "Pengeluaran Melebihi Target", 
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                        color = labelColor,
                        modifier = Modifier.fillMaxWidth()
                    ) 
                },
                text = {
                    val expense = balanceData?.pengeluaran?.toDoubleOrNull() ?: 0.0
                    val target = balanceData?.targetPengeluaran?.toDoubleOrNull() ?: 0.0
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Pengeluaran bulan ini sudah melebihi batas yang kamu tetapkan.",
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            color = secondaryLabelColor,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            formatCurrencyFlexible(expense),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentRed,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "dari target ${formatCurrencyFlexible(target)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = secondaryLabelColor
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showOverBudgetAlert = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            "Mengerti", 
                            color = labelColor, 
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = surfaceColor
            )
        }

        // Set Target Dialog
        if (showTargetDialog) {
            SetTargetDialog(
                currentTarget = balanceData?.targetPengeluaran?.toDoubleOrNull() ?: 0.0,
                onDismissRequest = { showTargetDialog = false },
                onConfirm = { newTarget ->
                    showTargetDialog = false
                    homeViewModel.simpanTargetPengeluaran(userId, newTarget.toLong())
                }
            )
        }

        // Pemasukan Form Sheet
        TransactionFormSheet(
            isVisible = showPemasukanSheet,
            isExpense = false,
            formState = pemasukanFormState,
            onDismiss = { 
                showPemasukanSheet = false 
                pemasukanFormState = TransactionFormState.Idle
            },
            onSubmit = { formData ->
                pemasukanFormState = TransactionFormState.Submitting
                // Use yyyy-MM-dd HH:mm:ss to preserve time from System.currentTimeMillis()
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(formData.date))
                homeViewModel.tambahPemasukan(
                    userId, 
                    formData.amount.toLong(), 
                    formData.category.replaceFirstChar { it.uppercase() }, 
                    formData.notes,
                    dateStr
                )
            }
        )
        
        // Pengeluaran Form Sheet
        val budgetedCategories = remember(statistikData) {
            statistikData?.budgetKategori?.map { budget ->
                com.example.kassaku.ui.components.form.CategoryOption(
                    id = budget.id.toString(),
                    label = budget.kategori.replaceFirstChar { it.uppercase() }
                )
            }
        }

        TransactionFormSheet(
            isVisible = showPengeluaranSheet,
            isExpense = true,
            formState = pengeluaranFormState,
            customCategories = budgetedCategories,
            onDismiss = { 
                showPengeluaranSheet = false 
                pengeluaranFormState = TransactionFormState.Idle
            },
            onSubmit = { formData ->
                pengeluaranFormState = TransactionFormState.Submitting
                // Use yyyy-MM-dd HH:mm:ss to preserve time from System.currentTimeMillis()
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(formData.date))
                homeViewModel.tambahPengeluaran(
                    userId, 
                    formData.amount.toLong(), 
                    formData.category.replaceFirstChar { it.uppercase() }, 
                    formData.notes,
                    dateStr
                )
            }
        )

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismissRequest = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    homeViewModel.logout()
                },
                isDark = isDark
            )
        }
    }
}






@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // iOS-style: Slimmer button with subtle appearance
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = contentColor, 
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text.replace("\n", " "),
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

@Composable
fun TransactionItemCard(transaction: Transaction, isDark: Boolean) {
    val isIncome = transaction.type == TransactionType.INCOME
    val labelColor = if (isDark) iOSLabelDark else iOSLabelLight
    val secondaryLabel = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight
    val surfaceColor = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight
    
    val accentColor = if (isIncome) StitchPrimary else StitchAccentRed
    
    // Clean, flat list item design
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Detail view if needed */ }
            .padding(vertical = 12.dp, horizontal = 4.dp), // Less horizontal padding as it's in a list
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = if (isIncome) StitchPrimaryLight else Color(0x33EF4444),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIncome) Icons.Rounded.Add else Icons.Rounded.Remove,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Transaction Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = labelColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatDisplayDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryLabel
                )
                if (transaction.time != null) {
                    Text(
                        text = " • ${transaction.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryLabel
                    )
                }
            }
        }
        
        // Amount
        Text(
            text = "${if (isIncome) "+" else "-"}${formatCurrencyFlexible(abs(transaction.amount))}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isIncome) BudgetSafeGreen else labelColor
        )
    }
}


// Dialogs (Reusing logic)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetTargetDialog(
    currentTarget: Double,
    onDismissRequest: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var targetAmount by remember { mutableStateOf(if (currentTarget > 0) currentTarget.toLong().toString() else "") }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(28.dp),
        title = { 
            Text(
                "Set Target Bulanan", 
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Atur batas maksimal pengeluaran per bulan untuk membantu mengontrol keuangan Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StitchTextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Target Pengeluaran") },
                    placeholder = { Text("Contoh: 500000") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StitchPrimary,
                        focusedLabelColor = StitchPrimary
                    )
                )
                
                Text(
                    "Kosongkan atau isi 0 untuk menghapus target.",
                    style = MaterialTheme.typography.labelSmall,
                    color = StitchTextSecondary.copy(alpha = 0.7f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = targetAmount.toDoubleOrNull() ?: 0.0
                    onConfirm(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Target", color = StitchTextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal", color = StitchTextSecondary)
            }
        },
        containerColor = if(isSystemInDarkTheme()) StitchSurfaceDark else StitchSurfaceLight
    )
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    // Preview setup
}

@Composable
fun AnimatedCurrency(
    amount: Double,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "amount"
    )
    Text(
        text = formatCurrencyFlexible(animatedAmount),
        style = style,
        color = color,
        modifier = modifier
    )
}

