package com.example.kassaku.data.remote.model

import com.google.gson.annotations.SerializedName

data class StatistikResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StatistikData?
)

data class MotivasiItem(
    @SerializedName("id") val id: Int,
    @SerializedName("tipe") val tipe: String,
    @SerializedName("isi") val isi: String?,
    @SerializedName("foto") val foto: String?
)

data class CashflowPeriodData(
    @SerializedName("labels") val labels: List<String> = emptyList(),
    @SerializedName("income") val income: List<Double> = emptyList(),
    @SerializedName("expense") val expense: List<Double> = emptyList(),
    @SerializedName("net") val net: List<Double> = emptyList(),
    @SerializedName("total_income") val totalIncome: Double = 0.0,
    @SerializedName("total_expense") val totalExpense: Double = 0.0,
    @SerializedName("total_net") val totalNet: Double = 0.0,
    @SerializedName("change_pct") val changePct: Double = 0.0,
    @SerializedName("max_expense_label") val maxExpenseLabel: String? = null,
    @SerializedName("max_expense_value") val maxExpenseValue: Double = 0.0,
    @SerializedName("categories_income") val categoriesIncome: Map<String, Double> = emptyMap(),
    @SerializedName("categories_expense") val categoriesExpense: Map<String, Double> = emptyMap()
)

data class StatistikData(
    @SerializedName("labels") val labels: List<String> = emptyList(),
    @SerializedName("pemasukan") val pemasukan: List<Double> = emptyList(),
    @SerializedName("pengeluaran") val pengeluaran: List<Double> = emptyList(),
    @SerializedName("net") val net: List<Double> = emptyList(),
    @SerializedName("summary") val summary: StatistikSummaryData? = null,
    @SerializedName("cashflow_series") val cashflowSeries: Map<String, CashflowPeriodData>? = null,
    @SerializedName("default_cashflow_period") val defaultCashflowPeriod: String? = "30d",
    @SerializedName("budget_kategori") val budgetKategori: List<BudgetKategoriItem>? = emptyList(),
    @SerializedName("kategori_list") val kategoriList: List<String>? = emptyList(),
    @SerializedName("motivasi") val motivasi: List<MotivasiItem>? = null,
    @SerializedName("recent_transactions") val recentTransactions: List<RiwayatItem>? = emptyList(),
    @SerializedName("dream_forecast") val dreamForecast: List<DreamForecastItem>? = emptyList(),
    @SerializedName("kategori_cepat_pemasukan") val kategoriCepatPemasukan: List<String>? = emptyList(),
    @SerializedName("kategori_cepat_pengeluaran") val kategoriCepatPengeluaran: List<String>? = emptyList(),
    @SerializedName("budget_kategori_list") val budgetKategoriList: List<String>? = emptyList()
)

data class StatistikSummaryData(
    @SerializedName("saldo") val saldo: Double = 0.0,
    @SerializedName("monthly_pemasukan") val monthlyPemasukan: Double = 0.0,
    @SerializedName("monthly_pengeluaran") val monthlyPengeluaran: Double = 0.0,
    @SerializedName("target_pengeluaran") val targetPengeluaran: Double? = null,
    @SerializedName("is_over_budget") val isOverBudget: Boolean = false,
    @SerializedName("is_expense_higher_than_income") val isExpenseHigherThanIncome: Boolean = false,
    @SerializedName("expense_income_gap") val expenseIncomeGap: Double = 0.0,
    @SerializedName("target_progress_percent") val targetProgressPercent: Double? = null,
    @SerializedName("prev_month_pemasukan") val prevMonthPemasukan: Double = 0.0,
    @SerializedName("prev_month_pengeluaran") val prevMonthPengeluaran: Double = 0.0,
    @SerializedName("avg_savings") val avgSavings: Double = 0.0,
    @SerializedName("trend") val trend: String? = null,
    @SerializedName("trend_icon") val trendIcon: String? = null,
    @SerializedName("most_productive_month") val mostProductiveMonth: String? = null,
    @SerializedName("most_wasteful_month") val mostWastefulMonth: String? = null
)

data class DreamForecastItem(
    @SerializedName("id_impian") val idImpian: Long,
    @SerializedName("nama_barang") val namaBarang: String?,
    @SerializedName("foto_barang") val fotoBarang: String?,
    @SerializedName("harga_barang") val hargaBarang: Double?,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("avg_savings") val avgSavings: Double = 0.0,
    @SerializedName("reach_percent") val reachPercent: Double = 0.0
)

data class AiInsightResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("insight") val insight: String?,
    @SerializedName("message") val message: String?
)
