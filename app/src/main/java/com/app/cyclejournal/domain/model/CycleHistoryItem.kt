package com.app.cyclejournal.domain.model

import java.time.LocalDate

/**
 * Clean data model for cycle history visualization and statistics adhering to brief spec.
 */
data class CycleHistoryItem(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val cycleLength: Int,
    val periodDuration: Int,
    val ovulationDay: Int
) {
    companion object {
        /**
         * Ensures ovulationDay is strictly bounded (positive and does not exceed cycle length).
         */
        fun safeOvulationDay(cycleLength: Int): Int =
            (cycleLength - 14).coerceIn(1, cycleLength)
    }
}
