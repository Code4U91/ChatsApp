package com.example.chatapp.chat_feature.domain.message_use_case

import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo

class MarkMessageAsSeen (
    private val messageHandlerRepo: MessageHandlerRepo
) {
    operator fun invoke(chatId : String, userId : String, friendId : String){

        messageHandlerRepo.markMessageAsSeen(
            chatId = chatId,
            currentUserId = userId,
            friendId = friendId
        )
    }
}