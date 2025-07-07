package com.example.chatapp.localData.roomDbCache

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, ChatEntity::class, MessageEntity::class,
        FriendEntity::class, CallHistoryEntity::class],
    version = 4,
    exportSchema = false
)
abstract class LocalRoomDatabase : RoomDatabase() {

    abstract fun getDbDao() : Dao
}

