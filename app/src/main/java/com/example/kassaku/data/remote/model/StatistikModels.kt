package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class StatistikResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StatistikData?
)

data class StatistikData(
    @SerializedName("labels") val labels: List<String>,
    @SerializedName("pemasukan") val pemasukan: List<Double>,
    @SerializedName("pengeluaran") val pengeluaran: List<Double>
)
