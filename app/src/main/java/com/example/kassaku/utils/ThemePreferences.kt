package com.example.kassaku.utils

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class ThemePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString("theme_mode", mode.name).apply()
    }

    fun getThemeMode(): ThemeMode {
        val modeName = sharedPreferences.getString("theme_mode", ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeName ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dynamic_color", enabled).apply()
    }

    fun isDynamicColorEnabled(): Boolean {
        return sharedPreferences.getBoolean("dynamic_color", false)
    }
}
