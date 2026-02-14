package com.dayquest.app.domain.usecase.reminder

import java.time.Duration
import java.time.LocalDateTime

object ReminderScheduleCalculator {
    fun initialDelayMillis(
        now: LocalDateTime,
        targetHour: Int,
        targetMinute: Int
    ): Long {
        val targetToday = now
            .withHour(targetHour)
            .withMinute(targetMinute)
            .withSecond(0)
            .withNano(0)

        val nextRun = if (targetToday.isAfter(now)) targetToday else targetToday.plusDays(1)
        return Duration.between(now, nextRun).toMillis()
    }
}
