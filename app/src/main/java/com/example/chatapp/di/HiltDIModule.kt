package com.example.chatapp.di

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.example.chatapp.localData.roomDbCache.LocalRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
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
    fun providesFirebaseAuth() : FirebaseAuth{
        return FirebaseAuth.getInstance()
    }


    @Provides
    @Singleton
    fun providesSignClient(context :  Application) :  CredentialManager {
        return  CredentialManager.create(context)
    }

    @Provides
    @Singleton
    fun firestoreDb() : FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun realTimeDb() : FirebaseDatabase
    {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun firebaseMessaging(): FirebaseMessaging
    {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun provideHttpClient() : HttpClient{
        return HttpClient(OkHttp){
            install(ContentNegotiation){
                json()
            }
        }
    }

    @Singleton
    @Provides
    fun providesDb(@ApplicationContext context : Context) : LocalRoomDatabase {

        return  Room.databaseBuilder(context, LocalRoomDatabase::class.java, "chatDbTable")
            .fallbackToDestructiveMigration(false)
            .build()
    }
}