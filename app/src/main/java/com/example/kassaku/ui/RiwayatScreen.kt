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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import com.example.kassaku.ui.components.skeleton.SkeletonTransactionList
import com.example.kassaku.ui.components.formatCurrencyFlexible
import com.example.kassaku.ui.components.EmptyStateLottie

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

    // Colors based on theme/design
    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val primaryColor = StitchPrimary
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
    val resetSaldoResult by homeViewModel.resetSaldoResult.collectAsState()
    
    var showExportDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
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
    
    // Handle reset saldo result
    LaunchedEffect(resetSaldoResult) {
        when (val result = resetSaldoResult) {
            is com.example.kassaku.viewmodel.ResetSaldoResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                homeViewModel.resetResetSaldoResult()
            }
            is com.example.kassaku.viewmodel.ResetSaldoResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                homeViewModel.resetResetSaldoResult()
            }
            else -> {}
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
                        text = "Catatan Keuangan",
                        color = textPrimary,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconModifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))

                        IconButton(
                            onClick = { showResetDialog = true },
                            modifier = iconModifier
                        ) {
                            Icon(Icons.Default.Refresh, "Reset", tint = textPrimary, modifier = Modifier.size(20.dp))
                        }
                        
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = iconModifier
                        ) {
                            Icon(Icons.Outlined.Save, "Export", tint = textPrimary, modifier = Modifier.size(20.dp))
                        }
                        
                        IconButton(
                            onClick = { showFilterDialog = true },
                            modifier = iconModifier
                        ) {
                            Icon(Icons.Outlined.FilterList, "Filter", tint = textPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Main Content Area
            Column(
                modifier = modifier.fillMaxSize()
        ) {
            // Segmented Filter Control (Jenis)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .background(surfaceColor, RoundedCornerShape(16.dp))
                        .padding(KassakuSpacing.elementGap - 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterTabButton(
                        text = "Semua",
                        selected = selectedJenis == null,
                        onClick = { selectedJenis = null },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        text = "Uang Masuk",
                        selected = selectedJenis == "pemasukan",
                        onClick = { selectedJenis = "pemasukan" },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        text = "Uang Keluar",
                        selected = selectedJenis == "pengeluaran",
                        onClick = { selectedJenis = "pengeluaran" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = selectedSearch,
                onValueChange = { selectedSearch = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                singleLine = true,
                placeholder = { Text("Cari jenis atau catatan") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Cari")
                }
            )

            // Active Filters Display
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
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = StitchPrimaryLight,
                        shape = RoundedCornerShape(8.dp),
                        onClick = { showFilterDialog = true }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = activeFilters.joinToString(" • "), style = MaterialTheme.typography.labelMedium, color = StitchPrimaryDark)
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        selectedPeriode = null
                                        selectedSearch = ""
                                        selectedTanggal = null
                                        selectedBulan = null
                                        selectedTahun = null
                                    },
                                tint = StitchPrimaryDark
                            )
                        }
                    }
                }
            }

            // Content
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
                                message = "Belum ada catatan",
                                subtitle = "Belum ada data untuk ditampilkan",
                                isDark = isDark
                            )
                        } else {
                            val groupedItems = filteredItems.groupBy { 
                                it.tanggal?.split(" ")?.get(0) ?: "Tidak Diketahui" 
                            }

                            LazyColumn(
                                contentPadding = PaddingValues(
                                    horizontal = KassakuSpacing.screenHorizontal,
                                    vertical = KassakuSpacing.screenVertical
                                ),
                                verticalArrangement = Arrangement.spacedBy(KassakuSpacing.sectionGap)
                            ) {
                                groupedItems.forEach { (date, items) ->
                                    item {
                                        Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)) {
                                            // Date Header
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap),
                                                modifier = Modifier.padding(start = KassakuSpacing.sectionTitleInset, bottom = 4.dp)
                                            ) {
                                                Box(modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFFCBD5E1), CircleShape))
                                                Text(
                                                    text = formatDisplayDate(date),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textSecondary,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }

                                            // Card for this date
                                            Card(
                                                shape = RoundedCornerShape(24.dp),
                                                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                                                modifier = Modifier.border(1.dp, if(isDark) Color(0xFF334155) else Color(0xFFF1F5F9), RoundedCornerShape(24.dp))
                                            ) {
                                                Column {
                                                    items.forEachIndexed { index, item ->
                                                        RiwayatItemRow(
                                                            item = item, 
                                                            isLast = index == items.lastIndex, 
                                                            isDark = isDark,
                                                            currencyCode = homeViewModel.balanceData.value?.currency ?: "IDR",
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
                                text = "Gagal memuat catatan",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { homeViewModel.fetchRiwayatTransaksi(userId) }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
        }
        
        // Filter Dialog implementation
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

        if (showResetDialog) {
            var password by remember { mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Masukkan Kata Sandi", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Masukkan kata sandi kamu untuk menghapus semua catatan bulan ini.")
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible) androidx.compose.material.icons.Icons.Filled.Visibility else androidx.compose.material.icons.Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = null)
                                }
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (password.isNotEmpty()) {
                                showResetDialog = false
                                homeViewModel.resetSaldo(userId, password)
                            }
                        },
                        enabled = password.isNotEmpty(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("Ya, Hapus", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Batal")
                    }
                },
                containerColor = surfaceColor,
                titleContentColor = textPrimary,
                textContentColor = textSecondary,
                shape = RoundedCornerShape(24.dp)
            )
        }

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
}



@Composable
fun FilterTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) StitchPrimary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) Color.White else StitchTextSecondary
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val hapticManager = remember { com.example.kassaku.utils.HapticManager(context) }

    with(sharedTransitionScope) {
        val isIncome = item.tipe?.equals("pemasukan", ignoreCase = true) == true
    val amountColor = if (isIncome) StitchPrimary else StitchNegative
    val icon = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
    val iconBgColor = if (isIncome) StitchPrimaryLight else Color(0x33EF4444)
    val iconTint = if (isIncome) StitchPrimary else StitchNegative
    
    val numberFormatter = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    val formattedAmount = numberFormatter.format(abs(item.nominal ?: 0.0))
    val sign = if (isIncome) "+ " else "- "
    
    val dateDisplay = formatDisplayDate(item.tanggal ?: "")
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
            .padding(KassakuSpacing.cardInner),
        verticalAlignment = Alignment.CenterVertically
    ) {
         Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBgColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(KassakuSpacing.elementGap + 4.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.kategori ?: "Lainnya",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if(isDark) Color.White else StitchTextPrimary
            )
            Text(
                text = "${if (isIncome) "Uang Masuk" else "Uang Keluar"} • $timeDisplay • ${item.keterangan ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if(isDark) Color(0xFF94A3B8) else StitchTextSecondary,
                maxLines = 1
            )
        }
        
        Text(
            text = "${if (isIncome) "+ " else "- "}${formatCurrencyFlexible(abs(item.nominal ?: 0.0), currencyCode)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
    }
    
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
        )
    }
}

@Composable
fun EmptyState(message: String, subMessage: String) {
    val isDark = com.example.kassaku.ui.theme.LocalIsDark.current
    EmptyStateLottie(
        message = message,
        subtitle = subMessage,
        isDark = isDark,
        modifier = Modifier.fillMaxSize()
    )
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

    val months = listOf(
        null to "Semua Bulan",
        1 to "Januari", 2 to "Februari", 3 to "Maret", 4 to "April",
        5 to "Mei", 6 to "Juni", 7 to "Juli", 8 to "Agustus",
        9 to "September", 10 to "Oktober", 11 to "November", 12 to "Desember"
    )
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear downTo 2020).map { it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Catatan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Periode Cepat
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Periode Cepat", style = MaterialTheme.typography.labelMedium, color = StitchTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(null to "Semua", "hari_ini" to "Hari", "minggu_ini" to "Minggu", "bulan_ini" to "Bulan").forEach { (valKey, label) ->
                            FilterChip(
                                selected = tempPeriode == valKey,
                                onClick = { 
                                    tempPeriode = valKey
                                    if (valKey != null) {
                                        tempTanggal = null
                                        tempBulan = null
                                        tempTahun = null
                                    }
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.alpha(0.5f))

                // Spesifik Tanggal
                OutlinedTextField(
                    value = tempTanggal ?: "",
                    onValueChange = {},
                    label = { Text("Tanggal Spesifik") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = { 
                        if (tempTanggal != null) {
                            IconButton(onClick = { tempTanggal = null }) {
                                Icon(Icons.Default.Close, "Clear")
                            }
                        } else {
                            Icon(Icons.Default.CalendarToday, null)
                        }
                    }
                )

                // Bulan & Tahun Row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Bulan Dropdown
                    var showMonthMenu by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = showMonthMenu,
                        onExpandedChange = { showMonthMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = months.find { it.first == tempBulan }?.second ?: "Semua Bulan",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bulan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMonthMenu) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showMonthMenu,
                            onDismissRequest = { showMonthMenu = false }
                        ) {
                            months.forEach { (m, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        tempBulan = m
                                        tempPeriode = null
                                        tempTanggal = null
                                        showMonthMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Tahun Dropdown
                    var showYearMenu by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = showYearMenu,
                        onExpandedChange = { showYearMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = tempTahun?.toString() ?: "Semua Tahun",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tahun") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showYearMenu) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showYearMenu,
                            onDismissRequest = { showYearMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Tahun") },
                                onClick = { 
                                    tempTahun = null
                                    showYearMenu = false 
                                }
                            )
                            years.forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y.toString()) },
                                    onClick = {
                                        tempTahun = y
                                        tempPeriode = null
                                        tempTanggal = null
                                        showYearMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onFilterChange(tempPeriode, tempTanggal, tempBulan, tempTahun)
                onDismiss()
            }) { Text("Terapkan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        tempTanggal = formatMillisToString(it)
                        tempPeriode = null
                        tempBulan = null
                        tempTahun = null
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
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
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.PictureAsPdf, null, tint = StitchPrimary) },
        title = { Text("Export Laporan PDF") },
        text = {
            Text("PDF akan diexport menggunakan filter riwayat yang sedang aktif.")
        },
        confirmButton = { Button(onClick = onExport, colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)) { Text("Export PDF") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

// Helpers
fun formatMillisToString(millis: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(calendar.time)
}

private fun savePdfToDownloads(context: Context, pdfBytes: ByteArray, fileName: String): Uri? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/")
        }

        val resolver = context.contentResolver
        // Use MediaStore.Downloads for better compatibility on Q+
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        
        try {
            val uri = resolver.insert(collection, contentValues)
            
            return if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pdfBytes)
                }
                uri
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    } else {
        // Legacy approach for Android < 10 (Q)
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = java.io.File(downloadsDir, fileName)
            java.io.FileOutputStream(file).use { outputStream ->
                outputStream.write(pdfBytes)
            }
            
            // Return file URI using FileProvider for security
            return androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

private fun openPdfFile(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Tidak ada aplikasi pembaca PDF ditemukan", Toast.LENGTH_SHORT).show()
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun RiwayatPreview() {
    // Mock viewmodel and display
}
