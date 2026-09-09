/**
 * Goldie configuration for CycleJournal Play Store & App Store Screenshots
 * Drives automated captures and compositing with the Coral-to-Pink brand gradient.
 */
export default {
  appRoot: ".",
  flowsDir: "./flows",
  android: {
    applicationId: "com.app.cyclejournal",
    appPath: "./app/build/outputs/apk/release/app-release.apk",
  },
  devices: ["pixel-10-pro"],
  locales: ["en-US", "id-ID"],
  appearance: "light",
  frame: { variant: "17-pro-blue" },
  theme: {
    // Brand Gradient: Coral (#FF8A71) to Pink (#FF5E7D)
    background: "linear-gradient(145deg, #FF8A71 0%, #FF5E7D 100%)",
    headlineColor: "#FFFFFF",
    subheadColor: "#FFF1F2",
    fontFamily: '-apple-system, "SF Pro Display", "Montserrat", "DM Sans", system-ui, sans-serif',
    copyHeightRatio: 0.22,
    deviceWidthRatio: 0.86,
    layout: "classic",
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
      headline: {
        "en-US": "Medical-Grade Cycle Tracking",
        "id-ID": "Pelacak Siklus Berstandar Medis",
      },
      subhead: {
        "en-US": "Accurate period, ovulation, and fertile predictions based on FIGO clinical guidelines.",
        "id-ID": "Prediksi haid, ovulasi, dan masa subur presisi berdasar konsensus klinis FIGO.",
      },
    },
    {
      kind: "screenshot",
      id: "daily-log",
      flow: "store-02-daily-log",
      headline: {
        "en-US": "Clinical Biomarker & Pain Journal",
        "id-ID": "Jurnal Gejala & Skala Nyeri Klinis",
      },
      subhead: {
        "en-US": "Log BBT, cervical mucus, flow, and VAS pain intensity with clean vector icons and zero emojis.",
        "id-ID": "Catat BBT, lendir serviks, darah haid, dan skala nyeri VAS dengan ikon vektor tanpa emoji.",
      },
    },
    {
      kind: "screenshot",
      id: "medical-report",
      flow: "store-03-medical-report",
      headline: {
        "en-US": "OB-GYN Ready Clinical Reports",
        "id-ID": "Laporan Medis Siap Dokter SpOG",
      },
      subhead: {
        "en-US": "Export 1-page A4 PDF medical summaries with FIGO metrics, red flags, and doctor sign-off.",
        "id-ID": "Ekspor ringkasan medis PDF A4 1-halaman dengan metrik FIGO, anomali, dan cap dokter.",
      },
    },
    {
      kind: "screenshot",
      id: "privacy-security",
      flow: "store-04-privacy-security",
      headline: {
        "en-US": "Absolute Zero-Knowledge Privacy",
        "id-ID": "Privasi Mutlak Zero-Knowledge",
      },
      subhead: {
        "en-US": "100% offline-first with hardware SQLCipher encryption and zero third-party tracking.",
        "id-ID": "100% offline dengan enkripsi hardware SQLCipher tanpa pelacak pihak ketiga.",
      },
    },
    {
      kind: "screenshot",
      id: "data-portability",
      flow: "store-05-data-portability",
      headline: {
        "en-US": "Total Data Sovereignty",
        "id-ID": "Kedaulatan Penuh Atas Data Anda",
      },
      subhead: {
        "en-US": "Export raw data to Excel CSV anytime or permanently destroy all records in one tap.",
        "id-ID": "Ekspor data mentah ke CSV Excel kapan saja atau musnahkan total dalam satu sentuhan.",
      },
    },
  ],
};
