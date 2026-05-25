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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.graphics.Brush
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
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.LoginUiState
import com.example.kassaku.viewmodel.LoginViewModel
import com.example.kassaku.viewmodel.UnblockUiState
import kotlinx.coroutines.delay
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (userId: Int, rememberMe: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    onSyncFCM: (Int?) -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    val loginUiState = loginViewModel.loginUiState
    val forgotPasswordUiState = loginViewModel.forgotPasswordUiState
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
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

    // Google Sign In Setup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("277429880739-4ihakunhcgftda8b7ltqj40k4a8rdlmk.apps.googleusercontent.com")
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                loginViewModel.loginWithGoogle(idToken)
            } ?: run {
                Toast.makeText(context, "Google login failed: No ID Token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            val statusCode = e.statusCode
            val statusMessage = com.google.android.gms.common.api.CommonStatusCodes.getStatusCodeString(statusCode)
            Log.e("LoginScreen", "Google sign in failed. Code: $statusCode ($statusMessage)", e)
            
            val friendlyMessage = when(statusCode) {
                10 -> "Developer Error (Cek SHA-1 & Client ID di Firebase)"
                7 -> "Network Error (Cek koneksi internet)"
                12500 -> "Sign-in Gagal (Cek konfigurasi Google Play Services)"
                12501 -> "Login dibatalkan oleh pengguna"
                else -> e.message ?: "Terjadi kesalahan"
            }
            Toast.makeText(context, "Google login failed: $friendlyMessage", Toast.LENGTH_LONG).show()
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
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo KasSaku",
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Glass Card Form
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(1000, delayMillis = 300)) + slideInVertically(initialOffsetY = { 50 })
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, glassBorderColor, RoundedCornerShape(48.dp)),
                        shape = RoundedCornerShape(48.dp),
                        color = glassColor,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(KassakuSpacing.cardGap)
                        ) {
                            Text(
                                text = "Masuk ke Akun",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Kelola Uangmu dengan Mudah",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary
                            )

                            // Username
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "USERNAME",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textSecondary.copy(alpha = 0.7f),
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                TextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    placeholder = { Text("Masukkan username", color = textSecondary.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth().height(58.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    leadingIcon = { Icon(Icons.Default.Person, null, tint = if (errorMessage != null) StitchNegative else textSecondary.copy(alpha = 0.5f)) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = if (errorMessage != null) StitchNegative.copy(alpha = 0.08f) else if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        unfocusedContainerColor = if (errorMessage != null) StitchNegative.copy(alpha = 0.08f) else if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = if (errorMessage != null) StitchNegative else primaryColor,
                                        focusedTextColor = textPrimary, unfocusedTextColor = textPrimary
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                                )
                            }

                            // Password
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = textSecondary.copy(alpha = 0.7f), letterSpacing = 2.sp)
                                    Text(
                                        text = "Lupa Password?",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = primaryColor,
                                        modifier = Modifier.clickable { showForgotPasswordDialog = true; loginViewModel.resetForgotPasswordState() }
                                    )
                                }
                                TextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = { Text("••••••••", color = textSecondary.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth().height(58.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = if (errorMessage != null) StitchNegative else textSecondary.copy(alpha = 0.5f)) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = textSecondary.copy(alpha = 0.5f))
                                        }
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = if (errorMessage != null) StitchNegative.copy(alpha = 0.08f) else if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        unfocusedContainerColor = if (errorMessage != null) StitchNegative.copy(alpha = 0.08f) else if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = if (errorMessage != null) StitchNegative else primaryColor,
                                        focusedTextColor = textPrimary, unfocusedTextColor = textPrimary
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); loginViewModel.login(username, password) })
                                )
                            }

                            // Remember Me
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 4.dp).clickable { rememberMe = !rememberMe },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = primaryColor, uncheckedColor = textSecondary, checkmarkColor = Color.White),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Ingat Saya", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                errorMessage = null
                                                loginViewModel.resetLoginState()
                                                showForgotPasswordDialog = true
                                                loginViewModel.resetForgotPasswordState()
                                            }
                                        ) {
                                            Text("Lupa Password?", color = primaryColor, fontWeight = FontWeight.Bold)
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

                            // Login Button - Gradient
                            Button(
                                onClick = { focusManager.clearFocus(); loginViewModel.login(username, password) },
                                modifier = Modifier.fillMaxWidth().height(58.dp),
                                enabled = loginUiState !is LoginUiState.Loading,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(Brush.horizontalGradient(listOf(primaryColor, Color(0xFF059669))), RoundedCornerShape(24.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (loginUiState is LoginUiState.Loading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Masuk Sekarang", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                            Spacer(Modifier.width(8.dp))
                                            Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }

                            // Social Divider
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = glassBorderColor)
                                Text(
                                    text = "ATAU",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textSecondary.copy(alpha = 0.6f)
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = glassBorderColor)
                            }

                            // Google Login Button
                            OutlinedButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, glassBorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = textPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Masuk dengan Google",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Footer - inside card
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("Belum punya akun? ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                                Text(
                                    text = "Daftar Sini",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryColor,
                                    modifier = Modifier.clickable { onNavigateToRegister() }
                                )
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
                            Column(verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)) {
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

                                HorizontalDivider(color = glassBorderColor)

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
                                text = if (isApproved) "Permintaan Disetujui!" else "Permintaan Ditolak",
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

                if (showForgotPasswordDialog) {
                    ForgotPasswordDialog(
                        uiState = forgotPasswordUiState,
                        onDismiss = { 
                            showForgotPasswordDialog = false
                            loginViewModel.resetForgotPasswordState()
                        },
                        onSendOtp = { u, e -> loginViewModel.sendOtp(u, e) },
                        onVerifyOtp = { u, e, o -> loginViewModel.verifyOtp(u, e, o) },
                        onResetPassword = { u, e, o, p -> loginViewModel.resetPassword(u, e, o, p) },
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    uiState: com.example.kassaku.viewmodel.ForgotPasswordState,
    onDismiss: () -> Unit,
    onSendOtp: (String, String) -> Unit,
    onVerifyOtp: (String, String, String) -> Unit,
    onResetPassword: (String, String, String, String) -> Unit,
    isDark: Boolean
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val textPrimary = if (isDark) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pemulihan Akun",
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState) {
                    is com.example.kassaku.viewmodel.ForgotPasswordState.Idle, 
                    is com.example.kassaku.viewmodel.ForgotPasswordState.Loading,
                    is com.example.kassaku.viewmodel.ForgotPasswordState.PasswordResetError -> {
                        
                        if (uiState is com.example.kassaku.viewmodel.ForgotPasswordState.Idle || 
                            uiState is com.example.kassaku.viewmodel.ForgotPasswordState.Loading ||
                            (uiState is com.example.kassaku.viewmodel.ForgotPasswordState.PasswordResetError && otp.isEmpty())) {
                            
                            Text(
                                text = "Masukkan username dan email yang sama seperti saat registrasi.",
                                fontSize = 14.sp,
                                color = textSecondary
                            )

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                enabled = uiState !is com.example.kassaku.viewmodel.ForgotPasswordState.Loading
                            )
                            
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                enabled = uiState !is com.example.kassaku.viewmodel.ForgotPasswordState.Loading
                            )
                        } else if (otp.isNotEmpty() || uiState is com.example.kassaku.viewmodel.ForgotPasswordState.OtpSent) {
                             // This part is handled by the next case, but let's keep it safe
                        }
                    }
                    
                    is com.example.kassaku.viewmodel.ForgotPasswordState.OtpSent -> {
                        Text(
                            text = "Kode OTP telah dikirim ke ${uiState.email}. Periksa kotak masuk Anda.",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                        
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { if (it.length <= 6) otp = it },
                            label = { Text("Kode OTP (6 Digit)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                        )
                    }

                    is com.example.kassaku.viewmodel.ForgotPasswordState.OtpVerified -> {
                        Text(
                            text = "OTP Berhasil diverifikasi. Silakan masukkan password baru Anda.",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                        
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Password Baru") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = textSecondary
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Konfirmasi Password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                        )
                    }

                    is com.example.kassaku.viewmodel.ForgotPasswordState.PasswordResetSuccess -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = uiState.message,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                    }
                }

                if (uiState is com.example.kassaku.viewmodel.ForgotPasswordState.PasswordResetError) {
                    Text(
                        text = uiState.message,
                        color = StitchNegative,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            when (uiState) {
                is com.example.kassaku.viewmodel.ForgotPasswordState.Idle, 
                is com.example.kassaku.viewmodel.ForgotPasswordState.PasswordResetError -> {
                    if (otp.isEmpty() && uiState !is com.example.kassaku.viewmodel.ForgotPasswordState.OtpSent) {
                        Button(
                            onClick = { onSendOtp(username.trim(), email.trim()) },
                            enabled = username.trim().length >= 2 && email.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Kirim OTP")
                        }
                    } else if (otp.isNotEmpty() || uiState is com.example.kassaku.viewmodel.ForgotPasswordState.OtpSent) {
                        // Handled by OtpSent case below
                    }
                }
                is com.example.kassaku.viewmodel.ForgotPasswordState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                    is com.example.kassaku.viewmodel.ForgotPasswordState.OtpSent -> {
                        Button(
                        onClick = { onVerifyOtp(uiState.username, uiState.email, otp) },
                        enabled = otp.length == 6,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verifikasi OTP")
                    }
                }
                    is com.example.kassaku.viewmodel.ForgotPasswordState.OtpVerified -> {
                    Button(
                        onClick = { onResetPassword(uiState.username, uiState.email, uiState.otp, newPassword) },
                        enabled = newPassword.length >= 8 && newPassword == confirmPassword,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset Password")
                    }
                }
                is com.example.kassaku.viewmodel.ForgotPasswordState.PasswordResetSuccess -> {
                    Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text("Selesai")
                    }
                }
            }
        },
        dismissButton = {
            if (uiState !is com.example.kassaku.viewmodel.ForgotPasswordState.PasswordResetSuccess) {
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = textSecondary)
                }
            }
        },
        containerColor = if (isDark) Color(0xFF1E1E2D) else Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = { _, _ -> }, onNavigateToRegister = {}, onNavigateToForgotPassword = {}, onSyncFCM = {})
}
