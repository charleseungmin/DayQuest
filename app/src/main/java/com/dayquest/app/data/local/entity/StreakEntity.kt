package com.dayquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastAchievedDateKey: String? = null,
    val updatedAtEpochMillis: Long
)
