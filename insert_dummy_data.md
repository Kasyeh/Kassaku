# Insert Data Dummy ke Database Laravel

## Cara 1: Via Tinker (Paling Mudah)

Di terminal Laravel project:

```bash
php artisan tinker
```

Lalu jalankan:

```php
// Insert beberapa transaksi dummy untuk user_id = 1
DB::table('transaksi')->insert([
    [
        'id_user' => 1,
        'tipe' => 'pemasukan',
        'nominal' => 5000000,
        'kategori' => 'Gaji',
        'tanggal' => now()->format('Y-m-d'),
        'created_at' => now(),
        'updated_at' => now(),
    ],
    [
        'id_user' => 1,
        'tipe' => 'pengeluaran',
        'nominal' => 150000,
        'kategori' => 'Makan',
        'tanggal' => now()->format('Y-m-d'),
        'created_at' => now(),
        'updated_at' => now(),
    ],
    [
        'id_user' => 1,
        'tipe' => 'pengeluaran',
        'nominal' => 50000,
        'kategori' => 'Transportasi',
        'tanggal' => now()->subDay()->format('Y-m-d'),
        'created_at' => now()->subDay(),
        'updated_at' => now()->subDay(),
    ],
    [
        'id_user' => 1,
        'tipe' => 'pemasukan',
        'nominal' => 1000000,
        'kategori' => 'Bonus',
        'tanggal' => now()->subDays(2)->format('Y-m-d'),
        'created_at' => now()->subDays(2),
        'updated_at' => now()->subDays(2),
    ],
]);

// Cek berhasil atau tidak
DB::table('transaksi')->where('id_user', 1)->count();
```

## Cara 2: Via Raw SQL

Jalankan query SQL langsung di database client:

```sql
INSERT INTO transaksi (id_user, tipe, nominal, kategori, tanggal, created_at, updated_at) VALUES
(1, 'pemasukan', 5000000, 'Gaji', CURDATE(), NOW(), NOW()),
(1, 'pengeluaran', 150000, 'Makan', CURDATE(), NOW(), NOW()),
(1, 'pengeluaran', 50000, 'Transportasi', DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW()),
(1, 'pemasukan', 1000000, 'Bonus', DATE_SUB(CURDATE(), INTERVAL 2 DAY), NOW(), NOW());
```

## Cara 3: Via Seeder (Untuk Production)

Buat seeder:

```bash
php artisan make:seeder TransaksiSeeder
```

Edit file `database/seeders/TransaksiSeeder.php`:

```php
<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class TransaksiSeeder extends Seeder
{
    public function run()
    {
        DB::table('transaksi')->insert([
            [
                'id_user' => 1,
                'tipe' => 'pemasukan',
                'nominal' => 5000000,
                'kategori' => 'Gaji',
                'tanggal' => Carbon::now()->format('Y-m-d'),
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'id_user' => 1,
                'tipe' => 'pengeluaran',
                'nominal' => 150000,
                'kategori' => 'Makan',
                'tanggal' => Carbon::now()->format('Y-m-d'),
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            // ... tambah lebih banyak jika perlu
        ]);
    }
}
```

Jalankan:
```bash
php artisan db:seed --class=TransaksiSeeder
```

## Test Setelah Insert

```bash
# Via curl
curl -X GET "http://localhost:8000/api/riwayat/1" -H "Accept: application/json"

# Via tinker
php artisan tinker
DB::table('transaksi')->where('id_user', 1)->get();
```

## Jika Menggunakan User ID Berbeda

Ganti `id_user => 1` dengan user ID yang Anda gunakan untuk login!

Cek user ID dari login response atau dari database:
```php
DB::table('users')->select('id', 'username')->get();
```
