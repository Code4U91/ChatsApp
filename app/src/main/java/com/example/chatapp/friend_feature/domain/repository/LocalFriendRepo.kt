package com.example.chatapp.friend_feature.domain.repository

import com.example.chatapp.friend_feature.data.remote_source.FriendData
import com.example.chatapp.friend_feature.domain.model.Friend
import kotlinx.coroutines.flow.Flow

interface LocalFriendRepo {

    suspend fun insertFriendData(data : List<FriendData>)

    fun getFriendList() : Flow<List<Friend>>

    fun getFriendDataById(id : String) : Flow<Friend?>

    suspend fun deleteFriend(ids : Set<String>)
}