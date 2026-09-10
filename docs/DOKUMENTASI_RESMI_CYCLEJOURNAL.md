# DOKUMENTASI RESMI SISTEM & KLINIS
# CycleJournal — Menstrual Cycle & Fertility Medical Tracker

**Versi Dokumen:** 1.1.2  
**Versi Aplikasi:** 1.1.2 (Build VersionCode: 4)  
**Pengembang:** Asri Digital (Ahmad Asrizalmi)  
**Arsitektur:** Android Native (Kotlin, Jetpack Compose, Room, SQLCipher, Dagger Hilt)  
**Filosofi Utama:** *Offline-First, Zero-Knowledge Encryption, Standar Klinis FIGO & ACOG, SpOG-Ready.*

---

## 1. Ringkasan & Filosofi Desain

**CycleJournal** adalah aplikasi pencatatan siklus menstruasi, gejala tubuh harian, dan evaluasi fertilitas berstandar medis klinis. Aplikasi ini dirancang khusus untuk memberikan akurasi tingkat obstetri & ginekologi (*SpOG-ready*) dengan tetap menjamin **privasi 100% tanpa kompromi**.

### Prinsip Utama:
1. **Offline-First & Tanpa Akun Wajib:** Data tersimpan lokal di perangkat pengguna. Aplikasi dapat berfungsi penuh tanpa koneksi internet dan tanpa meminta email, nomor telepon, atau data pribadi yang dapat dilacak.
2. **Enkripsi Berlapis (Zero-Knowledge):**
   - Basis data lokal dienkripsi penuh menggunakan **SQLCipher AES-256** dengan kunci enkripsi yang dilindungi perangkat keras (*Android KeyStore TEE/StrongBox*).
   - Cadangan awan (*cloud backup*) menggunakan **Client-Side Encryption (AES-256-GCM + PBKDF2 100.000 iterasi)** sebelum dikirim ke Cloudflare D1. Pihak penyedia server tidak dapat membaca isi data Anda (*zero-knowledge*).
3. **Kepatuhan Medis Dunia:**
   - Standar **FIGO** (*International Federation of Gynecology and Obstetrics*).
   - Standar **ACOG** (*American College of Obstetricians and Gynecologists*).

---

## 2. Mekanisme Perhitungan Prediksi Siklus & Kesuburan

Perhitungan prediksi dalam CycleJournal **TIDAK bersifat kaku (*saklek*) pada angka 28 hari**, melainkan **sepenuhnya dinamis dan terus beradaptasi dengan ritme biologis tubuh setiap pengguna**.

```
                         ALUR KERJA PERHITUNGAN PREDIKSI
                         
   [Input Hari Pertama Haid]
              │
              ▼
   ┌─────────────────────────────────────────────────────────────┐
   │ Apakah sudah ada riwayat siklus yang selesai (N ≥ 1)?       │
   └──────────────┬───────────────────────────────┬──────────────┘
                  │ TIDAK                         │ YA
                  ▼                               ▼
     ┌────────────────────────┐      ┌──────────────────────────────┐
     │ Siklus Pertama:        │      │ Siklus Kedua & Seterusnya:   │
     │ Gunakan Baseline FIGO  │      │ Hitung Rata-rata Pribadi (x̄) │
     │ (Panjang Siklus = 28h) │      │ & Deviasi Standar Riil (σ)   │
     └────────────┬───────────┘      └──────────────┬───────────────┘
                  │                                  │
                  └───────────────┬──────────────────┘
                                  ▼
   ┌─────────────────────────────────────────────────────────────┐
   │ TIER 1: Prediksi Kalender (Standard FIGO/ACOG)              │
   │ • Estimasi Haid Berikutnya = Haid Terakhir + Panjang Rata²  │
   │ • Puncak Ovulasi = Estimasi Haid - 14 Hari                  │
   │ • Jendela Subur = [Ovulasi - 5 Hari] s/d [Ovulasi + 1 Hari] │
   └──────────────────────────────┬──────────────────────────────┘
                                  │
                                  ▼ (Jika mencatat BBT)
   ┌─────────────────────────────────────────────────────────────┐
   │ TIER 2: Konfirmasi Biologis Sintotermal (3-over-6 BBT Shift)│
   │ Suhu naik ≥ +0.20°C selama 3 hari berturut-turut di atas    │
   │ rata-rata 6 hari baseline ──► Kunci Tanggal Ovulasi Riil    │
   └─────────────────────────────────────────────────────────────┘
```

### A. Fase Siklus Pertama (Kalibrasi Awal)
* Saat pengguna baru pertama kali mencatat hari pertama haid, sistem belum memiliki data historis mengenai panjang siklus alami tubuh pengguna.
* Sebagai estimasi awal yang aman, aplikasi mengadopsi baseline standar internasional FIGO:
  $$\text{Panjang Siklus Awal} = 28 \text{ hari}$$
  $$\text{Estimasi Haid Berikutnya} = \text{Hari Pertama Haid} + 28 \text{ hari}$$
  $$\text{Estimasi Puncak Ovulasi} = \text{Estimasi Haid} - 14 \text{ hari (Asumsi Fase Luteal)}$$
  $$\text{Jendela Subur (Fertile Window)} = [\text{Ovulasi} - 5 \text{ hari}] \text{ sampai } [\text{Ovulasi} + 1 \text{ hari}]$$

### B. Fase Siklus Kedua dan Seterusnya (Personalisasi Penuh)
Begitu pengguna mencatat hari pertama haid untuk siklus berikutnya, siklus pertama ditutup dan panjang siklus riilnya diketahui. Mesin kalkulasi (`ClinicalCycleEngine.kt`) otomatis menghitung statistik pribadi pengguna:
1. **Rata-rata Panjang Siklus Pribadi ($\bar{x}$):**
   $$\bar{x} = \frac{\sum_{i=1}^{N} \text{Panjang Siklus Riil}_i}{N}$$
2. **Variasi / Deviasi Standar Bessel-Corrected ($\sigma$):**
   $$\sigma = \sqrt{\frac{\sum_{i=1}^{N} (x_i - \bar{x})^2}{N - 1}}$$
3. **Proyeksi Otomatis:**
   Prediksi haid berikutnya dan puncak ovulasi langsung beralih menggunakan rata-rata riil pengguna ($\bar{x}$).  
   *Contoh:* Jika rata-rata 3 siklus seorang pengguna adalah **32 hari**, maka proyeksi siklus berikutnya otomatis bertambah **+32 hari**, dan puncak ovulasi diperkirakan pada hari ke-18 siklus (32 - 14).

### C. Konfirmasi Ovulasi Biologis via Suhu Basal Tubuh (BBT Tier 2)
Selain metode kalender, aplikasi menyertakan algoritma sintotermal **3-over-6 Rule** (`BbtEngine.kt`):
* Menelusuri 6 hari berturut-turut suhu baseline terendah.
* Ovulasi dinyatakan **terkonfirmasi secara biologis** jika suhu basal naik minimal **$\ge +0.20^\circ\text{C}$** di atas garis *coverline* selama 3 hari berturut-turut.
* Jika terkonfirmasi, tanggal ovulasi biologis terkunci (*locked*) pada hari terakhir suhu rendah sebelum lonjakan (Hari ke-6).

---

## 3. Batasan Klinis & Skrining Anomali FIGO

Standar FIGO pada aplikasi ini berfungsi sebagai **sistem deteksi dini kesehatan reproduksi (*red flags screening*)**:

| Parameter FIGO | Rentang Normal | Anomali Terdeteksi | Arti Medis Klinis |
|---|---|---|---|
| **Panjang Siklus** | 24 – 38 Hari | `< 24 Hari` | **Polimenorea** (Siklus terlalu pendek / sering) |
| **Panjang Siklus** | 24 – 38 Hari | `> 38 Hari` | **Oligomenorea** (Haid jarang / terlambat jauh) |
| **Variasi Antar-Siklus** | Selisih < 8 Hari | `Selisih ≥ 8 Hari` | **Siklus Tidak Teratur** (*Cycle Irregularity*) |
| **Lama Pendarahan** | 3 – 8 Hari | `> 8 Hari` | **Pendarahan Berkepanjangan** (*Prolonged Bleeding / AUB*) |
| **Lama Pendarahan** | 3 – 8 Hari | `< 3 Hari` | **Hipomenorea** (Pendarahan sangat singkat) |
| **Fase Luteal** | $\ge 10$ Hari | `< 10 Hari` | **Fase Luteal Pendek** (Defek fase luteal pasca-ovulasi) |

---

## 4. Logika State Machine Siklus (`CycleAggregator.kt`)

Sistem pencatatan siklus dikelola oleh state machine deterministik:
1. **Definisi Pendarahan Sejati (*True Bleeding*):**
   - Hanya pendarahan dengan intensitas **`Ringan` (LIGHT)**, **`Sedang` (MEDIUM)**, atau **`Deras` (HEAVY)** yang diakui sebagai hari menstruasi aktif.
   - Pilihan **`Tidak` (NONE)** dicatat sebagai jurnal harian biasa (bukan haid).
   - Pilihan **`Bercak` (SPOTTING)** dicatat sebagai pendarahan sela/bercak dan tidak memicu pembuatan siklus baru.
2. **Ambang Batas Siklus Baru (`MIN_DAYS_FOR_NEW_CYCLE = 20`):**
   Pendarahan yang terjadi sebelum hari ke-20 siklus diperlakukan sebagai pendarahan intermenstruasi (*AUB-IMB*) tanpa memecah siklus secara prematur.
3. **Rekonsiliasi Riwayat Penuh (*Deterministic Reconciliation*):**
   Setiap kali catatan harian disimpan, diedit, atau dihapus, fungsi `reconcileAllHistory()` mengevaluasi ulang seluruh log secara kronologis dari tanggal terlama ke terbaru. Hal ini memastikan input tanggal mundur (retroaktif) atau urutan acak tetap menghasilkan urutan siklus yang 100% valid.

---

## 5. Panduan Navigasi & Struktur Layar Aplikasi

Aplikasi memiliki 4 tab navigasi utama di bagian bawah layar:

### A. Tab Beranda (Dashboard)
* **Hero Card Dinamis:** Menampilkan nama fase aktif (misal: *Fase Menstruasi*, *Fase Folikuler*, *Jendela Subur*, *Puncak Ovulasi*), hitung mundur ovulasi, rata-rata siklus, dan peluang konsepsi.
* **Circular Progress Ring:** Menampilkan progres hari siklus riil (misal: `Hari 3 dari 28`) yang bergerak dinamis.
* **Weekly Calendar Strip (7 Hari):** Strip horizontal minggu berjalan dengan indikator titik pendarahan, BBT, dan status nyeri.
* **Tren Kurva Suhu Basal (BBT):** Grafik pergeseran suhu basal untuk memantau fase folikuler vs luteal.
* **Ringkasan Catatan Harian:** Kotak ringkasan parameter tubuh yang diinput pada hari yang dipilih.

### B. Tab Kalender (Peta Siklus & Proyeksi 6 Bulan)
* **Header Interaktif:** Tombol panah `<` (Bulan Lalu) dan `>` (Bulan Depan) untuk menavigasi bulan secara bebas.
* **Subtitle Real-Time:** Menampilkan estimasi haid dan ovulasi langsung di bawah nama bulan (misal: `Haid: 7 Okt • Ovulasi: 23 Sep`).
* **Grid Warna Kalender:**
  - **Merah Muda Lembut (Menstruasi):** Hari pendarahan haid riil yang dicatat.
  - **Merah Muda Bergaris / Dot (Prediksi Haid):** Estimasi tanggal mulai haid berikutnya.
  - **Biru Muda (Masa Subur):** Rentang jendela subur.
  - **Biru Toska Tua (Puncak Ovulasi):** Hari puncak pelepasan sel telur.
* **Inspector Card:** Menampilkan data klinis lengkap dari tanggal mana pun yang diklik pada kalender.

### C. Tab Laporan Medis SpOG
* **Kotak Parameter FIGO:** Rekapitulasi Rata-rata Siklus, Variasi Standar Deviasi, dan Lama Haid riil pengguna.
* **Grafik Biphasik BBT:** Memetakan garis *coverline* dan fase ovulasi untuk verifikasi dokter.
* **Skrining Anomali Medis:** Kotak status hijau jika normal, atau peringatan merah jika terdeteksi anomali klinis FIGO.
* **Tabel Riwayat Siklus Medis:** Menampilkan daftar siklus (Mulai, Panjang, Durasi, Estimasi Ovulasi), dengan siklus berjalan (*ongoing*) di baris teratas.
* **Tabel Log Harian Terakhir:** Menampilkan 5 riwayat catatan harian riil terakhir (Tanggal, Flow, BBT, VAS Nyeri).
* *(Catatan Desain: Kotak paraf dokter fisik yang tidak relevan di layar smartphone telah dihapus agar antarmuka bersih; kolom ini tetap tersedia pada cetakan PDF).*
* **Tombol Ekspor Dokumen:**
  - **Unduh PDF Medis:** Menghasilkan dokumen rekam medis A4 resmi berstandar FIGO siap cetak.
  - **Ekspor CSV Mentah:** File spreadsheet `.csv` berisi seluruh data siklus dan data harian lengkap.

### D. Tab Pengaturan & Keamanan
* **Keamanan Akses:** Pengaturan PIN 4-digit dan autentikasi biometrik (sidik jari / face unlock). Auto-lock 30 detik di latar belakang.
* **Mode Tampilan:** Mode Samaran (*Discreet Mode* — menyamarkan istilah sensitif) dan Mode Subuh Gelap (*Dark Mode*).
* **Cadangan Awan Terenkripsi:** Backup manual dan restore data menggunakan enkripsi client-side.
* **Wipe Data (Hapus Bersih):** Menghapus seluruh data medis lokal dan kunci enkripsi secara permanen (*cryptographic wipe*).

---

## 6. Prosedur Pencatatan Data Harian (Best Practice)

Agar prediksi siklus dan kalkulasi kesuburan berjalan maksimal:
1. **Mencatat Hari Pertama Haid:**
   Buka form pencatatan pada tanggal mulai haid, lalu **pilih salah satu opsi pendarahan: `Ringan`, `Sedang`, atau `Deras`**, kemudian tekan **Simpan Catatan Hari Ini**.  
   *(Jika memilih `Tidak`, sistem menganggapnya sebagai catatan gejala biasa tanpa haid).*
2. **Mencatat Suhu Basal Tubuh (BBT):**
   Gunakan termometer basal 2 desimal di pagi hari segera setelah bangun tidur sebelum beranjak dari tempat tidur. Masukkan angka suhu (misal `36.50°C`).
3. **Mencatat Gejala Lain:**
   Pilih jenis lendir serviks (Kering, Krim, Cair, Putih Telur), tingkat nyeri (skala VAS 0–10), dan gejala tambahan seperti kram, sakit kepala, atau mual.

---

## 7. Lokasi File Rilis & Panduan Build

File distribusi rilis terletak pada folder:
`D:\9 KANDA\APPS Android\CycleJournal\release\`

| File Rilis | Versi | Ukuran | Kegunaan |
|---|---|---|---|
| **`CycleJournal_v1.1.2.apk`** | v1.1.2 (Code: 4) | ~26 MB | Instalasi langsung di perangkat fisik / emulator Android |
| **`CycleJournal_v1.1.2.aab`** | v1.1.2 (Code: 4) | ~18 MB | Berkas Android App Bundle untuk diunggah ke Google Play Console |

### Script Otomatisasi Build (Tinggal Dobel Klik di Windows):
* **`BUILD_ALL.bat`**: Melakukan clean build dan menghasilkan APK & AAB rilis sekaligus ke folder `release/`.
* **`BUILD_APK.bat`**: Menghasilkan installer APK rilis saja.
* **`BUILD_AAB.bat`**: Menghasilkan berkas AAB rilis saja untuk upload toko.

---
*Dokumen ini merupakan spesifikasi teknis dan panduan operasional resmi untuk repositori CycleJournal.*  
*Hak Cipta © 2026 Asri Digital. Dilindungi Undang-Undang.*
