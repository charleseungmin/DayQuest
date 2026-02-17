package com.dayquest.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dayquest.app.ui.model.QuestProgressUi
import com.dayquest.app.ui.model.TaskFormUi
import com.dayquest.app.ui.model.TaskItemUi

@Composable
fun ScreenSectionHeader(title: String, subtitle: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TaskStateDebugCard(
    onLoading: () -> Unit,
    onEmpty: () -> Unit,
    onError: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("상태 전환 (와이어프레임 점검)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLoading) { Text("로딩") }
                Button(onClick = onEmpty) { Text("빈상태") }
                Button(onClick = onError) { Text("오류") }
            }
        }
    }
}

@Composable
fun QuestProgressCard(progress: QuestProgressUi) {
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
fun QuestCompletionBanner(progress: QuestProgressUi) {
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
fun LoadingCard() {
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
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("오류", style = MaterialTheme.typography.titleMedium)
            Text(message)
            Button(onClick = onRetry) { Text("다시 시도") }
        }
    }
}

@Composable
fun TaskFormCard(
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
fun TaskListCard(
    tasks: List<TaskItemUi>,
    onToggleDone: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDefer: (String) -> Unit = {},
    showEditAction: Boolean = true,
    showDeleteAction: Boolean = true,
    showDeferAction: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        if (tasks.isEmpty()) {
            Text(
                text = "등록된 태스크가 없습니다. 위 폼에서 새 태스크를 추가하세요.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.titleSmall)
                            val statusLabel = when {
                                task.isDone -> "완료"
                                task.isDeferred -> "미룸"
                                else -> "진행중"
                            }
                            Text("${task.category} · $statusLabel", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = { onToggleDone(task.id) }) {
                                Text(if (task.isDone) "되돌리기" else "완료")
                            }
                            if (showDeferAction) {
                                Button(onClick = { onDefer(task.id) }) {
                                    Text(if (task.isDeferred) "복원" else "미루기")
                                }
                            }
                            if (showEditAction) {
                                Button(onClick = { onEdit(task.id) }) { Text("수정") }
                            }
                            if (showDeleteAction) {
                                Button(onClick = { onDelete(task.id) }) { Text("삭제") }
                            }
                        }
                    }
                }
            }
        }
    }
}
