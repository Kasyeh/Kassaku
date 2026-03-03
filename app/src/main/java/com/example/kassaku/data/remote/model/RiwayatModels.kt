package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

// Response dengan pagination wrapper
data class RiwayatResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("dataPage") val dataPage: RiwayatPageData?  // FIXED: Backend pakai "dataPage" bukan "data"
)

// Response alternatif jika API mengembalikan array langsung
data class RiwayatResponseDirect(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val riwayatItems: List<RiwayatItem>?
)

data class RiwayatPageData(
    @SerializedName("riwayatItems") val riwayatItems: List<RiwayatItem>?,  // FIXED: Backend return "riwayatItems" langsung
    // Fields pagination opsional (jika backend nanti pakai pagination)
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("first_page_url") val firstPageUrl: String? = null,
    @SerializedName("from") val from: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("last_page_url") val lastPageUrl: String? = null,
    @SerializedName("links") val links: List<LinkItem>? = null,
    @SerializedName("next_page_url") val nextPageUrl: String? = null,
    @SerializedName("path") val path: String? = null,
    @SerializedName("per_page") val perPage: Int? = null,
    @SerializedName("prev_page_url") val prevPageUrl: String? = null,
    @SerializedName("to") val to: Int? = null,
    @SerializedName("total") val total: Int? = null
)

data class RiwayatItem(
    @SerializedName("id_transaksi") val idTransaksi: Long,
    @SerializedName("id_user") val idUser: Long,
    @SerializedName("tipe") val tipe: String?,
    @SerializedName("nominal") val nominal: Double?,
    @SerializedName("kategori") val kategori: String?,
    @SerializedName("tanggal") val tanggal: String?,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class LinkItem(
    @SerializedName("url") val url: String?,
    @SerializedName("label") val label: String?,
    @SerializedName("active") val active: Boolean?
)
