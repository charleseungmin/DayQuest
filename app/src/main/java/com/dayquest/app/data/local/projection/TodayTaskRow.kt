package com.dayquest.app.data.local.projection

import com.dayquest.app.core.model.DailyItemStatus

data class TodayTaskRow(
    val dailyItemId: Long,
    val sourceTaskId: Long,
    val title: String,
    val category: String,
    val isImportant: Boolean,
    val status: DailyItemStatus
)
