package com.openhands.tvgamerefund.data.repository

import com.openhands.tvgamerefund.data.dao.ShowDao
import com.openhands.tvgamerefund.data.models.Show
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepository @Inject constructor(
    private val showDao: ShowDao
) {
    fun getAllShows(): Flow<List<Show>> = showDao.getAllShows()
    
    fun getShowsByChannel(channel: String): Flow<List<Show>> = showDao.getShowsByChannel(channel)
    
    fun searchShows(query: String): Flow<List<Show>> = showDao.searchShows(query)
    
    suspend fun getShowById(id: String): Show? = showDao.getShowById(id)
    
    suspend fun insertShow(show: Show) = showDao.insertShow(show)
    
    suspend fun insertShows(shows: List<Show>) = showDao.insertShows(shows)
    
    suspend fun updateShow(show: Show) = showDao.updateShow(show)
    
    suspend fun deleteShow(show: Show) = showDao.deleteShow(show)
    
    suspend fun deleteShowById(id: String) = showDao.deleteShowById(id)
}