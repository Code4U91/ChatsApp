package com.example.chatapp.localData.roomDbCache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userEntity: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chatEntity: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messageEntity: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friendEntity: FriendEntity)

    @Insert
    suspend fun insertCallHistory(callHistoryEntity: CallHistoryEntity)

    // Room handles threading for Flow-returning queries, so no need to specifically make it suspend
    @Query ("SELECT * FROM user LIMIT 1")
    fun getUserData() : Flow<UserEntity?>

    @Query("SELECT * FROM chats ORDER BY lastMessageTimeStamp DESC")
    fun getChats() : Flow<List<ChatEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timeStamp DESC")
    fun getMessages(chatId: String) : Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages")
    fun getAllMessages() : Flow<List<MessageEntity>>

    @Query("SELECT * FROM callHistory")
    fun getCallHistory() : Flow<List<CallHistoryEntity>>

    @Query("SELECT * FROM friends ORDER BY LOWER(friendName) ASC")
    fun getFriendList() : Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE friendId = :friendId")
    fun getFriendData(friendId: String) : Flow<FriendEntity?>

}