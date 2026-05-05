package com.dayquest.app.ui.logic

import com.dayquest.app.ui.model.SettingsUiState

object SettingsLogic {
    fun toggleNotifications(state: SettingsUiState): SettingsUiState =
        state.copy(
            notificationsEnabled = !state.notificationsEnabled,
            resetDone = false
        )

    fun resetData(state: SettingsUiState): SettingsUiState =
        state.copy(
            notificationsEnabled = true,
            resetDone = true
        )
}
