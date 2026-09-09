package com.app.cyclejournal.domain.engine

import com.app.cyclejournal.data.local.dao.CycleDao
import com.app.cyclejournal.data.local.dao.DailyLogDao
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CycleAggregatorTest {

    // In-memory fake DAOs for isolated unit testing
    private val fakeDailyLogs = mutableListOf<DailyLogEntity>()
    private val fakeCycles = mutableListOf<CycleEntity>()

    private val fakeDailyLogDao = object : DailyLogDao {
        override suspend fun upsertDailyLog(log: DailyLogEntity) {
            fakeDailyLogs.removeAll { it.date == log.date }
            fakeDailyLogs.add(log)
        }
        override suspend fun getLogByDate(date: LocalDate): DailyLogEntity? = fakeDailyLogs.find { it.date == date }
        override suspend fun getLogsBetween(startDate: LocalDate, endDate: LocalDate): List<DailyLogEntity> =
            fakeDailyLogs.filter { !it.date.isBefore(startDate) && !it.date.isAfter(endDate) }.sortedBy { it.date }
        override suspend fun getRecentLogs(limit: Int): List<DailyLogEntity> = fakeDailyLogs.sortedByDescending { it.date }.take(limit)
        override suspend fun getAllLogsAsc(): List<DailyLogEntity> = fakeDailyLogs.sortedBy { it.date }
        override suspend fun getAllLogsDesc(): List<DailyLogEntity> = fakeDailyLogs.sortedByDescending { it.date }
        override fun getAllLogsFlow(): Flow<List<DailyLogEntity>> = flowOf(fakeDailyLogs)
        override suspend fun clearAllLogs() { fakeDailyLogs.clear() }
    }

    private val fakeCycleDao = object : CycleDao {
        private var idCounter = 1L
        override suspend fun insertCycle(cycle: CycleEntity): Long {
            val assigned = cycle.copy(id = if (cycle.id == 0L) idCounter++ else cycle.id)
            fakeCycles.add(assigned)
            return assigned.id
        }
        override suspend fun updateCycle(cycle: CycleEntity) {
            fakeCycles.removeAll { it.id == cycle.id }
            fakeCycles.add(cycle)
        }
        override suspend fun getLatestCycle(): CycleEntity? = fakeCycles.maxByOrNull { it.startDate }
        override suspend fun getCycleForDate(date: LocalDate): CycleEntity? = fakeCycles.find {
            !date.isBefore(it.startDate) && (it.endDate == null || !date.isAfter(it.endDate))
        }
        override suspend fun getCompletedCycles(): List<CycleEntity> = fakeCycles.filter { it.cycleLengthDays != null }
        override suspend fun getAllCycles(): List<CycleEntity> = fakeCycles.sortedByDescending { it.startDate }
        override fun getAllCyclesFlow(): Flow<List<CycleEntity>> = flowOf(fakeCycles)
        override suspend fun clearAllCycles() { fakeCycles.clear() }
        override suspend fun deleteCycle(cycle: CycleEntity) { fakeCycles.remove(cycle) }
    }

    private lateinit var aggregator: CycleAggregator

    @Before
    fun setUp() {
        fakeDailyLogs.clear()
        fakeCycles.clear()
        aggregator = CycleAggregator(fakeDailyLogDao, fakeCycleDao)
    }

    @Test
    fun testNewCycleCreation_activeBleedingThreshold() = runBlocking {
        val startDay = LocalDate.of(2026, 9, 1)

        // Day 1: Heavy bleed -> opens Cycle 1
        aggregator.onDailyLogSaved(DailyLogEntity(date = startDay, flow = FlowIntensity.HEAVY))
        assertEquals(1, fakeCycles.size)
        assertEquals(startDay, fakeCycles[0].startDate)

        // Day 15: Spotting -> does NOT open a new cycle
        aggregator.onDailyLogSaved(DailyLogEntity(date = startDay.plusDays(14), flow = FlowIntensity.SPOTTING))
        assertEquals(1, fakeCycles.size)

        // Day 25: Medium bleed (>= 20 days threshold) -> closes Cycle 1 and opens Cycle 2!
        aggregator.onDailyLogSaved(DailyLogEntity(date = startDay.plusDays(24), flow = FlowIntensity.MEDIUM))
        assertEquals(2, fakeCycles.size)

        val closedCycle = fakeCycles.find { it.startDate == startDay }
        assertNotNull(closedCycle)
        assertEquals(startDay.plusDays(23), closedCycle!!.endDate)
        assertEquals(24, closedCycle.cycleLengthDays)

        val newCycle = fakeCycles.find { it.startDate == startDay.plusDays(24) }
        assertNotNull(newCycle)
        assertNull(newCycle!!.endDate)
    }

    @Test
    fun testReconcileAllHistory_deterministicReconstruction() = runBlocking {
        // Feed out-of-order daily logs
        val cycle1Start = LocalDate.of(2026, 1, 1)
        val cycle2Start = LocalDate.of(2026, 1, 29) // 28 days later

        val logs = listOf(
            DailyLogEntity(date = cycle1Start, flow = FlowIntensity.HEAVY),
            DailyLogEntity(date = cycle1Start.plusDays(1), flow = FlowIntensity.MEDIUM),
            DailyLogEntity(date = cycle1Start.plusDays(2), flow = FlowIntensity.LIGHT),
            DailyLogEntity(date = cycle2Start, flow = FlowIntensity.HEAVY),
            DailyLogEntity(date = cycle2Start.plusDays(1), flow = FlowIntensity.MEDIUM)
        )

        logs.shuffled().forEach { fakeDailyLogDao.upsertDailyLog(it) }

        aggregator.reconcileAllHistory()

        assertEquals(2, fakeCycles.size)
        val sortedCycles = fakeCycles.sortedBy { it.startDate }

        // Cycle 1
        assertEquals(cycle1Start, sortedCycles[0].startDate)
        assertEquals(cycle2Start.minusDays(1), sortedCycles[0].endDate)
        assertEquals(28, sortedCycles[0].cycleLengthDays)
        assertEquals(3, sortedCycles[0].periodDurationDays)

        // Cycle 2 (ongoing)
        assertEquals(cycle2Start, sortedCycles[1].startDate)
        assertNull(sortedCycles[1].endDate)
        assertEquals(2, sortedCycles[1].periodDurationDays)
    }
}
