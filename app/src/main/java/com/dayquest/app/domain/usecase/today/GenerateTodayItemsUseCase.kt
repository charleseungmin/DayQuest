package com.dayquest.app.domain.usecase.today

import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.dao.TaskDao
import com.dayquest.app.data.local.entity.DailyItemEntity
import com.dayquest.app.domain.usecase.task.ShouldGenerateTaskForDateUseCase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GenerateTodayItemsUseCase @Inject constructor(
    private val taskDao: TaskDao,
    private val dailyItemDao: DailyItemDao,
    private val shouldGenerateTaskForDateUseCase: ShouldGenerateTaskForDateUseCase
) {
    suspend operator fun invoke(date: LocalDate, nowEpochMillis: Long): Int {
        val dateKey = date.toDateKey()

        // idempotent: if already generated, do nothing
        if (dailyItemDao.countByDate(dateKey) > 0) return 0

        val tasks = taskDao.getActiveTasks()
        val toInsert = tasks.filter { task ->
            val baseDate = epochMillisToLocalDate(task.createdAtEpochMillis)
            shouldGenerateTaskForDateUseCase(
                repeatType = task.repeatType,
                baseDate = baseDate,
                targetDate = date,
                repeatDaysMask = task.repeatDaysMask
            )
        }.map { task ->
            DailyItemEntity(
                dateKey = dateKey,
                taskId = task.id,
                createdAtEpochMillis = nowEpochMillis
            )
        }

        if (toInsert.isEmpty()) return 0
        val results = dailyItemDao.insertAll(toInsert)
        return results.count { it > 0 }
    }

    private fun LocalDate.toDateKey(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

    private fun epochMillisToLocalDate(epochMillis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(epochMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
}
