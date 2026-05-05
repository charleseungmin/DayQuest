@file:OptIn(ExperimentalMaterial3Api::class)

package com.dayquest.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayquest.app.ui.theme.DayQuestTheme
import com.dayquest.app.ui.theme.DayQuestTaskAccentCompleted
import com.dayquest.app.ui.theme.DayQuestTaskAccentMain
import com.dayquest.app.ui.theme.DayQuestTaskAccentNormal
import com.dayquest.app.ui.theme.DayQuestTaskAccentOverdue
import com.dayquest.app.ui.theme.DayQuestTaskAccentRare
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class RootTab { TODAY, MANAGE, HISTORY, SETTINGS }
enum class ManageFilter { ALL, ACTIVE, COMPLETED }
enum class HistoryRange { DAILY, WEEKLY, MONTHLY }
enum class QuestTier { MAIN, RARE, NORMAL }

data class TodayTaskUi(
    val id: Long,
    val title: String,
    val memo: String?,
    val categoryLabel: String?,
    val timeLabel: String?,
    val repeatLabel: String?,
    val startDate: String?,
    val endDate: String?,
    val isCompleted: Boolean,
    val isOverdue: Boolean,
    val isSkippedToday: Boolean,
    val tier: QuestTier,
    val reminderEnabled: Boolean,
    val dailyDate: String? = null,
    val dailySource: String? = null,
)

data class HistoryDayUi(
    val dateLabel: String,
    val completedCount: Int,
    val totalCount: Int,
    val earnedXp: Int,
    val note: String,
    val categorySummary: String? = null,
)

data class CharacterGrowthUi(
    val level: Int = 1,
    val title: String = "초보 모험가",
    val expInLevel: Int = 0,
    val nextLevelExp: Int = 100,
    val totalExp: Int = 0,
    val trainingPoints: Int = 0,
    val focus: Int = 0,
    val vitality: Int = 0,
    val insight: Int = 0,
    val balance: Int = 0,
)

private data class DailyQuestUi(
    val type: String,
    val title: String,
    val description: String,
    val achieved: Boolean,
    val enabled: Boolean,
)

private fun RootTab.label(): String = when (this) {
    RootTab.TODAY -> "오늘"
    RootTab.MANAGE -> "관리"
    RootTab.HISTORY -> "기록"
    RootTab.SETTINGS -> "설정"
}

private fun ManageFilter.label(): String = when (this) {
    ManageFilter.ALL -> "전체"
    ManageFilter.ACTIVE -> "진행 중"
    ManageFilter.COMPLETED -> "완료"
}

private fun HistoryRange.label(): String = when (this) {
    HistoryRange.DAILY -> "일간"
    HistoryRange.WEEKLY -> "주간"
    HistoryRange.MONTHLY -> "월간"
}

private fun TodayTaskUi.isScheduledFor(date: LocalDate): Boolean {
    val rawDate = date.toString()
    if (dailyDate == rawDate && (isCompleted || dailySource == "DEFERRED")) return true

    val starts = startDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val ends = endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    if (starts != null && date.isBefore(starts)) return false
    if (ends != null && date.isAfter(ends)) return false

    val repeat = repeatLabel.orEmpty()
    return when {
        repeat.startsWith("${DayQuestViewModel.CUSTOM_REPEAT_LABEL}:") -> {
            val selectedDays = repeat.substringAfter(":")
                .split(",")
                .filter { it.isNotBlank() }
                .toSet()
            selectedDays.isEmpty() || date.koreanDayLabel() in selectedDays
        }
        repeat.startsWith("${DayQuestViewModel.MONTHLY_REPEAT_LABEL}:") ->
            repeat.substringAfter(":").toIntOrNull()?.coerceIn(1, 31) == date.dayOfMonth
        repeat == "주중" -> date.dayOfWeek.value in 1..5
        repeat == "주말" -> date.dayOfWeek.value in 6..7
        else -> true
    }
}

private fun LocalDate.koreanDayLabel(): String = when (dayOfWeek.value) {
    1 -> "월"
    2 -> "화"
    3 -> "수"
    4 -> "목"
    5 -> "금"
    6 -> "토"
    else -> "일"
}

private fun QuestTier.label(): String = when (this) {
    QuestTier.MAIN -> "핵심"
    QuestTier.RARE -> "중요"
    QuestTier.NORMAL -> "일반"
}

private fun QuestTier.priorityLabel(): String = when (this) {
    QuestTier.MAIN -> "높음"
    QuestTier.RARE -> "중간"
    QuestTier.NORMAL -> "낮음"
}

@Composable
fun DayQuestWireframeApp(dayQuestViewModel: DayQuestViewModel = viewModel()) {
    val uiState by dayQuestViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentStreak = remember(uiState.history) {
        uiState.history.takeWhile { it.totalCount > 0 && it.completedCount >= it.totalCount }.size
    }
    val todayListState = rememberLazyListState()
    val manageListState = rememberLazyListState()
    val historyListState = rememberLazyListState()
    val settingsListState = rememberLazyListState()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            dayQuestViewModel.onToggleNotification(true)
        } else {
            dayQuestViewModel.onNotificationPermissionDenied()
        }
    }

    LaunchedEffect(dayQuestViewModel) {
        dayQuestViewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    DayQuestTheme(darkTheme = uiState.darkModeEnabled) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (uiState.selectedTab == RootTab.MANAGE) {
                    FloatingActionButton(
                        modifier = Modifier
                            .testTag("manage_fab")
                            .semantics { contentDescription = "의뢰 등록 열기" },
                        onClick = dayQuestViewModel::onOpenCreateSheet,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            },
            bottomBar = { BottomQuestBar(uiState.selectedTab, dayQuestViewModel::onTabSelected) },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appBackgroundBrush())
                    .padding(padding),
            ) {
                when (uiState.selectedTab) {
                    RootTab.TODAY -> TodayScreen(
                        tasks = uiState.tasks
                            .filter { it.isScheduledFor(LocalDate.now()) }
                            .filterNot { it.isSkippedToday },
                        loadState = uiState.todayLoadState,
                        listState = todayListState,
                        quickInput = uiState.quickInput,
                        quickMemo = uiState.quickMemo,
                        quickTime = uiState.quickTime,
                        quickRepeat = uiState.quickRepeat,
                        quickOptionsOpen = uiState.quickOptionsOpen,
                        character = uiState.character,
                        streakDays = currentStreak,
                        onQuickInputChange = dayQuestViewModel::onQuickInputChange,
                        onQuickMemoChange = dayQuestViewModel::onQuickMemoChange,
                        onQuickTimeChange = dayQuestViewModel::onQuickTimeChange,
                        onQuickRepeatChange = dayQuestViewModel::onQuickRepeatChange,
                        onToggleQuickOptions = dayQuestViewModel::onToggleQuickOptions,
                        onQuickAdd = dayQuestViewModel::onQuickAdd,
                        onToggleComplete = dayQuestViewModel::onToggleComplete,
                        onSnooze = dayQuestViewModel::onSnooze,
                        onSkip = dayQuestViewModel::onSkip,
                        onNavigateManage = { dayQuestViewModel.onTabSelected(RootTab.MANAGE) },
                        onRetry = dayQuestViewModel::onRetryToday,
                    )

                    RootTab.MANAGE -> ManageScreen(
                        tasks = uiState.tasks,
                        filter = uiState.manageFilter,
                        listState = manageListState,
                        onFilterChange = dayQuestViewModel::onFilterChange,
                        onEdit = dayQuestViewModel::onOpenEditSheet,
                        onDelete = { dayQuestViewModel.onRequestDelete(it.id) },
                    )

                    RootTab.HISTORY -> HistoryScreen(
                        history = uiState.history,
                        loadState = uiState.historyLoadState,
                        range = uiState.historyRange,
                        listState = historyListState,
                        onRangeChange = dayQuestViewModel::onHistoryRangeChange,
                        onRetry = dayQuestViewModel::onRetryHistory,
                    )

                    RootTab.SETTINGS -> SettingsScreen(
                        notificationEnabled = uiState.notificationEnabled,
                        onToggleNotification = { enabled ->
                            val hasNotificationPermission =
                                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) == PackageManager.PERMISSION_GRANTED

                            if (enabled && !hasNotificationPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                dayQuestViewModel.onToggleNotification(enabled)
                            }
                        },
                        darkModeEnabled = uiState.darkModeEnabled,
                        onToggleDarkMode = dayQuestViewModel::onToggleDarkMode,
                        reminderTime = uiState.reminderTime,
                        onReminderTimeChange = dayQuestViewModel::onReminderTimeChange,
                        loadState = uiState.settingsLoadState,
                        listState = settingsListState,
                        onRetry = dayQuestViewModel::onRetrySettings,
                        onReset = dayQuestViewModel::onRequestReset,
                    )
                }
            }
        }

        if (uiState.form.isOpen) {
            QuestFormSheet(
                form = uiState.form,
                onDismiss = dayQuestViewModel::onDismissForm,
                onTitleChange = dayQuestViewModel::onFormTitleChange,
                onMemoChange = dayQuestViewModel::onFormMemoChange,
                onCategoryChange = dayQuestViewModel::onFormCategoryChange,
                onRepeatChange = dayQuestViewModel::onFormRepeatChange,
                onToggleCustomRepeatDay = dayQuestViewModel::onToggleCustomRepeatDay,
                onMonthlyDayChange = dayQuestViewModel::onFormMonthlyDayChange,
                onTierChange = dayQuestViewModel::onFormTierChange,
                onStartDateChange = dayQuestViewModel::onFormStartDateChange,
                onEndDateChange = dayQuestViewModel::onFormEndDateChange,
                onTimeChange = dayQuestViewModel::onFormTimeChange,
                onReminderChange = dayQuestViewModel::onFormReminderChange,
                onSave = dayQuestViewModel::onSubmitForm,
            )
        }
        if (uiState.pendingDeleteTaskId != null) {
            DeleteConfirmDialog(dayQuestViewModel::onDismissDelete, dayQuestViewModel::onConfirmDelete)
        }
        if (uiState.showResetConfirm) {
            ResetConfirmDialog(dayQuestViewModel::onDismissReset, dayQuestViewModel::onConfirmReset)
        }
    }
}

@Composable
private fun TodayScreen(
    tasks: List<TodayTaskUi>,
    loadState: LoadState,
    listState: LazyListState,
    quickInput: String,
    quickMemo: String,
    quickTime: String,
    quickRepeat: String,
    quickOptionsOpen: Boolean,
    character: CharacterGrowthUi,
    streakDays: Int,
    onQuickInputChange: (String) -> Unit,
    onQuickMemoChange: (String) -> Unit,
    onQuickTimeChange: (String) -> Unit,
    onQuickRepeatChange: (String) -> Unit,
    onToggleQuickOptions: () -> Unit,
    onQuickAdd: () -> Unit,
    onToggleComplete: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onNavigateManage: () -> Unit,
    onRetry: () -> Unit,
) {
    val completedCount = tasks.count { it.isCompleted }
    val progress = if (tasks.isEmpty()) 0f else completedCount.toFloat() / tasks.size.toFloat()
    val dailyQuests = remember(tasks) { buildDailyQuests(tasks) }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("today_task_list"),
        contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_top_bar"),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("길드 장부", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text("DayQuest", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text(screenDateLabel(), style = MaterialTheme.typography.displayLarge)
                Text(
                    "오늘의 리스트를 완수해 보세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            CharacterGrowthCard(character)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "오늘의 진행도",
                    value = "$completedCount/${tasks.size}",
                    subtitle = when {
                        tasks.isEmpty() -> "첫 완료를 시작하세요"
                        completedCount == tasks.size -> "Quest Complete"
                        else -> "다음 보상까지 ${tasks.size - completedCount}건"
                    },
                    progress = progress,
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "현재 스트릭",
                    value = if (streakDays > 0) "${streakDays}일" else "0일",
                    subtitle = if (streakDays > 0) "흐름을 이어가고 있습니다" else "첫 완수를 시작해 보세요",
                    progress = null,
                )
            }
        }

        item {
            QuickAddCard(
                input = quickInput,
                memo = quickMemo,
                time = quickTime,
                repeat = quickRepeat,
                optionsOpen = quickOptionsOpen,
                onInputChange = onQuickInputChange,
                onMemoChange = onQuickMemoChange,
                onTimeChange = onQuickTimeChange,
                onRepeatChange = onQuickRepeatChange,
                onToggleOptions = onToggleQuickOptions,
                onQuickAdd = onQuickAdd,
            )
        }

        item {
            DailyQuestBoard(dailyQuests)
        }

        when (loadState) {
            LoadState.LOADING -> items(4) { LoadingCard() }

            LoadState.ERROR -> item {
                BoardCard {
                    Text("오늘의 의뢰를 불러오지 못했습니다.", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "네트워크 상태를 확인한 뒤 다시 시도해 주세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(modifier = Modifier.testTag("today_error_retry"), onClick = onRetry) {
                        Text("다시 시도")
                    }
                }
            }

            LoadState.CONTENT -> {
                if (tasks.isEmpty()) {
                    item {
                        BoardCard {
                            Text("오늘 등록된 의뢰가 없습니다.", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "관리 화면에서 첫 의뢰를 추가하면 오늘 목록이 바로 채워집니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(
                                modifier = Modifier
                                    .testTag("today_empty_cta")
                                    .semantics { contentDescription = "관리 화면으로 이동" },
                                onClick = onNavigateManage,
                            ) {
                                Text("관리로 이동")
                            }
                        }
                    }
                } else {
                    items(tasks, key = { it.id }) { task ->
                        SwipeTaskCard(
                            task = task,
                            onToggleComplete = { onToggleComplete(task.id) },
                            onPrimarySwipe = { onSnooze(task.id) },
                            onSecondarySwipe = { onSkip(task.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageScreen(
    tasks: List<TodayTaskUi>,
    filter: ManageFilter,
    listState: LazyListState,
    onFilterChange: (ManageFilter) -> Unit,
    onEdit: (TodayTaskUi) -> Unit,
    onDelete: (TodayTaskUi) -> Unit,
) {
    val filtered = tasks.filter {
        when (filter) {
            ManageFilter.ALL -> true
            ManageFilter.ACTIVE -> !it.isCompleted
            ManageFilter.COMPLETED -> it.isCompleted
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("manage_task_list"),
        contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manage_top_bar"),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("관리", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "길드 장부처럼 의뢰를 정리하고 수정합니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manage_filter_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ManageFilter.entries.forEach { target ->
                    FilterChip(
                        modifier = Modifier
                            .height(48.dp)
                            .semantics { contentDescription = "관리 필터 ${target.label()} 선택" },
                        selected = filter == target,
                        onClick = { onFilterChange(target) },
                        label = { Text(target.label()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                BoardCard {
                    Text("표시할 의뢰가 없습니다.", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "필터를 바꾸거나 새 의뢰를 등록해 보세요.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(filtered, key = { it.id }) { task ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        when (value) {
                            SwipeToDismissBoxValue.StartToEnd -> onEdit(task)
                            SwipeToDismissBoxValue.EndToStart -> onDelete(task)
                            SwipeToDismissBoxValue.Settled -> Unit
                        }
                        false
                    },
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = { SwipeBackground("수정", "삭제") },
                ) {
                    BoardCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    TierBadge(task.tier)
                                    task.categoryLabel?.let {
                                        StatusBadge(
                                            text = it,
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                    StatusBadge(
                                        text = taskStatusLabel(task),
                                        containerColor = taskStatusContainer(task),
                                        contentColor = taskStatusContent(task),
                                    )
                                    Text(
                                        text = task.timeLabel?.let { "오늘 $it" } ?: "시간 미지정",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(task.title, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = taskLedgerNote(task),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = if (task.reminderEnabled) "개별 알림 켜짐" else "개별 알림 꺼짐",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { onEdit(task) }) {
                                Text("수정")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    history: List<HistoryDayUi>,
    loadState: LoadState,
    range: HistoryRange,
    listState: LazyListState,
    onRangeChange: (HistoryRange) -> Unit,
    onRetry: () -> Unit,
) {
    val visible = when (range) {
        HistoryRange.DAILY -> history.take(3)
        HistoryRange.WEEKLY -> history.take(7)
        HistoryRange.MONTHLY -> history
    }
    val completed = visible.sumOf { it.completedCount }
    val total = visible.sumOf { it.totalCount }
    val recentXp = visible.sumOf { it.earnedXp }
    val streakDays = history.takeWhile { it.totalCount > 0 && it.completedCount >= it.totalCount }.size
    val completionRate = if (total == 0) 0 else (completed * 100 / total)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_day_list"),
        contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_top_bar"),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Archive Ledger", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("기록 보관소", style = MaterialTheme.typography.headlineMedium)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryRange.entries.forEach { target ->
                    FilterChip(
                        modifier = Modifier
                            .height(48.dp)
                            .semantics { contentDescription = "기록 범위 ${target.label()} 선택" },
                        selected = range == target,
                        onClick = { onRangeChange(target) },
                        label = { Text(target.label()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
        }

        when (loadState) {
            LoadState.LOADING -> items(3) { LoadingCard() }

            LoadState.ERROR -> item {
                BoardCard {
                    Text("기록을 불러오지 못했습니다.", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = onRetry) {
                        Text("다시 시도")
                    }
                }
            }

            LoadState.CONTENT -> {
                item {
                    BoardCard(modifier = Modifier.testTag("history_summary_card")) {
                        Text("길드 기록 요약", style = MaterialTheme.typography.titleLarge)
                        Text(
                            historySummaryRangeLabel(range),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("연속 달성", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${streakDays}일", style = MaterialTheme.typography.headlineMedium)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("누적 XP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("+$recentXp", style = MaterialTheme.typography.headlineMedium)
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text("달성률", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$completionRate%", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (total == 0) 0f else completed.toFloat() / total.toFloat())
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.tertiary),
                            )
                        }
                    }
                }

                if (visible.isEmpty()) {
                    item {
                        BoardCard {
                            Text("표시할 기록이 없습니다.", style = MaterialTheme.typography.titleLarge)
                            Text("완료한 의뢰가 쌓이면 이곳에 흐름이 정리됩니다.")
                        }
                    }
                } else {
                    items(visible, key = { it.dateLabel }) { entry ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(entry.dateLabel, style = MaterialTheme.typography.titleMedium)
                            BoardCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 5.dp)
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondary),
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                archivalTitle(entry),
                                                style = MaterialTheme.typography.titleMedium,
                                            )
                                            Text(
                                                archivalSubtitle(entry),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    StatusBadge(
                                        text = "기록됨",
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    notificationEnabled: Boolean,
    onToggleNotification: (Boolean) -> Unit,
    darkModeEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    reminderTime: String,
    onReminderTimeChange: (String) -> Unit,
    loadState: LoadState,
    listState: LazyListState,
    onRetry: () -> Unit,
    onReset: () -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_top_bar"),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("설정", style = MaterialTheme.typography.headlineMedium)
            }
        }

        when (loadState) {
            LoadState.LOADING -> items(3) { LoadingCard() }

            LoadState.ERROR -> item {
                BoardCard {
                    Text("설정을 불러오지 못했습니다.", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "저장된 설정을 다시 확인해 주세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(modifier = Modifier.testTag("settings_error_retry"), onClick = onRetry) {
                        Text("다시 시도")
                    }
                }
            }

            LoadState.CONTENT -> {
                item {
                    BoardCard(modifier = Modifier.testTag("settings_preference_card")) {
                        Text("알림 설정", style = MaterialTheme.typography.titleLarge)
                        PreferenceRow(
                            title = "전체 알림",
                            subtitle = "리스트와 주요 소식을 확인합니다.",
                        ) {
                            Switch(
                                modifier = Modifier.semantics { contentDescription = "전체 알림 전환" },
                                checked = notificationEnabled,
                                onCheckedChange = onToggleNotification,
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("고정 알림 시간", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.testTag("settings_reminder_time_group"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("07:00", "21:00").forEach { option ->
                                FilterChip(
                                    modifier = Modifier.semantics {
                                        contentDescription = "고정 알림 시간 $option"
                                    },
                                    selected = true,
                                    onClick = { if (option == "21:00") onReminderTimeChange(option) },
                                    label = { Text(option) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                )
                            }
                        }
                    }
                }

                item {
                    BoardCard {
                        Text("화면 설정", style = MaterialTheme.typography.titleLarge)
                        PreferenceRow(
                            title = "다크 모드",
                            subtitle = "야간 장부 테마로 전환합니다.",
                        ) {
                            Switch(
                                modifier = Modifier.semantics { contentDescription = "다크 모드 전환" },
                                checked = darkModeEnabled,
                                onCheckedChange = onToggleDarkMode,
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                }

                item {
                    BoardCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("데이터 초기화", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "모든 기록을 비우고 새로 시작할 때 사용합니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            modifier = Modifier.semantics { contentDescription = "모든 기록 초기화" },
                            onClick = onReset,
                        ) {
                            Text("초기화")
                        }
                    }
                }

                item {
                    BoardCard {
                        Text("앱 정보", style = MaterialTheme.typography.titleLarge)
                        PreferenceRow(
                            title = "버전 정보",
                            subtitle = "v0.1.0-wireframe",
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomQuestBar(selectedTab: RootTab, onSelect: (RootTab) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                RootTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    TextButton(
                        onClick = { onSelect(tab) },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "${tab.label()} 탭" },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    ),
                            )
                            Text(
                                tab.label(),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    progress: Float?,
) {
    BoardCard(modifier = modifier, containerColor = MaterialTheme.colorScheme.surface) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (progress != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.tertiary),
                )
            }
        }
    }
}

@Composable
private fun QuickAddCard(
    input: String,
    memo: String,
    time: String,
    repeat: String,
    optionsOpen: Boolean,
    onInputChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onRepeatChange: (String) -> Unit,
    onToggleOptions: () -> Unit,
    onQuickAdd: () -> Unit,
) {
    BoardCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("today_quick_add_input"),
                    value = input,
                    onValueChange = onInputChange,
                    singleLine = true,
                    placeholder = { Text("새로운 의뢰 추가...") },
                    colors = questTextFieldColors(),
                )
                Spacer(modifier = Modifier.width(10.dp))
                TextButton(
                    modifier = Modifier.semantics { contentDescription = "빠른 추가 상세 옵션" },
                    onClick = onToggleOptions,
                ) {
                    Text(if (optionsOpen) "접기" else "상세")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    modifier = Modifier
                        .testTag("today_quick_add_button")
                        .semantics { contentDescription = "새 의뢰 빠른 추가" },
                    onClick = onQuickAdd,
                    enabled = input.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("기록")
                }
            }

            if (optionsOpen) {
                TextField(
                    value = memo,
                    onValueChange = onMemoChange,
                    label = { Text("메모") },
                    maxLines = 2,
                    colors = questTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = time,
                    onValueChange = onTimeChange,
                    label = { Text("목표 시간 HH:mm") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = questTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DayQuestViewModel.QUICK_REPEAT_OPTIONS.forEach { option ->
                        FilterChip(
                            selected = repeat == option,
                            onClick = { onRepeatChange(option) },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterGrowthCard(character: CharacterGrowthUi) {
    val progress = if (character.nextLevelExp <= 0) {
        0f
    } else {
        character.expInLevel.toFloat() / character.nextLevelExp.toFloat()
    }

    BoardCard(modifier = Modifier.testTag("today_character_growth_card")) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CharacterAvatar(character, Modifier.size(96.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("캐릭터 성장", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Lv.${character.level} ${character.title}", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "일일 의뢰 보상으로 성장합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusBadge(
                    text = "훈련 ${character.trainingPoints}",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            SummaryProgressBar(progress)
            Text(
                "EXP ${character.expInLevel}/${character.nextLevelExp} · 누적 ${character.totalExp}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CharacterStatChip("집중", character.focus, Modifier.weight(1f))
                CharacterStatChip("체력", character.vitality, Modifier.weight(1f))
                CharacterStatChip("통찰", character.insight, Modifier.weight(1f))
                CharacterStatChip("균형", character.balance, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CharacterAvatar(character: CharacterGrowthUi, modifier: Modifier = Modifier) {
    val stage = when {
        character.level >= 10 -> 4
        character.level >= 7 -> 3
        character.level >= 4 -> 2
        else -> 1
    }
    val primary = characterAvatarColor(character.level)
    val surface = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outlineVariant
    val ink = MaterialTheme.colorScheme.onSurface
    val cloak = MaterialTheme.colorScheme.secondary
    val spark = MaterialTheme.colorScheme.tertiary
    val skin = Color(0xFFFFD6B8)
    val gold = Color(0xFFFFC857)
    val statColors = listOf(
        DayQuestTaskAccentMain,
        DayQuestTaskAccentOverdue,
        DayQuestTaskAccentRare,
        DayQuestTaskAccentNormal,
    )

    Canvas(
        modifier = modifier.semantics {
            contentDescription = "레벨 ${character.level} ${character.title} 캐릭터 그래픽"
        },
    ) {
        val unit = if (size.width < size.height) size.width else size.height
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = unit * 0.46f
        val floorY = center.y + unit * 0.32f

        drawCircle(surface.copy(alpha = 0.88f), baseRadius, center)
        drawCircle(primary.copy(alpha = 0.14f), baseRadius * 0.86f, center)
        drawCircle(
            outline.copy(alpha = 0.55f),
            baseRadius,
            center,
            style = Stroke(width = unit * 0.018f),
        )
        if (stage >= 2) {
            drawCircle(
                primary.copy(alpha = 0.32f),
                baseRadius * 0.98f,
                center,
                style = Stroke(width = unit * 0.035f),
            )
        }
        if (stage >= 4) {
            drawCircle(
                gold.copy(alpha = 0.42f),
                baseRadius * 1.08f,
                center,
                style = Stroke(width = unit * 0.022f),
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
                    floorY,
                )
                lineTo(center.x + unit * 0.18f, floorY)
                cubicTo(
                    center.x + unit * 0.35f,
                    floorY,
                    center.x + unit * 0.36f,
                    center.y + unit * 0.02f,
                    center.x,
                    center.y - unit * 0.04f,
                )
                close()
            }
            drawPath(cloakPath, cloak.copy(alpha = 0.72f))
        }

        drawRect(
            color = skin,
            topLeft = Offset(center.x - unit * 0.055f, center.y - unit * 0.02f),
            size = Size(unit * 0.11f, unit * 0.12f),
        )
        drawOval(
            color = primary.copy(alpha = 0.95f),
            topLeft = Offset(center.x - unit * 0.18f, center.y + unit * 0.02f),
            size = Size(unit * 0.36f, unit * 0.34f),
        )
        drawOval(
            color = primary.copy(alpha = 0.32f),
            topLeft = Offset(center.x - unit * 0.13f, center.y + unit * 0.10f),
            size = Size(unit * 0.26f, unit * 0.16f),
        )

        val faceCenter = Offset(center.x, center.y - unit * 0.16f)
        drawCircle(skin, unit * 0.145f, faceCenter)
        drawOval(
            color = ink.copy(alpha = 0.22f),
            topLeft = Offset(center.x - unit * 0.14f, center.y - unit * 0.30f),
            size = Size(unit * 0.28f, unit * 0.13f),
        )
        drawCircle(ink, unit * 0.014f, Offset(center.x - unit * 0.052f, center.y - unit * 0.15f))
        drawCircle(ink, unit * 0.014f, Offset(center.x + unit * 0.052f, center.y - unit * 0.15f))
        drawLine(
            color = ink.copy(alpha = 0.45f),
            start = Offset(center.x - unit * 0.035f, center.y - unit * 0.095f),
            end = Offset(center.x + unit * 0.035f, center.y - unit * 0.095f),
            strokeWidth = unit * 0.008f,
        )

        if (stage >= 3) {
            val staffBase = Offset(center.x + unit * 0.27f, center.y + unit * 0.26f)
            val staffTop = Offset(center.x + unit * 0.33f, center.y - unit * 0.29f)
            drawLine(
                color = spark.copy(alpha = 0.9f),
                start = staffBase,
                end = staffTop,
                strokeWidth = unit * 0.026f,
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
                size = Size(unit * 0.24f, unit * 0.035f),
            )
        }

        val stats = listOf(character.focus, character.vitality, character.insight, character.balance)
        stats.forEachIndexed { index, value ->
            if (value > 0) {
                val scale = value.coerceAtMost(12) / 12f
                drawCircle(
                    color = statColors[index].copy(alpha = 0.35f + scale * 0.45f),
                    radius = unit * (0.018f + scale * 0.012f),
                    center = Offset(center.x - unit * 0.18f + index * unit * 0.12f, center.y + unit * 0.36f),
                )
            }
        }
    }
}

private fun characterAvatarColor(level: Int): Color = when {
    level >= 10 -> Color(0xFF6A51D8)
    level >= 7 -> Color(0xFF1E8A6A)
    level >= 4 -> Color(0xFF2667C9)
    else -> Color(0xFF64748B)
}

@Composable
private fun SummaryProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun CharacterStatChip(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun DailyQuestBoard(quests: List<DailyQuestUi>) {
    BoardCard(modifier = Modifier.testTag("today_daily_quest_board")) {
        Text("오늘의 퀘스트", style = MaterialTheme.typography.titleLarge)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            quests.forEach { quest ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(quest.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            quest.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    StatusBadge(
                        text = when {
                            !quest.enabled -> "대상 없음"
                            quest.achieved -> "달성"
                            else -> "진행 중"
                        },
                        containerColor = if (quest.achieved) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (quest.achieved) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeTaskCard(
    task: TodayTaskUi,
    onToggleComplete: () -> Unit,
    onPrimarySwipe: () -> Unit,
    onSecondarySwipe: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> onPrimarySwipe()
                SwipeToDismissBoxValue.EndToStart -> onSecondarySwipe()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SwipeBackground("보류", "건너뜀") },
    ) {
        BoardCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(taskAccentColor(task)),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            color = if (task.isCompleted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                        Text(
                            text = taskMeta(task),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    StatusBadge(
                        text = taskStatusLabel(task),
                        containerColor = taskStatusContainer(task),
                        contentColor = taskStatusContent(task),
                    )
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        modifier = Modifier.semantics {
                            contentDescription = "${task.title} 완료 체크"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeBackground(startLabel: String, endLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(startLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(endLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LoadingCard() {
    BoardCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        )
    }
}

@Composable
private fun QuestFormSheet(
    form: TaskFormUi,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onRepeatChange: (String) -> Unit,
    onToggleCustomRepeatDay: (String) -> Unit,
    onMonthlyDayChange: (String) -> Unit,
    onTierChange: (QuestTier) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onReminderChange: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .testTag("manage_form_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionHeader(
                title = if (form.editingTaskId == null) "의뢰 등록" else "의뢰 수정",
                subtitle = "카테고리, 반복, 등급, 알림까지 한 번에 정리합니다.",
                tag = "GUILD FORM",
            )
            TextField(
                value = form.title,
                onValueChange = onTitleChange,
                label = { Text("의뢰 제목") },
                singleLine = true,
                isError = form.titleError != null,
                colors = questTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            form.titleError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            TextField(
                value = form.memo,
                onValueChange = onMemoChange,
                label = { Text("메모 선택") },
                maxLines = 3,
                colors = questTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("카테고리", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("업무", "건강", "학습", "개인").forEach { option ->
                    FilterChip(
                        modifier = Modifier
                            .height(48.dp)
                            .semantics { contentDescription = "카테고리 $option 선택" },
                        selected = form.category == option,
                        onClick = { onCategoryChange(option) },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                    )
                }
            }
            TextField(
                value = form.category,
                onValueChange = onCategoryChange,
                label = { Text("카테고리 직접 입력") },
                singleLine = true,
                colors = questTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("반복 규칙", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    listOf("매일", "주중"),
                    listOf("주말", "매달"),
                    listOf("커스텀"),
                ).forEach { rowOptions ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowOptions.forEach { option ->
                            FilterChip(
                                modifier = Modifier
                                    .height(48.dp)
                                    .semantics { contentDescription = "반복 규칙 $option 선택" },
                                selected = form.repeat == option,
                                onClick = { onRepeatChange(option) },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                ),
                            )
                        }
                    }
                }
            }

            if (form.repeat == DayQuestViewModel.CUSTOM_REPEAT_LABEL) {
                Text("반복 요일", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayQuestViewModel.CUSTOM_REPEAT_DAYS.chunked(4).forEach { rowOptions ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowOptions.forEach { day ->
                                FilterChip(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .semantics { contentDescription = "반복 요일 $day 선택" },
                                    selected = day in form.customRepeatDays,
                                    onClick = { onToggleCustomRepeatDay(day) },
                                    label = { Text(day) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            if (form.repeat == DayQuestViewModel.MONTHLY_REPEAT_LABEL) {
                TextField(
                    value = form.monthlyDay,
                    onValueChange = onMonthlyDayChange,
                    label = { Text("매달 반복일 1-31") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = questTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Text("의뢰 등급", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(QuestTier.NORMAL, QuestTier.RARE, QuestTier.MAIN).forEach { option ->
                    FilterChip(
                        modifier = Modifier
                            .height(48.dp)
                            .semantics { contentDescription = "의뢰 등급 ${option.priorityLabel()} 선택" },
                        selected = form.tier == option,
                        onClick = { onTierChange(option) },
                        label = { Text(option.priorityLabel()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            TextField(
                value = form.time,
                onValueChange = onTimeChange,
                label = { Text("목표 시간 HH:mm") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = form.timeError != null,
                colors = questTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            form.timeError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(
                    value = form.startDate,
                    onValueChange = onStartDateChange,
                    label = { Text("시작일 yyyy-MM-dd") },
                    singleLine = true,
                    colors = questTextFieldColors(),
                    modifier = Modifier.weight(1f),
                )
                TextField(
                    value = form.endDate,
                    onValueChange = onEndDateChange,
                    label = { Text("종료일 yyyy-MM-dd") },
                    singleLine = true,
                    colors = questTextFieldColors(),
                    modifier = Modifier.weight(1f),
                )
            }
            form.dateError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            PreferenceRow(
                title = "개별 알림",
                subtitle = "이 의뢰의 목표 시간에 리마인더를 받습니다.",
            ) {
                Switch(
                    modifier = Modifier.semantics { contentDescription = "개별 알림 전환" },
                    checked = form.reminderEnabled,
                    onCheckedChange = onReminderChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSave) {
                    Text(if (form.editingTaskId == null) "저장" else "수정")
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        modifier = Modifier.testTag("manage_delete_dialog"),
        onDismissRequest = onDismiss,
        title = { Text("의뢰를 삭제할까요?") },
        text = { Text("삭제한 의뢰는 복구되지 않습니다.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
private fun ResetConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("모든 기록을 초기화할까요?") },
        text = { Text("의뢰 목록, 기록, 설정이 모두 기본 상태로 돌아갑니다.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text("초기화")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
private fun PreferenceRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        trailing()
    }
}

@Composable
private fun BoardCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = containerColor.copy(alpha = 0.98f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun TierBadge(tier: QuestTier) {
    val containerColor = when (tier) {
        QuestTier.MAIN -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        QuestTier.RARE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
        QuestTier.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (tier) {
        QuestTier.MAIN -> MaterialTheme.colorScheme.primary
        QuestTier.RARE -> MaterialTheme.colorScheme.tertiary
        QuestTier.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    StatusBadge(tier.label(), containerColor, contentColor)
}

@Composable
private fun StatusBadge(text: String, containerColor: Color, contentColor: Color) {
    Surface(shape = RoundedCornerShape(999.dp), color = containerColor) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, tag: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        tag?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun questTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
)

@Composable
private fun appBackgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        MaterialTheme.colorScheme.background,
    ),
)

private fun screenDateLabel(): String = LocalDate.now()
    .format(DateTimeFormatter.ofPattern("M월 d일, EEEE", Locale.KOREAN))

private fun taskStatusLabel(task: TodayTaskUi): String = when {
    task.isCompleted -> "완료"
    task.isOverdue -> "미룸"
    else -> "진행 중"
}

@Composable
private fun taskStatusContainer(task: TodayTaskUi): Color = when {
    task.isCompleted -> MaterialTheme.colorScheme.secondaryContainer
    task.isOverdue -> MaterialTheme.colorScheme.surfaceVariant
    else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
}

@Composable
private fun taskStatusContent(task: TodayTaskUi): Color = when {
    task.isCompleted -> MaterialTheme.colorScheme.onSecondaryContainer
    task.isOverdue -> MaterialTheme.colorScheme.onSurfaceVariant
    else -> MaterialTheme.colorScheme.tertiary
}

private fun taskAccentColor(task: TodayTaskUi): Color = when {
    task.isCompleted -> DayQuestTaskAccentCompleted
    task.isOverdue -> DayQuestTaskAccentOverdue
    task.tier == QuestTier.MAIN -> DayQuestTaskAccentMain
    task.tier == QuestTier.RARE -> DayQuestTaskAccentRare
    else -> DayQuestTaskAccentNormal
}

private fun taskMeta(task: TodayTaskUi): String {
    val parts = buildList {
        add(task.tier.label())
        task.categoryLabel?.let { add(it) }
        task.timeLabel?.let { add(it) }
        task.repeatLabel?.let { add(it) }
        task.startDate?.let { add("시작 $it") }
        task.endDate?.let { add("종료 $it") }
        if (task.reminderEnabled) add("알림")
    }
    return parts.joinToString(" · ")
}

private fun taskLedgerNote(task: TodayTaskUi): String {
    val schedule = buildList {
        task.repeatLabel?.let { add(it) }
        task.timeLabel?.let { add(it) }
        task.startDate?.let { add("시작 $it") }
        task.endDate?.let { add("종료 $it") }
    }.joinToString(" · ")

    return when {
        !task.memo.isNullOrBlank() && schedule.isNotBlank() -> "${task.memo} · $schedule"
        !task.memo.isNullOrBlank() -> task.memo
        task.isCompleted -> "관련 의뢰를 정리하고 완료 처리했습니다."
        task.isOverdue -> "오늘 안에 다시 확인이 필요한 의뢰입니다.${if (schedule.isNotBlank()) " $schedule" else ""}"
        schedule.isNotBlank() -> "반복 일정 $schedule 기준으로 관리합니다."
        else -> "반복 일정 없이 단발성으로 관리하는 의뢰입니다."
    }
}

private fun buildDailyQuests(tasks: List<TodayTaskUi>): List<DailyQuestUi> {
    val completedCount = tasks.count { it.isCompleted }
    val importantTasks = tasks.filter { it.tier == QuestTier.MAIN || it.tier == QuestTier.RARE }
    val hasTasks = tasks.isNotEmpty()

    return buildList {
        add(
            DailyQuestUi(
                type = "META_ONE",
                title = "첫 완료 달성",
                description = if (hasTasks) "오늘 의뢰 중 1개 이상 완료" else "오늘 할 일이 없어 달성할 수 없습니다.",
                achieved = hasTasks && completedCount >= 1,
                enabled = hasTasks,
            ),
        )
        add(
            DailyQuestUi(
                type = "META_ALL",
                title = "오늘 의뢰 전부 완료",
                description = if (hasTasks) "스킵된 의뢰를 제외하고 전체 완료" else "오늘 할 일이 없어 달성할 수 없습니다.",
                achieved = hasTasks && completedCount == tasks.size,
                enabled = hasTasks,
            ),
        )
        if (importantTasks.isNotEmpty()) {
            add(
                DailyQuestUi(
                    type = "IMPORTANT_ONE",
                    title = "중요 의뢰 완료",
                    description = "높음/중간 등급 의뢰 중 1개 이상 완료",
                    achieved = importantTasks.any { it.isCompleted },
                    enabled = true,
                ),
            )
        }
    }
}

private fun historySummaryRangeLabel(range: HistoryRange): String = when (range) {
    HistoryRange.DAILY -> "오늘 기준 요약"
    HistoryRange.WEEKLY -> "최근 7일 요약"
    HistoryRange.MONTHLY -> "최근 30일 요약"
}

private fun archivalTitle(entry: HistoryDayUi): String = when {
    entry.completedCount >= entry.totalCount && entry.totalCount > 0 -> "모든 의뢰를 정리했습니다"
    entry.completedCount > 0 -> "완료한 의뢰를 보관했습니다"
    else -> "기록된 의뢰가 없습니다"
}

private fun archivalSubtitle(entry: HistoryDayUi): String {
    val rate = if (entry.totalCount == 0) 0 else entry.completedCount * 100 / entry.totalCount
    return buildList {
        add("완료 ${entry.completedCount}/${entry.totalCount}")
        add("달성률 ${rate}%")
        entry.categorySummary?.let { add("분야 $it") }
        add(entry.note)
    }.joinToString(" · ")
}

fun defaultTasks(): List<TodayTaskUi> = listOf(
    TodayTaskUi(1, "분기별 세금 보고서 초안 작성", "증빙 파일 확인", "업무", "09:00", "매일", null, null, false, false, false, QuestTier.MAIN, true),
    TodayTaskUi(2, "디자인 시스템 정리", null, "학습", "11:30", "커스텀:월,수,금", null, null, false, false, false, QuestTier.RARE, true),
    TodayTaskUi(3, "운동 30분", null, "건강", null, "매일", null, null, false, true, false, QuestTier.NORMAL, false),
    TodayTaskUi(4, "팀 미팅 준비", "아젠다 초안 작성", "업무", "15:00", "주중", null, null, true, false, false, QuestTier.NORMAL, true),
    TodayTaskUi(5, "이메일 확인", null, "개인", null, "매일", null, null, true, false, false, QuestTier.NORMAL, false),
)

fun defaultHistory(): List<HistoryDayUi> = listOf(
    HistoryDayUi("10월 24일 목요일", 3, 5, 0, "핵심 의뢰 1건을 포함해 안정적으로 진행했습니다."),
    HistoryDayUi("10월 23일 수요일", 4, 4, 0, "모든 의뢰를 완수했습니다."),
    HistoryDayUi("10월 22일 화요일", 2, 3, 0, "짧은 의뢰 위주로 정리했습니다."),
    HistoryDayUi("10월 21일 월요일", 3, 4, 0, "반복 의뢰를 중심으로 마감했습니다."),
    HistoryDayUi("10월 20일 일요일", 1, 2, 0, "주간 계획을 다시 정돈했습니다."),
)
