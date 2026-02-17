package com.dayquest.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dayquest.app.ui.component.ErrorCard
import com.dayquest.app.ui.component.LoadingCard
import com.dayquest.app.ui.component.QuestProgressCard
import com.dayquest.app.ui.component.ScreenSectionHeader
import com.dayquest.app.ui.component.TaskListCard
import com.dayquest.app.ui.logic.QuestFeedbackLogic
import com.dayquest.app.ui.model.TodayUiState

@Composable
fun TodayScreen(
    onGoToManage: () -> Unit = {},
    onEditTask: (String) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenSectionHeader(
            title = "Today",
            subtitle = "오늘 생성된 할 일과 완료 현황"
        )

        when (val state = uiState) {
            TodayUiState.Loading -> LoadingCard()
            is TodayUiState.Error -> ErrorCard(message = state.message, onRetry = viewModel::refresh)
            is TodayUiState.Ready -> {
                QuestProgressCard(progress = QuestFeedbackLogic.progress(state.tasks))

                if (state.tasks.isEmpty()) {
                    Card {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("오늘 생성된 할 일이 없습니다. Manage에서 태스크를 추가해 주세요.")
                            Button(onClick = onGoToManage) {
                                Text("Manage로 이동")
                            }
                        }
                    }
                } else {
                    TaskListCard(
                        tasks = state.tasks,
                        onToggleDone = { taskId ->
                            val task = state.tasks.firstOrNull { it.id == taskId } ?: return@TaskListCard
                            viewModel.toggleDone(taskId, task.isDone)
                        },
                        onDefer = { taskId ->
                            val task = state.tasks.firstOrNull { it.id == taskId } ?: return@TaskListCard
                            viewModel.toggleDeferred(taskId, task.isDeferred)
                        },
                        onEdit = { dailyItemId ->
                            val task = state.tasks.firstOrNull { it.id == dailyItemId } ?: return@TaskListCard
                            val sourceTaskId = task.sourceTaskId ?: return@TaskListCard
                            onEditTask(sourceTaskId)
                        },
                        onDelete = {},
                        showEditAction = true,
                        showDeleteAction = false,
                        showDeferAction = true
                    )
                }
            }
        }
    }
}
