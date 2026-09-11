package com.app.cyclejournal.data.local.entity

import androidx.annotation.StringRes
import com.app.cyclejournal.R

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
enum class AnomalyType(val code: String, @StringRes val descriptionRes: Int) {
    OLIGOMENORRHEA("ANO_01", R.string.anomaly_oligomenorrhea),
    POLYMENORRHEA("ANO_02", R.string.anomaly_polymenorrhea),
    CYCLE_IRREGULARITY("ANO_03", R.string.anomaly_cycle_irregularity),
    PROLONGED_BLEEDING("ANO_04", R.string.anomaly_prolonged_bleeding),
    INTERMENSTRUAL_BLEEDING("ANO_05", R.string.anomaly_intermenstrual_bleeding),
    SHORT_LUTEAL_PHASE("ANO_06", R.string.anomaly_short_luteal_phase),
    SEVERE_DYSMENORRHEA("ANO_07", R.string.anomaly_severe_dysmenorrhea)
}
