package com.dayquest.app.domain.usecase.today

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.core.model.QuestType
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.dao.QuestDao
import com.dayquest.app.data.local.entity.QuestEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SyncDailyQuestsUseCase @Inject constructor(
    private val dailyItemDao: DailyItemDao,
    private val questDao: QuestDao
) {
    suspend fun ensureQuestMeta(date: LocalDate) {
        val dateKey = date.toDateKey()
        val totalCount = dailyItemDao.countByDate(dateKey)
        val importantCount = dailyItemDao.countImportantByDateAndStatus(dateKey, DailyItemStatus.TODO) +
            dailyItemDao.countImportantByDateAndStatus(dateKey, DailyItemStatus.DONE) +
            dailyItemDao.countImportantByDateAndStatus(dateKey, DailyItemStatus.DEFERRED) +
            dailyItemDao.countImportantByDateAndStatus(dateKey, DailyItemStatus.SKIPPED)

        upsertQuestMeta(dateKey, QuestType.COMPLETE_ONE, targetCount = if (totalCount > 0) 1 else 0, title = "오늘 1개 완료")
        upsertQuestMeta(dateKey, QuestType.COMPLETE_ALL, targetCount = totalCount, title = "오늘 모두 완료")

        if (importantCount > 0) {
            upsertQuestMeta(dateKey, QuestType.COMPLETE_IMPORTANT, targetCount = importantCount, title = "중요 작업 완료")
        }
    }

    suspend fun syncProgress(date: LocalDate, nowEpochMillis: Long) {
        val dateKey = date.toDateKey()
        val doneCount = dailyItemDao.countByDateAndStatus(dateKey, DailyItemStatus.DONE)
        val importantDoneCount = dailyItemDao.countImportantByDateAndStatus(dateKey, DailyItemStatus.DONE)

        updateQuestProgress(dateKey, QuestType.COMPLETE_ONE, doneCount, nowEpochMillis)
        updateQuestProgress(dateKey, QuestType.COMPLETE_ALL, doneCount, nowEpochMillis)
        updateQuestProgress(dateKey, QuestType.COMPLETE_IMPORTANT, importantDoneCount, nowEpochMillis)
    }

    private suspend fun upsertQuestMeta(dateKey: String, questType: QuestType, targetCount: Int, title: String) {
        val existing = questDao.getByType(dateKey, questType)
        if (existing == null) {
            questDao.insertAll(
                listOf(
                    QuestEntity(
                        dateKey = dateKey,
                        questType = questType,
                        title = title,
                        targetCount = targetCount
                    )
                )
            )
            return
        }

        if (existing.targetCount != targetCount || existing.title != title) {
            questDao.update(existing.copy(title = title, targetCount = targetCount))
        }
    }

    private suspend fun updateQuestProgress(
        dateKey: String,
        questType: QuestType,
        progressSourceCount: Int,
        nowEpochMillis: Long
    ) {
        val quest = questDao.getByType(dateKey, questType) ?: return
        val safeTarget = quest.targetCount.coerceAtLeast(0)
        val progress = progressSourceCount.coerceIn(0, safeTarget)
        val achieved = safeTarget > 0 && progress >= safeTarget
        val achievedAt = if (achieved) quest.achievedAtEpochMillis ?: nowEpochMillis else null

        if (
            quest.progressCount != progress ||
            quest.achieved != achieved ||
            quest.achievedAtEpochMillis != achievedAt
        ) {
            questDao.update(
                quest.copy(
                    progressCount = progress,
                    achieved = achieved,
                    achievedAtEpochMillis = achievedAt
                )
            )
        }
    }

    private fun LocalDate.toDateKey(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)
}
