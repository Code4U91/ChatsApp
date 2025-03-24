package com.example.chatapp.di

import android.app.Application
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

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
}