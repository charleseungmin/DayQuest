package com.dayquest.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskManageLogicTest {

    @Test
    fun `upsert adds new task when form is valid`() {
        val state = TaskManageUiState.Ready(
            tasks = emptyList(),
            form = TaskFormUi(title = "운동", category = "건강")
        )

        val updated = TaskManageLogic.upsert(state)

        assertEquals(1, updated.tasks.size)
        assertEquals("운동", updated.tasks.first().title)
        assertEquals("건강", updated.tasks.first().category)
        assertFalse(updated.tasks.first().isDone)
        assertEquals("", updated.form.title)
        assertNull(updated.form.editingTaskId)
    }

    @Test
    fun `toggleDone flips completion state`() {
        val state = TaskManageUiState.Ready(
            tasks = listOf(TaskItemUi(id = "t1", title = "리뷰", category = "업무", isDone = false))
        )

        val updated = TaskManageLogic.toggleDone(state, "t1")

        assertTrue(updated.tasks.first().isDone)
    }

    @Test
    fun `shouldCelebrate becomes true when quest is newly completed`() {
        val before = listOf(
            TaskItemUi(id = "t1", title = "A", category = "일반", isDone = true),
            TaskItemUi(id = "t2", title = "B", category = "일반", isDone = false)
        )
        val after = listOf(
            TaskItemUi(id = "t1", title = "A", category = "일반", isDone = true),
            TaskItemUi(id = "t2", title = "B", category = "일반", isDone = true)
        )

        assertTrue(QuestFeedbackLogic.shouldCelebrate(before, after))
    }
}
