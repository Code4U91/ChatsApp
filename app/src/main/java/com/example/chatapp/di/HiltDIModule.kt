package com.example.chatapp.di

import android.content.Context
import androidx.room.Room
import com.example.chatapp.core.local_database.LocalRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltDIModule {




    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    @Singleton
    @Provides
    fun providesDb(@ApplicationContext context: Context): LocalRoomDatabase {

        return Room.databaseBuilder(context, LocalRoomDatabase::class.java, "chatDbTable")
            .fallbackToDestructiveMigration(false)
            .build()
    }

}