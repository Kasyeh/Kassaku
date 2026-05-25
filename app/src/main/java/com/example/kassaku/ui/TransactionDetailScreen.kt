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
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.viewmodel.RiwayatUiState
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
    val isDark = LocalIsDark.current
    
    // Find the item in the current state
    val item = remember(riwayatUiState) {
        (riwayatUiState as? RiwayatUiState.Success)?.riwayatItems?.find { it.idTransaksi == transactionId }
    }

    val backgroundColor = if (isDark) StitchBackgroundDark else StitchBackgroundLight
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary

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
                    val icon = if (isIncome) Icons.Rounded.Add else Icons.Rounded.Remove
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
                    val numberFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                        maximumFractionDigits = 0
                    }
                    Text(
                        text = numberFormatter.format(abs(item.nominal ?: 0.0)),
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
