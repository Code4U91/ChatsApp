package com.code4u.chatsapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.code4u.chatsapp.call_feature.data.local_source.dao.CallHistoryDao
import com.code4u.chatsapp.call_feature.data.local_source.entity.CallHistoryEntity
import com.code4u.chatsapp.chat_feature.data.local_source.dao.ChatDao
import com.code4u.chatsapp.chat_feature.data.local_source.entity.ChatEntity
import com.code4u.chatsapp.chat_feature.data.local_source.entity.MessageEntity
import com.code4u.chatsapp.friend_feature.data.local_source.FriendDao
import com.code4u.chatsapp.friend_feature.data.local_source.FriendEntity
import com.code4u.chatsapp.profile_feature.data.local_source.Converters
import com.code4u.chatsapp.profile_feature.data.local_source.UserDao
import com.code4u.chatsapp.profile_feature.data.local_source.UserEntity

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