package com.yourapp.productivity.data.repository

import com.yourapp.productivity.data.local.database.dao.CompletionHistoryDao
import com.yourapp.productivity.data.local.database.entities.CompletionHistory
import com.yourapp.productivity.domain.repository.CompletionHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompletionHistoryRepositoryImpl @Inject constructor(
    private val dao: CompletionHistoryDao
) : CompletionHistoryRepository {
    override suspend fun insert(history: CompletionHistory) = dao.insert(history)
    override fun getAllHistory(): Flow<List<CompletionHistory>> = dao.getAllHistory()
    override suspend fun getCompletionsCountSince(startOfWeek: Long): Int = dao.getCompletionsCountSince(startOfWeek)
    override suspend fun getTotalCompletionsCount(): Int = dao.getTotalCompletionsCount()
}
