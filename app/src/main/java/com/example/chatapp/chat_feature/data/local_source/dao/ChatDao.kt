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

    @Query("SELECT * FROM chats ORDER BY lastMessageTimeStamp DESC")
    fun getChats() : Flow<List<ChatEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timeStamp DESC")
    fun getMessagesByChatId(chatId: String) : Flow<List<MessageEntity>>

}