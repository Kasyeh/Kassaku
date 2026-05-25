package com.example.kassaku.ui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.data.remote.model.ImpianItem
import com.example.kassaku.ui.theme.StitchAccentRed
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchTextSecondary
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

/**
 * Form state for setor impian input.
 */
sealed interface SetorImpianFormState {
    object Idle : SetorImpianFormState
    object Submitting : SetorImpianFormState
    object Success : SetorImpianFormState
    data class Error(val message: String) : SetorImpianFormState
}

/**
 * Bottom sheet form for depositing funds to an impian (dream/goal).
 * Design is consistent with TransactionFormSheet (Tambah Pemasukan/Pengeluaran).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetorImpianFormSheet(
    isVisible: Boolean,
    item: ImpianItem?,
    formState: SetorImpianFormState,
    onDismiss: () -> Unit,
    onSubmit: (nominal: Long, keterangan: String?) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isVisible || item == null) return

    val accentColor = StitchPrimary
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    val sisaTarget = (item.sisaTarget ?: 0.0).toLong()
    val danaTerkumpul = (item.danaTerkumpul ?: 0.0).toLong()
    val progressValue = ((item.persentaseProgress ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)

    // Form fields
    var amount by remember { mutableStateOf("") }
    var keterangan by remember { mutableStateOf("") }

    // Validation states
    var amountError by remember { mutableStateOf<String?>(null) }

    // UI states
    var showDismissConfirm by remember { mutableStateOf(false) }

    // Haptic feedback
    val haptic = LocalHapticFeedback.current

    // Form changed check
    val hasChanges by remember {
        derivedStateOf {
            amount.isNotEmpty() || keterangan.isNotEmpty()
        }
    }

    // Focus for amount field
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle success state
    LaunchedEffect(formState) {
        if (formState is SetorImpianFormState.Success) {
            onDismiss()
        }
    }

    val isSubmitting = formState is SetorImpianFormState.Submitting

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
                    text = "Setor Impian",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Impian Info Card
            Surface(
                color = accentColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = item.namaBarang ?: "-",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.15f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Terkumpul",
                                fontSize = 11.sp,
                                color = StitchTextSecondary
                            )
                            Text(
                                text = "Rp ${formatter.format(danaTerkumpul)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Sisa Target",
                                fontSize = 11.sp,
                                color = StitchTextSecondary
                            )
                            Text(
                                text = "Rp ${formatter.format(sisaTarget)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
                isExpense = false,
                isError = amountError != null,
                errorMessage = amountError,
                enabled = !isSubmitting
            )

            LaunchedEffect(isVisible) {
                if (isVisible) {
                    delay(100)
                    runCatching { focusRequester.requestFocus() }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick nominal presets
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
                            val isOverLimit = nominalCepat > sisaTarget
                            Button(
                                onClick = {
                                    if (!isSubmitting) {
                                        amount = nominalCepat.toString()
                                        amountError = null
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSubmitting && !isOverLimit,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.12f),
                                    contentColor = accentColor,
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.08f),
                                    disabledContentColor = Color.Gray.copy(alpha = 0.4f)
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

            // Keterangan Input (Optional)
            OutlinedTextField(
                value = keterangan,
                onValueChange = { keterangan = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Keterangan (opsional)") },
                placeholder = { Text("Contoh: Nabung mingguan") },
                singleLine = true,
                enabled = !isSubmitting
            )

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
                    } else if (amountValue > sisaTarget) {
                        amountError = "Nominal melebihi sisa target (Rp ${formatter.format(sisaTarget)})"
                        hasError = true
                    }

                    if (!hasError) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSubmit(amountValue, keterangan.ifBlank { null })
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan Setoran",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error message from submission
            if (formState is SetorImpianFormState.Error) {
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
