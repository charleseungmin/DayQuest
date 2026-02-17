package com.dayquest.app.ui.model

import java.util.UUID

data class TaskItemUi(
    val id: String,
    val title: String,
    val category: String,
    val isDone: Boolean = false
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
    val resetDone: Boolean = false
)

sealed interface TaskManageUiState {
    data object Loading : TaskManageUiState
    data class Ready(
        val tasks: List<TaskItemUi> = emptyList(),
        val form: TaskFormUi = TaskFormUi()
    ) : TaskManageUiState

    data class Error(val message: String) : TaskManageUiState
}

fun newTaskItem(title: String, category: String): TaskItemUi =
    TaskItemUi(
        id = UUID.randomUUID().toString(),
        title = title,
        category = category
    )
