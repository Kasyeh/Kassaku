package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("response_code") val responseCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("content") val content: com.google.gson.JsonElement?
)

data class UserContent(
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String,
    @SerializedName("active") val active: String,
    @SerializedName("token") val token: String? = null
)

data class BlockedContent(
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("rejected_unblock") val rejectedUnblock: Boolean,
    @SerializedName("rejected_message") val rejectedMessage: String?
)

data class RegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserContent?
)
