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
import com.example.kassaku.data.repository.RealtimeDatabaseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val pendingUnblock: Boolean,
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
    private val authRepository: AuthRepository = AuthRepository(ApiClient.api),
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository = RealtimeDatabaseRepository()
) : ViewModel() {
    private var lastLoginUsername: String? = null
    private var lastLoginPassword: String? = null
    
    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set

    var unblockUiState: UnblockUiState by mutableStateOf(UnblockUiState.Idle)
        private set

    // Realtime unblock response from admin via RTDB
    private val _unblockResponseState = MutableStateFlow<RealtimeDatabaseRepository.UnblockResponseData?>(null)
    val unblockResponseState: StateFlow<RealtimeDatabaseRepository.UnblockResponseData?> = _unblockResponseState.asStateFlow()

    private var unblockResponseJob: Job? = null

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            loginUiState = LoginUiState.Error("Username dan Password tidak boleh kosong")
            return
        }

        loginUiState = LoginUiState.Loading

        viewModelScope.launch {
            try {
                lastLoginUsername = username
                lastLoginPassword = password
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
                                exception.pendingUnblock,
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

        val username = lastLoginUsername
        val password = lastLoginPassword
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            unblockUiState = UnblockUiState.Error("Silakan login ulang sebelum kirim permintaan unblock.")
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

                val result = authRepository.submitUnblockRequest(username, password, pesan, fcmToken)
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

    /**
     * Start listening to RTDB for admin's unblock response
     * Called when blocked dialog is shown and user ID is available
     */
    fun startListeningUnblockResponse(userId: Int) {
        // Cancel any existing listener
        unblockResponseJob?.cancel()
        unblockResponseJob = viewModelScope.launch {
            realtimeDatabaseRepository.getUnblockResponseFlow(userId).collect { response ->
                Log.d("LoginViewModel", "Unblock response update: $response")
                _unblockResponseState.value = response
            }
        }
    }

    /**
     * Stop listening to RTDB and clean up
     */
    fun stopListeningUnblockResponse() {
        unblockResponseJob?.cancel()
        unblockResponseJob = null
    }

    /**
     * Acknowledge the unblock response and clear it from RTDB
     */
    fun acknowledgeUnblockResponse(userId: Int) {
        _unblockResponseState.value = null
        realtimeDatabaseRepository.clearUnblockResponse(userId)
    }

    fun resetUnblockState() {
        unblockUiState = UnblockUiState.Idle
    }

    fun resetLoginState() {
        loginUiState = LoginUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningUnblockResponse()
    }
}
