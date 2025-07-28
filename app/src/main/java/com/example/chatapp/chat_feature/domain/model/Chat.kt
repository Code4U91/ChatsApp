package com.example.chatapp.chat_feature.domain.model

data class Chat(

    val chatId: String,
    val otherUserId: String,
    val lastMessageTimeStamp: Long,
    val otherUserName: String,
    val profileUrl : String
)
