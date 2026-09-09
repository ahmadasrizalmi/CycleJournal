package com.app.cyclejournal.domain.engine

import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Result of the 3-over-6 symptothermal basal body temperature (BBT) evaluation.
 */
data class BbtEvaluationResult(
    val isConfirmed: Boolean,
    val ovulationDate: LocalDate?,
    val coverlineCelsius: Double?,
    val shiftDegrees: Double?,
    val baselineTemps: List<Double>
)

/**
 * 4 distinct clinical hormonal phases of the female menstrual cycle.
 */
enum class HormonalPhase(val displayName: String, val discreetName: String) {
    MENSTRUAL("Fase Menstruasi", "Fase 01"),
    FOLLICULAR("Fase Folikuler", "Fase 02"),
    FERTILE_OVULATION("Jendela Subur & Ovulasi", "Fase Tengah"),
    LUTEAL("Fase Luteal", "Fase 03")
}

/**
 * Clinical engine implementing the 3-over-6 rule for BBT biphasic shift confirmation
 * and dynamic hormonal phase determination.
 */
class BbtEngine {

    companion object {
        const val BBT_SHIFT_THRESHOLD_CELSIUS = 0.20
        const val REQUIRED_BASELINE_DAYS = 6
        const val REQUIRED_SHIFT_DAYS = 3
    }

    /**
     * Evaluates daily logs for a valid symptothermal 3-over-6 biphasic temperature shift.
     *
     * Algorithm:
     * 1. Filters records with non-null basal body temperatures sorted chronologically.
     * 2. Scans for 6 consecutive baseline days.
     * 3. Calculates the coverline as the highest of the 6 baseline temperatures.
     * 4. Confirms ovulation if the subsequent 3 consecutive days all remain at least 0.20°C above the coverline.
     * 5. Returns confirmed ovulation date pegged to Day 6 (the last low day prior to the shift).
     */
    fun evaluateBbtShift(logsSortedByDate: List<DailyLogEntity>): BbtEvaluationResult {
        val bbtLogs = logsSortedByDate.filter { it.basalBodyTempCelsius != null }
        if (bbtLogs.size < REQUIRED_BASELINE_DAYS + REQUIRED_SHIFT_DAYS) {
            return BbtEvaluationResult(
                isConfirmed = false,
                ovulationDate = null,
                coverlineCelsius = null,
                shiftDegrees = null,
                baselineTemps = emptyList()
            )
        }

        for (i in REQUIRED_BASELINE_DAYS until bbtLogs.size - (REQUIRED_SHIFT_DAYS - 1)) {
            val baselineSlice = bbtLogs.subList(i - REQUIRED_BASELINE_DAYS, i)
            val baselineTemps = baselineSlice.mapNotNull { it.basalBodyTempCelsius }
            if (baselineTemps.size < REQUIRED_BASELINE_DAYS) continue

            val coverline = baselineTemps.maxOrNull() ?: continue
            val shiftThreshold = coverline + BBT_SHIFT_THRESHOLD_CELSIUS

            val day1 = bbtLogs[i].basalBodyTempCelsius ?: 0.0
            val day2 = bbtLogs[i + 1].basalBodyTempCelsius ?: 0.0
            val day3 = bbtLogs[i + 2].basalBodyTempCelsius ?: 0.0

            if (day1 >= shiftThreshold && day2 >= shiftThreshold && day3 >= shiftThreshold) {
                val averageShift = ((day1 + day2 + day3) / 3.0) - coverline
                return BbtEvaluationResult(
                    isConfirmed = true,
                    ovulationDate = bbtLogs[i - 1].date,
                    coverlineCelsius = coverline,
                    shiftDegrees = averageShift,
                    baselineTemps = baselineTemps
                )
            }
        }

        return BbtEvaluationResult(
            isConfirmed = false,
            ovulationDate = null,
            coverlineCelsius = null,
            shiftDegrees = null,
            baselineTemps = emptyList()
        )
    }

    /**
     * Determines current hormonal phase based on active cycle, current date, and predicted/confirmed ovulation.
     */
    fun determineHormonalPhase(
        currentCycle: CycleEntity?,
        today: LocalDate = LocalDate.now(),
        predictedOvulationDate: LocalDate? = null,
        confirmedOvulationDate: LocalDate? = null
    ): HormonalPhase {
        if (currentCycle == null) return HormonalPhase.FOLLICULAR

        val cycleDay = ChronoUnit.DAYS.between(currentCycle.startDate, today) + 1

        // 1. Menstrual phase: Day 1 until period duration ends (default 5 days)
        val periodDuration = maxOf(1, currentCycle.periodDurationDays)
        if (cycleDay <= periodDuration) {
            return HormonalPhase.MENSTRUAL
        }

        val effectiveOvulation = confirmedOvulationDate ?: predictedOvulationDate
        if (effectiveOvulation != null) {
            val fertileStart = effectiveOvulation.minusDays(5)
            val fertileEnd = effectiveOvulation.plusDays(1)

            // 2. Fertile Window & Ovulation Peak: [Ovulation - 5d, Ovulation + 1d]
            if (!today.isBefore(fertileStart) && !today.isAfter(fertileEnd)) {
                return HormonalPhase.FERTILE_OVULATION
            }

            // 3. Luteal Phase: after ovulation window until next menses
            if (today.isAfter(fertileEnd)) {
                return HormonalPhase.LUTEAL
            }
        }

        // 4. Follicular Phase: between menses end and fertile window
        return HormonalPhase.FOLLICULAR
    }
}
