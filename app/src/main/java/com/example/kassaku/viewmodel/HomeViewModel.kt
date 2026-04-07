package com.example.kassaku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kassaku.data.remote.ApiClient
import com.example.kassaku.data.remote.model.BalanceData
import com.example.kassaku.data.repository.TransactionRepository
import com.example.kassaku.data.repository.ImpianRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.kassaku.utils.ThemeMode

enum class LogoutReason {
    MANUAL,
    BLOCKED
}




open class HomeViewModel(
    private val transactionRepository: TransactionRepository = TransactionRepository(ApiClient.api),
    private val impianRepository: ImpianRepository = ImpianRepository(ApiClient.api),
    private val realtimeDatabaseRepository: com.example.kassaku.data.repository.RealtimeDatabaseRepository = com.example.kassaku.data.repository.RealtimeDatabaseRepository()
) : ViewModel() {

    private val _balanceData = MutableStateFlow<BalanceData?>(null)
    open val balanceData: StateFlow<BalanceData?> = _balanceData.asStateFlow()

    private val _pemasukanResult = MutableStateFlow<PemasukanResult>(PemasukanResult.Idle)
    val pemasukanResult: StateFlow<PemasukanResult> = _pemasukanResult.asStateFlow()

    private val _pengeluaranResult = MutableStateFlow<PengeluaranResult>(PengeluaranResult.Idle)
    val pengeluaranResult: StateFlow<PengeluaranResult> = _pengeluaranResult.asStateFlow()

    private val _tambahImpianResult = MutableStateFlow<TambahImpianResult>(TambahImpianResult.Idle)
    val tambahImpianResult: StateFlow<TambahImpianResult> = _tambahImpianResult.asStateFlow()

    private val _riwayatUiState = MutableStateFlow<RiwayatUiState>(RiwayatUiState.Idle)
    open val riwayatUiState: StateFlow<RiwayatUiState> = _riwayatUiState.asStateFlow()

    private val _impianUiState = MutableStateFlow<ImpianUiState>(ImpianUiState.Idle)
    open val impianUiState: StateFlow<ImpianUiState> = _impianUiState.asStateFlow()

    private val _hapusImpianResult = MutableStateFlow<HapusImpianResult>(HapusImpianResult.Idle)
    val hapusImpianResult: StateFlow<HapusImpianResult> = _hapusImpianResult.asStateFlow()
    private val _setorImpianResult = MutableStateFlow<SetorImpianResult>(SetorImpianResult.Idle)
    val setorImpianResult: StateFlow<SetorImpianResult> = _setorImpianResult.asStateFlow()

    private val _logoutNavigationEvent = MutableSharedFlow<LogoutReason>()
    open val logoutNavigationEvent: SharedFlow<LogoutReason> = _logoutNavigationEvent.asSharedFlow()

    private val _statistikData = MutableStateFlow<com.example.kassaku.data.remote.model.StatistikData?>(null)
    val statistikData: StateFlow<com.example.kassaku.data.remote.model.StatistikData?> = _statistikData.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    open val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _budgetActionResult = MutableStateFlow<BudgetActionResult>(BudgetActionResult.Idle)
    val budgetActionResult: StateFlow<BudgetActionResult> = _budgetActionResult.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    open fun loadBalanceData(userId: Int) {
        // Initial load from API
        viewModelScope.launch {
            val result = transactionRepository.getUserBalance(userId)
            result.fold(
                onSuccess = { balanceData ->
                    _balanceData.value = balanceData
                },
                onFailure = {
                    _balanceData.value = null
                }
            )
        }

        // Listen for Realtime Updates (Balance)
        viewModelScope.launch {
            realtimeDatabaseRepository.getUserBalanceFlow(userId).collect { newSaldo ->
                newSaldo?.let {
                    val currentBalance = _balanceData.value
                    if (currentBalance != null) {
                        // Update saldo only, keep other fields
                        _balanceData.value = currentBalance.copy(saldo = it.toLong().toString())
                    }
                }
            }
        }

        // Listen for Account Status Updates (Auto-Logout if blocked)
        viewModelScope.launch {
            realtimeDatabaseRepository.getUserStatusFlow(userId).collect { active ->
                if (active == 0) {
                    // Akun diblokir, paksa logout
                    android.util.Log.w("HomeViewModel", "Account blocked by admin! Forcing logout.")
                    _logoutNavigationEvent.emit(LogoutReason.BLOCKED)
                }
            }
        }
    }

    fun tambahPemasukan(userId: Int, nominal: Long, kategori: String, keterangan: String, tanggal: String? = null) {
        viewModelScope.launch {
            transactionRepository.tambahPemasukan(userId, nominal, kategori, keterangan, tanggal).collect { result ->
                _pemasukanResult.value = result
                if (result is PemasukanResult.Success) {
                    loadBalanceData(userId)
                    fetchRiwayatTransaksi(userId)
                }
            }
        }
    }

    fun tambahPengeluaran(userId: Int, nominal: Long, kategori: String, keterangan: String, tanggal: String? = null) {
        viewModelScope.launch {
            transactionRepository.tambahPengeluaran(userId, nominal, kategori, keterangan, tanggal).collect { result ->
                _pengeluaranResult.value = result
                if (result is PengeluaranResult.Success) {
                    loadBalanceData(userId)
                    fetchRiwayatTransaksi(userId)
                }
            }
        }
    }

    open fun fetchRiwayatTransaksi(
        userId: Int,
        periode: String? = null,
        jenis: String? = null,
        tanggal: String? = null,
        bulan: Int? = null,
        tahun: Int? = null
    ) {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "fetchRiwayatTransaksi: $userId, $periode, $jenis, $tanggal, $bulan, $tahun")
            _riwayatUiState.value = RiwayatUiState.Loading
            val result = transactionRepository.getRiwayatTransaksi(
                userId, periode, jenis, tanggal, bulan, tahun
            )
            result.fold(
                onSuccess = { items ->
                    _riwayatUiState.value = RiwayatUiState.Success(items)
                },
                onFailure = { exception ->
                    android.util.Log.e("HomeViewModel", "Error fetching riwayat: ${exception.message}", exception)
                    _riwayatUiState.value = RiwayatUiState.Error(exception.message ?: "Gagal memuat riwayat")
                }
            )
        }
    }

    fun resetPemasukanResult() {
        _pemasukanResult.value = PemasukanResult.Idle
    }

    fun resetPengeluaranResult() {
        _pengeluaranResult.value = PengeluaranResult.Idle
    }

    open fun fetchImpian(userId: Int) {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "fetchImpian called with userId: $userId")
            _impianUiState.value = ImpianUiState.Loading
            val result = impianRepository.getImpian(userId)
            result.fold(
                onSuccess = { items ->
                    android.util.Log.d("HomeViewModel", "Success: received ${items.size} impian items")
                    items.forEach { item ->
                        android.util.Log.d("HomeViewModel", "Impian Item: $item")
                    }
                    _impianUiState.value = ImpianUiState.Success(items)
                },
                onFailure = { exception ->
                    android.util.Log.e("HomeViewModel", "Error fetching impian: ${exception.message}", exception)
                    _impianUiState.value = ImpianUiState.Error(exception.message ?: "Gagal memuat impian")
                }
            )
        }
    }

    fun tambahImpian(userId: Int, namaBarang: String, hargaBarang: Long, deadline: String, keterangan: String?, fotoPart: okhttp3.MultipartBody.Part?) {
        viewModelScope.launch {
            val result = impianRepository.tambahImpian(userId, namaBarang, hargaBarang, deadline, keterangan, fotoPart)
            result.fold(
                onSuccess = { message ->
                    _tambahImpianResult.value = TambahImpianResult.Success(message)
                    // Refresh data impian setelah sukses
                    fetchImpian(userId)
                },
                onFailure = { exception ->
                    _tambahImpianResult.value = TambahImpianResult.Error(exception.message ?: "Gagal menambahkan impian")
                }
            )
        }
    }

    fun resetTambahImpianResult() {
        _tambahImpianResult.value = TambahImpianResult.Idle
    }

    fun hapusImpian(idImpian: Long, userId: Int, password: String) {
        viewModelScope.launch {
            _hapusImpianResult.value = HapusImpianResult.Loading
            val result = impianRepository.hapusImpian(idImpian, userId, password)
            result.fold(
                onSuccess = { message ->
                    _hapusImpianResult.value = HapusImpianResult.Success(message)
                    fetchImpian(userId)
                },
                onFailure = { exception ->
                    _hapusImpianResult.value = HapusImpianResult.Error(exception.message ?: "Gagal menghapus impian")
                }
            )
        }
    }

    fun resetHapusImpianResult() {
        _hapusImpianResult.value = HapusImpianResult.Idle
    }

    fun setorImpian(idImpian: Long, userId: Int, nominal: Long, keterangan: String?) {
        viewModelScope.launch {
            _setorImpianResult.value = SetorImpianResult.Loading
            val result = impianRepository.setorImpian(idImpian, userId, nominal, keterangan)
            result.fold(
                onSuccess = { message ->
                    _setorImpianResult.value = SetorImpianResult.Success(message)
                    fetchImpian(userId)
                    fetchRiwayatTransaksi(userId)
                    loadBalanceData(userId)
                },
                onFailure = { exception ->
                    _setorImpianResult.value = SetorImpianResult.Error(
                        exception.message ?: "Gagal menyimpan setoran impian"
                    )
                }
            )
        }
    }

    fun resetSetorImpianResult() {
        _setorImpianResult.value = SetorImpianResult.Idle
    }

    // ===============================
    // EXPORT PDF
    // ===============================
    private val _exportPdfResult = MutableStateFlow<ExportPdfResult>(ExportPdfResult.Idle)
    val exportPdfResult: StateFlow<ExportPdfResult> = _exportPdfResult.asStateFlow()

    /**
     * Export riwayat transaksi sebagai PDF
     * @param userId ID user
     * @param periode Filter periode: "hari_ini", "minggu_ini", "bulan_ini", atau null untuk semua
     * @param tanggal Filter tanggal spesifik (format: yyyy-MM-dd), atau null
     * @param bulan Filter bulan (1-12), atau null
     * @param tahun Filter tahun (YYYY), atau null
     */
    fun exportPdf(
        userId: Int,
        periode: String? = null,
        tanggal: String? = null,
        bulan: Int? = null,
        tahun: Int? = null
    ) {
        viewModelScope.launch {
            _exportPdfResult.value = ExportPdfResult.Loading
            try {
                val response = ApiClient.api.exportPdf(userId, periode, tanggal, bulan, tahun)
                if (response.isSuccessful && response.body() != null) {
                    _exportPdfResult.value = ExportPdfResult.Success(response.body()!!)
                } else {
                    _exportPdfResult.value = ExportPdfResult.Error("Gagal mengunduh PDF: ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error exporting PDF: ${e.message}", e)
                _exportPdfResult.value = ExportPdfResult.Error(e.message ?: "Gagal mengunduh PDF")
            }
        }
    }

    fun resetExportPdfResult() {
        _exportPdfResult.value = ExportPdfResult.Idle
    }

    fun logout() {
        viewModelScope.launch {
            // Reset semua state
            _balanceData.value = null
            _riwayatUiState.value = RiwayatUiState.Idle
            _impianUiState.value = ImpianUiState.Idle
            _statistikData.value = null
            
            // Trigger navigasi logout
            _logoutNavigationEvent.emit(LogoutReason.MANUAL)
        }
    }

    fun fetchStatistik(userId: Int) {
        viewModelScope.launch {
            val result = transactionRepository.getStatistik(userId)
            result.fold(
                onSuccess = { data -> _statistikData.value = data },
                onFailure = { _statistikData.value = null }
            )
        }
    }

    // ===============================
    // TARGET PENGELUARAN BULANAN
    // ===============================
    private val _targetPengeluaranResult = MutableStateFlow<TargetPengeluaranResult>(TargetPengeluaranResult.Idle)
    val targetPengeluaranResult: StateFlow<TargetPengeluaranResult> = _targetPengeluaranResult.asStateFlow()

    fun simpanTargetPengeluaran(userId: Int, targetPengeluaran: Long) {
        viewModelScope.launch {
            transactionRepository.simpanTargetPengeluaran(userId, targetPengeluaran).collect { result ->
                _targetPengeluaranResult.value = result
                if (result is TargetPengeluaranResult.Success) {
                    loadBalanceData(userId)
                }
            }
        }
    }

    fun resetTargetPengeluaranResult() {
        _targetPengeluaranResult.value = TargetPengeluaranResult.Idle
    }

    // ===============================
    // RESET SALDO & RIWAYAT
    // ===============================
    private val _resetSaldoResult = MutableStateFlow<ResetSaldoResult>(ResetSaldoResult.Idle)
    val resetSaldoResult: StateFlow<ResetSaldoResult> = _resetSaldoResult.asStateFlow()

    fun resetSaldo(userId: Int, password: String) {
        viewModelScope.launch {
            _resetSaldoResult.value = ResetSaldoResult.Loading
            try {
                val response = ApiClient.api.resetSaldo(userId, password)
                if (response.isSuccessful && response.body()?.success == true) {
                    _resetSaldoResult.value = ResetSaldoResult.Success(
                        response.body()?.message ?: "Saldo berhasil direset"
                    )
                    // Refresh semua data setelah reset sukses
                    loadBalanceData(userId)
                    fetchRiwayatTransaksi(userId)
                    fetchStatistik(userId)
                    fetchImpian(userId) // Opsional, mungkin tetap biarkan impian? Tapi user bilang reset pengeluaran/pemasukan.
                } else {
                    _resetSaldoResult.value = ResetSaldoResult.Error(
                        response.body()?.message ?: "Gagal meriset saldo"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error resetting saldo: ${e.message}", e)
                _resetSaldoResult.value = ResetSaldoResult.Error(
                    e.message ?: "Gagal meriset saldo"
                )
            }
        }
    }

    fun resetResetSaldoResult() {
        _resetSaldoResult.value = ResetSaldoResult.Idle
    }

    // ===============================
    // BUDGET PER KATEGORI
    // ===============================
    fun simpanBudgetKategori(
        userId: Int,
        kategori: String,
        nominal: Long,
        periode: String,
        tanggalMulai: String? = null,
        tanggalAkhir: String? = null
    ) {
        viewModelScope.launch {
            _budgetActionResult.value = BudgetActionResult.Loading
            transactionRepository.simpanBudgetKategori(userId, kategori, nominal, periode, tanggalMulai, tanggalAkhir)
                .collect { result ->
                    _budgetActionResult.value = result
                    if (result is BudgetActionResult.Success) {
                        fetchStatistik(userId)
                    }
                }
        }
    }

    fun hapusBudgetKategori(budgetId: Int, userId: Int, password: String) {
        viewModelScope.launch {
            _budgetActionResult.value = BudgetActionResult.Loading
            transactionRepository.hapusBudgetKategori(budgetId, userId, password).collect { result ->
                _budgetActionResult.value = result
                if (result is BudgetActionResult.Success) {
                    fetchStatistik(userId)
                }
            }
        }
    }

    fun resetBudgetActionResult() {
        _budgetActionResult.value = BudgetActionResult.Idle
    }
}

