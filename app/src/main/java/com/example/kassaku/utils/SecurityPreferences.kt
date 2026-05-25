package com.example.kassaku.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Menyimpan preferensi keamanan aplikasi
 */
class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
    }

    /**
     * Cek apakah penguncian aplikasi aktif
     */
    fun isAppLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
    }

    /**
     * Mengatur status penguncian aplikasi
     */
    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }
}
