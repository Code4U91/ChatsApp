package com.example.chatapp.friend_feature.domain.use_case

import android.util.Log
import com.example.chatapp.friend_feature.domain.repository.LocalFriendRepo
import com.example.chatapp.friend_feature.domain.repository.RemoteFriendRepo
import kotlinx.coroutines.flow.distinctUntilChanged


class SyncVisibleFriendData (
    private val remoteFriendRepo: RemoteFriendRepo,
    private val localFriendRepo: LocalFriendRepo
) {

    suspend operator fun invoke(visibleFriendIds : Set<String>){


        remoteFriendRepo.syncOnlyVisibleFriendIds(visibleFriendIds)
            .distinctUntilChanged()
            .collect { friendData ->

            Log.i("VISIBLE_FRIEND_DATA", friendData.toString())

            localFriendRepo.insertFriendData(listOf(friendData))
        }
    }
}