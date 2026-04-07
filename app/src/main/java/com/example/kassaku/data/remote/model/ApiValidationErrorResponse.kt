package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class ApiValidationErrorResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null
)
