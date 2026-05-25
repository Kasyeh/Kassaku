package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class AvatarResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("avatar_url") val avatarUrl: String?
)
