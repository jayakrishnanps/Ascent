package com.yourapp.productivity.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val conditionType: String,
    val conditionValue: Int,
    val iconRes: Int
)
