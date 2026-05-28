package com.example.kassaku.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kassaku.data.remote.ApiClient
import com.example.kassaku.data.remote.model.BalanceData
import com.example.kassaku.data.repository.NotificationRepository
import com.example.kassaku.data.repository.TransactionRepository
import com.example.kassaku.data.repository.ImpianRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import com.example.kassaku.utils.ThemeMode
import com.google.gson.JsonParser
import java.util.concurrent.atomic.AtomicBoolean

enum class LogoutReason {
    MANUAL,
    BLOCKED
}

sealed interface AvatarUpdateResult {
    object Idle : AvatarUpdateResult
    object Loading : AvatarUpdateResult
    data class Success(val message: String, val avatarUrl: String?) : AvatarUpdateResult
    data class Error(val message: String) : AvatarUpdateResult
}

sealed interface AiInsightUiState {
    object Idle : AiInsightUiState
    object Loading : AiInsightUiState
    data class Success(val insight: String) : AiInsightUiState
    data class Error(val message: String) : AiInsightUiState
}

open class HomeViewModel(
    private val transactionRepository: TransactionRepository = TransactionRepository(ApiClient.api),
    private val impianRepository: ImpianRepository = ImpianRepository(ApiClient.api),
    private val notificationRepository: NotificationRepository = NotificationRepository(ApiClient.api),
    private val realtimeDatabaseRepository: com.example.kassaku.data.repository.RealtimeDatabaseRepository = com.example.kassaku.data.repository.RealtimeDatabaseRepository()
) : ViewModel() {
    private var balanceRealtimeJob: Job? = null
    private var statusRealtimeJob: Job? = null
    private var accountEventJob: Job? = null
    private var monitoredUserId: Int? = null
    private val blockedLogoutTriggered = AtomicBoolean(false)

    private val _balanceData = MutableStateFlow<BalanceData?>(null)
    open val balanceData: StateFlow<BalanceData?> = _balanceData.asStateFlow()

    private val _isHomeRefreshing = MutableStateFlow(false)
    val isHomeRefreshing: StateFlow<Boolean> = _isHomeRefreshing.asStateFlow()

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

    private val _isDynamicColor = MutableStateFlow(false)
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor.asStateFlow()

    private val _budgetActionResult = MutableStateFlow<BudgetActionResult>(BudgetActionResult.Idle)
    val budgetActionResult: StateFlow<BudgetActionResult> = _budgetActionResult.asStateFlow()

    private val _notificationUiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Idle)
    val notificationUiState: StateFlow<NotificationUiState> = _notificationUiState.asStateFlow()

    private val _notificationUnreadCount = MutableStateFlow(0)
    val notificationUnreadCount: StateFlow<Int> = _notificationUnreadCount.asStateFlow()

    private val _aiInsightState = MutableStateFlow<AiInsightUiState>(AiInsightUiState.Idle)
    val aiInsightState: StateFlow<AiInsightUiState> = _aiInsightState.asStateFlow()

    private val _smartNudges = MutableStateFlow<List<com.example.kassaku.data.remote.model.NudgeItem>>(emptyList())
    val smartNudges: StateFlow<List<com.example.kassaku.data.remote.model.NudgeItem>> = _smartNudges.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setDynamicColor(enabled: Boolean) {
        _isDynamicColor.value = enabled
    }

    open fun loadBalanceData(userId: Int) {
        startSessionMonitoring(userId)

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
        if (monitoredUserId != userId) {
            balanceRealtimeJob?.cancel()
            monitoredUserId = userId
        }

        // Initial fetch from API
        refreshAllData(userId)

        if (balanceRealtimeJob?.isActive != true) {
            balanceRealtimeJob = viewModelScope.launch {
                realtimeDatabaseRepository.getUserBalanceFlow(userId).collect { data ->
                    if (data == null) {
                        Log.w("HomeViewModel", "Received null data from Firebase (check permissions/path)")
                        return@collect
                    }

                    val newSaldo = when (val s = data["saldo"]) {
                        is Number -> s.toDouble()
                        is String -> s.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    
                    val newSaldoStr = newSaldo.toLong().toString()
                    val currentBalance = _balanceData.value
                    
                    Log.d("HomeViewModel", "Realtime Sync: Current=$currentBalance, Firebase=$newSaldoStr")

                    if (currentBalance == null || currentBalance.saldo != newSaldoStr) {
                        Log.i("HomeViewModel", "Saldo mismatch detected! Triggering data refresh...")
                        
                        // Immediately update the balance number for perceived speed
                        if (currentBalance != null) {
                            _balanceData.value = currentBalance.copy(saldo = newSaldoStr)
                        }
                        
                        refreshAllData(userId)
                    }
                }
            }
        }
    }

    /**
     * Manual refresh from pull-to-refresh on HomeScreen.
     * Runs in parallel without the debounce used by realtime [refreshAllData].
     */
    fun refreshHomeScreen(userId: Int) {
        if (userId == 0 || _isHomeRefreshing.value) return
        viewModelScope.launch {
            _isHomeRefreshing.value = true
            try {
                coroutineScope {
                    val balanceDeferred = async {
                        transactionRepository.getUserBalance(userId).fold(
                            onSuccess = { _balanceData.value = it },
                            onFailure = { }
                        )
                    }
                    val riwayatDeferred = async {
                        _riwayatUiState.value = RiwayatUiState.Loading
                        transactionRepository.getRiwayatTransaksi(userId).fold(
                            onSuccess = { _riwayatUiState.value = RiwayatUiState.Success(it) },
                            onFailure = { e ->
                                _riwayatUiState.value = RiwayatUiState.Error(
                                    e.message ?: "Gagal memuat riwayat"
                                )
                            }
                        )
                    }
                    val statistikDeferred = async {
                        transactionRepository.getStatistik(userId).fold(
                            onSuccess = { _statistikData.value = it },
                            onFailure = { _statistikData.value = null }
                        )
                    }
                    val notificationsDeferred = async {
                        notificationRepository.getNotifications().fold(
                            onSuccess = { (items, unreadCount) ->
                                _notificationUnreadCount.value = unreadCount
                                _notificationUiState.value = NotificationUiState.Success(items)
                            },
                            onFailure = { e ->
                                _notificationUiState.value = NotificationUiState.Error(
                                    e.message ?: "Gagal memuat notifikasi."
                                )
                            }
                        )
                    }
                    val nudgesDeferred = async {
                        try {
                            val response = ApiClient.api.getSmartNudges()
                            if (response.isSuccessful) {
                                _smartNudges.value = response.body()?.data ?: emptyList()
                            } else {
                                Log.w("HomeViewModel", "Nudges refresh failed: ${response.message()}")
                            }
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Exception fetching nudges on refresh", e)
                        }
                    }
                    balanceDeferred.await()
                    riwayatDeferred.await()
                    statistikDeferred.await()
                    notificationsDeferred.await()
                    nudgesDeferred.await()
                }
            } finally {
                _isHomeRefreshing.value = false
            }
        }
    }

    private var refreshJob: Job? = null
    private fun refreshAllData(userId: Int) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            // Debounce to prevent multiple refreshes if multiple nodes update rapidly
            kotlinx.coroutines.delay(500)
            
            // 1. Refresh Balance (gets monthly totals)
            val balanceResult = transactionRepository.getUserBalance(userId)
            balanceResult.onSuccess { _balanceData.value = it }

            // 2. Refresh History
            fetchRiwayatTransaksi(userId)

            // 3. Refresh Statistics
            fetchStatistik(userId)

            // 4. Refresh Impian
            fetchImpian(userId)

            // 5. Fetch Smart Nudges
            fetchSmartNudges()
        }
    }

    fun startSessionMonitoring(userId: Int) {
        if (monitoredUserId != userId) {
            statusRealtimeJob?.cancel()
            accountEventJob?.cancel()
            blockedLogoutTriggered.set(false)
            monitoredUserId = userId
        }

        if (statusRealtimeJob?.isActive != true) {
            statusRealtimeJob = viewModelScope.launch {
                realtimeDatabaseRepository.getUserStatusFlow(userId)
                    .filterNotNull()
                    .collect { active ->
                        if (active == 0) {
                            emitBlockedLogout("Account blocked via status listener")
                        }
                    }
            }
        }

        if (accountEventJob?.isActive != true) {
            accountEventJob = viewModelScope.launch {
                realtimeDatabaseRepository.getAccountEventFlow(userId)
                    .filterNotNull()
                    .collect { event ->
                        if (event.event.equals("blocked", ignoreCase = true)) {
                            emitBlockedLogout("Account blocked via account_event listener")
                        }
                    }
            }
        }
    }

    private suspend fun emitBlockedLogout(logMessage: String) {
        if (blockedLogoutTriggered.compareAndSet(false, true)) {
            android.util.Log.w("HomeViewModel", logMessage)
            _logoutNavigationEvent.emit(LogoutReason.BLOCKED)
        }
    }

    fun resetRealtimeStateAfterLogout() {
        balanceRealtimeJob?.cancel()
        statusRealtimeJob?.cancel()
        accountEventJob?.cancel()
        balanceRealtimeJob = null
        statusRealtimeJob = null
        accountEventJob = null
        monitoredUserId = null
        blockedLogoutTriggered.set(false)
    }

    fun tambahPemasukan(userId: Int, nominal: Long, kategori: String, keterangan: String, tanggal: String? = null) {
        viewModelScope.launch {
            transactionRepository.tambahPemasukan(userId, nominal, kategori, keterangan, tanggal).collect { result ->
                _pemasukanResult.value = result
                if (result is PemasukanResult.Success) {
                    refreshAllData(userId)
                }
            }
        }
    }

    fun tambahPengeluaran(userId: Int, nominal: Long, kategori: String, keterangan: String, tanggal: String? = null) {
        viewModelScope.launch {
            transactionRepository.tambahPengeluaran(userId, nominal, kategori, keterangan, tanggal).collect { result ->
                _pengeluaranResult.value = result
                if (result is PengeluaranResult.Success) {
                    refreshAllData(userId)
                }
            }
        }
    }

    open fun fetchRiwayatTransaksi(
        userId: Int,
        periode: String? = null,
        jenis: String? = null,
        search: String? = null,
        tanggal: String? = null,
        bulan: Int? = null,
        tahun: Int? = null
    ) {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "fetchRiwayatTransaksi: $userId, $periode, $jenis, $search, $tanggal, $bulan, $tahun")
            _riwayatUiState.value = RiwayatUiState.Loading
            val result = transactionRepository.getRiwayatTransaksi(
                userId, periode, jenis, search, tanggal, bulan, tahun
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
            _tambahImpianResult.value = TambahImpianResult.Loading
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

    fun exportPdf(
        userId: Int,
        periode: String? = null,
        jenis: String? = null,
        search: String? = null,
        tanggal: String? = null,
        bulan: Int? = null,
        tahun: Int? = null
    ) {
        viewModelScope.launch {
            _exportPdfResult.value = ExportPdfResult.Loading
            try {
                val response = ApiClient.api.exportMyPdf(periode, jenis, search, tanggal, bulan, tahun)
                if (response.isSuccessful && response.body() != null) {
                    _exportPdfResult.value = ExportPdfResult.Success(response.body()!!)
                } else {
                    val parsedMessage = parseApiErrorMessage(response.errorBody()?.string().orEmpty())
                    _exportPdfResult.value = ExportPdfResult.Error(parsedMessage ?: "Gagal mengunduh PDF: ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error exporting PDF: ${e.message}", e)
                _exportPdfResult.value = ExportPdfResult.Error(e.message ?: "Gagal mengunduh PDF")
            }
        }
    }

    private fun parseApiErrorMessage(raw: String): String? {
        return try {
            if (raw.isBlank()) return null
            JsonParser.parseString(raw).asJsonObject.get("message")?.asString
        } catch (_: Exception) {
            null
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
            resetRealtimeStateAfterLogout()
            
            // Trigger navigasi logout
            _logoutNavigationEvent.emit(LogoutReason.MANUAL)
        }
    }

    override fun onCleared() {
        resetRealtimeStateAfterLogout()
        super.onCleared()
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

    fun fetchAiInsight(userId: Int, period: String) {
        viewModelScope.launch {
            _aiInsightState.value = AiInsightUiState.Loading
            val result = transactionRepository.getAiInsight(userId, period)
            result.fold(
                onSuccess = { insight ->
                    _aiInsightState.value = AiInsightUiState.Success(insight)
                },
                onFailure = { exception ->
                    android.util.Log.e("HomeViewModel", "Error fetching AI Insight", exception)
                    _aiInsightState.value = AiInsightUiState.Error(exception.message ?: "Gagal memuat AI Insight")
                }
            )
        }
    }

    fun fetchSmartNudges() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getSmartNudges()
                if (response.isSuccessful) {
                    _smartNudges.value = response.body()?.data ?: emptyList()
                } else {
                    android.util.Log.e("HomeViewModel", "Error fetching nudges: ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Exception fetching nudges", e)
            }
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
                val response = ApiClient.api.resetSaldo(password)
                if (response.isSuccessful && response.body()?.success == true) {
                    _resetSaldoResult.value = ResetSaldoResult.Success(
                        response.body()?.message ?: "Saldo berhasil direset"
                    )
                    // Refresh semua data setelah reset sukses
                    loadBalanceData(userId)
                    fetchRiwayatTransaksi(userId)
                    fetchStatistik(userId)
                    fetchImpian(userId)
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

    fun fetchNotifications() {
        viewModelScope.launch {
            _notificationUiState.value = NotificationUiState.Loading
            notificationRepository.getNotifications().fold(
                onSuccess = { (items, unreadCount) ->
                    _notificationUnreadCount.value = unreadCount
                    _notificationUiState.value = NotificationUiState.Success(items)
                },
                onFailure = { exception ->
                    _notificationUiState.value = NotificationUiState.Error(
                        exception.message ?: "Gagal memuat notifikasi."
                    )
                }
            )
        }
    }

    fun refreshNotificationBadge() {
        viewModelScope.launch {
            notificationRepository.getNotifications().fold(
                onSuccess = { (_, unreadCount) ->
                    _notificationUnreadCount.value = unreadCount
                },
                onFailure = { }
            )
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead().fold(
                onSuccess = {
                    fetchNotifications()
                },
                onFailure = { exception ->
                    _notificationUiState.value = NotificationUiState.Error(
                        exception.message ?: "Gagal menandai notifikasi."
                    )
                }
            )
        }
    }

    // ===============================
    // EMAIL UPDATE
    // ===============================
    private val _emailUpdateResult = MutableStateFlow<EmailUpdateResult>(EmailUpdateResult.Idle)
    val emailUpdateResult: StateFlow<EmailUpdateResult> = _emailUpdateResult.asStateFlow()

    fun updateEmail(userId: Int, email: String?, password: String) {
        viewModelScope.launch {
            _emailUpdateResult.value = EmailUpdateResult.Loading
            try {
                val response = ApiClient.api.updateEmail(email, password)
                if (response.isSuccessful) {
                    _emailUpdateResult.value = EmailUpdateResult.Success(
                        response.body()?.message ?: "Email berhasil diperbarui"
                    )
                    loadBalanceData(userId)
                } else {
                    val rawError = response.errorBody()?.string().orEmpty()
                    val message = parseApiErrorMessage(rawError) ?: "Gagal memperbarui email"
                    _emailUpdateResult.value = EmailUpdateResult.Error(message)
                }
            } catch (e: Exception) {
                _emailUpdateResult.value = EmailUpdateResult.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetEmailUpdateResult() {
        _emailUpdateResult.value = EmailUpdateResult.Idle
    }

    // ===============================
    // PASSWORD UPDATE
    // ===============================
    private val _passwordUpdateResult = MutableStateFlow<EmailUpdateResult>(EmailUpdateResult.Idle)
    val passwordUpdateResult: StateFlow<EmailUpdateResult> = _passwordUpdateResult.asStateFlow()

    fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _passwordUpdateResult.value = EmailUpdateResult.Loading
            try {
                val response = ApiClient.api.updatePassword(currentPassword, newPassword, confirmPassword)
                if (response.isSuccessful && response.body()?.success == true) {
                    _passwordUpdateResult.value = EmailUpdateResult.Success(
                        response.body()?.message ?: "Password berhasil diperbarui"
                    )
                } else {
                    val rawError = response.errorBody()?.string().orEmpty()
                    val message = parseApiErrorMessage(rawError) ?: "Gagal memperbarui password"
                    _passwordUpdateResult.value = EmailUpdateResult.Error(message)
                }
            } catch (e: Exception) {
                _passwordUpdateResult.value = EmailUpdateResult.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetPasswordUpdateResult() {
        _passwordUpdateResult.value = EmailUpdateResult.Idle
    }

    // ===============================
    // CURRENCY UPDATE
    // ===============================
    private val _currencyUpdateResult = MutableStateFlow<EmailUpdateResult>(EmailUpdateResult.Idle)
    val currencyUpdateResult: StateFlow<EmailUpdateResult> = _currencyUpdateResult.asStateFlow()

    fun updateCurrency(userId: Int, currency: String, currencyFormat: String? = null) {
        viewModelScope.launch {
            _currencyUpdateResult.value = EmailUpdateResult.Loading
            try {
                val response = ApiClient.api.updateCurrency(currency, currencyFormat)
                if (response.isSuccessful) {
                    _currencyUpdateResult.value = EmailUpdateResult.Success(
                        response.body()?.message ?: "Format mata uang berhasil diperbarui"
                    )
                    loadBalanceData(userId)
                } else {
                    val rawError = response.errorBody()?.string().orEmpty()
                    val message = parseApiErrorMessage(rawError) ?: "Gagal memperbarui mata uang"
                    _currencyUpdateResult.value = EmailUpdateResult.Error(message)
                }
            } catch (e: Exception) {
                _currencyUpdateResult.value = EmailUpdateResult.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetCurrencyUpdateResult() {
        _currencyUpdateResult.value = EmailUpdateResult.Idle
    }

    // ===============================
    // AVATAR UPDATE
    // ===============================
    private val _avatarUpdateResult = MutableStateFlow<AvatarUpdateResult>(AvatarUpdateResult.Idle)
    val avatarUpdateResult: StateFlow<AvatarUpdateResult> = _avatarUpdateResult.asStateFlow()

    fun uploadAvatar(userId: Int, avatarPart: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            _avatarUpdateResult.value = AvatarUpdateResult.Loading
            try {
                val response = ApiClient.api.uploadAvatar(avatarPart)
                if (response.isSuccessful && response.body()?.success == true) {
                    _avatarUpdateResult.value = AvatarUpdateResult.Success(
                        response.body()?.message ?: "Avatar berhasil diunggah",
                        response.body()?.avatarUrl
                    )
                    loadBalanceData(userId)
                } else {
                    _avatarUpdateResult.value = AvatarUpdateResult.Error(
                        response.body()?.message ?: "Gagal mengunggah avatar"
                    )
                }
            } catch (e: Exception) {
                _avatarUpdateResult.value = AvatarUpdateResult.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun setPredefinedAvatar(userId: Int, avatarId: String) {
        viewModelScope.launch {
            _avatarUpdateResult.value = AvatarUpdateResult.Loading
            try {
                val response = ApiClient.api.setPredefinedAvatar(avatarId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _avatarUpdateResult.value = AvatarUpdateResult.Success(
                        response.body()?.message ?: "Avatar berhasil diperbarui",
                        response.body()?.avatarUrl
                    )
                    loadBalanceData(userId)
                } else {
                    _avatarUpdateResult.value = AvatarUpdateResult.Error(
                        response.body()?.message ?: "Gagal memperbarui avatar"
                    )
                }
            } catch (e: Exception) {
                _avatarUpdateResult.value = AvatarUpdateResult.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun removeAvatar(userId: Int) {
        viewModelScope.launch {
            _avatarUpdateResult.value = AvatarUpdateResult.Loading
            try {
                val response = ApiClient.api.removeAvatar()
                if (response.isSuccessful && response.body()?.success == true) {
                    _avatarUpdateResult.value = AvatarUpdateResult.Success(
                        response.body()?.message ?: "Avatar berhasil dihapus",
                        null
                    )
                    loadBalanceData(userId)
                }
            } catch (e: Exception) {
                _avatarUpdateResult.value = AvatarUpdateResult.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetAvatarUpdateResult() {
        _avatarUpdateResult.value = AvatarUpdateResult.Idle
    }

    // ===============================
    // FEEDBACK SUBMISSION
    // ===============================
    private val _feedbackResult = MutableStateFlow<FeedbackResult>(FeedbackResult.Idle)
    val feedbackResult: StateFlow<FeedbackResult> = _feedbackResult.asStateFlow()

    fun sendFeedback(subjek: String, pesan: String, rating: Int? = null) {
        viewModelScope.launch {
            _feedbackResult.value = FeedbackResult.Loading
            try {
                val response = ApiClient.api.sendFeedback(subjek, pesan, rating)
                if (response.isSuccessful && response.body()?.success == true) {
                    _feedbackResult.value = FeedbackResult.Success(
                        response.body()?.message ?: "Umpan balik berhasil dikirim"
                    )
                } else {
                    val rawError = response.errorBody()?.string().orEmpty()
                    val message = parseApiErrorMessage(rawError) ?: "Gagal mengirim umpan balik"
                    _feedbackResult.value = FeedbackResult.Error(message)
                }
            } catch (e: Exception) {
                _feedbackResult.value = FeedbackResult.Error(e.message ?: "Terjadi kesalahan koneksi")
            }
        }
    }

    fun resetFeedbackResult() {
        _feedbackResult.value = FeedbackResult.Idle
    }
}

sealed interface EmailUpdateResult {
    object Idle : EmailUpdateResult
    object Loading : EmailUpdateResult
    data class Success(val message: String) : EmailUpdateResult
    data class Error(val message: String) : EmailUpdateResult
}
