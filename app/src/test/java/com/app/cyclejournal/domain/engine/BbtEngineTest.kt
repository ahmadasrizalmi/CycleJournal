package com.app.cyclejournal.domain.engine

import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class BbtEngineTest {

    private lateinit var bbtEngine: BbtEngine

    @Before
    fun setUp() {
        bbtEngine = BbtEngine()
    }

    @Test
    fun test3Over6Shift_validConfirmation() {
        val startDate = LocalDate.of(2026, 8, 1)
        // 6 baseline low days: 36.30, 36.35, 36.40, 36.30, 36.35, 36.30 -> Coverline = 36.40
        // 3 shifted days: 36.65, 36.70, 36.65 (all >= 36.60, shift >= +0.20°C)
        val logs = listOf(
            DailyLogEntity(date = startDate, basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(1), basalBodyTempCelsius = 36.35),
            DailyLogEntity(date = startDate.plusDays(2), basalBodyTempCelsius = 36.40), // Coverline max
            DailyLogEntity(date = startDate.plusDays(3), basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(4), basalBodyTempCelsius = 36.35),
            DailyLogEntity(date = startDate.plusDays(5), basalBodyTempCelsius = 36.30), // Day 6 (Ovulation day)
            DailyLogEntity(date = startDate.plusDays(6), basalBodyTempCelsius = 36.65), // Shift 1
            DailyLogEntity(date = startDate.plusDays(7), basalBodyTempCelsius = 36.70), // Shift 2
            DailyLogEntity(date = startDate.plusDays(8), basalBodyTempCelsius = 36.65)  // Shift 3
        )

        val result = bbtEngine.evaluateBbtShift(logs)

        assertTrue(result.isConfirmed)
        assertEquals(startDate.plusDays(5), result.ovulationDate) // Pegged to Day 6
        assertEquals(36.40, result.coverlineCelsius!!, 0.001)
        assertTrue(result.shiftDegrees!! >= 0.20)
        assertEquals(6, result.baselineTemps.size)
    }

    @Test
    fun test3Over6Shift_unsustainedDropReturnsFalse() {
        val startDate = LocalDate.of(2026, 8, 1)
        // Day 9 drops to 36.35°C (below coverline + 0.20°C)
        val logs = listOf(
            DailyLogEntity(date = startDate, basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(1), basalBodyTempCelsius = 36.35),
            DailyLogEntity(date = startDate.plusDays(2), basalBodyTempCelsius = 36.40),
            DailyLogEntity(date = startDate.plusDays(3), basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(4), basalBodyTempCelsius = 36.35),
            DailyLogEntity(date = startDate.plusDays(5), basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(6), basalBodyTempCelsius = 36.65),
            DailyLogEntity(date = startDate.plusDays(7), basalBodyTempCelsius = 36.70),
            DailyLogEntity(date = startDate.plusDays(8), basalBodyTempCelsius = 36.35) // Drop!
        )

        val result = bbtEngine.evaluateBbtShift(logs)
        assertFalse(result.isConfirmed)
        assertNull(result.ovulationDate)
    }

    @Test
    fun test3Over6Shift_insufficientDataReturnsFalse() {
        val logs = (0..5).map {
            DailyLogEntity(date = LocalDate.now().plusDays(it.toLong()), basalBodyTempCelsius = 36.30)
        }
        val result = bbtEngine.evaluateBbtShift(logs)
        assertFalse(result.isConfirmed)
    }

    @Test
    fun testHormonalPhaseDetermination() {
        val cycleStart = LocalDate.of(2026, 9, 1)
        val cycle = CycleEntity(startDate = cycleStart, periodDurationDays = 5)
        val predictedOvulation = cycleStart.plusDays(14) // Sep 15

        // Day 3 (Sep 3): Menstrual
        assertEquals(
            HormonalPhase.MENSTRUAL,
            bbtEngine.determineHormonalPhase(cycle, cycleStart.plusDays(2), predictedOvulation)
        )

        // Day 8 (Sep 8): Follicular (before fertile window Sep 10)
        assertEquals(
            HormonalPhase.FOLLICULAR,
            bbtEngine.determineHormonalPhase(cycle, cycleStart.plusDays(7), predictedOvulation)
        )

        // Day 14 (Sep 14): Fertile Window & Ovulation Peak
        assertEquals(
            HormonalPhase.FERTILE_OVULATION,
            bbtEngine.determineHormonalPhase(cycle, cycleStart.plusDays(13), predictedOvulation)
        )

        // Day 20 (Sep 20): Luteal Phase (post-ovulation)
        assertEquals(
            HormonalPhase.LUTEAL,
            bbtEngine.determineHormonalPhase(cycle, cycleStart.plusDays(19), predictedOvulation)
        )
    }
}
