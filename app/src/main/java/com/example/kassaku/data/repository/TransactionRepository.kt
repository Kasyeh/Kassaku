package com.example.kassaku.data.repository

import com.example.kassaku.data.remote.ApiService
import com.example.kassaku.data.remote.model.BalanceData
import com.example.kassaku.data.remote.model.BalanceResponse
import com.example.kassaku.data.remote.model.PemasukanResponse
import com.example.kassaku.data.remote.model.PengeluaranResponse
import com.example.kassaku.data.remote.model.RiwayatItem
import com.example.kassaku.data.remote.model.RiwayatResponse
import com.example.kassaku.data.remote.model.BudgetKategoriItem
import com.example.kassaku.data.remote.model.BudgetKategoriResponse
import com.example.kassaku.data.remote.model.SimpanBudgetResponse
import com.example.kassaku.data.remote.model.HapusBudgetResponse
import com.example.kassaku.viewmodel.PemasukanResult
import com.example.kassaku.viewmodel.PengeluaranResult
import com.example.kassaku.viewmodel.TargetPengeluaranResult
import com.example.kassaku.viewmodel.BudgetActionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    fun tambahPemasukan(
        userId: Int,
        nominal: Long,
        kategori: String,
        keterangan: String?,
        tanggal: String?
    ): Flow<PemasukanResult> = flow {
        try {
            val response = apiService.tambahPemasukan(userId, nominal, kategori, keterangan, tanggal)
            if (response.success) {
                emit(PemasukanResult.Success(response.message))
            } else {
                emit(PemasukanResult.Error(response.message))
            }
        } catch (e: Exception) {
            emit(PemasukanResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    fun tambahPengeluaran(
        userId: Int,
        nominal: Long,
        kategori: String,
        keterangan: String?,
        tanggal: String?
    ): Flow<PengeluaranResult> = flow {
        try {
            val response = apiService.tambahPengeluaran(userId, nominal, kategori, keterangan, tanggal)
            if (response.success) {
                emit(PengeluaranResult.Success(response.message))
            } else {
                emit(PengeluaranResult.Error(response.message))
            }
        } catch (e: Exception) {
            emit(PengeluaranResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    fun simpanTargetPengeluaran(
        userId: Int,
        target: Long
    ): Flow<TargetPengeluaranResult> = flow {
        try {
            val response = apiService.simpanTargetPengeluaran(userId, target)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                emit(TargetPengeluaranResult.Success(
                    body.message,
                    body.data?.isOverBudget ?: false,
                    body.data?.targetPengeluaran?.toLong() ?: 0L
                ))
            } else {
                val errorMsg = response.message()
                emit(TargetPengeluaranResult.Error(if (errorMsg.isNullOrEmpty()) "Gagal menyimpan target" else errorMsg))
            }
        } catch (e: Exception) {
            emit(TargetPengeluaranResult.Error(e.message ?: "Terjadi kesalahan"))
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

    suspend fun getBudgetKategori(userId: Int): Result<List<BudgetKategoriItem>> {
        return try {
            val response = apiService.getBudgetKategori(userId)
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception("Gagal ambil budget: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun simpanBudgetKategori(
        userId: Int,
        kategori: String,
        nominal: Long,
        periode: String,
        tanggalMulai: String? = null,
        tanggalAkhir: String? = null
    ): Flow<BudgetActionResult> = flow {
        try {
            val response = apiService.simpanBudgetKategori(userId, kategori, nominal, periode, tanggalMulai, tanggalAkhir)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(BudgetActionResult.Success(response.body()?.message ?: "Budget disimpan"))
            } else {
                emit(BudgetActionResult.Error(response.body()?.message ?: "Gagal simpan budget"))
            }
        } catch (e: Exception) {
            emit(BudgetActionResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    fun hapusBudgetKategori(
        budgetId: Int,
        userId: Int,
        password: String
    ): Flow<BudgetActionResult> = flow {
        try {
            val response = apiService.hapusBudgetKategori(budgetId, userId, password)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(BudgetActionResult.Success(response.body()?.message ?: "Budget dihapus"))
            } else {
                emit(BudgetActionResult.Error(response.body()?.message ?: "Gagal hapus budget"))
            }
        } catch (e: Exception) {
            emit(BudgetActionResult.Error(e.message ?: "Terjadi kesalahan"))
        }
    }
}
