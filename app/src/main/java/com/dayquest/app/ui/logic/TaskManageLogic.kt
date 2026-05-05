package com.dayquest.app.ui.logic

import com.dayquest.app.ui.model.TaskFormUi
import com.dayquest.app.ui.model.TaskManageUiState
import com.dayquest.app.ui.model.newTaskItem

object TaskManageLogic {
    fun upsert(state: TaskManageUiState.Ready): TaskManageUiState.Ready {
        val title = state.form.title.trim()
        if (title.isEmpty()) return state

        val category = state.form.category.trim().ifEmpty { "일반" }
        val updated = if (state.form.editingTaskId == null) {
            state.tasks + newTaskItem(title = title, category = category)
        } else {
            state.tasks.map {
                if (it.id == state.form.editingTaskId) {
                    it.copy(title = title, category = category)
                } else {
                    it
                }
            }
        }
        return state.copy(tasks = updated, form = TaskFormUi())
    }

    fun edit(state: TaskManageUiState.Ready, taskId: String): TaskManageUiState.Ready {
        val task = state.tasks.firstOrNull { it.id == taskId } ?: return state
        return state.copy(form = TaskFormUi(editingTaskId = task.id, title = task.title, category = task.category))
    }

    fun toggleDone(state: TaskManageUiState.Ready, taskId: String): TaskManageUiState.Ready {
        return state.copy(tasks = state.tasks.map { if (it.id == taskId) it.copy(isDone = !it.isDone) else it })
    }

    fun delete(state: TaskManageUiState.Ready, taskId: String): TaskManageUiState.Ready {
        return state.copy(
            tasks = state.tasks.filterNot { it.id == taskId },
            form = if (state.form.editingTaskId == taskId) TaskFormUi() else state.form
        )
    }
}
