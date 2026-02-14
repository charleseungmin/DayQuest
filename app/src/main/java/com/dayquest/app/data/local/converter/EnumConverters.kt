package com.dayquest.app.data.local.converter

import androidx.room.TypeConverter
import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.core.model.QuestType
import com.dayquest.app.core.model.RepeatType
import com.dayquest.app.core.model.TaskPriority
import com.dayquest.app.core.model.TaskType

class EnumConverters {
    @TypeConverter
    fun fromTaskType(value: TaskType): String = value.name

    @TypeConverter
    fun toTaskType(value: String): TaskType = TaskType.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromRepeatType(value: RepeatType): String = value.name

    @TypeConverter
    fun toRepeatType(value: String): RepeatType = RepeatType.valueOf(value)

    @TypeConverter
    fun fromDailyItemStatus(value: DailyItemStatus): String = value.name

    @TypeConverter
    fun toDailyItemStatus(value: String): DailyItemStatus = DailyItemStatus.valueOf(value)

    @TypeConverter
    fun fromQuestType(value: QuestType): String = value.name

    @TypeConverter
    fun toQuestType(value: String): QuestType = QuestType.valueOf(value)
}
