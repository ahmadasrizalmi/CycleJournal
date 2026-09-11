package com.app.cyclejournal.domain.engine

import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.AnomalyType
import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.domain.model.AnomalyAlert
import com.app.cyclejournal.domain.model.ResArg
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Functional pain severity categories for Visual Analog Scale (VAS 0–10).
 */
enum class VasCategory(val labelRes: Int, val isRedFlag: Boolean) {
    NONE(R.string.vas_category_none, false),
    MILD(R.string.vas_category_mild, false),
    MODERATE(R.string.vas_category_moderate, false),
    SEVERE(R.string.vas_category_severe, true),
    EXTREME(R.string.vas_category_extreme, true)
}

data class VasImpact(
    val score: Int,
    val category: VasCategory,
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
            0 -> VasImpact(0, VasCategory.NONE, false)
            in 1..3 -> VasImpact(clampedScore, VasCategory.MILD, false)
            in 4..6 -> VasImpact(clampedScore, VasCategory.MODERATE, false)
            in 7..8 -> VasImpact(clampedScore, VasCategory.SEVERE, true)
            else -> VasImpact(clampedScore, VasCategory.EXTREME, true)
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
                        R.string.anomaly_detail_oligomenorrhea_pcos,
                        listOf(len)
                    )
                )
            } else if (len < MIN_NORMAL_CYCLE_DAYS) {
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.POLYMENORRHEA,
                        cycle.startDate,
                        R.string.anomaly_detail_polymenorrhea,
                        listOf(len)
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
                        R.string.anomaly_detail_irregularity_figo,
                        listOf(delta)
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
                            R.string.anomaly_detail_prolonged_bleeding_single,
                            listOf(streak, log.date.toString())
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
                                R.string.anomaly_detail_imb_day,
                                listOf(dayOfCycle)
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
                        R.string.anomaly_detail_vas_category_area,
                        listOf(log.painVasScore, ResArg(impact.category.labelRes), log.painLocation ?: "pelvis")
                    )
                )
            }
        }

        return alerts
    }
}
