/**
 * Goldie configuration for CycleJournal Play Store screenshots.
 *
 * Writing rules for the copy below, so the next edit keeps the same voice:
 *  - A headline names the problem the screen solves, not the feature it contains.
 *  - A subhead is one concrete sentence: who it helps and what they get.
 *  - The audience is every woman, not only someone trying to conceive - the home, log and
 *    privacy scenes speak to students and busy people, the fertility scenes to promil.
 *  - Plain advertising language. No "basal", "cervical mucus", "VAS", "biphasic", "luteal",
 *    "self-managed" or "AES-256", and Indonesian copy says "menstruasi", never "haid".
 *
 * Sixteen scenes cover every page: the four tabs, the sheets they open and the dialogs
 * behind them, plus the appearance and first-run screens.
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
      "en-US": "Never be surprised by your period",
      "id-ID": "Siap sebelum menstruasi datang",
    },
    developer: "CycleJournal Health",
    category: "Health & Fitness",
    rating: 4.9,
    ratingCount: "2.4K Ratings",
    ageRating: "12+",
    price: "Free",
    description: {
      "en-US":
        "CycleJournal helps you understand your own body. Log your period, pain and mood in two taps, then see your next period and fertile days on a calendar that is easy to read.\n\nIt works for anyone: if you are a teenager who just started tracking, if you are busy and want to stop being caught off guard, or if you are trying for a baby and want to follow signs like morning temperature and cervical fluid - each one explained in plain words.\n\nWhat you get:\n- Predictions for your next period and your fertile days\n- Fast daily log: flow, pain, symptoms, notes\n- Monthly calendar and cycle history\n- A PDF report ready to show your doctor\n- PIN and fingerprint lock, no account, data stays on your phone\n- Dark mode, two languages, four text sizes",
      "id-ID":
        "CycleJournal membantumu mengenali tubuhmu sendiri. Catat menstruasi, nyeri, dan suasana hati dalam dua ketukan, lalu lihat perkiraan menstruasi dan masa subur berikutnya di kalender yang mudah dibaca.\n\nCocok untuk siapa saja: untuk remaja yang baru mulai mencatat, untuk kamu yang sibuk dan tidak mau lagi ketahuan mendadak, sampai untuk yang sedang program hamil dan ingin memantau tanda kesuburan seperti suhu pagi dan cairan serviks - semuanya dijelaskan dengan bahasa sederhana.\n\nYang kamu dapat:\n- Perkiraan menstruasi dan masa subur berikutnya\n- Catatan harian cepat: aliran, nyeri, gejala, catatan\n- Kalender bulanan dan riwayat siklus\n- Laporan PDF siap dibawa ke dokter\n- Kunci PIN dan sidik jari, tanpa akun, data tersimpan di HP-mu\n- Mode gelap, dua bahasa, empat ukuran huruf",
    },
  },
  scenes: [
    {
      kind: "screenshot",
      id: "dashboard",
      flow: "store-01-dashboard",
      headline: { "en-US": "No more surprises", "id-ID": "Tidak ada lagi kejadian mendadak" },
      subhead: {
        "en-US": "Your next period stays on the home screen, so you are ready before it arrives - at school, on campus, or at work.",
        "id-ID": "Perkiraan menstruasi berikutnya selalu terlihat di layar utama, jadi kamu siap sebelum harinya tiba - di sekolah, kampus, atau kantor.",
      },
    },
    {
      kind: "screenshot",
      id: "calendar",
      flow: "store-02-calendar",
      headline: { "en-US": "Plan the month with confidence", "id-ID": "Rencanakan sebulan tanpa was-was" },
      subhead: {
        "en-US": "Every note you kept, your fertile days and your next period on one calendar that is easy to read.",
        "id-ID": "Semua catatanmu, masa subur, dan perkiraan menstruasi tersusun di satu kalender yang mudah dibaca.",
      },
    },
    {
      kind: "screenshot",
      id: "daily-log",
      flow: "store-03-daily-log",
      headline: { "en-US": "Log today in two taps", "id-ID": "Catat hari ini dalam dua ketukan" },
      subhead: {
        "en-US": "Pick what you feel today and you are done in seconds - easy to use between classes or on a break.",
        "id-ID": "Pilih yang kamu rasakan hari ini, selesai dalam hitungan detik - praktis dipakai di sela kelas atau jam istirahat.",
      },
    },
    {
      kind: "screenshot",
      id: "fertility-signs",
      flow: "store-04-clinical-detail",
      headline: {
        "en-US": "Trying for a baby? Track your fertility signs",
        "id-ID": "Sedang program hamil? Pantau tanda suburmu",
      },
      subhead: {
        "en-US": "Morning temperature, cervical fluid and pain in one place, so you know when your chances are highest.",
        "id-ID": "Suhu pagi, cairan serviks, dan nyeri tercatat rapi, jadi kamu tahu kapan peluang kehamilan paling besar.",
      },
    },
    {
      kind: "screenshot",
      id: "medical-report",
      flow: "store-06-medical-report",
      headline: { "en-US": "See your doctor with data, not guesses", "id-ID": "Ke dokter bawa data, bukan tebakan" },
      subhead: {
        "en-US": "One PDF with your cycle history, your pain and the patterns found in your notes - just show it at the appointment.",
        "id-ID": "Satu laporan PDF berisi riwayat siklus, nyeri, dan pola dari catatanmu - tinggal ditunjukkan saat konsultasi.",
      },
    },
    {
      kind: "screenshot",
      id: "privacy-security",
      flow: "store-08-security",
      headline: { "en-US": "Your journal stays private", "id-ID": "Isi jurnalmu tetap rahasia" },
      subhead: {
        "en-US": "PIN and fingerprint lock keep it away from prying eyes, and everything is stored on your phone - no account needed.",
        "id-ID": "Kunci PIN dan sidik jari menjaganya dari yang mengintip, dan semua data tersimpan di HP-mu - tanpa akun.",
      },
    },
    {
      kind: "screenshot",
      id: "dark-mode",
      flow: "store-15-dark-mode",
      headline: { "en-US": "Gentle on the eyes at night", "id-ID": "Nyaman dipakai sebelum tidur" },
      subhead: {
        "en-US": "Dark mode keeps the screen calm when you log before bed, and it is kinder to your battery.",
        "id-ID": "Mode gelap menenangkan mata saat mencatat sebelum tidur, dan lebih hemat baterai.",
      },
    },
    {
      kind: "screenshot",
      id: "no-account",
      flow: "store-16-onboarding",
      headline: { "en-US": "Start without an account", "id-ID": "Mulai tanpa daftar akun" },
      subhead: {
        "en-US": "Open the app and log today right away - no email, no login, your data stays on your phone.",
        "id-ID": "Buka aplikasi dan langsung catat hari ini - tanpa email, tanpa login, data tetap di HP-mu.",
      },
    },
  ],
};
