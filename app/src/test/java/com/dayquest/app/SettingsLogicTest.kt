package com.dayquest.app

import com.dayquest.app.ui.logic.SettingsLogic
import com.dayquest.app.ui.model.SettingsUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsLogicTest {

    @Test
    fun `toggleNotifications changes enabled state`() {
        val state = SettingsUiState(notificationsEnabled = true)

        val updated = SettingsLogic.toggleNotifications(state)

        assertFalse(updated.notificationsEnabled)
    }

    @Test
    fun `resetData marks resetDone`() {
        val state = SettingsUiState(notificationsEnabled = false, resetDone = false)

        val updated = SettingsLogic.resetData(state)

        assertTrue(updated.notificationsEnabled)
        assertTrue(updated.resetDone)
    }

    @Test
    fun `toggleNotifications clears resetDone flag`() {
        val state = SettingsUiState(notificationsEnabled = true, resetDone = true)

        val updated = SettingsLogic.toggleNotifications(state)

        assertFalse(updated.notificationsEnabled)
        assertFalse(updated.resetDone)
    }

    @Test
    fun `toggleNotifications turns on notifications when previously off`() {
        val state = SettingsUiState(notificationsEnabled = false, resetDone = false)

        val updated = SettingsLogic.toggleNotifications(state)

        assertTrue(updated.notificationsEnabled)
        assertFalse(updated.resetDone)
    }
}
