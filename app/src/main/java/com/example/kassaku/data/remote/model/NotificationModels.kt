package com.example.kassaku.data.remote.model

data class NotificationInboxResponse(
    val success: Boolean,
    val data: NotificationInboxData?
)

data class NotificationInboxData(
    val items: List<NotificationItemDto> = emptyList(),
    val unread_count: Int = 0
)

data class NotificationItemDto(
    val id: Long,
    val category: String,
    val title: String,
    val body: String,
    val sent_at: String?,
    val sent_at_human: String?,
    val read: Boolean,
    val accent: String,
    val icon: String,
    val excerpt: String?
)

data class BasicMessageResponse(
    val success: Boolean,
    val message: String?
)

data class ReminderPreferenceResponse(
    val success: Boolean,
    val data: ReminderPreferenceDto?
)

data class ReminderPreferenceDto(
    val reminders_enabled: Boolean,
    val daily_reminder_enabled: Boolean,
    val daily_reminder_hour: Int,
    val budget_alert_enabled: Boolean,
    val budget_alert_threshold: Int,
    val dream_reminder_enabled: Boolean,
    val dream_inactive_days: Int
)

data class ReminderPreferenceRequest(
    val reminders_enabled: Boolean,
    val daily_reminder_enabled: Boolean,
    val daily_reminder_hour: Int,
    val budget_alert_enabled: Boolean,
    val budget_alert_threshold: Int,
    val dream_reminder_enabled: Boolean,
    val dream_inactive_days: Int
)
