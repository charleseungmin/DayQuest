package com.dayquest.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 목표 시간(예: 운동 19:00, 독서 22:00)에 맞춘 다음 알림 시각을 계산한다.
 */
class GoalTimeReminderScheduler(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    fun nextReminder(
        now: ZonedDateTime,
        goalTimes: List<LocalTime>
    ): ZonedDateTime? {
        val normalized = normalize(goalTimes)
        if (normalized.isEmpty()) return null

        val localNow = now.withZoneSameInstant(zoneId)
        return sequenceOf(
            remindersForDate(localNow.toLocalDate(), normalized),
            remindersForDate(localNow.toLocalDate().plusDays(1), normalized)
        ).flatten().firstOrNull { !it.isBefore(localNow) }
    }

    fun upcomingReminders(
        from: ZonedDateTime,
        goalTimes: List<LocalTime>,
        days: Int
    ): List<ZonedDateTime> {
        require(days >= 1) { "days must be >= 1" }

        val normalized = normalize(goalTimes)
        if (normalized.isEmpty()) return emptyList()

        val localFrom = from.withZoneSameInstant(zoneId)
        val startDate = localFrom.toLocalDate()

        val all = (0 until days).flatMap { offset ->
            remindersForDate(startDate.plusDays(offset.toLong()), normalized)
        }

        return all.filter { !it.isBefore(localFrom) }
    }

    private fun remindersForDate(date: LocalDate, goalTimes: List<LocalTime>): List<ZonedDateTime> {
        return goalTimes.map { time -> ZonedDateTime.of(date, time, zoneId) }
    }

    private fun normalize(goalTimes: List<LocalTime>): List<LocalTime> {
        return goalTimes.distinct().sorted()
    }
}
