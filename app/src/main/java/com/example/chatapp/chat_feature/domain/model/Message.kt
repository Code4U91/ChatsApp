package com.example.chatapp.chat_feature.domain.model

data class Message(

    val messageId: String,
    val chatId: String,
    val messageContent: String,
    val receiverId: String,
    val senderId: String,
    val status: String,
    val timeInMills: Long
)
