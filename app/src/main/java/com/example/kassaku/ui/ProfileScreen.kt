package com.example.kassaku.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import com.example.kassaku.ui.components.PremiumAlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kassaku.ui.components.LogoutConfirmationDialog
import com.example.kassaku.ui.components.formatCurrencyFlexible
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.viewmodel.EmailUpdateResult
import com.example.kassaku.data.remote.model.StatistikData
import com.example.kassaku.data.remote.model.BalanceData
import com.example.kassaku.utils.ThemeMode
import com.example.kassaku.utils.SecurityPreferences
import com.example.kassaku.utils.SecurityUtils
import com.example.kassaku.utils.HapticManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.example.kassaku.viewmodel.AvatarUpdateResult
import com.example.kassaku.viewmodel.FeedbackResult
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    onNavigateToReminderSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary
    val surfaceColor = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
    val hapticManager = remember { HapticManager(context) }

    val balanceData by homeViewModel.balanceData.collectAsStateWithLifecycle()
    val statistikData by homeViewModel.statistikData.collectAsStateWithLifecycle()
    val avatarUpdateResult by homeViewModel.avatarUpdateResult.collectAsStateWithLifecycle()
    val passwordUpdateResult by homeViewModel.passwordUpdateResult.collectAsStateWithLifecycle()
    val emailUpdateResult by homeViewModel.emailUpdateResult.collectAsStateWithLifecycle()
    val currencyUpdateResult by homeViewModel.currencyUpdateResult.collectAsStateWithLifecycle()

    var showCurrencySheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showEmailEditDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    val feedbackResult by homeViewModel.feedbackResult.collectAsStateWithLifecycle()
    val username = balanceData?.username ?: "User"
    val avatarUrl = balanceData?.avatar
    val initial = if (username.isNotEmpty()) username.take(1).uppercase() else "?"

    LaunchedEffect(key1 = userId) {
        if (userId != 0) {
            homeViewModel.loadBalanceData(userId)
            homeViewModel.fetchStatistik(userId)
        }
    }

    LaunchedEffect(currencyUpdateResult) {
        if (currencyUpdateResult is EmailUpdateResult.Success) {
            kotlinx.coroutines.delay(2000)
            homeViewModel.resetCurrencyUpdateResult()
            showCurrencySheet = false
        }
    }



    LaunchedEffect(avatarUpdateResult) {
        when (val result = avatarUpdateResult) {
            is AvatarUpdateResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                homeViewModel.resetAvatarUpdateResult()
            }
            is AvatarUpdateResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                homeViewModel.resetAvatarUpdateResult()
            }
            else -> {}
        }
    }

    LaunchedEffect(passwordUpdateResult) {
        when (val result = passwordUpdateResult) {
            is EmailUpdateResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                homeViewModel.resetEmailUpdateResult()
                showPasswordDialog = false
            }
            is EmailUpdateResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                homeViewModel.resetEmailUpdateResult()
            }
            else -> {}
        }
    }

    LaunchedEffect(emailUpdateResult) {
        when (val result = emailUpdateResult) {
            is EmailUpdateResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                homeViewModel.resetEmailUpdateResult()
                showEmailEditDialog = false
            }
            is EmailUpdateResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                homeViewModel.resetEmailUpdateResult()
            }
            else -> {}
        }
    }

    LaunchedEffect(feedbackResult) {
        when (val result = feedbackResult) {
            is FeedbackResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                homeViewModel.resetFeedbackResult()
                showFeedbackDialog = false
            }
            is FeedbackResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                homeViewModel.resetFeedbackResult()
            }
            else -> {}
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val part = SecurityUtils.createMultipartFromUri(context, it, "avatar")
            if (part != null) {
                homeViewModel.uploadAvatar(userId, part)
            }
        }
    }

    val securityPrefs = remember { SecurityPreferences(context) }
    var appLockEnabled by remember { mutableStateOf(securityPrefs.isAppLockEnabled()) }

    Box(
        modifier = modifier
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
                        text = "Profil",
                        color = textPrimary,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    bottom = 120.dp,
                    start = KassakuSpacing.screenHorizontal,
                    end = KassakuSpacing.screenHorizontal,
                    top = KassakuSpacing.screenVertical
                ),
                verticalArrangement = Arrangement.spacedBy(KassakuSpacing.sectionGap)
            ) {
                // Identity Header Section
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isAvatarLoading = avatarUpdateResult is AvatarUpdateResult.Loading
                        
                        Box(
                            modifier = Modifier
                                .size(124.dp)
                                .clickable {
                                    hapticManager.lightTick()
                                    imagePickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Gradient Ring
                            Box(
                                modifier = Modifier
                                    .size(118.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(StitchPrimary, Color(0xFF8B5CF6))))
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(surfaceColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!avatarUrl.isNullOrEmpty()) {
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
                                                fontSize = 44.sp,
                                                fontWeight = FontWeight.Black,
                                                brush = Brush.linearGradient(listOf(StitchPrimary, Color(0xFF8B5CF6)))
                                            )
                                        )
                                    }
                                }
                            }

                            // Camera Badge overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(34.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = "Ubah Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (isAvatarLoading) {
                                Box(
                                    modifier = Modifier
                                        .size(112.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = StitchPrimary,
                                        modifier = Modifier.size(36.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = username,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    hapticManager.lightTick()
                                    showEmailEditDialog = true
                                }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = balanceData?.email ?: "Tambah Email Pemulihan",
                                fontSize = 14.sp,
                                color = if (balanceData?.email.isNullOrEmpty()) StitchPrimary else textSecondary,
                                fontWeight = if (balanceData?.email.isNullOrEmpty()) FontWeight.Bold else FontWeight.Medium
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Rounded.Edit, 
                                contentDescription = null, 
                                modifier = Modifier.size(14.dp), 
                                tint = if (balanceData?.email.isNullOrEmpty()) StitchPrimary else textSecondary
                            )
                        }
                    }
                }

                // Bento-Grid Summary Cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card 1: Tabungan Impian
                        val activeDreamsCount = statistikData?.dreamForecast?.size ?: 0
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(115.dp)
                                .clickable { hapticManager.lightTick() },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFE0F2FE).copy(alpha = if (isDark) 0.15f else 0.85f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "$activeDreamsCount",
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textPrimary
                                        )
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = "Tabungan Impian",
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Target Impian Aktif",
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textSecondary
                                        )
                                    )
                                }
                            }
                        }

                        // Card 2: Batas Belanja
                        val monthlyExpense = balanceData?.pengeluaran?.toDoubleOrNull() ?: 0.0
                        val limitExpense = balanceData?.targetPengeluaran?.toDoubleOrNull() ?: 0.0
                        val isOverlimit = limitExpense > 0 && monthlyExpense > limitExpense
                        val spendPercent = if (limitExpense > 0) (monthlyExpense / limitExpense) else 0.0
                        val budgetStatusColor = when {
                            isOverlimit -> StitchNegative
                            spendPercent >= 0.8 -> Color(0xFFF59E0B)
                            else -> StitchPrimary
                        }
                        val budgetStatusText = when {
                            limitExpense <= 0 -> "Belum Diatur"
                            isOverlimit -> "Over Budget!"
                            spendPercent >= 0.8 -> "Waspada!"
                            else -> "Sangat Aman"
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(115.dp)
                                .clickable { hapticManager.lightTick() },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(budgetStatusColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = budgetStatusColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    Text(
                                        text = budgetStatusText,
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = budgetStatusColor
                                        ),
                                        maxLines = 1
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = "Batas Belanja",
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (limitExpense > 0) {
                                            "${(spendPercent * 100).toInt()}% terpakai"
                                        } else {
                                            "Target belanja"
                                        },
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Settings Group: Akun & Keamanan
                item {
                    SettingGroupTitle(title = "Akun & Keamanan", textPrimary = textPrimary)
                    
                    SettingGroupCard(isDark = isDark, surfaceColor = surfaceColor) {
                        SettingItemRow(
                            icon = Icons.Rounded.Email,
                            iconColor = StitchPrimary,
                            title = "Email Pemulihan",
                            subtitle = balanceData?.email ?: "Belum diatur",
                            onClick = { showEmailEditDialog = true },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                        SettingItemRow(
                            icon = Icons.Rounded.LockReset,
                            iconColor = Color(0xFFF59E0B),
                            title = "Ganti Password",
                            subtitle = "Perbarui kata sandi akun Anda",
                            onClick = { showPasswordDialog = true },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                        SettingItemRowWithSwitch(
                            icon = Icons.Rounded.Fingerprint,
                            iconColor = Color(0xFF8B5CF6),
                            title = "Kunci Aplikasi",
                            subtitle = "Gunakan Biometrik / PIN perangkat",
                            isChecked = appLockEnabled,
                            onCheckedChange = { isChecked ->
                                hapticManager.lightTick()
                                if (isChecked) {
                                    val activity = SecurityUtils.getActivity(context)
                                    if (activity != null) {
                                        if (SecurityUtils.isBiometricAvailable(context)) {
                                            SecurityUtils.showBiometricPrompt(
                                                activity = activity,
                                                onSuccess = {
                                                    appLockEnabled = true
                                                    securityPrefs.setAppLockEnabled(true)
                                                },
                                                onError = { 
                                                    Toast.makeText(context, "Gagal mengaktifkan: $it", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            Toast.makeText(context, "PIN atau Kunci Layar belum diatur di perangkat Anda.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    appLockEnabled = false
                                    securityPrefs.setAppLockEnabled(false)
                                }
                            },
                            isDark = isDark
                        )
                    }
                }

                // Settings Group: Preferensi
                item {
                    SettingGroupTitle(title = "Preferensi & Tampilan", textPrimary = textPrimary)
                    
                    val themeMode by homeViewModel.themeMode.collectAsStateWithLifecycle()
                    val dynamicColor by homeViewModel.isDynamicColor.collectAsStateWithLifecycle()
                    
                    SettingGroupCard(isDark = isDark, surfaceColor = surfaceColor) {
                        SettingItemRow(
                            icon = Icons.Rounded.NotificationsActive,
                            iconColor = StitchPrimary,
                            title = "Pengingat Finansial",
                            subtitle = "Atur notifikasi harian & budget",
                            onClick = onNavigateToReminderSettings,
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                        SettingItemRowRadio(
                            icon = Icons.Rounded.SettingsBrightness,
                            iconColor = textSecondary,
                            title = "Sistem Default",
                            isSelected = themeMode == ThemeMode.SYSTEM,
                            onClick = { 
                                hapticManager.lightTick()
                                homeViewModel.setThemeMode(ThemeMode.SYSTEM) 
                            },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                        SettingItemRowRadio(
                            icon = Icons.Rounded.LightMode,
                            iconColor = Color(0xFFF59E0B),
                            title = "Mode Terang",
                            isSelected = themeMode == ThemeMode.LIGHT,
                            onClick = { 
                                hapticManager.lightTick()
                                homeViewModel.setThemeMode(ThemeMode.LIGHT) 
                            },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                        SettingItemRowRadio(
                            icon = Icons.Rounded.DarkMode,
                            iconColor = Color(0xFF8B5CF6),
                            title = "Mode Gelap",
                            isSelected = themeMode == ThemeMode.DARK,
                            onClick = { 
                                hapticManager.lightTick()
                                homeViewModel.setThemeMode(ThemeMode.DARK) 
                            },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                        SettingItemRowWithSwitch(
                            icon = Icons.Rounded.Palette,
                            iconColor = Color(0xFFEC4899),
                            title = "Warna Dinamis",
                            subtitle = "Tema menyesuaikan wallpaper hp",
                            isChecked = dynamicColor,
                            onCheckedChange = { 
                                hapticManager.lightTick()
                                homeViewModel.setDynamicColor(it) 
                            },
                            isDark = isDark
                        )
                    }
                }

                // Feedback & Support
                item {
                    SettingGroupTitle(title = "Bantuan & Masukan", textPrimary = textPrimary)
                    SettingGroupCard(isDark = isDark, surfaceColor = surfaceColor) {
                        SettingItemRow(
                            icon = Icons.Rounded.RateReview,
                            iconColor = Color(0xFF8B5CF6),
                            title = "Kirim Masukan",
                            subtitle = "Bantu kami menjadi lebih baik",
                            onClick = { showFeedbackDialog = true },
                            isDark = isDark
                        )
                    }
                }

                // Destructive Actions
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingGroupCard(isDark = isDark, surfaceColor = surfaceColor) {
                        SettingItemRowDestructive(
                            icon = Icons.AutoMirrored.Rounded.Logout,
                            title = "Keluar dari Akun",
                            onClick = { showLogoutDialog = true },
                            isWarning = false
                        )
                    }
                }
            }
        }

        // --- Dialogs and Sheets --- //

        if (showEmailEditDialog) {
            var inputEmail by remember { mutableStateOf(balanceData?.email ?: "") }
            var confirmPassword by remember { mutableStateOf("") }
            val isEmailValid = inputEmail.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()

            PremiumAlertDialog(
                onDismissRequest = { showEmailEditDialog = false },
                isDark = isDark,
                title = "Atur Email Pemulihan",
                confirmText = "Simpan",
                confirmEnabled = isEmailValid && confirmPassword.isNotBlank() && emailUpdateResult !is EmailUpdateResult.Loading,
                isConfirmLoading = emailUpdateResult is EmailUpdateResult.Loading,
                onConfirm = {
                    hapticManager.lightTick()
                    if (isEmailValid && confirmPassword.isNotBlank()) {
                        homeViewModel.updateEmail(userId, inputEmail, confirmPassword)
                    }
                },
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.cardGap)) {
                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            label = { Text("Email Baru") },
                            isError = !isEmailValid,
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!isEmailValid) {
                            Text("Format email tidak valid", color = StitchAccentRed, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Password Akun Saat Ini") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }

        if (showPasswordDialog) {
            var currentPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            
            PremiumAlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                isDark = isDark,
                title = "Ganti Password",
                confirmText = "Simpan",
                confirmEnabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword && emailUpdateResult !is EmailUpdateResult.Loading,
                isConfirmLoading = emailUpdateResult is EmailUpdateResult.Loading,
                onConfirm = {
                    hapticManager.lightTick()
                    if (currentPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword) {
                        homeViewModel.updatePassword(currentPassword, newPassword, confirmPassword)
                    }
                },
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Password Lama") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Password Baru") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Konfirmasi Password Baru") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }



        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    hapticManager.lightTick()
                    showLogoutDialog = false
                    onLogout()
                },
                onDismissRequest = { showLogoutDialog = false },
                isDark = isDark
            )
        }

        // --- Feedback Dialog --- //
        if (showFeedbackDialog) {
            var feedbackSubjek by remember { mutableStateOf("") }
            var feedbackPesan by remember { mutableStateOf("") }
            var feedbackRating by remember { mutableIntStateOf(0) }
            val isLoading = feedbackResult is FeedbackResult.Loading
            val canSubmit = feedbackSubjek.isNotBlank() && feedbackPesan.isNotBlank() && !isLoading

            PremiumAlertDialog(
                onDismissRequest = { showFeedbackDialog = false },
                isDark = isDark,
                title = "Berikan Feedback",
                confirmText = "Kirim",
                confirmEnabled = canSubmit,
                onConfirm = {
                    hapticManager.lightTick()
                    homeViewModel.sendFeedback(
                        subjek = feedbackSubjek.trim(),
                        pesan = feedbackPesan.trim(),
                        rating = if (feedbackRating > 0) feedbackRating else null
                    )
                },
                content = {
                    Column {
                        Text(
                            text = "Seberapa puas Anda?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            for (i in 1..5) {
                                IconButton(
                                    onClick = { 
                                        hapticManager.lightTick()
                                        feedbackRating = if (feedbackRating == i) 0 else i 
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = if (i <= feedbackRating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                        contentDescription = "Bintang $i",
                                        tint = if (i <= feedbackRating) Color(0xFFF59E0B) else {
                                            if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                        if (feedbackRating > 0) {
                            Text(
                                text = when (feedbackRating) {
                                    1 -> "Sangat Tidak Puas"
                                    2 -> "Kurang Puas"
                                    3 -> "Cukup Baik"
                                    4 -> "Puas"
                                    5 -> "Sangat Puas"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Subjek Masukan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        val subjectOptions = listOf("Saran Fitur", "Laporan Bug", "Pertanyaan", "Lainnya")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                subjectOptions.take(2).forEach { option ->
                                    val isSelected = feedbackSubjek == option
                                    val chipBg = if (isSelected) {
                                        when (option) {
                                            "Saran Fitur" -> StitchPrimary
                                            "Laporan Bug" -> StitchNegative
                                            "Pertanyaan" -> Color(0xFF0EA5E9)
                                            else -> Color(0xFF475569)
                                        }
                                    } else {
                                        if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
                                    }
                                    val contentColor = if (isSelected) Color.White else (if (isDark) Color(0xFF94A3B8) else StitchTextSecondary)
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(chipBg)
                                            .clickable { 
                                                hapticManager.lightTick()
                                                feedbackSubjek = option 
                                            }
                                            .padding(vertical = 12.dp, horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = option,
                                            color = contentColor,
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
                                subjectOptions.drop(2).take(2).forEach { option ->
                                    val isSelected = feedbackSubjek == option
                                    val chipBg = if (isSelected) {
                                        when (option) {
                                            "Saran Fitur" -> StitchPrimary
                                            "Laporan Bug" -> StitchNegative
                                            "Pertanyaan" -> Color(0xFF0EA5E9)
                                            else -> Color(0xFF475569)
                                        }
                                    } else {
                                        if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
                                    }
                                    val contentColor = if (isSelected) Color.White else (if (isDark) Color(0xFF94A3B8) else StitchTextSecondary)
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(chipBg)
                                            .clickable { 
                                                hapticManager.lightTick()
                                                feedbackSubjek = option 
                                            }
                                            .padding(vertical = 12.dp, horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = option,
                                            color = contentColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = feedbackPesan,
                            onValueChange = { feedbackPesan = it },
                            label = { Text("Pesan Masukan") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                cursorColor = Color(0xFF8B5CF6),
                                focusedLabelColor = Color(0xFF8B5CF6)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }
                }
            )
        }
    }
}

// --- Helper Composables --- //

@Composable
fun SettingGroupTitle(title: String, textPrimary: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = textPrimary,
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingGroupCard(
    isDark: Boolean,
    surfaceColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(1.dp, if(isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingItemRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val textPrimary = if (isDark) Color.White else StitchTextPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                hapticManager.lightTick()
                onClick() 
            }
            .padding(horizontal = KassakuSpacing.cardInnerLarge, vertical = KassakuSpacing.cardInner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(KassakuSpacing.elementGap + 4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = textSecondary)
    }
}

@Composable
fun SettingItemRowWithSwitch(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean
) {
    val textPrimary = if (isDark) Color.White else StitchTextPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KassakuSpacing.cardInnerLarge, vertical = KassakuSpacing.cardInner - 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(KassakuSpacing.elementGap + 4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StitchPrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = if(isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SettingItemRowRadio(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val textPrimary = if (isDark) Color.White else StitchTextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = KassakuSpacing.cardInnerLarge, vertical = KassakuSpacing.cardInner - 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(KassakuSpacing.elementGap + 4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = textPrimary,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = StitchPrimary)
        )
    }
}

@Composable
fun SettingItemRowDestructive(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isWarning: Boolean = false
) {
    val color = if (isWarning) Color(0xFFF59E0B) else Color(0xFFEF4444)
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                hapticManager.lightTick()
                onClick() 
            }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.weight(1f)
        )
    }
}
