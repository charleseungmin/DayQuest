package com.dayquest.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayquest.app.domain.usecase.history.ObserveHistorySummaryUseCase
import com.dayquest.app.ui.logic.HistoryProgressSeed
import com.dayquest.app.ui.logic.buildHistoryTimeline
import com.dayquest.app.ui.logic.filterHistoryTimeline
import com.dayquest.app.ui.model.HistoryPeriodUi
import com.dayquest.app.ui.model.HistoryUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
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
    private val weekdayFormatter = DateTimeFormatter.ofPattern("E", Locale.KOREAN)

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var selectedPeriod: HistoryPeriodUi = HistoryPeriodUi.Weekly
    private var showOnlyActiveDays: Boolean = false
    private var latestToday: LocalDate = LocalDate.now()
    private var latestProgress: List<HistoryProgressSeed> = emptyList()
    private var latestTodayDone = 0
    private var latestTodayDeferred = 0
    private var latestTodayTotal = 0
    private var observeJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        observeJob?.cancel()
        _uiState.value = HistoryUiState.Loading
        observeJob = viewModelScope.launch {
            latestToday = LocalDate.now()
            observeHistorySummaryUseCase(today = latestToday)
                .catch { _uiState.value = HistoryUiState.Error("히스토리 요약을 불러오지 못했습니다.") }
                .collect { summary ->
                    latestTodayDone = summary.todayDoneCount
                    latestTodayDeferred = summary.todayDeferredCount
                    latestTodayTotal = summary.todayTotalCount
                    latestProgress = summary.recentDailyProgress.map {
                        HistoryProgressSeed(
                            dateKey = it.dateKey,
                            doneCount = it.doneCount,
                            deferredCount = it.deferredCount,
                            totalCount = it.totalCount
                        )
                    }
                    publishReadyState()
                }
        }
    }

    fun selectPeriod(period: HistoryPeriodUi) {
        if (selectedPeriod == period) return
        selectedPeriod = period
        if (_uiState.value is HistoryUiState.Ready) {
            publishReadyState()
        }
    }

    fun toggleShowOnlyActiveDays() {
        showOnlyActiveDays = !showOnlyActiveDays
        if (_uiState.value is HistoryUiState.Ready) {
            publishReadyState()
        }
    }

    private fun publishReadyState() {
        val dailyTimeline = buildHistoryTimeline(
            today = latestToday,
            periodDays = selectedPeriod.days,
            progress = latestProgress,
            dateFormatter = dateFormatter,
            weekdayFormatter = weekdayFormatter
        )
        val visibleTimeline = filterHistoryTimeline(dailyTimeline, showOnlyActiveDays)

        val periodDone = dailyTimeline.sumOf { it.doneCount }
        val periodDeferred = dailyTimeline.sumOf { it.deferredCount }
        val periodTotal = dailyTimeline.sumOf { it.totalCount }

        _uiState.value = HistoryUiState.Ready(
            todayDoneCount = latestTodayDone,
            todayDeferredCount = latestTodayDeferred,
            todayTotalCount = latestTodayTotal,
            selectedPeriod = selectedPeriod,
            periodDoneCount = periodDone,
            periodDeferredCount = periodDeferred,
            periodTotalCount = periodTotal,
            showOnlyActiveDays = showOnlyActiveDays,
            dailyProgress = visibleTimeline
        )
    }
}
