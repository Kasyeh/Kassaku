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
    @SerializedName("max_expense_value") val maxExpenseValue: Double = 0.0
)

data class StatistikData(
    @SerializedName("labels") val labels: List<String>,
    @SerializedName("pemasukan") val pemasukan: List<Double>,
    @SerializedName("pengeluaran") val pengeluaran: List<Double>,
    @SerializedName("net") val net: List<Double> = emptyList(),
    @SerializedName("cashflow_series") val cashflowSeries: Map<String, CashflowPeriodData>? = null,
    @SerializedName("default_cashflow_period") val defaultCashflowPeriod: String? = "30d",
    @SerializedName("budget_kategori") val budgetKategori: List<BudgetKategoriItem>?,
    @SerializedName("kategori_list") val kategoriList: List<String>?,
    @SerializedName("motivasi") val motivasi: List<MotivasiItem>? = null
)
