package com.app.cyclejournal.domain.engine

import com.app.cyclejournal.data.local.entity.AnomalyType
import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.domain.model.AnomalyAlert
import com.app.cyclejournal.domain.model.CycleStats
import com.app.cyclejournal.domain.model.FertilePrediction
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * Pure clinical mathematics and anomaly screening engine adhering strictly to FIGO guidelines.
 * Completely side-effect free and decoupled from Android framework dependencies for fast JVM unit testing.
 */
class ClinicalCycleEngine {

    companion object {
        const val DEFAULT_LUTEAL_PHASE_DAYS = 14L
        const val MIN_NORMAL_CYCLE_DAYS = 24
        const val MAX_NORMAL_CYCLE_DAYS = 38
        const val MAX_NORMAL_PERIOD_DAYS = 8
        const val IRREGULARITY_THRESHOLD_DAYS = 8
        const val SHORT_LUTEAL_PHASE_THRESHOLD_DAYS = 10L
        const val BBT_SHIFT_CELSIUS = 0.20
    }

    /**
     * 1. Calculate FIGO Cycle Statistics: Mean, Sample Variance, Sample Standard Deviation (sigma), and Min/Max.
     */
    fun calculateCycleStats(completedCycles: List<CycleEntity>): CycleStats? {
        val validLengths = completedCycles.mapNotNull { it.cycleLengthDays }.filter { it > 0 }
        if (validLengths.isEmpty()) return null

        val avgLength = validLengths.average()
        val minLen = validLengths.minOrNull() ?: 0
        val maxLen = validLengths.maxOrNull() ?: 0

        // Calculate Bessel-corrected Sample Standard Deviation (n - 1)
        val variance = if (validLengths.size > 1) {
            validLengths.sumOf { (it - avgLength).pow(2) } / (validLengths.size - 1)
        } else {
            0.0
        }
        val stdDev = sqrt(variance)

        val avgPeriod = completedCycles.map { it.periodDurationDays }
            .filter { it > 0 }
            .takeIf { it.isNotEmpty() }
            ?.average() ?: 0.0

        return CycleStats(
            averageLength = avgLength,
            standardDeviation = stdDev,
            minLength = minLen,
            maxLength = maxLen,
            averagePeriodDuration = avgPeriod
        )
    }

    /**
     * 2. Predict Fertile Window & Ovulation Date using Standard Calendar Method (Tier 1).
     * Assumes a constant luteal phase duration of 14 days (FIGO / ACOG clinical standard).
     */
    fun predictFertileWindow(
        lastPeriodStartDate: LocalDate,
        averageCycleLength: Double = 28.0
    ): FertilePrediction {
        val cycleLength = averageCycleLength.roundToLong()
        val nextPeriod = lastPeriodStartDate.plusDays(cycleLength)
        val ovulation = nextPeriod.minusDays(DEFAULT_LUTEAL_PHASE_DAYS)

        return FertilePrediction(
            predictedNextPeriodDate = nextPeriod,
            predictedOvulationDate = ovulation,
            fertileWindowStart = ovulation.minusDays(5),
            fertileWindowEnd = ovulation.plusDays(1)
        )
    }

    /**
     * 3. Confirm Symptothermal Ovulation via 3-over-6 BBT Shift Rule (Tier 2).
     *
     * Rule:
     * - Scans chronologically ordered logs with basal body temperature recordings.
     * - Identifies 6 consecutive baseline days where highest temp is T_base_max.
     * - Ovulation is confirmed if the subsequent 3 consecutive days all reach >= T_base_max + 0.20°C.
     * - Ovulation date is assigned to the last low baseline day before the shift (Day 6).
     */
    fun detectSymptothermalOvulation(logsSortedByDate: List<DailyLogEntity>): LocalDate? {
        val bbtLogs = logsSortedByDate.filter { it.basalBodyTempCelsius != null }
        if (bbtLogs.size < 9) return null // Requires minimum 6 baseline + 3 shifted days

        for (i in 6 until bbtLogs.size - 2) {
            val baselineTemps = bbtLogs.subList(i - 6, i).mapNotNull { it.basalBodyTempCelsius }
            if (baselineTemps.size < 6) continue

            val highestBaseline = baselineTemps.maxOrNull() ?: continue
            val shiftThreshold = highestBaseline + BBT_SHIFT_CELSIUS

            val day1 = bbtLogs[i].basalBodyTempCelsius ?: 0.0
            val day2 = bbtLogs[i + 1].basalBodyTempCelsius ?: 0.0
            val day3 = bbtLogs[i + 2].basalBodyTempCelsius ?: 0.0

            if (day1 >= shiftThreshold && day2 >= shiftThreshold && day3 >= shiftThreshold) {
                // Ovulation day pegged to last low baseline day before shift
                return bbtLogs[i - 1].date
            }
        }
        return null
    }

    /**
     * 4. Comprehensive FIGO Anomaly Evaluation & Clinical Red Flag Screening.
     */
    fun evaluateAnomalies(
        currentCycle: CycleEntity?,
        historicalCycles: List<CycleEntity>,
        recentDailyLogs: List<DailyLogEntity>
    ): List<AnomalyAlert> {
        val alerts = mutableListOf<AnomalyAlert>()

        // A. Oligomenorrhea & Polymenorrhea (Length Abnormalities in recent cycles)
        historicalCycles.take(6).filter { it.cycleLengthDays != null }.forEach { cycle ->
            val length = cycle.cycleLengthDays!!
            if (length > MAX_NORMAL_CYCLE_DAYS) {
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.OLIGOMENORRHEA,
                        cycle.startDate,
                        "Siklus berlangsung selama $length hari (> 38 hari)."
                    )
                )
            } else if (length < MIN_NORMAL_CYCLE_DAYS) {
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.POLYMENORRHEA,
                        cycle.startDate,
                        "Siklus berlangsung selama $length hari (< 24 hari)."
                    )
                )
            }
        }

        // B. Cycle Irregularity (FIGO Standard: delta >= 8 days across >= 3 cycles in 6 months)
        val validHistLengths = historicalCycles.take(6).mapNotNull { it.cycleLengthDays }
        if (validHistLengths.size >= 3) {
            val delta = (validHistLengths.maxOrNull() ?: 0) - (validHistLengths.minOrNull() ?: 0)
            if (delta >= IRREGULARITY_THRESHOLD_DAYS) {
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.CYCLE_IRREGULARITY,
                        LocalDate.now(),
                        "Selisih siklus terpanjang dan terpendek adalah $delta hari (ambang batas >= 8 hari)."
                    )
                )
            }
        }

        // C. Prolonged Bleeding (> 8 consecutive days of active bleeding)
        // Groups consecutive bleeding streaks and emits one alert per episode
        val sortedLogs = recentDailyLogs.sortedBy { it.date }
        var streakCount = 0
        var streakStart: LocalDate? = null
        var lastBleedDate: LocalDate? = null

        for (i in sortedLogs.indices) {
            val log = sortedLogs[i]
            val isBleed = log.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)

            if (isBleed) {
                if (streakCount == 0) streakStart = log.date
                streakCount++
                lastBleedDate = log.date
            }

            val nextContinues = if (isBleed && i + 1 < sortedLogs.size) {
                val nextLog = sortedLogs[i + 1]
                val isNextDay = ChronoUnit.DAYS.between(log.date, nextLog.date) == 1L
                val nextBleeds = nextLog.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)
                isNextDay && nextBleeds
            } else false

            if (isBleed && !nextContinues) {
                if (streakCount > MAX_NORMAL_PERIOD_DAYS && streakStart != null && lastBleedDate != null) {
                    alerts.add(
                        AnomalyAlert(
                            AnomalyType.PROLONGED_BLEEDING,
                            lastBleedDate,
                            "Perdarahan aktif berlangsung $streakCount hari berturut-turut ($streakStart s/d $lastBleedDate)."
                        )
                    )
                }
                streakCount = 0
                streakStart = null
                lastBleedDate = null
            } else if (!isBleed) {
                streakCount = 0
                streakStart = null
                lastBleedDate = null
            }
        }

        // D. Intermenstrual Bleeding (Spotting after Day 8 within current cycle, excluding ovulatory spotting)
        if (currentCycle != null) {
            val currentCycleLogs = recentDailyLogs.filter {
                !it.date.isBefore(currentCycle.startDate) &&
                        (currentCycle.endDate == null || !it.date.isAfter(currentCycle.endDate))
            }
            val imbDates = mutableListOf<LocalDate>()
            currentCycleLogs.forEach { log ->
                val dayOfCycle = ChronoUnit.DAYS.between(currentCycle.startDate, log.date) + 1
                if (dayOfCycle > 8 && log.flow == FlowIntensity.SPOTTING) {
                    val isOvulationSpotting = log.cervicalMucus == CervicalMucusType.EGG_WHITE
                    if (!isOvulationSpotting) {
                        imbDates.add(log.date)
                    }
                }
            }
            if (imbDates.isNotEmpty()) {
                val latestImb = imbDates.last()
                val count = imbDates.size
                val dayOfCycle = ChronoUnit.DAYS.between(currentCycle.startDate, latestImb) + 1
                val detailStr = if (count == 1) {
                    "Pendarahan bercak (spotting) terdeteksi pada hari ke-$dayOfCycle siklus ($latestImb)."
                } else {
                    "Pendarahan bercak (spotting) terdeteksi $count kali dalam siklus ini (terakhir hari ke-$dayOfCycle, $latestImb)."
                }
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.INTERMENSTRUAL_BLEEDING,
                        latestImb,
                        detailStr
                    )
                )
            }
        }

        // E. Short Luteal Phase (< 10 days between confirmed ovulation and next period)
        if (currentCycle != null && currentCycle.confirmedOvulationDate != null && currentCycle.endDate != null) {
            val lutealDays = ChronoUnit.DAYS.between(
                currentCycle.confirmedOvulationDate,
                currentCycle.endDate.plusDays(1)
            )
            if (lutealDays < SHORT_LUTEAL_PHASE_THRESHOLD_DAYS) {
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.SHORT_LUTEAL_PHASE,
                        currentCycle.startDate,
                        "Fase luteal hanya berlangsung $lutealDays hari (< 10 hari)."
                    )
                )
            }
        }

        // F. Severe Dysmenorrhea (VAS >= 7, or VAS >= 5 with analgesic medication)
        // Group pain logs by clusters (within 3 days) to avoid repeated duplicate alerts
        val severePainLogs = recentDailyLogs.filter {
            it.painVasScore >= 7 || (it.painVasScore >= 5 && it.takenAnalgesic)
        }.sortedByDescending { it.date }

        val clusteredAlertDates = mutableSetOf<LocalDate>()
        for (log in severePainLogs) {
            val isAlreadyCovered = clusteredAlertDates.any {
                kotlin.math.abs(ChronoUnit.DAYS.between(log.date, it)) <= 3
            }
            if (!isAlreadyCovered) {
                clusteredAlertDates.add(log.date)
                val locationStr = if (!log.painLocation.isNullOrBlank()) " pada area ${log.painLocation}" else ""
                alerts.add(
                    AnomalyAlert(
                        AnomalyType.SEVERE_DYSMENORRHEA,
                        log.date,
                        "Skor nyeri skala VAS ${log.painVasScore}/10 terdeteksi$locationStr pada tanggal ${log.date}."
                    )
                )
            }
        }

        return alerts
    }
}
