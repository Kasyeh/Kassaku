package com.example.kassaku.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kassaku.data.remote.ApiClient
import com.example.kassaku.data.remote.model.UserContent
import com.example.kassaku.data.repository.AuthRepository
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface RegisterUiState {
    object Idle : RegisterUiState
    object Loading : RegisterUiState
    data class Success(val user: UserContent) : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository(ApiClient.api)
) : ViewModel() {

    var registerUiState: RegisterUiState by mutableStateOf(RegisterUiState.Idle)
        private set

    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            registerUiState = RegisterUiState.Error("Semua field harus diisi")
            return
        }

        if (password.length < 8) {
            registerUiState = RegisterUiState.Error("Password minimal 8 karakter")
            return
        }

        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>])(?!.*\\s).{8,}\$")
        if (!passwordRegex.matches(password)) {
            registerUiState = RegisterUiState.Error("Password harus mengandung huruf kapital, angka, simbol, dan tidak mengandung spasi.")
            return
        }

        registerUiState = RegisterUiState.Loading

        viewModelScope.launch {
            try {
                val result = authRepository.register(username, password)

                result.fold(
                    onSuccess = { user ->
                        registerUiState = RegisterUiState.Success(user)
                    },
                    onFailure = { exception ->
                        registerUiState = when (exception) {
                            is IOException -> RegisterUiState.Error("Koneksi gagal: ${exception.message ?: "Periksa koneksi internet Anda."}")
                            else -> RegisterUiState.Error(exception.message ?: "Terjadi kesalahan saat registrasi")
                        }
                    }
                )
            } catch (_: Exception) {
                registerUiState = RegisterUiState.Error("Terjadi kesalahan saat registrasi")
            }
        }
    }

    fun resetRegisterState() {
        registerUiState = RegisterUiState.Idle
    }
}
