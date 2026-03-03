package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class UnblockRequestResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("error") val error: String? = null
)
