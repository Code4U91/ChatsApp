package com.example.chatapp.friend_feature.domain.repository

import com.example.chatapp.friend_feature.data.remote_source.FriendData
import kotlinx.coroutines.flow.Flow

interface RemoteFriendRepo {

    fun fetchFriendList() : Flow<List<FriendData>>

    fun addFriend(
        friendUserIdEmail: String, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit
    )

    suspend fun fetchFriendDataById(id: String): FriendData?

    fun syncOnlyVisibleFriendIds(visibleFriendIds : Set<String>): Flow<FriendData>

    fun deleteFriend(friendIds: Set<String>)

    suspend fun clearFriendDataListeners()
}