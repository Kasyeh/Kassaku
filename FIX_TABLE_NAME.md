# Fix: Update Nama Table ke tb_transaksi

## Masalah
Model Laravel menggunakan nama table default, tapi actual table name adalah `tb_transaksi`.

## Solusi

### Update Model Laravel

Edit file `app/Models/TransactionModel.php`:

```php
<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class TransactionModel extends Model
{
    // ⭐ TAMBAHKAN INI - Spesifikasikan nama table yang benar
    protected $table = 'tb_transaksi';
    
    // ⭐ TAMBAHKAN INI - Jika primary key bukan 'id'
    protected $primaryKey = 'id_transaksi'; // Sesuaikan dengan column primary key Anda
    
    protected $fillable = [
        'id_user',
        'tipe',
        'nominal',
        'kategori',
        'tanggal',
    ];
    
    protected $casts = [
        'nominal' => 'float',
        'tanggal' => 'date',
    ];
}
```

---

## Insert Data Dummy

Setelah update model, di **tinker**:

```php
// Keluar & masuk tinker lagi untuk refresh model
exit
php artisan tinker

// Insert data dummy
DB::table('tb_transaksi')->insert([
    'id_user' => 1,
    'tipe' => 'pemasukan',
    'nominal' => 5000000,
    'kategori' => 'Gaji',
    'tanggal' => now()->format('Y-m-d'),
    'created_at' => now(),
    'updated_at' => now(),
]);

DB::table('tb_transaksi')->insert([
    'id_user' => 1,
    'tipe' => 'pengeluaran',
    'nominal' => 150000,
    'kategori' => 'Makan',
    'tanggal' => now()->format('Y-m-d'),
    'created_at' => now(),
    'updated_at' => now(),
]);

DB::table('tb_transaksi')->insert([
    'id_user' => 1,
    'tipe' => 'pengeluaran',
    'nominal' => 50000,
    'kategori' => 'Transportasi',
    'tanggal' => now()->subDay()->format('Y-m-d'),
    'created_at' => now()->subDay(),
    'updated_at' => now()->subDay(),
]);

DB::table('tb_transaksi')->insert([
    'id_user' => 1,
    'tipe' => 'pemasukan',
    'nominal' => 1000000,
    'kategori' => 'Bonus',
    'tanggal' => now()->subDays(2)->format('Y-m-d'),
    'created_at' => now()->subDays(2),
    'updated_at' => now()->subDays(2),
]);

// Verify data berhasil diinsert
DB::table('tb_transaksi')->count();
DB::table('tb_transaksi')->get();
```

---

## Test API

```bash
curl -X GET "http://localhost:8000/api/riwayat/1" -H "Accept: application/json"
```

Seharusnya sekarang return data transaksi!

---

## Jika Primary Key Bukan 'id_transaksi'

Cek struktur table dulu:
```php
DB::select("DESCRIBE tb_transaksi");
```

Lalu sesuaikan `$primaryKey` di model dengan nama column primary key yang sebenarnya.

### Contoh jika primary key adalah 'id':
```php
protected $primaryKey = 'id';
```

### Contoh jika primary key adalah 'id_transaksi':
```php
protected $primaryKey = 'id_transaksi';
```

---

## Struktur Table yang Diharapkan

Pastikan table `tb_transaksi` punya kolom:
- `id_transaksi` atau `id` (primary key, auto increment)
- `id_user` (bigint)
- `tipe` (varchar/enum: 'pemasukan' atau 'pengeluaran')
- `nominal` (decimal/double)
- `kategori` (varchar)
- `tanggal` (date)
- `created_at` (timestamp, nullable)
- `updated_at` (timestamp, nullable)

Cek dengan:
```php
DB::select("DESCRIBE tb_transaksi");
```
