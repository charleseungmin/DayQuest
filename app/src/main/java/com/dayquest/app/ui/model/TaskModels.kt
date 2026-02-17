package com.dayquest.app.ui.model

import java.util.UUID

data class TaskItemUi(
    val id: String,
    val sourceTaskId: String? = null,
    val title: String,
    val category: String,
    val isDone: Boolean = false,
    val isDeferred: Boolean = false
)

data class TaskFormUi(
    val editingTaskId: String? = null,
    val title: String = "",
    val category: String = "일반"
)

data class QuestProgressUi(
    val doneCount: Int,
    val totalCount: Int
)

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val resetDone: Boolean = false,
    val isLoading: Boolean = false,
    val isResetting: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null
)

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data class Ready(
        val tasks: List<TaskItemUi> = emptyList()
    ) : TodayUiState

    data class Error(val message: String) : TodayUiState
}

data class HistoryDayProgressUi(
    val dateLabel: String,
    val doneCount: Int,
    val totalCount: Int
)

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Ready(
        val todayDoneCount: Int,
        val todayDeferredCount: Int,
        val todayTotalCount: Int,
        val weeklyDoneCount: Int,
        val weeklyDeferredCount: Int,
        val weeklyTotalCount: Int,
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
