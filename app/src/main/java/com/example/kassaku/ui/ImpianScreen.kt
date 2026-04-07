package com.example.kassaku.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kassaku.data.remote.model.ImpianItem
import com.example.kassaku.ui.theme.*
import com.example.kassaku.ui.components.LogoutConfirmationDialog
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
    var showTambahDialog by remember { mutableStateOf(false) }
    var selectedImpian by remember { mutableStateOf<ImpianItem?>(null) }
    var showHapusDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var impianToDelete by remember { mutableStateOf<ImpianItem?>(null) }
    var impianToSetor by remember { mutableStateOf<ImpianItem?>(null) }
    var showSetorDialog by remember { mutableStateOf(false) }

    // Colors
    val backgroundColor = if (isDark) StitchBackgroundDark else StitchBackgroundLight
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary

    LaunchedEffect(key1 = userId) {
        homeViewModel.fetchImpian(userId)
    }

    // Handle tambah impian result
    LaunchedEffect(key1 = homeViewModel) {
        launch {
            homeViewModel.tambahImpianResult.collectLatest { result ->
                when (result) {
                    is TambahImpianResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        homeViewModel.resetTambahImpianResult()
                    }
                    is TambahImpianResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        homeViewModel.resetTambahImpianResult()
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
                    }
                    is SetorImpianResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        homeViewModel.resetSetorImpianResult()
                    }
                    else -> {}
                }
            }
        }
    }

    val impianUiState by homeViewModel.impianUiState.collectAsState()
    val primaryColor = StitchPrimary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Curved Header Background (matching HomeScreen)

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
                        text = "Impian Saya",
                        color = textPrimary,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showTambahDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.05f))
                        ) {
                            Icon(Icons.Default.Add, "Tambah", tint = textPrimary, modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.05f))
                        ) {
                            Icon(Icons.Filled.ExitToApp, "Logout", tint = StitchTextPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

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
                        EmptyImpianView()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.impianItems, key = { it.idImpian!! }) { item ->
                                ImpianItemRow(
                                    item = item, 
                                    onClick = { selectedImpian = item },
                                    onDeleteClick = { 
                                        impianToDelete = item
                                        showHapusDialog = true 
                                    },
                                    onSetorClick = {
                                        impianToSetor = item
                                        showSetorDialog = true
                                    },
                                    isDark = isDark,
                                    surfaceColor = surfaceColor
                                )
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
                            text = "Gagal memuat impian: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { homeViewModel.fetchImpian(userId) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
        }
        
        selectedImpian?.let { item ->
            ImpianDetailDialog(
                item = item,
                onDismissRequest = { selectedImpian = null }
            )
        }
        
        // Dialog tambah impian
        if (showTambahDialog) {
            TambahImpianDialog(
                onDismissRequest = { showTambahDialog = false },
                onConfirm = { namaBarang, hargaBarang, deadline, keterangan, imageUri ->
                    showTambahDialog = false
                    val fotoBarang = imageUri?.let { uri ->
                        createMultipartFromUri(context, uri, "foto_barang")
                    }
                    homeViewModel.tambahImpian(userId, namaBarang, hargaBarang, deadline, keterangan, fotoBarang)
                }
            )
        }

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

        if (showSetorDialog) {
            SetorImpianDialog(
                item = impianToSetor,
                onDismissRequest = {
                    showSetorDialog = false
                    impianToSetor = null
                },
                onConfirm = { nominal, keterangan ->
                    val target = impianToSetor
                    if (target != null) {
                        homeViewModel.setorImpian(target.idImpian, userId, nominal, keterangan)
                    }
                    showSetorDialog = false
                    impianToSetor = null
                }
            )
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
    surfaceColor: Color
) {
    val numberFormatter = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    val formattedPrice = numberFormatter.format(item.hargaBarang ?: 0)

    val progressValue = ((item.persentaseProgress ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)
    val danaTerkumpul = item.danaTerkumpul ?: 0L
    val sisaTarget = item.sisaTarget ?: 0L
    val isTargetTercapai = item.isTercapai == true

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                RoundedCornerShape(24.dp)
            )
            .clickable { onClick(item) }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Delete Button at Top-Right
            IconButton(
                onClick = { onDeleteClick(item) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Larger Image
                    AsyncImage(
                        model = "http://10.0.2.2:8000/storage/${item.fotoBarang}",
                        contentDescription = item.namaBarang,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Gray.copy(alpha = 0.1f)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = painterResource(id = android.R.drawable.ic_menu_report_image)
                    )

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = StitchTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatDate(item.deadline),
                                style = MaterialTheme.typography.bodySmall,
                                color = StitchTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rp $formattedPrice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = StitchPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { progressValue },
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
                                text = "Terkumpul",
                                style = MaterialTheme.typography.labelSmall,
                                color = StitchTextSecondary
                            )
                            Text(
                                text = "Rp ${numberFormatter.format(danaTerkumpul)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else StitchTextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Sisa Target",
                                style = MaterialTheme.typography.labelSmall,
                                color = StitchTextSecondary
                            )
                            Text(
                                text = "Rp ${numberFormatter.format(sisaTarget)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isTargetTercapai) Color(0xFF10B981) else StitchNegative
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button
                if (isTargetTercapai) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Target Tercapai! 🎉",
                            modifier = Modifier.padding(vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
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
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Setor Dana",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyImpianView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Paid,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = StitchTextSecondary.copy(alpha = 0.5f)
            )
            Text(
                text = "Belum ada impian",
                style = MaterialTheme.typography.titleMedium,
                color = StitchTextSecondary
            )
            Text(
                text = "Mulai menabung untuk mewujudkan impianmu!",
                style = MaterialTheme.typography.bodyMedium,
                color = StitchTextSecondary.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ImpianDetailDialog(item: ImpianItem, onDismissRequest: () -> Unit) {
    val numberFormatter = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    val formattedPrice = numberFormatter.format(item.hargaBarang ?: 0)
    val danaTerkumpul = numberFormatter.format(item.danaTerkumpul ?: 0)
    val sisaTarget = numberFormatter.format(item.sisaTarget ?: 0)
    val progress = item.persentaseProgress ?: 0.0
    val statusText = if (item.isTercapai == true) "Target Tercapai" else "Sedang Menabung"

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = item.namaBarang ?: "Detail Impian", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "Rp $formattedPrice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        text = "Progress Nyata",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rp $danaTerkumpul / Rp $formattedPrice",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary
                    )
                    Text(
                        text = "Sisa target: Rp $sisaTarget • ${"%.1f".format(progress)}% • $statusText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    onConfirm: (namaBarang: String, hargaBarang: Long, deadline: String, keterangan: String?, fotoBarang: Uri?) -> Unit
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
        title = { Text(text = "Tambah Impian", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Image Picker
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

                // Nama Barang
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

                // Harga Barang
                OutlinedTextField(
                    value = hargaBarang,
                    onValueChange = { newValue -> 
                        hargaBarang = newValue.filter { it.isDigit() }
                        isHargaError = false 
                    },
                    label = { Text("Harga Barang") },
                    prefix = { Text("Rp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorVisualTransformation(),
                    isError = isHargaError,
                    supportingText = { if (isHargaError) Text("Harga tidak valid") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Keterangan (opsional)
                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan (Opsional)") },
                    placeholder = { Text("Catatan tambahan untuk impian") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // Deadline
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
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal")
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
fun SetorImpianDialog(
    item: ImpianItem?,
    onDismissRequest: () -> Unit,
    onConfirm: (nominal: Long, keterangan: String?) -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    var nominalInput by remember { mutableStateOf("") }
    var keterangan by remember { mutableStateOf("") }
    var isNominalError by remember { mutableStateOf(false) }
    val sisaTarget = item?.sisaTarget ?: 0L

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Setor Impian", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = item?.namaBarang ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sisa target: Rp ${formatter.format(sisaTarget)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = { value ->
                        nominalInput = value.filter { it.isDigit() }
                        isNominalError = false
                    },
                    label = { Text("Nominal Setoran") },
                    prefix = { Text("Rp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorVisualTransformation(),
                    isError = isNominalError,
                    supportingText = {
                        if (isNominalError) Text("Nominal tidak valid atau melebihi sisa target")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan (Opsional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nominal = nominalInput.toLongOrNull() ?: 0L
                    isNominalError = nominal <= 0L || nominal > sisaTarget
                    if (!isNominalError) {
                        onConfirm(nominal, keterangan.takeIf { it.isNotBlank() })
                    }
                },
                enabled = item != null && sisaTarget > 0
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun HapusImpianDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Verifikasi Password", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Silakan masukkan password Anda untuk menghapus impian ini.")
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
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal")
            }
        }
    )
}
