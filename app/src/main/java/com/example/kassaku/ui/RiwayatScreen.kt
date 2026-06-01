package com.example.kassaku.ui

import android.Manifest
import android.content.Context
import android.content.ContentValues
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import com.example.kassaku.ui.components.PremiumAlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.kassaku.data.remote.model.BalanceData
import com.example.kassaku.data.remote.model.RiwayatItem
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*
import com.example.kassaku.ui.components.EmptyStateLottie
import com.example.kassaku.ui.components.LogoutConfirmationDialog
import com.example.kassaku.viewmodel.ExportPdfResult
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.viewmodel.RiwayatUiState
import com.example.kassaku.utils.HapticManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import com.example.kassaku.utils.formatMillisToString
import com.example.kassaku.utils.savePdfToDownloads
import com.example.kassaku.utils.openPdfFile
import com.example.kassaku.utils.formatDisplayDate
import com.example.kassaku.utils.formatDisplayTime
import com.example.kassaku.ui.components.skeleton.SkeletonTransactionList
import com.example.kassaku.ui.components.formatCurrencyFlexible

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RiwayatScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    navController: androidx.navigation.NavController,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = LocalIsDark.current
    val hapticManager = remember { HapticManager(context) }

    // Colors based on theme/design
    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary

    // Filter states
    var selectedPeriode by remember { mutableStateOf<String?>(null) }
    var selectedJenis by remember { mutableStateOf<String?>(null) } // null, "pemasukan", "pengeluaran"
    var selectedSearch by remember { mutableStateOf("") }
    var selectedTanggal by remember { mutableStateOf<String?>(null) }
    var selectedBulan by remember { mutableStateOf<Int?>(null) }
    var selectedTahun by remember { mutableStateOf<Int?>(null) }
    
    var showFilterDialog by remember { mutableStateOf(false) }
    val exportPdfResult by homeViewModel.exportPdfResult.collectAsState()
    
    var showExportDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Fetch when filters change
    LaunchedEffect(userId, selectedPeriode, selectedJenis, selectedSearch, selectedTanggal, selectedBulan, selectedTahun) {
        homeViewModel.fetchRiwayatTransaksi(
            userId, 
            selectedPeriode, 
            selectedJenis, 
            selectedSearch.takeIf { it.isNotBlank() },
            selectedTanggal, 
            selectedBulan, 
            selectedTahun
        )
    }

    LaunchedEffect(key1 = homeViewModel) {
        launch {
            homeViewModel.logoutNavigationEvent.collect { onLogout() }
        }
    }

    val riwayatUiState by homeViewModel.riwayatUiState.collectAsState()
    val balanceData by homeViewModel.balanceData.collectAsState()
    val currencyCode = balanceData?.currency ?: "IDR"
    
    // Permission Launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            homeViewModel.exportPdf(
                userId = userId,
                periode = selectedPeriode,
                jenis = selectedJenis,
                search = selectedSearch.takeIf { it.isNotBlank() },
                tanggal = selectedTanggal,
                bulan = selectedBulan,
                tahun = selectedTahun
            )
        } else {
            Toast.makeText(context, "Izin penyimpanan diperlukan untuk menyimpan PDF", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Handle export PDF result
    LaunchedEffect(exportPdfResult) {
        when (val result = exportPdfResult) {
            is ExportPdfResult.Success -> {
                scope.launch(Dispatchers.IO) {
                    try {
                        val fileName = "laporan_riwayat_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
                        val uri = savePdfToDownloads(context, result.responseBody.bytes(), fileName)
                        withContext(Dispatchers.Main) {
                            if (uri != null) {
                                Toast.makeText(context, "PDF berhasil disimpan. Membuka...", Toast.LENGTH_SHORT).show()
                                openPdfFile(context, uri)
                            } else {
                                Toast.makeText(context, "Gagal menyimpan PDF (URI null)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    homeViewModel.resetExportPdfResult()
                }
            }
            is ExportPdfResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                homeViewModel.resetExportPdfResult()
            }
            else -> {}
        }
    }
    


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
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
                        text = "Catatan Keuangan",
                        color = textPrimary,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconModifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))

                        IconButton(
                            onClick = { 
                                hapticManager.lightTick()
                                showExportDialog = true 
                            },
                            modifier = iconModifier
                        ) {
                            Icon(Icons.Rounded.PictureAsPdf, "Export", tint = textPrimary, modifier = Modifier.size(20.dp))
                        }
                        
                        IconButton(
                            onClick = { 
                                hapticManager.lightTick()
                                showFilterDialog = true 
                            },
                            modifier = iconModifier
                        ) {
                            Icon(Icons.Rounded.Tune, "Filter", tint = textPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Area
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                // Segmented Filter Control (Jenis)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterTabButton(
                            text = "Semua",
                            selected = selectedJenis == null,
                            onClick = { 
                                hapticManager.lightTick()
                                selectedJenis = null 
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterTabButton(
                            text = "Uang Masuk",
                            selected = selectedJenis == "pemasukan",
                            onClick = { 
                                hapticManager.lightTick()
                                selectedJenis = "pemasukan" 
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterTabButton(
                            text = "Uang Keluar",
                            selected = selectedJenis == "pengeluaran",
                            onClick = { 
                                hapticManager.lightTick()
                                selectedJenis = "pengeluaran" 
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Search Bar Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = selectedSearch,
                        onValueChange = { selectedSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        placeholder = { Text("Cari catatan atau nominal") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = textSecondary)
                        },
                        trailingIcon = {
                            if (selectedSearch.isNotEmpty()) {
                                IconButton(onClick = { 
                                    hapticManager.lightTick()
                                    selectedSearch = "" 
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus", tint = textSecondary)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StitchPrimary,
                            unfocusedBorderColor = if(isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                            cursorColor = StitchPrimary
                        )
                    )
                }

                // Active Filters Pill Display
                val activeFilters = mutableListOf<String>()
                selectedPeriode?.let { 
                    val label = when(it) {
                        "hari_ini" -> "Hari Ini"
                        "minggu_ini" -> "Minggu Ini"
                        "bulan_ini" -> "Bulan Ini"
                        else -> it
                    }
                    activeFilters.add(label) 
                }
                selectedTanggal?.let { activeFilters.add(it) }
                selectedBulan?.let { 
                    val monthName = SimpleDateFormat("MMMM", Locale("id", "ID")).format(Calendar.getInstance().apply { set(Calendar.MONTH, it - 1) }.time)
                    activeFilters.add(monthName) 
                }
                selectedTahun?.let { activeFilters.add(it.toString()) }
                if (selectedSearch.isNotBlank()) {
                    activeFilters.add("Cari: $selectedSearch")
                }

                if (activeFilters.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            color = StitchPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            onClick = { 
                                hapticManager.lightTick()
                                showFilterDialog = true 
                            }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = activeFilters.joinToString(" • "), 
                                    style = MaterialTheme.typography.labelMedium, 
                                    color = StitchPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Hapus Filter",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            hapticManager.lightTick()
                                            selectedPeriode = null
                                            selectedSearch = ""
                                            selectedTanggal = null
                                            selectedBulan = null
                                            selectedTahun = null
                                        },
                                    tint = StitchPrimary
                                )
                            }
                        }
                    }
                }

                // Main Content List Area
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = riwayatUiState) {
                        is RiwayatUiState.Loading,
                        is RiwayatUiState.Idle -> {
                            SkeletonTransactionList(
                                itemCount = 6,
                                isDarkTheme = isDark,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                            )
                        }

                        is RiwayatUiState.Success -> {
                            val filteredItems = state.riwayatItems
                            
                            if (filteredItems.isEmpty()) {
                                EmptyStateLottie(
                                    message = "Catatan Tidak Ditemukan",
                                    subtitle = "Coba ubah kata kunci pencarian atau filter yang aktif.",
                                    isDark = isDark,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp)
                                )
                            } else {
                                val groupedItems = filteredItems.groupBy { 
                                    it.tanggal?.split(" ")?.get(0) ?: "Tidak Diketahui" 
                                }

                                // Calculate ledger metrics in real-time
                                val totalIncome = filteredItems.filter { it.tipe?.equals("pemasukan", ignoreCase = true) == true }.sumOf { it.nominal ?: 0.0 }
                                val totalExpense = filteredItems.filter { it.tipe?.equals("pengeluaran", ignoreCase = true) == true }.sumOf { it.nominal ?: 0.0 }
                                val netBalance = totalIncome - totalExpense

                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        horizontal = KassakuSpacing.screenHorizontal,
                                        vertical = KassakuSpacing.screenVertical
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(KassakuSpacing.sectionGap)
                                ) {
                                    // Ringkasan Arus Kas (Ledger summary bento card)
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(18.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                Text(
                                                    text = "Ringkasan Filter Aktif",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textPrimary
                                                )
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    // Pemasukan
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(20.dp)
                                                                    .clip(CircleShape)
                                                                    .background(StitchPrimary.copy(alpha = 0.15f)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(Icons.Rounded.ArrowDownward, null, tint = StitchPrimary, modifier = Modifier.size(12.dp))
                                                            }
                                                            Text("Pemasukan", fontSize = 12.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                                                        }
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            text = formatCurrencyFlexible(totalIncome, currencyCode),
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = StitchPrimary
                                                        )
                                                    }

                                                    // Pengeluaran
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(20.dp)
                                                                    .clip(CircleShape)
                                                                    .background(StitchNegative.copy(alpha = 0.15f)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(Icons.Rounded.ArrowUpward, null, tint = StitchNegative, modifier = Modifier.size(12.dp))
                                                            }
                                                            Text("Pengeluaran", fontSize = 12.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                                                        }
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            text = formatCurrencyFlexible(totalExpense, currencyCode),
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = StitchNegative
                                                        )
                                                    }
                                                }

                                                HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                                                // Net Savings
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Arus Kas Bersih", fontSize = 13.sp, color = textSecondary, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = if (netBalance >= 0) {
                                                            "+${formatCurrencyFlexible(netBalance, currencyCode)}"
                                                        } else {
                                                            "-${formatCurrencyFlexible(abs(netBalance), currencyCode)}"
                                                        },
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (netBalance >= 0) StitchPrimary else StitchNegative
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Transaction Statement date lists
                                    groupedItems.forEach { (date, items) ->
                                        item {
                                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                // Premium timeline Date Header
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(Brush.linearGradient(listOf(StitchPrimary, Color(0xFF8B5CF6))))
                                                    )
                                                    Text(
                                                        text = formatDisplayDate(date),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Black,
                                                        color = textPrimary,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }

                                                // Statement list Grouped Card
                                                Card(
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
                                                ) {
                                                    Column {
                                                        items.forEachIndexed { index, item ->
                                                            RiwayatItemRow(
                                                                item = item, 
                                                                isLast = index == items.lastIndex, 
                                                                isDark = isDark,
                                                                currencyCode = currencyCode,
                                                                onClick = {
                                                                    navController.navigate("transaction_detail/${item.idTransaksi}")
                                                                },
                                                                sharedTransitionScope = sharedTransitionScope,
                                                                animatedVisibilityScope = animatedVisibilityScope
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        is RiwayatUiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Gagal memuat catatan keuangan",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { homeViewModel.fetchRiwayatTransaksi(userId) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
                                ) {
                                    Text("Coba Lagi", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // --- Custom Bento Grid Filter Drawer Dialog --- //
        if (showFilterDialog) {
            FilterDialog(
                periode = selectedPeriode,
                tanggal = selectedTanggal,
                bulan = selectedBulan,
                tahun = selectedTahun,
                onFilterChange = { p, t, b, th ->
                    selectedPeriode = p
                    selectedTanggal = t
                    selectedBulan = b
                    selectedTahun = th
                },
                onDismiss = { showFilterDialog = false }
            )
        }
        
        // Export Laporan PDF Dialog
        if (showExportDialog) {
            ExportPdfDialog(
                onDismiss = { showExportDialog = false },
                onExport = {
                    showExportDialog = false
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            homeViewModel.exportPdf(
                                userId = userId,
                                periode = selectedPeriode,
                                jenis = selectedJenis,
                                search = selectedSearch.takeIf { it.isNotBlank() },
                                tanggal = selectedTanggal,
                                bulan = selectedBulan,
                                tahun = selectedTahun
                            )
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    } else {
                        homeViewModel.exportPdf(
                            userId = userId,
                            periode = selectedPeriode,
                            jenis = selectedJenis,
                            search = selectedSearch.takeIf { it.isNotBlank() },
                            tanggal = selectedTanggal,
                            bulan = selectedBulan,
                            tahun = selectedTahun
                        )
                    }
                }
            )
        }



        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismissRequest = { showLogoutDialog = false },
                onConfirm = {
                    hapticManager.lightTick()
                    showLogoutDialog = false
                    homeViewModel.logout()
                },
                isDark = isDark
            )
        }
    }
}

@Composable
fun FilterTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val activeBgColor = StitchPrimary
    val inactiveBgColor = Color.Transparent
    
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) activeBgColor else inactiveBgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else (if (isDark) Color(0xFF94A3B8) else StitchTextSecondary)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RiwayatItemRow(
    item: RiwayatItem,
    isLast: Boolean,
    isDark: Boolean,
    currencyCode: String,
    onClick: () -> Unit,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope
) {
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }

    with(sharedTransitionScope) {
        val isIncome = item.tipe?.equals("pemasukan", ignoreCase = true) == true
        val amountColor = if (isIncome) StitchPrimary else StitchNegative
        val icon = if (isIncome) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward
        val iconBgColor = if (isIncome) StitchPrimary.copy(alpha = 0.12f) else StitchNegative.copy(alpha = 0.12f)
        val iconTint = if (isIncome) StitchPrimary else StitchNegative
        
        val timeDisplay = formatDisplayTime(item.tanggal)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    hapticManager.lightTick()
                    onClick() 
                }
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "transaction_item_${item.idTransaksi}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .padding(horizontal = KassakuSpacing.cardInnerLarge, vertical = KassakuSpacing.cardInner),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.kategori ?: "Lainnya",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if(isDark) Color.White else StitchTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${if (isIncome) "Uang Masuk" else "Uang Keluar"} • $timeDisplay${if (!item.keterangan.isNullOrEmpty()) " • ${item.keterangan}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if(isDark) Color(0xFF94A3B8) else StitchTextSecondary,
                    maxLines = 1
                )
            }
            
            Text(
                text = "${if (isIncome) "+" else "-"}${formatCurrencyFlexible(abs(item.nominal ?: 0.0), currencyCode)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = amountColor
            )
        }
    }
    
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    periode: String?,
    tanggal: String?,
    bulan: Int?,
    tahun: Int?,
    onFilterChange: (String?, String?, Int?, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var tempPeriode by remember { mutableStateOf(periode) }
    var tempTanggal by remember { mutableStateOf(tanggal) }
    var tempBulan by remember { mutableStateOf(bulan) }
    var tempTahun by remember { mutableStateOf(tahun) }
    var showDatePicker by remember { mutableStateOf(false) }

    val monthsGrid = listOf(
        1 to "Jan", 2 to "Feb", 3 to "Mar", 4 to "Apr",
        5 to "Mei", 6 to "Jun", 7 to "Jul", 8 to "Agu",
        9 to "Sep", 10 to "Okt", 11 to "Nov", 12 to "Des"
    )
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear downTo 2020).toList()

    val isDark = com.example.kassaku.ui.theme.LocalIsDark.current
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }

    PremiumAlertDialog(
        onDismissRequest = onDismiss,
        isDark = isDark,
        title = "Filter Catatan Keuangan",
        confirmText = "Terapkan",
        onConfirm = {
            hapticManager.lightTick()
            onFilterChange(tempPeriode, tempTanggal, tempBulan, tempTahun)
            onDismiss()
        },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Periode Cepat
                val periodOptions = listOf(
                    null to "Semua", 
                    "hari_ini" to "Hari Ini", 
                    "minggu_ini" to "Minggu Ini", 
                    "bulan_ini" to "Bulan Ini"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Periode Cepat", style = MaterialTheme.typography.labelMedium, color = textSecondary, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periodOptions.take(2).forEach { (valKey, label) ->
                            val isSelected = tempPeriode == valKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) StitchPrimary else (if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)))
                                    .clickable {
                                        hapticManager.lightTick()
                                        tempPeriode = valKey
                                        if (valKey != null) {
                                            tempTanggal = null
                                            tempBulan = null
                                            tempTahun = null
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else (if (isDark) Color(0xFF94A3B8) else StitchTextSecondary),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periodOptions.drop(2).forEach { (valKey, label) ->
                            val isSelected = tempPeriode == valKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) StitchPrimary else (if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)))
                                    .clickable {
                                        hapticManager.lightTick()
                                        tempPeriode = valKey
                                        if (valKey != null) {
                                            tempTanggal = null
                                            tempBulan = null
                                            tempTahun = null
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else (if (isDark) Color(0xFF94A3B8) else StitchTextSecondary),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                // Spesifik Tanggal
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tanggal Spesifik", style = MaterialTheme.typography.labelMedium, color = textSecondary, fontWeight = FontWeight.Bold)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                            .clickable { 
                                hapticManager.lightTick()
                                showDatePicker = true 
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tempTanggal ?: "Pilih Tanggal Spesifik",
                                color = if (tempTanggal != null) (if (isDark) Color.White else StitchTextPrimary) else textSecondary,
                                fontWeight = if (tempTanggal != null) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            if (tempTanggal != null) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = textSecondary,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            hapticManager.lightTick()
                                            tempTanggal = null
                                        }
                                )
                            } else {
                                Icon(Icons.Rounded.CalendarMonth, null, tint = textSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                // Bulan Bento Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bulan", style = MaterialTheme.typography.labelMedium, color = textSecondary, fontWeight = FontWeight.Bold)
                    
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 4) {
                                val index = row * 4 + col
                                val item = monthsGrid[index]
                                val isSelected = tempBulan == item.first
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) StitchPrimary else (if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)))
                                        .clickable {
                                            hapticManager.lightTick()
                                            tempBulan = if (isSelected) null else item.first
                                            tempPeriode = null
                                            tempTanggal = null
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.second,
                                        color = if (isSelected) Color.White else (if (isDark) Color(0xFF94A3B8) else StitchTextSecondary),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        if (row < 2) Spacer(Modifier.height(4.dp))
                    }
                }

                HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                // Tahun Horizontal List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tahun", style = MaterialTheme.typography.labelMedium, color = textSecondary, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "Semua Tahun" chip
                        val isSemuaTahun = tempTahun == null
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSemuaTahun) StitchPrimary else (if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)))
                                .clickable {
                                    hapticManager.lightTick()
                                    tempTahun = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Semua",
                                color = if (isSemuaTahun) Color.White else (if (isDark) Color(0xFF94A3B8) else StitchTextSecondary),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        
                        years.take(3).forEach { y ->
                            val isSelected = tempTahun == y
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) StitchPrimary else (if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)))
                                    .clickable {
                                        hapticManager.lightTick()
                                        tempTahun = y
                                        tempPeriode = null
                                        tempTanggal = null
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = y.toString(),
                                    color = if (isSelected) Color.White else (if (isDark) Color(0xFF94A3B8) else StitchTextSecondary),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { 
                TextButton(onClick = { 
                    hapticManager.lightTick()
                    showDatePicker = false 
                    datePickerState.selectedDateMillis?.let {
                        tempTanggal = formatMillisToString(it)
                        tempPeriode = null
                        tempBulan = null
                        tempTahun = null
                    }
                }) { 
                    Text("OK", fontWeight = FontWeight.Bold, color = StitchPrimary) 
                } 
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { 
                    Text("Batal", color = StitchAccentRed) 
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ExportPdfDialog(
    onDismiss: () -> Unit,
    onExport: () -> Unit
) {
    PremiumAlertDialog(
        onDismissRequest = onDismiss,
        isDark = com.example.kassaku.ui.theme.LocalIsDark.current,
        title = "Export Laporan PDF",
        text = "Laporan PDF akan diexport menggunakan filter riwayat yang sedang aktif.",
        icon = Icons.Rounded.PictureAsPdf,
        confirmText = "Export",
        onConfirm = onExport
    )
}

@Preview(showBackground = true)
@Composable
fun RiwayatPreview() {
    // Mock preview container
}
