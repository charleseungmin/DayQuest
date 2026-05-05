package com.dayquest.app

import android.app.Application
import androidx.room.Room
import com.dayquest.data.DayQuestDatabase
import com.dayquest.data.RoomDayQuestRepository
import com.dayquest.reminder.DayQuestReminderManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class DayQuestApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: DayQuestDatabase by lazy {
        Room.databaseBuilder(this, DayQuestDatabase::class.java, "dayquest.db")
            .addMigrations(
                DayQuestDatabase.MIGRATION_1_2,
                DayQuestDatabase.MIGRATION_2_3,
                DayQuestDatabase.MIGRATION_3_4,
                DayQuestDatabase.MIGRATION_4_5,
                DayQuestDatabase.MIGRATION_5_6,
                DayQuestDatabase.MIGRATION_6_7,
            )
            .build()
    }

    val repository: RoomDayQuestRepository by lazy {
        RoomDayQuestRepository(
            questDao = database.questDao(),
            settingsDao = database.settingsDao(),
            completionLogDao = database.completionLogDao(),
            dailyItemDao = database.dailyItemDao(),
            dailyQuestDao = database.dailyQuestDao(),
            characterProgressDao = database.characterProgressDao(),
            characterRewardLogDao = database.characterRewardLogDao(),
        )
    }

    val reminderManager: DayQuestReminderManager by lazy {
        DayQuestReminderManager(this, repository)
    }

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            repository.seedIfEmpty()
            reminderManager.refreshSchedule()
        }
    }
}
