package com.example.kassaku.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kassaku.R
import com.example.kassaku.ui.components.AuroraBackground
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.LoginUiState
import com.example.kassaku.viewmodel.LoginViewModel
import com.example.kassaku.viewmodel.UnblockUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (userId: Int, rememberMe: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    onSyncFCM: (Int?) -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    val loginUiState = loginViewModel.loginUiState
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var blockedInfo by remember { mutableStateOf<LoginUiState.Blocked?>(null) }
    var unblockPesan by remember { mutableStateOf("") }
    val unblockUiState = loginViewModel.unblockUiState
    val unblockResponse by loginViewModel.unblockResponseState.collectAsState()
    var showUnblockResponseDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    // Animation States
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Colors - Glass Style
    val glassColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f)
    val glassBorderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.4f)
    val textPrimary = if (isDark) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val primaryColor = StitchPrimary

    LaunchedEffect(loginUiState) {
        when (loginUiState) {
            is LoginUiState.Success -> {
                val user = loginUiState.user
                Toast.makeText(context, "Welcome back, ${user.username}", Toast.LENGTH_SHORT).show()
                errorMessage = null
                onLoginSuccess(user.idUser, rememberMe)
                loginViewModel.resetLoginState()
            }
            is LoginUiState.Error -> {
                errorMessage = loginUiState.message
                showBlockedDialog = false
            }
            is LoginUiState.Blocked -> {
                errorMessage = null
                blockedInfo = loginUiState
                showBlockedDialog = true
                // Sync token even if blocked so admin can notify
                onSyncFCM(loginUiState.idUser)
                // Start listening for admin's unblock response via RTDB
                loginViewModel.startListeningUnblockResponse(loginUiState.idUser)
            }
            LoginUiState.Idle, LoginUiState.Loading -> {
                errorMessage = null
                showBlockedDialog = false
            }
        }
    }

    // Listen for realtime unblock response from admin
    LaunchedEffect(unblockResponse) {
        unblockResponse?.let { response ->
            showUnblockResponseDialog = true
        }
    }

    // Clean up RTDB listener when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            loginViewModel.stopListeningUnblockResponse()
        }
    }

    LaunchedEffect(unblockUiState) {
        if (unblockUiState is UnblockUiState.Success) {
            Toast.makeText(context, unblockUiState.message, Toast.LENGTH_LONG).show()
            showBlockedDialog = false
            unblockPesan = ""
            loginViewModel.resetUnblockState()
            loginViewModel.resetLoginState()
        }
    }

    AuroraBackground(isDark = isDark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Sectionhttps://age-verification.privateid.com/handoff-age?handoffId=a5a8bd91-86d5-4d14-b231-d0e0bd94e46d
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800)) + expandVertically(expandFrom = Alignment.Top)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .shadow(24.dp, CircleShape, spotColor = primaryColor.copy(alpha = 0.3f))
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f))
                                .border(1.dp, glassBorderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(56.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "KasSaku",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary,
                            letterSpacing = (-1).sp
                        )
                        
                        Text(
                            text = "Kelola Uangmu dengan Mudah",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSecondary,
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Glass Card Form
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(1000, delayMillis = 300)) + slideInVertically(initialOffsetY = { 50 })
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, glassBorderColor, RoundedCornerShape(32.dp)),
                        shape = RoundedCornerShape(32.dp),
                        color = glassColor,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Masuk ke Akun",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )

                            // Username
                            TextField(
                                value = username,
                                onValueChange = { username = it },
                                placeholder = { 
                                    Text(
                                        "Username", 
                                        color = if (errorMessage != null) StitchNegative.copy(alpha = 0.6f) else textSecondary.copy(alpha = 0.6f)
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(
                                        width = if (errorMessage != null) 2.dp else 0.dp,
                                        color = if (errorMessage != null) StitchNegative else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null, // Or generic icon
                                        tint = if (errorMessage != null) StitchNegative else textSecondary
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (errorMessage != null) StitchNegative.copy(alpha = 0.1f) 
                                        else if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                    unfocusedContainerColor = if (errorMessage != null) StitchNegative.copy(alpha = 0.1f) 
                                        else if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = if (errorMessage != null) StitchNegative else primaryColor,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            // Password
                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { 
                                    Text(
                                        "Password", 
                                        color = if (errorMessage != null) StitchNegative.copy(alpha = 0.6f) else textSecondary.copy(alpha = 0.6f)
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(
                                        width = if (errorMessage != null) 2.dp else 0.dp,
                                        color = if (errorMessage != null) StitchNegative else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = {
                                     Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (errorMessage != null) StitchNegative else textSecondary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = if (errorMessage != null) StitchNegative else textSecondary
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (errorMessage != null) StitchNegative.copy(alpha = 0.1f) 
                                        else if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                    unfocusedContainerColor = if (errorMessage != null) StitchNegative.copy(alpha = 0.1f) 
                                        else if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = if (errorMessage != null) StitchNegative else primaryColor,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    loginViewModel.login(username, password)
                                })
                            )

                            // Remember Me Checkbox
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { rememberMe = !rememberMe },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = primaryColor,
                                        uncheckedColor = textSecondary,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Text(
                                    text = "Ingat Saya",
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = StitchNegative,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(start = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                                
                                // Error Dialog
                                AlertDialog(
                                    onDismissRequest = { 
                                        errorMessage = null 
                                        loginViewModel.resetLoginState()
                                    },
                                    title = {
                                        Text(
                                            text = "Login Gagal",
                                            fontWeight = FontWeight.Bold,
                                            color = StitchNegative
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = errorMessage ?: "Terjadi kesalahan",
                                            color = textPrimary
                                        )
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = { 
                                                errorMessage = null
                                                loginViewModel.resetLoginState()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = StitchNegative
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Coba Lagi", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    containerColor = if (isDark) Color(0xFF1E1E2D) else Color.White,
                                    shape = RoundedCornerShape(24.dp),
                                    icon = {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Error, // Standard Error Icon
                                            contentDescription = null,
                                            tint = StitchNegative,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Login Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    loginViewModel.login(username, password)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(12.dp, CircleShape, spotColor = primaryColor.copy(alpha = 0.5f)),
                                enabled = loginUiState !is LoginUiState.Loading,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.White
                                )
                            ) {
                                if (loginUiState is LoginUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }

                // Blocked Dialog with Request Form
                if (showBlockedDialog && blockedInfo != null) {
                    val hasPendingUnblock = blockedInfo?.pendingUnblock == true
                    AlertDialog(
                        onDismissRequest = { 
                            showBlockedDialog = false
                            loginViewModel.resetLoginState()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = StitchPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = {
                            Text(
                                text = "Akun Diblokir",
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = blockedInfo?.message ?: "Akun Anda sedang ditangguhkan oleh admin.",
                                    color = textSecondary,
                                    fontSize = 14.sp
                                )

                                if (hasPendingUnblock) {
                                    Surface(
                                        color = StitchPrimary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HourglassTop,
                                                contentDescription = null,
                                                tint = StitchPrimary
                                            )
                                            Column {
                                                Text(
                                                    text = "Permintaan sedang diproses",
                                                    color = textPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Admin belum memberi respons. Anda tidak perlu mengirim permintaan baru.",
                                                    color = textSecondary,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                if (blockedInfo?.rejectedUnblock == true) {
                                    Surface(
                                        color = StitchNegative.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = blockedInfo?.rejectedMessage ?: "Permintaan sebelumnya ditolak.",
                                            color = StitchNegative,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(8.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Divider(color = glassBorderColor)

                                Text(
                                    text = if (hasPendingUnblock) "Status Permintaan Unblock" else "Ajukan Permintaan Unblock",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary
                                )

                                if (hasPendingUnblock) {
                                    Text(
                                        text = "Anda bisa menunggu notifikasi dari admin. Jika permintaan disetujui atau ditolak, aplikasi akan memberi tahu Anda secara realtime.",
                                        color = textSecondary,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = unblockPesan,
                                        onValueChange = { unblockPesan = it },
                                        placeholder = { Text("Alasan atau pesan ke admin...", fontSize = 14.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        maxLines = 5,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = StitchPrimary,
                                            unfocusedBorderColor = textSecondary.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                if (unblockUiState is UnblockUiState.Error) {
                                    Text(
                                        text = unblockUiState.message,
                                        color = StitchNegative,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { 
                                    blockedInfo?.let { 
                                        loginViewModel.submitUnblockRequest(it.idUser, unblockPesan)
                                    }
                                },
                                enabled = !hasPendingUnblock && unblockUiState !is UnblockUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (unblockUiState is UnblockUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (hasPendingUnblock) "Menunggu Respons Admin" else "Kirim Permintaan",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { 
                                showBlockedDialog = false 
                                loginViewModel.resetLoginState()
                            }) {
                                Text("Batal", color = textSecondary)
                            }
                        },
                        containerColor = if (isDark) Color(0xFF1E1E2D) else Color.White,
                        shape = RoundedCornerShape(28.dp)
                    )
                }

                // Realtime Unblock Response Dialog from Admin
                if (showUnblockResponseDialog && unblockResponse != null) {
                    val isApproved = unblockResponse?.status == "approved"
                    AlertDialog(
                        onDismissRequest = {
                            showUnblockResponseDialog = false
                            blockedInfo?.let { loginViewModel.acknowledgeUnblockResponse(it.idUser) }
                            if (isApproved) {
                                showBlockedDialog = false
                                loginViewModel.resetLoginState()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isApproved) Icons.Default.Lock else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isApproved) StitchPrimary else StitchNegative,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = {
                            Text(
                                text = if (isApproved) "🎉 Permintaan Disetujui!" else "❌ Permintaan Ditolak",
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        },
                        text = {
                            Text(
                                text = unblockResponse?.message ?: if (isApproved)
                                    "Akun Anda sudah aktif kembali. Silakan login ulang."
                                else
                                    "Permintaan unblock Anda ditolak oleh admin.",
                                color = textSecondary,
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showUnblockResponseDialog = false
                                    blockedInfo?.let { loginViewModel.acknowledgeUnblockResponse(it.idUser) }
                                    if (isApproved) {
                                        showBlockedDialog = false
                                        loginViewModel.resetLoginState()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isApproved) StitchPrimary else StitchNegative
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isApproved) "Login Sekarang" else "Tutup",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        containerColor = if (isDark) Color(0xFF1E1E2D) else Color.White,
                        shape = RoundedCornerShape(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Footer
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, delayMillis = 600))
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(glassColor.copy(alpha = 0.3f))
                            .border(0.5.dp, glassBorderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Belum punya akun? ",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                        Text(
                            text = "Daftar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = { _, _ -> }, onNavigateToRegister = {}, onSyncFCM = {})
}
