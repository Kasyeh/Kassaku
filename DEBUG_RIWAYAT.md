# Debug Guide: Masalah Riwayat Tidak Muncul

## Masalah yang Ditemukan

Riwayat transaksi tidak muncul di HomeScreen dan RiwayatScreen.

## Kemungkinan Penyebab

### 1. **Struktur Response API Tidak Sesuai dengan Model**

Model saat ini mengharapkan response dengan pagination wrapper:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "current_page": 1,
    "data": [
      { "id_transaksi": 1, "nominal": 5000, ... }
    ],
    "total": 10,
    ...
  }
}
```

Namun backend Anda mungkin mengembalikan response langsung:
```json
{
  "success": true,
  "message": "Success",
  "data": [
    { "id_transaksi": 1, "nominal": 5000, ... },
    { "id_transaksi": 2, "nominal": 3000, ... }
  ]
}
```

### 2. **API Endpoint Tidak Aktif**
- Backend Laravel mungkin tidak running
- URL base `http://10.0.2.2:8000/api/` hanya untuk emulator Android
- Untuk device fisik, gunakan IP lokal komputer

### 3. **UserId Tidak Valid**
- UserId yang diterima mungkin 0 atau null

## Perubahan yang Telah Dilakukan

### 1. Menambahkan Logging Lengkap
File yang diubah:
- `TransactionRepository.kt` - Log detail response API
- `HomeViewModel.kt` - Log proses fetching
- `HomeScreen.kt` - Log lifecycle dan state changes
- `RiwayatScreen.kt` - Log lifecycle dan state changes

### 2. Menambahkan Response Model Alternatif
- `RiwayatResponseDirect` - untuk handle response array langsung
- Fallback mechanism di repository

### 3. Error Handling Lebih Baik
- Log error body dari API
- Log exception dengan stack trace

## Cara Debug

### Langkah 1: Jalankan Aplikasi dan Periksa Logcat

Buka Logcat di Android Studio dan filter dengan tag berikut:
```
TransactionRepo
HomeViewModel
HomeScreen
RiwayatScreen
```

### Langkah 2: Periksa Log yang Muncul

#### A. Jika melihat log seperti ini:
```
TransactionRepo: Response body (paginated): RiwayatResponse(success=true, message=..., dataPage=null)
TransactionRepo: DataPage null, trying direct response...
```
**Artinya**: API mengembalikan array langsung, bukan pagination wrapper.

**Solusi**: Response model sudah ditangani dengan fallback.

#### B. Jika melihat log seperti ini:
```
TransactionRepo: Response not successful: 404 - Not Found
TransactionRepo: Error body: {"error": "User not found"}
```
**Artinya**: Endpoint tidak ditemukan atau userId salah.

**Solusi**: 
- Periksa apakah backend berjalan di `http://localhost:8000`
- Cek route di Laravel: `php artisan route:list`
- Pastikan endpoint `/api/riwayat/{id}` ada

#### C. Jika melihat log seperti ini:
```
TransactionRepo: Exception: Failed to connect to /10.0.2.2:8000
```
**Artinya**: Backend tidak running atau tidak accessible.

**Solusi**:
- Jalankan backend Laravel: `php artisan serve`
- Untuk device fisik, ganti base URL di `ApiService.kt`:
  ```kotlin
  private const val BASE_URL = "http://192.168.x.x:8000/api/"
  ```
  Ganti `192.168.x.x` dengan IP komputer Anda.

#### D. Jika melihat log seperti ini:
```
HomeScreen: userId is 0 or invalid!
```
**Artinya**: UserId tidak valid setelah login.

**Solusi**: Periksa response login di `LoginViewModel`.

### Langkah 3: Test API Langsung

Gunakan Postman atau curl untuk test API:

```bash
# Test endpoint riwayat
curl -X GET http://localhost:8000/api/riwayat/1

# Atau dengan verbose
curl -v http://localhost:8000/api/riwayat/1
```

Lihat struktur response yang dikembalikan.

### Langkah 4: Jika API Mengembalikan Structure Berbeda

#### Scenario A: API mengembalikan data langsung tanpa wrapper
Response:
```json
[
  { "id_transaksi": 1, "nominal": 5000, ... }
]
```

Ubah `ApiService.kt` dan `TransactionRepository.kt` untuk handle ini.

#### Scenario B: Field names berbeda
Jika API menggunakan nama field seperti `id` bukan `id_transaksi`, update `@SerializedName` di `RiwayatItem`.

## Test Backend Laravel

Pastikan backend memiliki endpoint ini di `routes/api.php`:

```php
Route::get('/riwayat/{id}', [TransaksiController::class, 'getRiwayat']);
```

Dan controller method:
```php
public function getRiwayat($userId)
{
    $transaksi = Transaksi::where('id_user', $userId)
        ->orderBy('tanggal', 'desc')
        ->get();
    
    return response()->json([
        'success' => true,
        'message' => 'Data retrieved successfully',
        'data' => $transaksi  // atau 'data' => ['data' => $transaksi] jika pakai pagination
    ]);
}
```

## Checklist Debug

- [ ] Backend Laravel running di `http://localhost:8000`
- [ ] Route `/api/riwayat/{id}` exists
- [ ] API mengembalikan status 200 saat di-curl
- [ ] Structure response API sesuai dengan model
- [ ] UserId valid dan tidak 0
- [ ] Logcat menunjukkan log dari TransactionRepo
- [ ] Logcat menunjukkan "Success with X items"

## Jika Masih Bermasalah

1. Share screenshot Logcat (filter dengan tag di atas)
2. Share response dari curl/Postman
3. Share kode backend controller untuk endpoint riwayat

## Quick Fix: Test dengan Data Mock

Jika ingin test UI dulu tanpa backend, uncomment/gunakan `FakeHomeViewModel` di `HomeScreen.kt` untuk preview.
