package com.example.chatapp.friend_feature.domain.repository

import com.example.chatapp.friend_feature.data.remote_source.FriendData
import kotlinx.coroutines.flow.Flow

interface RemoteFriendRepo {

    fun fetchFriendList() : Flow<List<FriendData>>

    fun fetchRemoteFriendDataById(id : String) : Flow<FriendData>

    fun addFriend(
        friendUserIdEmail: String, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit
    )

    fun deleteFriend(friendIds: Set<String>)
}