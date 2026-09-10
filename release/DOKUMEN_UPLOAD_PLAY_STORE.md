# DOKUMEN PANDUAN LENGKAP UPLOAD GOOGLE PLAY STORE
**Aplikasi:** CycleJournal — Kalender Haid, Kesuburan & Laporan Dokter SpOG  
**Pengembang:** Asri Digital (Ahmad Asrizalmi)  
**Status File Rilis:** Siap Upload (Production Track / Closed Testing)  
**Versi Rilis Saat Ini:** `1.1.2` (Version Code: `4`)

---

## 1. Identitas Teknis Aplikasi (Technical Specifications)

| Parameter | Nilai / Value | Keterangan |
|---|---|---|
| **Application ID / Package Name** | `com.app.cyclejournal` | Pengenal unik aplikasi di Google Play Console |
| **Version Name** | `1.1.2` | Nomor versi yang tampil kepada pengguna di toko |
| **Version Code** | `4` | Nomor integer inkremental untuk Play Console |
| **Target SDK** | `36` (Android 16) | Wajib sesuai kepatuhan Google Play terbaru |
| **Compile SDK** | `36` (Android 16) | Mendukung API Android 16 |
| **Minimum SDK** | `26` (Android 8.0) | Mendukung 95%+ perangkat Android aktif |
| **Google Play Billing** | `8.0.0` | Wajib sesuai kepatuhan Play Billing terbaru Google |
| **Format File Upload** | **Android App Bundle (`.aab`)** | File: `release/CycleJournal_v1.1.2.aab` (18 MB) |
| **Format File Testing Fisik** | **Release APK (`.apk`)** | File: `release/CycleJournal_v1.1.2.apk` (26 MB) |
| **Keystore Signing** | `cyclejournal-release.jks` | Alias: `cyclejournal`, Signed & ZipAligned |

---

## 2. Listing Toko Aplikasi (Store Listing Metadata)

### 🇮🇩 Bahasa Indonesia (Indonesian Listing — Bahasa Utama)

* **Nama Aplikasi (Maks. 30 Karakter):**
  ```
  CycleJournal: Kalender & SpOG
  ```
  *(29 karakter — mencakup kata kunci pencarian utama: CycleJournal, Kalender Haid, Dokter SpOG)*

* **Deskripsi Singkat (Maks. 80 Karakter):**
  ```
  Kalender haid & subur medis: 100% offline, FIGO, enkripsi & laporan PDF SpOG.
  ```
  *(77 karakter — padat nilai jual: medis, offline, standar FIGO, enkripsi, PDF SpOG)*

* **Deskripsi Lengkap (Maks. 4000 Karakter):**
  ```
  CycleJournal adalah instrumen pencatatan siklus menstruasi dan kesuburan berstandar medis klinis yang dirancang 100% offline-first dengan privasi mutlak zero-knowledge. 

  Bukan sekadar kalender haid biasa, CycleJournal dirancang bersama standar ginekologi internasional untuk membantu Anda mengenali pola tubuh dan menghasilkan laporan medis 1 lembar A4 yang siap dibawa saat berkonsultasi ke dokter spesialis kandungan (SpOG).

  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  🩺 APA ITU STANDAR FIGO & MENGAPA PENTING?
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  FIGO (International Federation of Gynecology and Obstetrics) adalah federasi dokter spesialis obstetri dan ginekologi tingkat dunia yang menetapkan standar emas diagnosis kesehatan reproduksi wanita.

  Menurut konsensus klinis FIGO, siklus menstruasi yang sehat bukan hanya soal "teratur 28 hari", melainkan harus memenuhi batas biologis normal:
  • Panjang Siklus Normal: 24 hingga 38 hari.
  • Durasi Perdarahan Normal: Tidak lebih dari 8 hari berturut-turut.
  • Variabilitas Siklus Sehat: Perbedaan antar-siklus tidak melebihi 7–9 hari.

  CycleJournal secara otomatis memproses data siklus Anda menggunakan algoritma berbasis konsensus FIGO untuk mendeteksi dini 7 Tanda Bahaya (Red Flags) yang sering terabaikan:
  1. Oligomenore (Siklus terlalu panjang > 38 hari)
  2. Polimenore (Siklus terlalu pendek < 24 hari)
  3. Ketidakteraturan Siklus Signifikan (Variasi siklus >= 8 hari)
  4. Perdarahan Berkepanjangan (> 8 hari aktif)
  5. Perdarahan Antar-Menstruasi (Spotting di luar fase ovulasi)
  6. Fase Luteal Pendek (< 10 hari pasca-ovulasi, indikasi defisiensi progesteron)
  7. Dismenore Berat (Skala nyeri VAS >= 7 atau ketergantungan obat pereda nyeri)

  Deteksi dini ini sangat berharga untuk membantu Anda dan dokter mendeteksi risiko kondisi ginekologis seperti PCOS, endometriosis, kista ovarium, atau adenomiosis sedini mungkin.

  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ✨ FITUR UTAMA CYCLEJOURNAL
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  1. 100% OFFLINE & PRIVASI MUTLAK ZERO-KNOWLEDGE
  • Tanpa Akun & Tanpa Email: Gunakan seluruh fitur tanpa registrasi, tanpa nomor HP, dan tanpa login media sosial. Identitas Anda murni berupa ID Anonim terenkripsi (contoh: px-7f9a2b1c4e0d).
  • Enkripsi Hardware SQLCipher: Data kesehatan Anda disimpan lokal di ponsel dengan enkripsi hardware setara militer AES-256 yang dilindungi Android Keystore.
  • Mode Samaran (Anti-Intip): Samarkan istilah sensitif menjadi kode netral (Fase 01, Fase 02) saat berada di tempat umum.
  • Kunci PIN 4-Digit & Biometrik: Lindungi aplikasi dengan kunci keamanan otomatis setiap kali aplikasi ditinggal di latar belakang.

  2. LAPORAN MEDIS PDF A4 SIAP DOKTER SpOG
  • Hemat waktu konsultasi berharga Anda. Ekspor ringkasan medis 1 lembar format PDF A4 standar rumah sakit yang memuat:
    - Statistik siklus FIGO (Rata-rata, Variabilitas Standar Deviasi σ, Durasi).
    - Grafik kurva temperatur basal BBT dan konfirmasi ovulasi.
    - Riwayat log siklus 3–6 bulan terakhir.
    - Notifikasi tanda bahaya (red flags) yang terdeteksi.
    - Kolom diagnosa dan paraf/stempel resmi dokter SpOG.
  • Dilengkapi ekspor data mentah ke CSV (Excel / Google Sheets).

  3. METODE ALAMI SINTOTERMAL (BBT & LENDIR SERVIKS)
  • Pantau pergeseran suhu basal tubuh (BBT) dengan aturan klinis 3-over-6 untuk mengonfirmasi terjadinya ovulasi secara ilmiah.
  • Catat karakteristik lendir serviks (Kering, Krim, Cair, Putih Telur) untuk menentukan hari puncak masa subur dengan akurasi optimal.

  4. PREDIKSI SIKLUS HINGGA 6 BULAN KE DEPAN
  • Kalender interaktif memproyeksikan estimasi haid, jendela subur 6-hari, dan puncak ovulasi hingga 6 siklus mendatang.
  • Sel kalender membedakan dengan jelas antara riwayat darah tercatat, jadwal prediksi haid, dan masa subur.

  5. PENCATATAN GEJALA MANDIRI & GEJALA KUSTOM
  • Pantau tingkat nyeri haid menggunakan Skala Analog Visual (VAS 0–10) dengan peringatan SpOG otomatis jika nyeri melumpuhkan aktivitas.
  • Catat gejala tubuh klinis: Kram Pelvis, Sakit Pinggang, Payudara Sensitif, Sakit Kepala, Perut Kembung, Mood Sensitif, Kelelahan, Mual.
  • Tambah Gejala Kustom: Buat dan simpan gejala pribadi Anda (misal: Migrain, Nyeri Sendi, Insomnia) tanpa batas.
  • Kolom catatan klinis bebas untuk dosis obat dan arahan dokter.

  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  💎 PILIHAN LISENSI
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  • Versi Gratis: Pelacakan siklus penuh, kalender proyeksi 6 bulan, deteksi anomali FIGO, dan ekspor laporan PDF (didukung 1 iklan singkat).
  • Lifetime Pro (Beli Putus Sekali Seumur Hidup): Bebas iklan selamanya, unduh PDF instan tanpa batas, dan sinkronisasi cadangan cloud terenkripsi zero-knowledge ke Cloudflare Vault.

  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ⚠️ DISCLAIMER MEDIS RESMI
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  CycleJournal adalah instrumen pencatatan mandiri untuk membantu evaluasi kesehatan reproduksi dan bukan pengganti diagnosa medis profesional, bukan alat kontrasepsi, dan tidak boleh dijadikan satu-satunya dasar penentuan tindakan medis. Selalu konsultasikan kondisi kesehatan reproduksi Anda dengan dokter spesialis obstetri dan ginekologi (SpOG).
  ```

---

### 🇺🇸 Bahasa Inggris (English Global Listing)

* **App Name (Max 30 Characters):**
  ```
  CycleJournal: Private Cycle
  ```
* **Short Description (Max 80 Characters):**
  ```
  Medical-grade cycle & fertility tracker: 100% offline, FIGO standard & Ob-Gyn PDF
  ```
* **Full Description (Max 4000 Characters):**
  ```
  CycleJournal is an offline-first, zero-knowledge, medical-grade menstrual cycle and fertility journal designed to support clinical OB-GYN consultations. 

  More than an ordinary period calendar, CycleJournal adheres to international gynecological standards to empower your reproductive health with precision symptothermal tracking and a 1-page A4 medical summary ready for your gynecologist.

  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  🩺 WHAT IS THE FIGO STANDARD & WHY DOES IT MATTER?
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  FIGO (International Federation of Gynecology and Obstetrics) is the global authority on women's reproductive health, establishing universal clinical definitions for normal versus abnormal uterine bleeding.

  According to FIGO clinical consensus, a healthy cycle is defined by rigorous biological parameters:
  • Normal Cycle Length: 24 to 38 days.
  • Normal Bleeding Duration: No longer than 8 consecutive days.
  • Healthy Cycle Regularity: Variation between cycle lengths of less than 8 days.

  CycleJournal automatically evaluates your cycle logs against FIGO clinical guidelines to screen for 7 critical Red Flags:
  1. Oligomenorrhea (Prolonged cycles > 38 days)
  2. Polymenorrhea (Frequent cycles < 24 days)
  3. Significant Cycle Irregularity (Variation >= 8 days across cycles)
  4. Prolonged Bleeding (> 8 consecutive days)
  5. Intermenstrual Bleeding (Mid-cycle bleeding / spotting outside ovulatory phase)
  6. Short Luteal Phase (< 10 days post-ovulation, indicating progesterone deficiency)
  7. Severe Dysmenorrhea (VAS pain scale >= 7 or dependency on analgesics)

  Early screening provides invaluable insights to discuss conditions such as PCOS, endometriosis, fibroids, or adenomyosis with your healthcare provider as early as possible.

  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ✨ KEY HIGHLIGHTS OF CYCLEJOURNAL
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  1. 100% OFFLINE & ZERO-KNOWLEDGE PRIVACY
  • No Account, No Email: Zero registration, no phone numbers, and no social logins required. You operate under a cryptographically generated Anonymous ID (e.g. px-7f9a2b1c4e0d).
  • Hardware SQLCipher Encryption: Your intimate biomarker logs are stored locally with military-grade AES-256 encryption anchored to the Android Keystore.
  • Discreet Shield Mode: Sanitize sensitive terminology into neutral codes in public.
  • 4-Digit PIN & Biometrics: Automatic app lock with fingerprint/face verification after 30 seconds of inactivity.

  2. 1-PAGE A4 CLINICAL PDF REPORT FOR OB-GYN
  • Maximize the value of your doctor appointments. Export a hospital-grade 1-page A4 PDF summary featuring:
    - FIGO cycle metrics (Mean, Sample Standard Deviation σ, Duration).
    - Basal body temperature biphasic curve chart confirming ovulation.
    - 3 to 6-cycle historical log table.
    - Automated FIGO anomaly red flag alerts.
    - Doctor notes and official sign-off/stamp box.
  • Raw Excel-compatible CSV export included.

  3. SYMPTOTHERMAL FERTILITY TRACKING (BBT & CERVICAL MUCUS)
  • Basal Body Temperature (BBT) biphasic shift tracking with the clinical 3-over-6 rule.
  • Cervical mucus biomarker logging (Dry, Creamy, Watery, Egg White) to verify conception windows.

  4. MULTI-CYCLE PREDICTIONS UP TO 6 MONTHS AHEAD
  • Calendar projection mapping future periods, 6-day fertile windows, and ovulation peaks up to 6 cycles ahead.

  5. BIOMARKER & CUSTOM SYMPTOM JOURNAL
  • Visual Analog Scale (VAS 0–10) pain tracker with automatic SpOG warnings for severe dysmenorrhea.
  • Built-in clinical symptoms: Pelvic Cramps, Lower Back Pain, Tender Breasts, Headaches, Bloating, Mood Changes, Fatigue, Nausea.
  • Custom Symptoms: Add and persist your own specific symptoms (e.g. Migraine, Joint Pain, Insomnia).
  • Free-form clinical notes for medication dosages and physician instructions.

  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  💎 LICENSING OPTIONS
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  • Free Tier: Complete cycle tracking, 6-month predictions, FIGO anomaly screening, and PDF report export with a brief ad.
  • Lifetime Pro (One-Time Purchase): Zero ads forever, instant unlimited PDF exports, and encrypted zero-knowledge Cloudflare Vault backups.

  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ⚠️ MEDICAL DISCLAIMER
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  CycleJournal is an instrument for personal self-tracking and clinical reference. It is not a substitute for professional medical diagnosis, not a contraceptive method, and should not be used as the sole basis for healthcare decisions. Always seek the advice of a qualified physician or OB-GYN.
  ```

---

## 3. Kategori, Usia & Target Audiens Play Console

* **Kategori Utama (Primary Category):** `Health & Fitness` (Kesehatan & Kebugaran)
* **Kategori Sekunder / Tags:**
  - `Medical` (Medis)
  - Tags: `Cycle Tracker`, `Period Tracker`, `Ovulation`, `Fertility`, `BBT`, `SpOG`, `Gynecology`, `Menstrual Health`
* **Target Usia (Target Age):** 12+ / 18+ (Kesehatan Reproduksi). Pilih opsi *"No"* pada pertanyaan apakah ditujukan khusus anak-anak (*Families Policy*).

---

## 4. Kuisioner Rating Konten (IARC)

Isi kuesioner IARC di Play Console dengan parameter:
1. **Kategori Aplikasi:** `Health & Fitness` / `Consumer Utility`.
2. **Kekerasan (Violence):** `No`
3. **Seksualitas / Pornografi:** `No` (konten murni edukasi kesehatan reproduksi dan kesuburan medis).
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
| **Apakah data dikumpulkan?** | **Ya (Collected: Yes)** | Data kesehatan dicatat oleh pengguna di aplikasi. |
| **Apakah data dibagikan ke pihak ketiga?** | **Tidak (Shared: No)** | Data 100% lokal. Tidak ada pihak ketiga yang memiliki akses ke plaintext. |
| **Enkripsi dalam transit?** | **Ya (In transit: Yes)** | HTTPS / TLS 1.3 pada cadangan cloud opsional. |
| **Enkripsi saat disimpan (At rest)?** | **Ya (At rest: Yes)** | Hardware-backed AES-256 SQLCipher pada penyimpanan lokal ponsel. |
| **Penghapusan data pengguna?** | **Ya (Deletion Request: Yes)** | Tombol *"Hapus Seluruh Data Permanen"* memusnahkan database dan master key seketika. |
| **Tipe Data Kesehatan (Health & Fitness):** | **Ya** | Kategori: Menstruasi, Suhu Tubuh Basal, Skala Nyeri, Gejala Reproduksi. |
| **Tujuan Pengumpulan:** | **App Functionality** | Hanya untuk operasional aplikasi, BUKAN untuk analitik/iklan pihak ketiga. |
| **Iklan (Google AdMob):** | **Device or other IDs** | Hanya mengaktifkan **Non-Personalized Ads (`npa = 1`)** tanpa tracking lintas aplikasi. |

---

## 6. Produk In-App Purchase (Monetisasi Play Console)

Masuk ke menu: **Monetize > Products > In-app products**:
* **Product ID:** `lifetime_pro_access`
* **Nama Produk:** `Beli Putus Lifetime Pro`
* **Deskripsi:** `Buka ekspor PDF tanpa batas, sinkronisasi cloud terenkripsi, dan bebas iklan selamanya.`
* **Harga Default (Indonesia):** `Rp 49.000` (Sudah termasuk PPN)
* **Harga Internasional:** Aktifkan konversi otomatis Google Play (~$4.99 USD).
* **Status:** Aktif (*Active*) — Jenis *Non-Consumable* (Sekali Beli Selamanya).

---

## 7. Lokasi File Rilis & Aset Grafis

| Aset | Keterangan & Lokasi di Repositori |
|---|---|
| **File Rilis AAB (Wajib Upload)** | 📁 `release/CycleJournal_v1.1.2.aab` (18 MB, Version Code: 4) |
| **File Rilis APK (Install Uji Coba)** | 📁 `release/CycleJournal_v1.1.2.apk` (26 MB) |
| **Ikon Aplikasi Resolusi Tinggi** | 📁 `store_assets/play_store_icon_512.png` (512 × 512 px, 32-bit PNG transparan) |
| **Gambar Fitur Utama (Feature Graphic)** | 📁 `store_assets/feature_graphic_1024x500.png` (1024 × 500 px, Coral-to-Pink gradient) |
| **5 Screenshot Play Store (Bahasa Indonesia)** | 📁 `out/screenshots/pixel-10-pro/id-ID/*.png` (1080 × 1920 px, 5 berkas) |
| **5 Screenshot Play Store (Bahasa Inggris)** | 📁 `out/screenshots/pixel-10-pro/en-US/*.png` (1080 × 1920 px, 5 berkas) |

---

## 8. Kebijakan Privasi Publik (Privacy Policy URL)

* **URL Resmi Kebijakan Privasi:** `https://asridigital.com/cyclejournal/privacy`
* **Kepatuhan Regulasi:** GDPR Pasal 9 (Perlindungan Khusus Data Kesehatan), UU Pelindungan Data Pribadi (UU PDP No. 27/2022 Indonesia), dan Kebijakan Data Kesehatan Google Play.
