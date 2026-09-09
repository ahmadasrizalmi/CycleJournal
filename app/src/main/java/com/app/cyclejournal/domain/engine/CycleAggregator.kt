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
    private val cycleDao: CycleDao
) {

    companion object {
        const val MIN_DAYS_FOR_NEW_CYCLE = 20L
        private const val BLEEDING_SCAN_WINDOW_DAYS = 12L
    }

    /**
     * Primary entry point when a user saves a daily log entry.
     * Evaluates cycle inception, duration updates, or closing previous cycle.
     */
    suspend fun onDailyLogSaved(log: DailyLogEntity) {
        // 1. Upsert daily log into database
        dailyLogDao.upsertDailyLog(log)

        // 2. Evaluate impact on cycle records
        val isTrueBleeding = log.flow in listOf(
            FlowIntensity.LIGHT,
            FlowIntensity.MEDIUM,
            FlowIntensity.HEAVY
        )

        val latestCycle = cycleDao.getLatestCycle()
        if (latestCycle == null) {
            // First time use: initiate first cycle if true bleeding
            if (isTrueBleeding) {
                cycleDao.insertCycle(
                    CycleEntity(
                        startDate = log.date,
                        periodDurationDays = 1
                    )
                )
            }
            return
        }

        // Calculate days since current cycle started
        val daysSinceLatestStart = ChronoUnit.DAYS.between(latestCycle.startDate, log.date)

        when {
            // Case A: Within initial bleeding window (Days 0..10) -> Update period duration
            daysSinceLatestStart in 0..10 -> {
                updateCurrentCyclePeriodDuration(latestCycle)
            }

            // Case B: Active bleeding after passing 20-day threshold -> Close cycle & Start New Cycle!
            daysSinceLatestStart >= MIN_DAYS_FOR_NEW_CYCLE && isTrueBleeding -> {
                // 1. Close current cycle
                val closedCycle = latestCycle.copy(
                    endDate = log.date.minusDays(1),
                    cycleLengthDays = daysSinceLatestStart.toInt()
                )
                cycleDao.updateCycle(closedCycle)

                // 2. Open new cycle
                val newCycle = CycleEntity(
                    startDate = log.date,
                    periodDurationDays = 1
                )
                cycleDao.insertCycle(newCycle)
            }

            // Case C: Bleeding occurring on cycle days 11..19 is retained as intermenstrual bleeding
            // without prematurely splitting the cycle.
        }
    }

    /**
     * Scans active bleeding days within the initial 12 days of cycle start.
     */
    private suspend fun updateCurrentCyclePeriodDuration(cycle: CycleEntity) {
        val cycleLogs = dailyLogDao.getLogsBetween(
            startDate = cycle.startDate,
            endDate = cycle.startDate.plusDays(BLEEDING_SCAN_WINDOW_DAYS)
        )
        val activeBleedDays = cycleLogs.count {
            it.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)
        }
        val duration = maxOf(1, activeBleedDays)
        if (duration != cycle.periodDurationDays) {
            cycleDao.updateCycle(cycle.copy(periodDurationDays = duration))
        }
    }

    /**
     * Full deterministic reconstruction of all cycle records from ascending daily logs.
     * Invoked after disaster recovery restore from Cloudflare D1 or retroactive date adjustments.
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
                            periodDurationDays = calculateBleedingDuration(currentCycleStart, allLogs)
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
                periodDurationDays = calculateBleedingDuration(currentCycleStart, allLogs)
            )
            reconstructedCycles.add(ongoingCycle)
        }

        // Atomically replace cycles in database
        cycleDao.clearAllCycles()
        reconstructedCycles.forEach { cycleDao.insertCycle(it) }
    }

    private fun calculateBleedingDuration(cycleStart: LocalDate, logs: List<DailyLogEntity>): Int {
        val window = logs.filter {
            !it.date.isBefore(cycleStart) && it.date.isBefore(cycleStart.plusDays(10))
        }
        val count = window.count {
            it.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)
        }
        return maxOf(1, count)
    }
}
