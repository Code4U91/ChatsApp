package com.example.chatapp.chat_feature.domain.use_case.message_use_case

import com.example.chatapp.chat_feature.domain.model.Chat
import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo
import kotlinx.coroutines.flow.Flow

class GetAllChats (
    private val localChatRepo: LocalChatRepo
) {

    operator fun invoke(): Flow<List<Chat>> {

       return localChatRepo.getChats()
    }
}