package com.app.cyclejournal.domain.model

import java.time.LocalDate

/**
 * Fertile window and next period prediction based on clinical luteal phase assumptions.
 */
data class FertilePrediction(
    val predictedNextPeriodDate: LocalDate,
    val predictedOvulationDate: LocalDate,
    val fertileWindowStart: LocalDate, // Ovulation - 5 days
    val fertileWindowEnd: LocalDate // Ovulation + 1 day
)
