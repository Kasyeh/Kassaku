package com.example.kassaku.data.repository

import com.example.kassaku.data.remote.ApiService
import com.example.kassaku.data.remote.model.BalanceData
import com.example.kassaku.data.remote.model.BalanceResponse
import com.example.kassaku.data.remote.model.PemasukanResponse
import com.example.kassaku.data.remote.model.PengeluaranResponse
import com.example.kassaku.data.remote.model.RiwayatItem
import com.example.kassaku.data.remote.model.RiwayatResponse
import retrofit2.Response

/**
 * Repository untuk mengelola transaksi dan saldo user
 */
class TransactionRepository(private val apiService: ApiService) {

    /**
     * Ambil data saldo user
     * @return Result dengan BalanceData jika sukses, atau pesan error jika gagal
     */
    suspend fun getUserBalance(userId: Int): Result<BalanceData> {
        return try {
            val response: Response<BalanceResponse> = apiService.getUserBalance(userId)
            
            if (response.isSuccessful) {
                val balanceData = response.body()?.data
                if (balanceData != null) {
                    Result.success(balanceData)
                } else {
                    Result.failure(Exception("Data saldo tidak ditemukan"))
                }
            } else {
                Result.failure(Exception("Gagal mengambil saldo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ambil riwayat transaksi user
     * @return Result dengan List<RiwayatItem> jika sukses, atau pesan error jika gagal
     */
    suspend fun getRiwayatTransaksi(
        userId: Int, 
        periode: String? = null,
        jenis: String? = null,
        tanggal: String? = null,
        bulan: Int? = null,
        tahun: Int? = null,
        page: Int? = null
    ): Result<List<RiwayatItem>> {
        return try {
            // Coba dengan response pagination dulu
            val response: Response<RiwayatResponse> = apiService.getRiwayatTransaksi(
                userId, periode, jenis, tanggal, bulan, tahun, page
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("TransactionRepo", "Response body (paginated): $body")
                
                // Jika dataPage null, coba parsing sebagai direct array
                val riwayatItems = if (body?.dataPage?.riwayatItems != null) {
                    body.dataPage.riwayatItems
                } else {
                    // Fallback: coba parsing sebagai array langsung
                    android.util.Log.d("TransactionRepo", "DataPage null, trying direct response...")
                    try {
                        val directResponse = apiService.getRiwayatTransaksiDirect(
                            userId, periode, jenis, tanggal, bulan, tahun, page
                        )
                        directResponse.body()?.riwayatItems ?: emptyList()
                    } catch (e: Exception) {
                        android.util.Log.e("TransactionRepo", "Direct response also failed: ${e.message}")
                        emptyList()
                    }
                }
                
                Result.success(riwayatItems)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Gagal mengambil riwayat: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("TransactionRepo", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Tambah pemasukan
     * @return Result dengan pesan sukses jika berhasil, atau pesan error jika gagal
     */
    suspend fun tambahPemasukan(userId: Int, nominal: Int, kategori: String, keterangan: String, tanggal: String? = null): Result<String> {
        return try {
            val response: Response<PemasukanResponse> = apiService.tambahPemasukan(userId, nominal, kategori, keterangan, tanggal)
            
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Pemasukan berhasil ditambahkan"
                Result.success(message)
            } else {
                Result.failure(Exception("Gagal menambahkan pemasukan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tambah pengeluaran
     * @return Result dengan pesan sukses jika berhasil, atau pesan error jika gagal
     */
    suspend fun tambahPengeluaran(userId: Int, nominal: Int, kategori: String, keterangan: String, tanggal: String? = null): Result<String> {
        return try {
            val response: Response<PengeluaranResponse> = apiService.tambahPengeluaran(userId, nominal, kategori, keterangan, tanggal)
            
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Pengeluaran berhasil ditambahkan"
                Result.success(message)
            } else {
                Result.failure(Exception("Gagal menambahkan pengeluaran: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStatistik(userId: Int): Result<com.example.kassaku.data.remote.model.StatistikData> {
        return try {
            val response = apiService.getStatistik(userId)
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Data statistik kosong"))
                }
            } else {
                Result.failure(Exception("Gagal ambil statistik: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
