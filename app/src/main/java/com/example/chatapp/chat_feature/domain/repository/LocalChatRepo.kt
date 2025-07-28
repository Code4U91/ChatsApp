package com.example.chatapp.chat_feature.domain.repository

import com.example.chatapp.chat_feature.domain.model.Chat
import com.example.chatapp.chat_feature.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface LocalChatRepo {

    suspend fun insertChat(chats: List<Chat>)

    suspend fun insertMessage(messages: List<Message>)

    fun getChats() : Flow<List<Chat>>

    fun getMessagesByChatId(chatId : String) : Flow<List<Message>>


}