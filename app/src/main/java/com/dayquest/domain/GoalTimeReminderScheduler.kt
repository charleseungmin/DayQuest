package com.dayquest.domain

import java.time.DayOfWeek
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
        goalTimes: List<LocalTime>,
        activeDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()
    ): ZonedDateTime? {
        val normalized = normalize(goalTimes)
        val normalizedActiveDays = normalizeActiveDays(activeDays)
        if (normalized.isEmpty() || normalizedActiveDays.isEmpty()) return null

        val localNow = now.withZoneSameInstant(zoneId)
        val startDate = localNow.toLocalDate()

        val all = (0 until 14).asSequence().flatMap { offset ->
            remindersForDate(startDate.plusDays(offset.toLong()), normalized, normalizedActiveDays).asSequence()
        }

        return all.firstOrNull { !it.isBefore(localNow) }
    }

    fun upcomingReminders(
        from: ZonedDateTime,
        goalTimes: List<LocalTime>,
        days: Int,
        activeDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()
    ): List<ZonedDateTime> {
        require(days >= 1) { "days must be >= 1" }

        val normalized = normalize(goalTimes)
        val normalizedActiveDays = normalizeActiveDays(activeDays)
        if (normalized.isEmpty() || normalizedActiveDays.isEmpty()) return emptyList()

        val localFrom = from.withZoneSameInstant(zoneId)
        val startDate = localFrom.toLocalDate()

        val all = (0 until days).flatMap { offset ->
            remindersForDate(startDate.plusDays(offset.toLong()), normalized, normalizedActiveDays)
        }

        return all.filter { !it.isBefore(localFrom) }
    }

    private fun remindersForDate(
        date: LocalDate,
        goalTimes: List<LocalTime>,
        activeDays: Set<DayOfWeek>
    ): List<ZonedDateTime> {
        if (date.dayOfWeek !in activeDays) return emptyList()
        return goalTimes.map { time -> ZonedDateTime.of(date, time, zoneId) }
    }

    private fun normalize(goalTimes: List<LocalTime>): List<LocalTime> {
        return goalTimes.distinct().sorted()
    }

    private fun normalizeActiveDays(activeDays: Set<DayOfWeek>): Set<DayOfWeek> {
        return activeDays.toSet()
    }
}
