package com.code4u.chatsapp.auth_feature.domain.usecase.online_state_case

import com.code4u.chatsapp.auth_feature.domain.repository.OnlineStatusRepo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class ListenForOnlineStatus(
    private val onlineStatusRepo: OnlineStatusRepo
) {
    operator fun invoke(
        userId: String,
        onStatusChanged: (Long) -> Unit
    ): Pair<DatabaseReference, ValueEventListener> {

        return onlineStatusRepo.listenForOnlineStatus(
            userId = userId,
            onStatusChanged = { timeInMills -> onStatusChanged(timeInMills) }
        )
    }
}