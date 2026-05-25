package com.example.kassaku.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kassaku.data.remote.ApiClient
import com.example.kassaku.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed interface ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState
    object Loading : ForgotPasswordUiState
    data class OtpSent(val username: String, val email: String) : ForgotPasswordUiState
    object Success : ForgotPasswordUiState
    data class Error(val message: String) : ForgotPasswordUiState
}

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository(ApiClient.api)
) : ViewModel() {

    var uiState: ForgotPasswordUiState by mutableStateOf(ForgotPasswordUiState.Idle)
        private set

    fun sendOtp(username: String, email: String) {
        if (username.isBlank() || username.length < 2) {
            uiState = ForgotPasswordUiState.Error("Username tidak valid")
            return
        }
        if (email.isBlank()) {
            uiState = ForgotPasswordUiState.Error("Email tidak boleh kosong")
            return
        }

        uiState = ForgotPasswordUiState.Loading
        viewModelScope.launch {
            authRepository.sendOtp(username, email).fold(
                onSuccess = { uiState = ForgotPasswordUiState.OtpSent(username, email) },
                onFailure = { uiState = ForgotPasswordUiState.Error(it.message ?: "Gagal mengirim OTP") }
            )
        }
    }

    fun resetPassword(username: String, email: String, otp: String, password: String) {
        if (otp.length != 6) {
            uiState = ForgotPasswordUiState.Error("OTP harus 6 digit")
            return
        }
        if (password.length < 8) {
            uiState = ForgotPasswordUiState.Error("Password minimal 8 karakter")
            return
        }

        uiState = ForgotPasswordUiState.Loading
        viewModelScope.launch {
            authRepository.resetPassword(username, email, otp, password).fold(
                onSuccess = { uiState = ForgotPasswordUiState.Success },
                onFailure = { uiState = ForgotPasswordUiState.Error(it.message ?: "Gagal mereset password") }
            )
        }
    }

    fun resetState() {
        uiState = ForgotPasswordUiState.Idle
    }
}
