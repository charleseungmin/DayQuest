package com.dayquest.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class TaskRulesTest {
    @Test
    fun validateDraftTrimsOptionalFieldsAndKeepsFullFeatureChoices() {
        val result = TaskDraftValidator.validate(
            title = "  보고서 작성  ",
            memo = "  초안부터  ",
            categoryLabel = "  업무  ",
            repeatRule = "매일",
            startDate = "2026-05-01",
            endDate = "2026-05-31",
            timeLabel = "09:30",
            tier = QuestTierChoice.HIGH,
            reminderEnabled = true,
        )

        assertTrue(result.isValid)
        assertEquals(
            TaskDraft(
                title = "보고서 작성",
                memo = "초안부터",
                categoryLabel = "업무",
                repeatRule = "매일",
                startDate = "2026-05-01",
                endDate = "2026-05-31",
                timeLabel = "09:30",
                tier = QuestTierChoice.HIGH,
                reminderEnabled = true,
            ),
            result.draft,
        )
        assertNull(result.titleError)
        assertNull(result.timeError)
        assertNull(result.dateError)
    }

    @Test
    fun validateDraftRejectsBlankTitleAndInvalidGoalTime() {
        val result = TaskDraftValidator.validate(
            title = "   ",
            memo = "",
            categoryLabel = "",
            repeatRule = "",
            startDate = "",
            endDate = "",
            timeLabel = "25:99",
            tier = QuestTierChoice.NORMAL,
            reminderEnabled = false,
        )

        assertFalse(result.isValid)
        assertEquals("의뢰 제목을 입력해 주세요.", result.titleError)
        assertEquals("시간은 HH:mm 형식으로 입력해 주세요.", result.timeError)
        assertNull(result.draft)
    }

    @Test
    fun validateDraftRejectsInvalidDateRange() {
        val result = TaskDraftValidator.validate(
            title = "보고서 작성",
            memo = null,
            categoryLabel = "업무",
            repeatRule = "매일",
            startDate = "2026-05-31",
            endDate = "2026-05-01",
            timeLabel = "09:30",
            tier = QuestTierChoice.HIGH,
            reminderEnabled = true,
        )

        assertFalse(result.isValid)
        assertEquals("종료일은 시작일 이후여야 합니다.", result.dateError)
        assertNull(result.draft)
    }

    @Test
    fun snoozeAddsMinutesToExistingGoalTime() {
        val result = TaskSchedulePolicy.snoozedTimeLabel(
            currentTimeLabel = "23:45",
            now = LocalTime.of(8, 0),
            minutes = 30,
        )

        assertEquals("00:15", result)
    }

    @Test
    fun snoozeUsesCurrentTimeWhenTaskHasNoGoalTime() {
        val result = TaskSchedulePolicy.snoozedTimeLabel(
            currentTimeLabel = null,
            now = LocalTime.of(10, 5),
            minutes = 30,
        )

        assertEquals("10:35", result)
    }
}
