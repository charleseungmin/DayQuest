package com.dayquest.app

import android.app.Application
import com.dayquest.app.domain.usecase.reminder.ScheduleFixedRemindersUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DayQuestApp : Application() {

    @Inject
    lateinit var scheduleFixedRemindersUseCase: ScheduleFixedRemindersUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            scheduleFixedRemindersUseCase()
        }
    }
}
