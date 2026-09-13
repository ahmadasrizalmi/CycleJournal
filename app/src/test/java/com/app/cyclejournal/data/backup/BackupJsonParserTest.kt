package com.app.cyclejournal.data.backup

import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * The backup payload is the only copy of a user's history once the app is reinstalled, so the
 * serializer and parser must round-trip every field - including the empty ones - without loss.
 */
class BackupJsonParserTest {

    private val logs = listOf(
        DailyLogEntity(
            date = LocalDate.of(2026, 8, 24),
            flow = FlowIntensity.HEAVY,
            basalBodyTempCelsius = 36.38,
            cervicalMucus = CervicalMucusType.CREAMY,
            painVasScore = 6,
            painLocation = "pelvis, lower_back",
            takenAnalgesic = true,
            notes = "cramps, took a painkiller"
        ),
        DailyLogEntity(
            date = LocalDate.of(2026, 8, 25),
            flow = FlowIntensity.SPOTTING,
            basalBodyTempCelsius = null,
            cervicalMucus = CervicalMucusType.NONE,
            painVasScore = 0,
            painLocation = null,
            takenAnalgesic = false,
            notes = null
        ),
        DailyLogEntity(date = LocalDate.of(2026, 9, 13), flow = FlowIntensity.NONE)
    )

    @Test
    fun `round trip keeps every field, including the empty ones`() {
        val json = BackupJsonParser.serializeBackupPayload("px-test-1", logs)
        val restored = BackupJsonParser.parseDailyLogs(json)

        assertEquals(logs.size, restored.size)
        logs.zip(restored).forEach { (original, copy) ->
            assertEquals(original.date, copy.date)
            assertEquals(original.flow, copy.flow)
            assertEquals(original.basalBodyTempCelsius, copy.basalBodyTempCelsius)
            assertEquals(original.cervicalMucus, copy.cervicalMucus)
            assertEquals(original.painVasScore, copy.painVasScore)
            assertEquals(original.painLocation, copy.painLocation)
            assertEquals(original.takenAnalgesic, copy.takenAnalgesic)
            assertEquals(original.notes, copy.notes)
        }
        assertNull(restored[1].basalBodyTempCelsius)
        assertNull(restored[1].painLocation)
        assertNull(restored[1].notes)
        assertEquals(CervicalMucusType.NONE, restored[1].cervicalMucus)
    }

    @Test
    fun `payload carries the user id so a restore can attribute the data`() {
        val json = BackupJsonParser.serializeBackupPayload("px-abc-123", logs)
        assertTrue(json.contains("px-abc-123"))
    }

    @Test
    fun `a payload without daily logs restores nothing instead of throwing`() {
        assertEquals(emptyList<DailyLogEntity>(), BackupJsonParser.parseDailyLogs("{}"))
        assertEquals(emptyList<DailyLogEntity>(), BackupJsonParser.parseDailyLogs("""{"version":1}"""))
    }

    @Test
    fun `an unknown enum value falls back to the neutral option`() {
        val json = """{"daily_logs":[{"date":"2026-09-13","flow":"GUSHING","mucus":"GLITTER"}]}"""
        val restored = BackupJsonParser.parseDailyLogs(json)
        assertEquals(1, restored.size)
        assertEquals(FlowIntensity.NONE, restored[0].flow)
        assertEquals(CervicalMucusType.NONE, restored[0].cervicalMucus)
    }
}
