# Dokumentasi Perubahan Warna Aplikasi

## Ringkasan Perubahan

Aplikasi telah diperbarui dengan skema warna baru:
- **Warna Primer**: #56DFCF (Tosca/Cyan Cerah)
- **Warna Secondary**: #D32F2F (Merah untuk Pengeluaran)
- **Warna Accent**: #FFEDF3 (Pink Muda untuk Surface/Background)

## Detail Perubahan

### 1. File Color.kt
**Path**: `/app/src/main/java/com/example/kassaku/ui/theme/Color.kt`

**Perubahan**:
- ✅ `TealPrimary` → `ToscaPrimary` (0xFF56DFCF)
- ✅ Menambahkan `PinkAccent` (0xFFFFEDF3)
- ✅ `LightBackground` → warna dengan nuansa pink (0xFFFFFBFC)
- ✅ `LightSurfaceVariant` → menggunakan `PinkAccent`

### 2. File Theme.kt
**Path**: `/app/src/main/java/com/example/kassaku/ui/theme/Theme.kt`

**Perubahan Light Theme**:
- ✅ `primary` = ToscaPrimary (warna utama tosca)
- ✅ `secondary` = SoftRed (merah untuk pengeluaran)
- ✅ `onSecondary` = Color.White (text putih pada merah)

**Perubahan Dark Theme**:
- ✅ `primary` = ToscaPrimary (tetap tosca di dark mode)
- ✅ `secondary` = SoftRed (merah untuk pengeluaran)

### 3. File LoginScreen.kt
**Path**: `/app/src/main/java/com/example/kassaku/ui/LoginScreen.kt`

**Perubahan**:
- ✅ Gradient background: Menggunakan `MaterialTheme.colorScheme.primary` dengan transparansi
- ✅ Icon username & password: Menggunakan warna primary dari tema
- ✅ TextField borders & labels: Menggunakan warna primary saat focus
- ✅ Button login: Menggunakan warna primary dari tema
- ✅ Link "Daftar Sekarang": Menggunakan warna primary dari tema

### 4. File HomeScreen.kt
**Path**: `/app/src/main/java/com/example/kassaku/ui/HomeScreen.kt`

**Perubahan**:
- ✅ Gradient balance card: Menggunakan warna primary dengan transparansi
- ✅ Warna pemasukan: Menggunakan `MaterialTheme.colorScheme.primary` (tosca)
- ✅ InfoChip pemasukan: Menggunakan warna primary
- ✅ Transaction list - warna pemasukan: Menggunakan warna primary

### 5. File RiwayatScreen.kt
**Path**: `/app/src/main/java/com/example/kassaku/ui/RiwayatScreen.kt`

**Perubahan**:
- ✅ toscaColor: Menggunakan `MaterialTheme.colorScheme.primary` dari tema
- ✅ Semua item riwayat pemasukan: Otomatis menggunakan warna primary

## Penggunaan Warna Dalam Aplikasi

### Warna Tosca (#56DFCF) - Primary
Digunakan untuk:
- **Button utama** (Login, Pemasukan)
- **Gradient background** (Login Screen, Balance Card)
- **Icon dan borders** saat focused
- **Transaksi pemasukan** (income)
- **Filter chips** yang aktif
- **Link dan accent text**

### Warna Merah (#D32F2F) - Secondary/Error
Digunakan untuk:
- **Button pengeluaran**
- **Transaksi pengeluaran** (expense)
- **Error messages**
- **Validasi form**

### Warna Pink Muda (#FFEDF3) - Accent
Digunakan untuk:
- **Card backgrounds**
- **Surface variants** (chips, cards)
- **Background dengan nuansa pink**
- **Subtle accents**

## Konsistensi Tema

Semua warna hardcoded telah diubah menjadi menggunakan `MaterialTheme.colorScheme.*` untuk memastikan:
- ✅ Konsistensi warna di seluruh aplikasi
- ✅ Dukungan dark mode otomatis
- ✅ Mudah diubah dari satu tempat (Theme.kt)
- ✅ Mengikuti Material Design 3 guidelines

## Cara Mengubah Warna di Masa Depan

Jika ingin mengubah warna aplikasi lagi, cukup edit 2 file:

1. **Color.kt**: Ubah nilai `ToscaPrimary` dan `PinkAccent`
2. **Theme.kt**: Sesuaikan color scheme jika perlu

Semua UI component akan otomatis menggunakan warna baru karena sudah menggunakan `MaterialTheme.colorScheme.*`

## Preview Warna

### Warna Primer (Tosca)
```
Hex: #56DFCF
RGB: (86, 223, 207)
HSL: (172°, 68%, 61%)
```

### Warna Secondary (Pink Muda)
```
Hex: #FFEDF3
RGB: (255, 237, 243)
HSL: (340°, 100%, 96%)
```

## Catatan
- **Warna tosca (#56DFCF)** memberikan kesan modern, fresh, dan bersahabat untuk pemasukan
- **Warna merah (#D32F2F)** tetap dipertahankan untuk pengeluaran agar jelas dan familiar
- **Pink muda (#FFEDF3)** digunakan sebagai accent untuk surface/background, memberikan kesan lembut
- Kombinasi tosca-merah-pink menciptakan UI yang colorful namun tetap profesional
- Kontras warna sudah dipastikan memenuhi accessibility guidelines
- Button pemasukan (tosca) dan pengeluaran (merah) tetap dengan warna yang jelas dan berbeda
