package com.openhands.tvgamerefund.di

import com.openhands.tvgamerefund.BuildConfig
import com.openhands.tvgamerefund.data.api.BackendApi
import com.openhands.tvgamerefund.data.api.TMDbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("tmdbRetrofit")
    fun provideTMDbRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("backendRetrofit")
    fun provideBackendRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // Utiliser directement l'URL de l'API yomazone.com
        val backendUrl = "https://api.yomazone.com/"
        
        return Retrofit.Builder()
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTMDbApi(@Named("tmdbRetrofit") retrofit: Retrofit): TMDbApi {
        return retrofit.create(TMDbApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideBackendApi(@Named("backendRetrofit") retrofit: Retrofit): BackendApi {
        return retrofit.create(BackendApi::class.java)
    }
}