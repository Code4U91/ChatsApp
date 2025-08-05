package com.example.chatapp.friend_feature.di

import com.example.chatapp.core.database.LocalRoomDatabase
import com.example.chatapp.friend_feature.data.local_source.LocalFriendRepoImpl
import com.example.chatapp.friend_feature.data.remote_source.RemoteFriendRepoImpl
import com.example.chatapp.friend_feature.domain.repository.LocalFriendRepo
import com.example.chatapp.friend_feature.domain.repository.RemoteFriendRepo
import com.example.chatapp.friend_feature.domain.use_case.AddFriend
import com.example.chatapp.friend_feature.domain.use_case.DeleteFriend
import com.example.chatapp.friend_feature.domain.use_case.FriendUseCase
import com.example.chatapp.friend_feature.domain.use_case.GetFriendDataById
import com.example.chatapp.friend_feature.domain.use_case.GetFriendList
import com.example.chatapp.friend_feature.domain.use_case.SyncFriendData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FriendModule {

    @Provides
    @Singleton
    fun providesLocalFriendRepo(
        db : LocalRoomDatabase
    ) : LocalFriendRepo {
        return LocalFriendRepoImpl(db.getFriendDao())
    }

    @Provides
    @Singleton
    fun providesRemoteFriendRepo(
        firestoreDb : FirebaseFirestore,
        auth :  FirebaseAuth
    ) : RemoteFriendRepo {
        return RemoteFriendRepoImpl(
            firestoreDb = firestoreDb,
            auth = auth
        )
    }

    //----
    // PROVIDES USE CASE
    //----

    @Provides
    @Singleton
    fun providesFriendUseCase(
        localFriendRepo: LocalFriendRepo,
        remoteFriendRepo: RemoteFriendRepo
    ) : FriendUseCase{

        return FriendUseCase(
            addFriend = AddFriend(remoteFriendRepo),
            deleteFriend = DeleteFriend(remoteFriendRepo, localFriendRepo),
            getFriendDataById = GetFriendDataById(localFriendRepo),
            syncFriendData = SyncFriendData(remoteFriendRepo, localFriendRepo),
            getFriendList = GetFriendList(localFriendRepo)
        )

    }
}