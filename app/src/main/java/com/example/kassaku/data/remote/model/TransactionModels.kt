package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class PemasukanResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PemasukanData?
)

data class PemasukanData(
    @SerializedName("id_user") val idUser: String?,
    @SerializedName("nominal") val nominal: String?,
    @SerializedName("kategori") val kategori: String?,
    @SerializedName("keterangan") val keterangan: String?
)

data class PengeluaranResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PengeluaranData?
)

data class PengeluaranData(
    @SerializedName("id_user") val idUser: String?,
    @SerializedName("nominal") val nominal: String?,
    @SerializedName("kategori") val kategori: String?,
    @SerializedName("keterangan") val keterangan: String?
)
