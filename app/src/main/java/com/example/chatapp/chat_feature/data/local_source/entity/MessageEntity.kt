package com.example.chatapp.chat_feature.data.local_source.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,

    val chatId: String,
    val messageContent: String,
    val receiverId: String,
    val senderId: String,
    val status: String,
    val timeInMills: Long
)