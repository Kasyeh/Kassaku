package com.example.kassaku.data.repository

import com.example.kassaku.data.remote.ApiService
import com.example.kassaku.data.remote.model.ImpianItem
import com.example.kassaku.data.remote.model.ImpianResponse
import com.example.kassaku.data.remote.model.SetorImpianResponse
import com.example.kassaku.data.remote.model.TambahImpianResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

/**
 * Repository untuk mengelola data impian user
 */
class ImpianRepository(private val apiService: ApiService) {

    /**
     * Ambil data impian user
     * @return Result dengan List<ImpianItem> jika sukses, atau pesan error jika gagal
     */
    suspend fun getImpian(userId: Int): Result<List<ImpianItem>> {
        return try {
            val response: Response<ImpianResponse> = apiService.getMyImpian()
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("ImpianRepo", "Response body: $body")
                android.util.Log.d("ImpianRepo", "DataPage: ${body?.dataPage}")
                android.util.Log.d("ImpianRepo", "ImpianItems: ${body?.dataPage?.impianItems}")
                
                val impianItems = body?.dataPage?.impianItems ?: emptyList()
                
                android.util.Log.d("ImpianRepo", "Final items count: ${impianItems.size}")
                Result.success(impianItems)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ImpianRepo", "Response not successful: ${response.code()} - ${response.message()}")
                android.util.Log.e("ImpianRepo", "Error body: $errorBody")
                Result.failure(Exception("Gagal mengambil impian: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ImpianRepo", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Tambah impian baru
     * @return Result dengan pesan sukses jika berhasil, atau pesan error jika gagal
     */
    suspend fun tambahImpian(
        userId: Int,
        namaBarang: String,
        hargaBarang: Long,
        deadline: String,
        keterangan: String?,
        fotoBarang: MultipartBody.Part?
    ): Result<String> {
        return try {
            // Foto is required, return error if null
            if (fotoBarang == null) {
                return Result.failure(Exception("Foto impian wajib diisi"))
            }
            
            // Convert parameters to RequestBody
            val namaBarangBody = namaBarang.toRequestBody("text/plain".toMediaTypeOrNull())
            val hargaBarangBody = hargaBarang.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val deadlineBody = deadline.toRequestBody("text/plain".toMediaTypeOrNull())
            val keteranganBody = if (keterangan.isNullOrBlank()) null else keterangan.toRequestBody("text/plain".toMediaTypeOrNull())

            val response: Response<TambahImpianResponse> = apiService.tambahImpian(
                namaBarang = namaBarangBody,
                hargaBarang = hargaBarangBody,
                deadline = deadlineBody,
                keterangan = keteranganBody,
                fotoBarang = fotoBarang
            )
            
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Impian berhasil ditambahkan"
                Result.success(message)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ImpianRepo", "Error adding impian: ${response.code()} - $errorBody")
                Result.failure(Exception("Gagal menambahkan impian: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ImpianRepo", "Exception adding impian: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Hapus impian
     * @return Result dengan pesan sukses jika berhasil, atau pesan error jika gagal
     */
    suspend fun hapusImpian(idImpian: Long, userId: Int, password: String): Result<String> {
        return try {
            val response = apiService.hapusImpian(idImpian, password)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Impian berhasil dihapus")
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    val json = com.google.gson.JsonParser.parseString(errorBody)
                    json.asJsonObject.get("message").asString
                } catch (e: Exception) {
                    "Gagal menghapus impian"
                }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            android.util.Log.e("ImpianRepo", "Exception deleting impian: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Setor dana ke impian tertentu.
     */
    suspend fun setorImpian(
        idImpian: Long,
        userId: Int,
        nominal: Long,
        keterangan: String?
    ): Result<String> {
        return try {
            val response: Response<SetorImpianResponse> = apiService.setorImpian(
                idImpian = idImpian,
                nominal = nominal,
                keterangan = keterangan,
                tanggal = null
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Setoran impian berhasil disimpan")
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ImpianRepo", "Error setor impian: ${response.code()} - $errorBody")
                Result.failure(Exception("Gagal setor impian: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ImpianRepo", "Exception setor impian: ${e.message}", e)
            Result.failure(e)
        }
    }
}
