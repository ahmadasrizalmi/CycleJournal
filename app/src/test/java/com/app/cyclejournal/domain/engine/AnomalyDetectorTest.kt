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

class AnomalyDetectorTest {

    private lateinit var detector: AnomalyDetector

    @Before
    fun setUp() {
        detector = AnomalyDetector()
    }

    @Test
    fun testVasImpactClassification() {
        val impact0 = detector.mapVasImpact(0)
        assertEquals(VasCategory.NONE, impact0.category)
        assertFalse(impact0.isRedFlag)

        val impact2 = detector.mapVasImpact(2)
        assertEquals(VasCategory.MILD, impact2.category)
        assertFalse(impact2.isRedFlag)

        val impact5 = detector.mapVasImpact(5)
        assertEquals(VasCategory.MODERATE, impact5.category)
        assertFalse(impact5.isRedFlag)

        val impact7 = detector.mapVasImpact(7)
        assertEquals(VasCategory.SEVERE, impact7.category)
        assertTrue(impact7.isRedFlag)

        val impact9 = detector.mapVasImpact(9)
        assertEquals(VasCategory.EXTREME, impact9.category)
        assertTrue(impact9.isRedFlag)
    }

    @Test
    fun testDetectOligomenorrheaAndPolymenorrhea() {
        val cycles = listOf(
            CycleEntity(id = 1, startDate = LocalDate.of(2026, 1, 1), cycleLengthDays = 42), // > 38 days
            CycleEntity(id = 2, startDate = LocalDate.of(2026, 2, 12), cycleLengthDays = 21) // < 24 days
        )

        val alerts = detector.detectAnomalies(null, cycles, emptyList())
        assertTrue(alerts.any { it.type == AnomalyType.OLIGOMENORRHEA })
        assertTrue(alerts.any { it.type == AnomalyType.POLYMENORRHEA })
    }

    @Test
    fun testDetectFigoIrregularity() {
        // Delta = 36 - 26 = 10 days (>= 8 days threshold)
        val cycles = listOf(
            CycleEntity(id = 1, startDate = LocalDate.of(2026, 1, 1), cycleLengthDays = 26),
            CycleEntity(id = 2, startDate = LocalDate.of(2026, 1, 27), cycleLengthDays = 28),
            CycleEntity(id = 3, startDate = LocalDate.of(2026, 2, 24), cycleLengthDays = 36)
        )

        val alerts = detector.detectAnomalies(null, cycles, emptyList())
        assertTrue(alerts.any { it.type == AnomalyType.CYCLE_IRREGULARITY })
    }

    @Test
    fun testDetectProlongedBleeding() {
        val baseDate = LocalDate.of(2026, 9, 1)
        val logs = (0..9).map {
            DailyLogEntity(date = baseDate.plusDays(it.toLong()), flow = FlowIntensity.HEAVY)
        } // 10 consecutive bleeding days

        val alerts = detector.detectAnomalies(null, emptyList(), logs)
        assertTrue(alerts.any { it.type == AnomalyType.PROLONGED_BLEEDING })
    }

    @Test
    fun testDetectIntermenstrualBleeding() {
        val cycle = CycleEntity(startDate = LocalDate.of(2026, 9, 1))
        val logs = listOf(
            DailyLogEntity(
                date = LocalDate.of(2026, 9, 16), // Day 16 of cycle
                flow = FlowIntensity.SPOTTING,
                cervicalMucus = CervicalMucusType.STICKY // Non-ovulatory
            )
        )

        val alerts = detector.detectAnomalies(cycle, emptyList(), logs)
        assertTrue(alerts.any { it.type == AnomalyType.INTERMENSTRUAL_BLEEDING })
    }

    @Test
    fun testDetectSevereDysmenorrhea() {
        val logs = listOf(
            DailyLogEntity(
                date = LocalDate.of(2026, 9, 2),
                painVasScore = 8,
                painLocation = "lower_back"
            )
        )

        val alerts = detector.detectAnomalies(null, emptyList(), logs)
        assertTrue(alerts.any { it.type == AnomalyType.SEVERE_DYSMENORRHEA })
    }
}
