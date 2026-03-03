package com.example.kassaku.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kassaku.data.remote.ApiClient
import com.example.kassaku.data.remote.model.UserContent
import com.example.kassaku.data.repository.AuthRepository
import com.example.kassaku.data.repository.BlockedException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val user: UserContent) : LoginUiState
    data class Error(val message: String) : LoginUiState
    data class Blocked(
        val idUser: Int,
        val message: String,
        val rejectedUnblock: Boolean,
        val rejectedMessage: String?
    ) : LoginUiState
}

sealed interface UnblockUiState {
    object Idle : UnblockUiState
    object Loading : UnblockUiState
    data class Success(val message: String) : UnblockUiState
    data class Error(val message: String) : UnblockUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository(ApiClient.api)
) : ViewModel() {
    
    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set

    var unblockUiState: UnblockUiState by mutableStateOf(UnblockUiState.Idle)
        private set

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            loginUiState = LoginUiState.Error("Username dan Password tidak boleh kosong")
            return
        }

        loginUiState = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val result = authRepository.login(username, password)
                
                result.fold(
                    onSuccess = { user ->
                        loginUiState = LoginUiState.Success(user)
                    },
                    onFailure = { exception ->
                        loginUiState = when (exception) {
                            is BlockedException -> LoginUiState.Blocked(
                                exception.idUser,
                                exception.message,
                                exception.rejectedUnblock,
                                exception.rejectedMessage
                            )
                            is IOException -> LoginUiState.Error("Koneksi gagal: ${exception.message ?: "Periksa koneksi internet Anda."}")
                            else -> LoginUiState.Error(exception.message ?: "Terjadi kesalahan")
                        }
                    }
                )
            } catch (e: Exception) {
                loginUiState = LoginUiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun submitUnblockRequest(idUser: Int, pesan: String) {
        if (pesan.isBlank()) {
            unblockUiState = UnblockUiState.Error("Pesan tidak boleh kosong")
            return
        }

        unblockUiState = UnblockUiState.Loading

        viewModelScope.launch {
            try {
                // Fetch latest FCM token
                val fcmToken = try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Failed to get FCM token: ${e.message}")
                    null
                }

                val result = authRepository.submitUnblockRequest(idUser, pesan, fcmToken)
                result.fold(
                    onSuccess = { response ->
                        unblockUiState = UnblockUiState.Success(response.message)
                    },
                    onFailure = { exception ->
                        unblockUiState = UnblockUiState.Error(exception.message ?: "Gagal mengirim permintaan")
                    }
                )
            } catch (e: Exception) {
                unblockUiState = UnblockUiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun resetUnblockState() {
        unblockUiState = UnblockUiState.Idle
    }

    fun resetLoginState() {
        loginUiState = LoginUiState.Idle
    }
}
