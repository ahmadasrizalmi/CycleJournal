package com.app.cyclejournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyLog(log: DailyLogEntity)

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getLogByDate(date: LocalDate): DailyLogEntity?

    @Query("SELECT * FROM daily_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getLogsBetween(startDate: LocalDate, endDate: LocalDate): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs ORDER BY date ASC")
    suspend fun getAllLogsAsc(): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    suspend fun getAllLogsDesc(): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllLogsFlow(): Flow<List<DailyLogEntity>>

    @Query("DELETE FROM daily_logs")
    suspend fun clearAllLogs()
}
