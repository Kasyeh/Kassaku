package com.example.kassaku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kassaku.data.remote.ApiClient
import com.example.kassaku.data.remote.model.ReminderPreferenceDto
import com.example.kassaku.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReminderSettingsUiState {
    object Idle : ReminderSettingsUiState()
    object Loading : ReminderSettingsUiState()
    data class Success(val preferences: ReminderPreferenceDto) : ReminderSettingsUiState()
    data class Error(val message: String) : ReminderSettingsUiState()
}

sealed class SavePreferencesResult {
    object Idle : SavePreferencesResult()
    object Loading : SavePreferencesResult()
    data class Success(val message: String) : SavePreferencesResult()
    data class Error(val message: String) : SavePreferencesResult()
}

class ReminderSettingsViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepository(ApiClient.api)
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReminderSettingsUiState>(ReminderSettingsUiState.Idle)
    val uiState: StateFlow<ReminderSettingsUiState> = _uiState.asStateFlow()

    private val _saveResult = MutableStateFlow<SavePreferencesResult>(SavePreferencesResult.Idle)
    val saveResult: StateFlow<SavePreferencesResult> = _saveResult.asStateFlow()

    // State for the form
    var remindersEnabled = MutableStateFlow(true)
    var dailyReminderEnabled = MutableStateFlow(true)
    var dailyReminderHour = MutableStateFlow("20")
    var budgetAlertEnabled = MutableStateFlow(true)
    var budgetAlertThreshold = MutableStateFlow("80")
    var dreamReminderEnabled = MutableStateFlow(true)
    var dreamInactiveDays = MutableStateFlow("7")

    fun loadPreferences() {
        viewModelScope.launch {
            _uiState.value = ReminderSettingsUiState.Loading
            notificationRepository.getReminderPreferences().fold(
                onSuccess = { prefs ->
                    _uiState.value = ReminderSettingsUiState.Success(prefs)
                    
                    // Update form state
                    remindersEnabled.value = prefs.reminders_enabled
                    dailyReminderEnabled.value = prefs.daily_reminder_enabled
                    dailyReminderHour.value = prefs.daily_reminder_hour.toString()
                    budgetAlertEnabled.value = prefs.budget_alert_enabled
                    budgetAlertThreshold.value = prefs.budget_alert_threshold.toString()
                    dreamReminderEnabled.value = prefs.dream_reminder_enabled
                    dreamInactiveDays.value = prefs.dream_inactive_days.toString()
                },
                onFailure = { e ->
                    _uiState.value = ReminderSettingsUiState.Error(e.message ?: "Gagal memuat preferensi")
                }
            )
        }
    }

    fun savePreferences() {
        viewModelScope.launch {
            _saveResult.value = SavePreferencesResult.Loading
            
            val hour = dailyReminderHour.value.toIntOrNull() ?: 20
            val threshold = budgetAlertThreshold.value.toIntOrNull() ?: 80
            val inactiveDays = dreamInactiveDays.value.toIntOrNull() ?: 7

            notificationRepository.saveReminderPreferences(
                remindersEnabled.value,
                dailyReminderEnabled.value,
                hour,
                budgetAlertEnabled.value,
                threshold,
                dreamReminderEnabled.value,
                inactiveDays
            ).fold(
                onSuccess = {
                    _saveResult.value = SavePreferencesResult.Success("Preferensi berhasil disimpan")
                },
                onFailure = { e ->
                    _saveResult.value = SavePreferencesResult.Error(e.message ?: "Gagal menyimpan preferensi")
                }
            )
        }
    }

    fun resetSaveResult() {
        _saveResult.value = SavePreferencesResult.Idle
    }
}
