package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class BudgetKategoriItem(
    @SerializedName("id") val id: Int,
    @SerializedName("kategori") val kategori: String,
    @SerializedName("nominal") val nominal: Double,
    @SerializedName("periode") val periode: String,
    @SerializedName("tanggal_mulai") val tanggalMulai: String?,
    @SerializedName("tanggal_akhir") val tanggalAkhir: String?,
    @SerializedName("spent") val spent: Double?,
    @SerializedName("percentage") val percentage: Double?,
    @SerializedName("over") val over: Boolean?,
    @SerializedName("periode_label") val periodeLabel: String?
)

data class BudgetKategoriResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<BudgetKategoriItem>?
)

data class SimpanBudgetResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: BudgetKategoriItem?
)

data class HapusBudgetResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
