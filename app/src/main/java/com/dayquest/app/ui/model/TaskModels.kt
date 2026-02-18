package com.dayquest.app.ui.model

import com.dayquest.app.core.model.TaskPriority
import java.util.UUID

data class TaskItemUi(
    val id: String,
    val sourceTaskId: String? = null,
    val title: String,
    val category: String,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isImportant: Boolean = false,
    val isDone: Boolean = false,
    val isDeferred: Boolean = false
)

data class TaskFormUi(
    val editingTaskId: String? = null,
    val title: String = "",
    val category: String = "일반",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isImportant: Boolean = false
)

data class QuestProgressUi(
    val doneCount: Int,
    val totalCount: Int
)

data class StreakUi(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
)

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val resetDone: Boolean = false,
    val isLoading: Boolean = false,
    val isResetting: Boolean = false,
    val isSyncingToday: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null
)

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data class Ready(
        val tasks: List<TaskItemUi> = emptyList(),
        val streak: StreakUi = StreakUi()
    ) : TodayUiState

    data class Error(val message: String) : TodayUiState
}

data class HistoryDayProgressUi(
    val dateKey: String,
    val dateLabel: String,
    val weekdayLabel: String,
    val doneCount: Int,
    val deferredCount: Int,
    val totalCount: Int,
    val completionRate: Int
)

enum class HistoryPeriodUi(val label: String, val days: Long) {
    Weekly("최근 7일", 7),
    Monthly("최근 30일", 30)
}

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Ready(
        val todayDoneCount: Int,
        val todayDeferredCount: Int,
        val todayTotalCount: Int,
        val selectedPeriod: HistoryPeriodUi,
        val periodDoneCount: Int,
        val periodDeferredCount: Int,
        val periodTotalCount: Int,
        val showOnlyActiveDays: Boolean,
        val dailyProgress: List<HistoryDayProgressUi>
    ) : HistoryUiState

    data class Error(val message: String) : HistoryUiState
}

sealed interface TaskManageUiState {
    data object Loading : TaskManageUiState
    data class Ready(
        val tasks: List<TaskItemUi> = emptyList(),
        val form: TaskFormUi = TaskFormUi(),
        val noticeMessage: String? = null
    ) : TaskManageUiState

    data class Error(val message: String) : TaskManageUiState
}

fun newTaskItem(title: String, category: String): TaskItemUi =
    TaskItemUi(
        id = UUID.randomUUID().toString(),
        title = title,
        category = category
    )
