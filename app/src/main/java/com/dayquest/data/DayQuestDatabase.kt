package com.dayquest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        QuestEntity::class,
        AppSettingsEntity::class,
        CompletionLogEntity::class,
        DailyItemEntity::class,
        DailyQuestEntity::class,
        CharacterProgressEntity::class,
        CharacterRewardLogEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class DayQuestDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
    abstract fun settingsDao(): SettingsDao
    abstract fun completionLogDao(): CompletionLogDao
    abstract fun dailyItemDao(): DailyItemDao
    abstract fun dailyQuestDao(): DailyQuestDao
    abstract fun characterProgressDao(): CharacterProgressDao
    abstract fun characterRewardLogDao(): CharacterRewardLogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quests ADD COLUMN categoryLabel TEXT")
                db.execSQL("ALTER TABLE quests ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE quests ADD COLUMN skippedDate TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quests ADD COLUMN memo TEXT")
                db.execSQL("ALTER TABLE quests ADD COLUMN startDate TEXT")
                db.execSQL("ALTER TABLE quests ADD COLUMN endDate TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_items (
                        taskId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        status TEXT NOT NULL,
                        source TEXT NOT NULL,
                        doneAtEpochMillis INTEGER,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(taskId, date)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_items_date ON daily_items(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_items_status ON daily_items(status)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quests ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE quests ADD COLUMN createdAtEpochMillis INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_quests (
                        date TEXT NOT NULL,
                        type TEXT NOT NULL,
                        status TEXT NOT NULL,
                        progressCurrent INTEGER NOT NULL,
                        progressTarget INTEGER NOT NULL,
                        achievedAtEpochMillis INTEGER,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(date, type)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_quests_date ON daily_quests(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_quests_status ON daily_quests(status)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS character_progress (
                        id INTEGER NOT NULL,
                        level INTEGER NOT NULL,
                        expInLevel INTEGER NOT NULL,
                        nextLevelExp INTEGER NOT NULL,
                        totalExp INTEGER NOT NULL,
                        trainingPoints INTEGER NOT NULL,
                        focus INTEGER NOT NULL,
                        vitality INTEGER NOT NULL,
                        insight INTEGER NOT NULL,
                        balance INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS character_reward_logs (
                        taskId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        rewardXp INTEGER NOT NULL,
                        statType TEXT NOT NULL,
                        statPoints INTEGER NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(taskId, date)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_character_reward_logs_date ON character_reward_logs(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_character_reward_logs_statType ON character_reward_logs(statType)")
            }
        }
    }
}
