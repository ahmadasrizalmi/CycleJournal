package com.app.cyclejournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Aggregated menstrual cycle record derived from daily log entries.
 */
@Entity(tableName = "cycle_records")
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: LocalDate, // Day 1 of true menstrual bleeding
    val endDate: LocalDate? = null, // Day before start of next cycle (null if ongoing)
    val periodDurationDays: Int = 0, // Consecutive days of active bleeding at cycle start
    val cycleLengthDays: Int? = null, // Total days from startDate to endDate inclusive
    val confirmedOvulationDate: LocalDate? = null // Confirmed via symptothermal 3-over-6 rule
)
