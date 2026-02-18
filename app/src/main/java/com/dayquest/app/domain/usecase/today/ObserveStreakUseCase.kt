package com.dayquest.app.domain.usecase.today

import com.dayquest.app.data.local.dao.StreakDao
import com.dayquest.app.ui.model.StreakUi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveStreakUseCase @Inject constructor(
    private val streakDao: StreakDao
) {
    operator fun invoke(): Flow<StreakUi> =
        streakDao.observe().map { streak ->
            StreakUi(
                currentStreak = streak?.currentStreak ?: 0,
                bestStreak = streak?.bestStreak ?: 0
            )
        }
}
