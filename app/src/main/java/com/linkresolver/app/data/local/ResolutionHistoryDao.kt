package com.linkresolver.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ResolutionHistoryDao {

    @Query("SELECT * FROM resolution_history ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ResolutionHistoryEntity>>

    @Insert
    suspend fun insert(entity: ResolutionHistoryEntity): Long

    @Delete
    suspend fun delete(entity: ResolutionHistoryEntity)

    @Query("DELETE FROM resolution_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM resolution_history")
    suspend fun clearAll()
}
