package com.code4u.chatsapp.chat_feature.domain.message_use_case

import com.code4u.chatsapp.chat_feature.domain.model.Message
import com.code4u.chatsapp.chat_feature.domain.repository.LocalChatRepo
import kotlinx.coroutines.flow.Flow

class GetMessage (
    private val localChatRepo: LocalChatRepo
) {
    operator fun invoke(chatId : String): Flow<List<Message>> {

        return localChatRepo.getMessagesByChatId(chatId)
    }
}