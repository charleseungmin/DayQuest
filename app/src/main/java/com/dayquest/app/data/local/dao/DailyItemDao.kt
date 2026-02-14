package com.dayquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.entity.DailyItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyItemDao {
    @Query("SELECT * FROM daily_items WHERE dateKey = :dateKey ORDER BY id ASC")
    fun observeByDate(dateKey: String): Flow<List<DailyItemEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: DailyItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<DailyItemEntity>): List<Long>

    @Update
    suspend fun update(item: DailyItemEntity)

    @Query("SELECT * FROM daily_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DailyItemEntity?

    @Query("UPDATE daily_items SET status = :status, completedAtEpochMillis = :completedAt, deferredToDateKey = :deferredToDateKey WHERE id = :id")
    suspend fun updateState(
        id: Long,
        status: DailyItemStatus,
        completedAt: Long?,
        deferredToDateKey: String?
    )

    @Query("SELECT COUNT(*) FROM daily_items WHERE dateKey = :dateKey")
    suspend fun countByDate(dateKey: String): Int

    @Query("SELECT COUNT(*) FROM daily_items WHERE dateKey = :dateKey AND status = :status")
    suspend fun countByDateAndStatus(dateKey: String, status: DailyItemStatus): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM daily_items di
        INNER JOIN tasks t ON t.id = di.taskId
        WHERE di.dateKey = :dateKey
          AND di.status = :status
          AND t.isImportant = 1
        """
    )
    suspend fun countImportantByDateAndStatus(dateKey: String, status: DailyItemStatus): Int
}
