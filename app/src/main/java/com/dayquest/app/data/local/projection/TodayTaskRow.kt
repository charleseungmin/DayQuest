package com.dayquest.app.data.local.projection

import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.core.model.TaskPriority

data class TodayTaskRow(
    val dailyItemId: Long,
    val sourceTaskId: Long,
    val title: String,
    val category: String,
    val priority: TaskPriority,
    val isImportant: Boolean,
    val status: DailyItemStatus
)
