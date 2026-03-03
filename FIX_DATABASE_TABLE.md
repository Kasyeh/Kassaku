# Fix: Table 'transaksi' Tidak Ada

## Error:
```
Table 'KasSaku.transaksi' doesn't exist
```

## Solusi

### Langkah 1: Cek Table yang Ada di Database

Di tinker:
```php
DB::select('SHOW TABLES');
```

Atau via MySQL/MariaDB:
```bash
mysql -u root -p
USE KasSaku;
SHOW TABLES;
```

---

### Langkah 2A: Jika Table dengan Nama Berbeda Ada (misal 'transactions')

Update model Laravel untuk menggunakan nama table yang benar.

Edit `app/Models/TransactionModel.php`:
```php
protected $table = 'transactions'; // Atau nama table yang sebenarnya
protected $primaryKey = 'id'; // Atau 'id_transaksi' jika primary key berbeda
```

---

### Langkah 2B: Jika Table Belum Ada - Buat Migration

#### 1. Buat Migration File:
```bash
php artisan make:migration create_transaksi_table
```

#### 2. Edit Migration File di `database/migrations/xxxx_create_transaksi_table.php`:

```php
<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('transaksi', function (Blueprint $table) {
            $table->id('id_transaksi'); // Primary key dengan nama custom
            $table->unsignedBigInteger('id_user'); // Foreign key ke users
            $table->enum('tipe', ['pemasukan', 'pengeluaran']);
            $table->decimal('nominal', 15, 2); // 15 digit, 2 desimal
            $table->string('kategori', 100);
            $table->date('tanggal');
            $table->timestamps(); // created_at & updated_at
            
            // Foreign key constraint (opsional)
            $table->foreign('id_user')->references('id')->on('users')->onDelete('cascade');
            
            // Index untuk performa query
            $table->index('id_user');
            $table->index('tanggal');
        });
    }

    public function down()
    {
        Schema::dropIfExists('transaksi');
    }
};
```

#### 3. Jalankan Migration:
```bash
php artisan migrate
```

Jika ada error foreign key (table users belum ada), jalankan dulu migration users:
```bash
php artisan migrate --path=/database/migrations/xxxx_create_users_table.php
php artisan migrate
```

---

### Langkah 2C: Atau Buat Table Manual via SQL

Jika tidak mau pakai migration, jalankan SQL ini langsung:

```sql
CREATE TABLE `transaksi` (
  `id_transaksi` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `id_user` bigint(20) unsigned NOT NULL,
  `tipe` enum('pemasukan','pengeluaran') NOT NULL,
  `nominal` decimal(15,2) NOT NULL,
  `kategori` varchar(100) NOT NULL,
  `tanggal` date NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id_transaksi`),
  KEY `transaksi_id_user_index` (`id_user`),
  KEY `transaksi_tanggal_index` (`tanggal`),
  CONSTRAINT `transaksi_id_user_foreign` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Setelah Table Dibuat, Insert Data Dummy

Di tinker:
```php
DB::table('transaksi')->insert([
    'id_user' => 1,
    'tipe' => 'pemasukan',
    'nominal' => 5000000,
    'kategori' => 'Gaji',
    'tanggal' => now()->format('Y-m-d'),
    'created_at' => now(),
    'updated_at' => now(),
]);

DB::table('transaksi')->insert([
    'id_user' => 1,
    'tipe' => 'pengeluaran',
    'nominal' => 150000,
    'kategori' => 'Makan',
    'tanggal' => now()->format('Y-m-d'),
    'created_at' => now(),
    'updated_at' => now(),
]);

DB::table('transaksi')->insert([
    'id_user' => 1,
    'tipe' => 'pengeluaran',
    'nominal' => 50000,
    'kategori' => 'Transportasi',
    'tanggal' => now()->subDay()->format('Y-m-d'),
    'created_at' => now()->subDay(),
    'updated_at' => now()->subDay(),
]);

// Verify
DB::table('transaksi')->count();
DB::table('transaksi')->get();
```

---

## Cek Hasilnya

```bash
curl -X GET "http://localhost:8000/api/riwayat/1" -H "Accept: application/json"
```

Seharusnya sekarang return data transaksi!

---

## Jika Pakai Nama Table Berbeda (misal 'transactions')

Update model di `app/Models/TransactionModel.php`:

```php
<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class TransactionModel extends Model
{
    protected $table = 'transactions'; // ← Sesuaikan nama table
    protected $primaryKey = 'id'; // ← Atau 'id_transaksi'
    
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

Dan insert data dengan nama table yang benar:
```php
DB::table('transactions')->insert([...]); // Ganti 'transaksi' dengan 'transactions'
```
