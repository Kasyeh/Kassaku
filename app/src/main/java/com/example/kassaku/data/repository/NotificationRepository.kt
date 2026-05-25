package com.example.kassaku.data.repository

import com.example.kassaku.data.remote.ApiService
import com.example.kassaku.data.remote.model.NotificationItemDto
import com.example.kassaku.ui.model.NotificationAccent
import com.example.kassaku.ui.model.NotificationInboxItem

class NotificationRepository(
    private val apiService: ApiService
) {
    suspend fun getNotifications(): Result<Pair<List<NotificationInboxItem>, Int>> {
        return try {
            val response = apiService.getMyNotifications()
            if (!response.isSuccessful || response.body()?.success != true) {
                return Result.failure(Exception("Gagal memuat notifikasi."))
            }

            val data = response.body()?.data
            val items = data?.items.orEmpty().map(::mapItem)
            val unreadCount = data?.unread_count ?: 0

            Result.success(items to unreadCount)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Gagal memuat notifikasi."))
        }
    }

    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val response = apiService.markAllNotificationsAsRead()
            if (!response.isSuccessful || response.body()?.success != true) {
                return Result.failure(Exception(response.body()?.message ?: "Gagal menandai notifikasi."))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Gagal menandai notifikasi."))
        }
    }

    private fun mapItem(item: NotificationItemDto): NotificationInboxItem {
        return NotificationInboxItem(
            id = item.id,
            category = item.category,
            title = item.title,
            body = item.body,
            excerpt = item.excerpt ?: item.body,
            sentAtHuman = item.sent_at_human ?: "Baru saja",
            isRead = item.read,
            accent = mapAccent(item.accent),
            iconKey = item.icon.ifBlank { "notifications" }
        )
    }

    private fun mapAccent(accent: String?): NotificationAccent {
        return when (accent?.lowercase()) {
            "emerald" -> NotificationAccent.EMERALD
            "amber" -> NotificationAccent.AMBER
            "rose" -> NotificationAccent.ROSE
            "sky" -> NotificationAccent.SKY
            "violet" -> NotificationAccent.VIOLET
            else -> NotificationAccent.SLATE
        }
    }

    suspend fun getReminderPreferences(): Result<com.example.kassaku.data.remote.model.ReminderPreferenceDto> {
        return try {
            val response = apiService.getReminderPreferences()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Data preferensi kosong"))
                }
            } else {
                Result.failure(Exception("Gagal memuat preferensi reminder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveReminderPreferences(
        remindersEnabled: Boolean,
        dailyReminderEnabled: Boolean,
        dailyReminderHour: Int,
        budgetAlertEnabled: Boolean,
        budgetAlertThreshold: Int,
        dreamReminderEnabled: Boolean,
        dreamInactiveDays: Int
    ): Result<com.example.kassaku.data.remote.model.ReminderPreferenceDto> {
        return try {
            val response = apiService.saveReminderPreferences(
                if (remindersEnabled) 1 else 0,
                if (dailyReminderEnabled) 1 else 0,
                dailyReminderHour,
                if (budgetAlertEnabled) 1 else 0,
                budgetAlertThreshold,
                if (dreamReminderEnabled) 1 else 0,
                dreamInactiveDays
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Gagal menyimpan preferensi"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val message = parseErrorMessage(errorBody) ?: "Gagal menyimpan preferensi reminder"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
            obj.get("message")?.asString
        } catch (_: Exception) {
            null
        }
    }
}
