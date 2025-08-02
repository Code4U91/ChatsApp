package com.example.chatapp.friend_feature.domain.use_case

import android.util.Log
import com.example.chatapp.friend_feature.domain.repository.LocalFriendRepo
import com.example.chatapp.friend_feature.domain.repository.RemoteFriendRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapMerge


class SyncFriendData (
    private val remoteFriendRepo: RemoteFriendRepo,
    private val localFriendRepo: LocalFriendRepo
) {

    // concurrency issue
    // change to listen to only visible friend data

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(){

        remoteFriendRepo.fetchFriendList()
            .distinctUntilChanged()
            .flatMapMerge { friendList ->

                Log.i("FRIEND_SYNC_LIST", friendList.toString())
                friendList.asFlow()
                    .flatMapMerge { friend ->
                    remoteFriendRepo.fetchRemoteFriendDataById(friend.uid)
                        .distinctUntilChanged()
                }
            }
            .collect { friendData ->
                Log.i("FRIEND_SYNC", friendData.toString())
                localFriendRepo.insertFriendData(friendData)
            }
    }
}