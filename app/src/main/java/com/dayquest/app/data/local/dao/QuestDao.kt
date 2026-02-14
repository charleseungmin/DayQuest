package com.dayquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dayquest.app.core.model.QuestType
import com.dayquest.app.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests WHERE dateKey = :dateKey ORDER BY id ASC")
    fun observeByDate(dateKey: String): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quests: List<QuestEntity>)

    @Update
    suspend fun update(quest: QuestEntity)

    @Query("SELECT * FROM quests WHERE dateKey = :dateKey AND questType = :questType LIMIT 1")
    suspend fun getByType(dateKey: String, questType: QuestType): QuestEntity?

    @Query("SELECT COUNT(*) FROM quests WHERE dateKey = :dateKey AND achieved = 1")
    suspend fun countAchievedByDate(dateKey: String): Int
}
