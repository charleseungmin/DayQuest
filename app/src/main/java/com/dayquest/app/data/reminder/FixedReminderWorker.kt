package com.dayquest.app.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FixedReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        createChannelIfNeeded()

        if (!canPostNotifications()) {
            return Result.success()
        }

        val reminderId = inputData.getString(KEY_REMINDER_ID) ?: "fixed_reminder"
        val hour = inputData.getInt(KEY_HOUR, -1)
        val minute = inputData.getInt(KEY_MINUTE, -1)

        val title = "DayQuest 리마인드"
        val body = if (hour >= 0 && minute >= 0) {
            String.format("%02d:%02d 할 일을 점검할 시간입니다.", hour, minute)
        } else {
            "오늘의 DayQuest를 확인해 주세요."
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(reminderId.hashCode(), notification)
        return Result.success()
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "DayQuest Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
        private const val CHANNEL_ID = "dayquest_fixed_reminders"
    }
}
