package com.dayquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dayquest.app.core.model.DailyItemStatus
import com.dayquest.app.data.local.entity.DailyItemEntity
import com.dayquest.app.data.local.projection.HistoryDailyProgressRow
import com.dayquest.app.data.local.projection.TodayTaskRow
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyItemDao {
    @Query("SELECT * FROM daily_items WHERE dateKey = :dateKey ORDER BY id ASC")
    fun observeByDate(dateKey: String): Flow<List<DailyItemEntity>>

    @Query(
        """
        SELECT di.id AS dailyItemId,
               di.taskId AS sourceTaskId,
               t.title AS title,
               COALESCE(t.description, '일반') AS category,
               di.status AS status
        FROM daily_items di
        INNER JOIN tasks t ON t.id = di.taskId
        WHERE di.dateKey = :dateKey
        ORDER BY di.id ASC
        """
    )
    fun observeTodayTasks(dateKey: String): Flow<List<TodayTaskRow>>

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

    @Query("SELECT COUNT(*) FROM daily_items WHERE dateKey = :dateKey")
    fun observeCountByDate(dateKey: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_items WHERE dateKey = :dateKey AND status = :status")
    suspend fun countByDateAndStatus(dateKey: String, status: DailyItemStatus): Int

    @Query("SELECT COUNT(*) FROM daily_items WHERE dateKey = :dateKey AND status = :status")
    fun observeCountByDateAndStatus(dateKey: String, status: DailyItemStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_items WHERE dateKey BETWEEN :startDateKey AND :endDateKey AND status = :status")
    fun observeCountByDateRangeAndStatus(
        startDateKey: String,
        endDateKey: String,
        status: DailyItemStatus
    ): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_items WHERE dateKey BETWEEN :startDateKey AND :endDateKey")
    fun observeCountByDateRange(
        startDateKey: String,
        endDateKey: String
    ): Flow<Int>

    @Query(
        """
        SELECT dateKey AS dateKey,
               COUNT(*) AS totalCount,
               SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS doneCount
        FROM daily_items
        WHERE dateKey BETWEEN :startDateKey AND :endDateKey
        GROUP BY dateKey
        ORDER BY dateKey DESC
        """
    )
    fun observeDailyProgressByDateRange(
        startDateKey: String,
        endDateKey: String
    ): Flow<List<HistoryDailyProgressRow>>

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
