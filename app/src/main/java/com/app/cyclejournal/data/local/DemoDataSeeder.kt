package com.app.cyclejournal.data.local

import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import java.time.LocalDate
import kotlin.math.round
import kotlin.random.Random

/**
 * Seeds a realistic cycle history so marketing screenshots show a populated app instead of an
 * empty one. Only runs when `BuildConfig.DEMO_SEED` is true (built with `-PdemoSeed=true`), so the
 * shipped APK never writes demo content.
 *
 * Every value is language neutral on purpose: flow, mucus and pain render from enums and numbers,
 * and no symptom names or notes are stored, so one dataset looks correct in every locale.
 */
object DemoDataSeeder {

    private const val CYCLE_LENGTH = 27
    private const val PERIOD_LENGTH = 5

    /** Days from the newest period start to today: mid-cycle, so the dashboard shows the fertile window. */
    private const val DAYS_SINCE_LAST_START = 16

    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.dailyLogDao().getRecentLogs(1).isNotEmpty()) return

        val today = LocalDate.now()
        val currentStart = today.minusDays(DAYS_SINCE_LAST_START.toLong())
        val previousStart = currentStart.minusDays(CYCLE_LENGTH.toLong())
        val oldestStart = previousStart.minusDays(CYCLE_LENGTH.toLong())
        val random = Random(20260912)

        db.cycleDao().insertCycles(
            listOf(
                cycle(oldestStart, previousStart.minusDays(1), oldestStart.plusDays(13)),
                cycle(previousStart, currentStart.minusDays(1), previousStart.plusDays(13)),
                cycle(currentStart, null, currentStart.plusDays(13))
            )
        )

        val logs = mutableListOf<DailyLogEntity>()
        var cycleStart = oldestStart
        var date = oldestStart
        while (!date.isAfter(today)) {
            if (date == previousStart || date == currentStart) cycleStart = date
            val dayOfCycle = (date.toEpochDay() - cycleStart.toEpochDay() + 1).toInt()

            val flow = when (dayOfCycle) {
                1, 2 -> FlowIntensity.HEAVY
                3 -> FlowIntensity.MEDIUM
                4, 5 -> FlowIntensity.LIGHT
                6 -> FlowIntensity.SPOTTING
                else -> FlowIntensity.NONE
            }
            // Biphasic curve: low before ovulation, a clear thermal shift after it.
            val bbt = if (dayOfCycle <= 13) {
                36.28 + random.nextDouble(0.0, 0.12)
            } else {
                36.62 + random.nextDouble(0.0, 0.14)
            }
            val mucus = when (dayOfCycle) {
                in 12..15 -> CervicalMucusType.EGG_WHITE
                in 16..18 -> CervicalMucusType.CREAMY
                in 7..11 -> CervicalMucusType.STICKY
                else -> CervicalMucusType.DRY
            }
            val pain = when {
                dayOfCycle in 1..2 -> 7
                dayOfCycle == 3 -> 5
                dayOfCycle in 4..5 -> 3
                dayOfCycle >= CYCLE_LENGTH - 1 -> 2
                else -> 0
            }

            logs += DailyLogEntity(
                date = date,
                flow = flow,
                basalBodyTempCelsius = round(bbt * 100) / 100,
                cervicalMucus = mucus,
                painVasScore = pain,
                takenAnalgesic = pain >= 5
            )
            date = date.plusDays(1)
        }
        // One transaction, so the dashboard is populated by the time the first frame renders.
        db.dailyLogDao().upsertDailyLogs(logs)
    }

    private fun cycle(start: LocalDate, end: LocalDate?, ovulation: LocalDate) = CycleEntity(
        startDate = start,
        endDate = end,
        periodDurationDays = PERIOD_LENGTH,
        cycleLengthDays = if (end == null) null else CYCLE_LENGTH,
        confirmedOvulationDate = ovulation
    )
}
