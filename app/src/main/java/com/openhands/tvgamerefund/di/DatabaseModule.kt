package com.openhands.tvgamerefund.di

import android.content.Context
import com.openhands.tvgamerefund.data.dao.GameDao
import com.openhands.tvgamerefund.data.dao.ShowDao
import com.openhands.tvgamerefund.data.dao.UserParticipationDao
import com.openhands.tvgamerefund.data.dao.UserVoteDao
import com.openhands.tvgamerefund.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideShowDao(database: AppDatabase): ShowDao {
        return database.showDao()
    }
    
    @Provides
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }
    
    @Provides
    fun provideUserParticipationDao(database: AppDatabase): UserParticipationDao {
        return database.userParticipationDao()
    }
    
    @Provides
    fun provideUserVoteDao(database: AppDatabase): UserVoteDao {
        return database.userVoteDao()
    }
}