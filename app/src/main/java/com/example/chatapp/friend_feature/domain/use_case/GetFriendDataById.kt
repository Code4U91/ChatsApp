package com.example.chatapp.friend_feature.domain.use_case

import com.example.chatapp.friend_feature.domain.model.Friend
import com.example.chatapp.friend_feature.domain.repository.LocalFriendRepo
import kotlinx.coroutines.flow.Flow

class GetFriendDataById (
    private val localFriendRepo: LocalFriendRepo
) {
    operator fun invoke(id : String): Flow<Friend?> {

        return localFriendRepo.getFriendDataById(id)
    }
}