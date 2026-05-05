package com.dayquest.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dayquest.app.DayQuestApp

class DayQuestBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        (context.applicationContext as DayQuestApp).reminderManager.refreshSchedule()
    }
}
