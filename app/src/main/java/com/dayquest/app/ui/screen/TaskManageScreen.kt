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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dayquest.app.ui.component.ErrorCard
import com.dayquest.app.ui.component.LoadingCard
import com.dayquest.app.ui.component.QuestCompletionBanner
import com.dayquest.app.ui.component.QuestProgressCard
import com.dayquest.app.ui.component.ScreenSectionHeader
import com.dayquest.app.ui.component.TaskFormCard
import com.dayquest.app.ui.component.TaskListCard
import com.dayquest.app.ui.component.TaskStateDebugCard
import com.dayquest.app.ui.logic.QuestFeedbackLogic
import com.dayquest.app.ui.logic.TaskManageLogic
import com.dayquest.app.ui.model.TaskManageUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TaskManageScreen() {
    var uiState by remember { mutableStateOf<TaskManageUiState>(TaskManageUiState.Loading) }
    var showQuestBanner by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(450)
        if (uiState is TaskManageUiState.Loading) {
            uiState = TaskManageUiState.Ready()
        }
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

            TaskStateDebugCard(
                onLoading = {
                    showQuestBanner = false
                    uiState = TaskManageUiState.Loading
                },
                onEmpty = {
                    showQuestBanner = false
                    uiState = TaskManageUiState.Ready()
                },
                onError = {
                    showQuestBanner = false
                    uiState = TaskManageUiState.Error("네트워크 오류가 발생했습니다.")
                }
            )

            when (val state = uiState) {
                TaskManageUiState.Loading -> LoadingCard()
                is TaskManageUiState.Error -> ErrorCard(
                    message = state.message,
                    onRetry = {
                        showQuestBanner = false
                        uiState = TaskManageUiState.Ready()
                    }
                )

                is TaskManageUiState.Ready -> {
                    val progress = QuestFeedbackLogic.progress(state.tasks)
                    if (showQuestBanner) {
                        QuestCompletionBanner(progress)
                    }
                    QuestProgressCard(progress)
                    TaskFormCard(
                        form = state.form,
                        onTitleChange = { uiState = state.copy(form = state.form.copy(title = it)) },
                        onCategoryChange = { uiState = state.copy(form = state.form.copy(category = it)) },
                        onSubmit = { uiState = TaskManageLogic.upsert(state) }
                    )
                    TaskListCard(
                        tasks = state.tasks,
                        onToggleDone = { taskId ->
                            val updated = TaskManageLogic.toggleDone(state, taskId)
                            val celebrate = QuestFeedbackLogic.shouldCelebrate(state.tasks, updated.tasks)
                            uiState = updated
                            if (celebrate) {
                                showQuestBanner = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("퀘스트 달성! 오늘 할 일을 전부 완료했어요 ⚒️")
                                }
                            }
                        },
                        onEdit = { uiState = TaskManageLogic.edit(state, it) },
                        onDelete = { uiState = TaskManageLogic.delete(state, it) }
                    )
                }
            }
        }
    }
}
