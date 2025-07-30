package com.example.chatapp.chat_feature.domain.repository

import com.example.chatapp.chat_feature.data.remote_source.model.ChatData
import com.example.chatapp.chat_feature.data.remote_source.model.MessageData
import com.example.chatapp.chat_feature.domain.model.Chat
import com.example.chatapp.chat_feature.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface LocalChatRepo {

    suspend fun insertChat(chats: List<ChatData>)

    suspend fun insertMessage(messages: List<MessageData>)

    suspend fun deleteMessagesById(chatId: String, messageIds : List<String>)

    fun getChats() : Flow<List<Chat>>

    fun getMessagesByChatId(chatId : String) : Flow<List<Message>>


}