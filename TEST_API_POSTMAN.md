# Test API dengan Postman

## Endpoint: Get Riwayat Transaksi

### Request Details

**Method:** `GET`

**URL:** `http://localhost:8000/api/riwayat/1`

Ganti `1` dengan user ID yang Anda gunakan.

**Headers:**
```
Accept: application/json
Content-Type: application/json
```

---

## Cara Setup di Postman

### 1. Buka Postman

### 2. Create New Request
- Klik **New** → **HTTP Request**

### 3. Set Request Type
- Pilih **GET** dari dropdown

### 4. Masukkan URL
```
http://localhost:8000/api/riwayat/1
```

### 5. Set Headers
Klik tab **Headers**, tambahkan:

| Key | Value |
|-----|-------|
| Accept | application/json |
| Content-Type | application/json |

### 6. (Optional) Query Parameters
Jika mau filter berdasarkan periode, klik tab **Params**, tambahkan:

| Key | Value | Description |
|-----|-------|-------------|
| periode | semua | Default (semua transaksi) |
| periode | hari_ini | Transaksi hari ini |
| periode | minggu_ini | Transaksi minggu ini |
| periode | bulan_ini | Transaksi bulan ini |

Contoh dengan parameter:
```
http://localhost:8000/api/riwayat/1?periode=hari_ini
```

### 7. Send Request
Klik tombol **Send**

---

## Expected Response

### Jika Ada Data:
```json
{
    "success": true,
    "message": "Data riwayat transaksi berhasil diambil",
    "dataPage": {
        "riwayatItems": [
            {
                "id_transaksi": 1,
                "id_user": 1,
                "tipe": "pemasukan",
                "nominal": 5000000,
                "kategori": "Gaji",
                "tanggal": "2025-11-01",
                "created_at": "2025-11-01 10:00:00",
                "updated_at": "2025-11-01 10:00:00"
            },
            {
                "id_transaksi": 2,
                "id_user": 1,
                "tipe": "pengeluaran",
                "nominal": 150000,
                "kategori": "Makan",
                "tanggal": "2025-11-01",
                "created_at": "2025-11-01 10:05:00",
                "updated_at": "2025-11-01 10:05:00"
            }
        ]
    }
}
```

### Jika Database Kosong:
```json
{
    "success": true,
    "message": "Data riwayat transaksi berhasil diambil",
    "dataPage": {
        "riwayatItems": []
    }
}
```

### Jika Error:
```json
{
    "success": false,
    "message": "Error message here",
    "dataPage": null
}
```

---

## Test Endpoints Lainnya

### 1. Login
**Method:** `POST`  
**URL:** `http://localhost:8000/api/login`  
**Body (x-www-form-urlencoded):**
```
username: your_username
password: your_password
```

### 2. Get Balance
**Method:** `GET`  
**URL:** `http://localhost:8000/api/user/1/saldo`

### 3. Tambah Pemasukan
**Method:** `POST`  
**URL:** `http://localhost:8000/api/pemasukan/tambah`  
**Body (x-www-form-urlencoded):**
```
id_user: 1
nominal: 1000000
kategori: Gaji
```

### 4. Tambah Pengeluaran
**Method:** `POST`  
**URL:** `http://localhost:8000/api/pengeluaran/tambah`  
**Body (x-www-form-urlencoded):**
```
id_user: 1
nominal: 50000
kategori: Makan
```

---

## Troubleshooting

### Error: "Could not get any response"
- Pastikan Laravel server running: `php artisan serve`
- Cek Laravel berjalan di port 8000

### Error: 404 Not Found
- Cek route: `php artisan route:list | grep riwayat`
- Pastikan endpoint benar: `/api/riwayat/{id}`

### Error: 500 Internal Server Error
- Cek log Laravel: `storage/logs/laravel.log`
- Pastikan model `TransactionModel` sudah benar
- Pastikan `protected $table = 'tb_transaksi';` sudah ada di model

### Response Success tapi Data Kosong `[]`
- Database belum ada data
- Insert data dummy via tinker (lihat FIX_TABLE_NAME.md)

---

## Save Request di Postman

Setelah berhasil:
1. Klik **Save** di pojok kanan atas
2. Beri nama: "Get Riwayat Transaksi"
3. Buat Collection: "KasSaku API"
4. Save

Sehingga bisa digunakan lagi nanti!

---

## Export Postman Collection (Optional)

Setelah buat semua request, bisa export collection:
1. Klik **...** di collection "KasSaku API"
2. **Export**
3. Format: Collection v2.1
4. Save file JSON

Bisa di-share ke tim atau di-import lagi nanti!
