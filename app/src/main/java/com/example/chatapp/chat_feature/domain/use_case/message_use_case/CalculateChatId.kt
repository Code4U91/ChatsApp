package com.example.chatapp.chat_feature.domain.use_case.message_use_case

import com.example.chatapp.auth_feature.domain.usecase.auth_case.AuthUseCase
import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo

class CalculateChatId (
    private val messageHandlerRepo: MessageHandlerRepo,
    private val authUseCase: AuthUseCase
) {

    operator fun invoke(
        userId : String,
        friendUserId : String,

    ): String {
        return messageHandlerRepo.chatIdCreator(
            currentUserId = userId,
            friendUserId = friendUserId
        )
    }
}