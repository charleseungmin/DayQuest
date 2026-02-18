package com.dayquest.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayquest.app.data.local.entity.TaskEntity
import com.dayquest.app.domain.usecase.task.DeleteManageTaskUseCase
import com.dayquest.app.domain.usecase.task.ObserveManageTasksUseCase
import com.dayquest.app.domain.usecase.task.SaveManageTaskResult
import com.dayquest.app.domain.usecase.task.SaveManageTaskUseCase
import com.dayquest.app.ui.model.TaskFormUi
import com.dayquest.app.ui.model.TaskItemUi
import com.dayquest.app.ui.model.TaskManageUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TaskManageViewModel @Inject constructor(
    private val observeManageTasksUseCase: ObserveManageTasksUseCase,
    private val saveManageTaskUseCase: SaveManageTaskUseCase,
    private val deleteManageTaskUseCase: DeleteManageTaskUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<TaskManageUiState>(TaskManageUiState.Loading)
    val uiState: StateFlow<TaskManageUiState> = _uiState.asStateFlow()

    private var doneTaskIds: Set<String> = emptySet()
    private var observeJob: Job? = null
    private var pendingEditTaskId: String? = null

    init {
        observeTasks()
    }

    fun retry() {
        observeTasks(forceLoading = true)
    }

    fun consumeNotice() {
        _uiState.updateReady { it.copy(noticeMessage = null) }
    }

    fun updateTitle(title: String) {
        _uiState.updateReady { it.copy(form = it.form.copy(title = title)) }
    }

    fun updateCategory(category: String) {
        _uiState.updateReady { it.copy(form = it.form.copy(category = category)) }
    }

    fun updateImportant(isImportant: Boolean) {
        _uiState.updateReady { it.copy(form = it.form.copy(isImportant = isImportant)) }
    }

    fun edit(taskId: String) {
        _uiState.updateReady { state ->
            val task = state.tasks.firstOrNull { it.id == taskId } ?: return@updateReady state
            state.copy(form = TaskFormUi(editingTaskId = task.id, title = task.title, category = task.category, isImportant = task.isImportant))
        }
    }

    fun requestEdit(taskId: String?) {
        if (taskId.isNullOrBlank()) return
        pendingEditTaskId = taskId
        _uiState.updateReady { state ->
            if (state.tasks.any { it.id == taskId }) {
                pendingEditTaskId = null
                val task = state.tasks.first { it.id == taskId }
                state.copy(form = TaskFormUi(editingTaskId = task.id, title = task.title, category = task.category, isImportant = task.isImportant))
            } else {
                state
            }
        }
    }

    fun upsert() {
        val state = _uiState.value as? TaskManageUiState.Ready ?: return
        val title = state.form.title.trim()
        if (title.isEmpty()) return
        val category = state.form.category.trim().ifEmpty { "일반" }
        val isImportant = state.form.isImportant
        val taskId = state.form.editingTaskId?.toLongOrNull()

        viewModelScope.launch {
            when (saveManageTaskUseCase(taskId = taskId, title = title, category = category, isImportant = isImportant, now = System.currentTimeMillis())) {
                SaveManageTaskResult.Created,
                SaveManageTaskResult.Updated -> _uiState.updateReady { it.copy(form = TaskFormUi(), noticeMessage = null) }

                SaveManageTaskResult.MissingTarget -> {
                    _uiState.updateReady {
                        it.copy(
                            form = it.form.copy(editingTaskId = null),
                            noticeMessage = "수정 대상 할 일을 찾지 못했어요. 내용은 유지했으니 다시 저장해 주세요."
                        )
                    }
                }

                SaveManageTaskResult.DuplicateTitle -> {
                    _uiState.updateReady {
                        it.copy(noticeMessage = "같은 이름의 할 일이 이미 있어요. 제목을 다르게 입력해 주세요.")
                    }
                }
            }
        }
    }

    fun toggleDone(taskId: String) {
        doneTaskIds = if (doneTaskIds.contains(taskId)) {
            doneTaskIds - taskId
        } else {
            doneTaskIds + taskId
        }
        _uiState.updateReady { state ->
            state.copy(tasks = state.tasks.map { if (it.id == taskId) it.copy(isDone = !it.isDone) else it })
        }
    }

    fun delete(taskId: String) {
        val longId = taskId.toLongOrNull() ?: return
        viewModelScope.launch {
            deleteManageTaskUseCase(longId, System.currentTimeMillis())
            doneTaskIds = doneTaskIds - taskId
            _uiState.updateReady { state ->
                if (state.form.editingTaskId == taskId) state.copy(form = TaskFormUi()) else state
            }
        }
    }

    private fun observeTasks(forceLoading: Boolean = false) {
        observeJob?.cancel()
        if (forceLoading || _uiState.value !is TaskManageUiState.Ready) {
            _uiState.value = TaskManageUiState.Loading
        }
        observeJob = viewModelScope.launch {
            observeManageTasksUseCase()
                .catch { _uiState.value = TaskManageUiState.Error("Task 목록을 불러오지 못했습니다.") }
                .collect { tasks ->
                    val mapped = tasks.map { it.toTaskItemUi(doneTaskIds.contains(it.id.toString())) }
                    val ready = _uiState.value as? TaskManageUiState.Ready
                    val currentForm = ready?.form ?: TaskFormUi()
                    val currentNotice = ready?.noticeMessage
                    val nextForm = pendingEditTaskId
                        ?.let { editId ->
                            mapped.firstOrNull { it.id == editId }?.let {
                                pendingEditTaskId = null
                                TaskFormUi(editingTaskId = it.id, title = it.title, category = it.category, isImportant = it.isImportant)
                            }
                        }
                        ?: currentForm
                    _uiState.value = TaskManageUiState.Ready(
                        tasks = mapped,
                        form = nextForm,
                        noticeMessage = currentNotice
                    )
                }
        }
    }
}

private fun MutableStateFlow<TaskManageUiState>.updateReady(block: (TaskManageUiState.Ready) -> TaskManageUiState.Ready) {
    update { state ->
        if (state is TaskManageUiState.Ready) block(state) else state
    }
}

private fun TaskEntity.toTaskItemUi(isDone: Boolean): TaskItemUi {
    return TaskItemUi(
        id = id.toString(),
        title = title,
        category = description?.takeIf { it.isNotBlank() } ?: "일반",
        isImportant = isImportant,
        isDone = isDone
    )
}
