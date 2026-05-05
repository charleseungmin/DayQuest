package com.dayquest.domain

import com.dayquest.app.ui.HistoryDayUi
import com.dayquest.app.ui.CharacterGrowthUi
import com.dayquest.app.ui.TodayTaskUi
import kotlinx.coroutines.flow.Flow

data class DayQuestSettings(
    val notificationEnabled: Boolean,
    val darkModeEnabled: Boolean,
    val reminderTime: String,
)

interface DayQuestRepository {
    fun observeTasks(): Flow<List<TodayTaskUi>>
    fun observeCharacter(): Flow<CharacterGrowthUi>
    fun observeSettings(): Flow<DayQuestSettings>
    fun observeHistory(): Flow<List<HistoryDayUi>>
    suspend fun addTask(draft: TaskDraft)
    suspend fun updateTask(taskId: Long, draft: TaskDraft)
    suspend fun toggleTask(taskId: Long, nowEpochMillis: Long)
    suspend fun snoozeTask(taskId: Long, nowEpochMillis: Long, minutes: Long = 30)
    suspend fun skipTask(taskId: Long, nowEpochMillis: Long)
    suspend fun deleteTask(taskId: Long)
    suspend fun updateNotificationEnabled(enabled: Boolean)
    suspend fun updateDarkMode(enabled: Boolean)
    suspend fun updateReminderTime(time: String)
    suspend fun resetAll()
    suspend fun loadReminderPlan(): ReminderPlan
}

data class ReminderPlan(
    val enabled: Boolean,
    val reminderTime: String,
    val quests: List<TodayTaskUi>,
)
