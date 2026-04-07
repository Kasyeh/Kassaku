package com.example.kassaku.data.remote

import com.example.kassaku.data.remote.model.*
import com.example.kassaku.utils.ForceLogoutManager
import com.example.kassaku.viewmodel.LogoutReason
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// --- API SERVICE INTERFACE ---

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<RegisterResponse>

    @GET("me/saldo")
    suspend fun getMyBalance(): Response<BalanceResponse>

    @GET("me/riwayat")
    suspend fun getMyRiwayatTransaksi(
        @Query("periode") periode: String? = null,
        @Query("jenis") jenis: String? = null,
        @Query("search") search: String? = null,
        @Query("tanggal") tanggal: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("page") page: Int? = null
    ): Response<RiwayatResponse>

    @GET("me/riwayat")
    suspend fun getMyRiwayatTransaksiDirect(
        @Query("periode") periode: String? = null,
        @Query("jenis") jenis: String? = null,
        @Query("search") search: String? = null,
        @Query("tanggal") tanggal: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("page") page: Int? = null
    ): Response<RiwayatResponseDirect>

    @FormUrlEncoded
    @POST("pemasukan/tambah")
    suspend fun tambahPemasukan(
        @Field("nominal") nominal: Long,
        @Field("kategori") kategori: String,
        @Field("keterangan") keterangan: String?,
        @Field("tanggal") tanggal: String?
    ): PemasukanResponse

    @FormUrlEncoded
    @POST("pengeluaran/tambah")
    suspend fun tambahPengeluaran(
        @Field("nominal") nominal: Long,
        @Field("kategori") kategori: String,
        @Field("keterangan") keterangan: String?,
        @Field("tanggal") tanggal: String?
    ): PengeluaranResponse

    @GET("me/impian")
    suspend fun getMyImpian(): Response<ImpianResponse>

    @Multipart
    @POST("impian/tambah")
    suspend fun tambahImpian(
        @Part("nama_barang") namaBarang: okhttp3.RequestBody,
        @Part("harga_barang") hargaBarang: okhttp3.RequestBody,
        @Part("deadline") deadline: okhttp3.RequestBody,
        @Part("keterangan") keterangan: okhttp3.RequestBody?,
        @Part fotoBarang: okhttp3.MultipartBody.Part  // ✅ Tanpa nama & tidak nullable
    ): Response<TambahImpianResponse>

    // Export PDF Riwayat Transaksi
    @GET("me/riwayat/export-pdf")
    @Streaming  // Penting untuk download file besar
    suspend fun exportMyPdf(
        @Query("periode") periode: String? = null,      // hari_ini, minggu_ini, bulan_ini, semua
        @Query("tipe") tipe: String? = null,
        @Query("search") search: String? = null,
        @Query("tanggal") tanggal: String? = null,      // format: yyyy-MM-dd
        @Query("bulan") bulan: Int? = null,             // 1-12
        @Query("tahun") tahun: Int? = null              // YYYY
    ): Response<okhttp3.ResponseBody>

    @GET("me/statistik")
    suspend fun getMyStatistik(): Response<StatistikResponse>

    @FormUrlEncoded
    @POST("target-pengeluaran/simpan")
    suspend fun simpanTargetPengeluaran(
        @Field("target_pengeluaran") targetPengeluaran: Long
    ): Response<TargetPengeluaranResponse>

    @FormUrlEncoded
    @POST("user/reset-saldo")
    suspend fun resetSaldo(
        @Field("password") password: String
    ): Response<ResetSaldoResponse>

    @FormUrlEncoded
    @POST("impian/hapus/{id_impian}")
    suspend fun hapusImpian(
        @Path("id_impian") idImpian: Long,
        @Field("password") password: String
    ): Response<HapusImpianResponse>

    @FormUrlEncoded
    @POST("impian/{id_impian}/setoran")
    suspend fun setorImpian(
        @Path("id_impian") idImpian: Long,
        @Field("nominal") nominal: Long,
        @Field("keterangan") keterangan: String?,
        @Field("tanggal") tanggal: String? = null
    ): Response<SetorImpianResponse>

    // FCM Token Management
    @FormUrlEncoded
    @POST("fcm-token")
    suspend fun saveFcmToken(
        @Field("token") token: String,
        @Field("user_id") userId: Int
    ): Response<FcmTokenResponse>

    @FormUrlEncoded
    @POST("unblock-request")
    suspend fun submitUnblockRequest(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("pesan") pesan: String,
        @Field("fcm_token") fcmToken: String? = null
    ): Response<UnblockRequestResponse>

    @GET("me/budget-kategori")
    suspend fun getMyBudgetKategori(): Response<BudgetKategoriResponse>

    @FormUrlEncoded
    @POST("user/budget-kategori/simpan")
    suspend fun simpanBudgetKategori(
        @Field("kategori") kategori: String,
        @Field("nominal") nominal: Long,
        @Field("periode") periode: String,
        @Field("tanggal_mulai") tanggalMulai: String? = null,
        @Field("tanggal_akhir") tanggalAkhir: String? = null
    ): Response<SimpanBudgetResponse>

    @FormUrlEncoded
    @POST("user/budget-kategori/hapus/{id}")
    suspend fun hapusBudgetKategori(
        @Path("id") budgetId: Int,
        @Field("password") password: String
    ): Response<HapusBudgetResponse>
}

// --- API CLIENT OBJECT ---

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8000/api/"

    // Token holder — set setelah login/register berhasil
    @Volatile
    private var authToken: String? = null

    fun setToken(token: String?) {
        authToken = token
    }

    fun clearToken() {
        authToken = null
    }

    fun getToken(): String? = authToken

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .addHeader("Accept", "application/json")

            // Tambahkan Authorization header jika token tersedia
            authToken?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            val code = response.code

            if (code != 401 && code != 403) {
                return@addInterceptor response
            }

            val bodyString = response.body?.string().orEmpty()
            if (shouldForceLogout(bodyString)) {
                ForceLogoutManager.trigger(LogoutReason.BLOCKED)
            }

            response.newBuilder()
                .body(bodyString.toResponseBody(response.body?.contentType()))
                .build()
        }
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun shouldForceLogout(rawBody: String): Boolean {
        if (rawBody.isBlank()) {
            return false
        }

        return try {
            val json = JsonParser.parseString(rawBody).asJsonObject
            isForceLogoutValueTrue(json, "force_logout") ||
                json.getAsJsonObjectOrNull("content")?.let { isForceLogoutValueTrue(it, "force_logout") } == true ||
                json.getAsJsonObjectOrNull("data")?.let { isForceLogoutValueTrue(it, "force_logout") } == true
        } catch (_: Exception) {
            false
        }
    }

    private fun isForceLogoutValueTrue(jsonObject: JsonObject, key: String): Boolean {
        val value = jsonObject.get(key) ?: return false
        return when {
            value.isJsonPrimitive && value.asJsonPrimitive.isBoolean -> value.asBoolean
            value.isJsonPrimitive && value.asJsonPrimitive.isString -> value.asString.equals("true", ignoreCase = true)
            else -> false
        }
    }

    private fun JsonObject.getAsJsonObjectOrNull(memberName: String): JsonObject? {
        val member = get(memberName) ?: return null
        return if (member.isJsonObject) member.asJsonObject else null
    }
}
