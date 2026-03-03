# Filter Riwayat Transaksi - Dokumentasi

## Fitur yang Ditambahkan

### 1. Filter Kategori (Tipe Transaksi)
- **Semua**: Menampilkan semua transaksi (default)
- **Pemasukan**: Hanya menampilkan transaksi pemasukan
- **Pengeluaran**: Hanya menampilkan transaksi pengeluaran

Filter kategori ditampilkan sebagai **FilterChips** di bagian atas list riwayat yang dapat diklik untuk beralih antar kategori.

### 2. Filter Tanggal
- **Rentang Tanggal**: Memilih tanggal mulai dan tanggal akhir
- **Format**: YYYY-MM-DD (contoh: 2024-08-01)
- **Fleksibel**: 
  - Kosongkan keduanya = tampilkan semua tanggal
  - Isi tanggal mulai saja = tampilkan transaksi dari tanggal tersebut hingga sekarang
  - Isi tanggal akhir saja = tampilkan transaksi sampai tanggal tersebut
  - Isi keduanya = tampilkan transaksi dalam rentang yang ditentukan

Filter tanggal diakses melalui:
- **Icon filter** (☰) di TopAppBar → membuka dialog filter tanggal
- **Chip tanggal aktif** ditampilkan jika filter tanggal sudah dipilih, dengan tombol X untuk menghapus filter

## Cara Menggunakan

### Menggunakan Filter Kategori
1. Scroll ke bagian atas list riwayat
2. Klik chip "Semua", "Pemasukan", atau "Pengeluaran"
3. List akan otomatis difilter sesuai pilihan

### Menggunakan Filter Tanggal
1. Klik icon filter (☰) di pojok kanan atas
2. Dialog akan muncul dengan 2 field input:
   - **Tanggal Mulai**: Masukkan tanggal awal (opsional)
   - **Tanggal Akhir**: Masukkan tanggal akhir (opsional)
3. Klik "Terapkan" untuk mengaktifkan filter
4. Chip tanggal akan muncul di bawah filter kategori
5. Untuk menghapus filter tanggal, klik icon X pada chip tanggal

### Kombinasi Filter
Kedua filter bekerja bersamaan:
- Contoh: Filter "Pemasukan" + tanggal "2024-08-01" sampai "2024-08-31" = menampilkan hanya pemasukan di bulan Agustus 2024

## Komponen yang Ditambahkan

### 1. FilterChipsRow
Komponen yang menampilkan:
- 3 chip untuk kategori (Semua, Pemasukan, Pengeluaran)
- 1 chip untuk menampilkan filter tanggal aktif (jika ada)

### 2. FilterDialog
Dialog untuk memilih rentang tanggal dengan:
- Input field untuk tanggal mulai
- Input field untuk tanggal akhir
- Tombol "Terapkan" dan "Batal"
- Instruksi penggunaan

## State Management

Filter menggunakan state lokal di RiwayatScreen:
- `selectedFilterType`: String untuk kategori yang dipilih
- `startDateFilter`: String? untuk tanggal mulai
- `endDateFilter`: String? untuk tanggal akhir
- `showFilterDialog`: Boolean untuk menampilkan/menyembunyikan dialog

## Logic Filtering

Filter diterapkan pada data sebelum ditampilkan:
1. **Filter Kategori**: Membandingkan field `tipe` dengan pilihan ("pemasukan"/"pengeluaran")
2. **Filter Tanggal**: Membandingkan field `tanggal` dengan rentang yang dipilih menggunakan string comparison
3. **Kombinasi**: Hanya item yang memenuhi KEDUA filter yang ditampilkan

## UI/UX
- Filter chips selalu tampil di atas list (kecuali saat list kosong)
- Icon filter di TopAppBar untuk akses cepat ke filter tanggal
- Visual feedback: chip yang dipilih akan highlighted
- Pesan yang berbeda ditampilkan jika:
  - Tidak ada transaksi sama sekali
  - Tidak ada transaksi yang sesuai filter

## Perhatian
- Format tanggal harus sesuai dengan format di database (YYYY-MM-DD)
- Filtering dilakukan di client-side (tidak memerlukan perubahan backend)
- Filter kategori case-insensitive untuk keamanan
