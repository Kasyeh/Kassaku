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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
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
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val hasMinLength by remember { derivedStateOf { password.length >= 8 } }
    val hasUppercase by remember { derivedStateOf { password.any { it.isUpperCase() } } }
    val hasDigit by remember { derivedStateOf { password.any { it.isDigit() } } }
    val hasSymbol by remember { derivedStateOf { password.any { !it.isLetterOrDigit() && !it.isWhitespace() } } }
    val hasNoSpace by remember { derivedStateOf { password.isNotEmpty() && !password.any { it.isWhitespace() } } }

    val isPasswordValid by remember { derivedStateOf { hasMinLength && hasUppercase && hasDigit && hasSymbol && hasNoSpace } }
    val isFormValid by remember { derivedStateOf { username.isNotBlank() && isPasswordValid } }
    
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
                            text = "Buat Akun",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary,
                            letterSpacing = (-1).sp
                        )
                        
                        Text(
                            text = "Mulai Kelola Keuanganmu",
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
                                text = "Registrasi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )

                            // Username
                            TextField(
                                value = username,
                                onValueChange = { username = it },
                                placeholder = { Text("Username", color = textSecondary.copy(alpha = 0.6f)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                    unfocusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = primaryColor
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            // Password
                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("Password", color = textSecondary.copy(alpha = 0.6f)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = textSecondary
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                    unfocusedContainerColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = primaryColor
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (isFormValid) {
                                        focusManager.clearFocus()
                                        registerViewModel.register(username, password)
                                    }
                                })
                            )

                            // Password Checklist
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RequirementItem("Minimal 8 karakter", hasMinLength, isDark)
                                RequirementItem("Huruf Kapital", hasUppercase, isDark)
                                RequirementItem("Angka", hasDigit, isDark)
                                RequirementItem("Simbol (!@#\$%^&*_)", hasSymbol, isDark)
                                if (password.any { it.isWhitespace() }) {
                                    RequirementItem("Tidak mengandung spasi", hasNoSpace, isDark)
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

                            // Register Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    registerViewModel.register(username, password)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(12.dp, CircleShape, spotColor = if(isFormValid) primaryColor.copy(alpha = 0.5f) else Color.Transparent),
                                enabled = registerUiState !is RegisterUiState.Loading && isFormValid,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.White,
                                    disabledContainerColor = if(isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                                    disabledContentColor = textSecondary.copy(alpha = 0.5f)
                                )
                            ) {
                                if (registerUiState is RegisterUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Daftar Sekarang", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
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
                            text = "Sudah punya akun? ",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                        Text(
                            text = "Masuk",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
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
