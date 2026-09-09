package com.app.cyclejournal.domain.model

/**
 * Statistical summary of menstrual cycle characteristics adhering to FIGO clinical standards.
 */
data class CycleStats(
    val averageLength: Double,
    val standardDeviation: Double,
    val minLength: Int,
    val maxLength: Int,
    val averagePeriodDuration: Double
)
