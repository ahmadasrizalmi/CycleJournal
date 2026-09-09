package com.app.cyclejournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Daily log record capturing physical biomarkers, pain scores, and clinical notes for a single calendar date.
 */
@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey
    val date: LocalDate,
    val flow: FlowIntensity = FlowIntensity.NONE,
    val basalBodyTempCelsius: Double? = null, // e.g. 36.45
    val cervicalMucus: CervicalMucusType = CervicalMucusType.NONE,
    val painVasScore: Int = 0, // Visual Analog Scale 0 to 10
    val painLocation: String? = null, // e.g. "pelvis", "lower_back"
    val takenAnalgesic: Boolean = false,
    val notes: String? = null
)
