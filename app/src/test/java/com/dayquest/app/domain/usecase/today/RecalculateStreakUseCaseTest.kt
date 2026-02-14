package com.dayquest.app.domain.usecase.today

import com.dayquest.app.core.model.QuestType
import com.dayquest.app.data.local.dao.QuestDao
import com.dayquest.app.data.local.dao.StreakDao
import com.dayquest.app.data.local.entity.QuestEntity
import com.dayquest.app.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RecalculateStreakUseCaseTest {

    @Test
    fun achieved_today_increments_streak_when_yesterday_was_achieved() = runBlocking {
        val questDao = FakeQuestDaoForStreak(
            achievedDates = setOf("2026-02-14")
        )
        val streakDao = FakeStreakDao(
            StreakEntity(
                currentStreak = 2,
                bestStreak = 3,
                lastAchievedDateKey = "2026-02-13",
                updatedAtEpochMillis = 0L
            )
        )
        val useCase = RecalculateStreakUseCase(questDao, streakDao)

        useCase(LocalDate.of(2026, 2, 14), nowEpochMillis = 1000L)

        val updated = streakDao.current!!
        assertEquals(3, updated.currentStreak)
        assertEquals(3, updated.bestStreak)
        assertEquals("2026-02-14", updated.lastAchievedDateKey)
        assertEquals(1000L, updated.updatedAtEpochMillis)
    }

    @Test
    fun midnight_boundary_resets_streak_when_gap_exists_and_today_not_achieved() = runBlocking {
        val questDao = FakeQuestDaoForStreak(achievedDates = emptySet())
        val streakDao = FakeStreakDao(
            StreakEntity(
                currentStreak = 4,
                bestStreak = 4,
                lastAchievedDateKey = "2026-02-10",
                updatedAtEpochMillis = 0L
            )
        )
        val useCase = RecalculateStreakUseCase(questDao, streakDao)

        useCase(LocalDate.of(2026, 2, 14), nowEpochMillis = 2000L)

        val updated = streakDao.current!!
        assertEquals(0, updated.currentStreak)
        assertEquals(4, updated.bestStreak)
        assertEquals("2026-02-10", updated.lastAchievedDateKey)
        assertEquals(2000L, updated.updatedAtEpochMillis)
    }

    @Test
    fun zero_tasks_or_skipped_only_does_not_increase_streak() = runBlocking {
        val questDao = FakeQuestDaoForStreak(achievedDates = emptySet())
        val streakDao = FakeStreakDao(null)
        val useCase = RecalculateStreakUseCase(questDao, streakDao)

        useCase(LocalDate.of(2026, 2, 14), nowEpochMillis = 3000L)

        val updated = streakDao.current!!
        assertEquals(0, updated.currentStreak)
        assertEquals(0, updated.bestStreak)
        assertNull(updated.lastAchievedDateKey)
    }
}

private class FakeQuestDaoForStreak(
    private val achievedDates: Set<String>
) : QuestDao {
    override fun observeByDate(dateKey: String): Flow<List<QuestEntity>> = flowOf(emptyList())
    override suspend fun insertAll(quests: List<QuestEntity>) = Unit
    override suspend fun update(quest: QuestEntity) = Unit
    override suspend fun getByType(dateKey: String, questType: QuestType): QuestEntity? = null
    override suspend fun countAchievedByDate(dateKey: String): Int = if (achievedDates.contains(dateKey)) 1 else 0
}

private class FakeStreakDao(
    initial: StreakEntity?
) : StreakDao {
    var current: StreakEntity? = initial

    override fun observe(): Flow<StreakEntity?> = flowOf(current)
    override suspend fun get(): StreakEntity? = current
    override suspend fun upsert(streak: StreakEntity) {
        current = streak
    }

    override suspend fun update(streak: StreakEntity) {
        current = streak
    }
}
