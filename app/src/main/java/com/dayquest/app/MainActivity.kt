package com.dayquest.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class TaskItemUi(
    val id: String,
    val title: String,
    val category: String,
    val isDone: Boolean = false
)

data class TaskFormUi(
    val editingTaskId: String? = null,
    val title: String = "",
    val category: String = "일반"
)

data class QuestProgressUi(
    val doneCount: Int,
    val totalCount: Int
)

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val resetDone: Boolean = false
)

object SettingsLogic {
    fun toggleNotifications(state: SettingsUiState): SettingsUiState =
        state.copy(notificationsEnabled = !state.notificationsEnabled)

    fun resetData(state: SettingsUiState): SettingsUiState =
        state.copy(resetDone = true)
}

sealed interface TaskManageUiState {
    data object Loading : TaskManageUiState
    data class Ready(
        val tasks: List<TaskItemUi> = emptyList(),
        val form: TaskFormUi = TaskFormUi()
    ) : TaskManageUiState

    data class Error(val message: String) : TaskManageUiState
}

object QuestFeedbackLogic {
    fun progress(tasks: List<TaskItemUi>): QuestProgressUi {
        val done = tasks.count { it.isDone }
        return QuestProgressUi(doneCount = done, totalCount = tasks.size)
    }

    fun shouldCelebrate(before: List<TaskItemUi>, after: List<TaskItemUi>): Boolean {
        val prev = progress(before)
        val next = progress(after)
        return prev.totalCount > 0 && prev.doneCount < prev.totalCount && next.totalCount > 0 && next.doneCount == next.totalCount
    }
}

object TaskManageLogic {
    fun upsert(state: TaskManageUiState.Ready): TaskManageUiState.Ready {
        val title = state.form.title.trim()
        if (title.isEmpty()) return state

        val updated = if (state.form.editingTaskId == null) {
            state.tasks + TaskItemUi(
                id = UUID.randomUUID().toString(),
                title = title,
                category = state.form.category.trim().ifEmpty { "일반" }
            )
        } else {
            state.tasks.map {
                if (it.id == state.form.editingTaskId) {
                    it.copy(title = title, category = state.form.category.trim().ifEmpty { "일반" })
                } else {
                    it
                }
            }
        }
        return state.copy(tasks = updated, form = TaskFormUi())
    }

    fun edit(state: TaskManageUiState.Ready, taskId: String): TaskManageUiState.Ready {
        val task = state.tasks.firstOrNull { it.id == taskId } ?: return state
        return state.copy(form = TaskFormUi(editingTaskId = task.id, title = task.title, category = task.category))
    }

    fun toggleDone(state: TaskManageUiState.Ready, taskId: String): TaskManageUiState.Ready {
        return state.copy(tasks = state.tasks.map { if (it.id == taskId) it.copy(isDone = !it.isDone) else it })
    }

    fun delete(state: TaskManageUiState.Ready, taskId: String): TaskManageUiState.Ready {
        return state.copy(
            tasks = state.tasks.filterNot { it.id == taskId },
            form = if (state.form.editingTaskId == taskId) TaskFormUi() else state.form
        )
    }
}

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DayQuestHome()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun DayQuestHome() {
    var selectedTab by remember { mutableStateOf("manage") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { selectedTab = "manage" }) { Text("Task 관리") }
            Button(onClick = { selectedTab = "history" }) { Text("기록 조회") }
            Button(onClick = { selectedTab = "settings" }) { Text("설정") }
        }

        when (selectedTab) {
            "manage" -> TaskManageScreen()
            "history" -> HistoryScreen()
            else -> SettingsScreen()
        }
    }
}

@Composable
private fun HistoryScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("기록 조회", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("오늘", style = MaterialTheme.typography.titleMedium)
                Text("완료 0개 / 총 0개")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("주간", style = MaterialTheme.typography.titleMedium)
                Text("이번 주 누적 완료 0개")
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    var state by remember { mutableStateOf(SettingsUiState()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("설정", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("알림 사용", style = MaterialTheme.typography.titleMedium)
                    Text(if (state.notificationsEnabled) "07:00/21:00 알림 활성화" else "알림 비활성화")
                }
                Switch(
                    checked = state.notificationsEnabled,
                    onCheckedChange = { state = SettingsLogic.toggleNotifications(state) }
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("데이터 초기화", style = MaterialTheme.typography.titleMedium)
                Text("학습용 샘플에서는 즉시 로컬 상태를 리셋했다고 가정합니다.")
                Button(onClick = { state = SettingsLogic.resetData(state) }) {
                    Text("초기화 실행")
                }
                if (state.resetDone) {
                    Text("초기화가 완료되었습니다.")
                }
            }
        }
    }
}

@Composable
private fun TaskManageScreen() {
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
            Text("TaskManageScreen", style = MaterialTheme.typography.headlineSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    showQuestBanner = false
                    uiState = TaskManageUiState.Loading
                }) { Text("로딩") }
                Button(onClick = {
                    showQuestBanner = false
                    uiState = TaskManageUiState.Ready()
                }) { Text("빈상태") }
                Button(onClick = {
                    showQuestBanner = false
                    uiState = TaskManageUiState.Error("네트워크 오류가 발생했습니다.")
                }) { Text("오류") }
            }

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

@Composable
private fun QuestProgressCard(progress: QuestProgressUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("오늘의 퀘스트", style = MaterialTheme.typography.titleMedium)
            Text("진행도 ${progress.doneCount} / ${progress.totalCount}")
        }
    }
}

@Composable
private fun QuestCompletionBanner(progress: QuestProgressUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🎉 퀘스트 달성!", style = MaterialTheme.typography.titleMedium)
            Text("${progress.doneCount}개 할 일을 모두 완료했습니다.")
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()
            Text("태스크 목록을 불러오는 중...")
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("오류", style = MaterialTheme.typography.titleMedium)
            Text(message)
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}

@Composable
private fun TaskFormCard(
    form: TaskFormUi,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (form.editingTaskId == null) "Task 추가" else "Task 수정",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.title,
                onValueChange = onTitleChange,
                label = { Text("제목") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.category,
                onValueChange = onCategoryChange,
                label = { Text("카테고리") },
                singleLine = true
            )
            Button(onClick = onSubmit) {
                Text(if (form.editingTaskId == null) "추가" else "저장")
            }
        }
    }
}

@Composable
private fun TaskListCard(
    tasks: List<TaskItemUi>,
    onToggleDone: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        if (tasks.isEmpty()) {
            Text(
                text = "등록된 태스크가 없습니다. 위 폼에서 새 태스크를 추가하세요.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.titleSmall)
                            Text("${task.category} · ${if (task.isDone) "완료" else "진행중"}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = { onToggleDone(task.id) }) { Text(if (task.isDone) "되돌리기" else "완료") }
                            Button(onClick = { onEdit(task.id) }) { Text("수정") }
                            Button(onClick = { onDelete(task.id) }) { Text("삭제") }
                        }
                    }
                }
            }
        }
    }
}
