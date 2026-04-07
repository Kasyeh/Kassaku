package com.example.kassaku.data.repository

import com.example.kassaku.data.remote.ApiService
import com.example.kassaku.data.remote.model.ApiValidationErrorResponse
import com.example.kassaku.data.remote.model.BlockedContent
import com.example.kassaku.data.remote.model.LoginResponse
import com.example.kassaku.data.remote.model.UnblockRequestResponse
import com.example.kassaku.data.remote.model.UserContent
import com.google.gson.Gson
import com.google.gson.JsonParser
import retrofit2.Response

class BlockedException(
    val idUser: Int,
    override val message: String,
    val pendingUnblock: Boolean,
    val rejectedUnblock: Boolean,
    val rejectedMessage: String?
) : Exception(message)

/**
 * Repository untuk mengelola autentikasi user
 */
class AuthRepository(private val apiService: ApiService) {

    /**
     * Login user dengan username dan password
     * @return Result dengan UserContent jika sukses, atau pesan error jika gagal
     */
    suspend fun login(username: String, password: String): Result<UserContent> {
        return try {
            val response: Response<LoginResponse> = apiService.login(username, password)
            val gson = Gson()

            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null && loginResponse.responseCode == 200 && loginResponse.content != null) {
                    val userContent = gson.fromJson(loginResponse.content, UserContent::class.java)
                    // Set token di ApiClient untuk request berikutnya
                    userContent.token?.let { com.example.kassaku.data.remote.ApiClient.setToken(it) }
                    Result.success(userContent)
                } else {
                    Result.failure(Exception(loginResponse?.message ?: "Login gagal: Respons tidak valid"))
                }
            } else if (response.code() == 403) {
                val errorBody = response.errorBody()?.string()
                val loginResponse = gson.fromJson(errorBody, LoginResponse::class.java)
                if (loginResponse != null && loginResponse.content != null && !loginResponse.content.isJsonNull) {
                    val blockedContent = gson.fromJson(loginResponse.content, BlockedContent::class.java)
                    Result.failure(
                        BlockedException(
                            blockedContent.idUser,
                            loginResponse.message,
                            blockedContent.pendingUnblock,
                            blockedContent.rejectedUnblock,
                            blockedContent.rejectedMessage
                        )
                    )
                } else {
                    Result.failure(Exception(loginResponse?.message ?: "Akses ditolak (403)"))
                }
            } else {
                Result.failure(Exception("Login gagal dengan kode: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengirim permintaan unblock
     */
    suspend fun submitUnblockRequest(username: String, password: String, message: String, fcmToken: String? = null): Result<UnblockRequestResponse> {
        return try {
            val response = apiService.submitUnblockRequest(username, password, message, fcmToken)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal mengirim permintaan unblock"))
                }
            } else {
                val rawError = response.errorBody()?.string().orEmpty()
                val userMessage = parseApiMessage(rawError)
                    ?: when (response.code()) {
                        400 -> "Permintaan unblock Anda masih sedang diproses admin."
                        401 -> "Kredensial tidak valid. Silakan login ulang."
                        422 -> "Data permintaan unblock belum lengkap."
                        else -> "Gagal mengirim permintaan unblock. Silakan coba lagi."
                    }
                Result.failure(Exception(userMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseApiMessage(rawBody: String): String? {
        if (rawBody.isBlank()) {
            return null
        }

        return try {
            val jsonObject = JsonParser.parseString(rawBody).asJsonObject
            jsonObject.get("message")?.asString
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Register user baru
     */
    suspend fun register(username: String, password: String): Result<UserContent> {
        return try {
            val response = apiService.register(username, password)

            if (response.isSuccessful) {
                val registerResponse = response.body()
                if (registerResponse != null && registerResponse.success && registerResponse.data != null) {
                    // Set token di ApiClient untuk request berikutnya
                    registerResponse.data.token?.let { com.example.kassaku.data.remote.ApiClient.setToken(it) }
                    Result.success(registerResponse.data)
                } else {
                    Result.failure(Exception(registerResponse?.message ?: "Registrasi gagal. Coba lagi."))
                }
            } else {
                val code = response.code()
                val errorBody = response.errorBody()?.string().orEmpty()
                val userMessage = when {
                    code == 422 -> parseValidationMessage(errorBody)
                    code >= 500 -> "Server sedang bermasalah. Coba beberapa saat lagi."
                    else -> "Registrasi gagal. Coba lagi."
                }
                Result.failure(Exception(userMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Registrasi gagal. Coba lagi."))
        }
    }

    private fun parseValidationMessage(errorBody: String): String {
        return try {
            val parsed = Gson().fromJson(errorBody, ApiValidationErrorResponse::class.java)
            parsed.errors?.get("username")?.firstOrNull()
                ?: parsed.errors?.get("password")?.firstOrNull()
                ?: parsed.message
                ?: "Registrasi gagal. Coba lagi."
        } catch (e: Exception) {
            "Registrasi gagal. Coba lagi."
        }
    }
}
