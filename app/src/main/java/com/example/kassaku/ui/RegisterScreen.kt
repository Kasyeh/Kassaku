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
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.kassaku.viewmodel.RegisterUiState
import com.example.kassaku.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (userId: Int) -> Unit,
    onNavigateToLogin: () -> Unit,
    onSyncFCM: (Int?) -> Unit = {},
    registerViewModel: RegisterViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val hasMinLength by remember { derivedStateOf { password.length >= 8 } }
    val hasUppercase by remember { derivedStateOf { password.any { it.isUpperCase() } } }
    val hasDigit by remember { derivedStateOf { password.any { it.isDigit() } } }
    val hasSymbol by remember { derivedStateOf { password.any { !it.isLetterOrDigit() && !it.isWhitespace() } } }
    val hasNoSpace by remember { derivedStateOf { password.isNotEmpty() && !password.any { it.isWhitespace() } } }

    val isEmailValid by remember { derivedStateOf { email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() } }
    val isPasswordValid by remember { derivedStateOf { hasMinLength && hasUppercase && hasDigit && hasSymbol && hasNoSpace } }
    val isFormValid by remember { derivedStateOf { username.isNotBlank() && email.isNotBlank() && isPasswordValid && isEmailValid } }
    
    val registerUiState = registerViewModel.registerUiState
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    // Animation States
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Colors - Glass Style (Consistent with LoginScreen)
    val glassColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f)
    val glassBorderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.4f)
    val textPrimary = if (isDark) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val primaryColor = StitchPrimary

    LaunchedEffect(registerUiState) {
        when (registerUiState) {
            is RegisterUiState.Success -> {
                val user = registerUiState.user
                Toast.makeText(context, "Registrasi berhasil! Selamat datang, ${user.username}", Toast.LENGTH_SHORT).show()
                errorMessage = null
                onRegisterSuccess(user.idUser)
                onSyncFCM(user.idUser)
                registerViewModel.resetRegisterState()
            }
            is RegisterUiState.Error -> {
                errorMessage = registerUiState.message
            }
            RegisterUiState.Idle -> errorMessage = null
            RegisterUiState.Loading -> errorMessage = null
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
                // Header Section
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
                                text = "Buat Akun",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Silakan lengkapi data dirimu",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary
                            )

                            // Username
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    placeholder = { Text("Pilih username", color = textSecondary.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth().height(58.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    leadingIcon = { Icon(Icons.Default.Person, null, tint = textSecondary.copy(alpha = 0.5f)) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        unfocusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        errorContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = Color.Transparent,
                                        cursorColor = primaryColor,
                                        focusedTextColor = textPrimary, unfocusedTextColor = textPrimary,
                                        errorTextColor = textPrimary
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                                )
                            }

                            // Gmail
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "EMAIL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textSecondary.copy(alpha = 0.7f),
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                TextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = { Text("Contoh: budi@gmail.com", color = textSecondary.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth().height(58.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    isError = !isEmailValid && email.isNotEmpty(),
                                    leadingIcon = { Icon(Icons.Default.Email, null, tint = textSecondary.copy(alpha = 0.5f)) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        unfocusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        errorContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = Color.Transparent,
                                        cursorColor = primaryColor,
                                        focusedTextColor = textPrimary, unfocusedTextColor = textPrimary,
                                        errorTextColor = textPrimary
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                                )
                            }

                            // Password
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "PASSWORD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textSecondary.copy(alpha = 0.7f),
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                TextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = { Text("••••••••", color = textSecondary.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth().height(58.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = textSecondary.copy(alpha = 0.5f)) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = textSecondary.copy(alpha = 0.5f))
                                        }
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        unfocusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        errorContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = Color.Transparent,
                                        cursorColor = primaryColor,
                                        focusedTextColor = textPrimary, unfocusedTextColor = textPrimary,
                                        errorTextColor = textPrimary
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        if (isFormValid) {
                                            focusManager.clearFocus()
                                            registerViewModel.register(username, password, email)
                                        }
                                    })
                                )
                            }

                            // Password Strength & Checklist
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap)
                            ) {
                                val strength = listOf(hasMinLength, hasUppercase, hasDigit, hasSymbol).count { it }
                                LinearProgressIndicator(
                                    progress = strength / 4f,
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = when(strength) {
                                        0, 1 -> Color(0xFFEF4444)
                                        2, 3 -> Color(0xFFF59E0B)
                                        else -> Color(0xFF10B981)
                                    },
                                    trackColor = if(isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Box(Modifier.weight(1f)) { RequirementItem("Minimal 8 karakter", hasMinLength, isDark) }
                                        Box(Modifier.weight(1f)) { RequirementItem("Huruf Kapital", hasUppercase, isDark) }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Box(Modifier.weight(1f)) { RequirementItem("Angka", hasDigit, isDark) }
                                        Box(Modifier.weight(1f)) { RequirementItem("Simbol (!@#$)", hasSymbol, isDark) }
                                    }
                                    if (password.any { it.isWhitespace() }) {
                                        RequirementItem("Tidak mengandung spasi", hasNoSpace, isDark)
                                    }
                                }
                            }

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = StitchNegative,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(start = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Register Button - Gradient
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    registerViewModel.register(username, password, email)
                                },
                                modifier = Modifier.fillMaxWidth().height(58.dp),
                                enabled = registerUiState !is RegisterUiState.Loading && isFormValid,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(Brush.horizontalGradient(listOf(primaryColor, Color(0xFF059669))), RoundedCornerShape(24.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (registerUiState is RegisterUiState.Loading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Daftar Sekarang", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                            Spacer(Modifier.width(8.dp))
                                            Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }

                            // Footer - inside card
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("Sudah punya akun? ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                                Text(
                                    text = "Masuk Sini",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryColor,
                                    modifier = Modifier.clickable { onNavigateToLogin() }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean, isDark: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (isMet) androidx.compose.material.icons.Icons.Default.CheckCircle else androidx.compose.material.icons.Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (isMet) Color(0xFF10B981) else (if(isDark) Color(0xFF475569) else Color(0xFFCBD5E1))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) (if(isDark) Color.White else Color(0xFF1E293B)) else (if(isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
            fontWeight = if (isMet) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {}, onSyncFCM = {})
}
