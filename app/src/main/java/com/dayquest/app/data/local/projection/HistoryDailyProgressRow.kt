package com.dayquest.app.data.local.projection

data class HistoryDailyProgressRow(
    val dateKey: String,
    val totalCount: Int,
    val doneCount: Int,
    val deferredCount: Int
)
