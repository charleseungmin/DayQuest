package com.dayquest.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("주간", style = MaterialTheme.typography.titleMedium)
                        Text("이번 주 누적 완료 ${state.weeklyDoneCount}개 · 미룸 ${state.weeklyDeferredCount}개 / 총 ${state.weeklyTotalCount}개")
                        val rate = if (state.weeklyTotalCount == 0) 0 else (state.weeklyDoneCount * 100 / state.weeklyTotalCount)
                        Text("주간 완료율 ${rate}%")
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("최근 진행", style = MaterialTheme.typography.titleMedium)
                        if (state.dailyProgress.isEmpty()) {
                            Text("이번 주 기록이 아직 없습니다.")
                        } else {
                            state.dailyProgress.forEach { day ->
                                Text("${day.dateLabel} · 완료 ${day.doneCount}/${day.totalCount}")
                            }
                        }
                    }
                }
            }
        }
    }
}
