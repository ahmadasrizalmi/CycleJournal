# Copywriting Aset Store — CycleJournal

Sumber resmi copy ini: **`goldie.config.ts`** (bagian `store` dan `scenes`). Dokumen ini adalah
salinan untuk dibaca manusia — kalau mengubah kata, ubah di config lalu perbarui tabel di sini.

Semua aset digenerate dari data demo (build `-PdemoSeed=true`) dengan capture dari emulator, lalu
didesain goldie. Lokasi hasil: `out/screenshots/<device>/<locale>/`.

---

## 1. Prinsip penulisan

1. **Headline = masalah yang dipecahkan**, bukan fitur yang dimiliki.
   Salah: "Suhu pagi, cairan serviks, gejala". Benar: "Sedang program hamil? Pantau tanda suburmu".
2. **Subhead = satu kalimat konkret**: siapa yang dibantu dan apa yang dia dapat.
3. **Audiens luas.** Aplikasi ini bukan hanya untuk yang promil: ada remaja yang baru mulai
   mencatat, mahasiswi dan pekerja yang tidak mau ketahuan mendadak, dan yang sedang program hamil.
   Setiap scene menyasar salah satu dari mereka.
4. **Bahasa iklan, bukan bahasa klinis.** Tidak ada "basal", "cervical mucus", "VAS", "biphasic",
   "luteal", "self-managed", atau "AES-256" di teks yang dibaca pengguna.
5. **Istilah yang nyaman.** Bahasa Indonesia memakai **"menstruasi"** (bukan "haid"), dan tidak
   memakai kata **"lendir"** (dipakai "cairan serviks" pada teks teknis di dalam app, dan tidak
   disebut sama sekali di headline).
6. **Tidak menakut-nakuti dan tidak menjanjikan diagnosis.** Aplikasi membantu memahami tubuh dan
   membawa data ke dokter, bukan menggantikan dokter.

Catatan: **string di dalam aplikasi tidak diubah.** Aturan istilah di atas berlaku untuk lapisan
desain/store. Tampilan app di screenshot tetap memakai kata-katanya sendiri.

---

## 2. Scene yang dipakai (8 scene × 3 device × 2 bahasa)

| # | Scene (flow) | Headline (ID) | Subhead (ID) | Headline (EN) | Subhead (EN) | Audiens / masalah |
|---|---|---|---|---|---|---|
| 1 | `dashboard` (store-01) | Tidak ada lagi kejadian mendadak | Perkiraan menstruasi berikutnya selalu terlihat di layar utama, jadi kamu siap sebelum harinya tiba - di sekolah, kampus, atau kantor. | No more surprises | Your next period stays on the home screen, so you are ready before it arrives - at school, on campus, or at work. | Remaja, pelajar, pekerja — tidak mau ketahuan mendadak |
| 2 | `calendar` (store-02) | Rencanakan sebulan tanpa was-was | Semua catatanmu, masa subur, dan perkiraan menstruasi tersusun di satu kalender yang mudah dibaca. | Plan the month with confidence | Every note you kept, your fertile days and your next period on one calendar that is easy to read. | Semua — ingin melihat pola sebulan |
| 3 | `daily-log` (store-03) | Catat hari ini dalam dua ketukan | Pilih yang kamu rasakan hari ini, selesai dalam hitungan detik - praktis dipakai di sela kelas atau jam istirahat. | Log today in two taps | Pick what you feel today and you are done in seconds - easy to use between classes or on a break. | Remaja & pekerja — males mengisi formulir panjang |
| 4 | `fertility-signs` (store-04) | Sedang program hamil? Pantau tanda suburmu | Suhu pagi, cairan serviks, dan nyeri tercatat rapi, jadi kamu tahu kapan peluang kehamilan paling besar. | Trying for a baby? Track your fertility signs | Morning temperature, cervical fluid and pain in one place, so you know when your chances are highest. | Yang sedang promil |
| 5 | `medical-report` (store-06) | Ke dokter bawa data, bukan tebakan | Satu laporan PDF berisi riwayat siklus, nyeri, dan pola dari catatanmu - tinggal ditunjukkan saat konsultasi. | See your doctor with data, not guesses | One PDF with your cycle history, your pain and the patterns found in your notes - just show it at the appointment. | Yang ingin konsultasi (nyeri hebat, siklus tidak teratur) |
| 6 | `privacy-security` (store-08) | Isi jurnalmu tetap rahasia | Kunci PIN dan sidik jari menjaganya dari yang mengintip, dan semua data tersimpan di HP-mu - tanpa akun. | Your journal stays private | PIN and fingerprint lock keep it away from prying eyes, and everything is stored on your phone - no account needed. | Semua — malu kalau catatannya terbaca |
| 7 | `dark-mode` (store-15) | Nyaman dipakai sebelum tidur | Mode gelap menenangkan mata saat mencatat sebelum tidur, dan lebih hemat baterai. | Gentle on the eyes at night | Dark mode keeps the screen calm when you log before bed, and it is kinder to your battery. | Yang mencatat malam hari |
| 8 | `no-account` (store-16) | Mulai tanpa daftar akun | Buka aplikasi dan langsung catat hari ini - tanpa email, tanpa login, data tetap di HP-mu. | Start without an account | Open the app and log today right away - no email, no login, your data stays on your phone. | Remaja — ingin coba tanpa meninggalkan jejak data |

---

## 3. Copy cadangan (belum dipakai, flow-nya sudah ada)

Sudah ditulis dan siap pakai; tinggal capture scene-nya di ketiga device lalu `goldie frame`.

| Scene (flow) | Headline (ID) | Subhead (ID) | Headline (EN) | Subhead (EN) |
|---|---|---|---|---|
| `bbt-chart` (store-05) | Kenali ovulasi dari suhu pagi | Grafik suhu harian memperlihatkan perubahan tubuh yang menandai masa paling subur. | Read ovulation from your temperature | The daily temperature chart shows the shift in your body that marks your most fertile days. |
| `bilingual-text-size` (store-07) | Bahasa dan ukuran huruf sesuai kamu | Indonesia atau Inggris, dengan empat ukuran huruf supaya nyaman dibaca siapa saja. | Your language, your text size | Indonesian or English, with four text sizes so it stays comfortable for every pair of eyes. |
| `day-detail` (store-09) | Mau cek satu hari? Ketuk saja | Sekali ketuk, rincian hari itu terbuka: aliran menstruasi, nyeri, suhu, dan catatanmu. | Want to check a day? Just tap it | One tap opens that day: flow, pain, temperature and the note you left yourself. |
| `report-export` (store-10) | Simpan sendiri atau bagikan | Unduh PDF siap cetak untuk dokter, atau CSV untuk diolah sendiri di spreadsheet-mu. | Keep it or share it | Download a print-ready PDF for your doctor, or a CSV to work with in your own spreadsheet. |
| `fertility-explainer` (store-11) | Bingung arti tanda dari tubuhmu? | Ketuk Keterangan dan pahami tanda kesuburan satu per satu, tanpa istilah medis yang membingungkan. | Not sure what your body is telling you? | Tap Info and understand your fertility signs one by one, without the confusing medical words. |
| `language` (store-12) | Mau bahasa Indonesia atau Inggris? | Ganti kapan saja - seluruh aplikasi langsung mengikuti. | Indonesian or English? | Switch whenever you like - the whole app follows along immediately. |
| `pin-lock` (store-13) | Tenang kalau HP dipinjam orang | Kunci aplikasi dengan empat angka, ditambah sidik jari kalau HP-mu mendukung. | Safe to hand your phone over | Lock the app with four digits, plus fingerprint if your phone supports it. |
| `backup` (store-14) | Riwayatmu tidak akan hilang | Simpan cadangan ke file milikmu sendiri - bisa dikunci dengan PIN dan dipulihkan kapan saja. | Your history, safe | Save a backup file you own - lock it with a PIN and restore it whenever you need it. |

---

## 4. Listing Play Store

**Subtitle**

- ID: `Siap sebelum menstruasi datang`
- EN: `Never be surprised by your period`

**Deskripsi (ID)**

> CycleJournal membantumu mengenali tubuhmu sendiri. Catat menstruasi, nyeri, dan suasana hati
> dalam dua ketukan, lalu lihat perkiraan menstruasi dan masa subur berikutnya di kalender yang
> mudah dibaca.
>
> Cocok untuk siapa saja: untuk remaja yang baru mulai mencatat, untuk kamu yang sibuk dan tidak
> mau lagi ketahuan mendadak, sampai untuk yang sedang program hamil dan ingin memantau tanda
> kesuburan seperti suhu pagi dan cairan serviks - semuanya dijelaskan dengan bahasa sederhana.
>
> Yang kamu dapat:
> - Perkiraan menstruasi dan masa subur berikutnya
> - Catatan harian cepat: aliran, nyeri, gejala, catatan
> - Kalender bulanan dan riwayat siklus
> - Laporan PDF siap dibawa ke dokter
> - Kunci PIN dan sidik jari, tanpa akun, data tersimpan di HP-mu
> - Mode gelap, dua bahasa, empat ukuran huruf

**Description (EN)**

> CycleJournal helps you understand your own body. Log your period, pain and mood in two taps,
> then see your next period and fertile days on a calendar that is easy to read.
>
> It works for anyone: if you are a teenager who just started tracking, if you are busy and want to
> stop being caught off guard, or if you are trying for a baby and want to follow signs like morning
> temperature and cervical fluid - each one explained in plain words.
>
> What you get:
> - Predictions for your next period and your fertile days
> - Fast daily log: flow, pain, symptoms, notes
> - Monthly calendar and cycle history
> - A PDF report ready to show your doctor
> - PIN and fingerprint lock, no account, data stays on your phone
> - Dark mode, two languages, four text sizes

---

## 5. Device & aset

| Device key | Ukuran (spesifikasi Play) | Jumlah scene | Bahasa |
|---|---|---|---|
| `pixel-10-pro` (HP) | 1080 × 1920 | 8 | id-ID, en-US |
| `tablet-7` (7") | 1200 × 1920 | 8 | id-ID, en-US |
| `tablet-10` (10") | 1600 × 2560 | 8 | id-ID, en-US |

Total 48 tile, semuanya terverifikasi sesuai ukuran di atas.

- Capture: `python scripts/capture_store.py <device> [locale] [scene ...]` — capture dari emulator,
  menunggu penanda layar + retry supaya tidak pernah menyimpan frame setengah jadi.
  **Penting:** HP harus di-capture pada **1279×2853** (density 520) agar screenshot pas di dalam
  bezel goldie; 1080×1920 membuat sisi kiri-kanan terpotong.
- Frame HP: `goldie frame --locale <locale>` (manifest capture ditulis per-locale).
- Frame tablet: `python scripts/frame_tablets.py` — goldie tidak menyediakan device tablet, jadi
  tile tablet disusun skrip ini dengan bahasa desain yang sama.
- Verifikasi: `goldie verify` (set HP) dan pemeriksaan ukuran otomatis di skrip tablet.
