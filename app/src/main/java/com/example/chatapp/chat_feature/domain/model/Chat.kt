package com.example.chatapp.chat_feature.domain.model

data class Chat(

    val chatId: String,
    val otherUserId: String,
    val lastMessageTimeInMills: Long,
    val otherUserName: String,
    val profileUrl : String
)
