package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

// Response dengan pagination wrapper
data class ImpianResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("dataPage") val dataPage: ImpianPageData?
)

data class ImpianPageData(
    @SerializedName("impianItems") val impianItems: List<ImpianItem>?,
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

data class ImpianItem(
    @SerializedName("id_impian") val idImpian: Long,
    @SerializedName("id_user") val idUser: Long,
    @SerializedName("nama_barang") val namaBarang: String?,
    @SerializedName("foto_barang") val fotoBarang: String?,
    @SerializedName("harga_barang") val hargaBarang: Long?,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

// Response untuk tambah impian
data class TambahImpianResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

// Response untuk hapus impian
data class HapusImpianResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
