package com.openhands.tvgamerefund.di

import android.content.Context
import com.openhands.tvgamerefund.data.network.FreeApiService
import com.openhands.tvgamerefund.data.network.FreeAuthManager
import com.openhands.tvgamerefund.data.network.TMDbService
import com.openhands.tvgamerefund.data.scraper.JsoupTF1GameScraper
import com.openhands.tvgamerefund.data.scraper.TF1GameScraper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideFreeApiService(okHttpClient: OkHttpClient): FreeApiService {
        return Retrofit.Builder()
            .baseUrl("https://mobile.free.fr/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FreeApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideFreeAuthManager(
        okHttpClient: OkHttpClient,
        @ApplicationContext context: Context
    ): FreeAuthManager {
        return FreeAuthManager(okHttpClient, context)
    }
    
    @Provides
    @Singleton
    fun provideTF1GameScraper(okHttpClient: OkHttpClient): com.openhands.tvgamerefund.data.scraper.TF1GameScraper {
        return com.openhands.tvgamerefund.data.scraper.TF1GameScraper(okHttpClient)
    }
    
    @Provides
    @Singleton
    fun provideJsoupTF1GameScraper(): com.openhands.tvgamerefund.data.scraper.JsoupTF1GameScraper {
        return com.openhands.tvgamerefund.data.scraper.JsoupTF1GameScraper()
    }
    
    @Provides
    @Singleton
    fun provideTMDbService(okHttpClient: OkHttpClient): TMDbService {
        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TMDbService::class.java)
    }
}