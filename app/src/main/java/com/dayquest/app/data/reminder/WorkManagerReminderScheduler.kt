package com.dayquest.app.data.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dayquest.app.domain.usecase.reminder.ReminderScheduleCalculator
import com.dayquest.app.domain.usecase.reminder.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderScheduler {

    override suspend fun scheduleDaily(id: String, hour: Int, minute: Int) {
        val initialDelay = ReminderScheduleCalculator.initialDelayMillis(
            now = LocalDateTime.now(),
            targetHour = hour,
            targetMinute = minute
        )

        val work = PeriodicWorkRequestBuilder<FixedReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    FixedReminderWorker.KEY_REMINDER_ID to id,
                    FixedReminderWorker.KEY_HOUR to hour,
                    FixedReminderWorker.KEY_MINUTE to minute
                )
            )
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(id, ExistingPeriodicWorkPolicy.UPDATE, work)
    }

    override suspend fun cancelDaily(id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(id)
    }
}
