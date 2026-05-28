package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class FeedbackResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
