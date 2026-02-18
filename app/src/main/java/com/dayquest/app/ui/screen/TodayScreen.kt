package com.dayquest.app.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dayquest.app.ui.component.ErrorCard
import com.dayquest.app.ui.component.LoadingCard
import com.dayquest.app.ui.logic.QuestFeedbackLogic
import com.dayquest.app.ui.logic.TodayTaskFilter
import com.dayquest.app.ui.logic.TodayTaskSort
import com.dayquest.app.ui.logic.buildTodayFilterCounts
import com.dayquest.app.ui.logic.filterTodayTasks
import com.dayquest.app.ui.logic.sortTodayTasks
import com.dayquest.app.ui.component.QuestProgressCard
import com.dayquest.app.ui.component.ScreenSectionHeader
import com.dayquest.app.ui.component.StreakStatusCard
import com.dayquest.app.ui.component.TaskListCard
import com.dayquest.app.ui.model.TodayUiState

@Composable
fun TodayScreen(
    onGoToManage: () -> Unit = {},
    onEditTask: (String) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by rememberSaveable { mutableStateOf(TodayTaskFilter.All) }
    var selectedSort by rememberSaveable { mutableStateOf(TodayTaskSort.Recommended) }

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
                StreakStatusCard(streak = state.streak)

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
                    val filterCounts = buildTodayFilterCounts(state.tasks)
                    val filteredTasks = filterTodayTasks(state.tasks, selectedFilter)
                    val sortedTasks = sortTodayTasks(filteredTasks, selectedSort)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        TodayTaskFilter.entries.forEach { filter ->
                            Button(
                                onClick = { selectedFilter = filter },
                                enabled = selectedFilter != filter
                            ) {
                                Text("${filter.label}(${filterCounts[filter] ?: 0})")
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        TodayTaskSort.entries.forEach { sort ->
                            Button(
                                onClick = { selectedSort = sort },
                                enabled = selectedSort != sort
                            ) {
                                Text(sort.label)
                            }
                        }
                    }

                    if (sortedTasks.isEmpty()) {
                        Card {
                            Text("${selectedFilter.label} 상태의 항목이 없습니다.")
                        }
                    } else {
                        TaskListCard(
                            tasks = sortedTasks,
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
                            onDelete = { dailyItemId ->
                                val task = state.tasks.firstOrNull { it.id == dailyItemId } ?: return@TaskListCard
                                viewModel.deleteSourceTask(task.sourceTaskId)
                            },
                            showEditAction = true,
                            showDeleteAction = true,
                            showDeferAction = true
                        )
                    }
                }
            }
        }
    }
}
