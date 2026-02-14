package com.dayquest.app.domain.usecase.today

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.core.model.QuestType
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.dao.QuestDao
import com.dayquest.app.data.local.entity.DailyItemEntity
import com.dayquest.app.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class SyncDailyQuestsUseCaseTest {

    @Test
    fun ensure_quest_meta_creates_complete_one_and_all() = runBlocking {
        val dailyDao = FakeDailyItemDaoForQuestSync(
            items = listOf(
                DailyItemEntity(id = 1L, dateKey = "2026-02-14", taskId = 1L, status = DailyItemStatus.TODO, createdAtEpochMillis = 0L),
                DailyItemEntity(id = 2L, dateKey = "2026-02-14", taskId = 2L, status = DailyItemStatus.TODO, createdAtEpochMillis = 0L)
            ),
            importantTaskIds = setOf(2L)
        )
        val questDao = FakeQuestDao()
        val useCase = SyncDailyQuestsUseCase(dailyDao, questDao)

        useCase.ensureQuestMeta(LocalDate.of(2026, 2, 14))

        val completeOne = questDao.getByType("2026-02-14", QuestType.COMPLETE_ONE)
        val completeAll = questDao.getByType("2026-02-14", QuestType.COMPLETE_ALL)
        val important = questDao.getByType("2026-02-14", QuestType.COMPLETE_IMPORTANT)

        assertNotNull(completeOne)
        assertNotNull(completeAll)
        assertNotNull(important)
        assertEquals(1, completeOne?.targetCount)
        assertEquals(2, completeAll?.targetCount)
        assertEquals(1, important?.targetCount)
    }

    @Test
    fun sync_progress_updates_achieved_state() = runBlocking {
        val dailyDao = FakeDailyItemDaoForQuestSync(
            items = listOf(
                DailyItemEntity(id = 1L, dateKey = "2026-02-14", taskId = 1L, status = DailyItemStatus.DONE, createdAtEpochMillis = 0L),
                DailyItemEntity(id = 2L, dateKey = "2026-02-14", taskId = 2L, status = DailyItemStatus.DONE, createdAtEpochMillis = 0L)
            ),
            importantTaskIds = setOf(2L)
        )
        val questDao = FakeQuestDao(
            mutableListOf(
                QuestEntity(id = 1L, dateKey = "2026-02-14", questType = QuestType.COMPLETE_ONE, title = "오늘 1개 완료", targetCount = 1),
                QuestEntity(id = 2L, dateKey = "2026-02-14", questType = QuestType.COMPLETE_ALL, title = "오늘 모두 완료", targetCount = 2),
                QuestEntity(id = 3L, dateKey = "2026-02-14", questType = QuestType.COMPLETE_IMPORTANT, title = "중요 작업 완료", targetCount = 1)
            )
        )
        val useCase = SyncDailyQuestsUseCase(dailyDao, questDao)

        useCase.syncProgress(LocalDate.of(2026, 2, 14), nowEpochMillis = 999L)

        assertEquals(1, questDao.getByType("2026-02-14", QuestType.COMPLETE_ONE)?.progressCount)
        assertEquals(true, questDao.getByType("2026-02-14", QuestType.COMPLETE_ONE)?.achieved)

        assertEquals(2, questDao.getByType("2026-02-14", QuestType.COMPLETE_ALL)?.progressCount)
        assertEquals(true, questDao.getByType("2026-02-14", QuestType.COMPLETE_ALL)?.achieved)

        assertEquals(1, questDao.getByType("2026-02-14", QuestType.COMPLETE_IMPORTANT)?.progressCount)
        assertEquals(true, questDao.getByType("2026-02-14", QuestType.COMPLETE_IMPORTANT)?.achieved)
        assertEquals(999L, questDao.getByType("2026-02-14", QuestType.COMPLETE_IMPORTANT)?.achievedAtEpochMillis)
    }
}

private class FakeDailyItemDaoForQuestSync(
    private val items: List<DailyItemEntity>,
    private val importantTaskIds: Set<Long>
) : DailyItemDao {
    override fun observeByDate(dateKey: String): Flow<List<DailyItemEntity>> = flowOf(items.filter { it.dateKey == dateKey })
    override suspend fun insert(item: DailyItemEntity): Long = item.id
    override suspend fun insertAll(items: List<DailyItemEntity>): List<Long> = items.map { it.id }
    override suspend fun update(item: DailyItemEntity) = Unit
    override suspend fun getById(id: Long): DailyItemEntity? = items.firstOrNull { it.id == id }
    override suspend fun updateState(id: Long, status: DailyItemStatus, completedAt: Long?, deferredToDateKey: String?) = Unit
    override suspend fun countByDate(dateKey: String): Int = items.count { it.dateKey == dateKey }

    override suspend fun countByDateAndStatus(dateKey: String, status: DailyItemStatus): Int =
        items.count { it.dateKey == dateKey && it.status == status }

    override suspend fun countImportantByDateAndStatus(dateKey: String, status: DailyItemStatus): Int =
        items.count { it.dateKey == dateKey && it.status == status && importantTaskIds.contains(it.taskId) }
}

private class FakeQuestDao(
    private val quests: MutableList<QuestEntity> = mutableListOf()
) : QuestDao {
    override fun observeByDate(dateKey: String): Flow<List<QuestEntity>> = flowOf(quests.filter { it.dateKey == dateKey })

    override suspend fun insertAll(quests: List<QuestEntity>) {
        var seq = (this.quests.maxOfOrNull { it.id } ?: 0L) + 1L
        quests.forEach {
            val existingIdx = this.quests.indexOfFirst { q -> q.dateKey == it.dateKey && q.questType == it.questType }
            if (existingIdx >= 0) {
                this.quests[existingIdx] = it.copy(id = this.quests[existingIdx].id)
            } else {
                this.quests += it.copy(id = if (it.id > 0L) it.id else seq++)
            }
        }
    }

    override suspend fun update(quest: QuestEntity) {
        val idx = quests.indexOfFirst { it.id == quest.id }
        if (idx >= 0) quests[idx] = quest
    }

    override suspend fun getByType(dateKey: String, questType: QuestType): QuestEntity? =
        quests.firstOrNull { it.dateKey == dateKey && it.questType == questType }
}
