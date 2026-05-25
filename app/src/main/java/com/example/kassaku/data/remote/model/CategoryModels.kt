package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<CategoryItem>
)

data class CategoryItem(
    @SerializedName("id_kategori") val id: Int,
    @SerializedName("nama_kategori") val name: String,
    @SerializedName("tipe") val type: String, // pemasukan / pengeluaran
    @SerializedName("icon") val icon: String?,
    @SerializedName("is_default") val isDefault: Boolean
)
