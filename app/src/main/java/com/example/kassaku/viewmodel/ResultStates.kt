package com.example.kassaku.viewmodel

import okhttp3.ResponseBody

sealed class PemasukanResult {
    data class Success(val message: String) : PemasukanResult()
    data class Error(val message: String) : PemasukanResult()
    object Idle : PemasukanResult()
}

sealed class PengeluaranResult {
    data class Success(val message: String) : PengeluaranResult()
    data class Error(val message: String) : PengeluaranResult()
    object Idle : PengeluaranResult()
}

sealed class TambahImpianResult {
    data class Success(val message: String) : TambahImpianResult()
    data class Error(val message: String) : TambahImpianResult()
    object Idle : TambahImpianResult()
}

sealed class TargetPengeluaranResult {
    object Idle : TargetPengeluaranResult()
    object Loading : TargetPengeluaranResult()
    data class Success(
        val message: String, 
        val isOverBudget: Boolean,
        val targetAmount: Long = 0L
    ) : TargetPengeluaranResult()
    data class Error(val message: String) : TargetPengeluaranResult()
}

sealed class ResetSaldoResult {
    object Idle : ResetSaldoResult()
    object Loading : ResetSaldoResult()
    data class Success(val message: String) : ResetSaldoResult()
    data class Error(val message: String) : ResetSaldoResult()
}

sealed class ExportPdfResult {
    object Idle : ExportPdfResult()
    object Loading : ExportPdfResult()
    data class Success(val responseBody: ResponseBody) : ExportPdfResult()
    data class Error(val message: String) : ExportPdfResult()
}

sealed class HapusImpianResult {
    object Idle : HapusImpianResult()
    object Loading : HapusImpianResult()
    data class Success(val message: String) : HapusImpianResult()
    data class Error(val message: String) : HapusImpianResult()
}

sealed class SetorImpianResult {
    object Idle : SetorImpianResult()
    object Loading : SetorImpianResult()
    data class Success(val message: String) : SetorImpianResult()
    data class Error(val message: String) : SetorImpianResult()
}

sealed class BudgetActionResult {
    object Idle : BudgetActionResult()
    object Loading : BudgetActionResult()
    data class Success(val message: String) : BudgetActionResult()
    data class Error(val message: String) : BudgetActionResult()
}
