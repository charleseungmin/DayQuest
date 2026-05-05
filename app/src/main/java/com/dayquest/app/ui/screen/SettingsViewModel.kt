package com.dayquest.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayquest.app.domain.usecase.settings.ObserveNotificationEnabledUseCase
import com.dayquest.app.domain.usecase.settings.ResetLocalDataUseCase
import com.dayquest.app.domain.usecase.settings.SetNotificationEnabledUseCase
import com.dayquest.app.domain.usecase.today.GenerateTodayItemsUseCase
import com.dayquest.app.domain.usecase.today.RecalculateStreakUseCase
import com.dayquest.app.domain.usecase.today.SyncDailyQuestsUseCase
import com.dayquest.app.ui.model.SettingsUiState
import java.time.LocalDate
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
    private val resetLocalDataUseCase: ResetLocalDataUseCase,
    private val generateTodayItemsUseCase: GenerateTodayItemsUseCase,
    private val syncDailyQuestsUseCase: SyncDailyQuestsUseCase,
    private val recalculateStreakUseCase: RecalculateStreakUseCase
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

    fun syncTodayData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncingToday = true,
                errorMessage = null,
                resetDone = false
            )

            val now = System.currentTimeMillis()
            val today = LocalDate.now()

            runCatching {
                generateTodayItemsUseCase(today, now)
                syncDailyQuestsUseCase.ensureQuestMeta(today)
                syncDailyQuestsUseCase.syncProgress(today, now)
                recalculateStreakUseCase(today, now)
            }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSyncingToday = false,
                        noticeMessage = "오늘 데이터 동기화가 완료되었습니다."
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSyncingToday = false,
                        errorMessage = "오늘 데이터 동기화에 실패했습니다.",
                        noticeMessage = "동기화 중 오류가 발생했습니다."
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
