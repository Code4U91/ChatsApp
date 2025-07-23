package com.example.chatapp.auth_feature.domain.usecase.online_state_case

import com.example.chatapp.auth_feature.domain.repository.OnlineStatusRepo

class SetActiveChatUseCase (
    private val onlineStatusRepo: OnlineStatusRepo
) {

    operator fun invoke(chatId: String){

        onlineStatusRepo.activeChatUpdate(chatId)
    }
}