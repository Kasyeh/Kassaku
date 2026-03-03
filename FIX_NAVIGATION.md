# Fix Masalah Navigasi "Lihat Semua"

## Masalah yang Ditemukan

Ketika user menekan tombol **"Lihat Semua"** di bagian Transaksi Terbaru pada Home screen, kemudian tidak bisa kembali ke halaman Home melalui bottom navigation bar.

## Penyebab Masalah

Navigasi pada tombol "Lihat Semua" hanya menggunakan:
```kotlin
navController.navigate(BottomNavItem.Riwayat.route)
```

Sedangkan bottom navigation bar menggunakan konfigurasi lengkap dengan:
- `popUpTo()` - untuk mengelola back stack
- `launchSingleTop` - menghindari duplikasi destination
- `saveState` & `restoreState` - menyimpan dan restore state screen

Karena perbedaan ini, navigation stack tidak konsisten dan menyebabkan masalah navigasi.

## Solusi

Mengubah navigasi tombol "Lihat Semua" agar menggunakan konfigurasi yang sama dengan bottom navigation:

```kotlin
navController.navigate(BottomNavItem.Riwayat.route) {
    // Pop up to start destination untuk mengelola back stack
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
    // Hindari multiple copies dari destination yang sama
    launchSingleTop = true
    // Restore state saat memilih item yang sudah dipilih sebelumnya
    restoreState = true
}
```

## File yang Dimodifikasi

**File**: `/app/src/main/java/com/example/kassaku/ui/HomeScreen.kt`

### Perubahan:
1. ✅ Menambahkan import `NavGraph.Companion.findStartDestination`
2. ✅ Mengupdate navigasi "Lihat Semua" dengan konfigurasi lengkap

## Penjelasan Konfigurasi Navigasi

### `popUpTo()`
Membersihkan back stack sampai ke destination tertentu. Dengan menggunakan `findStartDestination()`, kita pop sampai ke start destination (Home), sehingga tidak ada duplikasi screen di back stack.

### `saveState = true`
Menyimpan state dari destination yang di-pop, sehingga jika user kembali ke screen tersebut, state-nya akan di-restore (scroll position, form input, dll).

### `launchSingleTop = true`
Memastikan hanya ada satu instance dari destination di back stack. Jika destination sudah ada di top, tidak akan membuat instance baru.

### `restoreState = true`
Merestore state yang sudah disimpan sebelumnya saat navigasi ke destination yang sama.

## Hasil

✅ User sekarang bisa:
1. Klik "Lihat Semua" dari Home → Pindah ke Riwayat
2. Klik tab "Home" di bottom nav → Kembali ke Home dengan lancar
3. Navigation state tetap terjaga dengan baik
4. Tidak ada duplikasi screen di back stack

## Best Practice

Untuk aplikasi dengan bottom navigation, **selalu gunakan konfigurasi navigasi yang konsisten** di semua tempat:
- Dari bottom nav items
- Dari button/link yang mengarah ke bottom nav destination
- Dari deep links

Ini memastikan:
- ✅ Back stack terkelola dengan baik
- ✅ State preservation konsisten
- ✅ UX yang smooth dan predictable
- ✅ Tidak ada memory leaks dari multiple instances
