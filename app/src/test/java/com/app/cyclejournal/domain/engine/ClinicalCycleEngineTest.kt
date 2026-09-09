package com.app.cyclejournal.domain.engine

import com.app.cyclejournal.data.local.entity.AnomalyType
import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ClinicalCycleEngineTest {

    private lateinit var engine: ClinicalCycleEngine

    @Before
    fun setUp() {
        engine = ClinicalCycleEngine()
    }

    @Test
    fun testCalculateCycleStats_normalVariation() {
        // Dataset: 3 completed cycles with lengths 26, 28, 30 days
        // Mean = 28.0
        // Variance = ((26-28)^2 + (28-28)^2 + (30-28)^2) / (3 - 1) = (4 + 0 + 4) / 2 = 4.0
        // StdDev = sqrt(4.0) = 2.0
        val cycles = listOf(
            CycleEntity(id = 1, startDate = LocalDate.of(2026, 1, 1), endDate = LocalDate.of(2026, 1, 26), cycleLengthDays = 26, periodDurationDays = 5),
            CycleEntity(id = 2, startDate = LocalDate.of(2026, 1, 27), endDate = LocalDate.of(2026, 2, 23), cycleLengthDays = 28, periodDurationDays = 5),
            CycleEntity(id = 3, startDate = LocalDate.of(2026, 2, 24), endDate = LocalDate.of(2026, 3, 25), cycleLengthDays = 30, periodDurationDays = 5)
        )

        val stats = engine.calculateCycleStats(cycles)
        assertNotNull(stats)
        assertEquals(28.0, stats!!.averageLength, 0.001)
        assertEquals(2.0, stats.standardDeviation, 0.001)
        assertEquals(26, stats.minLength)
        assertEquals(30, stats.maxLength)
        assertEquals(5.0, stats.averagePeriodDuration, 0.001)
    }

    @Test
    fun testCalculateCycleStats_emptyOrInvalidListReturnsNull() {
        assertNull(engine.calculateCycleStats(emptyList()))

        val ongoingCycles = listOf(
            CycleEntity(id = 1, startDate = LocalDate.now(), endDate = null, cycleLengthDays = null)
        )
        assertNull(engine.calculateCycleStats(ongoingCycles))
    }

    @Test
    fun testPredictFertileWindow_standardCalendar() {
        val startDate = LocalDate.of(2026, 9, 1)
        val prediction = engine.predictFertileWindow(startDate, averageCycleLength = 28.0)

        assertEquals(LocalDate.of(2026, 9, 29), prediction.predictedNextPeriodDate)
        assertEquals(LocalDate.of(2026, 9, 15), prediction.predictedOvulationDate)
        assertEquals(LocalDate.of(2026, 9, 10), prediction.fertileWindowStart)
        assertEquals(LocalDate.of(2026, 9, 16), prediction.fertileWindowEnd)
    }

    @Test
    fun testDetectSymptothermalOvulation_valid3Over6Shift() {
        // 6 baseline days (max 36.40°C), followed by 3 days >= 36.60°C (shift >= +0.20°C)
        val startDate = LocalDate.of(2026, 8, 1)
        val logs = listOf(
            DailyLogEntity(date = startDate, basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(1), basalBodyTempCelsius = 36.35),
            DailyLogEntity(date = startDate.plusDays(2), basalBodyTempCelsius = 36.40), // Baseline max
            DailyLogEntity(date = startDate.plusDays(3), basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(4), basalBodyTempCelsius = 36.35),
            DailyLogEntity(date = startDate.plusDays(5), basalBodyTempCelsius = 36.30), // Day 6 (Ovulation day)
            DailyLogEntity(date = startDate.plusDays(6), basalBodyTempCelsius = 36.65), // Day 7 (Shift 1)
            DailyLogEntity(date = startDate.plusDays(7), basalBodyTempCelsius = 36.70), // Day 8 (Shift 2)
            DailyLogEntity(date = startDate.plusDays(8), basalBodyTempCelsius = 36.65)  // Day 9 (Shift 3)
        )

        val confirmedDate = engine.detectSymptothermalOvulation(logs)
        assertNotNull(confirmedDate)
        assertEquals(startDate.plusDays(5), confirmedDate) // Pegged to Day 6
    }

    @Test
    fun testDetectSymptothermalOvulation_unsustainedShiftReturnsNull() {
        val startDate = LocalDate.of(2026, 8, 1)
        val logs = listOf(
            DailyLogEntity(date = startDate, basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(1), basalBodyTempCelsius = 36.35),
            DailyLogEntity(date = startDate.plusDays(2), basalBodyTempCelsius = 36.40),
            DailyLogEntity(date = startDate.plusDays(3), basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(4), basalBodyTempCelsius = 36.35),
            DailyLogEntity(date = startDate.plusDays(5), basalBodyTempCelsius = 36.30),
            DailyLogEntity(date = startDate.plusDays(6), basalBodyTempCelsius = 36.65),
            DailyLogEntity(date = startDate.plusDays(7), basalBodyTempCelsius = 36.70),
            DailyLogEntity(date = startDate.plusDays(8), basalBodyTempCelsius = 36.35) // Drops below threshold!
        )

        assertNull(engine.detectSymptothermalOvulation(logs))
    }

    @Test
    fun testAnomalies_oligomenorrheaAndPolymenorrhea() {
        val historical = listOf(
            CycleEntity(id = 1, startDate = LocalDate.of(2026, 1, 1), cycleLengthDays = 40), // > 38 days
            CycleEntity(id = 2, startDate = LocalDate.of(2026, 2, 10), cycleLengthDays = 22) // < 24 days
        )

        val alerts = engine.evaluateAnomalies(null, historical, emptyList())
        assertTrue(alerts.any { it.type == AnomalyType.OLIGOMENORRHEA })
        assertTrue(alerts.any { it.type == AnomalyType.POLYMENORRHEA })
    }

    @Test
    fun testAnomalies_cycleIrregularity() {
        // Delta between max (35) and min (26) = 9 days (>= 8 days threshold)
        val historical = listOf(
            CycleEntity(id = 1, startDate = LocalDate.of(2026, 1, 1), cycleLengthDays = 26),
            CycleEntity(id = 2, startDate = LocalDate.of(2026, 1, 27), cycleLengthDays = 28),
            CycleEntity(id = 3, startDate = LocalDate.of(2026, 2, 24), cycleLengthDays = 35)
        )

        val alerts = engine.evaluateAnomalies(null, historical, emptyList())
        assertTrue(alerts.any { it.type == AnomalyType.CYCLE_IRREGULARITY })
    }

    @Test
    fun testAnomalies_prolongedBleeding() {
        val baseDate = LocalDate.of(2026, 9, 1)
        val logs = (0..8).map {
            DailyLogEntity(date = baseDate.plusDays(it.toLong()), flow = FlowIntensity.MEDIUM)
        } // 9 consecutive days (> 8 days)

        val alerts = engine.evaluateAnomalies(null, emptyList(), logs)
        assertTrue(alerts.any { it.type == AnomalyType.PROLONGED_BLEEDING })
    }

    @Test
    fun testAnomalies_intermenstrualBleeding() {
        val currentCycle = CycleEntity(id = 1, startDate = LocalDate.of(2026, 9, 1))
        val logs = listOf(
            // Spotting on cycle Day 15 with STICKY mucus (not ovulatory egg-white)
            DailyLogEntity(
                date = LocalDate.of(2026, 9, 15),
                flow = FlowIntensity.SPOTTING,
                cervicalMucus = CervicalMucusType.STICKY
            )
        )

        val alerts = engine.evaluateAnomalies(currentCycle, emptyList(), logs)
        assertTrue(alerts.any { it.type == AnomalyType.INTERMENSTRUAL_BLEEDING })
    }

    @Test
    fun testAnomalies_severeDysmenorrhea() {
        val logs = listOf(
            DailyLogEntity(
                date = LocalDate.of(2026, 9, 2),
                painVasScore = 8,
                painLocation = "pelvis"
            )
        )

        val alerts = engine.evaluateAnomalies(null, emptyList(), logs)
        assertTrue(alerts.any { it.type == AnomalyType.SEVERE_DYSMENORRHEA })
    }
}
