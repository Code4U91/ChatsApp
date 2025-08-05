package com.example.chatapp.chat_feature.domain.message_use_case

import com.example.chatapp.chat_feature.domain.repository.GlobalMessageListenerRepo
import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo

class LoadOldMessageOnce(
    private val globalMessageListenerRepo: GlobalMessageListenerRepo,
    private val localChatRepo: LocalChatRepo
) {
    suspend operator fun invoke(
        isUserInChatScreen: (String) -> Boolean,
        chatId: String
    ){

        val messages =globalMessageListenerRepo.fetchMessagesOnceForChat(
            isUserInChatScreen = {id -> isUserInChatScreen(id)},
            chatId = chatId
        )

        localChatRepo.insertMessage(messages)
    }
}