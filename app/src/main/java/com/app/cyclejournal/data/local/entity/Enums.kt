package com.app.cyclejournal.data.local.entity

/**
 * Intensity of menstrual bleeding.
 * Only LIGHT, MEDIUM, and HEAVY are classified as true menstruation by CycleAggregator.
 * SPOTTING represents mid-cycle spotting or light breakthrough bleeding.
 */
enum class FlowIntensity(val level: Int) {
    NONE(0),
    SPOTTING(1),
    LIGHT(2),
    MEDIUM(3),
    HEAVY(4)
}

/**
 * Biomarker classification for cervical mucus consistency in symptothermal fertility tracking.
 */
enum class CervicalMucusType {
    NONE,
    DRY,
    STICKY,
    CREAMY,
    WATERY,
    EGG_WHITE // Highest fertility biomarker; confirms ovulatory window within +-24 hours
}

/**
 * FIGO-compliant clinical anomaly red flags.
 */
enum class AnomalyType(val code: String, val description: String) {
    OLIGOMENORRHEA("ANO_01", "Siklus memanjang (> 38 hari)"),
    POLYMENORRHEA("ANO_02", "Siklus terlalu pendek (< 24 hari)"),
    CYCLE_IRREGULARITY("ANO_03", "Variabilitas siklus signifikan (rentang >= 8 hari)"),
    PROLONGED_BLEEDING("ANO_04", "Durasi perdarahan haid melebihi 8 hari berturut-turut"),
    INTERMENSTRUAL_BLEEDING("ANO_05", "Pendarahan / spotting di luar jadwal haid"),
    SHORT_LUTEAL_PHASE("ANO_06", "Fase luteal pendek (< 10 hari)"),
    SEVERE_DYSMENORRHEA("ANO_07", "Nyeri haid intensitas berat (Skor VAS >= 7)")
}
