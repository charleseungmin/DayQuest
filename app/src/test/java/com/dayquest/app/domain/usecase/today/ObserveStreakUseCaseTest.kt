package com.dayquest.app.domain.usecase.today

import com.dayquest.app.data.local.dao.StreakDao
import com.dayquest.app.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveStreakUseCaseTest {

    @Test
    fun maps_streak_entity_to_ui_values() = runBlocking {
        val dao = FakeObserveStreakDao(
            StreakEntity(
                currentStreak = 5,
                bestStreak = 9,
                lastAchievedDateKey = "2026-02-18",
                updatedAtEpochMillis = 1000L
            )
        )

        val emitted = ObserveStreakUseCase(dao).invoke().first()

        assertEquals(5, emitted.currentStreak)
        assertEquals(9, emitted.bestStreak)
    }

    @Test
    fun returns_zero_values_when_streak_is_missing() = runBlocking {
        val dao = FakeObserveStreakDao(null)

        val emitted = ObserveStreakUseCase(dao).invoke().first()

        assertEquals(0, emitted.currentStreak)
        assertEquals(0, emitted.bestStreak)
    }
}

private class FakeObserveStreakDao(
    private val current: StreakEntity?
) : StreakDao {
    override fun observe(): Flow<StreakEntity?> = flowOf(current)

    override suspend fun get(): StreakEntity? = current

    override suspend fun upsert(streak: StreakEntity) = Unit

    override suspend fun update(streak: StreakEntity) = Unit
}
