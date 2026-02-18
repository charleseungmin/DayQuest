package com.dayquest.app.ui.logic

import com.dayquest.app.ui.model.HistoryDayProgressUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HistoryProgressSeed(
    val dateKey: String,
    val doneCount: Int,
    val deferredCount: Int,
    val totalCount: Int
)

fun buildHistoryTimeline(
    today: LocalDate,
    periodDays: Long,
    progress: List<HistoryProgressSeed>,
    dateFormatter: DateTimeFormatter,
    weekdayFormatter: DateTimeFormatter
): List<HistoryDayProgressUi> {
    val progressByDate = progress.associateBy { it.dateKey }
    val cutoff = today.minusDays(periodDays - 1)

    return generateSequence(today) { date ->
        val next = date.minusDays(1)
        if (next.isBefore(cutoff)) null else next
    }
        .map { date ->
            val dateKey = date.toString()
            val row = progressByDate[dateKey]
            val doneCount = row?.doneCount ?: 0
            val deferredCount = row?.deferredCount ?: 0
            val totalCount = row?.totalCount ?: 0
            val completionRate = if (totalCount == 0) 0 else (doneCount * 100 / totalCount)

            HistoryDayProgressUi(
                dateKey = dateKey,
                dateLabel = date.format(dateFormatter),
                weekdayLabel = date.format(weekdayFormatter),
                doneCount = doneCount,
                deferredCount = deferredCount,
                totalCount = totalCount,
                completionRate = completionRate
            )
        }
        .toList()
}

fun filterHistoryTimeline(
    timeline: List<HistoryDayProgressUi>,
    showOnlyActiveDays: Boolean
): List<HistoryDayProgressUi> {
    if (!showOnlyActiveDays) return timeline
    return timeline.filter { it.totalCount > 0 }
}
