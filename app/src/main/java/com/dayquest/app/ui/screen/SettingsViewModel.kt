package com.dayquest.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayquest.app.domain.usecase.settings.ObserveNotificationEnabledUseCase
import com.dayquest.app.domain.usecase.settings.ResetLocalDataUseCase
import com.dayquest.app.domain.usecase.settings.SetNotificationEnabledUseCase
import com.dayquest.app.ui.model.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeNotificationEnabledUseCase: ObserveNotificationEnabledUseCase,
    private val setNotificationEnabledUseCase: SetNotificationEnabledUseCase,
    private val resetLocalDataUseCase: ResetLocalDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { setNotificationEnabledUseCase(enabled) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(noticeMessage = "알림 설정 변경에 실패했습니다.")
                }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isResetting = true,
                resetDone = false,
                errorMessage = null
            )
            runCatching { resetLocalDataUseCase() }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isResetting = false,
                        resetDone = true,
                        noticeMessage = "로컬 데이터 초기화가 완료되었습니다."
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isResetting = false,
                        resetDone = false,
                        errorMessage = "로컬 데이터 초기화에 실패했습니다.",
                        noticeMessage = "초기화 중 오류가 발생했습니다."
                    )
                }
        }
    }

    fun consumeNotice() {
        _uiState.value = _uiState.value.copy(noticeMessage = null)
    }

    private fun observeSettings() {
        viewModelScope.launch {
            observeNotificationEnabledUseCase()
                .catch {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "설정 정보를 불러오지 못했습니다."
                    )
                }
                .collect { enabled ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notificationsEnabled = enabled,
                        errorMessage = null
                    )
                }
        }
    }
}
