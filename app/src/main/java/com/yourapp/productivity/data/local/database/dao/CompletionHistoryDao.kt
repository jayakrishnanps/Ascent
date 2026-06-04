package com.yourapp.productivity.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourapp.productivity.data.local.database.entities.CompletionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: CompletionHistory)

    @Query("SELECT * FROM completion_history ORDER BY completedAt DESC")
    fun getAllHistory(): Flow<List<CompletionHistory>>

    @Query("SELECT COUNT(*) FROM completion_history WHERE completedAt >= :startOfWeek")
    suspend fun getCompletionsCountSince(startOfWeek: Long): Int

    @Query("SELECT COUNT(*) FROM completion_history")
    suspend fun getTotalCompletionsCount(): Int
}
