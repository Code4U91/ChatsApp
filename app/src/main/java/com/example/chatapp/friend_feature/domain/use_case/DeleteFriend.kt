package com.example.chatapp.friend_feature.domain.use_case

import com.example.chatapp.friend_feature.domain.repository.LocalFriendRepo
import com.example.chatapp.friend_feature.domain.repository.RemoteFriendRepo

class DeleteFriend (
    private val remoteFriendRepo: RemoteFriendRepo,
    private val localFriendRepo: LocalFriendRepo
) {

    suspend operator fun invoke(friendIds : Set<String>){

        if (friendIds.isNotEmpty()){

            remoteFriendRepo.deleteFriend(
                 friendIds = friendIds
            )

            localFriendRepo.deleteFriend(friendIds)
        }

    }
}