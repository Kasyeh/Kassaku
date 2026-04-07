package com.example.kassaku.data.remote

import com.example.kassaku.data.remote.model.*
import okhttp3.OkHttpClient
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

    @GET("user/{id}/saldo")
    suspend fun getUserBalance(@Path("id") userId: Int): Response<BalanceResponse>

    @GET("riwayat/{id}")
    suspend fun getRiwayatTransaksi(
        @Path("id") userId: Int,
        @Query("periode") periode: String? = null,
        @Query("jenis") jenis: String? = null,
        @Query("tanggal") tanggal: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("page") page: Int? = null
    ): Response<RiwayatResponse>

    @GET("riwayat/{id}")
    suspend fun getRiwayatTransaksiDirect(
        @Path("id") userId: Int,
        @Query("periode") periode: String? = null,
        @Query("jenis") jenis: String? = null,
        @Query("tanggal") tanggal: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("page") page: Int? = null
    ): Response<RiwayatResponseDirect>

    @FormUrlEncoded
    @POST("pemasukan/tambah")
    suspend fun tambahPemasukan(
        @Field("id_user") idUser: Int,
        @Field("nominal") nominal: Long,
        @Field("kategori") kategori: String,
        @Field("keterangan") keterangan: String?,
        @Field("tanggal") tanggal: String?
    ): PemasukanResponse

    @FormUrlEncoded
    @POST("pengeluaran/tambah")
    suspend fun tambahPengeluaran(
        @Field("id_user") idUser: Int,
        @Field("nominal") nominal: Long,
        @Field("kategori") kategori: String,
        @Field("keterangan") keterangan: String?,
        @Field("tanggal") tanggal: String?
    ): PengeluaranResponse

    @GET("impian/{id_user}")
    suspend fun getImpian(
        @Path("id_user") userId: Int
    ): Response<ImpianResponse>

    @Multipart
    @POST("impian/tambah")
    suspend fun tambahImpian(
        @Part("id_user") idUser: okhttp3.RequestBody,
        @Part("nama_barang") namaBarang: okhttp3.RequestBody,
        @Part("harga_barang") hargaBarang: okhttp3.RequestBody,
        @Part("deadline") deadline: okhttp3.RequestBody,
        @Part("keterangan") keterangan: okhttp3.RequestBody?,
        @Part fotoBarang: okhttp3.MultipartBody.Part  // ✅ Tanpa nama & tidak nullable
    ): Response<TambahImpianResponse>

    // Export PDF Riwayat Transaksi
    @GET("riwayat/{id_user}/export-pdf")
    @Streaming  // Penting untuk download file besar
    suspend fun exportPdf(
        @Path("id_user") userId: Int,
        @Query("periode") periode: String? = null,      // hari_ini, minggu_ini, bulan_ini, semua
        @Query("tanggal") tanggal: String? = null,      // format: yyyy-MM-dd
        @Query("bulan") bulan: Int? = null,             // 1-12
        @Query("tahun") tahun: Int? = null              // YYYY
    ): Response<okhttp3.ResponseBody>

    @GET("user/{id}/statistik")
    suspend fun getStatistik(@Path("id") userId: Int): Response<StatistikResponse>

    @FormUrlEncoded
    @POST("target-pengeluaran/simpan")
    suspend fun simpanTargetPengeluaran(
        @Field("id_user") idUser: Int,
        @Field("target_pengeluaran") targetPengeluaran: Long
    ): Response<TargetPengeluaranResponse>

    @FormUrlEncoded
    @POST("user/reset-saldo")
    suspend fun resetSaldo(
        @Field("id_user") idUser: Int,
        @Field("password") password: String
    ): Response<ResetSaldoResponse>

    @FormUrlEncoded
    @POST("impian/hapus/{id_impian}")
    suspend fun hapusImpian(
        @Path("id_impian") idImpian: Long,
        @Field("id_user") idUser: Int,
        @Field("password") password: String
    ): Response<HapusImpianResponse>

    @FormUrlEncoded
    @POST("impian/{id_impian}/setoran")
    suspend fun setorImpian(
        @Path("id_impian") idImpian: Long,
        @Field("id_user") idUser: Int,
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
        @Field("id_user") idUser: Int,
        @Field("pesan") pesan: String,
        @Field("fcm_token") fcmToken: String? = null
    ): Response<UnblockRequestResponse>

    @GET("user/{id}/budget-kategori")
    suspend fun getBudgetKategori(@Path("id") userId: Int): Response<BudgetKategoriResponse>

    @FormUrlEncoded
    @POST("user/budget-kategori/simpan")
    suspend fun simpanBudgetKategori(
        @Field("id_user") idUser: Int,
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
        @Field("id_user") idUser: Int,
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
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
