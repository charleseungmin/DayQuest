package com.dayquest.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.projection.TodayTaskRow
import com.dayquest.app.domain.usecase.today.GenerateTodayItemsUseCase
import com.dayquest.app.domain.usecase.today.ObserveTodayTasksUseCase
import com.dayquest.app.domain.usecase.today.UpdateDailyItemStatusUseCase
import com.dayquest.app.ui.model.TaskItemUi
import com.dayquest.app.ui.model.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val generateTodayItemsUseCase: GenerateTodayItemsUseCase,
    private val observeTodayTasksUseCase: ObserveTodayTasksUseCase,
    private val updateDailyItemStatusUseCase: UpdateDailyItemStatusUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<TodayUiState>(TodayUiState.Loading)
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val today: LocalDate = LocalDate.now()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = TodayUiState.Loading
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            runCatching { generateTodayItemsUseCase(today, now) }
                .onFailure {
                    _uiState.value = TodayUiState.Error("오늘 할 일 생성을 실패했습니다.")
                    return@launch
                }

            observeTodayTasksUseCase(today)
                .catch { _uiState.value = TodayUiState.Error("오늘 할 일을 불러오지 못했습니다.") }
                .collect { rows ->
                    _uiState.value = TodayUiState.Ready(tasks = rows.map { it.toTaskItemUi() })
                }
        }
    }

    fun toggleDone(taskId: String, isDone: Boolean) {
        val dailyItemId = taskId.toLongOrNull() ?: return
        viewModelScope.launch {
            updateDailyItemStatusUseCase(
                dailyItemId = dailyItemId,
                toStatus = if (isDone) DailyItemStatus.TODO else DailyItemStatus.DONE,
                nowEpochMillis = System.currentTimeMillis()
            )
        }
    }

    fun toggleDeferred(taskId: String, isDeferred: Boolean) {
        val dailyItemId = taskId.toLongOrNull() ?: return
        viewModelScope.launch {
            updateDailyItemStatusUseCase(
                dailyItemId = dailyItemId,
                toStatus = if (isDeferred) DailyItemStatus.TODO else DailyItemStatus.DEFERRED,
                nowEpochMillis = System.currentTimeMillis()
            )
        }
    }
}

private fun TodayTaskRow.toTaskItemUi(): TaskItemUi =
    TaskItemUi(
        id = dailyItemId.toString(),
        sourceTaskId = sourceTaskId.toString(),
        title = title,
        category = category,
        isDone = status == DailyItemStatus.DONE,
        isDeferred = status == DailyItemStatus.DEFERRED
    )
