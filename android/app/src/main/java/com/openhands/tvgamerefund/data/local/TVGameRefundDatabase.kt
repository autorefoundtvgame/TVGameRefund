package com.openhands.tvgamerefund.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.openhands.tvgamerefund.data.local.dao.GameDao
import com.openhands.tvgamerefund.data.local.dao.ParticipationDao
import com.openhands.tvgamerefund.data.local.entities.GameEntity
import com.openhands.tvgamerefund.data.local.entities.ParticipationEntity
import com.openhands.tvgamerefund.data.local.utils.Converters

@Database(
    entities = [
        GameEntity::class,
        ParticipationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TVGameRefundDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun participationDao(): ParticipationDao

    companion object {
        private const val DATABASE_NAME = "tvgamerefund.db"

        @Volatile
        private var INSTANCE: TVGameRefundDatabase? = null

        fun getInstance(context: Context): TVGameRefundDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): TVGameRefundDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TVGameRefundDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}