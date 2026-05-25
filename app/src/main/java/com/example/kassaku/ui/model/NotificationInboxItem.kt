package com.example.kassaku.ui.model

enum class NotificationAccent {
    EMERALD,
    AMBER,
    ROSE,
    SKY,
    VIOLET,
    SLATE
}

data class NotificationInboxItem(
    val id: Long,
    val category: String,
    val title: String,
    val body: String,
    val excerpt: String,
    val sentAtHuman: String,
    val isRead: Boolean,
    val accent: NotificationAccent,
    val iconKey: String
)
