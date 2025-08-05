package com.example.chatapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chatapp.call_feature.data.local_source.dao.CallHistoryDao
import com.example.chatapp.call_feature.data.local_source.entity.CallHistoryEntity
import com.example.chatapp.chat_feature.data.local_source.dao.ChatDao
import com.example.chatapp.chat_feature.data.local_source.entity.ChatEntity
import com.example.chatapp.chat_feature.data.local_source.entity.MessageEntity
import com.example.chatapp.friend_feature.data.local_source.FriendDao
import com.example.chatapp.friend_feature.data.local_source.FriendEntity
import com.example.chatapp.profile_feature.data.local_source.Converters
import com.example.chatapp.profile_feature.data.local_source.UserDao
import com.example.chatapp.profile_feature.data.local_source.UserEntity

@Database(
    entities = [UserEntity::class, ChatEntity::class, MessageEntity::class,
        FriendEntity::class, CallHistoryEntity::class],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LocalRoomDatabase : RoomDatabase() {

    abstract fun callDao(): CallHistoryDao

    abstract fun getChatDao(): ChatDao

    abstract fun getUserDao() : UserDao

    abstract fun getFriendDao() : FriendDao
}