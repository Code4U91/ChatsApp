package com.example.chatapp.chat_feature.data.local_source.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("chats")
data class ChatEntity(

    @PrimaryKey val chatId: String,

    val otherUserId: String,
    val lastMessageTimeStamp: Long,
    val otherUserName: String,
    val profileUrl : String
)