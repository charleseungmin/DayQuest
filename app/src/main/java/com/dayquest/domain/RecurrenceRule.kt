package com.dayquest.domain

import java.time.DayOfWeek
import java.time.LocalDate

enum class RecurrenceType {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class RecurrenceRule(
    val type: RecurrenceType,
    val weekdays: Set<DayOfWeek> = emptySet(),
    val daysOfMonth: Set<Int> = emptySet()
)

object RecurrenceEvaluator {
    fun shouldScheduleOn(date: LocalDate, rule: RecurrenceRule): Boolean {
        return when (rule.type) {
            RecurrenceType.DAILY -> true
            RecurrenceType.WEEKLY -> rule.weekdays.contains(date.dayOfWeek)
            RecurrenceType.MONTHLY -> rule.daysOfMonth.contains(date.dayOfMonth)
        }
    }
}
