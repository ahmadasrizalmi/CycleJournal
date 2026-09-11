package com.app.cyclejournal.domain.model

import android.content.res.Resources
import androidx.annotation.StringRes
import com.app.cyclejournal.data.local.entity.AnomalyType
import java.time.LocalDate

/**
 * Clinical alert flagged by the anomaly evaluation engine.
 *
 * The human-readable detail is stored as a template resource so it follows the active app language.
 * [detailArgs] entries may be [ResArg] values, which must be resolved through [localizedDetail].
 */
data class AnomalyAlert(
    val type: AnomalyType,
    val detectedDate: LocalDate,
    @StringRes val detailRes: Int,
    val detailArgs: List<Any> = emptyList()
) {
    /** Formats the detail template using the language of [resources]. */
    fun localizedDetail(resources: Resources): String = resources.getString(
        detailRes,
        *detailArgs.map { if (it is ResArg) resources.getString(it.id) else it }.toTypedArray()
    )
}

/** Formatting argument that must be resolved as a string resource before formatting. */
@JvmInline
value class ResArg(@StringRes val id: Int)
