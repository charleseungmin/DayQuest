package com.dayquest.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dayquest.app.ui.component.ErrorCard
import com.dayquest.app.ui.component.LoadingCard
import com.dayquest.app.ui.component.ScreenSectionHeader
import com.dayquest.app.ui.model.HistoryPeriodUi
import com.dayquest.app.ui.model.HistoryUiState

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenSectionHeader(
            title = "History",
            subtitle = "일간/주간 진행 요약"
        )

        when (val state = uiState) {
            HistoryUiState.Loading -> LoadingCard()
            is HistoryUiState.Error -> ErrorCard(
                message = state.message,
                onRetry = viewModel::refresh
            )

            is HistoryUiState.Ready -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("오늘", style = MaterialTheme.typography.titleMedium)
                        Text("완료 ${state.todayDoneCount}개 · 미룸 ${state.todayDeferredCount}개 / 총 ${state.todayTotalCount}개")
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("기간 요약", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HistoryPeriodUi.entries.forEach { period ->
                                Button(
                                    onClick = { viewModel.selectPeriod(period) },
                                    enabled = state.selectedPeriod != period
                                ) {
                                    Text(period.label)
                                }
                            }
                        }

                        val rate = if (state.periodTotalCount == 0) 0 else (state.periodDoneCount * 100 / state.periodTotalCount)
                        Text("${state.selectedPeriod.label} 완료 ${state.periodDoneCount}개 · 미룸 ${state.periodDeferredCount}개 / 총 ${state.periodTotalCount}개")
                        Text("완료율 ${rate}%")

                        Button(onClick = viewModel::toggleShowOnlyActiveDays) {
                            Text(if (state.showOnlyActiveDays) "빈 날짜 포함해서 보기" else "기록 있는 날짜만 보기")
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("최근 진행", style = MaterialTheme.typography.titleMedium)
                        if (state.dailyProgress.isEmpty()) {
                            val emptyMessage = if (state.showOnlyActiveDays) {
                                "선택한 기간에 기록된 날짜가 없습니다."
                            } else {
                                "선택한 기간의 기록이 아직 없습니다."
                            }
                            Text(emptyMessage)
                        } else {
                            state.dailyProgress.forEach { day ->
                                Text("${day.dateLabel}(${day.weekdayLabel}) · 완료 ${day.doneCount} · 미룸 ${day.deferredCount} / 총 ${day.totalCount} · 완료율 ${day.completionRate}%")
                            }
                        }
                    }
                }
            }
        }
    }
}
