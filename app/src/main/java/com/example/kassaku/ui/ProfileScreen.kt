package com.example.kassaku.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kassaku.ui.components.LogoutConfirmationDialog
import com.example.kassaku.ui.components.formatCurrencyFlexible
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.ui.components.skeleton.SkeletonChartView
import com.example.kassaku.ui.components.skeleton.SkeletonStatSummary
import com.example.kassaku.utils.ThemeMode
import java.util.Locale

@Composable
fun ProfileScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val backgroundColor = if (isDark) StitchBackgroundDark else StitchBackgroundLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary

    val balanceData by homeViewModel.balanceData.collectAsStateWithLifecycle()
    val statistikData by homeViewModel.statistikData.collectAsStateWithLifecycle()
    val resetSaldoResult by homeViewModel.resetSaldoResult.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val username = balanceData?.username ?: "User"
    val initial = if (username.isNotEmpty()) username.take(1).uppercase() else "?"

    LaunchedEffect(key1 = userId) {
        if (userId != 0) {
            homeViewModel.loadBalanceData(userId)
            homeViewModel.fetchStatistik(userId)
        }
    }

    val primaryColor = StitchPrimary

    LaunchedEffect(resetSaldoResult) {
        when (val result = resetSaldoResult) {
            is com.example.kassaku.viewmodel.ResetSaldoResult.Success -> {
                snackbarHostState.showSnackbar(result.message)
                homeViewModel.resetResetSaldoResult()
            }
            is com.example.kassaku.viewmodel.ResetSaldoResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
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

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Content
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
                        text = "Profil",
                        color = textPrimary,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // User Card Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(32.dp), spotColor = Color(0x1A000000)),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Brush.linearGradient(listOf(StitchPrimary, Color(0xFF60A5FA))))
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(surfaceColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    style = TextStyle(
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        brush = Brush.linearGradient(listOf(StitchPrimary, Color(0xFF60A5FA)))
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = username,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary
                        )
                        
                        Text(
                            text = "Personal Account",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        Divider(color = if(isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(24.dp))

                        // Balance horizontal info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("TOTAL SALDO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = textSecondary, letterSpacing = 1.sp)
                                Text(
                                    text = formatCurrencyFlexible(balanceData?.saldo?.toDoubleOrNull() ?: 0.0),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StitchPrimary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileStatBox(
                                label = "PEMASUKAN",
                                value = balanceData?.pemasukan?.toDoubleOrNull() ?: 0.0,
                                color = Color(0xFF10B981),
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                            ProfileStatBox(
                                label = "PENGELUARAN",
                                value = balanceData?.pengeluaran?.toDoubleOrNull() ?: 0.0,
                                color = StitchAccentRed,
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(isDark) Color(0xFFF59E0B).copy(alpha = 0.1f) else Color(0xFFFFF7ED),
                                contentColor = Color(0xFFD97706)
                            )
                        ) {
                            Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Reset Saldo & Riwayat", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(isDark) Color(0xFFEF4444).copy(alpha = 0.1f) else Color(0xFFFEF2F2),
                                contentColor = Color(0xFFEF4444)
                            )
                        ) {
                            Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Keluar dari Akun", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Appearance / Theme Section
            item {
                Text(
                    text = "Penampilan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            item {
                val themeMode by homeViewModel.themeMode.collectAsStateWithLifecycle()
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = if(!isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)) else null
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ThemeOptionRow(
                            label = "Gunakan Pengaturan Sistem",
                            isSelected = themeMode == ThemeMode.SYSTEM,
                            onClick = { homeViewModel.setThemeMode(ThemeMode.SYSTEM) },
                            isDark = isDark
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = if(isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                        ThemeOptionRow(
                            label = "Mode Terang",
                            isSelected = themeMode == ThemeMode.LIGHT,
                            onClick = { homeViewModel.setThemeMode(ThemeMode.LIGHT) },
                            isDark = isDark
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = if(isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                        ThemeOptionRow(
                            label = "Mode Gelap",
                            isSelected = themeMode == ThemeMode.DARK,
                            onClick = { homeViewModel.setThemeMode(ThemeMode.DARK) },
                            isDark = isDark
                        )
                    }
                }
            }

            // Analytics Section
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Analytics, null, tint = StitchPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Analisis 6 Bulan Terakhir",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }

            item {
                statistikData?.let { data ->
                    SmoothLineChart(
                        labels = data.labels,
                        pemasukan = data.pemasukan,
                        pengeluaran = data.pengeluaran,
                        isDark = isDark
                    )
                } ?: SkeletonChartView(
                    isDarkTheme = isDark,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Monthly Table Section
            item {
                Text(
                    text = "Ringkasan Bulanan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = if(!isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)) else null
                ) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BULAN", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = textSecondary)
                            Text("MASUK", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = textSecondary, textAlign = TextAlign.End)
                            Text("KELUAR", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = textSecondary, textAlign = TextAlign.End)
                        }

                        statistikData?.let { data ->
                            data.labels.forEachIndexed { index, label ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text(
                                        formatCurrencyFlexible(data.pemasukan.getOrElse(index) { 0.0 }),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        formatCurrencyFlexible(data.pengeluaran.getOrElse(index) { 0.0 }),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StitchAccentRed,
                                        textAlign = TextAlign.End
                                    )
                                }
                                if (index < data.labels.size - 1) {
                                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = if(isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                                }
                            }
                        } ?: SkeletonStatSummary(
                            rowCount = 6,
                            isDarkTheme = isDark,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

    if (showResetDialog) {
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Verifikasi Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Password diperlukan untuk meriset saldo menjadi Rp 0 dan menghapus riwayat transaksi bulan ini.")
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
                    Text("Ya, Reset", fontWeight = FontWeight.Bold)
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


@Composable
fun ProfileStatBox(label: String, value: Double, color: Color, isDark: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = if(isDark) Color(0xFF94A3B8) else StitchTextSecondary, letterSpacing = 1.sp)
        Text(
            text = formatCurrencyFlexible(value),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}


@Composable
fun ThemeOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color.White else StitchTextPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = StitchPrimary,
                unselectedColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
            )
        )
    }
}
