package com.dayquest.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dayquest.app.MainActivity
import com.dayquest.app.ui.TodayTaskUi
import com.dayquest.domain.DayQuestRepository
import com.dayquest.domain.GoalTimeReminderScheduler
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DayQuestReminderManager(
    private val context: Context,
    private val repository: DayQuestRepository,
    private val scheduler: GoalTimeReminderScheduler = GoalTimeReminderScheduler(),
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun refreshSchedule() {
        cancel()
        val nextReminder = runBlocking { calculateNextReminder() } ?: return
        val triggerAtMillis = nextReminder.toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                alarmIntent(),
            )
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            alarmIntent(),
        )
    }

    fun cancel() {
        alarmManager.cancel(alarmIntent())
    }

    fun onAlarmReceived() {
        if (hasNotificationPermission()) {
            ensureChannel()
            notificationManager.notify(
                NOTIFICATION_ID,
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("DayQuest 리마인더")
                    .setContentText("오늘 퀘스트를 확인하고 다음 보상을 챙길 시간입니다.")
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent())
                    .build(),
            )
        }
        refreshSchedule()
    }

    private suspend fun calculateNextReminder(): ZonedDateTime? {
        val plan = repository.loadReminderPlan()
        if (!plan.enabled) return null

        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val candidates = buildList {
            scheduler.nextReminder(
                now,
                listOf(MORNING_REMINDER_TIME, EVENING_REMINDER_TIME),
                DayOfWeek.entries.toSet(),
            )?.let(::add)
            buildRuleGroups(plan.quests.filterNot { it.isCompleted }).forEach { (days, times) ->
                scheduler.nextReminder(now, times, days)?.let(::add)
            }
            plan.quests.filterNot { it.isCompleted }.mapNotNull(::monthlyReminderCandidate).forEach(::add)
        }
        return candidates.minOrNull()
    }

    private fun buildRuleGroups(quests: List<TodayTaskUi>): Map<Set<DayOfWeek>, List<LocalTime>> =
        quests.mapNotNull { quest ->
            if (quest.repeatLabel?.startsWith("$MONTHLY_REPEAT_LABEL:") == true) return@mapNotNull null
            val time = parseTime(quest.timeLabel) ?: return@mapNotNull null
            ruleToDays(quest.repeatLabel) to time
        }.groupBy(keySelector = { it.first }, valueTransform = { it.second })

    private fun monthlyReminderCandidate(quest: TodayTaskUi): ZonedDateTime? {
        val repeat = quest.repeatLabel ?: return null
        if (!repeat.startsWith("$MONTHLY_REPEAT_LABEL:")) return null
        val dayOfMonth = repeat.substringAfter(":").toIntOrNull()?.coerceIn(1, 31) ?: return null
        val time = parseTime(quest.timeLabel) ?: return null
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val clampedDay = dayOfMonth.coerceAtMost(now.toLocalDate().lengthOfMonth())
        var candidate = now
            .withDayOfMonth(clampedDay)
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .withNano(0)
        if (!candidate.isAfter(now)) {
            val nextMonth = now.plusMonths(1)
            candidate = nextMonth
                .withDayOfMonth(dayOfMonth.coerceAtMost(nextMonth.toLocalDate().lengthOfMonth()))
                .withHour(time.hour)
                .withMinute(time.minute)
                .withSecond(0)
                .withNano(0)
        }
        return candidate
    }

    private fun parseTime(value: String?): LocalTime? = try {
        value?.takeIf { it.isNotBlank() }?.let(LocalTime::parse)
    } catch (_: Exception) {
        null
    }

    private fun ruleToDays(rule: String?): Set<DayOfWeek> {
        if (rule?.startsWith("$CUSTOM_REPEAT_LABEL:") == true) {
            val days = rule.substringAfter(":")
                .split(",")
                .mapNotNull { CUSTOM_DAY_TO_WEEKDAY[it] }
                .toSet()
            return days.ifEmpty { DayOfWeek.entries.toSet() }
        }

        return when (rule) {
            "주중" -> setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
            )
            "주말" -> setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            else -> DayOfWeek.entries.toSet()
        }
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun alarmIntent(): PendingIntent {
        val intent = Intent(context, DayQuestReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "DayQuest Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
    }

    private companion object {
        const val CHANNEL_ID = "dayquest_reminder"
        const val REQUEST_CODE = 1001
        const val NOTIFICATION_ID = 1002
        const val CUSTOM_REPEAT_LABEL = "커스텀"
        const val MONTHLY_REPEAT_LABEL = "매달"
        val MORNING_REMINDER_TIME: LocalTime = LocalTime.of(7, 0)
        val EVENING_REMINDER_TIME: LocalTime = LocalTime.of(21, 0)
        val CUSTOM_DAY_TO_WEEKDAY = mapOf(
            "월" to DayOfWeek.MONDAY,
            "화" to DayOfWeek.TUESDAY,
            "수" to DayOfWeek.WEDNESDAY,
            "목" to DayOfWeek.THURSDAY,
            "금" to DayOfWeek.FRIDAY,
            "토" to DayOfWeek.SATURDAY,
            "일" to DayOfWeek.SUNDAY,
        )
    }
}
