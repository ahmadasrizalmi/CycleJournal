package com.app.cyclejournal.domain.model

import com.app.cyclejournal.data.local.entity.AnomalyType
import java.time.LocalDate

/**
 * Clinical alert flagged by the anomaly evaluation engine.
 */
data class AnomalyAlert(
    val type: AnomalyType,
    val detectedDate: LocalDate,
    val details: String
)
