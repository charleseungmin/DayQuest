package com.dayquest.app.domain.usecase.today

import com.dayquest.app.data.local.dao.QuestDao
import com.dayquest.app.data.local.dao.StreakDao
import com.dayquest.app.data.local.entity.StreakEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RecalculateStreakUseCase @Inject constructor(
    private val questDao: QuestDao,
    private val streakDao: StreakDao
) {
    suspend operator fun invoke(date: LocalDate, nowEpochMillis: Long) {
        val dateKey = date.toDateKey()
        val yesterdayKey = date.minusDays(1).toDateKey()
        val achievedToday = questDao.countAchievedByDate(dateKey) > 0

        val current = streakDao.get()
            ?: StreakEntity(updatedAtEpochMillis = nowEpochMillis)

        val updated = if (achievedToday) {
            val nextCurrent = when {
                current.lastAchievedDateKey == dateKey -> current.currentStreak
                current.lastAchievedDateKey == yesterdayKey -> current.currentStreak + 1
                else -> 1
            }

            current.copy(
                currentStreak = nextCurrent,
                bestStreak = maxOf(current.bestStreak, nextCurrent),
                lastAchievedDateKey = dateKey,
                updatedAtEpochMillis = nowEpochMillis
            )
        } else {
            val shouldReset = current.lastAchievedDateKey != null &&
                current.lastAchievedDateKey != yesterdayKey &&
                current.lastAchievedDateKey != dateKey

            if (!shouldReset) {
                current.copy(updatedAtEpochMillis = nowEpochMillis)
            } else {
                current.copy(currentStreak = 0, updatedAtEpochMillis = nowEpochMillis)
            }
        }

        streakDao.upsert(updated)
    }

    private fun LocalDate.toDateKey(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)
}
