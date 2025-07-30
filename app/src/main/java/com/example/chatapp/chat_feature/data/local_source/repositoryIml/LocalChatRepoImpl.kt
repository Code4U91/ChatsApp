package com.example.chatapp.chat_feature.data.local_source.repositoryIml

import com.example.chatapp.chat_feature.data.local_source.dao.ChatDao
import com.example.chatapp.chat_feature.data.local_source.mapper.toDomain
import com.example.chatapp.chat_feature.data.remote_source.mapper.toEntity
import com.example.chatapp.chat_feature.data.remote_source.model.ChatData
import com.example.chatapp.chat_feature.data.remote_source.model.MessageData
import com.example.chatapp.chat_feature.domain.model.Chat
import com.example.chatapp.chat_feature.domain.model.Message
import com.example.chatapp.chat_feature.domain.repository.LocalChatRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalChatRepoImpl(
    private val chatDao: ChatDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocalChatRepo {

    override suspend fun insertChat(chats: List<ChatData>) = withContext(dispatcher) {

        chatDao.insertChat(chats.map { it.toEntity() })

    }

    override suspend fun insertMessage(messages: List<MessageData>) = withContext(dispatcher) {

        chatDao.insertMessage(messages.map { it.toEntity() })

    }

    override suspend fun deleteMessagesById(
        chatId: String,
        messageIds: List<String>
    ) {
         chatDao.deleteMessageById(chatId, messageIds)
    }

    override fun getChats(): Flow<List<Chat>> {

        return chatDao.getChats().map { entity ->
            entity.map { it.toDomain() }
        }
    }

    override fun getMessagesByChatId(chatId: String): Flow<List<Message>> {

        return chatDao.getMessagesByChatId(chatId)
            .map { entities -> entities.map { it.toDomain() } }
    }


}