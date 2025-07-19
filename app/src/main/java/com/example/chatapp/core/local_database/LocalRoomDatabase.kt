package com.example.chatapp.core.local_database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chatapp.call_feature.data.local_source.dao.CallHistoryDao
import com.example.chatapp.call_feature.data.local_source.entity.CallHistoryEntity
import com.example.chatapp.localData.roomDbCache.ChatEntity
import com.example.chatapp.localData.roomDbCache.Dao
import com.example.chatapp.localData.roomDbCache.FriendEntity
import com.example.chatapp.localData.roomDbCache.MessageEntity
import com.example.chatapp.localData.roomDbCache.UserEntity

@Database(
    entities = [UserEntity::class, ChatEntity::class, MessageEntity::class,
        FriendEntity::class, CallHistoryEntity::class],
    version = 6,
    exportSchema = false
)
abstract class LocalRoomDatabase : RoomDatabase() {

    abstract fun getDbDao() : Dao

    abstract val callDao : CallHistoryDao
}