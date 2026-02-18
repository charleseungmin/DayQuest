package com.dayquest.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dayquest.app.domain.usecase.reminder.ScheduleFixedRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderBootstrapReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleFixedRemindersUseCase: ScheduleFixedRemindersUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action !in SUPPORTED_ACTIONS) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            runCatching { scheduleFixedRemindersUseCase() }
            pendingResult.finish()
        }
    }

    private companion object {
        val SUPPORTED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED
        )
    }
}
