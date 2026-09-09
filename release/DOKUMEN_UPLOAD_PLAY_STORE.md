# DOKUMEN PANDUAN LENGKAP UPLOAD GOOGLE PLAY STORE
**Aplikasi:** CycleJournal — Kalender Haid & Laporan SpOG  
**Pengembang:** Asri Digital (Ahmad Asrizalmi)  
**Status File Rilis:** Siap Upload (Production Track / Closed Testing)

---

## 1. Identitas Teknis Aplikasi (Technical Specifications)

| Parameter | Nilai / Value | Keterangan |
|---|---|---|
| **Application ID / Package Name** | `com.app.cyclejournal` | Pengenal unik aplikasi di Google Play |
| **Version Name** | `1.0.0` | Versi yang tampil kepada pengguna |
| **Version Code** | `1` | Nomor integer rilis untuk Play Console |
| **Target SDK** | `34` (Android 14) | Sesuai regulasi wajib Google Play |
| **Minimum SDK** | `26` (Android 8.0) | Mendukung mayoritas perangkat aktif |
| **Format File Upload** | **Android App Bundle (`.aab`)** | File: `release/CycleJournal_v1.0.0.aab` (19 MB) |
| **Keystore Signing** | `cyclejournal-release.jks` | Signer: `CN=CycleJournal, OU=Health, O=AsriDigital` (SHA-256: `e2c343b3...`) |

---

## 2. Listing Toko Aplikasi (Store Listing Metadata)

### 🇮🇩 Bahasa Indonesia (Indonesian Listing)

* **Nama Aplikasi (Maks. 30 Karakter):**
  ```
  CycleJournal: Kalender & SpOG
  ```
* **Deskripsi Singkat (Maks. 80 Karakter):**
  ```
  Jurnal haid & kesuburan medis: 100% offline, terenkripsi, laporan PDF siap dokter.
  ```
* **Deskripsi Lengkap (Maks. 4000 Karakter):**
  ```
  CycleJournal adalah kalender haid dan jurnal kesuburan berstandar medis klinis yang 100% offline-first dengan privasi mutlak — dirancang untuk mendampingi konsultasi dokter kandungan (SpOG).

  Fitur Utama:
  • Pelacakan Siklus Presisi: Kalender haid, estimasi ovulasi, dan jendela masa subur berbasis metodologi FIGO.
  • Deteksi Anomali FIGS: Screening otomatis untuk siklus memanjang, siklus pendek, siklus tak teratur, perdarahan memanjang, spotting di luar jadwal, fase luteal pendek, dan dismenore berat.
  • Laporan PDF Siap SpOG: Ekspor ringkasan klinis A4 satu halaman dengan metrik FIGO, red flags anomali, tabel riwayat siklus, dan kotak tanda tangan dokter.
  • Jurnal Gejala Harian: Catat suhu basal tubuh (BBT), karakteristik lendir serviks, dan skala nyeri VAS 0–10.
  • Enkripsi Zero-Knowledge: Seluruh data terenkripsi di perangkat (SQLCipher + Android Keystore). Cadangan cloud opsional dienkripsi AES-256 di sisi klien — server tidak pernah melihat isi data Anda.
  • Kedaulatan Penuh Data: Ekspor data mentah ke CSV (Excel-ready) dan hapus total semua data dalam satu sentuhan.
  • Tanpa Akun: Gunakan seluruh fitur tanpa email, nomor telepon, atau login media sosial — identitas tetap anonim (px-xxxxxxxxxxxx).

  Pilihan Lisensi:
  - Gratis: Pelacakan & kalender siklus penuh. Ekspor PDF dengan iklan singkat.
  - Lifetime Pro (Sekali Bayar): Ekspor PDF tanpa batas, sinkronisasi backup cloud otomatis, dan bebas iklan selamanya.

  Unduh sekarang dan kendalikan sepenuhnya data kesehatan reproduksi Anda!
  ```

### 🇺🇸 Bahasa Inggris (English Listing)

* **App Name (Max 30 Characters):**
  ```
  CycleJournal: Private Cycle
  ```
* **Short Description (Max 80 Characters):**
  ```
  Medical-grade cycle & fertility journal: offline, encrypted, clinician-ready PDF.
  ```
* **Full Description (Max 4000 Characters):**
  ```
  CycleJournal is an offline-first, medical-grade menstrual cycle and fertility journal with absolute privacy, designed to support OB-GYN (SpOG) consultations.

  Key Features:
  • Precise Cycle Tracking: Period calendar, ovulation estimate, and fertile-window prediction based on FIGO methodology.
  • FIGO Anomaly Screening: Automated alerts for long cycles, short cycles, cycle irregularity, prolonged bleeding, intermenstrual spotting, short luteal phase, and severe dysmenorrhea.
  • Clinician-Ready PDF Report: Export a one-page A4 clinical summary with FIGO metrics, red-flag anomalies, cycle history table, and doctor sign-off box.
  • Daily Symptom Journal: Log basal body temperature (BBT), cervical mucus, and a 0–10 VAS pain scale.
  • Zero-Knowledge Encryption: All data is encrypted on-device (SQLCipher + Android Keystore). Optional cloud backups are encrypted client-side with AES-256 — the server never sees your data.
  • Full Data Sovereignty: Export raw data to Excel CSV anytime, or permanently destroy all records in one tap.
  • Account-Free: Use every feature with no email, no phone number, and no social login — keep a fully anonymous identity (px-xxxxxxxxxxxx).

  Pricing:
  - Free: Full cycle tracking & calendar. PDF export with a short ad.
  - Lifetime Pro (One-time): Unlimited PDF exports, automatic encrypted cloud backup, and zero ads forever.

  Download now and take full control of your reproductive health data!
  ```

---

## 3. Kategori & Target Audiens (Categorization & Tags)

* **Kategori Aplikasi (Application Category):** `Health & Fitness` (Kesehatan & Kebugaran)
* **Kategori Sekunder / Tags:**
  - `Medical` (Medis) — Menstrual & Reproductive Health
  - Tags: `Cycle Tracker`, `Period`, `Ovulation`, `Fertility`, `Menstrual Health`, `BBT`, `OB-GYN`, `Fertility Calendar`
* **Target Usia (Target Age):** 12+ / 18+ (Kesehatan reproduksi). Tandai "18 and over" untuk konten kesehatan reproduksi sesuai kebijakan.
* **Tidak Ditujukan Khusus Anak-anak:** Pilih opsi *"No"* pada pertanyaan Families Policy.

---

## 4. Kuisioner Rating Konten (Content Rating - IARC)

Karena aplikasi ini berkategori Kesehatan & Kebugaran (kesehatan reproduksi), isi kuesioner IARC dengan jujur:
1. **Kategori Aplikasi:** `Health & Fitness, Medical`.
2. **Kekerasan (Violence):** `No`
3. **Konten Seksual (Sexuality):** `No` (konten edukasi reproduksi, bukan pornografi)
4. **Bahasa Kasar (Profanity):** `No`
5. **Narkoba/Alkohol/Tembakau:** `No`
6. **Apakah pengguna dapat berinteraksi/chat?** `No`
7. **Apakah aplikasi berbagi lokasi fisik?** `No`
8. **Apakah aplikasi membeli barang fisik?** `No`
* **Hasil Rating:** Biasanya **Rated 12+ atau 18+** (kesehatan reproduksi). Patuhi jawaban jujur dari kuesioner.

---

## 5. Deklarasi Keamanan Data (Data Safety Form)

Google Play mewajibkan formulir *Data Safety*; isi dengan jujur mengingat sifat data kesehatan:
* **Data yang dikumpulkan:**
  - **Health Info (Menstrual/Reproductive, Body Temperature, Pain Symptoms):** *Collected: Yes* (hanya disimpan lokal).
  - **Usage: App functionality** (Analytics/Marketing: **No**).
* **Apakah data dienkripsi?**
  - **In transit (TLS) & At rest (AES-256 / SQLCipher):** *Yes*.
* **Apakah pengguna dapat meminta penghapusan data?** *Yes* (fitur "Hapus Semua Data").
* **Iklan (Google AdMob):**
  - **Device or other IDs:** *Collected: Yes*, *Shared: Yes*, *Ephemeral: No*, *Required: No (Optional)* — hanya **Non-Personalized Ads** (npa: 1).
  - **Tujuan:** `Advertising or marketing`.
* **Pembelian Dalam Aplikasi (Google Play Billing):** Diproses langsung oleh Google Play Services.

---

## 6. Produk In-App Purchase (Monetisasi Play Console)

Masuk ke menu: **Monetize > Products > In-app products**:
* **Product ID:** `lifetime_pro_access`
* **Nama Produk:** `Beli Putus Lifetime Pro`
* **Deskripsi:** `Buka ekspor PDF tanpa batas, sinkronisasi backup cloud otomatis, dan bebas iklan selamanya.`
* **Harga Default (Indonesia):** `Rp 49.000`
* **Harga Global:** Centang konversi otomatis Google Play (~$4.99 USD).
* **Status:** Aktifkan (*Active*). *(Non-consumable / one-time purchase)*

---

## 7. Dimensi & Lokasi Aset Grafis (Store Graphic Assets)

| Aset | Dimensi / Format | Lokasi File di Repositori |
|---|---|---|
| **App Icon** | 512 × 512 px (PNG 32-bit) | `store_assets/play_store_icon_512.png` |
| **Feature Graphic** | 1024 × 500 px (PNG) | `store_assets/feature_graphic_1024x500.png` |
| **Phone Screenshots (ID)** | 1080 × 1920 px (PNG) | `out/screenshots/pixel-10-pro/id-ID/*.png` (5 gambar) |
| **Phone Screenshots (EN)** | 1080 × 1920 px (PNG) | `out/screenshots/pixel-10-pro/en-US/*.png` (5 gambar) |

> Catatan: Screenshot Goldie perlu diperiksa ulang agar sesuai dimensi ponsel penuh tanpa terpotong (lihat langkah berikutnya).

---

## 8. Kebijakan Privasi (Privacy Policy URL)

Google Play mewajibkan tautan Privacy Policy yang dapat diakses publik:
* **URL Resmi (sudah ter-deploy):** `https://asridigital.com/cyclejournal/privacy`
* **File Lokal di Repo:** `release/PRIVACY_POLICY.html` dan `web/cyclejournal/privacy.html`
* **Kepatuhan:** GDPR Pasal 9 (data kesehatan), UU PDP Indonesia, dan Google Play Health Policy. Menyatakan: zero plaintext cloud storage, enkripsi client-side AES-256-GCM, identitas anonim `px-...`, non-personalized ads, dan hak penghapusan total.

---

## 9. Disclaimer Medis Wajib (Listing)

Pastikan dicantumkan di deskripsi & privasi:
> CycleJournal adalah alat pencatatan klinis pribadi untuk referensi dokter kandungan dan **bukan** pengganti diagnosis medis profesional, bukan alat kontrasepsi, dan tidak boleh digunakan sebagai satu-satunya dasar keputusan kesehatan.
