package com.dayquest.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dayquest.app.core.model.RepeatType
import com.dayquest.app.core.model.TaskPriority
import java.time.DayOfWeek
import com.dayquest.app.ui.model.QuestProgressUi
import com.dayquest.app.ui.model.StreakUi
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
fun StreakStatusCard(streak: StreakUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("연속 달성", style = MaterialTheme.typography.titleMedium)
            Text("현재 ${streak.currentStreak}일 · 최고 ${streak.bestStreak}일")
        }
    }
}

@Composable
fun CharacterGrowthCard(doneCount: Int, totalCount: Int, streak: StreakUi) {
    val totalExp = doneCount * 35 + streak.currentStreak * 20 + streak.bestStreak * 10
    val level = totalExp / 100 + 1
    val expInLevel = totalExp % 100
    val title = when {
        level >= 10 -> "전설 모험가"
        level >= 7 -> "숙련 모험가"
        level >= 4 -> "성장 모험가"
        else -> "초보 모험가"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CharacterAvatar(
                level = level,
                title = title,
                focus = doneCount,
                vitality = streak.currentStreak,
                insight = totalCount,
                balance = streak.bestStreak,
                modifier = Modifier.size(96.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("캐릭터 성장", style = MaterialTheme.typography.titleMedium)
                Text("Lv.$level $title", style = MaterialTheme.typography.bodyLarge)
                Text("EXP $expInLevel/100 · 완료 $doneCount/$totalCount · 연속 ${streak.currentStreak}일")
            }
        }
    }
}

@Composable
private fun CharacterAvatar(
    level: Int,
    title: String,
    focus: Int,
    vitality: Int,
    insight: Int,
    balance: Int,
    modifier: Modifier = Modifier
) {
    val stage = when {
        level >= 10 -> 4
        level >= 7 -> 3
        level >= 4 -> 2
        else -> 1
    }
    val primary = when {
        level >= 10 -> Color(0xFF6A51D8)
        level >= 7 -> Color(0xFF1E8A6A)
        level >= 4 -> Color(0xFF2667C9)
        else -> Color(0xFF64748B)
    }
    val surface = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outlineVariant
    val ink = MaterialTheme.colorScheme.onSurface
    val cloak = MaterialTheme.colorScheme.secondary
    val spark = MaterialTheme.colorScheme.tertiary
    val skin = Color(0xFFFFD6B8)
    val gold = Color(0xFFFFC857)
    val statColors = listOf(
        Color(0xFF4F8CC9),
        Color(0xFFE25555),
        Color(0xFF7C65D1),
        Color(0xFF4D9F70)
    )

    Canvas(
        modifier = modifier.semantics {
            contentDescription = "레벨 $level $title 캐릭터 그래픽"
        }
    ) {
        val unit = minOf(size.width, size.height)
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = unit * 0.46f
        val floorY = center.y + unit * 0.32f

        drawCircle(surface.copy(alpha = 0.88f), baseRadius, center)
        drawCircle(primary.copy(alpha = 0.14f), baseRadius * 0.86f, center)
        drawCircle(
            outline.copy(alpha = 0.55f),
            baseRadius,
            center,
            style = Stroke(width = unit * 0.018f)
        )
        if (stage >= 2) {
            drawCircle(
                primary.copy(alpha = 0.32f),
                baseRadius * 0.98f,
                center,
                style = Stroke(width = unit * 0.035f)
            )
        }
        if (stage >= 4) {
            drawCircle(
                gold.copy(alpha = 0.42f),
                baseRadius * 1.08f,
                center,
                style = Stroke(width = unit * 0.022f)
            )
        }

        if (stage >= 2) {
            val cloakPath = Path().apply {
                moveTo(center.x, center.y - unit * 0.04f)
                cubicTo(
                    center.x - unit * 0.36f,
                    center.y + unit * 0.02f,
                    center.x - unit * 0.35f,
                    floorY,
                    center.x - unit * 0.18f,
                    floorY
                )
                lineTo(center.x + unit * 0.18f, floorY)
                cubicTo(
                    center.x + unit * 0.35f,
                    floorY,
                    center.x + unit * 0.36f,
                    center.y + unit * 0.02f,
                    center.x,
                    center.y - unit * 0.04f
                )
                close()
            }
            drawPath(cloakPath, cloak.copy(alpha = 0.72f))
        }

        drawRect(
            color = skin,
            topLeft = Offset(center.x - unit * 0.055f, center.y - unit * 0.02f),
            size = Size(unit * 0.11f, unit * 0.12f)
        )
        drawOval(
            color = primary.copy(alpha = 0.95f),
            topLeft = Offset(center.x - unit * 0.18f, center.y + unit * 0.02f),
            size = Size(unit * 0.36f, unit * 0.34f)
        )
        drawOval(
            color = primary.copy(alpha = 0.32f),
            topLeft = Offset(center.x - unit * 0.13f, center.y + unit * 0.10f),
            size = Size(unit * 0.26f, unit * 0.16f)
        )

        val faceCenter = Offset(center.x, center.y - unit * 0.16f)
        drawCircle(skin, unit * 0.145f, faceCenter)
        drawOval(
            color = ink.copy(alpha = 0.22f),
            topLeft = Offset(center.x - unit * 0.14f, center.y - unit * 0.30f),
            size = Size(unit * 0.28f, unit * 0.13f)
        )
        drawCircle(ink, unit * 0.014f, Offset(center.x - unit * 0.052f, center.y - unit * 0.15f))
        drawCircle(ink, unit * 0.014f, Offset(center.x + unit * 0.052f, center.y - unit * 0.15f))
        drawLine(
            color = ink.copy(alpha = 0.45f),
            start = Offset(center.x - unit * 0.035f, center.y - unit * 0.095f),
            end = Offset(center.x + unit * 0.035f, center.y - unit * 0.095f),
            strokeWidth = unit * 0.008f
        )

        if (stage >= 3) {
            val staffBase = Offset(center.x + unit * 0.27f, center.y + unit * 0.26f)
            val staffTop = Offset(center.x + unit * 0.33f, center.y - unit * 0.29f)
            drawLine(
                color = spark.copy(alpha = 0.9f),
                start = staffBase,
                end = staffTop,
                strokeWidth = unit * 0.026f
            )
            drawCircle(gold, unit * 0.045f, staffTop)
            drawCircle(spark.copy(alpha = 0.38f), unit * 0.078f, staffTop)
        }

        if (stage >= 4) {
            val crown = Path().apply {
                moveTo(center.x - unit * 0.13f, center.y - unit * 0.31f)
                lineTo(center.x - unit * 0.07f, center.y - unit * 0.40f)
                lineTo(center.x, center.y - unit * 0.31f)
                lineTo(center.x + unit * 0.07f, center.y - unit * 0.40f)
                lineTo(center.x + unit * 0.13f, center.y - unit * 0.31f)
                close()
            }
            drawPath(crown, gold)
            drawRect(
                color = gold.copy(alpha = 0.86f),
                topLeft = Offset(center.x - unit * 0.12f, center.y - unit * 0.31f),
                size = Size(unit * 0.24f, unit * 0.035f)
            )
        }

        val stats = listOf(focus, vitality, insight, balance)
        stats.forEachIndexed { index, value ->
            if (value > 0) {
                val scale = value.coerceAtMost(12) / 12f
                drawCircle(
                    color = statColors[index].copy(alpha = 0.35f + scale * 0.45f),
                    radius = unit * (0.018f + scale * 0.012f),
                    center = Offset(center.x - unit * 0.18f + index * unit * 0.12f, center.y + unit * 0.36f)
                )
            }
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
    onPriorityChange: (TaskPriority) -> Unit,
    onImportantChange: (Boolean) -> Unit,
    onRepeatTypeChange: (RepeatType) -> Unit,
    onToggleRepeatDay: (DayOfWeek) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.entries.forEach { priority ->
                    Button(
                        onClick = { onPriorityChange(priority) },
                        enabled = form.priority != priority
                    ) {
                        Text(
                            when (priority) {
                                TaskPriority.HIGH -> "높음"
                                TaskPriority.MEDIUM -> "보통"
                                TaskPriority.LOW -> "낮음"
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("중요 작업")
                Switch(
                    checked = form.isImportant,
                    onCheckedChange = onImportantChange
                )
            }

            Text("반복", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RepeatType.entries.forEach { repeatType ->
                    Button(
                        onClick = { onRepeatTypeChange(repeatType) },
                        enabled = form.repeatType != repeatType
                    ) {
                        Text(
                            when (repeatType) {
                                RepeatType.DAILY -> "매일"
                                RepeatType.WEEKLY -> "매주"
                                RepeatType.MONTHLY -> "매월"
                                RepeatType.CUSTOM -> "커스텀"
                            }
                        )
                    }
                }
            }

            if (form.repeatType == RepeatType.WEEKLY || form.repeatType == RepeatType.CUSTOM) {
                val mask = form.repeatDaysMask ?: 0
                val days = listOf(
                    DayOfWeek.MONDAY to "월",
                    DayOfWeek.TUESDAY to "화",
                    DayOfWeek.WEDNESDAY to "수",
                    DayOfWeek.THURSDAY to "목",
                    DayOfWeek.FRIDAY to "금",
                    DayOfWeek.SATURDAY to "토",
                    DayOfWeek.SUNDAY to "일"
                )
                Text("반복 요일", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    days.forEach { (day, label) ->
                        val selected = (mask and (1 shl (day.value - 1))) != 0
                        Button(onClick = { onToggleRepeatDay(day) }) {
                            Text(if (selected) "[$label]" else label)
                        }
                    }
                }
            }

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
                            val priorityLabel = when (task.priority) {
                                TaskPriority.HIGH -> "높음"
                                TaskPriority.MEDIUM -> "보통"
                                TaskPriority.LOW -> "낮음"
                            }
                            val importantLabel = if (task.isImportant) " · 중요" else ""
                            val repeatLabel = when (task.repeatType) {
                                RepeatType.DAILY -> "매일"
                                RepeatType.WEEKLY -> "매주"
                                RepeatType.MONTHLY -> "매월"
                                RepeatType.CUSTOM -> "커스텀"
                            }
                            Text("${task.category} · 우선순위 $priorityLabel$importantLabel · 반복 $repeatLabel · $statusLabel", style = MaterialTheme.typography.bodySmall)
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
