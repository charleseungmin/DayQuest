package com.dayquest.app

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
        val state = SettingsUiState(resetDone = false)

        val updated = SettingsLogic.resetData(state)

        assertTrue(updated.resetDone)
    }
}
