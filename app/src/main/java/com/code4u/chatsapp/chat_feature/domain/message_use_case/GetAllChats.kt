package com.code4u.chatsapp.chat_feature.domain.message_use_case

import com.code4u.chatsapp.chat_feature.domain.model.Chat
import com.code4u.chatsapp.chat_feature.domain.repository.LocalChatRepo
import kotlinx.coroutines.flow.Flow

class GetAllChats (
    private val localChatRepo: LocalChatRepo
) {

    operator fun invoke(): Flow<List<Chat>> {

       return localChatRepo.getChats()
    }
}