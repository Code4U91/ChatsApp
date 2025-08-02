package com.example.chatapp.chat_feature.data.local_source.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chatapp.chat_feature.data.local_source.entity.ChatEntity
import com.example.chatapp.chat_feature.data.local_source.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chatEntity: List<ChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(messageEntity: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE messageId IN (:messageIds) AND chatId = :chatId")
    suspend fun deleteMessageById(chatId : String, messageIds: List<String>)

    @Query("SELECT * FROM chats ORDER BY lastMessageTimeInMills DESC")
    fun getChats() : Flow<List<ChatEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timeInMills DESC")
    fun getMessagesByChatId(chatId: String) : Flow<List<MessageEntity>>

}