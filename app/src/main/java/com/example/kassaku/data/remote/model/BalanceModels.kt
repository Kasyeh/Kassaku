package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class BalanceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: BalanceData?
)

data class BalanceData(
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("currency") val currency: String = "IDR",
    @SerializedName("currency_format") val currencyFormat: String = "standard",
    @SerializedName("saldo") val saldo: String,
    @SerializedName("pemasukan") val pemasukan: String,
    @SerializedName("pengeluaran") val pengeluaran: String,
    @SerializedName("target_pengeluaran") val targetPengeluaran: String? = null,
    @SerializedName("is_over_budget") val isOverBudget: Boolean = false
)

// Response untuk simpan target pengeluaran
data class TargetPengeluaranResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: TargetPengeluaranData?
)

data class TargetPengeluaranData(
    @SerializedName("target_pengeluaran") val targetPengeluaran: String?,
    @SerializedName("pengeluaran_bulan_ini") val pengeluaranBulanIni: String,
    @SerializedName("is_over_budget") val isOverBudget: Boolean
)
