package com.code4u.chatsapp.friend_feature.domain.use_case

import com.code4u.chatsapp.friend_feature.domain.repository.RemoteFriendRepo

class AddFriend (
    private val remoteFriendRepo: RemoteFriendRepo
) {

    operator fun invoke(id : String, onSuccess : () -> Unit, onFailure : (Exception) -> Unit){

        remoteFriendRepo.addFriend(
            friendUserIdEmail = id,
            onSuccess = { onSuccess()},
            onFailure = {e -> onFailure(e)}
        )
    }
}