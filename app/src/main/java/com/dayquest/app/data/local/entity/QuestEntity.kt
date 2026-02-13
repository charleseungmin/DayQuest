package com.dayquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dayquest.app.core.model.QuestType

@Entity(
    tableName = "quests",
    indices = [Index(value = ["dateKey", "questType"], unique = true)]
)
data class QuestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dateKey: String,
    val questType: QuestType,
    val title: String,
    val targetCount: Int,
    val progressCount: Int = 0,
    val achieved: Boolean = false,
    val achievedAtEpochMillis: Long? = null
)
