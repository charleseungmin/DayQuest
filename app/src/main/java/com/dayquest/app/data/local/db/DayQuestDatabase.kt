package com.dayquest.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dayquest.app.data.local.converter.EnumConverters
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.dao.QuestDao
import com.dayquest.app.data.local.dao.StreakDao
import com.dayquest.app.data.local.dao.TaskDao
import com.dayquest.app.data.local.entity.DailyItemEntity
import com.dayquest.app.data.local.entity.QuestEntity
import com.dayquest.app.data.local.entity.StreakEntity
import com.dayquest.app.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        DailyItemEntity::class,
        QuestEntity::class,
        StreakEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(EnumConverters::class)
abstract class DayQuestDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun dailyItemDao(): DailyItemDao
    abstract fun questDao(): QuestDao
    abstract fun streakDao(): StreakDao

    companion object {
        const val DATABASE_NAME = "dayquest.db"
    }
}
