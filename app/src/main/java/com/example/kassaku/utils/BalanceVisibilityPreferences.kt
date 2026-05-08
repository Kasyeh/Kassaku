package com.example.kassaku.utils

import android.content.Context

class BalanceVisibilityPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("KassakuPrefs", Context.MODE_PRIVATE)

    fun isHomeBalanceVisible(): Boolean {
        return sharedPreferences.getBoolean(KEY_HOME_BALANCE_VISIBLE, true)
    }

    fun setHomeBalanceVisible(isVisible: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_HOME_BALANCE_VISIBLE, isVisible)
            .apply()
    }

    companion object {
        private const val KEY_HOME_BALANCE_VISIBLE = "balance_visibility_home"
    }
}
