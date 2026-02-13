package com.dayquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dayquest.app.core.model.DailyItemStatus

@Entity(
    tableName = "daily_items",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["dateKey"]),
        Index(value = ["taskId"]),
        Index(value = ["dateKey", "taskId"], unique = true)
    ]
)
data class DailyItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dateKey: String,
    val taskId: Long,
    val status: DailyItemStatus = DailyItemStatus.TODO,
    val completedAtEpochMillis: Long? = null,
    val deferredToDateKey: String? = null,
    val createdAtEpochMillis: Long
)
