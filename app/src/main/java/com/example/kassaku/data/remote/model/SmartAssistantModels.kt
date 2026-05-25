package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class NudgeResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<NudgeItem>
)

data class NudgeItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String, // success, warning, info
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("action_label") val actionLabel: String,
    @SerializedName("action_type") val actionType: String // NAVIGATE_IMPIAN, NAVIGATE_TABUNGAN, dll
)

data class ChatbotResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ChatbotMessage
)

data class ChatbotMessage(
    @SerializedName("type") val type: String, // bot or user
    @SerializedName("text") val text: String
)
