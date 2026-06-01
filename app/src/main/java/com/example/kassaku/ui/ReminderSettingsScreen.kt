package com.example.kassaku.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.ReminderSettingsUiState
import com.example.kassaku.viewmodel.ReminderSettingsViewModel
import com.example.kassaku.viewmodel.SavePreferencesResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    onBack: () -> Unit,
    viewModel: ReminderSettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveResult by viewModel.saveResult.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = true // Matching the dark premium look from image

    val backgroundColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val surfaceColor = StitchSurfaceDark
    val textPrimary = Color.White
    val textSecondary = Color(0xFF94A3B8)
    val accentColor = StitchPrimary

    val remindersEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()
    val dailyReminderEnabled by viewModel.dailyReminderEnabled.collectAsStateWithLifecycle()
    val dailyReminderHour by viewModel.dailyReminderHour.collectAsStateWithLifecycle()
    val budgetAlertEnabled by viewModel.budgetAlertEnabled.collectAsStateWithLifecycle()
    val budgetAlertThreshold by viewModel.budgetAlertThreshold.collectAsStateWithLifecycle()
    val dreamReminderEnabled by viewModel.dreamReminderEnabled.collectAsStateWithLifecycle()
    val dreamInactiveDays by viewModel.dreamInactiveDays.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadPreferences()
    }

    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            is SavePreferencesResult.Success -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.resetSaveResult()
            }
            is SavePreferencesResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.resetSaveResult()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Pengingat Finansial", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, "Back", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                is ReminderSettingsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = accentColor
                    )
                }
                is ReminderSettingsUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text((uiState as ReminderSettingsUiState.Error).message, color = Color.White)
                        Button(onClick = { viewModel.loadPreferences() }) {
                            Text("Coba Lagi")
                        }
                    }
                }
                is ReminderSettingsUiState.Success, is ReminderSettingsUiState.Idle -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = KassakuSpacing.screenHorizontal,
                            end = KassakuSpacing.screenHorizontal,
                            top = KassakuSpacing.screenVertical,
                            bottom = KassakuSpacing.listBottom
                        ),
                        verticalArrangement = Arrangement.spacedBy(KassakuSpacing.sectionGap)
                    ) {
                        item {
                            Text(
                                "ASISTEN & NOTIFIKASI PINTAR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary,
                                letterSpacing = 1.sp
                            )
                        }

                        // Master Toggle Card
                        item {
                            ReminderSectionCard(
                                title = "Aktifkan Semua Notifikasi",
                                subtitle = "Nyalakan opsi ini agar asisten pintar KasSaku dapat mengirim pesan penting ke HP Anda.",
                                isEnabled = remindersEnabled,
                                onToggle = { viewModel.remindersEnabled.value = it },
                                surfaceColor = surfaceColor,
                                accentColor = accentColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                icon = Icons.Rounded.NotificationsActive
                            )
                        }

                        item {
                            AnimatedVisibility(
                                visible = remindersEnabled,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.sectionGap)) {
                                    // Daily Reminder Section
                                    Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)) {
                                        ReminderOptionRow(
                                            label = "Pengingat Lupa Mencatat Transaksi",
                                            isSelected = dailyReminderEnabled,
                                            onSelect = { viewModel.dailyReminderEnabled.value = it },
                                            accentColor = accentColor,
                                            textPrimary = textPrimary
                                        )
                                        
                                        AnimatedVisibility(
                                            visible = dailyReminderEnabled,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            val hourFloat = dailyReminderHour.toFloatOrNull() ?: 20f
                                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "KIRIM PENGINGAT SETIAP PUKUL",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textSecondary,
                                                        letterSpacing = 1.sp
                                                    )
                                                    Text(
                                                        String.format("%02d:00 WIB", hourFloat.toInt()),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = accentColor
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Slider(
                                                    value = hourFloat,
                                                    onValueChange = { viewModel.dailyReminderHour.value = kotlin.math.round(it).toInt().toString() },
                                                    valueRange = 0f..23f,
                                                    steps = 22,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = accentColor,
                                                        activeTrackColor = accentColor,
                                                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Budget Alert Section
                                    Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)) {
                                        ReminderOptionRow(
                                            label = "Alarm Batas Pengeluaran Kategori (Budget)",
                                            isSelected = budgetAlertEnabled,
                                            onSelect = { viewModel.budgetAlertEnabled.value = it },
                                            accentColor = accentColor,
                                            textPrimary = textPrimary
                                        )
                                        
                                        AnimatedVisibility(
                                            visible = budgetAlertEnabled,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            val thresholdValue = budgetAlertThreshold.toFloatOrNull() ?: 80f
                                            val warningColor = when {
                                                thresholdValue < 75f -> Color(0xFF10B981) // Emerald Green
                                                thresholdValue < 90f -> Color(0xFFF59E0B) // Amber Warning
                                                else -> Color(0xFFEF4444) // Neon Red Critical
                                            }
                                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "BERI TAHU SAYA SAAT PENGELUARAN MENCAPAI (%)",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textSecondary,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Text(
                                                        "${thresholdValue.toInt()}%",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = warningColor
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Slider(
                                                    value = thresholdValue,
                                                    onValueChange = { viewModel.budgetAlertThreshold.value = kotlin.math.round(it).toInt().toString() },
                                                    valueRange = 50f..100f,
                                                    steps = 49,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = warningColor,
                                                        activeTrackColor = warningColor,
                                                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Dream Reminder Section
                                    Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)) {
                                        ReminderOptionRow(
                                            label = "Pemberi Semangat & Motivator Impian",
                                            isSelected = dreamReminderEnabled,
                                            onSelect = { viewModel.dreamReminderEnabled.value = it },
                                            accentColor = accentColor,
                                            textPrimary = textPrimary
                                        )
                                        
                                        AnimatedVisibility(
                                            visible = dreamReminderEnabled,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            val inactiveDaysValue = dreamInactiveDays.toFloatOrNull() ?: 7f
                                            val chips = listOf(3 to "3 Hari", 7 to "7 Hari", 14 to "14 Hari", 30 to "30 Hari")
                                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "INGATKAN JIKA BELUM MENYETOR SELAMA",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textSecondary,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Text(
                                                        "${inactiveDaysValue.toInt()} Hari",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = accentColor
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    chips.forEach { (days, label) ->
                                                        val isSelected = inactiveDaysValue.toInt() == days
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(if (isSelected) accentColor else surfaceColor.copy(alpha = 0.4f))
                                                                .clickable { viewModel.dreamInactiveDays.value = days.toString() }
                                                                .padding(vertical = 10.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                label,
                                                                color = if (isSelected) Color.White else textSecondary,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Slider(
                                                    value = inactiveDaysValue,
                                                    onValueChange = { viewModel.dreamInactiveDays.value = kotlin.math.round(it).toInt().toString() },
                                                    valueRange = 1f..30f,
                                                    steps = 28,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = accentColor,
                                                        activeTrackColor = accentColor,
                                                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(32.dp)) }

                        // Save Button
                        item {
                            Button(
                                onClick = { viewModel.savePreferences() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = accentColor.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = Color.White
                                ),
                                enabled = saveResult !is SavePreferencesResult.Loading
                            ) {
                                if (saveResult is SavePreferencesResult.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("Simpan Preferensi Reminder", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderSectionCard(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    surfaceColor: Color,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onToggle(!isEnabled) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isEnabled) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    null, 
                    tint = if (isEnabled) accentColor else textSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = textSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            }
            
            Checkbox(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = accentColor,
                    uncheckedColor = textSecondary,
                    checkmarkColor = Color.White
                )
            )
        }
    }
}

@Composable
fun ReminderOptionRow(
    label: String,
    isSelected: Boolean,
    onSelect: (Boolean) -> Unit,
    accentColor: Color,
    textPrimary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect(!isSelected) }
            .padding(vertical = 14.dp, horizontal = KassakuSpacing.elementGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelect,
            colors = CheckboxDefaults.colors(
                checkedColor = accentColor,
                uncheckedColor = Color.White.copy(alpha = 0.2f),
                checkmarkColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    surfaceColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = textSecondary.copy(alpha = 0.4f)) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = surfaceColor.copy(alpha = 0.4f),
                focusedBorderColor = StitchPrimary.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                cursorColor = StitchPrimary,
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}
