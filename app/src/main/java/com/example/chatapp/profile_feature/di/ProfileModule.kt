package com.example.chatapp.profile_feature.di

import com.example.chatapp.core.database.LocalRoomDatabase
import com.example.chatapp.profile_feature.data.local_source.LocalProfileRepoImpl
import com.example.chatapp.profile_feature.data.remote_source.RemoteProfileRepoImpl
import com.example.chatapp.profile_feature.domain.repository.LocalProfileRepo
import com.example.chatapp.profile_feature.domain.repository.RemoteProfileRepo
import com.example.chatapp.profile_feature.domain.use_case.ClearLocalDbUseCase
import com.example.chatapp.profile_feature.domain.use_case.ClearUserDataListener
import com.example.chatapp.profile_feature.domain.use_case.FetchUserDataOnce
import com.example.chatapp.profile_feature.domain.use_case.GetUserData
import com.example.chatapp.profile_feature.domain.use_case.SyncUserData
import com.example.chatapp.profile_feature.domain.use_case.UserDataUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides
    @Singleton
    fun providesLocalProfileRepo(
        db : LocalRoomDatabase
    ) : LocalProfileRepo {
        return LocalProfileRepoImpl(
            userDao = db.getUserDao(),
            db = db
        )
    }

    @Provides
    @Singleton
    fun providesRemoteProfileRepo(
        firestoreDb : FirebaseFirestore,
        auth :  FirebaseAuth
    ) : RemoteProfileRepo{
        return RemoteProfileRepoImpl(
            firestoreDb = firestoreDb,
            auth = auth
        )
    }

    //---
    // PROVIDES USE CASE
    // ---

    @Provides
    @Singleton
    fun providesUserDataUseCase(
        localProfileRepo: LocalProfileRepo,
        remoteProfileRepo: RemoteProfileRepo
    ) : UserDataUseCase {
        return UserDataUseCase(
            getUserData = GetUserData(localProfileRepo),
            syncUserData = SyncUserData(localProfileRepo, remoteProfileRepo),
            clearLocalDbUseCase = ClearLocalDbUseCase(localProfileRepo),
            clearUserDataListener = ClearUserDataListener(remoteProfileRepo),
            fetchUserDataOnce = FetchUserDataOnce(remoteProfileRepo,localProfileRepo)
        )
    }


}