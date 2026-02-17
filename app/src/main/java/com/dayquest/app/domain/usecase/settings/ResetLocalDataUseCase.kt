package com.dayquest.app.domain.usecase.settings

import com.dayquest.app.domain.repository.SettingsRepository
import javax.inject.Inject

class ResetLocalDataUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.resetLocalData()
    }
}
