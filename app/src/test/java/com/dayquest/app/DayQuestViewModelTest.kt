package com.dayquest.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DayQuestViewModelTest {

    private val viewModel = DayQuestViewModel()

    @Test
    fun `load success transitions state to ready`() {
        val start = DayQuestUiState(mode = DayQuestUiState.Mode.Loading)

        val updated = viewModel.reduce(start, DayQuestIntent.LoadSuccess)

        assertEquals(DayQuestUiState.Mode.Ready, updated.mode)
        assertNull(updated.errorMessage)
    }

    @Test
    fun `load failed transitions state to error with message`() {
        val updated = viewModel.reduce(
            DayQuestUiState(mode = DayQuestUiState.Mode.Loading),
            DayQuestIntent.LoadFailed("네트워크 오류")
        )

        assertEquals(DayQuestUiState.Mode.Error, updated.mode)
        assertEquals("네트워크 오류", updated.errorMessage)
    }

    @Test
    fun `retry clears error and transitions back to loading`() {
        val start = DayQuestUiState(mode = DayQuestUiState.Mode.Error, errorMessage = "timeout")

        val updated = viewModel.reduce(start, DayQuestIntent.Retry)

        assertEquals(DayQuestUiState.Mode.Loading, updated.mode)
        assertNull(updated.errorMessage)
    }
}
