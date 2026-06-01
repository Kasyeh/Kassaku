package com.example.kassaku.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.data.remote.model.RiwayatItem
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*
import com.example.kassaku.ui.components.PremiumAlertDialog
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.viewmodel.RiwayatUiState
import com.example.kassaku.viewmodel.TransactionActionResult
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    homeViewModel: HomeViewModel,
    navController: androidx.navigation.NavController,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope
) {
    val riwayatUiState by homeViewModel.riwayatUiState.collectAsState()
    val balanceData by homeViewModel.balanceData.collectAsState()
    val isDark = LocalIsDark.current
    
    // Find the item in the current state
    val item = remember(riwayatUiState) {
        (riwayatUiState as? RiwayatUiState.Success)?.riwayatItems?.find { it.idTransaksi == transactionId }
    }

    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val deleteResult by homeViewModel.deleteTransactionResult.collectAsState()
    val updateResult by homeViewModel.updateTransactionResult.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(deleteResult) {
        when (deleteResult) {
            is TransactionActionResult.Success -> {
                android.widget.Toast.makeText(context, (deleteResult as TransactionActionResult.Success).message, android.widget.Toast.LENGTH_SHORT).show()
                homeViewModel.resetDeleteTransactionResult()
                navController.popBackStack()
            }
            is TransactionActionResult.Error -> {
                android.widget.Toast.makeText(context, (deleteResult as TransactionActionResult.Error).message, android.widget.Toast.LENGTH_SHORT).show()
                homeViewModel.resetDeleteTransactionResult()
            }
            else -> {}
        }
    }

    LaunchedEffect(updateResult) {
        when (updateResult) {
            is TransactionActionResult.Success -> {
                android.widget.Toast.makeText(context, (updateResult as TransactionActionResult.Success).message, android.widget.Toast.LENGTH_SHORT).show()
                homeViewModel.resetUpdateTransactionResult()
                navController.popBackStack()
            }
            is TransactionActionResult.Error -> {
                android.widget.Toast.makeText(context, (updateResult as TransactionActionResult.Error).message, android.widget.Toast.LENGTH_SHORT).show()
                homeViewModel.resetUpdateTransactionResult()
            }
            else -> {}
        }
    }

    // Dialog state bindings
    var editNominalStr by remember(item) { mutableStateOf(item?.nominal?.toLong()?.let { abs(it).toString() } ?: "0") }
    var editKategori by remember(item) { mutableStateOf(item?.kategori ?: "") }
    var editKeterangan by remember(item) { mutableStateOf(item?.keterangan ?: "") }
    var editTanggal by remember(item) { mutableStateOf(item?.tanggal?.substring(0, 10) ?: "") }

    if (showDeleteDialog && item != null) {
        PremiumAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            isDark = isDark,
            title = "Hapus Catatan?",
            confirmText = "Hapus",
            confirmEnabled = deleteResult !is TransactionActionResult.Loading,
            isConfirmLoading = deleteResult is TransactionActionResult.Loading,
            onConfirm = {
                val userId = balanceData?.idUser?.toInt() ?: 0
                homeViewModel.deleteTransaction(userId, item.idTransaksi ?: 0L)
            },
            content = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus catatan transaksi ini? Tindakan ini akan mengoreksi nominal saldo Anda secara otomatis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
                )
            }
        )
    }

    if (showEditDialog && item != null) {
        PremiumAlertDialog(
            onDismissRequest = { showEditDialog = false },
            isDark = isDark,
            title = "Ubah Catatan Transaksi",
            confirmText = "Simpan",
            confirmEnabled = editNominalStr.isNotBlank() && editKategori.isNotBlank() && editTanggal.isNotBlank() && updateResult !is TransactionActionResult.Loading,
            isConfirmLoading = updateResult is TransactionActionResult.Loading,
            onConfirm = {
                val userId = balanceData?.idUser?.toInt() ?: 0
                val nominalLong = editNominalStr.toLongOrNull() ?: 0L
                homeViewModel.updateTransaction(
                    userId = userId,
                    idTransaksi = item.idTransaksi ?: 0L,
                    nominal = nominalLong,
                    kategori = editKategori,
                    keterangan = editKeterangan,
                    tanggal = editTanggal
                )
            },
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editNominalStr,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                editNominalStr = newValue
                            }
                        },
                        label = { Text("Nominal (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )

                    OutlinedTextField(
                        value = editKategori,
                        onValueChange = { editKategori = it },
                        label = { Text("Kategori") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editKeterangan,
                        onValueChange = { editKeterangan = it },
                        label = { Text("Keterangan (opsional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editTanggal,
                        onValueChange = { editTanggal = it },
                        label = { Text("Tanggal (YYYY-MM-DD)") },
                        placeholder = { Text("Contoh: 2026-05-31") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Transaksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textPrimary
                )
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        if (item == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            with(sharedTransitionScope) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(KassakuSpacing.screenHorizontal)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "transaction_item_${item.idTransaksi}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isIncome = item.tipe?.equals("pemasukan", ignoreCase = true) == true
                    val amountColor = if (isIncome) StitchPrimary else StitchNegative
                    val icon = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
                    val iconBgColor = if (isIncome) StitchPrimaryLight else Color(0x33EF4444)
                    val iconTint = if (isIncome) StitchPrimary else StitchNegative

                    // Large Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(iconBgColor, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Amount
                    Text(
                        text = com.example.kassaku.ui.components.formatCurrencyFlexible(
                            amount = abs(item.nominal ?: 0.0),
                            currencyCode = balanceData?.currency ?: "IDR",
                            formatMode = balanceData?.currencyFormat ?: "standard"
                        ),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = amountColor
                    )

                    Text(
                        text = if (isIncome) "Uang Masuk" else "Uang Keluar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StitchTextSecondary
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(KassakuSpacing.cardInnerLarge),
                            verticalArrangement = Arrangement.spacedBy(KassakuSpacing.cardGap)
                        ) {
                            DetailItem(
                                icon = Icons.Default.Category,
                                label = "Kategori",
                                value = item.kategori ?: "Lainnya",
                                isDark = isDark
                            )
                            DetailItem(
                                icon = Icons.Default.CalendarToday,
                                label = "Tanggal & Waktu",
                                value = item.tanggal ?: "-",
                                isDark = isDark
                            )
                            DetailItem(
                                icon = Icons.Default.Notes,
                                label = "Catatan",
                                value = item.keterangan ?: "-",
                                isDark = isDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Premium Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Ubah (Edit) Button
                        Button(
                            onClick = { showEditDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StitchPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ubah", fontWeight = FontWeight.Bold)
                        }

                        // Hapus (Delete) Button
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StitchNegative,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hapus", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String, isDark: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = if (isDark) Color.White else StitchTextPrimary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = StitchTextSecondary)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else StitchTextPrimary)
        }
    }
}
