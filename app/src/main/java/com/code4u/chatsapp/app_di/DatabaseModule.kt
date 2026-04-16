package com.code4u.chatsapp.app_di

import android.content.Context
import androidx.room.Room
import com.code4u.chatsapp.core.database.LocalRoomDatabase
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
    fun providesDb(@ApplicationContext context: Context): LocalRoomDatabase {

        return Room.databaseBuilder(context, LocalRoomDatabase::class.java, "chatDbTable")
            .fallbackToDestructiveMigration(false) // only for testing
            .build()
    }
}