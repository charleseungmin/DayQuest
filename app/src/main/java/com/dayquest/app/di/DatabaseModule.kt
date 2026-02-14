package com.dayquest.app.di

import android.content.Context
import androidx.room.Room
import com.dayquest.app.data.local.dao.DailyItemDao
import com.dayquest.app.data.local.dao.QuestDao
import com.dayquest.app.data.local.dao.StreakDao
import com.dayquest.app.data.local.dao.TaskDao
import com.dayquest.app.data.local.db.DayQuestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DayQuestDatabase {
        return Room.databaseBuilder(
            context,
            DayQuestDatabase::class.java,
            DayQuestDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTaskDao(database: DayQuestDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideDailyItemDao(database: DayQuestDatabase): DailyItemDao = database.dailyItemDao()

    @Provides
    fun provideQuestDao(database: DayQuestDatabase): QuestDao = database.questDao()

    @Provides
    fun provideStreakDao(database: DayQuestDatabase): StreakDao = database.streakDao()
}
