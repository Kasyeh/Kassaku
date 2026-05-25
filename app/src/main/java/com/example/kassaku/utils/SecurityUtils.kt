package com.example.kassaku.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.util.Log
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Utility untuk menangani autentikasi biometrik (Fingerprint/Face ID)
 */
object SecurityUtils {

    /**
     * Cek apakah perangkat mendukung dan sudah mengaktifkan biometrik
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Menampilkan prompt biometrik
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Keamanan KasSaku",
        subtitle: String = "Gunakan kunci perangkat untuk masuk",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Biasanya tidak perlu aksi khusus di sini karena prompt akan tetap muncul
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("SecurityUtils", "Failed to authenticate: ${e.message}")
            onError("Gagal memulai autentikasi: ${e.message}")
        }
    }

    /**
     * Helper untuk mendapatkan FragmentActivity dari Context (menangani ContextWrapper)
     */
    fun getActivity(context: Context): FragmentActivity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is FragmentActivity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Helper to create MultipartBody.Part from Uri
     */
    fun createMultipartFromUri(context: Context, uri: Uri, partName: String): okhttp3.MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/jpg", "image/jpeg" -> "jpg"
                else -> "jpg"
            }
            
            val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            
            okhttp3.MultipartBody.Part.createFormData(
                partName,
                "image_${System.currentTimeMillis()}.$extension",
                requestFile
            )
        } catch (e: Exception) {
            Log.e("SecurityUtils", "Error creating multipart: ${e.message}", e)
            null
        }
    }
}
