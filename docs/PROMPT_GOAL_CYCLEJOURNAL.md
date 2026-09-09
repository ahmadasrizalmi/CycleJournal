# MISSION: CYCLEJOURNAL - PRODUCTION-GRADE IMPLEMENTATION

## 1. PROJECT IDENTITY & GOAL
You are a Staff Android Systems & Security Engineer tasked with building "CycleJournal" from scratch.
CycleJournal is an offline-first, zero-knowledge, medical-grade menstrual cycle and fertility journal built for Android Native. It features an automated FIGO-compliant anomaly screening engine, a native canvas-rendered SpOG-ready clinical PDF report generator, hardware-backed SQLCipher encryption, an AlarmManager clinical reminder scheduler, and a client-side encrypted disaster-recovery backup engine integrated with Cloudflare Workers and Cloudflare D1.

- Application Name: CycleJournal
- Target Platform: Android Native (minSdk 26, targetSdk 34)
- Package Name: com.app.cyclejournal
- Core Philosophy: Absolute Privacy, Zero Plaintext Leakage, Medical Utility (SpOG-Ready), Zero Vendor Lock-in.

---

## 2. TECHNICAL ARCHITECTURE & STACK
- Language: Kotlin 1.9+
- UI Framework: Jetpack Compose with Material 3 (MVI / Clean Architecture)
- Dependency Injection: Dagger Hilt 2.51+
- Local Persistence: Room 2.6+ with native SQLCipher 4.6+ (net.zetetic:sqlcipher-android)
- Hardware Cryptography: Android Keystore (TEE/StrongBox) Envelope Encryption for Database Master Key
- Client-Side Backup Crypto: AES-256-GCM + PBKDF2WithHmacSHA256 (100,000 iterations)
- PDF Engine: Android Native android.graphics.pdf.PdfDocument and android.graphics.Canvas (Zero external heavy dependencies)
- Background Scheduling:
  * WorkManager (androidx.work:work-runtime-ktx) for 30-day encrypted cloud backups (Wi-Fi + Charging constraints)
  * AlarmManager (setExactAndAllowWhileIdle) with BootReceiver for 06:00 AM waking BBT and H-2 Period warnings
- App Security: BiometricPrompt + 4-digit PIN fallback, WindowManager.LayoutParams.FLAG_SECURE, 30s background lock timeout
- Cloud Backend: Cloudflare Workers (TypeScript) + Cloudflare D1 (SQL) for zero-knowledge ciphertext blobs

---
## 2.1 VISUAL DESIGN SYSTEM & ICONOGRAPHY MANDATE
### A. Color Palette & Gradients
- **Primary Brand Gradient**: Linear gradient from Coral (`#FF8A71`) to Pink (`#FF5E7D`):
  ```kotlin
  val CoralPinkGradient = Brush.linearGradient(
      colors = listOf(Color(0xFFFF8A71), Color(0xFFFF5E7D))
  )
  val PrimaryCoral = Color(0xFFFF8A71)
  val PrimaryPink = Color(0xFFFF5E7D)
  ```
- **Background Canvas**: Pure White (`#FFFFFF`) for all screens, scaffolds, and sheets.
- **Surfaces & Cards**: Pure White (`#FFFFFF`) with 1dp border (`#F1F5F9`) or soft tinted background (`#FFF5F5`).
- **Typography Colors**:
  * Primary Text: Slate Dark (`#1E293B`)
  * Secondary / Muted Text: Slate Gray (`#64748B`)
  * Disabled / Placeholder: Slate Light (`#94A3B8`)
- **Phase & Clinical Colors**:
  * Menstruation: Soft Rose (`#FFE4E6`), Rose Dark text (`#BE123C`), Gradient accent (`#FF5E7D`)
  * Fertile Window: Soft Cyan (`#CFFAFE`), Cyan Dark text (`#0E7490`)
  * Ovulation Indicator: Vibrant Cyan (`#06B6D4`) with `#0E7490` ring dot
  * Clinical Alert / Red Flag: Background Red (`#FEF2F2`), Border Red (`#EF4444`), Text Dark Red (`#991B1B`)

### B. Strict Iconography Mandate: 100% Vector Icons (Zero Emojis)
- **CRITICAL CONSTRAINT**: NEVER use emojis in the UI, button labels, chips, headers, dialogs, or notifications.
- Every visual element MUST use official Android Material 3 Vector Icons (`androidx.compose.material.icons`) or custom SVG XML vectors.
- Standardized Icon Mapping:
  * Flow / Bleeding: `Icons.Outlined.WaterDrop` (tinted according to flow intensity level)
  * Basal Body Temperature: `Icons.Outlined.Thermostat`
  * Cervical Mucus: `Icons.Outlined.Opacity`
  * Pain Score (VAS): `Icons.Outlined.SentimentDissatisfied` or custom linear medical slider
  * Medication / Analgesic: `Icons.Outlined.Medication`
  * Calendar / Cycle: `Icons.Outlined.CalendarMonth`
  * PDF Report / Export: `Icons.Outlined.PictureAsPdf`
  * Cloud Sync / Security: `Icons.Outlined.CloudQueue`, `Icons.Outlined.Lock`, `Icons.Outlined.Fingerprint`
  * Warnings / Anomalies: `Icons.Outlined.Warning`
  * Settings: `Icons.Outlined.Settings`



### C. Official Brand Logo & Asset Generation Pipeline
- **Source Asset**: `logo cyclejournal.jpg` (2048x2048 high-resolution master asset).
- **Visual Symbolism & Identity**:
  * Central Pearlescent Sphere: Represents the ovum, precision, biological nucleus, and patient privacy.
  * 3D Dynamic Spiral Ribbon Folds: In coral-to-pink gradient tones (`#FF8A71` to `#FF5E7D`), forming an organic circular 'C' (Cycle, Cradle, Care).
  * Background: Pure White (`#FFFFFF`).
- **Automated Asset Generation (`scripts/generate_app_icons.py`)**:
  * Android Launcher Icons:
    - `app/src/main/res/mipmap-mdpi/` (48x48)
    - `app/src/main/res/mipmap-hdpi/` (72x72)
    - `app/src/main/res/mipmap-xhdpi/` (96x96)
    - `app/src/main/res/mipmap-xxhdpi/` (144x144)
    - `app/src/main/res/mipmap-xxxhdpi/` (192x192)
    (Both standard `ic_launcher.png` and circular `ic_launcher_round.png`).
  * Clinical PDF Header Emblem: `app/src/main/res/drawable/logo_pdf_header.png` (128x128 PNG).
  * Google Play Store High-Res Icon: `store_assets/play_store_icon_512.png` (512x512 PNG, 32-bit).
  * Google Play Feature Graphic: `store_assets/feature_graphic_1024x500.png` (1024x500 PNG with coral-to-pink gradient background and logo emblem).
  * Execution: Run `python scripts/generate_app_icons.py` before release builds.

## 2.3 IDENTITY, AUTHENTICATION & PRIVACY ARCHITECTURE
CycleJournal adopts an **Anonymous-First, Zero-Profiling Identity Model**:
- **Primary Authentication (Default & Recommended)**:
  * **4-Digit Local PIN + Biometrics**: User sets a 4-digit PIN stored as a SHA-256 hash in `EncryptedSharedPreferences`. Supported by Android `BiometricPrompt` (fingerprint/face unlock).
  * **Anonymous User ID**: Cryptographically generated UUID v4 hash formatted as `px-<UUID-first-12>` (e.g. `px-7f9a2b1c4e0d`). Acts as the record key on Cloudflare D1 without revealing name, email, or device IMEI.
  * **Zero Mandatory Accounts**: The user can use 100% of tracking, analytics, and PDF export features without providing email, phone number, or social media logins.
- **Optional Cloud Identity (Hybrid Google Sign-In)**:
  * Positioned strictly in `Screen.Settings` as an **optional convenience**: *"Tautkan Akun Google untuk Pemulihan Cloud Otomatis"*.
  * Privacy Guarantee: Even if linked to a Google account, the data payload uploaded to Cloudflare D1 remains an opaque ciphertext blob encrypted client-side via AES-256-GCM using the user's PIN/passphrase. Google never sees plaintext menstrual health records.
## 2.2 INTERNATIONALIZATION & BILINGUAL ARCHITECTURE (EN-FIRST + ID)
To capture high-LTV global markets while dominating domestic Indonesian search volume:
- **Primary Base Language (Default)**: English (`app/src/main/res/values/strings.xml`)
  * Professional international clinical terminology: "Period", "Fertile Window", "Ovulation", "Dysmenorrhea", "OB-GYN Clinical Report", "Zero-Knowledge Backup", "Danger Zone".
- **Localized Language**: Indonesian (`app/src/main/res/values-id/strings.xml`)
  * Localized medical & colloquial terminology: "Haid / Menstruasi", "Masa Subur", "Ovulasi", "Dismenore / Nyeri Haid", "Laporan Medis Siap SpOG", "Cadangan Zero-Knowledge", "Zona Bahaya".
- **Per-App Language Preferences (Android 13+ / API 33+)**:
  * Configure `res/xml/locales_config.xml` declaring `en` and `id`.
  * Register in `AndroidManifest.xml` via `android:localeConfig="@xml/locales_config"`.
- **Compose Localization Rule**: Every user-visible string in Composables MUST resolve through `stringResource(id = R.string.<name>)`. Hardcoded string literals in UI code are strictly prohibited.
- **Date & Number Formatting**: Use `DateTimeFormatter.ofPattern(..., Locale.getDefault())` and localized decimal formats for BBT temperatures.

## 3. PACKAGE & DIRECTORY STRUCTURE
Ensure strict package organization under `app/src/main/java/com/app/cyclejournal/`:
```
com.app.cyclejournal/
├── CycleApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── Converters.kt
│   │   ├── dao/
│   │   │   ├── DailyLogDao.kt
│   │   │   └── CycleDao.kt
│   │   └── entity/
│   │       ├── DailyLogEntity.kt
│   │       ├── CycleEntity.kt
│   │       └── Enums.kt (FlowIntensity, CervicalMucusType, AnomalyType)
│   ├── remote/
│   │   ├── CloudflareBackupClient.kt
│   │   └── model/BackupDto.kt
│   ├── backup/
│   │   ├── BackupJsonParser.kt
│   │   └── BackupPayloadSerializer.kt
│   └── preferences/
│       ├── OnboardingPreferences.kt
│       └── UserPreferences.kt
├── domain/
│   ├── model/
│   │   ├── CycleStats.kt
│   │   ├── FertilePrediction.kt
│   │   └── AnomalyAlert.kt
│   ├── engine/
│   │   ├── ClinicalCycleEngine.kt
│   │   └── CycleAggregator.kt
│   └── manager/
│       ├── DataRestoreManager.kt
│       └── DataWipeManager.kt
├── security/
│   ├── DatabaseKeyManager.kt (Keystore Envelope Encryption)
│   ├── BackupCryptoEngine.kt (AES-256-GCM + PBKDF2)
│   ├── SecurityPinManager.kt (EncryptedSharedPreferences PIN Hash)
│   └── BiometricAuthHelper.kt
├── export/
│   ├── pdf/
│   │   ├── ClinicalPdfReportGenerator.kt
│   │   └── PdfShareHelper.kt
│   └── csv/
│       └── CsvExportHelper.kt
├── scheduler/
│   ├── alarm/
│   │   ├── CycleAlarmScheduler.kt
│   │   ├── CycleNotificationReceiver.kt
│   │   └── BootReceiver.kt
│   ├── worker/
│   │   ├── MonthlyBackupWorker.kt
│   │   └── BackupScheduler.kt
│   └── notification/
│       └── NotificationChannelManager.kt
├── billing/
│   └── UserEntitlementManager.kt
├── di/
│   ├── DatabaseModule.kt
│   └── AppModule.kt
└── ui/
    ├── navigation/
    │   ├── AppNavigation.kt
    │   └── Screen.kt
    ├── theme/ (Color, Type, Theme)
    ├── onboarding/
    │   ├── OnboardingScreen.kt
    │   └── OnboardingViewModel.kt
    ├── home/
    │   ├── CycleHomeScreen.kt
    │   └── CycleViewModel.kt
    ├── components/
    │   ├── CycleCalendarView.kt
    │   └── DailyLogInputSheet.kt
    ├── security/
    │   └── PinLockScreen.kt
    └── settings/
        ├── SettingsScreen.kt
        └── SettingsViewModel.kt
```

---

## 4. PHASE-BY-PHASE IMPLEMENTATION REQUIREMENTS

### PHASE 1: DATA LAYER, ENUMS & HARDWARE-BACKED SQLCIPHER
1. **Enums & Entities**:
   - `FlowIntensity(val level: Int)`: `NONE(0)`, `SPOTTING(1)`, `LIGHT(2)`, `MEDIUM(3)`, `HEAVY(4)`.
   - `CervicalMucusType`: `NONE`, `DRY`, `STICKY`, `CREAMY`, `WATERY`, `EGG_WHITE`.
   - `AnomalyType(val code: String, val description: String)`:
     * `ANO_01`: Oligomenorrhea (> 38 days)
     * `ANO_02`: Polymenorrhea (< 24 days)
     * `ANO_03`: Cycle Irregularity (range >= 8 days across historical cycles)
     * `ANO_04`: Prolonged Bleeding (> 8 consecutive days)
     * `ANO_05`: Intermenstrual Bleeding (spotting after Day 8, excluding ovulatory egg-white)
     * `ANO_06`: Short Luteal Phase (< 10 days)
     * `ANO_07`: Severe Dysmenorrhea (VAS >= 7 or VAS >= 5 with analgesics)
   - `DailyLogEntity` (table `daily_logs`): `@PrimaryKey val date: LocalDate`, `flow: FlowIntensity`, `basalBodyTempCelsius: Double?`, `cervicalMucus: CervicalMucusType`, `painVasScore: Int` (0..10), `painLocation: String?`, `takenAnalgesic: Boolean`, `notes: String?`.
   - `CycleEntity` (table `cycle_records`): `@PrimaryKey(autoGenerate = true) val id: Long`, `startDate: LocalDate`, `endDate: LocalDate?`, `periodDurationDays: Int`, `cycleLengthDays: Int?`, `confirmedOvulationDate: LocalDate?`.
2. **Room DAOs**:
   - `DailyLogDao`: Upsert, `getLogByDate`, `getLogsBetween`, `getRecentLogs(limit)`, `getAllLogsAsc`, `getAllLogsFlow()`.
   - `CycleDao`: Insert, Update, `getLatestCycle`, `getCycleForDate`, `getCompletedCycles`, `getAllCycles`, `getAllCyclesFlow()`, `clearAllCycles`, `deleteCycle`.
3. **DatabaseKeyManager (Hardware-Backed Envelope Encryption)**:
   - Generate an AES-256 key inside `AndroidKeyStore` (alias: `cycle_journal_master_key`, `KeyProperties.PURPOSE_ENCRYPT or PURPOSE_DECRYPT`, `BLOCK_MODE_GCM`, `ENCRYPTION_PADDING_NONE`).
   - Generate a 32-byte cryptographically secure random key via `SecureRandom` for SQLCipher.
   - Encrypt the 32-byte key using Keystore Master Key and save `[IV (12B)] + [EncryptedKey]` as Base64 in `db_key_secure_storage.xml`.
   - On `getDatabasePassphrase()`, decrypt using the Keystore key.
   - Inject passphrase into `net.zetetic.database.sqlcipher.SupportFactory(passphrase)`.
   - **Defense-in-depth**: Call `Arrays.fill(passphrase, 0.toByte())` immediately after passing to `SupportFactory`.

### PHASE 2: CLINICAL ENGINE, STATE MACHINE & UNIT TESTS
1. **ClinicalCycleEngine**:
   - `calculateCycleStats(completedCycles: List<CycleEntity>): CycleStats?`:
     * Compute mean cycle length $\bar{L}$.
     * Compute sample standard deviation $\sigma = \sqrt{\frac{\sum (L_i - \bar{L})^2}{n - 1}}$ for $n > 1$.
     * Compute minLength, maxLength, averagePeriodDuration.
   - `predictFertileWindow(lastPeriodStartDate: LocalDate, averageCycleLength: Double = 28.0): FertilePrediction`:
     * $T_{\text{next}} = T_{\text{last}} + \text{round}(\text{averageCycleLength})$
     * Ovulation date $O = T_{\text{next}} - 14$
     * Fertile window: $[O - 5, O + 1]$ (6-day window).
   - `detectSymptothermalOvulation(logsSortedByDate: List<DailyLogEntity>): LocalDate?`:
     * Implement strict 3-over-6 rule: minimum 6 baseline BBT entries + 3 shifted entries ($\ge 0.2^\circ\text{C}$ above max baseline).
     * Return $Day_6$ (last day before temperature shift).
   - `evaluateAnomalies(currentCycle: CycleEntity?, historicalCycles: List<CycleEntity>, recentDailyLogs: List<DailyLogEntity>): List<AnomalyAlert>`:
     * Evaluate ANO_01 to ANO_07 based on FIGO clinical rules.
2. **CycleAggregator**:
   - State machine governing cycle inception and progression.
   - `isTrueBleeding`: `flow in [LIGHT, MEDIUM, HEAVY]` (`SPOTTING` excluded).
   - `MIN_DAYS_FOR_NEW_CYCLE = 20L`.
   - On log saved:
     * Days 0..10: update `periodDurationDays`.
     * Days 11..19: retain as intermenstrual bleeding without splitting.
     * Days $\ge 20$ with active bleeding: close previous cycle (`endDate = log.date - 1`, `cycleLengthDays = daysSinceStart`), create new cycle (`startDate = log.date`, `periodDurationDays = 1`).
   - `reconcileAllHistory()`: Deterministic replay from all ascending daily logs, rebuilding cycles atomically.
3. **Unit Tests (`ClinicalCycleEngineTest.kt`)**:
   - JUnit 4 test suite validating:
     * FIGO statistical formula accuracy and null handling on empty/invalid lists.
     * Calendar fertile predictions.
     * Symptothermal BBT 3-over-6 valid shift and invalid/unsustained shift drop.
     * Flagging for all FIGO anomalies (ANO_01 through ANO_07).

### PHASE 3: SPOG-READY CLINICAL PDF GENERATOR & FILEPROVIDER
1. **ClinicalPdfReportGenerator**:
   - Use `android.graphics.pdf.PdfDocument` and `android.graphics.Canvas`.
   - Page dimensions: Standard A4 at 72 DPI ($595 \times 842$ pt). Margins: 40 pt horizontal. Usable width: 515 pt.
   - Styling: Professional monochrome palette with high-contrast red accents for clinical red flags.
   - Sections to draw:
     1. Header: Render official CycleJournal emblem (`logo_pdf_header.png` at 36x36 pt) beside document title "LAPORAN KLINIS SIKLUS MENSTRUASI & BIOMARKER", subtitle, horizontal divider line, Anonymous Patient ID (`PX-...`), and right-aligned export date.
     2. FIGO Summary Grid Box: 4 columns (Rata-rata Siklus, Variabilitas SD $\sigma$, Rentang Min-Max, Rata-rata Durasi Haid).
     3. Red Flags & Anomalies Section: Alert cards (take up to 3) with red border `#EF4444`, red background `#FEF2F2`, and dark red text `#991B1B`. Show neutral placeholder if empty.
     4. Historical Cycles Table: 6 most recent cycles with columns (Tanggal Awal, Tanggal Akhir, Panjang Siklus, Durasi Haid, Estimasi Ovulasi).
     5. Doctor Notes & Stamp Box: Height 110 pt, prompt "Diagnosa Medis / Rekomendasi Terapi:", and formal signature line "Tanda Tangan & Cap Dokter" at the bottom right.
   - Execution: Dispatch on `Dispatchers.IO`.
2. **FileProvider & Sharing Helper (`PdfShareHelper`)**:
   - Setup `res/xml/file_paths.xml` with `<cache-path name="pdf_reports" path="reports/" />` and `<files-path name="permanent_reports" path="reports/" />`.
   - Setup `FileProvider` in `AndroidManifest.xml` (`${applicationId}.fileprovider`, `exported="false"`, `grantUriPermissions="true"`).
   - Dispatch `Intent.ACTION_SEND` with MIME `application/pdf`, `FLAG_GRANT_READ_URI_PERMISSION`, and `ClipData.newRawUri` for Android 10+ permission propagation.

### PHASE 4: CLIENT-SIDE CRYPTOGRAPHY & CLOUDFLARE D1 BACKUP
1. **BackupCryptoEngine**:
   - Transformation: `AES/GCM/NoPadding`.
   - Key derivation: `PBKDF2WithHmacSHA256`, 100,000 iterations, 256-bit key.
   - Parameters: 16-byte random salt, 12-byte random IV, 128-bit authentication tag.
   - Binary buffer layout: `[Salt 16B] + [IV 12B] + [Ciphertext + Tag]`, encoded to Base64 (`NO_WRAP`).
   - SHA-256 hex checksum calculation over Base64 payload.
   - Decryption: Unpack salt & IV, re-derive key, catch `AEADBadTagException` to detect invalid PIN.
2. **Cloudflare Worker & D1 Backend (`backend/cloudflare/`)**:
   - `schema.sql`: Table `monthly_backups (user_id TEXT, backup_month TEXT, cipher_payload TEXT, payload_hash TEXT, created_at INTEGER, updated_at INTEGER, PRIMARY KEY(user_id, backup_month))`. Index `idx_user_backups ON monthly_backups(user_id, updated_at DESC)`.
   - `index.ts` (TypeScript Worker Handler):
     * `POST /api/backup`: Validate YYYY-MM month format, verify SHA-256 hash match against `cipher_payload`, execute upsert.
     * `GET /api/backup`: Fetch specific backup month or return list of available backup months.
     * `DELETE /api/backup`: Purge backups for `user_id` (GDPR Right to be Forgotten).
3. **Disaster Recovery Restore Pipeline (`DataRestoreManager`)**:
   - Download encrypted blob from Cloudflare Worker via `GET /api/backup`.
   - Verify SHA-256 integrity hash.
   - Decrypt with `BackupCryptoEngine.decrypt(payload, passphrase)`.
   - Deserialize JSON to `DailyLogEntity` records via `BackupJsonParser`.
   - Upsert logs into Room DB and trigger `cycleAggregator.reconcileAllHistory()`.

### PHASE 5: BACKGROUND WORKERS, ALARMS & SECURITY GATING
1. **WorkManager Backup (`MonthlyBackupWorker` & `BackupScheduler`)**:
   - 30-day periodic worker with 2-day flex window.
   - Constraints: `setRequiresCharging(true)`, `setRequiredNetworkType(NetworkType.UNMETERED)`, `setRequiresBatteryNotLow(true)`.
   - Retrieve backup passphrase and anonymous ID from `EncryptedSharedPreferences`.
   - Serialize Room data, encrypt via `BackupCryptoEngine`, and POST to Worker endpoint.
2. **AlarmManager Scheduling (`CycleAlarmScheduler`)**:
   - Morning BBT reminder: Exact alarm at 06:00 AM (`ACTION_TRIGGER_BBT`). In `CycleNotificationReceiver`, show notification and self-chain schedule tomorrow's alarm at 06:00 AM.
   - H-2 Period prediction: Exact alarm at 09:00 AM on predicted start date minus 2 days (`ACTION_TRIGGER_PERIOD`).
   - `BootReceiver`: On `ACTION_BOOT_COMPLETED`, query latest cycles and reschedule BBT and H-2 alarms.
   - Handle Android 12+ `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` and Android 13+ `POST_NOTIFICATIONS`.
3. **App Lock & Inactivity Guard**:
   - `SecurityPinManager`: Store SHA-256 hashed PIN in `EncryptedSharedPreferences`.
   - `BiometricAuthHelper`: BiometricPrompt with fallback to custom PIN screen.
   - `MainActivity`: Add `WindowManager.LayoutParams.FLAG_SECURE`. Observe `ProcessLifecycleOwner`: if app is backgrounded > 30 seconds, lock app and display `PinLockScreen`.

### PHASE 6: JETPACK COMPOSE UI & WIREFRAME SPECIFICATIONS

#### Wireframe Blueprint 1: Home Dashboard (`CycleHomeScreen.kt`)
- **Scaffold Background**: Pure White (`#FFFFFF`).
- **Top App Bar**:
  * Branding: Official CycleJournal logo emblem (28x28 dp) beside Title "CycleJournal" (Slate Dark `#1E293B`, Bold 20sp).
  * Actions: Quick Report Export icon (`Icons.Outlined.PictureAsPdf`) and Settings icon (`Icons.Outlined.Settings`).
- **Cycle Status Hero Card (Coral-to-Pink Gradient Container)**:
  * Background: `CoralPinkGradient` with 16dp rounded corners.
  * Content: Current Day of Cycle (e.g. "Hari ke-14"), Phase Chip (e.g. "Jendela Subur" / "Fase Folikuler"), Predicted Ovulation Countdown ("Ovulasi dalam 2 hari"), White vector icon.
  * Text Color: Pure White (`#FFFFFF`).
- **Monthly Calendar Matrix (`CycleCalendarView.kt`)**:
  * Month Navigator: Previous/Next arrow buttons, Month & Year text (`MMMM yyyy`).
  * Day of Week Bar: Mon to Sun labels (Slate Gray `#64748B`).
  * Calendar Grid: 7-column layout.
    - Menstruation days: Soft Rose background (`#FFE4E6`), Rose Dark text (`#BE123C`).
    - Fertile window: Soft Cyan background (`#CFFAFE`), Cyan Dark text (`#0E7490`).
    - Ovulation day: 1.5dp border `#0E7490` with 4dp center circular dot.
    - Today indicator: 1dp border `#94A3B8`.
    - Selected date: 2dp border with Primary Coral (`#FF8A71`).
  * Phase Legend: Clean vector dots with text "Menstruasi", "Masa Subur", "Ovulasi".
- **Bottom Floating / Sticky Action Button**:
  * Gradient Button (`CoralPinkGradient`), Rounded 28dp, vector icon `Icons.Outlined.EditCalendar` + Text "Catat Gejala Hari Ini".

#### Wireframe Blueprint 2: Daily Log Input Sheet (`DailyLogInputSheet.kt`)
- **Component**: ModalBottomSheet with Pure White surface (`#FFFFFF`) and 20dp top rounded corners.
- **Header**: Selected Date ("Senin, 14 September 2026"), close icon button.
- **Section 1 - Intensitas Pendarahan Haid (Flow)**:
  * 5 Horizontal FilterChips with vector icon `Icons.Outlined.WaterDrop`:
    - "Tidak" (None): Light Gray `#E2E8F0`
    - "Bercak" (Spotting): Rose Soft `#FFE4E6`
    - "Ringan" (Light): Soft Pink `#FDA4AF`
    - "Sedang" (Medium): Coral Pink `#F43F5E`
    - "Deras" (Heavy): Deep Red `#BE123C`
- **Section 2 - Skala Nyeri Klinis (VAS 0–10)**:
  * Header: "Skala Nyeri (VAS)" with numeric display on right: e.g. "7 / 10" in Red (`#DC2626`).
  * Slider: 0f..10f with 9 steps, Active Track colored with `CoralPinkGradient`.
  * Medical Descriptive Helper Text:
    - 0: "0: Tidak ada nyeri."
    - 1–3: "1–3: Nyeri ringan, aktivitas normal."
    - 4–6: "4–6: Nyeri sedang, mengganggu fokus."
    - 7–10: "7–10: Nyeri hebat, butuh istirahat/bedrest."
  * Dynamic Red Flag Card: Appears when VAS >= 7 with red border `#EF4444`, red background `#FEF2F2`, vector icon `Icons.Outlined.Warning`, warning text "Nyeri level tinggi terdeteksi. Ditandai pada laporan SpOG sebagai indikasi dismenore/endometriosis."
- **Section 3 - Suhu Basal Tubuh (BBT) & Analgesik**:
  * OutlinedTextField for BBT with leading icon `Icons.Outlined.Thermostat` and placeholder "36.50 °C".
  * Checkbox for Analgesic with leading icon `Icons.Outlined.Medication` and label "Konsumsi Obat Anti-Nyeri".
- **Section 4 - Karakteristik Lendir Serviks (Sintotermal)**:
  * 4 Chips with vector icon `Icons.Outlined.Opacity`: "Kering" (None), "Krim" (Creamy), "Cair" (Watery), "Putih Telur" (Egg White).
- **Section 5 - Catatan Tambahan & Tombol Simpan**:
  * OutlinedTextField for optional notes.
  * Primary Button: `CoralPinkGradient` background, text "Simpan Catatan Harian", vector icon `Icons.Outlined.Check`.

#### Wireframe Blueprint 3: Medical PDF Report Screen
- **Card Preview**: A4 Aspect Ratio preview card with shadow and 1dp border `#E2E8F0`.
- **Action Bar**:
  * "Ekspor PDF Siap SpOG" (Primary gradient button with `Icons.Outlined.PictureAsPdf`).
  * "Ekspor CSV Portabel" (Outlined button with Primary Coral stroke and `Icons.Outlined.TableChart`).

#### Wireframe Blueprint 4: Settings & Security Screen (`SettingsScreen.kt`)
- **Profile & Anonymous ID Card**: Displays `px-xxxxxxxxxxxx` badge with copy vector icon `Icons.Outlined.ContentCopy`.
- **Cloud Sync Card**: Cloudflare D1 status badge ("Terkoneksi / Zero-Knowledge"), Last Sync timestamp, "Cadangkan Sekarang" button.
- **Security Section**: Toggle Biometric (Fingerprint/Face), Change PIN, Inactivity Timeout slider.
- **Danger Zone Card (Red Tinted `#FEF2F2`)**:
  * Border Red `#EF4444`, icon `Icons.Outlined.DeleteForever`.
  * Button "Hapus Semua Data (Nuke)".
  * Modal Dialog with text confirmation input: User must type "HAPUS" to confirm complete wipe.

#### Wireframe Blueprint 5: Onboarding Wizard (`OnboardingScreen.kt`)
- Multi-step pager (Welcome & Disclaimer -> PIN Setup -> Baseline Cycle -> Anonymous ID).
- Welcome screen prominently displays the official CycleJournal logo emblem (120x120 dp) with brand gradient headline "CycleJournal" and value proposition subtitle.
- Clean white layout with coral-to-pink accents, numeric keypad, and date picker.
### PHASE 7: GDPR NUKE OPTION & CSV EXPORT
1. **DataWipeManager (Complete Wipe Cascade)**:
   - Step 1: Issue HTTP `DELETE` to Cloudflare Worker to purge D1 cloud records.
   - Step 2: Cancel AlarmManager alarms and WorkManager backup tasks.
   - Step 3: Close Room and delete database files (`.db`, `.db-wal`, `.db-shm`).
   - Step 4: Recursively delete temporary and permanent report files.
   - Step 5: Wipe all `EncryptedSharedPreferences`.
   - Step 6: Invoke `databaseKeyManager.destroyDatabaseKey()` and delete all Keystore aliases.
   - Step 7: Restart/exit application cleanly with `FLAG_ACTIVITY_CLEAR_TOP` and `Runtime.getRuntime().exit(0)`.
2. **CsvExportHelper**:
   - Write Byte Order Mark (`\uFEFF`) at byte 0 for Excel UTF-8 compatibility.
   - Section 1: `# RIWAYAT SIKLUS MENSTRUASI` (ID, Tanggal Mulai, Tanggal Akhir, Panjang Siklus, Durasi Haid, Estimasi Ovulasi).
   - Section 2: `# LOG HARIAN BIOMARKER & GEJALA` (Tanggal, Flow, BBT, Lendir, VAS, Lokasi, Analgesik, Catatan).
   - Apply RFC 4180 escaping and share via `FileProvider`.

### PHASE 8: COMPREHENSIVE UNIT TESTING & CLINICAL VERIFICATION
All tests run locally on JVM without an Android emulator (`./gradlew testDebugUnitTest`).
1. **`ClinicalCycleEngineTest.kt` (Clinical Math & FIGO Consensus)**:
   - `testCalculateCycleStats_normalVariation()`: Sample cycle lengths [26, 28, 30] -> Mean = 28.0, Sample StdDev $\sigma = 2.0$, Min = 26, Max = 30.
   - `testCalculateCycleStats_emptyOrInvalidList()`: Returns `null` on empty or open cycles without throwing crashes.
   - `testPredictFertileWindow_standardCalendar()`: Cycle start 2026-09-01 with length 28 -> Next period 2026-09-29, Ovulation 2026-09-15, Fertile window [2026-09-10 to 2026-09-16].
   - `testDetectSymptothermalOvulation_valid3Over6Shift()`: 6 baseline days (max 36.40°C) followed by 3 days >= 36.60°C -> Successfully confirms ovulation at Day 6.
   - `testDetectSymptothermalOvulation_unsustainedShiftReturnsNull()`: 3rd shifted day drops back below threshold -> Rejects and returns `null`.
   - `testAnomalies_oligomenorrhea()`: Cycle > 38 days triggers `ANO_01`.
   - `testAnomalies_polymenorrhea()`: Cycle < 24 days triggers `ANO_02`.
   - `testAnomalies_cycleIrregularity()`: Max - Min >= 8 days across historical cycles triggers `ANO_03`.
   - `testAnomalies_prolongedBleeding()`: > 8 consecutive bleeding days of LIGHT/MEDIUM/HEAVY triggers `ANO_04`.
   - `testAnomalies_intermenstrualBleeding()`: Spotting on cycle Day 15 with non-ovulatory mucus triggers `ANO_05`.
   - `testAnomalies_severeDysmenorrhea()`: Pain VAS score >= 7 triggers `ANO_07`.
2. **`CycleAggregatorTest.kt` (State Machine & Reconciliation)**:
   - `testNewCycleCreation_activeBleedingThreshold()`: Bleeding on Day 22 creates new cycle; spotting on Day 22 does NOT create new cycle.
   - `testMidCycleSpotting_treatedAsIntermenstrual()`: Bleeding on Days 11..19 updates duration or triggers IMB without creating duplicate cycles.
   - `testReconcileAllHistory_deterministicReconstruction()`: Replays chronological logs and validates identical reconstructed `CycleEntity` rows.
3. **`BackupCryptoEngineTest.kt` (Cryptography & Tamper Proofing)**:
   - `testEncryptionDecryptionRoundtrip()`: Raw JSON encrypted with passphrase decrypts back to identical string.
   - `testDecryptionWithWrongPassphrase_throwsBadTagException()`: Decrypting with wrong passphrase throws `AEADBadTagException` (never silent corruption).
   - `testPayloadIntegrity_checksumMismatchFails()`: Tampered Base64 ciphertext fails SHA-256 verification.
4. **`CsvExportHelperTest.kt` (Data Portability)**:
   - `testCsvUtf8Bom_writtenCorrectly()`: Validates byte 0 starts with `\uFEFF`.
   - `testCsvEscaping_quotesAndCommas()`: Verifies commas, quotes, and newlines are RFC 4180 escaped with double-double quotes (`""`).

### PHASE 9: RELEASE BUILD PIPELINE (.APK & .AAB) & MONETIZATION
1. **Release Compilation Commands**:
   - Generate both standard testing `.apk` and store-ready `.aab`:
     ```bash
     ./gradlew assembleRelease bundleRelease
     ```
   - Verified Output Paths:
     * Release Sideload APK: `app/build/outputs/apk/release/app-release.apk`
     * Google Play Bundle: `app/build/outputs/bundle/release/app-release.aab`
2. **Signing Configuration (`signingConfigs.release`)**:
   - Keystore generation:
     ```bash
     keytool -genkey -v -keystore cyclejournal-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias cyclejournal -storepass <STORE_PASS> -keypass <KEY_PASS>
     ```
   - Secure credentials injected via `keystore.properties` (never committed to git) or environment variables (`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).
3. **Monetization Architecture (`UserEntitlementManager`)**:
   - SKU: `lifetime_pro_access` (Non-consumable IAP).
   - Free Tier: Non-Personalized AdMob banner on non-critical screens (Settings only, NEVER on DailyLogInputSheet), rewarded video gate for PDF exports.
   - Guardrail: Append `bundleOf("npa" to "1")` to all AdMob requests.
   - Pro Tier: Hard kill-switch permanently disabling AdMob SDK components and unlocking unlimited direct PDF exports and cloud sync.
4. **ProGuard / R8 Rules (`proguard-rules.pro`)**:
   - Add keep rules for SQLCipher native JNI (`net.zetetic.**`), SQLite bindings, Room runtime classes, all entities, enums (`FlowIntensity`, `CervicalMucusType`, `AnomalyType`), AndroidX Security Crypto, Keystore, WorkManager workers, and BroadcastReceivers.

### PHASE 10: AUTOMATED PLAY STORE SCREENSHOTS & ASSETS (VIA GOLDIE)
Automate generation of store-ready marketing screenshots using Goldie CLI (`goldie`):
1. **Configuration (`goldie.config.ts`)**:
   - Devices: `pixel-10-pro` (frames screenshots into official Android flagship bezels).
   - Locales: Bilingual generation for `en-US` and `id-ID`.
   - Theme Background: Brand Coral-to-Pink linear gradient:
     `linear-gradient(145deg, #FF8A71 0%, #FF5E7D 100%)`.
   - Typography: Headlines `#FFFFFF`, Subheads `#FFF1F2`, font stack `-apple-system, "SF Pro Display", "Montserrat", "DM Sans", system-ui, sans-serif`.
2. **Author Automated Replay Flows (`flows/`)**:
   - `flows/store-01-dashboard.yaml`: Navigates to main cycle calendar and fertile window card.
   - `flows/store-02-daily-log.yaml`: Opens Daily Biomarker & Pain VAS log bottom sheet.
   - `flows/store-03-medical-report.yaml`: Navigates to SpOG A4 Medical PDF Report screen.
   - `flows/store-04-privacy-security.yaml`: Displays Zero-Knowledge cloud sync and hardware encryption status.
   - `flows/store-05-data-portability.yaml`: Shows CSV export and GDPR Danger Zone data wipe.
3. **Execution Commands**:
   - Diagnostics: `goldie doctor` (validates adb, ffmpeg, argent, flows, and release APK).
   - Capture & Compositing: `goldie all` or `goldie capture && goldie frame`.
   - Verification: `goldie verify` (ensures output PNGs strictly match Google Play Store dimensions).

### PHASE 11: WEB LANDING PAGE & GDPR PRIVACY POLICY (ASRIDIGITAL.COM)
Deploy professional marketing landing page and privacy policy to Asri Digital's Cloudflare Pages infrastructure:
1. **Hosting Architecture & Placement on `asridigital.com`**:
   - Infrastructure: Cloudflare Pages project `asridigital` (Zone: `asridigital.com`, Account ID: `6c18d33071f75f4fa94fc308a1fd4bd8`).
   - Production URLs:
     * Dedicated Landing Page: `https://asridigital.com/cyclejournal/`
     * Official Privacy Policy: `https://asridigital.com/cyclejournal/privacy` (Mandatory for Google Play Console).
   - Homepage Showcase: Add a modern featured card on `https://asridigital.com` under the mobile products/AI showcase linking to `/cyclejournal/`.
2. **Landing Page Specifications (`web/cyclejournal/index.html`)**:
   - Visuals: Coral-to-Pink gradient (`linear-gradient(145deg, #FF8A71 0%, #FF5E7D 100%)`), Pure White background, official logo emblem (`logo cyclejournal.jpg`), responsive mobile-first layout.
   - Hero: Headline *"Medical-Grade Menstrual Cycle & Fertility Journal with Absolute Privacy"*, subhead in Indonesian and English, Google Play download button badge, and high-res device mockup.
   - Feature Pillars:
     * Pillar 1: FIGO-compliant SpOG-Ready A4 PDF reports with interactive PDF sample preview.
     * Pillar 2: 100% Offline-First & Zero-Knowledge hardware-backed SQLCipher encryption.
     * Pillar 3: Symptothermal 3-over-6 BBT shift tracking & VAS 0-10 pain journal (zero emojis).
     * Pillar 4: Total Data Sovereignty with raw Excel CSV export and one-tap Nuke data wipe.
   - Footer: Medical disclaimer, copyright © 2026 Asri Digital, links to Privacy Policy and Terms.
3. **Privacy Policy Specifications (`web/cyclejournal/privacy.html`)**:
   - Strict compliance with GDPR Article 9 (Special Category Health Data), Indonesian UU PDP, and Google Play Console Health Policy.
   - Clear disclosures: Zero plaintext cloud storage, client-side encryption via AES-256-GCM, anonymous ID (`px-...`), zero third-party data broker sharing, non-personalized ads only, and user right to permanent erasure.

### PHASE 12: GOOGLE PLAY CONSOLE SUBMISSION GUIDE & CHECKLIST
Step-by-step checklist for publishing CycleJournal on Google Play Console:
1. **App Identity**:
   - App Name: `CycleJournal: Kalender & SpOG` (ID) / `CycleJournal: Private Cycle` (EN).
   - Default Language: English (United States) with Indonesian localization.
   - Category: Health & Fitness / Menstrual & Reproductive Health.
2. **Mandatory App Content Declarations**:
   - Privacy Policy URL: `https://asridigital.com/cyclejournal/privacy`
   - Ads Declaration: Select "Yes, my app contains ads" (Non-personalized AdMob banners on Free Tier).
   - App Access: Select "All functionality is available without special access" (Zero mandatory account creation).
   - Content Rating Questionnaire: Fill IARC rating as Medical Reference / Health & Fitness (typically Rated for 12+ or 18+ for reproductive health).
   - Target Audience & Content: Select 18+ (Reproductive health monitoring).
   - Data Safety Declaration:
     * Data collected: Health Info (Menstrual/Reproductive, Body Temperature, Pain Symptoms).
     * Data usage: App functionality (analytics/marketing: NO).
     * Security practices: Data is encrypted in transit (TLS 1.3), encrypted at rest (AES-256), and user can request deletion.
3. **Store Listing Assets**:
   - High-Res Icon: `store_assets/play_store_icon_512.png` (512x512 PNG 32-bit).
   - Feature Graphic: `store_assets/feature_graphic_1024x500.png` (1024x500 PNG).
   - Phone Screenshots: 5+ composite PNGs generated via `goldie frame` in `out/pixel-10-pro/`.
4. **Release Track Deployment**:
   - Create release in Internal Testing / Production track.
   - Upload signed Android App Bundle: `app/build/outputs/bundle/release/app-release.aab`.
   - Rollout release to 100%.

### PHASE 13: GITHUB REPOSITORY INITIALIZATION, COMMIT & PUSH
Automate full project version control and remote synchronization using GitHub Personal Access Token:
1. **Git Initialization & Hygiene**:
   - Verify `.gitignore` covers:
     * Android build artifacts: `/app/build/`, `.gradle/`, `*.apk`, `*.aab`, `local.properties`, `keystore.properties`.
     * Node & OS temporary files: `node_modules/`, `.DS_Store`, `Thumbs.db`.
     * Temporary export caches: `reports/`.
   - Initialize local repository and set branch to `main`:
     ```bash
     git init
     git branch -M main
     ```
2. **Automated Remote Creation**:
   - Create the remote repository `CycleJournal` on GitHub under account `ahmadasrizalmi` using GitHub REST API:
     ```bash
     curl -X POST -H "Authorization: token <GITHUB_TOKEN>" -H "Accept: application/vnd.github.v3+json" https://api.github.com/user/repos -d '{"name":"CycleJournal","description":"Medical-grade, offline-first menstrual cycle & fertility journal for Android with SpOG-ready PDF export and zero-knowledge encryption","private":false}'
     ```
3. **Commit & Push**:
   - Stage all source code, assets, scripts, web landing page, and documentation:
     ```bash
     git add .
     git commit -m "feat: complete production implementation of CycleJournal (Android Native, FIGO Engine, SQLCipher, Goldie, Web LP)"
     git remote add origin https://<GITHUB_TOKEN>@github.com/ahmadasrizalmi/CycleJournal.git
     git push -u origin main
     ```
---

## 5. ACCEPTANCE CRITERIA & VERIFICATION
1. **Automated Unit Testing Execution**: Run `./gradlew testDebugUnitTest` and achieve 100% passing tests across all 4 test suites (`ClinicalCycleEngineTest`, `CycleAggregatorTest`, `BackupCryptoEngineTest`, `CsvExportHelperTest`).
2. **Bilingual Verification**: App launches in English by default, seamlessly renders Indonesian when system locale is set to `id_ID`, and passes lint check for zero hardcoded UI strings.
3. **Zero Plaintext Invariant**: Verify that inspectable SQLite files on disk fail to open without SQLCipher passphrase and that Keystore keys are hardware-backed.
4. **Cycle State Machine Correctness**: Reconciling historical logs correctly handles bleeding gaps > 20 days and treats mid-cycle spotting (days 11..19) as non-cycle-splitting intermenstrual bleeding.
5. **PDF Fidelity**: Rendered SpOG PDF matches exact A4 dimensions, displays patient ID, summary FIGO grid, anomaly red flags, cycle table, and doctor sign-off block without visual clipping.
6. **Zero-Knowledge Cloud Roundtrip**: Data exported to JSON, encrypted with AES-256-GCM, uploaded to D1, downloaded, decrypted with PIN, and restored matches original database state bit-for-bit.
7. **Nuke Option Totality**: Executing data wipe leaves zero records in D1, zero database files, zero Keystore keys, and resets the app to initial onboarding.
8. **Play Store Marketing Asset Generation**: `goldie doctor` passes with zero flow errors, and `goldie all` generates bilingual composite screenshots in `out/` adhering to Play Store specifications on the Coral-to-Pink gradient background.
9. **Brand Asset Integrity**: Launcher icons in all density mipmap buckets (`mdpi` through `xxxhdpi`, regular and round), Google Play 512x512 icon, 1024x500 Feature Graphic, and PDF report emblem are verified present and generated from `logo cyclejournal.jpg`.
10. **Dual Release Artifacts Generated**: Both `app/build/outputs/apk/release/app-release.apk` and `app/build/outputs/bundle/release/app-release.aab` are compiled and verified signed.
11. **Web Landing Page & Privacy Policy**: Responsive landing page and GDPR/Play-compliant Privacy Policy are deployed at `https://asridigital.com/cyclejournal/` and `https://asridigital.com/cyclejournal/privacy`, with CycleJournal featured on the `asridigital.com` homepage.
12. **Play Console Ready**: Complete store submission documentation, Data Safety answers, and visual assets are verified ready for zero-friction Play Store approval.
13. **GitHub Remote Synchronization**: All source code, configs, automated flows, test suites, and web assets are committed and pushed to `https://github.com/ahmadasrizalmi/CycleJournal`.
