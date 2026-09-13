/**
 * Goldie configuration for CycleJournal Play Store screenshots.
 *
 * Fourteen scenes cover every page the app has: the four tabs (home, calendar, analysis,
 * settings), the sheets they open (daily log, clinical detail, calendar day detail) and the
 * dialogs behind them (mucus explainer, language, PIN, backup).
 *
 * The copy on the scenes and in the store listing is written in plain language: no FIGO, no
 * "symptothermal", no "zero-knowledge". Anyone should understand a screenshot without a medical
 * or technical background.
 *
 * The captures come from the demo-seeded release build, so the screens show a populated app.
 * Build it with: gradlew :app:assembleRelease -PdemoSeed=true
 */
export default {
  appRoot: ".",
  flowsDir: "./flows",
  android: {
    applicationId: "com.app.cyclejournal",
    // Demo-seeded build: goldie installs it with cleared data, so the app seeds itself on launch.
    appPath: "./out/demo/app-release-demo.apk",
  },
  devices: ["pixel-10-pro"],
  locales: ["en-US", "id-ID"],
  appearance: "light",
  frame: { variant: "17-pro-blue" },
  theme: {
    // Brand gradient: coral to pink.
    background: "linear-gradient(145deg, #FF8A71 0%, #FF5E7D 100%)",
    headlineColor: "#FFFFFF",
    subheadColor: "#FFF1F2",
    fontFamily: '-apple-system, "SF Pro Display", "Montserrat", "DM Sans", system-ui, sans-serif',
    copyHeightRatio: 0.22,
    deviceWidthRatio: 0.86,
    layout: "classic",
    template: "magazine",
  },
  store: {
    name: "CycleJournal",
    icon: "store_assets/play_store_icon_512.png",
    subtitle: {
      "en-US": "Period calendar and daily journal",
      "id-ID": "Kalender haid dan catatan harian",
    },
    developer: "CycleJournal Health",
    category: "Health & Fitness",
    rating: 4.9,
    ratingCount: "2.4K Ratings",
    ageRating: "12+",
    price: "Free",
    description: {
      "en-US":
        "CycleJournal is a period calendar and daily journal that works without internet. Write down your period, morning temperature, mucus and symptoms; the app estimates your next period and fertile days, points out anything worth a closer look, and can build a PDF report to bring to your doctor. Everything stays on your phone and can be locked with a PIN.",
      "id-ID":
        "CycleJournal adalah kalender haid dan catatan harian yang bisa dipakai tanpa internet. Catat haid, suhu pagi, lendir, dan gejalamu; aplikasi memperkirakan haid dan masa subur berikutnya, menandai hal yang perlu diperhatikan, dan bisa membuat laporan PDF untuk dibawa ke dokter. Semua data tersimpan di HP-mu dan bisa dikunci dengan PIN.",
    },
  },
  scenes: [
    {
      kind: "screenshot",
      id: "dashboard",
      flow: "store-01-dashboard",
      headline: { "en-US": "Everything about your cycle, in one place", "id-ID": "Semua soal haidmu, di satu layar" },
      subhead: {
        "en-US": "Your next period, fertile days and daily notes, all on the home screen.",
        "id-ID": "Perkiraan haid, masa subur, dan catatan harian dalam satu layar.",
      },
    },
    {
      kind: "screenshot",
      id: "calendar",
      flow: "store-02-calendar",
      headline: { "en-US": "See your whole month at a glance", "id-ID": "Lihat sebulan penuh sekilas" },
      subhead: {
        "en-US": "Every day you logged, plus period and fertile days, on one calendar.",
        "id-ID": "Semua hari yang kamu catat, plus hari haid dan masa subur, dalam satu kalender.",
      },
    },
    {
      kind: "screenshot",
      id: "daily-log",
      flow: "store-03-daily-log",
      headline: { "en-US": "Log your day in two taps", "id-ID": "Catat harian cuma dua ketukan" },
      subhead: {
        "en-US": "Bleeding and pain first; the rest waits until you need it.",
        "id-ID": "Darah haid dan nyeri dulu; sisanya menunggu sampai kamu butuh.",
      },
    },
    {
      kind: "screenshot",
      id: "clinical-detail",
      flow: "store-04-clinical-detail",
      headline: { "en-US": "Morning temperature, mucus and symptoms", "id-ID": "Suhu pagi, lendir, dan gejala" },
      subhead: {
        "en-US": "Every detail in one panel, with a marker for what is still empty.",
        "id-ID": "Semua detail dalam satu panel, lengkap dengan penanda yang belum diisi.",
      },
    },
    {
      kind: "screenshot",
      id: "bbt-chart",
      flow: "store-05-bbt",
      headline: { "en-US": "See your morning temperature pattern", "id-ID": "Lihat pola suhu pagimu" },
      subhead: {
        "en-US": "The curve helps estimate when your body releases an egg.",
        "id-ID": "Grafiknya membantu memperkirakan kapan sel telur dilepas.",
      },
    },
    {
      kind: "screenshot",
      id: "medical-report",
      flow: "store-06-medical-report",
      headline: { "en-US": "A report your doctor can read", "id-ID": "Laporan yang bisa dibaca dokter" },
      subhead: {
        "en-US": "One PDF page: cycle summary, temperature and anything worth a closer look.",
        "id-ID": "PDF satu halaman: ringkasan siklus, suhu, dan hal yang perlu diperhatikan.",
      },
    },
    {
      kind: "screenshot",
      id: "bilingual-text-size",
      flow: "store-07-settings",
      headline: { "en-US": "Your language, your text size", "id-ID": "Bahasa dan ukuran teks sesuai kamu" },
      subhead: {
        "en-US": "Indonesian or English, with four text sizes on top of your phone setting.",
        "id-ID": "Indonesia atau Inggris, dengan empat ukuran huruf di atas setelan HP-mu.",
      },
    },
    {
      kind: "screenshot",
      id: "privacy-security",
      flow: "store-08-security",
      headline: { "en-US": "Your data stays yours", "id-ID": "Datamu milikmu sendiri" },
      subhead: {
        "en-US": "PIN lock, fingerprint, and encrypted backups you keep yourself.",
        "id-ID": "Kunci PIN, sidik jari, dan cadangan terenkripsi yang kamu simpan sendiri.",
      },
    },
    {
      kind: "screenshot",
      id: "day-detail",
      flow: "store-09-day-detail",
      headline: { "en-US": "Tap a date to see that day", "id-ID": "Ketuk satu tanggal, lihat catatannya" },
      subhead: {
        "en-US": "One day in detail: bleeding, temperature, pain and mucus.",
        "id-ID": "Rincian satu hari: haid, suhu, nyeri, dan lendir.",
      },
    },
    {
      kind: "screenshot",
      id: "report-export",
      flow: "store-10-report-export",
      headline: { "en-US": "Share it, or keep the file", "id-ID": "Bagikan, atau simpan filenya" },
      subhead: {
        "en-US": "Download a print-ready PDF, or a CSV for your own spreadsheet.",
        "id-ID": "Unduh PDF siap cetak, atau CSV untuk diolah sendiri.",
      },
    },
    {
      kind: "screenshot",
      id: "mucus-info",
      flow: "store-11-mucus-info",
      headline: { "en-US": "Not sure what your mucus means?", "id-ID": "Bingung arti lendirmu?" },
      subhead: {
        "en-US": "Tap Keterangan and read what each type means for your fertile days.",
        "id-ID": "Ketuk Keterangan dan baca arti tiap jenis lendir untuk masa suburmu.",
      },
    },
    {
      kind: "screenshot",
      id: "language",
      flow: "store-12-language",
      headline: { "en-US": "Switch language any time", "id-ID": "Ganti bahasa kapan saja" },
      subhead: {
        "en-US": "Follow your phone, or pick Indonesian or English yourself.",
        "id-ID": "Ikuti bahasa HP, atau pilih Indonesia atau Inggris sendiri.",
      },
    },
    {
      kind: "screenshot",
      id: "pin-lock",
      flow: "store-13-pin",
      headline: { "en-US": "Lock the app with a PIN", "id-ID": "Kunci aplikasi dengan PIN" },
      subhead: {
        "en-US": "Four digits, plus fingerprint if your phone supports it.",
        "id-ID": "Empat angka, ditambah sidik jari kalau HP-mu mendukung.",
      },
    },
    {
      kind: "screenshot",
      id: "backup",
      flow: "store-14-backup",
      headline: { "en-US": "Make a backup you control", "id-ID": "Buat cadangan yang aman" },
      subhead: {
        "en-US": "Choose a plain backup, or one locked with a PIN.",
        "id-ID": "Pilih cadangan biasa, atau yang dikunci dengan PIN.",
      },
    },
  ],
};
