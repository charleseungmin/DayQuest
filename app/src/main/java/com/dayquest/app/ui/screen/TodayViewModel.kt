package com.dayquest.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.projection.TodayTaskRow
import com.dayquest.app.domain.usecase.task.DeleteManageTaskUseCase
import com.dayquest.app.domain.usecase.today.GenerateTodayItemsUseCase
import com.dayquest.app.domain.usecase.today.ObserveStreakUseCase
import com.dayquest.app.domain.usecase.today.ObserveTodayTasksUseCase
import com.dayquest.app.domain.usecase.today.RecalculateStreakUseCase
import com.dayquest.app.domain.usecase.today.SyncDailyQuestsUseCase
import com.dayquest.app.domain.usecase.today.UpdateDailyItemStatusUseCase
import com.dayquest.app.ui.model.TaskItemUi
import com.dayquest.app.ui.model.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val generateTodayItemsUseCase: GenerateTodayItemsUseCase,
    private val observeTodayTasksUseCase: ObserveTodayTasksUseCase,
    private val observeStreakUseCase: ObserveStreakUseCase,
    private val updateDailyItemStatusUseCase: UpdateDailyItemStatusUseCase,
    private val syncDailyQuestsUseCase: SyncDailyQuestsUseCase,
    private val recalculateStreakUseCase: RecalculateStreakUseCase,
    private val deleteManageTaskUseCase: DeleteManageTaskUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<TodayUiState>(TodayUiState.Loading)
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val today: LocalDate = LocalDate.now()
    private var observeJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        observeJob?.cancel()
        _uiState.value = TodayUiState.Loading
        observeJob = viewModelScope.launch {
            val now = System.currentTimeMillis()
            runCatching {
                generateTodayItemsUseCase(today, now)
                syncDailyQuestsUseCase.ensureQuestMeta(today)
                syncDailyQuestsUseCase.syncProgress(today, now)
                recalculateStreakUseCase(today, now)
            }.onFailure {
                _uiState.value = TodayUiState.Error("오늘 할 일 생성을 실패했습니다.")
                return@launch
            }

            combine(
                observeTodayTasksUseCase(today),
                observeStreakUseCase()
            ) { rows, streak ->
                TodayUiState.Ready(
                    tasks = rows.map(::todayTaskRowToTaskItemUi),
                    streak = streak
                )
            }
                .catch { _uiState.value = TodayUiState.Error("오늘 할 일을 불러오지 못했습니다.") }
                .collect { readyState ->
                    _uiState.value = readyState
                }
        }
    }

    fun toggleDone(taskId: String, isDone: Boolean) {
        val dailyItemId = taskId.toLongOrNull() ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            updateDailyItemStatusUseCase(
                dailyItemId = dailyItemId,
                toStatus = if (isDone) DailyItemStatus.TODO else DailyItemStatus.DONE,
                nowEpochMillis = now
            )
            syncDailyQuestsUseCase.syncProgress(today, now)
            recalculateStreakUseCase(today, now)
        }
    }

    fun toggleDeferred(taskId: String, isDeferred: Boolean) {
        val dailyItemId = taskId.toLongOrNull() ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            updateDailyItemStatusUseCase(
                dailyItemId = dailyItemId,
                toStatus = if (isDeferred) DailyItemStatus.TODO else DailyItemStatus.DEFERRED,
                nowEpochMillis = now
            )
            syncDailyQuestsUseCase.ensureQuestMeta(today)
            syncDailyQuestsUseCase.syncProgress(today, now)
            recalculateStreakUseCase(today, now)
        }
    }

    fun deleteSourceTask(sourceTaskId: String?) {
        val targetTaskId = sourceTaskId?.toLongOrNull() ?: return
        viewModelScope.launch {
            deleteManageTaskUseCase(targetTaskId, System.currentTimeMillis())
        }
    }
}

internal fun todayTaskRowToTaskItemUi(row: TodayTaskRow): TaskItemUi =
    TaskItemUi(
        id = row.dailyItemId.toString(),
        sourceTaskId = row.sourceTaskId.toString(),
        title = row.title,
        category = row.category,
        isImportant = row.isImportant,
        isDone = row.status == DailyItemStatus.DONE,
        isDeferred = row.status == DailyItemStatus.DEFERRED
    )
