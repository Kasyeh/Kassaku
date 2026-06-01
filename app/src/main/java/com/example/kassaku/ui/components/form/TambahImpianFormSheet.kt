package com.example.kassaku.ui.components.form

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchAccentRed
import com.example.kassaku.ui.theme.StitchTextSecondary
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface TambahImpianFormState {
    object Idle : TambahImpianFormState
    object Submitting : TambahImpianFormState
    object Success : TambahImpianFormState
    data class Error(val message: String) : TambahImpianFormState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahImpianFormSheet(
    isVisible: Boolean,
    formState: TambahImpianFormState,
    onDismiss: () -> Unit,
    onSubmit: (namaBarang: String, hargaBarang: Long, deadline: String, keterangan: String?, fotoBarang: Uri?) -> Unit,
    currencyCode: String,
    initialNamaBarang: String = "",
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isVisible) return

    val accentColor = StitchPrimary

    var namaBarang by remember(initialNamaBarang, isVisible) { mutableStateOf(initialNamaBarang) }
    var hargaBarang by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var keterangan by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isNamaError by remember { mutableStateOf(false) }
    var isHargaError by remember { mutableStateOf(false) }
    var isDeadlineError by remember { mutableStateOf(false) }
    var isImageError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDismissConfirm by remember { mutableStateOf(false) }

    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val haptic = LocalHapticFeedback.current
    val hasChanges by remember {
        derivedStateOf {
            namaBarang.isNotEmpty() || hargaBarang.isNotEmpty() || 
            deadline.isNotEmpty() || keterangan.isNotEmpty() || selectedImageUri != null
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
        isImageError = false
    }

    LaunchedEffect(formState) {
        if (formState is TambahImpianFormState.Success) {
            onDismiss()
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(100)
            runCatching { focusRequester.requestFocus() }
        }
    }

    val isSubmitting = formState is TambahImpianFormState.Submitting

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
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tambah Tabungan Impian",
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

            // Amount Input (Primary focus for Target Price / Harga Barang)
            AmountInputField(
                value = hargaBarang,
                onValueChange = { 
                    hargaBarang = it
                    isHargaError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                isExpense = false,
                isError = isHargaError,
                errorMessage = if (isHargaError) "Harga harus lebih dari 0" else null,
                enabled = !isSubmitting
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Target Presets in Millions
            Text(
                text = "Pilih target dana:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StitchTextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val quickAmounts = listOf(1000000L, 2000000L, 5000000L, 10000000L, 25000000L, 50000000L)
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
                                        hargaBarang = nominalCepat.toString()
                                        isHargaError = false
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
                                val prefix = when (currencyCode) {
                                    "USD" -> "$"
                                    "MYR" -> "RM"
                                    "EUR" -> "€"
                                    "SGD" -> "S$"
                                    else -> "Rp"
                                }
                                val formatted = if (currencyCode == "IDR" || prefix == "Rp") {
                                    val formattedNum = String.format("%,d", nominalCepat).replace(',', '.')
                                    "Rp $formattedNum"
                                } else {
                                    val formattedNum = String.format("%,d", nominalCepat)
                                    "$prefix $formattedNum"
                                }
                                Text(
                                    text = formatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nama Barang Input
            OutlinedTextField(
                value = namaBarang,
                onValueChange = { namaBarang = it; isNamaError = false },
                label = { Text("Nama Barang") },
                singleLine = true,
                placeholder = { Text("Contoh: Laptop, Mobil, Umroh") },
                isError = isNamaError,
                supportingText = if (isNamaError) {
                    { Text("Nama barang tidak boleh kosong", color = StitchAccentRed) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Common Dream suggestions
            Text(
                text = "Saran Impian:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StitchTextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val commonDreams = listOf(
                CategoryOption("laptop", "Laptop 💻"),
                CategoryOption("smartphone", "Gadget 📱"),
                CategoryOption("motor", "Motor 🏍️"),
                CategoryOption("mobil", "Mobil 🚗"),
                CategoryOption("umroh", "Umroh 🕋"),
                CategoryOption("menikah", "Menikah 💍"),
                CategoryOption("liburan", "Liburan ✈️"),
                CategoryOption("rumah", "Rumah 🏠")
            )

            CategoryChipRow(
                categories = commonDreams,
                selectedCategory = namaBarang,
                onCategorySelected = { selectedLabel ->
                    namaBarang = selectedLabel
                    isNamaError = false
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    keyboardController?.hide()
                },
                isExpense = false,
                enabled = !isSubmitting
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Image Upload Card
            Text(
                text = "Foto Impian (wajib):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StitchTextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable {
                        if (!isSubmitting) {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isImageError)
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isImageError) StitchAccentRed
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
                                    StitchAccentRed
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Klik untuk pilih foto",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isImageError)
                                    StitchAccentRed
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isImageError) {
                                Text(
                                    text = "Foto wajib diisi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StitchAccentRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Deadline selector card
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
                    Column {
                        Text(
                            text = "Deadline",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = StitchTextSecondary
                        )
                        Text(
                            text = if (deadline.isNotEmpty()) deadline else "Pilih tanggal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (isDeadlineError) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Wajib diisi",
                            fontSize = 12.sp,
                            color = StitchAccentRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Input (Optional)
            OutlinedTextField(
                value = keterangan,
                onValueChange = { keterangan = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Catatan (opsional)") },
                placeholder = { Text("Contoh: Nabung untuk masa depan") },
                singleLine = true,
                enabled = !isSubmitting,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    isNamaError = namaBarang.isBlank()
                    isHargaError = hargaBarang.toLongOrNull() == null || hargaBarang.toLongOrNull()!! <= 0
                    isDeadlineError = deadline.isBlank()
                    isImageError = selectedImageUri == null

                    if (!isNamaError && !isHargaError && !isDeadlineError && !isImageError) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSubmit(
                            namaBarang,
                            hargaBarang.toLong(),
                            deadline,
                            keterangan.ifBlank { null },
                            selectedImageUri
                        )
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSubmitting,
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

            if (formState is TambahImpianFormState.Error) {
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                            deadline = formatDateDisplay(it)
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

private fun formatDateDisplay(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}
