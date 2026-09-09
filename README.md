# CycleJournal
**Aplikasi Android Native — Kalender Haid & Jurnal Kesuburan Medis (Offline-first, Zero-Knowledge)**  
*Berbasis Jetpack Compose, SQLCipher, deteksi anomali FIGO, laporan PDF siap dokter SpOG, dan cadangan cloud terenkripsi.*

---

## ⚡ Panduan Cepat (Quick Start)

### 🔨 Cara Build File Rilis (Tinggal Klik Ganda)
Klik ganda file `.bat` di folder utama:

| File Batch | Fungsi | Output |
|---|---|---|
| 🚀 **`BUILD_ALL.bat`** | Build APK & AAB sekaligus | `release/CycleJournal_v1.0.0.apk` + `.aab` |
| 📱 **`BUILD_APK.bat`** | Build APK saja | `release/CycleJournal_v1.0.0.apk` |
| 📦 **`BUILD_AAB.bat`** | Build AAB saja | `release/CycleJournal_v1.0.0.aab` |

> Alternatif manual via terminal: `./gradlew assembleRelease bundleRelease`

---

## 📦 Folder Rilis Siap Pakai (`release/`)

1. **`CycleJournal_v1.0.0.aab` (19 MB)** — Android App Bundle ter-sign, langsung di-upload ke Google Play Console.
2. **`CycleJournal_v1.0.0.apk` (27 MB)** — Installer APK untuk uji coba di HP fisik/emulator.
3. **`DOKUMEN_UPLOAD_PLAY_STORE.md`** — Panduan lengkap pengisian Google Play Console (teknis, listing ID/EN, Data Safety, IARC, IAP, aset).
4. **`PRIVACY_POLICY.html`** — Kebijakan privasi siap hosting publik (URL: `https://asridigital.com/cyclejournal/privacy`).

---

## 📂 Struktur Direktori Proyek

```
CycleJournal/
├── release/                                <-- FOLDER FILE SIAP PAKAI
│   ├── CycleJournal_v1.0.0.aab             <-- AAB (Upload ke Play Console)
│   ├── CycleJournal_v1.0.0.apk             <-- APK (Instal di HP)
│   ├── DOKUMEN_UPLOAD_PLAY_STORE.md        <-- Panduan form Play Console
│   └── PRIVACY_POLICY.html                 <-- Dokumen Kebijakan Privasi
│
├── BUILD_ALL.bat / BUILD_APK.bat / BUILD_AAB.bat  <-- Script build rilis
│
├── app/                                    <-- SOURCE CODE APLIKASI (Compose + Hilt + Room + SQLCipher)
├── backend/cloudflare/                     <-- Worker + D1 schema (backup zero-knowledge)
├── web/cyclejournal/                       <-- Landing page + privacy (hosting)
├── store_assets/                           <-- Icon 512 & feature graphic 1024x500
├── out/                                    <-- Screenshot Goldie (raw + framed)
├── flows/                                  <-- Alur Goldie (auto capture Play Store)
├── design/                                 <-- Blueprint PDF, prototipe UI, logo master, analysis_chunks
├── docs/                                   <-- PROMPT_GOAL_CYCLEJOURNAL.md (misi)
└── scripts/                                <-- generate_app_icons.py, render_realistic_screens.py
```

---

## 🛠️ Teknologi
- **Kotlin / Jetpack Compose** (Material 3, MVI/Clean) • **Dagger Hilt**
- **Room + SQLCipher** (hardware-backed keystore) • **WorkManager** (backup bulanan)
- **AlarmManager** (pengingat BBT & H-2) • **BiometricPrompt** + PIN
- **Cloudflare Workers + D1** (enkripsi client-side AES-256-GCM)
- **PdfDocument native** (laporan A4 siap SpOG) • **Goldie** (screenshot toko)

---

## ⚠️ Disclaimer Medis
CycleJournal adalah alat pencatatan klinis pribadi untuk referensi dokter kandungan dan **bukan** pengganti diagnosis medis profesional, bukan alat kontrasepsi, dan tidak boleh digunakan sebagai satu-satunya dasar keputusan kesehatan.

© 2026 Asri Digital.
