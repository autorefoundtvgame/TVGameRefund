package com.openhands.tvgamerefund.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.openhands.tvgamerefund.data.dao.GameDao
import com.openhands.tvgamerefund.data.dao.ShowDao
import com.openhands.tvgamerefund.data.dao.UserParticipationDao
import com.openhands.tvgamerefund.data.dao.UserVoteDao
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.GameFee
import com.openhands.tvgamerefund.data.models.GameStats
import com.openhands.tvgamerefund.data.models.Show
import com.openhands.tvgamerefund.data.models.ShowSchedule
import com.openhands.tvgamerefund.data.models.UserParticipation
import com.openhands.tvgamerefund.data.models.UserVote

@Database(
    entities = [
        Show::class,
        ShowSchedule::class,
        Game::class,
        GameFee::class,
        GameStats::class,
        UserParticipation::class,
        UserVote::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
    abstract fun gameDao(): GameDao
    abstract fun userParticipationDao(): UserParticipationDao
    abstract fun userVoteDao(): UserVoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tvgamerefund_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}