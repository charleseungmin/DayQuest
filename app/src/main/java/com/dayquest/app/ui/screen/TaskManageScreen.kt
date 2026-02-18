package com.dayquest.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dayquest.app.ui.component.ErrorCard
import com.dayquest.app.ui.component.LoadingCard
import com.dayquest.app.ui.component.QuestCompletionBanner
import com.dayquest.app.ui.component.QuestProgressCard
import com.dayquest.app.ui.component.ScreenSectionHeader
import com.dayquest.app.ui.component.TaskFormCard
import com.dayquest.app.ui.component.TaskListCard
import com.dayquest.app.ui.logic.QuestFeedbackLogic
import com.dayquest.app.ui.model.TaskManageUiState
import java.time.DayOfWeek
import kotlinx.coroutines.launch

@Composable
fun TaskManageScreen(
    initialEditTaskId: String? = null,
    viewModel: TaskManageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showQuestBanner by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(initialEditTaskId) {
        viewModel.requestEdit(initialEditTaskId)
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenSectionHeader(
                title = "Manage",
                subtitle = "Task 등록 · 진행 · 완료 흐름"
            )

            when (val state = uiState) {
                TaskManageUiState.Loading -> LoadingCard()
                is TaskManageUiState.Error -> ErrorCard(
                    message = state.message,
                    onRetry = viewModel::retry
                )

                is TaskManageUiState.Ready -> {
                    LaunchedEffect(state.noticeMessage) {
                        if (!state.noticeMessage.isNullOrBlank()) {
                            snackbarHostState.showSnackbar(state.noticeMessage)
                            viewModel.consumeNotice()
                        }
                    }

                    val progress = QuestFeedbackLogic.progress(state.tasks)
                    if (showQuestBanner) {
                        QuestCompletionBanner(progress)
                    }
                    QuestProgressCard(progress)
                    TaskFormCard(
                        form = state.form,
                        onTitleChange = viewModel::updateTitle,
                        onCategoryChange = viewModel::updateCategory,
                        onPriorityChange = viewModel::updatePriority,
                        onImportantChange = viewModel::updateImportant,
                        onRepeatTypeChange = viewModel::updateRepeatType,
                        onToggleRepeatDay = { day: DayOfWeek -> viewModel.toggleRepeatDay(day) },
                        onSubmit = viewModel::upsert
                    )
                    TaskListCard(
                        tasks = state.tasks,
                        onToggleDone = { taskId ->
                            val before = state.tasks
                            viewModel.toggleDone(taskId)
                            val after = state.tasks.map { if (it.id == taskId) it.copy(isDone = !it.isDone) else it }
                            val celebrate = QuestFeedbackLogic.shouldCelebrate(before, after)
                            if (celebrate) {
                                showQuestBanner = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("퀘스트 달성! 오늘 할 일을 전부 완료했어요 ⚒️")
                                }
                            }
                        },
                        onEdit = viewModel::edit,
                        onDelete = viewModel::delete
                    )
                }
            }
        }
    }
}
