package com.app.cyclejournal.domain.engine

import com.app.cyclejournal.data.local.entity.AnomalyType
import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.domain.model.AnomalyAlert
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Functional pain severity categories for Visual Analog Scale (VAS 0–10).
 */
enum class VasCategory(val label: String, val isRedFlag: Boolean) {
    NONE("Tidak Ada Nyeri (Normal)", false),
    MILD("Nyeri Ringan (Aktivitas Normal)", false),
    MODERATE("Nyeri Sedang (Aktivitas Terganggu, Butuh Istirahat)", false),
    SEVERE("Nyeri Berat (Indikasi Dismenore / Red Flag)", true),
    EXTREME("Nyeri Ekstrem (Disarankan Evaluasi Medis Segera)", true)
}

data class VasImpact(
    val score: Int,
    val category: VasCategory,
    val description: String,
    val isRedFlag: Boolean
)

/**
 * Clinical pain classifier and FIGO-compliant anomaly detection engine.
 */
class AnomalyDetector {

    companion object {
        const val MAX_NORMAL_CYCLE_DAYS = 38
        const val MIN_NORMAL_CYCLE_DAYS = 24
        const val MAX_NORMAL_PERIOD_DAYS = 8
        const val FIGO_IRREGULARITY_THRESHOLD_DAYS = 8
    }

    /**
     * Maps numeric VAS score (0–10) to clinical functional impact category.
     */
    fun mapVasImpact(score: Int): VasImpact {
        val clampedScore = score.coerceIn(0, 10)
        return when (clampedScore) {
            0 -> VasImpact(0, VasCategory.NONE, "0: Tidak ada rasa nyeri sama sekali.", false)
            in 1..3 -> VasImpact(clampedScore, VasCategory.MILD, "1–3: Nyeri ringan, dapat diabaikan saat beraktivitas.", false)
            in 4..6 -> VasImpact(clampedScore, VasCategory.MODERATE, "4–6: Nyeri sedang, mengganggu fokus & butuh pereda nyeri.", false)
            in 7..8 -> VasImpact(clampedScore, VasCategory.SEVERE, "7–8: Nyeri hebat, membatasi gerak atau butuh tirah baring.", true)
            else -> VasImpact(clampedScore, VasCategory.EXTREME, "9–10: Nyeri tak tertahankan, disarankan rujukan medis ginekolog.", true)
        }
    }

    /**
     * Detects clinical FIGO anomalies across current and historical cycles and recent daily logs.
     */
    fun detectAnomalies(
        currentCycle: CycleEntity?,
        historicalCycles: List<CycleEntity>,
        recentLogs: List<DailyLogEntity>
    ): List<AnomalyAlert> {
        val alerts = mutableListOf<AnomalyAlert>()

        // 1. Oligomenorrhea (> 38 days) & Polymenorrhea (< 24 days)
        historicalCycles.filter { it.cycleLengthDays != null }.forEach { cycle ->
            val len = cycle.cycleLengthDays!!
            if (len > MAX_NORMAL_CYCLE_DAYS) {
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.OLIGOMENORRHEA,
                        cycle.startDate,
                        "Siklus berlangsung selama $len hari (> 38 hari, potensi anovulasi/PCOS)."
                    )
                )
            } else if (len < MIN_NORMAL_CYCLE_DAYS) {
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.POLYMENORRHEA,
                        cycle.startDate,
                        "Siklus berlangsung selama $len hari (< 24 hari)."
                    )
                )
            }
        }

        // 2. FIGO Irregularity: Delta >= 8 days across >= 3 historical cycles
        val completedLengths = historicalCycles.mapNotNull { it.cycleLengthDays }
        if (completedLengths.size >= 3) {
            val delta = (completedLengths.maxOrNull() ?: 0) - (completedLengths.minOrNull() ?: 0)
            if (delta >= FIGO_IRREGULARITY_THRESHOLD_DAYS) {
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.CYCLE_IRREGULARITY,
                        LocalDate.now(),
                        "Variasi panjang siklus mencapai $delta hari (ambang batas FIGO >= 8 hari)."
                    )
                )
            }
        }

        // 3. Prolonged Bleeding (> 8 consecutive days of active bleeding)
        var streak = 0
        recentLogs.sortedBy { it.date }.forEach { log ->
            if (log.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)) {
                streak++
                if (streak > MAX_NORMAL_PERIOD_DAYS) {
                    alerts.add(
                        AnomalyAlert(
                            AnomalyType.PROLONGED_BLEEDING,
                            log.date,
                            "Perdarahan haid aktif berlangsung $streak hari berturut-turut pada ${log.date}."
                        )
                    )
                }
            } else {
                streak = 0
            }
        }

        // 4. Intermenstrual Bleeding (Spotting after Day 8, excluding ovulatory egg-white spotting)
        if (currentCycle != null) {
            recentLogs.forEach { log ->
                val dayOfCycle = ChronoUnit.DAYS.between(currentCycle.startDate, log.date) + 1
                if (dayOfCycle > 8 && log.flow == FlowIntensity.SPOTTING) {
                    val isOvulationSpotting = log.cervicalMucus == CervicalMucusType.EGG_WHITE
                    if (!isOvulationSpotting) {
                        alerts.add(
                            AnomalyAlert(
                                AnomalyType.INTERMENSTRUAL_BLEEDING,
                                log.date,
                                "Pendarahan bercak di luar jadwal haid pada hari ke-$dayOfCycle siklus."
                            )
                        )
                    }
                }
            }
        }

        // 5. Severe Dysmenorrhea (VAS >= 7 or VAS >= 5 with analgesic dependence)
        recentLogs.forEach { log ->
            val impact = mapVasImpact(log.painVasScore)
            if (impact.isRedFlag || (log.painVasScore >= 5 && log.takenAnalgesic)) {
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.SEVERE_DYSMENORRHEA,
                        log.date,
                        "Skor nyeri VAS ${log.painVasScore}/10 (${impact.category.label}) pada area: ${log.painLocation ?: "pelvis"}."
                    )
                )
            }
        }

        return alerts
    }
}
