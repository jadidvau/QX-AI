package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SignalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    @Query("SELECT * FROM signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<SignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity): Long

    @Query("DELETE FROM signals WHERE id = :id")
    suspend fun deleteSignalById(id: Int)

    @Query("UPDATE signals SET outcome = :outcome WHERE id = :id")
    suspend fun updateSignalOutcome(id: Int, outcome: String)

    @Query("DELETE FROM signals")
    suspend fun clearAllSignals()
}
