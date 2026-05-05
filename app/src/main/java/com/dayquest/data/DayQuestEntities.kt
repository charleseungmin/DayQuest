package com.dayquest.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val memo: String?,
    val categoryLabel: String?,
    val timeLabel: String?,
    val repeatRule: String?,
    val startDate: String?,
    val endDate: String?,
    val isActive: Boolean,
    val isCompleted: Boolean,
    val isOverdue: Boolean,
    val tier: String,
    val reminderEnabled: Boolean,
    val skippedDate: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val notificationEnabled: Boolean,
    val darkModeEnabled: Boolean,
    val reminderTime: String,
)

@Entity(
    tableName = "completion_logs",
    primaryKeys = ["taskId", "completedDate"],
    indices = [Index("completedDate")],
)
data class CompletionLogEntity(
    val taskId: Long,
    val completedDate: String,
    val earnedXp: Int,
)

@Entity(
    tableName = "daily_items",
    primaryKeys = ["taskId", "date"],
    indices = [Index("date"), Index("status")],
)
data class DailyItemEntity(
    val taskId: Long,
    val date: String,
    val status: String,
    val source: String,
    val doneAtEpochMillis: Long?,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "daily_quests",
    primaryKeys = ["date", "type"],
    indices = [Index("date"), Index("status")],
)
data class DailyQuestEntity(
    val date: String,
    val type: String,
    val status: String,
    val progressCurrent: Int,
    val progressTarget: Int,
    val achievedAtEpochMillis: Long?,
    val updatedAtEpochMillis: Long,
)

@Entity(tableName = "character_progress")
data class CharacterProgressEntity(
    @PrimaryKey val id: Int = 1,
    val level: Int,
    val expInLevel: Int,
    val nextLevelExp: Int,
    val totalExp: Int,
    val trainingPoints: Int,
    val focus: Int,
    val vitality: Int,
    val insight: Int,
    val balance: Int,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "character_reward_logs",
    primaryKeys = ["taskId", "date"],
    indices = [Index("date"), Index("statType")],
)
data class CharacterRewardLogEntity(
    val taskId: Long,
    val date: String,
    val rewardXp: Int,
    val statType: String,
    val statPoints: Int,
    val createdAtEpochMillis: Long,
)
