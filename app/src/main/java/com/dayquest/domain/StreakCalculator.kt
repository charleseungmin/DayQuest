package com.dayquest.domain

import java.time.LocalDate

object StreakCalculator {
    fun calculate(currentDate: LocalDate, completedDates: Set<LocalDate>): Int {
        if (!completedDates.contains(currentDate)) return 0

        var streak = 0
        var cursor = currentDate
        while (completedDates.contains(cursor)) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
