package com.app.cyclejournal.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.cyclejournal.data.local.entity.CycleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CycleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: CycleEntity): Long

    @Update
    suspend fun updateCycle(cycle: CycleEntity)

    @Query("SELECT * FROM cycle_records ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestCycle(): CycleEntity?

    @Query("SELECT * FROM cycle_records WHERE startDate <= :date AND (endDate >= :date OR endDate IS NULL) LIMIT 1")
    suspend fun getCycleForDate(date: LocalDate): CycleEntity?

    @Query("SELECT * FROM cycle_records WHERE cycleLengthDays IS NOT NULL ORDER BY startDate DESC")
    suspend fun getCompletedCycles(): List<CycleEntity>

    @Query("SELECT * FROM cycle_records ORDER BY startDate DESC")
    suspend fun getAllCycles(): List<CycleEntity>

    @Query("SELECT * FROM cycle_records ORDER BY startDate DESC")
    fun getAllCyclesFlow(): Flow<List<CycleEntity>>

    @Query("DELETE FROM cycle_records")
    suspend fun clearAllCycles()

    @Delete
    suspend fun deleteCycle(cycle: CycleEntity)
}
