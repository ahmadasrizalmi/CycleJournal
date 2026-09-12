/**
 * Goldie configuration for CycleJournal Play Store screenshots.
 *
 * Eight scenes cover every page (dashboard, calendar, analysis, settings) and the features the
 * store listing leads with: two-tap logging, clinical biomarkers, the BBT curve, the doctor-ready
 * report, bilingual UI with adjustable text size, and the privacy controls.
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
      "en-US": "Private Cycle & Symptom Tracker",
      "id-ID": "Kalender Haid & Laporan SpOG",
    },
    developer: "CycleJournal Health",
    category: "Health & Fitness",
    rating: 4.9,
    ratingCount: "2.4K Ratings",
    ageRating: "12+",
    price: "Free",
    description: {
      "en-US":
        "CycleJournal is an offline-first, medical-grade menstrual cycle and fertility journal. It features FIGO-compliant anomaly detection, SpOG-ready A4 PDF reports, and zero-knowledge client-side encryption.",
      "id-ID":
        "CycleJournal adalah kalender haid dan jurnal kesuburan berstandar medis klinis. Dilengkapi deteksi anomali FIGO, ekspor PDF siap dokter SpOG, dan privasi mutlak zero-knowledge.",
    },
  },
  scenes: [
    {
      kind: "screenshot",
      id: "dashboard",
      flow: "store-01-dashboard",
      headline: { "en-US": "Your cycle, clinically tracked", "id-ID": "Siklus Anda, terpantau klinis" },
      subhead: {
        "en-US": "Period, ovulation and fertile window predicted from FIGO guidance.",
        "id-ID": "Haid, ovulasi, dan masa subur diprediksi berdasar panduan FIGO.",
      },
    },
    {
      kind: "screenshot",
      id: "calendar",
      flow: "store-02-calendar",
      headline: { "en-US": "See the whole cycle at once", "id-ID": "Lihat seluruh siklus sekilas" },
      subhead: {
        "en-US": "Every logged day, phase and fertile window on one month map.",
        "id-ID": "Semua catatan harian, fase, dan masa subur dalam satu peta bulan.",
      },
    },
    {
      kind: "screenshot",
      id: "daily-log",
      flow: "store-03-daily-log",
      headline: { "en-US": "Log a day in two taps", "id-ID": "Catat harian dalam dua ketukan" },
      subhead: {
        "en-US": "Bleeding and pain first; everything else stays out of the way until you need it.",
        "id-ID": "Darah haid dan nyeri lebih dulu; sisanya menunggu sampai Anda butuh.",
      },
    },
    {
      kind: "screenshot",
      id: "clinical-detail",
      flow: "store-04-clinical-detail",
      headline: { "en-US": "Basal temp, mucus, symptoms", "id-ID": "Suhu basal, lendir, gejala" },
      subhead: {
        "en-US": "Symptothermal detail in one panel, counted so you know what is still missing.",
        "id-ID": "Detail simptotermal dalam satu panel, lengkap dengan hitungan yang belum diisi.",
      },
    },
    {
      kind: "screenshot",
      id: "bbt-chart",
      flow: "store-05-bbt",
      headline: { "en-US": "Watch the thermal shift", "id-ID": "Pantau pergeseran suhu" },
      subhead: {
        "en-US": "A three-over-six curve confirms ovulation from your own morning readings.",
        "id-ID": "Kurva three-over-six memastikan ovulasi dari catatan suhu pagi Anda.",
      },
    },
    {
      kind: "screenshot",
      id: "medical-report",
      flow: "store-06-medical-report",
      headline: { "en-US": "A report your doctor can read", "id-ID": "Laporan siap dibaca dokter" },
      subhead: {
        "en-US": "One-page A4 PDF with FIGO metrics, anomalies and a printable summary.",
        "id-ID": "PDF A4 satu halaman berisi metrik FIGO, anomali, dan ringkasan siap cetak.",
      },
    },
    {
      kind: "screenshot",
      id: "bilingual-text-size",
      flow: "store-07-settings",
      headline: { "en-US": "Bilingual, and sized for you", "id-ID": "Dua bahasa, ukuran sesuai Anda" },
      subhead: {
        "en-US": "Indonesian or English, with four text sizes on top of your system setting.",
        "id-ID": "Indonesia atau Inggris, dengan empat ukuran teks di atas setelan sistem.",
      },
    },
    {
      kind: "screenshot",
      id: "privacy-security",
      flow: "store-08-security",
      headline: { "en-US": "Private by design", "id-ID": "Privat sejak dirancang" },
      subhead: {
        "en-US": "PIN lock, biometrics, discreet mode and encrypted backups you own.",
        "id-ID": "Kunci PIN, biometrik, mode samaran, dan cadangan terenkripsi milik Anda.",
      },
    },
  ],
};
