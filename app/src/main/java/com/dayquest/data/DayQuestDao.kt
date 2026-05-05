package com.dayquest.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY CASE isCompleted WHEN 0 THEN 0 ELSE 1 END, CASE tier WHEN 'MAIN' THEN 0 WHEN 'RARE' THEN 1 ELSE 2 END, updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests ORDER BY id ASC")
    suspend fun getAll(): List<QuestEntity>

    @Query("SELECT * FROM quests WHERE id = :taskId LIMIT 1")
    suspend fun getById(taskId: Long): QuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QuestEntity): Long

    @Update
    suspend fun update(entity: QuestEntity)

    @Query("DELETE FROM quests WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    @Query("DELETE FROM quests")
    suspend fun clear()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observe(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun get(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppSettingsEntity)
}

@Dao
interface CompletionLogDao {
    @Query("SELECT * FROM completion_logs ORDER BY completedDate DESC")
    fun observeAll(): Flow<List<CompletionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CompletionLogEntity)

    @Query("DELETE FROM completion_logs WHERE taskId = :taskId AND completedDate = :completedDate")
    suspend fun delete(taskId: Long, completedDate: String)

    @Query("DELETE FROM completion_logs")
    suspend fun clear()
}

@Dao
interface DailyItemDao {
    @Query("SELECT * FROM daily_items")
    fun observeAll(): Flow<List<DailyItemEntity>>

    @Query("SELECT * FROM daily_items WHERE date = :date")
    fun observeByDate(date: String): Flow<List<DailyItemEntity>>

    @Query("SELECT * FROM daily_items WHERE taskId = :taskId AND date = :date LIMIT 1")
    suspend fun get(taskId: Long, date: String): DailyItemEntity?

    @Query("SELECT * FROM daily_items WHERE date = :date")
    suspend fun getByDate(date: String): List<DailyItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyItemEntity)

    @Query("DELETE FROM daily_items WHERE taskId = :taskId AND date = :date AND status != :doneStatus")
    suspend fun deleteIfNotDone(taskId: Long, date: String, doneStatus: String)

    @Query("DELETE FROM daily_items WHERE taskId = :taskId AND date >= :fromDate AND status != :doneStatus")
    suspend fun deleteFutureIfNotDone(taskId: Long, fromDate: String, doneStatus: String)

    @Query("DELETE FROM daily_items")
    suspend fun clear()
}

@Dao
interface DailyQuestDao {
    @Query("SELECT * FROM daily_quests WHERE date = :date")
    suspend fun getByDate(date: String): List<DailyQuestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<DailyQuestEntity>)

    @Query("DELETE FROM daily_quests WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM daily_quests")
    suspend fun clear()

    @Transaction
    suspend fun replaceForDate(date: String, entities: List<DailyQuestEntity>) {
        deleteByDate(date)
        if (entities.isNotEmpty()) {
            upsertAll(entities)
        }
    }
}

@Dao
interface CharacterProgressDao {
    @Query("SELECT * FROM character_progress WHERE id = 1")
    fun observe(): Flow<CharacterProgressEntity?>

    @Query("SELECT * FROM character_progress WHERE id = 1")
    suspend fun get(): CharacterProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CharacterProgressEntity)
}

@Dao
interface CharacterRewardLogDao {
    @Query("SELECT * FROM character_reward_logs")
    suspend fun getAll(): List<CharacterRewardLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CharacterRewardLogEntity)

    @Query("DELETE FROM character_reward_logs WHERE taskId = :taskId AND date = :date")
    suspend fun delete(taskId: Long, date: String)

    @Query("DELETE FROM character_reward_logs")
    suspend fun clear()
}
