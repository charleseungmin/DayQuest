package com.dayquest.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dayquest.app.DayQuestApp

class DayQuestReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        (context.applicationContext as DayQuestApp).reminderManager.onAlarmReceived()
    }
}
