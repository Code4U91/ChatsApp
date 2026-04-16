package com.code4u.chatsapp.app_di

import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseAnalyticsModule {

    @Provides
    @Singleton
    fun providesFirebaseAnalytics(@ApplicationContext context: android.content.Context) : FirebaseAnalytics {

        return FirebaseAnalytics.getInstance(context)
    }
}