# DOKUMEN PANDUAN LENGKAP UPLOAD GOOGLE PLAY STORE
**Aplikasi:** CycleJournal: Period Tracker / CycleJournal: Kalender Haid  
**Pengembang:** Asri Digital (Ahmad Asrizalmi)  
**Status File Rilis:** Sedang Ditinjau Google Play (Production Track Rollout 100% — 177 Negara)  
**Versi Rilis Saat Ini:** `1.1.20` (Version Code: `22`)

---

## 1. Identitas Teknis Aplikasi (Technical Specifications)

| Parameter | Nilai / Value | Keterangan |
|---|---|---|
| **Application ID / Package Name** | `com.app.cyclejournal` | Pengenal unik aplikasi di Google Play Console |
| **Version Name** | `1.1.20` | Nomor versi yang tampil kepada pengguna di toko |
| **Version Code** | `22` | Nomor integer inkremental untuk Play Console |
| **Target SDK** | `36` (Android 16) | Wajib sesuai kepatuhan Google Play terbaru |
| **Compile SDK** | `36` (Android 16) | Mendukung API Android 16 |
| **Minimum SDK** | `26` (Android 8.0) | Mendukung 95%+ perangkat Android aktif |
| **Google Play Billing** | `8.0.0` | Wajib sesuai kepatuhan Play Billing terbaru Google |
| **Format File Upload** | **Android App Bundle (`.aab`)** | File: `release/CycleJournal_v1.1.20.aab` |
| **Format File Testing Fisik** | **Release APK (`.apk`)** | File: `release/CycleJournal_v1.1.20.apk` |
| **Keystore Signing** | `cyclejournal-release.jks` | Alias: `cyclejournal`, Signed & ZipAligned |

---

## 2. Listing Toko Aplikasi (Store Listing Metadata)

> **Catatan Kepatuhan Kebijakan Kesehatan Google Play (Akun Developer Personal):**  
> Bahasa default aplikasi di Google Play Console telah diset ke **English (United States) - en-US** untuk menjangkau 176 negara global, dengan **Bahasa Indonesia (id)** sebagai terjemahan lokal khusus pengguna di Indonesia.  
> Seluruh deskripsi menggunakan narasi *Personal Wellness & Self-Care Journal* yang 100% aman bagi akun pengembang personal (tanpa klaim perangkat medis diagnostik/klinis/SaMD).

### 🇺🇸 Bahasa Inggris (English Global Listing — Bahasa Default / Primary)

* **App Name (Maks. 30 Karakter):**
  ```
  CycleJournal: Period Tracker
  ```
  *(28 karakter — kata kunci pencarian global utama: CycleJournal, Period Tracker)*

* **Short Description (Maks. 80 Karakter):**
  ```
  Private offline period tracker & cycle calendar. No sign-up, 100% data privacy.
  ```
  *(78 karakter — menonjolkan keunggulan unik: offline-first, no account, privacy-focused)*

* **Full Description (Maks. 4000 Karakter):**
  ```
  CycleJournal is a private, offline-first menstrual cycle tracker and personal wellness journal designed to put you in complete control of your body's rhythm.

  Enjoy a modern, intuitive, and calm experience without worrying about data privacy, tracking cookies, or unwanted accounts.

  KEY FEATURES:

  1. 100% PRIVATE & OFFLINE-FIRST
  - All cycle entries, symptom notes, and personal logs stay securely on your local device.
  - Zero registration: No account, no email, and no phone number required.
  - Works completely offline without needing an active internet connection.

  2. EASY PERIOD & CYCLE TRACKING
  - Log period start and end dates, flow intensity, and daily physical sensations with ease.
  - Clear visual charts and monthly calendar views to help you understand your natural patterns.

  3. EXPORT 1-PAGE CYCLE SUMMARY (PDF)
  - Generate a neat, one-page printable PDF summary of your recent cycle history.
  - Perfect for personal self-care archives or sharing with your wellness provider when needed.

  4. LOCAL REMINDERS & NOTIFICATIONS
  - Gentle on-device reminders for upcoming estimated cycle dates or daily self-care logs.

  5. OPTIONAL LOCAL APP LOCK
  - Keep your journal confidential with optional biometric fingerprint or local PIN protection.

  Usage Disclaimer:
  CycleJournal is an independent self-care diary designed for personal lifestyle tracking and general wellness reference. It is not a contraceptive device and is not a substitute for professional clinical advice, evaluation, or medical diagnosis.
  ```

---

### 🇮🇩 Bahasa Indonesia (Indonesian Listing — Terjemahan Lokal)

* **Nama Aplikasi (Maks. 30 Karakter):**
  ```
  CycleJournal: Kalender Haid
  ```
  *(27 karakter — kata kunci pencarian lokal Indonesia: CycleJournal, Kalender Haid)*

* **Deskripsi Singkat (Maks. 80 Karakter):**
  ```
  Kalender haid & masa subur mandiri. 100% offline, privat, tanpa akun.
  ```
  *(66 karakter)*

* **Deskripsi Lengkap (Maks. 4000 Karakter):**
  ```
  CycleJournal adalah aplikasi pelacak siklus bulanan dan catatan masa subur mandiri yang mengutamakan privasi penuh dan kemudahan penggunaan.

  Dirancang untuk mencatat ritme tubuh harian Anda secara mandiri tanpa kekhawatiran privasi data.

  FITUR UTAMA:

  1. 100% PRIVAT & OFFLINE-FIRST
  - Semua catatan siklus dan jurnal harian tersimpan aman di penyimpanan internal perangkat Anda.
  - Tidak memerlukan registrasi akun, email, ataupun nomor ponsel.
  - Berjalan sepenuhnya tanpa memerlukan koneksi internet.

  2. PENCATATAN SIKLUS TERSTRUKTUR
  - Catat tanggal mulai, durasi haid, intensitas aliran, dan catatan gejala fisik dengan rapi.
  - Tampilan kurva dan riwayat bulanan yang intuitif untuk mengenali pola alami tubuh Anda.

  3. RINGKASAN REKAP RIWAYAT SIKLUS (CETAK PDF)
  - Simpan dan ekspor rangkuman riwayat jurnal haid Anda ke dalam lembar dokumen PDF 1 halaman yang terstruktur.
  - Memudahkan pengarsipan data pribadi atau untuk dicetak sebagai catatan referensi mandiri saat dibutuhkan.

  4. PENGINGAT MANDIRI
  - Pengingat lokal untuk perkiraan jadwal siklus berikutnya atau catatan rutinitas harian.

  5. PERLINDUNGAN AKSES TAMBAHAN
  - Opsi kunci keamanan lokal (PIN / Biometrik perangkat) agar catatan harian tetap privat dari jangkauan orang lain.

  Catatan Penggunaan:
  CycleJournal adalah buku harian digital mandiri (personal self-care journal) yang ditujukan untuk pencatatan gaya hidup dan referensi pribadi. Aplikasi ini bukan alat kontrasepsi dan bukan pengganti saran, diagnosis, atau penilaian medis profesional.
  ```

---

## 3. Kategori, Usia & Target Audiens Play Console

* **Kategori Utama (Primary Category):** `Health & Fitness` (Kesehatan & Kebugaran)
* **Kategori Sekunder / Tags:**
  - `Kesehatan & kebugaran`
  - `Kalender`
  - `Gaya Hidup`
* **Target Usia (Target Age):** 12+ / 18+ (Kesehatan Reproduksi). Pilih opsi *"No"* pada pertanyaan apakah ditujukan khusus anak-anak (*Families Policy*).
* **Deklarasi Aplikasi Kesehatan (Health Apps Declaration):**
  - Kategori dideklarasikan: `Pelacakan siklus menstruasi` di bawah `Kesehatan dan kebugaran`.
  - Status regulasi: *"Saat ini, Anda tidak perlu memberikan persyaratan regional apa pun untuk pernyataan kesehatan aplikasi Anda."* (Aman untuk akun Developer Personal).

---

## 4. Kuisioner Rating Konten (IARC)

Isi kuesioner IARC di Play Console dengan parameter:
1. **Kategori Aplikasi:** `Health & Fitness` / `Consumer Utility`.
2. **Kekerasan (Violence):** `No`
3. **Seksualitas / Pornografi:** `No` (konten murni pencatatan ritme tubuh dan kalender).
4. **Bahasa Kasar / Vulgar:** `No`
5. **Narkoba / Miras / Tembakau:** `No`
6. **Interaksi Pengguna / Chat Pribadi:** `No`
7. **Berbagi Lokasi Fisik:** `No`
8. **Membeli Barang Fisik:** `No`
* **Hasil Ekspektasi:** Rating **PEGI 3 / USK 0 / ESRB Everyone** atau **Rated 12+** (kesehatan umum).

---

## 5. Deklarasi Keamanan Data (Data Safety Form)

| Pertanyaan Google Play | Jawaban Wajib | Penjelasan |
|---|---|---|
| **Apakah data dikumpulkan?** | **Ya (Collected: Yes)** | Data siklus dicatat secara mandiri oleh pengguna di aplikasi. |
| **Apakah data dibagikan ke pihak ketiga?** | **Tidak (Shared: No)** | Data 100% lokal. Tidak ada pihak ketiga yang memiliki akses ke plaintext. |
| **Enkripsi dalam transit?** | **Ya (In transit: Yes)** | HTTPS / TLS 1.3. |
| **Enkripsi saat disimpan (At rest)?** | **Ya (At rest: Yes)** | Hardware-backed AES-256 SQLCipher pada penyimpanan lokal ponsel. |
| **Penghapusan data pengguna?** | **Ya (Deletion Request: Yes)** | Tombol *"Hapus Semua Data Permanen"* dilindungi konfirmasi ketik (HAPUS/DELETE) + verifikasi ulang PIN 4-digit, lalu memusnahkan database SQLCipher, master key Keystore, dan berkas laporan lokal. |
| **Tipe Data Kesehatan (Health & Fitness):** | **Ya** | Kategori: Menstruasi, Gejala Tubuh Harian. |
| **Tujuan Pengumpulan:** | **App Functionality** | Hanya untuk operasional aplikasi, BUKAN untuk analitik/iklan pihak ketiga. |
| **Iklan (Google AdMob):** | **Device or other IDs** | Hanya mengaktifkan **Non-Personalized Ads (`npa = 1`)** tanpa tracking lintas aplikasi. |

---

## 6. Produk In-App Purchase (Monetisasi Play Console)

Masuk ke menu: **Monetize > Products > One-time products (Produk sekali beli)**:
* **Product ID:** `lifetime_pro_access`
* **Nama & Deskripsi (Default - en-US):**
  - **Name:** `CycleJournal Pro (Lifetime Access)`
  - **Description:** `Lifetime Pro access: Ad-free experience and unlimited cycle history PDF export.`
* **Nama & Deskripsi (Terjemahan - id):**
  - **Nama:** `CycleJournal Pro (Akses Seumur Hidup)`
  - **Deskripsi:** `Akses Pro seumur hidup, bebas iklan dan ekspor laporan PDF SpOG tanpa batas.`
* **Harga Default (Indonesia):** `Rp 49.000` (Sudah termasuk PPN)
* **Harga Internasional:** 174 negara/wilayah aktif.
* **Status:** Aktif (*Active*) — Jenis *One-time Purchase* (Sekali Beli Selamanya).

---

## 7. Lokasi File Rilis & Aset Grafis

| Aset | Keterangan & Lokasi di Repositori |
|---|---|
| **File Rilis AAB (Wajib Upload)** | 📁 `release/CycleJournal_v1.1.20.aab` (Version Code: 22) |
| **File Rilis APK (Install Uji Coba)** | 📁 `release/CycleJournal_v1.1.20.apk` |
| **Ikon Aplikasi Resolusi Tinggi** | 📁 `store_assets/logo_clean_master white.png` (512 × 512 px) |
| **Gambar Fitur Utama (Feature Graphic)** | 📁 `store_assets/image tumbnail 2 ok.jpg` (1024 × 500 px) |
| **8 Screenshot Play Store** | 📁 `store_assets/01-dashboard.png` s/d `08-no-account.png` (1080 × 1920 px, 8 berkas) |

---

## 8. Kebijakan Privasi Publik (Privacy Policy URL)

* **URL Resmi Kebijakan Privasi:** `https://asridigital.com/cyclejournal/privacy`
* **Kepatuhan Regulasi:** GDPR Pasal 9 (Perlindungan Khusus Data Kesehatan), UU Pelindungan Data Pribadi (UU PDP No. 27/2022 Indonesia), dan Kebijakan Data Kesehatan Google Play.

---

## 9. Detail Login / Sign-in details (App content) — WAJIB

**Lokasi di Play Console:** `Kebijakan dan program` → `Konten aplikasi` → **Detail login** (*Sign-in details*).

> ### Jawaban yang benar
> Pilih: **"Semua fungsi di aplikasi saya tersedia tanpa akses khusus"**
> (EN: *All functionality is available without special access*)
>
> **Jangan** pilih "Semua atau sebagian fungsi dibatasi" (*All or some functionality is
> restricted*). Memilih opsi itu mewajibkan isi **Username + Password**; CycleJournal tidak
> punya akun sama sekali, jadi kolom itu tidak akan pernah bisa diisi secara valid — inilah
> yang memunculkan pesan **"Perlu diperhatikan — Detail login tidak ada"**.

### Bukti bahwa tidak ada login (hasil audit build v1.1.19, versionCode 21)

1. **Tidak ada akun/login di seluruh source.** Tidak ada Firebase Auth, Google Sign-In, OAuth,
   atau endpoint login. Gradle hanya memuat Billing, AdMob, Room/SQLCipher, Biometric.
2. **APK rilis hanya punya satu activity yang bisa diluncurkan:**
   `launchable-activity: com.app.cyclejournal.MainActivity` (dibaca langsung dari
   `release/CycleJournal_v1.1.19.apk` dengan `aapt dump badging`). Tidak ada activity login.
3. **PIN aplikasi bersifat opsional dan TIDAK diisi pabrik.** Saat instal baru
   `pinManager.isPinSet()` bernilai `false`, sehingga `MainActivity.kt:149` menandai aplikasi
   sudah terbuka dan `MainActivity.kt:245` tidak pernah menampilkan layar kunci. Layar PIN hanya
   muncul setelah pengguna sendiri menyimpannya dari menu Pengaturan (baris
   `settings_pin` → `PinSetupDialog`) — `SecurityPinManager.savePin()`. Saat instalasi bersih,
   baris tersebut terbaca **"Not set (Optional)"**.
4. **Alur pertama kali buka tidak meminta kredensial apa pun:** carousel onboarding 3 slide →
   tombol "Mulai" (`onboarding_new_user`) → langsung ke beranda. Tombol kedua
   ("Pulihkan") hanya membuka pemilih berkas `.cjbackup`.
5. **Tidak ada konten yang benar-benar terkunci di balik pembayaran.** Ekspor PDF & CSV selalu
   aktif (`AnalysisV4Screen.kt`, `ReportPill` dipanggil tanpa syarat `isPro`); `isPro` hanya
   menambah baris ajakan "Upgrade". `AdMobManager.ENABLE_ADS = false` (tidak ada iklan yang
   tampil), dan sinkronisasi cloud sudah dihapus di rilis ini
   (`CycleApplication.kt:25-27` + `LegacyCloudBackupCleanup`).

### Teks siap tempel untuk kolom "Instruksi lainnya" (WAJIB bahasa Inggris)

Dipakai bila Console tetap meminta keterangan tambahan:

```
No account, login, or sign-up is required to review this app. It has no backend sign-in:
there is no username, password, e-mail, or social login anywhere in the app, so no test
credentials exist.

Steps to reach every feature from a clean install:
1. Install and open the app. The first screen is a 3-slide onboarding carousel.
2. Tap the primary button ("Mulai" / "Start") at the bottom. You land directly on the Home
   dashboard. The other button only opens the system file picker to restore a .cjbackup file,
   so it can be ignored.
3. Use the bottom navigation: Home, Calendar, Analysis, Settings. All tracking screens,
   the 6-month calendar projection, the FIGO anomaly analysis, and the PDF/CSV export in the
   Analysis tab are fully usable without any purchase or sign-in.

About the optional app lock: Settings > "4-Digit PIN Lock" is an OPTIONAL local privacy
feature that is OFF by default (it shows "Not set (Optional)"). If you switch it on, you
choose your own 4-digit PIN and it will be requested the next time the app returns from the
background. We recommend leaving it OFF during review. If it was enabled and the PIN is no
longer known, uninstalling and reinstalling the app clears it.

About in-app purchases: the optional one-time "Lifetime Pro" product is a support/upgrade
purchase. No feature is gated behind it, so nothing needs to be purchased in order to review
the app.

All data stays on the device. The app works fully without network access.
```

### Catatan penting

* Kredensial harus **selalu aktif, bisa dipakai ulang, dan valid di semua negara**. PIN yang
  ditulis manual sebagai "password" akan ditolak — PIN itu tidak pernah ada sebelum pengguna
  membuatnya sendiri.
* Aturan resmi Google: <https://support.google.com/googleplay/android-developer/answer/15748846>
  → *"If your app requires users to set up their own password (for example, a PIN) to access
  in-app content, be sure to provide clear instructions."* dan
  *"If your app does not require sign-in details but you have functionalities or content behind
  a subscription paywall, please provide additional instructions or access details…"*
* **Tidak perlu upload AAB baru** untuk memperbaiki ini. Cukup: `Konten aplikasi` → `Detail
  login` → ubah jawaban → **Simpan** → buka `Ringkasan publikasi` → **Kirim untuk ditinjau**.
* Jangan menyalakan PIN di perangkat peninjau. Kalau peninjau menyalakannya lalu keluar dari
  aplikasi lebih dari 30 detik, layar kunci akan muncul dan mereka bisa melaporkan aplikasi
  "terkunci" — inilah satu-satunya jalur di mana aplikasi ini terlihat seperti butuh login.
