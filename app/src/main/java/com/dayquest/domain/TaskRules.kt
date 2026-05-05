package com.dayquest.domain

import java.time.LocalTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class QuestTierChoice { LOW, NORMAL, HIGH }

data class TaskDraft(
    val title: String,
    val memo: String?,
    val categoryLabel: String?,
    val repeatRule: String?,
    val startDate: String?,
    val endDate: String?,
    val timeLabel: String?,
    val tier: QuestTierChoice,
    val reminderEnabled: Boolean,
)

data class TaskDraftValidationResult(
    val draft: TaskDraft?,
    val titleError: String?,
    val timeError: String?,
    val dateError: String?,
) {
    val isValid: Boolean = draft != null
}

object TaskDraftValidator {
    private val timeRegex = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun validate(
        title: String,
        memo: String?,
        categoryLabel: String?,
        repeatRule: String?,
        startDate: String?,
        endDate: String?,
        timeLabel: String?,
        tier: QuestTierChoice,
        reminderEnabled: Boolean,
    ): TaskDraftValidationResult {
        val normalizedTitle = title.trim()
        val normalizedMemo = memo.orEmpty().trim()
        val normalizedTime = timeLabel.orEmpty().trim()
        val normalizedStartDate = startDate.orEmpty().trim()
        val normalizedEndDate = endDate.orEmpty().trim()
        val titleError = if (normalizedTitle.isBlank()) "의뢰 제목을 입력해 주세요." else null
        val timeError = if (normalizedTime.isNotEmpty() && !timeRegex.matches(normalizedTime)) {
            "시간은 HH:mm 형식으로 입력해 주세요."
        } else {
            null
        }
        val parsedStartDate = parseDate(normalizedStartDate)
        val parsedEndDate = parseDate(normalizedEndDate)
        val dateError = when {
            normalizedStartDate.isNotEmpty() && parsedStartDate == null -> "시작일은 yyyy-MM-dd 형식으로 입력해 주세요."
            normalizedEndDate.isNotEmpty() && parsedEndDate == null -> "종료일은 yyyy-MM-dd 형식으로 입력해 주세요."
            parsedStartDate != null && parsedEndDate != null && parsedEndDate.isBefore(parsedStartDate) ->
                "종료일은 시작일 이후여야 합니다."
            else -> null
        }

        if (titleError != null || timeError != null || dateError != null) {
            return TaskDraftValidationResult(
                draft = null,
                titleError = titleError,
                timeError = timeError,
                dateError = dateError,
            )
        }

        return TaskDraftValidationResult(
            draft = TaskDraft(
                title = normalizedTitle,
                memo = normalizedMemo.ifBlank { null },
                categoryLabel = categoryLabel.orEmpty().trim().ifBlank { null },
                repeatRule = repeatRule.orEmpty().trim().ifBlank { null },
                startDate = normalizedStartDate.ifBlank { null },
                endDate = normalizedEndDate.ifBlank { null },
                timeLabel = normalizedTime.ifBlank { null },
                tier = tier,
                reminderEnabled = reminderEnabled,
            ),
            titleError = null,
            timeError = null,
            dateError = null,
        )
    }

    private fun parseDate(value: String): LocalDate? = if (value.isBlank()) {
        null
    } else {
        runCatching { LocalDate.parse(value) }.getOrNull()
    }
}

object TaskSchedulePolicy {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun snoozedTimeLabel(currentTimeLabel: String?, now: LocalTime, minutes: Long): String {
        val baseTime = runCatching {
            currentTimeLabel?.takeIf { it.isNotBlank() }?.let(LocalTime::parse)
        }.getOrNull() ?: now

        return baseTime.plusMinutes(minutes).format(formatter)
    }
}
