package com.example.chatapp.auth_feature.domain.usecase.online_state_case

import com.example.chatapp.auth_feature.domain.repository.OnlineStatusRepo

class SetOnlineStatus (
    private val onlineStatusRepo: OnlineStatusRepo
) {
    operator fun invoke(status: Boolean, chatId: String = ""){

        onlineStatusRepo.setOnlineStatusWithDisconnect(status, chatId)

    }
}