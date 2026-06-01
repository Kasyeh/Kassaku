package com.example.kassaku.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kassaku.data.remote.model.ImpianItem
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*
import com.example.kassaku.ui.components.EmptyStateLottie
import com.example.kassaku.ui.components.LogoutConfirmationDialog
import com.example.kassaku.ui.components.PremiumAlertDialog
import com.example.kassaku.ui.components.ConfettiEffect
import com.example.kassaku.ui.components.AuroraBackground
import com.example.kassaku.ui.components.form.SetorImpianFormSheet
import com.example.kassaku.ui.components.form.SetorImpianFormState
import com.example.kassaku.ui.components.form.TambahImpianFormSheet
import com.example.kassaku.ui.components.form.TambahImpianFormState
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.viewmodel.ImpianUiState
import com.example.kassaku.viewmodel.TambahImpianResult
import com.example.kassaku.viewmodel.HapusImpianResult
import com.example.kassaku.viewmodel.SetorImpianResult
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.example.kassaku.ui.components.skeleton.SkeletonImpianList
import com.example.kassaku.utils.formatMillisToString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kassaku.ui.components.formatCurrencyFlexible

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpianScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val haptic = LocalHapticFeedback.current

    var selectedImpian by remember { mutableStateOf<ImpianItem?>(null) }
    var showHapusDialog by remember { mutableStateOf(false) }
    var impianToDelete by remember { mutableStateOf<ImpianItem?>(null) }
    var impianToSetor by remember { mutableStateOf<ImpianItem?>(null) }
    var showSetorSheet by remember { mutableStateOf(false) }
    var setorFormState by remember { mutableStateOf<SetorImpianFormState>(SetorImpianFormState.Idle) }
    var showTambahSheet by remember { mutableStateOf(false) }
    var tambahFormState by remember { mutableStateOf<TambahImpianFormState>(TambahImpianFormState.Idle) }
    
    // Preset name for Add Dream sheet when clicking Quick Inspiration Chips
    var presetNamaBarang by remember { mutableStateOf("") }
    
    // Confetti animation state
    var showConfetti by remember { mutableStateOf(false) }

    val balanceData by homeViewModel.balanceData.collectAsStateWithLifecycle()
    val currencyCode = balanceData?.currency ?: "IDR"

    // Theme Colors
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary

    LaunchedEffect(key1 = userId) {
        homeViewModel.fetchImpian(userId)
    }

    // Auto dismiss confetti after delay
    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            kotlinx.coroutines.delay(3000)
            showConfetti = false
        }
    }

    // Handle tambah impian result
    LaunchedEffect(key1 = homeViewModel) {
        launch {
            homeViewModel.tambahImpianResult.collectLatest { result ->
                when (result) {
                    is TambahImpianResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        homeViewModel.resetTambahImpianResult()
                        tambahFormState = TambahImpianFormState.Success
                        showTambahSheet = false
                        presetNamaBarang = ""
                        homeViewModel.fetchImpian(userId)
                    }
                    is TambahImpianResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        homeViewModel.resetTambahImpianResult()
                        tambahFormState = TambahImpianFormState.Error(result.message)
                    }
                    is TambahImpianResult.Loading -> {
                        tambahFormState = TambahImpianFormState.Submitting
                    }
                    else -> {}
                }
            }
        }
    }

    // Handle hapus impian result
    LaunchedEffect(key1 = homeViewModel) {
        launch {
            homeViewModel.hapusImpianResult.collectLatest { result ->
                when (result) {
                    is HapusImpianResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        homeViewModel.resetHapusImpianResult()
                        homeViewModel.fetchImpian(userId)
                    }
                    is HapusImpianResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        homeViewModel.resetHapusImpianResult()
                    }
                    else -> {}
                }
            }
        }
    }

    // Handle setor impian result
    LaunchedEffect(key1 = homeViewModel) {
        launch {
            homeViewModel.setorImpianResult.collectLatest { result ->
                when (result) {
                    is SetorImpianResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        homeViewModel.resetSetorImpianResult()
                        setorFormState = SetorImpianFormState.Success
                        showSetorSheet = false
                        
                        // Trigger premium confetti celebration!
                        showConfetti = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        
                        impianToSetor = null
                        homeViewModel.fetchImpian(userId)
                        homeViewModel.loadBalanceData(userId)
                    }
                    is SetorImpianResult.Error -> {
                        setorFormState = SetorImpianFormState.Error(result.message)
                        homeViewModel.resetSetorImpianResult()
                    }
                    is SetorImpianResult.Loading -> {
                        setorFormState = SetorImpianFormState.Submitting
                    }
                    else -> {}
                }
            }
        }
    }

    val impianUiState by homeViewModel.impianUiState.collectAsState()

    // 1. Wrap the entire layout inside the gorgeous moving AuroraBackground!
    AuroraBackground(
        modifier = Modifier.fillMaxSize(),
        isDark = isDark
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Curved Header/Header Content
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
                            text = "Tabungan Impian",
                            color = textPrimary,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap)) {
                            IconButton(
                                onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    presetNamaBarang = ""
                                    showTambahSheet = true 
                                    tambahFormState = TambahImpianFormState.Idle
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                            ) {
                                Icon(Icons.Default.Add, "Tambah", tint = textPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Content Area
                Box(
                    modifier = modifier.fillMaxSize()
                ) {
                    when (val state = impianUiState) {
                        is ImpianUiState.Loading,
                        is ImpianUiState.Idle -> {
                            SkeletonImpianList(
                                itemCount = 4,
                                isDarkTheme = isDark,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                            )
                        }

                        is ImpianUiState.Success -> {
                            if (state.impianItems.isEmpty()) {
                                // 2. Interactive Premium Empty State featuring animated Piggy Bank Canvas
                                EmptyImpianView(
                                    isDark = isDark,
                                    onChipClick = { category ->
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        presetNamaBarang = when (category) {
                                            "Gadget" -> "Beli Gadget Baru 📱"
                                            "Traveling" -> "Liburan Impian ✈️"
                                            "Kendaraan" -> "Beli Kendaraan Baru 🚗"
                                            "Ibadah" -> "Tabungan Umroh/Ibadah 🕋"
                                            else -> category
                                        }
                                        showTambahSheet = true
                                        tambahFormState = TambahImpianFormState.Idle
                                    },
                                    onCreateClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        presetNamaBarang = ""
                                        showTambahSheet = true
                                        tambahFormState = TambahImpianFormState.Idle
                                    }
                                )
                            } else {
                                // Extract and calculate macro statistics for the Bento Summary Card
                                val totalSaved = state.impianItems.sumOf { it.danaTerkumpul ?: 0.0 }
                                val totalRemaining = state.impianItems.sumOf { it.sisaTarget ?: 0.0 }
                                val activeCount = state.impianItems.count { it.isTercapai != true }
                                val completedCount = state.impianItems.count { it.isTercapai == true }
                                val averageProgress = if (state.impianItems.isNotEmpty()) {
                                    state.impianItems.map { ((it.persentaseProgress ?: 0.0) / 100.0) }.average().toFloat().coerceIn(0f, 1f)
                                } else {
                                    0f
                                }

                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        horizontal = KassakuSpacing.screenHorizontal,
                                        vertical = 8.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(KassakuSpacing.cardGap)
                                ) {
                                    // 3. Elegant Bento Summary Card at the top of the savings list
                                    item {
                                        BentoSummaryCard(
                                            totalSaved = totalSaved,
                                            totalRemaining = totalRemaining,
                                            activeCount = activeCount,
                                            completedCount = completedCount,
                                            averageProgress = averageProgress,
                                            currencyCode = currencyCode,
                                            isDark = isDark,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    items(state.impianItems, key = { it.idImpian!! }) { item ->
                                        // 4. Redesigned Premium Glassmorphic Dream Goals card
                                        ImpianItemRow(
                                            item = item, 
                                            onClick = { selectedImpian = item },
                                            onDeleteClick = { 
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                impianToDelete = item
                                                showHapusDialog = true 
                                            },
                                            onSetorClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                impianToSetor = item
                                                showSetorSheet = true
                                                setorFormState = SetorImpianFormState.Idle
                                            },
                                            isDark = isDark,
                                            surfaceColor = surfaceColor,
                                            currencyCode = currencyCode
                                        )
                                    }
                                    
                                    // Extra spacer at bottom to ensure no bottom-nav overlay hides actions
                                    item {
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                        }

                        is ImpianUiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Gagal memuat tabungan: ${state.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { homeViewModel.fetchImpian(userId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
                                ) {
                                    Text("Coba Lagi", color = StitchTextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Confetti celebration drawer
            if (showConfetti) {
                ConfettiEffect(
                    modifier = Modifier.fillMaxSize(),
                    isActive = true
                )
            }

            selectedImpian?.let { item ->
                com.example.kassaku.ui.components.PremiumDetailBottomSheet(
                    item = item,
                    currencyCode = currencyCode,
                    onDismissRequest = { selectedImpian = null }
                )
            }

            // Sheet tambah impian
            TambahImpianFormSheet(
                isVisible = showTambahSheet,
                formState = tambahFormState,
                initialNamaBarang = presetNamaBarang,
                onDismiss = {
                    showTambahSheet = false
                    tambahFormState = TambahImpianFormState.Idle
                    presetNamaBarang = ""
                },
                onSubmit = { namaBarang, hargaBarang, deadline, keterangan, imageUri ->
                    val fotoBarang = imageUri?.let { uri ->
                        createMultipartFromUri(context, uri, "foto_barang")
                    }
                    homeViewModel.tambahImpian(userId, namaBarang, hargaBarang, deadline, keterangan, fotoBarang)
                },
                currencyCode = currencyCode
            )

            // Dialog hapus impian
            if (showHapusDialog) {
                HapusImpianDialog(
                    onDismissRequest = { 
                        showHapusDialog = false 
                        impianToDelete = null
                    },
                    onConfirm = { password ->
                        showHapusDialog = false
                        impianToDelete?.idImpian?.let { id ->
                            homeViewModel.hapusImpian(id, userId, password)
                        }
                        impianToDelete = null
                    }
                )
            }

            SetorImpianFormSheet(
                isVisible = showSetorSheet,
                item = impianToSetor,
                formState = setorFormState,
                onDismiss = {
                    showSetorSheet = false
                    impianToSetor = null
                    setorFormState = SetorImpianFormState.Idle
                },
                onSubmit = { nominal, keterangan ->
                    val target = impianToSetor
                    if (target != null) {
                        setorFormState = SetorImpianFormState.Submitting
                        homeViewModel.setorImpian(target.idImpian, userId, nominal, keterangan)
                    }
                }
            )
        }
    }
}

@Composable
fun BentoSummaryCard(
    totalSaved: Double,
    totalRemaining: Double,
    activeCount: Int,
    completedCount: Int,
    averageProgress: Float,
    currencyCode: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0).copy(alpha = 0.8f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Title & Active Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Tabungan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
                )
                Surface(
                    color = StitchPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$activeCount Aktif • $completedCount Tercapai 🏆",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) StitchPrimary else Color(0xFF0F766E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Large Balance Amount
            Text(
                text = formatCurrencyFlexible(totalSaved, currencyCode, "standard"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else StitchTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Thin Divider line
            HorizontalDivider(
                color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bento 2-column details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Column 1: Sisa Target
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sisa Target",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrencyFlexible(totalRemaining, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else StitchTextPrimary
                    )
                }

                // Column 2: Rata-rata Progress
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rata-rata Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { averageProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = StitchPrimary,
                            trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "${"%.1f".format(averageProgress * 100)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else StitchTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImpianItemRow(
    item: ImpianItem,
    onClick: (ImpianItem) -> Unit,
    onDeleteClick: (ImpianItem) -> Unit,
    onSetorClick: (ImpianItem) -> Unit,
    isDark: Boolean,
    surfaceColor: Color,
    currencyCode: String
) {
    val progressValue = ((item.persentaseProgress ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)
    val danaTerkumpul = item.danaTerkumpul ?: 0L
    val sisaTarget = item.sisaTarget ?: 0L
    val isTargetTercapai = item.isTercapai == true
    val remainingDaysText = calculateDaysRemaining(item.deadline)

    // Soft glassmorphic surface
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0).copy(alpha = 0.7f),
                RoundedCornerShape(24.dp)
            )
            .clickable { onClick(item) }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Unobtrusive Glass delete button at Top-Right
            IconButton(
                onClick = { onDeleteClick(item) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Larger beautifully framed image
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                            .border(1.dp, if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = "http://10.0.2.2:8000/storage/${item.fotoBarang}",
                            contentDescription = item.namaBarang,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                            error = painterResource(id = android.R.drawable.ic_menu_report_image)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.namaBarang ?: "Tanpa Nama",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else StitchTextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Remaining Days Countdown Badge
                        Surface(
                            color = if (isTargetTercapai) Color(0xFF10B981).copy(alpha = 0.1f)
                                    else if (remainingDaysText.contains("Melewati")) Color(0xFFEF4444).copy(alpha = 0.1f)
                                    else if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (isTargetTercapai) Color(0xFF10B981)
                                           else if (remainingDaysText.contains("Melewati")) Color(0xFFEF4444)
                                           else if (isDark) Color(0xFF94A3B8) else StitchTextSecondary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = if (isTargetTercapai) "Tercapai!" else remainingDaysText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTargetTercapai) Color(0xFF10B981)
                                           else if (remainingDaysText.contains("Melewati")) Color(0xFFEF4444)
                                           else if (isDark) Color.White else StitchTextPrimary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = formatCurrencyFlexible(item.hargaBarang?.toDouble() ?: 0.0, currencyCode, "standard"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = StitchPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Bar Section
                Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap)) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressValue,
                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        label = "progressAnimation"
                    )

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = if (isTargetTercapai) Color(0xFF10B981) else StitchPrimary,
                        trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Sudah Terkumpul",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
                            )
                            Text(
                                text = formatCurrencyFlexible(danaTerkumpul.toDouble(), currencyCode),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else StitchTextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Masih Kurang",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
                            )
                            Text(
                                text = formatCurrencyFlexible(sisaTarget.toDouble(), currencyCode),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isTargetTercapai) Color(0xFF10B981) else StitchNegative
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                if (isTargetTercapai) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Target Tercapai! 🏆",
                            modifier = Modifier.padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Button(
                        onClick = { onSetorClick(item) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = StitchTextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tambah Tabungan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = StitchTextPrimary
                        )
                    }
                }
            }
        }
    }
}

// 5. Redesigned Premium Empty State View with custom wiggling piggy bank and inspiration chips
@Composable
fun EmptyImpianView(
    isDark: Boolean,
    onChipClick: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glowing background for the piggy bank
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Outer glowing circle
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    StitchPrimary.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Draw custom Canvas Animated Piggy Bank
                AnimatedPiggyBank(
                    modifier = Modifier.size(160.dp),
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Belum Ada Impian",
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else StitchTextPrimary,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Mulai buat target menabungmu sekarang!",
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Inspiration Chips Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF334155).copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Butuh ide menabung? Pilih inspirasi:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Inspiration Chips Row Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        InspirationChip("Gadget", "📱", onClick = { onChipClick("Gadget") }, isDark = isDark)
                        InspirationChip("Travel", "✈️", onClick = { onChipClick("Traveling") }, isDark = isDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        InspirationChip("Motor", "🚗", onClick = { onChipClick("Kendaraan") }, isDark = isDark)
                        InspirationChip("Umroh", "🕋", onClick = { onChipClick("Ibadah") }, isDark = isDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Glassmorphic Hero Call-to-Action Card
            Button(
                onClick = onCreateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = StitchTextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buat Impian Pertama",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
            }
        }
    }
}

@Composable
fun InspirationChip(
    text: String,
    emoji: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Color.White,
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF475569).copy(alpha = 0.5f) else Color(0xFFCBD5E1)
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else StitchTextPrimary
            )
        }
    }
}

// 6. Custom Canvas animation representing a piggy bank bouncing with falling gold coins
@Composable
fun AnimatedPiggyBank(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PiggyBankTransition")
    
    // Coin falling animation progress
    val coinProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CoinProgress"
    )

    // Bounce and scale when the coin enters the slot (progress between 0.65f and 0.85f)
    val isHitting = coinProgress in 0.65f..0.85f
    val scale by animateFloatAsState(
        targetValue = if (isHitting) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "PiggyScale"
    )

    val rotateDegrees by animateFloatAsState(
        targetValue = if (isHitting) -4f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "PiggyRotation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotateDegrees
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2
            val centerY = h / 2 + 10.dp.toPx()

            // 1. Draw Gold Coin falling from top
            val slotY = centerY - 44.dp.toPx()
            val startY = 10.dp.toPx()
            
            if (coinProgress < 0.75f) {
                val currentCoinY = startY + (slotY - startY) * (coinProgress / 0.75f)
                val coinAlpha = if (coinProgress > 0.62f) {
                    ((0.75f - coinProgress) / 0.13f).coerceIn(0f, 1f)
                } else {
                    1f
                }
                
                // Gold outer circle
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = 8.dp.toPx(),
                    center = Offset(centerX, currentCoinY),
                    alpha = coinAlpha
                )
                // Gold inner highlight
                drawCircle(
                    color = Color(0xFFFBBF24),
                    radius = 5.dp.toPx(),
                    center = Offset(centerX, currentCoinY),
                    alpha = coinAlpha
                )
            }

            // 2. Safe Vault metallic body
            val vaultWidth = 88.dp.toPx()
            val vaultHeight = 88.dp.toPx()
            val vaultColor = if (isDark) Color(0xFF334155) else Color(0xFF64748B)
            val vaultShadowColor = if (isDark) Color(0xFF1E293B) else Color(0xFF475569)
            val vaultBorderColor = if (isDark) Color(0xFF06B6D4) else Color(0xFF0891B2) // Glowing cyan accent

            // Base shadow layer for depth
            drawRoundRect(
                color = vaultShadowColor,
                topLeft = Offset(centerX - vaultWidth / 2 + 4.dp.toPx(), centerY - vaultHeight / 2 + 4.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(vaultWidth, vaultHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
            )

            // Vault main body
            drawRoundRect(
                color = vaultColor,
                topLeft = Offset(centerX - vaultWidth / 2, centerY - vaultHeight / 2),
                size = androidx.compose.ui.geometry.Size(vaultWidth, vaultHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
            )

            // Premium Cyan Inner Border
            drawRoundRect(
                color = vaultBorderColor,
                topLeft = Offset(centerX - vaultWidth / 2, centerY - vaultHeight / 2),
                size = androidx.compose.ui.geometry.Size(vaultWidth, vaultHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            // 3. Horizontal Coin Slot on top of safe
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(centerX - 16.dp.toPx(), centerY - vaultHeight / 2 + 8.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(32.dp.toPx(), 6.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
            )

            // 4. Glowing Indicator LED (Emerald green representing secure/unlocked)
            drawCircle(
                color = Color(0xFF10B981),
                radius = 3.5.dp.toPx(),
                center = Offset(centerX + vaultWidth / 2 - 12.dp.toPx(), centerY - vaultHeight / 2 + 14.dp.toPx())
            )
            // LED Glow effect
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.3f),
                radius = 7.dp.toPx(),
                center = Offset(centerX + vaultWidth / 2 - 12.dp.toPx(), centerY - vaultHeight / 2 + 14.dp.toPx())
            )

            // 5. Dial Lock Wheel in the center
            val dialCenterX = centerX + 12.dp.toPx()
            val dialCenterY = centerY + 8.dp.toPx()
            val dialRadius = 18.dp.toPx()
            val silverColor = Color(0xFFCBD5E1)
            val silverDark = Color(0xFF94A3B8)

            // Dial wheel outer rim
            drawCircle(
                color = silverDark,
                radius = dialRadius,
                center = Offset(dialCenterX, dialCenterY),
                style = Stroke(width = 4.dp.toPx())
            )

            // Dial wheel spokes (4-directional lock wheel)
            drawLine(
                color = silverColor,
                start = Offset(dialCenterX - dialRadius, dialCenterY),
                end = Offset(dialCenterX + dialRadius, dialCenterY),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = silverColor,
                start = Offset(dialCenterX, dialCenterY - dialRadius),
                end = Offset(dialCenterX, dialCenterY + dialRadius),
                strokeWidth = 3.dp.toPx()
            )

            // Center spindle cap
            drawCircle(
                color = silverColor,
                radius = 5.dp.toPx(),
                center = Offset(dialCenterX, dialCenterY)
            )

            // 6. Security PIN Keypad on the left
            val keypadLeft = centerX - vaultWidth / 2 + 12.dp.toPx()
            val keypadTop = centerY - 6.dp.toPx()
            val keypadWidth = 18.dp.toPx()
            val keypadHeight = 28.dp.toPx()

            // Keypad body
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(keypadLeft, keypadTop),
                size = androidx.compose.ui.geometry.Size(keypadWidth, keypadHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )

            // Keypad button dots (3 columns, 4 rows)
            val dotRadius = 1.dp.toPx()
            for (row in 0..3) {
                for (col in 0..2) {
                    drawCircle(
                        color = Color(0xFF94A3B8),
                        radius = dotRadius,
                        center = Offset(
                            keypadLeft + 4.dp.toPx() + col * 5.dp.toPx(),
                            keypadTop + 5.dp.toPx() + row * 6.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}

fun calculateDaysRemaining(deadlineString: String?): String {
    if (deadlineString == null) return "Tanpa deadline"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val deadlineDate = parser.parse(deadlineString) ?: return "Tanpa deadline"
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val diffInMillis = deadlineDate.time - currentDate.time
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
        
        when {
            diffInDays < 0 -> "Melewati deadline"
            diffInDays == 0L -> "Hari ini!"
            else -> "Sisa $diffInDays hari lagi"
        }
    } catch (e: Exception) {
        "Deadline tidak valid"
    }
}

@Composable
fun EmptyImpianView() {
    val isDark = com.example.kassaku.ui.theme.LocalIsDark.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        EmptyStateLottie(
            message = "Belum ada tabungan impian",
            subtitle = "Mulai menabung untuk mewujudkan impianmu!",
            isDark = isDark
        )
    }
}

@Composable
fun ImpianDetailDialog(item: ImpianItem, onDismissRequest: () -> Unit, currencyCode: String) {
    val danaTerkumpulVal = item.danaTerkumpul ?: 0L
    val sisaTargetVal = item.sisaTarget ?: 0L
    val progress = item.persentaseProgress ?: 0.0
    val statusText = if (item.isTercapai == true) "Target Tercapai" else "Sedang Menabung"
    val isTercapai = item.isTercapai == true
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isTercapai) {
        if (isTercapai) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = item.namaBarang ?: "Detail Tabungan", fontWeight = FontWeight.SemiBold) },
        text = {
            Box {
                Column(
                    verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)
                ) {
                    AsyncImage(
                        model = "http://10.0.2.2:8000/storage/${item.fotoBarang}",
                        contentDescription = item.namaBarang,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = painterResource(id = android.R.drawable.ic_menu_report_image)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Harga",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrencyFlexible(item.hargaBarang?.toDouble() ?: 0.0, currencyCode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatDate(item.deadline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Catatan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.keterangan ?: "Tidak ada catatan tambahan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Perkembangan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatCurrencyFlexible(danaTerkumpulVal.toDouble(), currencyCode)} / ${formatCurrencyFlexible(item.hargaBarang?.toDouble() ?: 0.0, currencyCode)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimary
                        )
                        Text(
                            text = "Masih kurang: ${formatCurrencyFlexible(sisaTargetVal.toDouble(), currencyCode)} • ${"%.1f".format(progress)}% • $statusText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (isTercapai) {
                    ConfettiEffect(
                        modifier = Modifier.fillMaxSize(),
                        isActive = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Tutup")
            }
        }
    )
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return "Tidak ada deadline"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val date = parser.parse(dateString)
        date?.let { formatter.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahImpianDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (namaBarang: String, hargaBarang: Long, deadline: String, keterangan: String?, fotoBarang: Uri?) -> Unit,
    currencyCode: String
) {
    var namaBarang by remember { mutableStateOf("") }
    var hargaBarang by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var keterangan by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isNamaError by remember { mutableStateOf(false) }
    var isHargaError by remember { mutableStateOf(false) }
    var isDeadlineError by remember { mutableStateOf(false) }
    var isImageError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
        isImageError = false
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Tambah Tabungan Impian", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isImageError) 
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isImageError) MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.outline
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Upload foto",
                                    modifier = Modifier.size(48.dp),
                                    tint = if (isImageError) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Klik untuk pilih foto",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isImageError) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isImageError) {
                                    Text(
                                        text = "Foto wajib diisi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = namaBarang,
                    onValueChange = { namaBarang = it; isNamaError = false },
                    label = { Text("Nama Barang") },
                    singleLine = true,
                    placeholder = { Text("Contoh: Laptop, Mobil, Umroh") },
                    isError = isNamaError,
                    supportingText = { if (isNamaError) Text("Nama barang tidak boleh kosong") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hargaBarang,
                    onValueChange = { newValue -> 
                        hargaBarang = newValue.filter { it.isDigit() }
                        isHargaError = false 
                    },
                    label = { Text("Harga Barang") },
                    prefix = { 
                        val prefix = when(currencyCode) {
                            "USD" -> "$"
                            "MYR" -> "RM"
                            "EUR" -> "€"
                            "SGD" -> "S$"
                            else -> "Rp"
                        }
                        Text(prefix) 
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorVisualTransformation(),
                    isError = isHargaError,
                    supportingText = { if (isHargaError) Text("Harga tidak valid") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan (Opsional)") },
                    placeholder = { Text("Catatan tambahan untuk impian") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { },
                    label = { Text("Deadline") },
                    placeholder = { Text("Pilih tanggal") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pilih tanggal",
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    },
                    isError = isDeadlineError,
                    supportingText = { if (isDeadlineError) Text("Deadline tidak boleh kosong") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isNamaError = namaBarang.isBlank()
                    val priceLong = hargaBarang.toLongOrNull()
                    isHargaError = priceLong == null || priceLong <= 0
                    isDeadlineError = deadline.isBlank()
                    isImageError = selectedImageUri == null

                    if (!isNamaError && !isHargaError && !isDeadlineError && !isImageError) {
                        onConfirm(namaBarang, priceLong ?: 0L, deadline, keterangan.takeIf { it.isNotBlank() }, selectedImageUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
            ) {
                Text("Simpan", color = StitchTextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal", color = StitchAccentRed)
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            deadline = formatMillisToString(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Pilih Deadline", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false
            )
        }
    }
}

fun createMultipartFromUri(context: Context, uri: Uri, partName: String): okhttp3.MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()
        
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/jpg", "image/jpeg" -> "jpg"
            else -> "jpg"
        }
        
        val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        
        okhttp3.MultipartBody.Part.createFormData(
            partName,
            "image_${System.currentTimeMillis()}.$extension",
            requestFile
        )
    } catch (e: Exception) {
        android.util.Log.e("ImpianScreen", "Error creating multipart: ${e.message}", e)
        null
    }
}

@Composable
fun HapusImpianDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val isDark = com.example.kassaku.ui.theme.LocalIsDark.current

    PremiumAlertDialog(
        onDismissRequest = onDismissRequest,
        isDark = isDark,
        title = "Masukkan Kata Sandi",
        text = null,
        confirmText = "Hapus",
        confirmColor = StitchAccentRed,
        onConfirm = { onConfirm(password) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Masukkan kata sandi kamu untuk menghapus tabungan impian ini.",
                    color = if (isDark) androidx.compose.ui.graphics.Color(0xFF94A3B8) else StitchTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
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
        }
    )
}
