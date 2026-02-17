package com.dayquest.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayquest.app.domain.usecase.history.ObserveHistorySummaryUseCase
import com.dayquest.app.ui.model.HistoryDayProgressUi
import com.dayquest.app.ui.model.HistoryUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeHistorySummaryUseCase: ObserveHistorySummaryUseCase
) : ViewModel() {
    private val dateFormatter = DateTimeFormatter.ofPattern("M/d")

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = HistoryUiState.Loading
        viewModelScope.launch {
            observeHistorySummaryUseCase()
                .catch { _uiState.value = HistoryUiState.Error("히스토리 요약을 불러오지 못했습니다.") }
                .collect { summary ->
                    _uiState.value = HistoryUiState.Ready(
                        todayDoneCount = summary.todayDoneCount,
                        todayDeferredCount = summary.todayDeferredCount,
                        todayTotalCount = summary.todayTotalCount,
                        weeklyDoneCount = summary.weeklyDoneCount,
                        weeklyDeferredCount = summary.weeklyDeferredCount,
                        weeklyTotalCount = summary.weeklyTotalCount,
                        dailyProgress = summary.dailyProgress.map {
                            HistoryDayProgressUi(
                                dateLabel = runCatching { LocalDate.parse(it.dateKey).format(dateFormatter) }
                                    .getOrElse { _ -> it.dateKey },
                                doneCount = it.doneCount,
                                totalCount = it.totalCount
                            )
                        }
                    )
                }
        }
    }
}
