package com.example.kassaku.ui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchTextSecondary
import kotlinx.coroutines.delay

data class BudgetFormData(
    val category: String,
    val amount: Long,
    val period: String,
    val startDate: String?,
    val endDate: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetFormSheet(
    isVisible: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (BudgetFormData) -> Unit,
    isDark: Boolean,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isVisible) return
    
    val accentColor = StitchPrimary
    val title = "Set Batas Belanja"
    
    // Form fields
    var amount by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("") }
    var periode by remember { mutableStateOf("bulanan") }
    var tglMulai by remember { mutableStateOf("") }
    var tglAkhir by remember { mutableStateOf("") }
    
    // Validation states
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    
    // Haptic feedback
    val haptic = LocalHapticFeedback.current
    
    // Focus for amount field
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    ModalBottomSheet(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                IconButton(
                    onClick = { if (!isLoading) onDismiss() },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = StitchTextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Amount Input (Primary focus)
            AmountInputField(
                value = amount,
                onValueChange = { 
                    amount = it
                    amountError = null
                },
                modifier = Modifier.focusRequester(focusRequester),
                isExpense = true, // Force red accent or use StitchPrimary depending on design. For budget, usually we use primary or warning.
                isError = amountError != null,
                errorMessage = amountError,
                enabled = !isLoading
            )

            // Wait for sheet + input to attach before requesting focus (avoids force close)
            LaunchedEffect(isVisible) {
                if (isVisible) {
                    delay(100)
                    runCatching { focusRequester.requestFocus() }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Quick nominal presets
            val quickAmounts = listOf(100000L, 500000L, 1000000L, 2000000L)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickAmounts) { nominalCepat ->
                    AssistChip(
                        onClick = {
                            if (!isLoading) {
                                amount = nominalCepat.toString()
                                amountError = null
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        label = {
                            Text(
                                text = "Rp ${String.format("%,d", nominalCepat).replace(',', '.')}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = accentColor.copy(alpha = 0.12f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Category Input
            OutlinedTextField(
                value = categoryText,
                onValueChange = { 
                    categoryText = it
                    categoryError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Jenis Kategori") },
                placeholder = { Text("Cth: Makanan, Transportasi") },
                singleLine = true,
                enabled = !isLoading,
                isError = categoryError != null,
                supportingText = if (categoryError != null) {
                    { Text(categoryError!!, color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor
                ),
                shape = RoundedCornerShape(16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick Select Categories
            val saranKategori = listOf("Makanan", "Transportasi", "Tagihan", "Belanja", "Hiburan", "Dana Darurat")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(saranKategori) { item ->
                    AssistChip(
                        onClick = { 
                            categoryText = item 
                            categoryError = null
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            keyboardController?.hide()
                        },
                        label = { 
                            Text(text = item, fontSize = 12.sp, fontWeight = FontWeight.Bold) 
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Period Selection
            Text("Periode", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("mingguan", "bulanan", "custom").forEach { p ->
                    FilterChip(
                        selected = periode == p,
                        onClick = { periode = p; dateError = null },
                        label = { Text(p.replaceFirstChar { it.uppercase() }) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (periode == "custom") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tglMulai,
                        onValueChange = { tglMulai = it; dateError = null },
                        label = { Text("Mulai") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        enabled = !isLoading,
                        isError = dateError != null
                    )
                    OutlinedTextField(
                        value = tglAkhir,
                        onValueChange = { tglAkhir = it; dateError = null },
                        label = { Text("Akhir") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        enabled = !isLoading,
                        isError = dateError != null
                    )
                }
                if (dateError != null) {
                    Text(
                        text = dateError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Submit Button
            Button(
                onClick = {
                    val amountValue = amount.toLongOrNull() ?: 0L
                    var hasError = false
                    
                    if (amountValue <= 0) {
                        amountError = "Nominal tidak valid"
                        hasError = true
                    }
                    if (categoryText.isBlank()) {
                        categoryError = "Kategori wajib diisi"
                        hasError = true
                    }
                    if (periode == "custom" && (tglMulai.isBlank() || tglAkhir.isBlank())) {
                        dateError = "Tanggal wajib diisi"
                        hasError = true
                    }
                    
                    if (!hasError) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSubmit(
                            BudgetFormData(
                                category = categoryText,
                                amount = amountValue,
                                period = periode,
                                startDate = tglMulai.ifBlank { null },
                                endDate = tglAkhir.ifBlank { null }
                            )
                        )
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Simpan Budget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
