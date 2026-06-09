package com.yourapp.productivity.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val taskId: String?, // ID of the specific task this achievement is tied to
    val conditionType: AchievementConditionType,
    val targetValue: Int,
    val currentProgress: Int = 0,
    val isEarned: Boolean = false,
    val earnedAt: Long? = null
)

enum class AchievementConditionType {
    COMPLETE_N_TIMES,
    COMPLETE_UNTIL_END_DATE
}
