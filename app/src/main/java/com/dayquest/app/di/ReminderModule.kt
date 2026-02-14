package com.dayquest.app.di

import com.dayquest.app.data.reminder.WorkManagerReminderScheduler
import com.dayquest.app.domain.usecase.reminder.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        impl: WorkManagerReminderScheduler
    ): ReminderScheduler
}
