package com.dayquest.app.domain.usecase.task

import com.dayquest.app.core.model.RepeatType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class ShouldGenerateTaskForDateUseCase @Inject constructor() {

    operator fun invoke(
        repeatType: RepeatType,
        baseDate: LocalDate,
        targetDate: LocalDate,
        repeatDaysMask: Int? = null
    ): Boolean {
        if (targetDate.isBefore(baseDate)) return false

        return when (repeatType) {
            RepeatType.DAILY -> true
            RepeatType.WEEKLY -> isWeeklyMatch(targetDate.dayOfWeek, resolveWeeklyMask(repeatDaysMask, baseDate.dayOfWeek))
            RepeatType.MONTHLY -> {
                val lastDay = YearMonth.from(targetDate).lengthOfMonth()
                val day = minOf(baseDate.dayOfMonth, lastDay)
                targetDate.dayOfMonth == day
            }
            RepeatType.CUSTOM -> isWeeklyMatch(targetDate.dayOfWeek, resolveWeeklyMask(repeatDaysMask, baseDate.dayOfWeek))
        }
    }

    private fun resolveWeeklyMask(repeatDaysMask: Int?, baseDayOfWeek: DayOfWeek): Int {
        if (repeatDaysMask == null || repeatDaysMask == 0) {
            return makeWeeklyMask(baseDayOfWeek)
        }
        return repeatDaysMask
    }

    private fun isWeeklyMatch(dayOfWeek: DayOfWeek, repeatDaysMask: Int): Boolean {
        val bit = 1 shl (dayOfWeek.value - 1) // MON=0 .. SUN=6
        return (repeatDaysMask and bit) != 0
    }

    companion object {
        fun makeWeeklyMask(vararg dayOfWeeks: DayOfWeek): Int {
            var mask = 0
            dayOfWeeks.forEach { day ->
                mask = mask or (1 shl (day.value - 1))
            }
            return mask
        }
    }
}
