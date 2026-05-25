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

sealed interface ForgotPasswordState {
    object Idle : ForgotPasswordState
    object Loading : ForgotPasswordState
    data class OtpSent(val username: String, val email: String) : ForgotPasswordState
    data class OtpVerified(val username: String, val email: String, val otp: String) : ForgotPasswordState
    data class PasswordResetSuccess(val message: String) : ForgotPasswordState
    data class PasswordResetError(val message: String) : ForgotPasswordState
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

    var forgotPasswordUiState: ForgotPasswordState by mutableStateOf(ForgotPasswordState.Idle)
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

    fun loginWithGoogle(idToken: String) {
        loginUiState = LoginUiState.Loading
        viewModelScope.launch {
            try {
                // Fetch latest FCM token
                val fcmToken = try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    null
                }

                val result = authRepository.loginWithGoogle(idToken, fcmToken)
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
                            else -> LoginUiState.Error(exception.message ?: "Login Google gagal")
                        }
                    }
                )
            } catch (e: Exception) {
                loginUiState = LoginUiState.Error("Login Google gagal: ${e.message}")
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

    fun sendOtp(username: String, email: String) {
        if (username.isBlank() || username.length < 2) {
            forgotPasswordUiState = ForgotPasswordState.PasswordResetError("Username tidak valid")
            return
        }
        if (email.isBlank()) {
            forgotPasswordUiState = ForgotPasswordState.PasswordResetError("Email tidak boleh kosong")
            return
        }
        forgotPasswordUiState = ForgotPasswordState.Loading
        viewModelScope.launch {
            authRepository.sendOtp(username, email).fold(
                onSuccess = { _: String ->
                    forgotPasswordUiState = ForgotPasswordState.OtpSent(username, email)
                },
                onFailure = { exception: Throwable ->
                    forgotPasswordUiState = ForgotPasswordState.PasswordResetError(exception.message ?: "Gagal mengirim OTP")
                }
            )
        }
    }

    fun verifyOtp(username: String, email: String, otp: String) {
        if (otp.length != 6) {
            forgotPasswordUiState = ForgotPasswordState.PasswordResetError("Kode OTP harus 6 digit")
            return
        }
        // Actually the backend verify is combined with reset, but we can simulate a step or just call reset.
        // For now, we move to the next UI step.
        forgotPasswordUiState = ForgotPasswordState.OtpVerified(username, email, otp)
    }

    fun resetPassword(username: String, email: String, otp: String, newPassword: String) {
        if (newPassword.length < 8) {
            forgotPasswordUiState = ForgotPasswordState.PasswordResetError("Password minimal 8 karakter")
            return
        }
        forgotPasswordUiState = ForgotPasswordState.Loading
        viewModelScope.launch {
            authRepository.resetPassword(username, email, otp, newPassword).fold(
                onSuccess = { msg: String ->
                    forgotPasswordUiState = ForgotPasswordState.PasswordResetSuccess(msg)
                },
                onFailure = { exception: Throwable ->
                    forgotPasswordUiState = ForgotPasswordState.PasswordResetError(exception.message ?: "Gagal mereset password")
                }
            )
        }
    }

    fun resetForgotPasswordState() {
        forgotPasswordUiState = ForgotPasswordState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningUnblockResponse()
    }
}
