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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.UUID

data class TaskItemUi(
    val id: String,
    val title: String,
    val category: String
)

data class TaskFormUi(
    val editingTaskId: String? = null,
    val title: String = "",
    val category: String = "일반"
)

sealed interface TaskManageUiState {
    data object Loading : TaskManageUiState
    data class Ready(
        val tasks: List<TaskItemUi> = emptyList(),
        val form: TaskFormUi = TaskFormUi()
    ) : TaskManageUiState

    data class Error(val message: String) : TaskManageUiState
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
                    TaskManageScreen()
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
private fun TaskManageScreen() {
    var uiState by remember { mutableStateOf<TaskManageUiState>(TaskManageUiState.Loading) }

    LaunchedEffect(Unit) {
        delay(450)
        if (uiState is TaskManageUiState.Loading) {
            uiState = TaskManageUiState.Ready()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("TaskManageScreen", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { uiState = TaskManageUiState.Loading }) { Text("로딩") }
            Button(onClick = { uiState = TaskManageUiState.Ready() }) { Text("빈상태") }
            Button(onClick = { uiState = TaskManageUiState.Error("네트워크 오류가 발생했습니다.") }) { Text("오류") }
        }

        when (val state = uiState) {
            TaskManageUiState.Loading -> LoadingCard()
            is TaskManageUiState.Error -> ErrorCard(
                message = state.message,
                onRetry = { uiState = TaskManageUiState.Ready() }
            )

            is TaskManageUiState.Ready -> {
                TaskFormCard(
                    form = state.form,
                    onTitleChange = { uiState = state.copy(form = state.form.copy(title = it)) },
                    onCategoryChange = { uiState = state.copy(form = state.form.copy(category = it)) },
                    onSubmit = { uiState = TaskManageLogic.upsert(state) }
                )
                TaskListCard(
                    tasks = state.tasks,
                    onEdit = { uiState = TaskManageLogic.edit(state, it) },
                    onDelete = { uiState = TaskManageLogic.delete(state, it) }
                )
            }
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
                            Text(task.category, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = { onEdit(task.id) }) { Text("수정") }
                            Button(onClick = { onDelete(task.id) }) { Text("삭제") }
                        }
                    }
                }
            }
        }
    }
}
