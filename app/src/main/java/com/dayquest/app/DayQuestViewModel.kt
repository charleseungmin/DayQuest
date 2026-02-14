package com.dayquest.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DayQuestUiState(
    val mode: Mode = Mode.Loading,
    val errorMessage: String? = null
) {
    enum class Mode {
        Loading,
        Ready,
        Error
    }
}

sealed interface DayQuestIntent {
    data object LoadSuccess : DayQuestIntent
    data class LoadFailed(val message: String) : DayQuestIntent
    data object Retry : DayQuestIntent
}

class DayQuestViewModel : ViewModel() {
    private val _state = MutableStateFlow(DayQuestUiState())
    val state: StateFlow<DayQuestUiState> = _state.asStateFlow()

    fun dispatch(intent: DayQuestIntent) {
        _state.value = reduce(_state.value, intent)
    }

    internal fun reduce(current: DayQuestUiState, intent: DayQuestIntent): DayQuestUiState {
        return when (intent) {
            DayQuestIntent.LoadSuccess -> current.copy(mode = DayQuestUiState.Mode.Ready, errorMessage = null)
            is DayQuestIntent.LoadFailed -> current.copy(mode = DayQuestUiState.Mode.Error, errorMessage = intent.message)
            DayQuestIntent.Retry -> current.copy(mode = DayQuestUiState.Mode.Loading, errorMessage = null)
        }
    }
}
