package com.example.chatapp.localData.roomDbCache

import com.example.chatapp.core.local_database.LocalRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDbRepo @Inject constructor(
    private val localDb: LocalRoomDatabase
) {

    val chats = localDb.getDbDao().getChats()
    val friendList = localDb.getDbDao().getFriendList()
    val userData = localDb.getDbDao().getUserData()

    suspend fun insertUserData(userData: UserEntity){

        localDb.getDbDao().insertUser(userData)
    }

    suspend fun insertChats(chats: ChatEntity){
        localDb.getDbDao().insertChats(chats)
    }

    suspend fun insertMessages(messages : MessageEntity){
        localDb.getDbDao().insertMessages(messages)
    }

    suspend fun insertFriend(friendEntity: FriendEntity){
        localDb.getDbDao().insertFriend(friendEntity)
    }

    fun getMessages(chatId : String) : Flow<List<MessageEntity>> {

        return localDb.getDbDao().getMessages(chatId)
    }


    fun getFriendData(friendId : String) : Flow<FriendEntity?>{

        return localDb.getDbDao().getFriendData(friendId)
    }

    suspend fun clearAllTables(){
        withContext(Dispatchers.IO){
            localDb.clearAllTables()
        }

    }


}