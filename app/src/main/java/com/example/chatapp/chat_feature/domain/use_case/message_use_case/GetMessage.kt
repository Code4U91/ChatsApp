package com.example.chatapp.chat_feature.domain.use_case.message_use_case

import com.example.chatapp.chat_feature.domain.model.Message
import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo
import kotlinx.coroutines.flow.Flow

class GetMessage (
    private val localChatRepo: LocalChatRepo
) {
    operator fun invoke(chatId : String): Flow<List<Message>> {

        return localChatRepo.getMessagesByChatId(chatId)
    }
}