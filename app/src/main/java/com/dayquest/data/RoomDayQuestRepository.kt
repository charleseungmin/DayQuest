package com.dayquest.data

import com.dayquest.app.ui.CharacterGrowthUi
import com.dayquest.app.ui.HistoryDayUi
import com.dayquest.app.ui.QuestTier
import com.dayquest.app.ui.TodayTaskUi
import com.dayquest.domain.CharacterGrowthRules
import com.dayquest.domain.DayQuestRepository
import com.dayquest.domain.DayQuestSettings
import com.dayquest.domain.QuestTierChoice
import com.dayquest.domain.ReminderPlan
import com.dayquest.domain.TaskDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RoomDayQuestRepository(
    private val questDao: QuestDao,
    private val settingsDao: SettingsDao,
    private val completionLogDao: CompletionLogDao,
    private val dailyItemDao: DailyItemDao,
    private val dailyQuestDao: DailyQuestDao,
    private val characterProgressDao: CharacterProgressDao,
    private val characterRewardLogDao: CharacterRewardLogDao,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : DayQuestRepository {

    override fun observeTasks(): Flow<List<TodayTaskUi>> {
        val today = LocalDate.now(zoneId).format(DateTimeFormatter.ISO_LOCAL_DATE)
        return combine(questDao.observeAll(), dailyItemDao.observeByDate(today)) { entities, dailyItems ->
            val itemsByTaskId = dailyItems.associateBy { it.taskId }
            entities.map { it.toUi(itemsByTaskId[it.id]) }
        }
    }

    override fun observeSettings(): Flow<DayQuestSettings> = settingsDao.observe().map { settings ->
        settings?.toDomain() ?: defaultSettings()
    }

    override fun observeCharacter(): Flow<CharacterGrowthUi> = characterProgressDao.observe().map { character ->
        (character ?: defaultCharacterEntity()).toUi()
    }

    override fun observeHistory(): Flow<List<HistoryDayUi>> =
        combine(questDao.observeAll(), completionLogDao.observeAll(), dailyItemDao.observeAll()) { quests, logs, dailyItems ->
            val questsById = quests.associateBy { it.id }
            val logsByDate = logs.groupBy { it.completedDate }
            val dailyItemsByDate = dailyItems.groupBy { it.date }
            val today = LocalDate.now(zoneId)

            (0L until HISTORY_WINDOW_DAYS).map { offset ->
                val date = today.minusDays(offset)
                val rawDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val entries = logsByDate[rawDate].orEmpty()
                val rawDailyItems = dailyItemsByDate[rawDate].orEmpty()
                val countedDailyItems = rawDailyItems.filter { it.status == STATUS_TODO || it.status == STATUS_DONE }
                val totalCount = if (rawDailyItems.isNotEmpty()) {
                    countedDailyItems.size
                } else {
                    quests.count { it.isActiveOn(date) }
                }
                val categorySummary = entries
                    .mapNotNull { log -> questsById[log.taskId]?.categoryLabel }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .take(3)
                    .joinToString(" · ") { "${it.key} ${it.value}" }
                    .ifBlank { null }
                HistoryDayUi(
                    dateLabel = formatDate(rawDate),
                    completedCount = entries.size,
                    totalCount = totalCount,
                    earnedXp = entries.sumOf { it.earnedXp },
                    note = when {
                        totalCount == 0 -> "등록된 의뢰가 없습니다"
                        entries.size >= totalCount -> "전 퀘스트 완료"
                        entries.isNotEmpty() -> "진행 기록 보관"
                        else -> "완료 기록 없음"
                    },
                    categorySummary = categorySummary,
                )
            }
        }

    override suspend fun addTask(draft: TaskDraft) {
        val now = System.currentTimeMillis()
        val taskId = questDao.insert(
            QuestEntity(
                title = draft.title,
                memo = draft.memo,
                categoryLabel = draft.categoryLabel,
                timeLabel = draft.timeLabel,
                repeatRule = draft.repeatRule,
                startDate = draft.startDate,
                endDate = draft.endDate,
                isActive = true,
                isCompleted = false,
                isOverdue = false,
                tier = draft.tier.toUiTier().name,
                reminderEnabled = draft.reminderEnabled,
                skippedDate = null,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
        questDao.getById(taskId)?.let { syncTodayItemForTask(it) }
        syncDailyQuests()
    }

    override suspend fun updateTask(taskId: Long, draft: TaskDraft) {
        val existing = questDao.getById(taskId) ?: return
        questDao.update(
            existing.copy(
                title = draft.title,
                memo = draft.memo,
                categoryLabel = draft.categoryLabel,
                repeatRule = draft.repeatRule,
                startDate = draft.startDate,
                endDate = draft.endDate,
                timeLabel = draft.timeLabel,
                tier = draft.tier.toUiTier().name,
                reminderEnabled = draft.reminderEnabled,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
        questDao.getById(taskId)?.let { syncTodayItemForTask(it) }
        syncDailyQuests()
    }

    override suspend fun toggleTask(taskId: Long, nowEpochMillis: Long) {
        val existing = questDao.getById(taskId) ?: return
        val date = localDate(nowEpochMillis)
        val currentItem = dailyItemDao.get(taskId, date)
        val nextDone = currentItem?.status != STATUS_DONE
        val updated = existing.copy(
            isCompleted = nextDone,
            isOverdue = false,
            updatedAtEpochMillis = nowEpochMillis,
        )
        questDao.update(updated)
        dailyItemDao.upsert(
            DailyItemEntity(
                taskId = taskId,
                date = date,
                status = if (nextDone) STATUS_DONE else STATUS_TODO,
                source = currentItem?.source ?: SOURCE_SCHEDULED,
                doneAtEpochMillis = if (nextDone) nowEpochMillis else null,
                updatedAtEpochMillis = nowEpochMillis,
            ),
        )

        if (nextDone) {
            val rewardXp = CharacterGrowthRules.rewardXpForTier(updated.tier)
            completionLogDao.upsert(
                CompletionLogEntity(
                    taskId = taskId,
                    completedDate = date,
                    earnedXp = rewardXp,
                ),
            )
            characterRewardLogDao.upsert(
                CharacterRewardLogEntity(
                    taskId = taskId,
                    date = date,
                    rewardXp = rewardXp,
                    statType = CharacterGrowthRules.statForCategory(updated.categoryLabel),
                    statPoints = 1,
                    createdAtEpochMillis = nowEpochMillis,
                ),
            )
        } else {
            completionLogDao.delete(taskId, date)
            characterRewardLogDao.delete(taskId, date)
        }
        rebuildCharacterProgress()
        syncDailyQuests()
    }

    @Suppress("UNUSED_PARAMETER")
    override suspend fun snoozeTask(taskId: Long, nowEpochMillis: Long, minutes: Long) {
        val existing = questDao.getById(taskId) ?: return
        val today = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId).toLocalDate()
        val todayRaw = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tomorrowRaw = today.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val currentItem = dailyItemDao.get(taskId, todayRaw)
        dailyItemDao.upsert(
            DailyItemEntity(
                taskId = taskId,
                date = todayRaw,
                status = STATUS_DEFERRED,
                source = currentItem?.source ?: SOURCE_SCHEDULED,
                doneAtEpochMillis = null,
                updatedAtEpochMillis = nowEpochMillis,
            ),
        )
        val tomorrowItem = dailyItemDao.get(taskId, tomorrowRaw)
        if (tomorrowItem == null || tomorrowItem.status != STATUS_DONE) {
            dailyItemDao.upsert(
                DailyItemEntity(
                    taskId = taskId,
                    date = tomorrowRaw,
                    status = tomorrowItem?.status ?: STATUS_TODO,
                    source = SOURCE_DEFERRED,
                    doneAtEpochMillis = tomorrowItem?.doneAtEpochMillis,
                    updatedAtEpochMillis = nowEpochMillis,
                ),
            )
        }
        questDao.update(
            existing.copy(
                isCompleted = false,
                isOverdue = true,
                skippedDate = null,
                updatedAtEpochMillis = nowEpochMillis,
            ),
        )
        syncDailyQuests(today)
        syncDailyQuests(today.plusDays(1))
    }

    override suspend fun skipTask(taskId: Long, nowEpochMillis: Long) {
        val existing = questDao.getById(taskId) ?: return
        val date = localDate(nowEpochMillis)
        val currentItem = dailyItemDao.get(taskId, date)
        dailyItemDao.upsert(
            DailyItemEntity(
                taskId = taskId,
                date = date,
                status = STATUS_SKIPPED,
                source = currentItem?.source ?: SOURCE_SCHEDULED,
                doneAtEpochMillis = null,
                updatedAtEpochMillis = nowEpochMillis,
            ),
        )
        questDao.update(
            existing.copy(
                isCompleted = false,
                skippedDate = date,
                isOverdue = false,
                updatedAtEpochMillis = nowEpochMillis,
            ),
        )
        syncDailyQuests()
    }

    override suspend fun deleteTask(taskId: Long) {
        dailyItemDao.deleteFutureIfNotDone(taskId, LocalDate.now(zoneId).format(DateTimeFormatter.ISO_LOCAL_DATE), STATUS_DONE)
        questDao.deleteById(taskId)
        syncDailyQuests()
    }

    override suspend fun updateNotificationEnabled(enabled: Boolean) {
        settingsDao.upsert(currentSettings().copy(notificationEnabled = enabled))
    }

    override suspend fun updateDarkMode(enabled: Boolean) {
        settingsDao.upsert(currentSettings().copy(darkModeEnabled = enabled))
    }

    override suspend fun updateReminderTime(time: String) {
        settingsDao.upsert(currentSettings().copy(reminderTime = time))
    }

    override suspend fun resetAll() {
        questDao.clear()
        completionLogDao.clear()
        dailyItemDao.clear()
        dailyQuestDao.clear()
        characterRewardLogDao.clear()
        characterProgressDao.upsert(defaultCharacterEntity())
        settingsDao.upsert(defaultSettingsEntity())
        syncTodayItems()
    }

    override suspend fun loadReminderPlan(): ReminderPlan {
        val settings = currentSettings()
        syncTodayItems()
        val today = LocalDate.now(zoneId)
        val todayRaw = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dailyItemsByTaskId = dailyItemDao.getByDate(todayRaw).associateBy { it.taskId }
        val tasks = questDao.getAll()
            .mapNotNull { entity ->
                val dailyItem = dailyItemsByTaskId[entity.id]
                if (entity.isActiveOn(today) || dailyItem?.source == SOURCE_DEFERRED || dailyItem?.status == STATUS_DONE) {
                    entity.toUi(dailyItem)
                } else {
                    null
                }
            }
            .filter { !it.isSkippedToday && it.reminderEnabled }
        return ReminderPlan(
            enabled = settings.notificationEnabled,
            reminderTime = settings.reminderTime,
            quests = tasks,
        )
    }

    suspend fun seedIfEmpty() {
        if (settingsDao.get() == null) {
            settingsDao.upsert(defaultSettingsEntity())
        }
        if (characterProgressDao.get() == null) {
            characterProgressDao.upsert(defaultCharacterEntity())
        }
        syncTodayItems()
    }

    private suspend fun currentSettings(): AppSettingsEntity = settingsDao.get() ?: defaultSettingsEntity()

    private suspend fun syncTodayItems() {
        val today = LocalDate.now(zoneId)
        questDao.getAll().forEach { task -> syncTodayItemForTask(task, today) }
        syncDailyQuests(today)
    }

    private suspend fun syncTodayItemForTask(task: QuestEntity, date: LocalDate = LocalDate.now(zoneId)) {
        val rawDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val existing = dailyItemDao.get(task.id, rawDate)
        if (task.isActiveOn(date)) {
            if (existing == null) {
                dailyItemDao.upsert(
                    DailyItemEntity(
                        taskId = task.id,
                        date = rawDate,
                        status = STATUS_TODO,
                        source = SOURCE_SCHEDULED,
                        doneAtEpochMillis = null,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    ),
                )
            }
        } else if (existing != null && existing.status != STATUS_DONE) {
            dailyItemDao.deleteIfNotDone(task.id, rawDate, STATUS_DONE)
        }
    }

    private suspend fun syncDailyQuests(date: LocalDate = LocalDate.now(zoneId)) {
        val rawDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tasksById = questDao.getAll().associateBy { it.id }
        val dailyItems = dailyItemDao.getByDate(rawDate)
            .filter { it.status == STATUS_TODO || it.status == STATUS_DONE }
            .filter { tasksById[it.taskId] != null }
        val completedCount = dailyItems.count { it.status == STATUS_DONE }
        val importantItems = dailyItems.filter { item ->
            val tier = tasksById[item.taskId]?.tier
            tier == QuestTier.MAIN.name || tier == QuestTier.RARE.name
        }
        val now = System.currentTimeMillis()
        val records = buildList {
            add(
                dailyQuest(
                    date = rawDate,
                    type = QUEST_META_ONE,
                    current = completedCount.coerceAtMost(1),
                    target = 1,
                    enabled = dailyItems.isNotEmpty(),
                    achieved = dailyItems.isNotEmpty() && completedCount >= 1,
                    now = now,
                ),
            )
            add(
                dailyQuest(
                    date = rawDate,
                    type = QUEST_META_ALL,
                    current = completedCount,
                    target = dailyItems.size,
                    enabled = dailyItems.isNotEmpty(),
                    achieved = dailyItems.isNotEmpty() && completedCount == dailyItems.size,
                    now = now,
                ),
            )
            if (importantItems.isNotEmpty()) {
                add(
                    dailyQuest(
                        date = rawDate,
                        type = QUEST_IMPORTANT_ONE,
                        current = importantItems.count { it.status == STATUS_DONE }.coerceAtMost(1),
                        target = 1,
                        enabled = true,
                        achieved = importantItems.any { it.status == STATUS_DONE },
                        now = now,
                    ),
                )
            }
        }
        dailyQuestDao.replaceForDate(rawDate, records)
    }

    private fun dailyQuest(
        date: String,
        type: String,
        current: Int,
        target: Int,
        enabled: Boolean,
        achieved: Boolean,
        now: Long,
    ): DailyQuestEntity = DailyQuestEntity(
        date = date,
        type = type,
        status = when {
            !enabled -> STATUS_BLOCKED
            achieved -> STATUS_DONE
            else -> STATUS_TODO
        },
        progressCurrent = current,
        progressTarget = target,
        achievedAtEpochMillis = if (achieved) now else null,
        updatedAtEpochMillis = now,
    )

    private fun QuestEntity.toUi(dailyItem: DailyItemEntity? = null): TodayTaskUi = TodayTaskUi(
        id = id,
        title = title,
        memo = memo,
        categoryLabel = categoryLabel,
        timeLabel = timeLabel,
        repeatLabel = repeatRule,
        startDate = startDate,
        endDate = endDate,
        isCompleted = dailyItem?.status == STATUS_DONE || (dailyItem == null && isCompleted),
        isOverdue = isOverdue || dailyItem?.status == STATUS_DEFERRED,
        isSkippedToday = dailyItem?.status in setOf(STATUS_SKIPPED, STATUS_DEFERRED) ||
            skippedDate == LocalDate.now(zoneId).format(DateTimeFormatter.ISO_LOCAL_DATE),
        tier = runCatching { QuestTier.valueOf(tier) }.getOrDefault(QuestTier.NORMAL),
        reminderEnabled = reminderEnabled,
        dailyDate = dailyItem?.date,
        dailySource = dailyItem?.source,
    )

    private fun QuestEntity.isActiveOn(date: LocalDate): Boolean {
        if (!isActive) return false

        val starts = startDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val ends = endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (starts != null && date.isBefore(starts)) return false
        if (ends != null && date.isAfter(ends)) return false

        val repeat = repeatRule.orEmpty()
        return when {
            repeat.startsWith("$CUSTOM_REPEAT_LABEL:") -> {
                val selectedDays = repeat.substringAfter(":")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .toSet()
                selectedDays.isEmpty() || date.koreanDayLabel() in selectedDays
            }
            repeat.startsWith("$MONTHLY_REPEAT_LABEL:") ->
                repeat.substringAfter(":").toIntOrNull()?.coerceIn(1, 31) == date.dayOfMonth
            repeat == "주중" -> date.dayOfWeek.value in 1..5
            repeat == "주말" -> date.dayOfWeek.value in 6..7
            else -> true
        }
    }

    private fun AppSettingsEntity.toDomain(): DayQuestSettings = DayQuestSettings(
        notificationEnabled = notificationEnabled,
        darkModeEnabled = darkModeEnabled,
        reminderTime = reminderTime,
    )

    private suspend fun rebuildCharacterProgress() {
        val logs = characterRewardLogDao.getAll()
        val totalExp = logs.sumOf { it.rewardXp }
        val levelProgress = CharacterGrowthRules.progressFor(totalExp)
        val stats = logs.groupBy { it.statType }
            .mapValues { (_, entries) -> entries.sumOf { it.statPoints } }

        characterProgressDao.upsert(
            CharacterProgressEntity(
                level = levelProgress.level,
                expInLevel = levelProgress.expInLevel,
                nextLevelExp = levelProgress.nextLevelExp,
                totalExp = levelProgress.totalExp,
                trainingPoints = logs.sumOf { it.statPoints },
                focus = stats[CharacterGrowthRules.STAT_FOCUS].orZero(),
                vitality = stats[CharacterGrowthRules.STAT_VITALITY].orZero(),
                insight = stats[CharacterGrowthRules.STAT_INSIGHT].orZero(),
                balance = stats[CharacterGrowthRules.STAT_BALANCE].orZero(),
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    private fun CharacterProgressEntity.toUi(): CharacterGrowthUi = CharacterGrowthUi(
        level = level,
        title = characterTitle(level),
        expInLevel = expInLevel,
        nextLevelExp = nextLevelExp,
        totalExp = totalExp,
        trainingPoints = trainingPoints,
        focus = focus,
        vitality = vitality,
        insight = insight,
        balance = balance,
    )

    private fun TodayTaskUi.toEntity(): QuestEntity = QuestEntity(
        id = id,
        title = title,
        memo = memo,
        categoryLabel = categoryLabel,
        timeLabel = timeLabel,
        repeatRule = repeatLabel,
        startDate = startDate,
        endDate = endDate,
        isActive = true,
        isCompleted = isCompleted,
        isOverdue = isOverdue,
        tier = tier.name,
        reminderEnabled = reminderEnabled,
        skippedDate = null,
        createdAtEpochMillis = System.currentTimeMillis(),
        updatedAtEpochMillis = System.currentTimeMillis(),
    )

    private fun defaultSettingsEntity() = AppSettingsEntity(
        notificationEnabled = true,
        darkModeEnabled = false,
        reminderTime = "21:00",
    )

    private fun defaultSettings() = DayQuestSettings(
        notificationEnabled = true,
        darkModeEnabled = false,
        reminderTime = "21:00",
    )

    private fun defaultCharacterEntity() = CharacterProgressEntity(
        level = 1,
        expInLevel = 0,
        nextLevelExp = 100,
        totalExp = 0,
        trainingPoints = 0,
        focus = 0,
        vitality = 0,
        insight = 0,
        balance = 0,
        updatedAtEpochMillis = System.currentTimeMillis(),
    )

    private fun seedTasks() = listOf(
        TodayTaskUi(1, "기획서 초안 작성", "핵심 문단부터 정리", "업무", "09:00", "매일", null, null, false, true, false, QuestTier.MAIN, true),
        TodayTaskUi(2, "점심 운동", null, "건강", "12:30", "주중", null, null, false, false, false, QuestTier.NORMAL, true),
        TodayTaskUi(3, "코드 리뷰", "오픈 코멘트 먼저 확인", "업무", "18:00", "주중", null, null, false, false, false, QuestTier.RARE, true),
        TodayTaskUi(4, "아침 일기 쓰기", null, "개인", "07:00", "매일", null, null, true, false, false, QuestTier.NORMAL, false),
    )

    private fun xpForTier(tier: String): Int = when (tier) {
        QuestTier.MAIN.name -> 60
        QuestTier.RARE.name -> 40
        else -> 25
    }

    private fun characterTitle(level: Int): String = when {
        level >= 10 -> "전설의 길드 마스터"
        level >= 7 -> "숙련 모험가"
        level >= 4 -> "성장 중인 모험가"
        else -> "초보 모험가"
    }

    private fun Int?.orZero(): Int = this ?: 0

    private fun formatDate(raw: String): String = runCatching {
        val date = java.time.LocalDate.parse(raw)
        val dayOfWeek = when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "월요일"
            java.time.DayOfWeek.TUESDAY -> "화요일"
            java.time.DayOfWeek.WEDNESDAY -> "수요일"
            java.time.DayOfWeek.THURSDAY -> "목요일"
            java.time.DayOfWeek.FRIDAY -> "금요일"
            java.time.DayOfWeek.SATURDAY -> "토요일"
            java.time.DayOfWeek.SUNDAY -> "일요일"
        }
        "${date.monthValue}월 ${date.dayOfMonth}일 $dayOfWeek"
    }.getOrDefault(raw)

    private fun QuestTierChoice.toUiTier(): QuestTier = when (this) {
        QuestTierChoice.HIGH -> QuestTier.MAIN
        QuestTierChoice.NORMAL -> QuestTier.RARE
        QuestTierChoice.LOW -> QuestTier.NORMAL
    }

    private fun localDate(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)

    private fun LocalDate.koreanDayLabel(): String = when (dayOfWeek.value) {
        1 -> "월"
        2 -> "화"
        3 -> "수"
        4 -> "목"
        5 -> "금"
        6 -> "토"
        else -> "일"
    }

    private companion object {
        const val HISTORY_WINDOW_DAYS = 30L
        const val CUSTOM_REPEAT_LABEL = "커스텀"
        const val MONTHLY_REPEAT_LABEL = "매달"
        const val STATUS_TODO = "TODO"
        const val STATUS_DONE = "DONE"
        const val STATUS_DEFERRED = "DEFERRED"
        const val STATUS_SKIPPED = "SKIPPED"
        const val STATUS_BLOCKED = "BLOCKED"
        const val SOURCE_SCHEDULED = "SCHEDULED"
        const val SOURCE_DEFERRED = "DEFERRED"
        const val QUEST_META_ONE = "META_ONE"
        const val QUEST_META_ALL = "META_ALL"
        const val QUEST_IMPORTANT_ONE = "IMPORTANT_ONE"
    }
}
