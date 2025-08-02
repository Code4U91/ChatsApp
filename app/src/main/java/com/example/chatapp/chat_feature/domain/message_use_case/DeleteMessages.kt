package com.example.chatapp.chat_feature.domain.message_use_case

import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo
import com.example.chatapp.chat_feature.domain.repository.MessageHandlerRepo

class DeleteMessages(
    private val messageHandlerRepo: MessageHandlerRepo,
    private val localChatRepo: LocalChatRepo
) {
    suspend operator fun invoke(
        chatId: String,
        messagesId : Set<String>,
        currentUserId : String
    ){
        messageHandlerRepo.deleteMessage(
            chatId = chatId,
            messageId = messagesId,
            currentUserId = currentUserId
        )

        localChatRepo.deleteMessagesById(chatId, messagesId.toList())
    }
}