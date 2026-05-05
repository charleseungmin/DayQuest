package com.dayquest.app.ui.screen

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.core.model.TaskPriority
import com.dayquest.app.data.local.projection.TodayTaskRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayTaskRowMapperTest {

    @Test
    fun `todayTaskRowToTaskItemUi maps important and status flags`() {
        val row = TodayTaskRow(
            dailyItemId = 11L,
            sourceTaskId = 7L,
            title = "약 복용",
            category = "건강",
            priority = TaskPriority.HIGH,
            isImportant = true,
            status = DailyItemStatus.DONE
        )

        val ui = todayTaskRowToTaskItemUi(row)

        assertEquals("11", ui.id)
        assertEquals("7", ui.sourceTaskId)
        assertEquals(TaskPriority.HIGH, ui.priority)
        assertTrue(ui.isImportant)
        assertTrue(ui.isDone)
        assertFalse(ui.isDeferred)
    }
}
