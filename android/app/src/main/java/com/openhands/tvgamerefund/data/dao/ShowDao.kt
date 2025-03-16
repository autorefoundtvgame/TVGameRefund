package com.openhands.tvgamerefund.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.openhands.tvgamerefund.data.models.Show
import kotlinx.coroutines.flow.Flow

@Dao
interface ShowDao {
    @Query("SELECT * FROM shows ORDER BY title ASC")
    fun getAllShows(): Flow<List<Show>>
    
    @Query("SELECT * FROM shows WHERE id = :id")
    suspend fun getShowById(id: String): Show?
    
    @Query("SELECT * FROM shows WHERE channel = :channel ORDER BY title ASC")
    fun getShowsByChannel(channel: String): Flow<List<Show>>
    
    @Query("SELECT * FROM shows WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchShows(query: String): Flow<List<Show>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShow(show: Show)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShows(shows: List<Show>)
    
    @Update
    suspend fun updateShow(show: Show)
    
    @Delete
    suspend fun deleteShow(show: Show)
    
    @Query("DELETE FROM shows WHERE id = :id")
    suspend fun deleteShowById(id: String)
}