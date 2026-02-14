package com.dayquest.app

import org.junit.Assert.assertEquals
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
        assertEquals("", updated.form.title)
        assertNull(updated.form.editingTaskId)
    }

    @Test
    fun `delete clears editing form when target task is being edited`() {
        val task = TaskItemUi(id = "t1", title = "리뷰", category = "업무")
        val state = TaskManageUiState.Ready(
            tasks = listOf(task),
            form = TaskFormUi(editingTaskId = "t1", title = "리뷰", category = "업무")
        )

        val updated = TaskManageLogic.delete(state, "t1")

        assertTrue(updated.tasks.isEmpty())
        assertNull(updated.form.editingTaskId)
        assertEquals("", updated.form.title)
    }
}
