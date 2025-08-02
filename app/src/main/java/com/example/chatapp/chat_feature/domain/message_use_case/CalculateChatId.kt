package com.example.chatapp.chat_feature.domain.message_use_case

import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo

class CalculateChatId (
    private val messageHandlerRepo: MessageHandlerRepo
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