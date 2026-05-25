package com.example.kassaku.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kassaku.ui.components.AuroraBackground
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.ForgotPasswordUiState
import com.example.kassaku.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = LocalIsDark.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Glass Styles
    val glassColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f)
    val glassBorderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.4f)
    val textPrimary = if (isDark) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    LaunchedEffect(uiState) {
        if (uiState is ForgotPasswordUiState.Success) {
            Toast.makeText(context, "Password berhasil diubah!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    AuroraBackground(isDark = isDark) {
        Box(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Back Button & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(glassColor)
                            .border(1.dp, glassBorderColor, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Icon Header
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(20.dp, CircleShape, spotColor = StitchPrimary.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .background(glassColor)
                        .border(1.dp, glassBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = StitchPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Lupa Password",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary
                )
                
                Text(
                    text = when (uiState) {
                        is ForgotPasswordUiState.OtpSent -> "Masukkan kode OTP yang dikirim ke\n${uiState.email}"
                        else -> "Masukkan username dan email terdaftar untuk menerima kode verifikasi"
                    },
                    fontSize = 14.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Main Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, glassBorderColor, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    color = glassColor
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(KassakuSpacing.cardGap)
                    ) {
                        if (uiState !is ForgotPasswordUiState.OtpSent) {
                            TextField(
                                value = username,
                                onValueChange = { username = it },
                                placeholder = { Text("Username", color = textSecondary.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = textSecondary) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = StitchPrimary,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            TextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("Email Terdaftar", color = textSecondary.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Rounded.Email, null, tint = textSecondary) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = StitchPrimary,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { viewModel.sendOtp(username.trim(), email.trim()) })
                            )

                            Button(
                                onClick = { viewModel.sendOtp(username.trim(), email.trim()) },
                                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(12.dp, CircleShape, spotColor = StitchPrimary.copy(alpha = 0.4f)),
                                shape = CircleShape,
                                enabled = uiState !is ForgotPasswordUiState.Loading
                                    && username.trim().length >= 2
                                    && email.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
                            ) {
                                if (uiState is ForgotPasswordUiState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("Kirim Kode OTP", fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        } else {
                            // Step 2: OTP & New Password
                            TextField(
                                value = otp,
                                onValueChange = { if(it.length <= 6) otp = it },
                                placeholder = { Text("Kode OTP (6 Digit)", color = textSecondary.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Rounded.Pin, null, tint = textSecondary) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = StitchPrimary,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            TextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                placeholder = { Text("Password Baru", color = textSecondary.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = textSecondary) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if(passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = textSecondary)
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = StitchPrimary,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    val st = uiState
                                    if (st is ForgotPasswordUiState.OtpSent) {
                                        viewModel.resetPassword(st.username, st.email, otp, newPassword)
                                    }
                                })
                            )

                            Button(
                                onClick = {
                                    val st = uiState
                                    if (st is ForgotPasswordUiState.OtpSent) {
                                        viewModel.resetPassword(st.username, st.email, otp, newPassword)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(12.dp, CircleShape, spotColor = StitchPrimary.copy(alpha = 0.4f)),
                                shape = CircleShape,
                                enabled = uiState !is ForgotPasswordUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
                            ) {
                                if (uiState is ForgotPasswordUiState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("Reset Password", fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            TextButton(
                                onClick = { viewModel.resetState() },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Ganti Email", color = StitchPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (uiState is ForgotPasswordUiState.Error) {
                            Text(
                                text = uiState.message,
                                color = StitchNegative,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
