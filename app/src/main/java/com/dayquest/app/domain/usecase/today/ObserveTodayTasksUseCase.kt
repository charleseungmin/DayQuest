package com.dayquest.app.domain.usecase.today

import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.projection.TodayTaskRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveTodayTasksUseCase @Inject constructor(
    private val dailyItemDao: DailyItemDao
) {
    operator fun invoke(date: LocalDate): Flow<List<TodayTaskRow>> {
        val dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dailyItemDao.observeTodayTasks(dateKey)
    }
}
