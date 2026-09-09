package com.app.cyclejournal.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Room TypeConverter handling java.time.LocalDate precision using ISO-8601 strings.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }
}
