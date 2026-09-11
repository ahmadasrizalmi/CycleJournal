package com.app.cyclejournal.domain.engine

import com.app.cyclejournal.data.local.dao.CycleDao
import com.app.cyclejournal.data.local.dao.DailyLogDao
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * State machine managing menstrual cycle lifecycle transitions from daily symptom and bleeding logs.
 *
 * Core Clinical Rules:
 * 1. True Bleeding: Only LIGHT, MEDIUM, or HEAVY flow denotes active menstruation.
 * 2. SPOTTING does NOT initiate a new cycle or close an existing cycle.
 * 3. Minimum Separation Threshold (MIN_DAYS_FOR_NEW_CYCLE = 20): Bleeding occurring before Day 20
 *    is treated as an intermenstrual bleeding episode (AUB-IMB) and does not falsely create new cycles.
 * 4. Retroactive History Reconciliation: Fully deterministic replay of all chronological logs
 *    used after remote cloud backup restore or past log edits.
 */
class CycleAggregator(
    private val dailyLogDao: DailyLogDao,
    private val cycleDao: CycleDao,
    private val bbtEngine: BbtEngine = BbtEngine()
) {

    companion object {
        const val MIN_DAYS_FOR_NEW_CYCLE = 20L
        private const val BLEEDING_SCAN_WINDOW_DAYS = 12L
    }

    /**
     * Primary entry point when a user saves a daily log entry.
     * Upserts daily log into database and deterministically reconciles all cycle records
     * so that out-of-order date entries, edits, and retroactive cycles are immediately calculated.
     */
    suspend fun onDailyLogSaved(log: DailyLogEntity) {
        // 1. Upsert daily log into database
        dailyLogDao.upsertDailyLog(log)

        // 2. Deterministically reconcile all cycles from complete chronological log history
        reconcileAllHistory()
    }

    /**
     * Full deterministic reconstruction of all cycle records from ascending daily logs.
     * Invoked after saving daily logs, disaster recovery restore, or retroactive date adjustments.
     */
    suspend fun reconcileAllHistory() {
        val allLogs = dailyLogDao.getAllLogsAsc()
        if (allLogs.isEmpty()) {
            cycleDao.clearAllCycles()
            return
        }

        val reconstructedCycles = mutableListOf<CycleEntity>()
        var currentCycleStart: LocalDate? = null
        var lastBleedingDate: LocalDate? = null

        for (log in allLogs) {
            val isBleeding = log.flow in listOf(
                FlowIntensity.LIGHT,
                FlowIntensity.MEDIUM,
                FlowIntensity.HEAVY
            )

            if (isBleeding) {
                if (currentCycleStart == null) {
                    currentCycleStart = log.date
                    lastBleedingDate = log.date
                } else {
                    val daysSinceCycleStart = ChronoUnit.DAYS.between(currentCycleStart, log.date)
                    val daysSinceLastBleed = ChronoUnit.DAYS.between(lastBleedingDate!!, log.date)

                    // If bleeding occurs >= 20 days from start AND there was a bleeding break > 3 days
                    if (daysSinceCycleStart >= MIN_DAYS_FOR_NEW_CYCLE && daysSinceLastBleed > 3) {
                        // Close ongoing cycle
                        val previousCycle = CycleEntity(
                            startDate = currentCycleStart,
                            endDate = log.date.minusDays(1),
                            cycleLengthDays = daysSinceCycleStart.toInt(),
                            periodDurationDays = calculateBleedingDuration(currentCycleStart, allLogs),
                            confirmedOvulationDate = findConfirmedOvulation(currentCycleStart, log.date.minusDays(1), allLogs)
                        )
                        reconstructedCycles.add(previousCycle)

                        // Start new cycle
                        currentCycleStart = log.date
                    }
                    lastBleedingDate = log.date
                }
            }
        }

        // Save ongoing cycle (endDate = null, cycleLengthDays = null)
        if (currentCycleStart != null) {
            val ongoingCycle = CycleEntity(
                startDate = currentCycleStart,
                endDate = null,
                cycleLengthDays = null,
                periodDurationDays = calculateBleedingDuration(currentCycleStart, allLogs),
                confirmedOvulationDate = findConfirmedOvulation(currentCycleStart, null, allLogs)
            )
            reconstructedCycles.add(ongoingCycle)
        }

        // Atomically replace cycles in database within a single transaction
        cycleDao.replaceAllCycles(reconstructedCycles)
    }

    private fun findConfirmedOvulation(
        cycleStart: LocalDate,
        cycleEnd: LocalDate?,
        allLogs: List<DailyLogEntity>
    ): LocalDate? {
        val cycleLogs = allLogs.filter {
            !it.date.isBefore(cycleStart) && (cycleEnd == null || !it.date.isAfter(cycleEnd))
        }
        val bbtResult = bbtEngine.evaluateBbtShift(cycleLogs)
        return if (bbtResult.isConfirmed) bbtResult.ovulationDate else null
    }

    private fun calculateBleedingDuration(cycleStart: LocalDate, logs: List<DailyLogEntity>): Int {
        val window = logs.filter {
            !it.date.isBefore(cycleStart) && it.date.isBefore(cycleStart.plusDays(BLEEDING_SCAN_WINDOW_DAYS))
        }
        val count = window.count {
            it.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)
        }
        return maxOf(1, count)
    }
}
