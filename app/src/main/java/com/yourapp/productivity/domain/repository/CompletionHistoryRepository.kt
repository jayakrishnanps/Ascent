package com.yourapp.productivity.domain.repository

import com.yourapp.productivity.data.local.database.entities.CompletionHistory
import kotlinx.coroutines.flow.Flow

interface CompletionHistoryRepository {
    suspend fun insert(history: CompletionHistory)
    fun getAllHistory(): Flow<List<CompletionHistory>>
    suspend fun getCompletionsCountSince(startOfWeek: Long): Int
    suspend fun getTotalCompletionsCount(): Int
}
