package com.dayquest.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dayquest.app.DayQuestApp
import com.dayquest.domain.CharacterGrowthRules
import com.dayquest.domain.DayQuestRepository
import com.dayquest.domain.QuestTierChoice
import com.dayquest.domain.TaskDraftValidator
import com.dayquest.reminder.DayQuestReminderManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class LoadState { LOADING, CONTENT, ERROR }

data class TaskFormUi(
    val isOpen: Boolean = false,
    val editingTaskId: Long? = null,
    val title: String = "",
    val memo: String = "",
    val category: String = "",
    val repeat: String = "매일",
    val customRepeatDays: Set<String> = emptySet(),
    val monthlyDay: String = "1",
    val tier: QuestTier = QuestTier.NORMAL,
    val startDate: String = "",
    val endDate: String = "",
    val time: String = "",
    val reminderEnabled: Boolean = true,
    val titleError: String? = null,
    val timeError: String? = null,
    val dateError: String? = null,
)

data class DayQuestUiState(
    val selectedTab: RootTab = RootTab.TODAY,
    val quickInput: String = "",
    val quickMemo: String = "",
    val quickTime: String = "",
    val quickRepeat: String = "단발성",
    val quickOptionsOpen: Boolean = false,
    val character: CharacterGrowthUi = CharacterGrowthUi(),
    val tasks: List<TodayTaskUi> = emptyList(),
    val todayLoadState: LoadState = LoadState.LOADING,
    val manageFilter: ManageFilter = ManageFilter.ALL,
    val historyRange: HistoryRange = HistoryRange.WEEKLY,
    val history: List<HistoryDayUi> = emptyList(),
    val historyLoadState: LoadState = LoadState.LOADING,
    val notificationEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val reminderTime: String = "21:00",
    val settingsLoadState: LoadState = LoadState.LOADING,
    val form: TaskFormUi = TaskFormUi(),
    val pendingDeleteTaskId: Long? = null,
    val showResetConfirm: Boolean = false,
)

class DayQuestViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DayQuestRepository = getApplication<DayQuestApp>().repository
    private val reminderManager: DayQuestReminderManager = getApplication<DayQuestApp>().reminderManager

    private val _uiState = MutableStateFlow(DayQuestUiState())
    val uiState = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    private var taskJob: Job? = null
    private var characterJob: Job? = null
    private var historyJob: Job? = null
    private var settingsJob: Job? = null

    init {
        observeTasks()
        observeCharacter()
        observeHistory()
        observeSettings()
    }

    fun onTabSelected(tab: RootTab) = _uiState.update { it.copy(selectedTab = tab) }

    fun onQuickInputChange(value: String) = _uiState.update { it.copy(quickInput = value.take(40)) }

    fun onQuickMemoChange(value: String) = _uiState.update { it.copy(quickMemo = value.take(200)) }

    fun onQuickTimeChange(value: String) = _uiState.update { it.copy(quickTime = value.take(5)) }

    fun onQuickRepeatChange(value: String) = _uiState.update { it.copy(quickRepeat = value) }

    fun onToggleQuickOptions() = _uiState.update { it.copy(quickOptionsOpen = !it.quickOptionsOpen) }

    fun onQuickAdd() {
        val state = _uiState.value
        val validation = TaskDraftValidator.validate(
            title = state.quickInput,
            memo = state.quickMemo,
            categoryLabel = null,
            repeatRule = state.quickRepeat.toQuickRepeatRule(),
            startDate = null,
            endDate = null,
            timeLabel = state.quickTime,
            tier = QuestTierChoice.LOW,
            reminderEnabled = state.quickTime.isNotBlank(),
        )

        if (!validation.isValid) {
            emitMessage(validation.titleError ?: validation.timeError ?: validation.dateError ?: "빠른 추가 값을 확인해 주세요.")
            return
        }

        val draft = validation.draft ?: return

        launchMutation(
            successMessage = "할 일이 추가되었습니다",
            errorMessage = "의뢰를 저장하지 못했습니다.",
            refreshReminder = true,
        ) {
            repository.addTask(draft)
            _uiState.update {
                it.copy(
                    quickInput = "",
                    quickMemo = "",
                    quickTime = "",
                    quickRepeat = QUICK_REPEAT_ONCE,
                    quickOptionsOpen = false,
                )
            }
        }
    }

    fun onToggleComplete(taskId: Long) {
        val successMessage = completionSuccessMessage(taskId)
        launchMutation(
            successMessage = successMessage,
            errorMessage = "의뢰 상태를 변경하지 못했습니다.",
            refreshReminder = true,
        ) {
            repository.toggleTask(taskId, System.currentTimeMillis())
        }
    }

    fun onSnooze(taskId: Long) {
        val task = _uiState.value.tasks.firstOrNull { it.id == taskId } ?: return
        launchMutation(
            successMessage = "${task.title}를 내일로 미뤘습니다.",
            errorMessage = "의뢰를 내일로 미루지 못했습니다.",
            refreshReminder = true,
        ) {
            repository.snoozeTask(taskId, System.currentTimeMillis())
        }
    }

    fun onSkip(taskId: Long) {
        val task = _uiState.value.tasks.firstOrNull { it.id == taskId } ?: return
        launchMutation(
            successMessage = "${task.title}를 오늘 일정에서 건너뛰었습니다.",
            errorMessage = "오늘 일정에서 건너뛰지 못했습니다.",
            refreshReminder = true,
        ) {
            repository.skipTask(taskId, System.currentTimeMillis())
        }
    }

    fun onFilterChange(filter: ManageFilter) = _uiState.update { it.copy(manageFilter = filter) }

    fun onHistoryRangeChange(range: HistoryRange) = _uiState.update { it.copy(historyRange = range) }

    fun onOpenCreateSheet() = _uiState.update { it.copy(form = TaskFormUi(isOpen = true)) }

    fun onOpenEditSheet(task: TodayTaskUi) {
        _uiState.update {
            it.copy(
                form = TaskFormUi(
                    isOpen = true,
                    editingTaskId = task.id,
                    title = task.title,
                    memo = task.memo.orEmpty(),
                    category = task.categoryLabel.orEmpty(),
                    repeat = task.repeatBaseLabel(),
                    customRepeatDays = task.customRepeatDays(),
                    monthlyDay = task.monthlyDay(),
                    tier = task.tier,
                    startDate = task.startDate.orEmpty(),
                    endDate = task.endDate.orEmpty(),
                    time = task.timeLabel.orEmpty(),
                    reminderEnabled = task.reminderEnabled,
                ),
            )
        }
    }

    fun onDismissForm() = _uiState.update { it.copy(form = TaskFormUi()) }

    fun onFormTitleChange(value: String) = _uiState.update { state ->
        state.copy(form = state.form.copy(title = value.take(40), titleError = null))
    }

    fun onFormMemoChange(value: String) = _uiState.update { state ->
        state.copy(form = state.form.copy(memo = value.take(200)))
    }

    fun onFormCategoryChange(value: String) = _uiState.update { state ->
        state.copy(form = state.form.copy(category = value.take(20)))
    }

    fun onFormRepeatChange(value: String) = _uiState.update { state ->
        state.copy(
            form = state.form.copy(
                repeat = value.take(20),
                customRepeatDays = when {
                    value != CUSTOM_REPEAT_LABEL -> emptySet()
                    state.form.customRepeatDays.isEmpty() -> DEFAULT_CUSTOM_REPEAT_DAYS
                    else -> state.form.customRepeatDays
                },
                monthlyDay = if (value == MONTHLY_REPEAT_LABEL) state.form.monthlyDay.ifBlank { "1" } else state.form.monthlyDay,
            ),
        )
    }

    fun onFormMonthlyDayChange(value: String) = _uiState.update { state ->
        state.copy(form = state.form.copy(monthlyDay = value.filter { it.isDigit() }.take(2)))
    }

    fun onToggleCustomRepeatDay(day: String) = _uiState.update { state ->
        val current = state.form.customRepeatDays
        val next = if (day in current) current - day else current + day
        state.copy(form = state.form.copy(customRepeatDays = next))
    }

    fun onFormTierChange(value: QuestTier) = _uiState.update { state ->
        state.copy(form = state.form.copy(tier = value))
    }

    fun onFormStartDateChange(value: String) = _uiState.update { state ->
        state.copy(form = state.form.copy(startDate = value.take(10), dateError = null))
    }

    fun onFormEndDateChange(value: String) = _uiState.update { state ->
        state.copy(form = state.form.copy(endDate = value.take(10), dateError = null))
    }

    fun onFormTimeChange(value: String) = _uiState.update { state ->
        state.copy(form = state.form.copy(time = value.take(5), timeError = null))
    }

    fun onFormReminderChange(enabled: Boolean) = _uiState.update { state ->
        state.copy(form = state.form.copy(reminderEnabled = enabled))
    }

    fun onSubmitForm() {
        val form = _uiState.value.form
        val validation = TaskDraftValidator.validate(
            title = form.title,
            memo = form.memo,
            categoryLabel = form.category,
            repeatRule = form.normalizedRepeatRule(),
            startDate = form.startDate,
            endDate = form.endDate,
            timeLabel = form.time,
            tier = form.tier.toChoice(),
            reminderEnabled = form.reminderEnabled,
        )

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    form = it.form.copy(
                        titleError = validation.titleError,
                        timeError = validation.timeError,
                        dateError = validation.dateError,
                    ),
                )
            }
            return
        }

        val draft = validation.draft ?: return
        val isCreate = form.editingTaskId == null

        launchMutation(
            successMessage = if (isCreate) "의뢰를 등록했습니다." else "의뢰를 수정했습니다.",
            errorMessage = if (isCreate) "의뢰를 등록하지 못했습니다." else "의뢰를 수정하지 못했습니다.",
            refreshReminder = true,
        ) {
            if (isCreate) {
                repository.addTask(draft)
            } else {
                repository.updateTask(taskId = form.editingTaskId ?: return@launchMutation, draft = draft)
            }
            _uiState.update { it.copy(form = TaskFormUi()) }
        }
    }

    fun onRequestDelete(taskId: Long) = _uiState.update { it.copy(pendingDeleteTaskId = taskId) }

    fun onDismissDelete() = _uiState.update { it.copy(pendingDeleteTaskId = null) }

    fun onConfirmDelete() {
        val targetId = _uiState.value.pendingDeleteTaskId ?: return
        launchMutation(
            successMessage = "의뢰를 삭제했습니다.",
            errorMessage = "의뢰를 삭제하지 못했습니다.",
            refreshReminder = true,
        ) {
            repository.deleteTask(targetId)
            _uiState.update { it.copy(pendingDeleteTaskId = null) }
        }
    }

    fun onToggleNotification(enabled: Boolean) {
        launchMutation(
            successMessage = if (enabled) "알림을 켰습니다." else "알림을 껐습니다.",
            errorMessage = "알림 설정을 저장하지 못했습니다.",
            refreshReminder = true,
        ) {
            repository.updateNotificationEnabled(enabled)
        }
    }

    fun onNotificationPermissionDenied() {
        emitMessage("알림 권한이 없어 리마인더를 켤 수 없습니다.")
    }

    fun onToggleDarkMode(enabled: Boolean) {
        launchMutation(
            errorMessage = "다크 모드 설정을 저장하지 못했습니다.",
        ) {
            repository.updateDarkMode(enabled)
        }
    }

    fun onReminderTimeChange(time: String) {
        launchMutation(
            successMessage = "기본 알림 시간이 ${time}로 설정되었습니다",
            errorMessage = "알림 시간을 저장하지 못했습니다.",
            refreshReminder = true,
        ) {
            repository.updateReminderTime(time)
        }
    }

    fun onRequestReset() = _uiState.update { it.copy(showResetConfirm = true) }

    fun onDismissReset() = _uiState.update { it.copy(showResetConfirm = false) }

    fun onConfirmReset() {
        launchMutation(
            successMessage = "모든 기록을 초기화했습니다.",
            errorMessage = "데이터를 초기화하지 못했습니다.",
            refreshReminder = true,
        ) {
            repository.resetAll()
            _uiState.update {
                it.copy(
                    showResetConfirm = false,
                    form = TaskFormUi(),
                    pendingDeleteTaskId = null,
                )
            }
        }
    }

    fun onRetryToday() = observeTasks()

    fun onRetryHistory() = observeHistory()

    fun onRetrySettings() = observeSettings()

    private fun observeTasks() {
        taskJob?.cancel()
        taskJob = viewModelScope.launch {
            _uiState.update { it.copy(todayLoadState = LoadState.LOADING) }
            repository.observeTasks()
                .catch {
                    emitMessage("오늘 의뢰를 불러오지 못했습니다.")
                    _uiState.update { state -> state.copy(todayLoadState = LoadState.ERROR) }
                }
                .collect { tasks ->
                    _uiState.update { state ->
                        state.copy(tasks = tasks, todayLoadState = LoadState.CONTENT)
                    }
                }
        }
    }

    private fun observeCharacter() {
        characterJob?.cancel()
        characterJob = viewModelScope.launch {
            repository.observeCharacter()
                .catch {
                    emitMessage("캐릭터 성장 정보를 불러오지 못했습니다.")
                }
                .collect { character ->
                    _uiState.update { state -> state.copy(character = character) }
                }
        }
    }

    private fun observeHistory() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            _uiState.update { it.copy(historyLoadState = LoadState.LOADING) }
            repository.observeHistory()
                .catch {
                    emitMessage("기록을 불러오지 못했습니다.")
                    _uiState.update { state -> state.copy(historyLoadState = LoadState.ERROR) }
                }
                .collect { history ->
                    _uiState.update { state ->
                        state.copy(history = history, historyLoadState = LoadState.CONTENT)
                    }
                }
        }
    }

    private fun observeSettings() {
        settingsJob?.cancel()
        settingsJob = viewModelScope.launch {
            _uiState.update { it.copy(settingsLoadState = LoadState.LOADING) }
            repository.observeSettings()
                .catch {
                    emitMessage("설정을 불러오지 못했습니다.")
                    _uiState.update { state -> state.copy(settingsLoadState = LoadState.ERROR) }
                }
                .collect { settings ->
                    applySettings(settings)
                }
        }
    }

    private fun applySettings(settings: com.dayquest.domain.DayQuestSettings) {
        _uiState.update {
            it.copy(
                notificationEnabled = settings.notificationEnabled,
                darkModeEnabled = settings.darkModeEnabled,
                reminderTime = settings.reminderTime,
                settingsLoadState = LoadState.CONTENT,
            )
        }
    }

    private fun launchMutation(
        successMessage: String? = null,
        errorMessage: String,
        refreshReminder: Boolean = false,
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            runCatching {
                block()
                if (refreshReminder) {
                    reminderManager.refreshSchedule()
                }
            }.onSuccess {
                successMessage?.let(::emitMessage)
            }.onFailure {
                emitMessage(errorMessage)
            }
        }
    }

    private fun emitMessage(message: String) {
        _messages.tryEmit(message)
    }

    private fun QuestTier.toChoice(): QuestTierChoice = when (this) {
        QuestTier.MAIN -> QuestTierChoice.HIGH
        QuestTier.RARE -> QuestTierChoice.NORMAL
        QuestTier.NORMAL -> QuestTierChoice.LOW
    }

    private fun TaskFormUi.normalizedRepeatRule(): String =
        when {
            repeat == CUSTOM_REPEAT_LABEL && customRepeatDays.isNotEmpty() ->
                "$CUSTOM_REPEAT_LABEL:${customRepeatDays.sortedBy { CUSTOM_REPEAT_DAYS.indexOf(it) }.joinToString(",")}"
            repeat == MONTHLY_REPEAT_LABEL ->
                "$MONTHLY_REPEAT_LABEL:${monthlyDay.toIntOrNull()?.coerceIn(1, 31) ?: 1}"
            else -> repeat
        }

    private fun String.toQuickRepeatRule(): String? = when (this) {
        QUICK_REPEAT_DAILY -> "매일"
        QUICK_REPEAT_WEEKDAY -> "주중"
        QUICK_REPEAT_WEEKEND -> "주말"
        else -> null
    }

    private fun questAchievementMessage(taskId: Long): String? {
        val state = _uiState.value
        val task = state.tasks.firstOrNull { it.id == taskId } ?: return null
        if (task.isCompleted) return null

        val today = LocalDate.now().toString()
        val todayTasks = state.tasks.filter { it.dailyDate == today && !it.isSkippedToday }
        if (todayTasks.isEmpty()) return null

        val completedAfter = todayTasks.count { it.isCompleted } + 1
        val importantTasks = todayTasks.filter { it.tier == QuestTier.MAIN || it.tier == QuestTier.RARE }

        return when {
            completedAfter == todayTasks.size -> "오늘 전체 퀘스트를 달성했습니다."
            task.id in importantTasks.map { it.id } && importantTasks.none { it.isCompleted } ->
                "중요 의뢰 퀘스트를 달성했습니다."
            completedAfter == 1 -> "첫 완료 퀘스트를 달성했습니다."
            else -> null
        }
    }

    private fun completionSuccessMessage(taskId: Long): String? {
        val task = _uiState.value.tasks.firstOrNull { it.id == taskId } ?: return null
        if (task.isCompleted) return null

        val rewardXp = CharacterGrowthRules.rewardXpForTier(task.tier.name)
        val rewardMessage = "보상 +${rewardXp} 성장 XP · ${statLabel(CharacterGrowthRules.statForCategory(task.categoryLabel))} +1"
        val questMessage = questAchievementMessage(taskId)
        return listOfNotNull(rewardMessage, questMessage).joinToString(" · ")
    }

    private fun statLabel(statType: String): String = when (statType) {
        CharacterGrowthRules.STAT_FOCUS -> "집중"
        CharacterGrowthRules.STAT_VITALITY -> "체력"
        CharacterGrowthRules.STAT_INSIGHT -> "통찰"
        else -> "균형"
    }

    private fun TodayTaskUi.repeatBaseLabel(): String =
        repeatLabel?.substringBefore(":").orEmpty().ifBlank { "매일" }

    private fun TodayTaskUi.monthlyDay(): String =
        repeatLabel
            ?.takeIf { it.startsWith("$MONTHLY_REPEAT_LABEL:") }
            ?.substringAfter(":")
            ?.toIntOrNull()
            ?.coerceIn(1, 31)
            ?.toString()
            ?: "1"

    private fun TodayTaskUi.customRepeatDays(): Set<String> =
        repeatLabel
            ?.takeIf { it.startsWith("$CUSTOM_REPEAT_LABEL:") }
            ?.substringAfter(":")
            ?.split(",")
            ?.filter { it in CUSTOM_REPEAT_DAYS }
            ?.toSet()
            .orEmpty()

    companion object {
        const val CUSTOM_REPEAT_LABEL = "커스텀"
        const val MONTHLY_REPEAT_LABEL = "매달"
        const val QUICK_REPEAT_ONCE = "단발성"
        const val QUICK_REPEAT_DAILY = "매일"
        const val QUICK_REPEAT_WEEKDAY = "주중"
        const val QUICK_REPEAT_WEEKEND = "주말"
        val CUSTOM_REPEAT_DAYS = listOf("월", "화", "수", "목", "금", "토", "일")
        val DEFAULT_CUSTOM_REPEAT_DAYS = setOf("월", "수", "금")
        val QUICK_REPEAT_OPTIONS = listOf(
            QUICK_REPEAT_ONCE,
            QUICK_REPEAT_DAILY,
            QUICK_REPEAT_WEEKDAY,
            QUICK_REPEAT_WEEKEND,
        )
    }
}
