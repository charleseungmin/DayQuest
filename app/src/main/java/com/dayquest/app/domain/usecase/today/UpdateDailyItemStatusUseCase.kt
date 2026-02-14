package com.dayquest.app.domain.usecase.today

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.dao.DailyItemDao
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UpdateDailyItemStatusUseCase @Inject constructor(
    private val dailyItemDao: DailyItemDao
) {
    suspend operator fun invoke(
        dailyItemId: Long,
        toStatus: DailyItemStatus,
        nowEpochMillis: Long,
        deferToDate: LocalDate? = null
    ) {
        require(dailyItemId > 0L) { "dailyItemId must be positive" }

        val item = dailyItemDao.getById(dailyItemId)
            ?: throw IllegalArgumentException("Daily item not found")

        when (toStatus) {
            DailyItemStatus.DONE -> {
                dailyItemDao.updateState(
                    id = dailyItemId,
                    status = DailyItemStatus.DONE,
                    completedAt = nowEpochMillis,
                    deferredToDateKey = null
                )
            }

            DailyItemStatus.DEFERRED -> {
                val deferDate = deferToDate ?: throw IllegalArgumentException("deferToDate is required for DEFERRED")
                val sourceDate = LocalDate.parse(item.dateKey, DateTimeFormatter.ISO_LOCAL_DATE)
                require(!deferDate.isBefore(sourceDate)) { "deferToDate must be same or after source date" }

                dailyItemDao.updateState(
                    id = dailyItemId,
                    status = DailyItemStatus.DEFERRED,
                    completedAt = null,
                    deferredToDateKey = deferDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
            }

            DailyItemStatus.SKIPPED -> {
                dailyItemDao.updateState(
                    id = dailyItemId,
                    status = DailyItemStatus.SKIPPED,
                    completedAt = null,
                    deferredToDateKey = null
                )
            }

            DailyItemStatus.TODO -> {
                dailyItemDao.updateState(
                    id = dailyItemId,
                    status = DailyItemStatus.TODO,
                    completedAt = null,
                    deferredToDateKey = null
                )
            }
        }
    }
}
