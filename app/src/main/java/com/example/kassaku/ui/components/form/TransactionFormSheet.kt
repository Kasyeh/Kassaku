package com.example.kassaku.ui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.kassaku.ui.theme.StitchAccentRed
import com.example.kassaku.ui.theme.StitchTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Form state for transaction input.
 */
sealed interface TransactionFormState {
    object Idle : TransactionFormState
    object Submitting : TransactionFormState
    object Success : TransactionFormState
    data class Error(val message: String) : TransactionFormState
}

/**
 * Transaction form data container.
 */
data class TransactionFormData(
    val amount: Long,
    val category: String,
    val notes: String,
    val date: Long // millis
)

/**
 * Bottom sheet form for adding income or expense transactions.
 * Reusable for both types with minimal variation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormSheet(
    isVisible: Boolean,
    isExpense: Boolean,
    formState: TransactionFormState,
    onDismiss: () -> Unit,
    onSubmit: (TransactionFormData) -> Unit,
    customCategories: List<com.example.kassaku.ui.components.form.CategoryOption>? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isVisible) return
    
    val accentColor = if (isExpense) StitchAccentRed else StitchPrimary
    val title = if (isExpense) "Tambah Pengeluaran" else "Tambah Pemasukan"
    val categories = if (isExpense) {
        customCategories ?: ExpenseCategories.list
    } else {
        IncomeCategories.list
    }
    
    // Form fields
    var amount by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Validation states
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    
    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var showDismissConfirm by remember { mutableStateOf(false) }
    
    // Haptic feedback
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Form changed check
    val hasChanges by remember {
        derivedStateOf {
            amount.isNotEmpty() || notes.isNotEmpty() || categoryText.isNotEmpty()
        }
    }
    
    // Focus for amount field
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Handle success state
    LaunchedEffect(formState) {
        if (formState is TransactionFormState.Success) {
            onDismiss()
        }
    }
    
    val isSubmitting = formState is TransactionFormState.Submitting
    
    ModalBottomSheet(
        onDismissRequest = {
            if (!isSubmitting) {
                if (hasChanges) {
                    showDismissConfirm = true
                } else {
                    onDismiss()
                }
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
                    onClick = { 
                        if (!isSubmitting) {
                            if (hasChanges) {
                                showDismissConfirm = true
                            } else {
                                onDismiss()
                            }
                        }
                    },
                    enabled = !isSubmitting
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = StitchTextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Auto-focus amount field after sheet content is composed
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(300)
                try {
                    focusRequester.requestFocus()
                } catch (_: IllegalStateException) {
                    // FocusRequester not yet attached; ignore
                }
            }

            // Amount Input (Primary focus)
            AmountInputField(
                value = amount,
                onValueChange = { 
                    amount = it
                    amountError = null
                },
                modifier = Modifier.focusRequester(focusRequester),
                isExpense = isExpense,
                isError = amountError != null,
                errorMessage = amountError,
                enabled = !isSubmitting
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Quick nominal presets (sync with web)
            Text(
                text = "Nominal cepat:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StitchTextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val quickAmounts = listOf(10000L, 20000L, 50000L, 100000L, 200000L, 500000L)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quickAmounts.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { nominalCepat ->
                            Button(
                                onClick = {
                                    if (!isSubmitting) {
                                        amount = nominalCepat.toString()
                                        amountError = null
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSubmitting,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.12f),
                                    contentColor = accentColor
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text(
                                    text = "Rp ${String.format("%,d", nominalCepat).replace(',', '.')}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Category Input (Text Input as requested)
            OutlinedTextField(
                value = categoryText,
                onValueChange = { 
                    categoryText = it
                    categoryError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kategori") },
                placeholder = { Text("Contoh: Gaji, Makan, Transport") },
                singleLine = true,
                enabled = !isSubmitting,
                isError = categoryError != null,
                supportingText = if (categoryError != null) {
                    { Text(categoryError!!, color = StitchAccentRed) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Optional: Quick Selection Suggestions
            Text(
                text = "Saran:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StitchTextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            CategoryChipRow(
                categories = categories,
                selectedCategory = categoryText, // Highlight if exact match
                onCategorySelected = { 
                    categoryText = it
                    categoryError = null
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    keyboardController?.hide() // Hide keyboard to show Save button
                },
                isExpense = isExpense,
                enabled = !isSubmitting
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Notes Input (Optional)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Keterangan (opsional)") },
                placeholder = { Text("Contoh: Makan siang kantor") },
                singleLine = true,
                enabled = !isSubmitting
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date Display
            Surface(
                onClick = { 
                    if (!isSubmitting) {
                        keyboardController?.hide()
                        showDatePicker = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = formatDateDisplay(selectedDate),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Submit Button
            Button(
                onClick = {
                    // Validate
                    val amountValue = amount.toLongOrNull() ?: 0L
                    var hasError = false
                    
                    if (amountValue <= 0) {
                        amountError = "Nominal harus lebih dari 0"
                        hasError = true
                    }
                    
                    if (categoryText.isBlank()) {
                        categoryError = "Isi kategori"
                        hasError = true
                    }
                    
                    if (!hasError) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSubmit(
                            TransactionFormData(
                                amount = amountValue,
                                category = categoryText,
                                notes = notes.ifBlank { "-" },
                                date = selectedDate
                            )
                        )
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Double tap-like error feedback
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSubmitting && amount.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Simpan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Error message from submission
            if (formState is TransactionFormState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = formState.message,
                    color = StitchAccentRed,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
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
                showModeToggle = false
            )
        }
    }

    // Dismiss Confirmation Dialog
    if (showDismissConfirm) {
        AlertDialog(
            onDismissRequest = { showDismissConfirm = false },
            title = { Text("Batalkan Perubahan?", fontWeight = FontWeight.Bold) },
            text = { Text("Data yang sudah Anda masukkan akan terhapus.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDismissConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StitchAccentRed)
                ) {
                    Text("Ya, Batalkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDismissConfirm = false }) {
                    Text("Lanjutkan Mengisi")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

/**
 * Format date millis to display string.
 */
private fun formatDateDisplay(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    return formatter.format(Date(millis))
}
