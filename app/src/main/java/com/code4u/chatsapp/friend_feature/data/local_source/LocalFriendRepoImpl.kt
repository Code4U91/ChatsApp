package com.code4u.chatsapp.friend_feature.data.local_source

import com.code4u.chatsapp.friend_feature.data.remote_source.FriendData
import com.code4u.chatsapp.friend_feature.data.remote_source.toEntity
import com.code4u.chatsapp.friend_feature.domain.model.Friend
import com.code4u.chatsapp.friend_feature.domain.repository.LocalFriendRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalFriendRepoImpl (
    private val friendDao: FriendDao
) : LocalFriendRepo {

    override suspend fun insertFriendData(data : List<FriendData>) {

        friendDao.insertFriend(data.map { it.toEntity() })
    }

    override fun getFriendList():  Flow<List<Friend>> {
         return friendDao.getFriendList().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getFriendDataById(id : String): Flow<Friend?> {
        return friendDao.getFriendData(id).map {entity -> entity?.toDomain() }

    }

    override suspend fun deleteFriend(ids: Set<String>) {

        friendDao.deleteFriend(ids)
    }


}